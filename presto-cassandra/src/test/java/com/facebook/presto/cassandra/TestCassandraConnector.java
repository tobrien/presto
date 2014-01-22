/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.cassandra;

import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.Connector;
import com.facebook.presto.spi.ConnectorHandleResolver;
import com.facebook.presto.spi.ConnectorMetadata;
import com.facebook.presto.spi.ConnectorRecordSetProvider;
import com.facebook.presto.spi.ConnectorSplitManager;
import com.facebook.presto.spi.ConnectorTableMetadata;
import com.facebook.presto.spi.PartitionResult;
import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.SchemaNotFoundException;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.SchemaTablePrefix;
import com.facebook.presto.spi.Split;
import com.facebook.presto.spi.SplitSource;
import com.facebook.presto.spi.TableHandle;
import com.facebook.presto.spi.TupleDomain;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.cassandraunit.model.StrategyModel;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static io.airlift.testing.Assertions.assertInstanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestCassandraConnector
{
    protected static final String INVALID_DATABASE = "totally_invalid_database";

    private ConnectorMetadata metadata;
    private ConnectorSplitManager splitManager;
    private ConnectorRecordSetProvider recordSetProvider;

    protected String database;
    protected SchemaTableName table;
    protected SchemaTableName tableUnpartitioned;
    protected SchemaTableName invalidTable;

    @BeforeClass
    public void setup()
            throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();

        createTestData();

        String connectorId = "cassandra-test";
        CassandraConnectorFactory connectorFactory = new CassandraConnectorFactory(
                connectorId,
                ImmutableMap.<String, String>of("node.environment", "test"));

        Connector connector = connectorFactory.create(connectorId, ImmutableMap.<String, String>of(
                "cassandra.contact-points", "localhost",
                "cassandra.native-protocol-port", "9142"));

        metadata = connector.getService(ConnectorMetadata.class);
        assertInstanceOf(metadata, CassandraMetadata.class);

        splitManager = connector.getService(ConnectorSplitManager.class);
        assertInstanceOf(splitManager, CassandraSplitManager.class);

        recordSetProvider = connector.getService(ConnectorRecordSetProvider.class);
        assertInstanceOf(recordSetProvider, CassandraRecordSetProvider.class);

        ConnectorHandleResolver handleResolver = connector.getService(ConnectorHandleResolver.class);
        assertInstanceOf(handleResolver, CassandraHandleResolver.class);

        database = "presto_database";
        table = new SchemaTableName(database, "presto_test");
        tableUnpartitioned = new SchemaTableName(database, "presto_test_unpartitioned");
        invalidTable = new SchemaTableName(database, "totally_invalid_table_name");
    }

    @AfterMethod
    public void tearDown()
            throws Exception
    {
        // todo how to stop cassandra
    }

    @Test
    public void testGetClient()
    {
    }

    @Test
    public void testGetDatabaseNames()
            throws Exception
    {
        List<String> databases = metadata.listSchemaNames();
        assertTrue(databases.contains(database));
    }

    @Test
    public void testGetTableNames()
            throws Exception
    {
        List<SchemaTableName> tables = metadata.listTables(database);
        assertTrue(tables.contains(table));
    }

    // disabled until metadata manager is updated to handle invalid catalogs and schemas
    @Test(enabled = false, expectedExceptions = SchemaNotFoundException.class)
    public void testGetTableNamesException()
            throws Exception
    {
        metadata.listTables(INVALID_DATABASE);
    }

    @Test
    public void testListUnknownSchema()
    {
        assertNull(metadata.getTableHandle(new SchemaTableName("totally_invalid_database_name", "dual")));
        assertEquals(metadata.listTables("totally_invalid_database_name"), ImmutableList.of());
        assertEquals(metadata.listTableColumns(new SchemaTablePrefix("totally_invalid_database_name", "dual")), ImmutableMap.of());
    }

    @Test
    public void testGetRecords()
            throws Exception
    {
        TableHandle tableHandle = getTableHandle(table);
        ConnectorTableMetadata tableMetadata = metadata.getTableMetadata(tableHandle);
        List<ColumnHandle> columnHandles = ImmutableList.copyOf(metadata.getColumnHandles(tableHandle).values());
        Map<String, Integer> columnIndex = indexColumns(columnHandles);

        PartitionResult partitionResult = splitManager.getPartitions(tableHandle, TupleDomain.all());
        List<Split> splits = getAllSplits(splitManager.getPartitionSplits(tableHandle, partitionResult.getPartitions()));

        long rowNumber = 0;
        for (Split split : splits) {
            CassandraSplit cassandraSplit = (CassandraSplit) split;

            long completedBytes = 0;
            try (RecordCursor cursor = recordSetProvider.getRecordSet(cassandraSplit, columnHandles).cursor()) {
                while (cursor.advanceNextPosition()) {
                    try {
                        assertReadFields(cursor, tableMetadata.getColumns());
                    }
                    catch (RuntimeException e) {
                        throw new RuntimeException("row " + rowNumber, e);
                    }

                    rowNumber++;

                    String keyValue = toUtf8String(cursor.getString(columnIndex.get("key")));
                    assertTrue(keyValue.startsWith("key "));
                    int rowId = Integer.parseInt(keyValue.substring(4));

                    assertEquals(keyValue, String.format("key %04d", rowId));
                    assertEquals(toUtf8String(cursor.getString(columnIndex.get("t_utf8"))), "utf8 " + rowId);

                    // bytes are encoded as a hex string for some reason
                    assertEquals(toUtf8String(cursor.getString(columnIndex.get("t_bytes"))), String.format("0x%08X", rowId));

                    // VARINT is returned as a string
                    assertEquals(toUtf8String(cursor.getString(columnIndex.get("t_integer"))), String.valueOf(rowId));

                    assertEquals(cursor.getLong(columnIndex.get("t_long")), 1000 + rowId);

                    assertEquals(toUtf8String(cursor.getString(columnIndex.get("t_uuid"))), String.format("00000000-0000-0000-0000-%012d", rowId));

                    // lexical UUIDs are encoded as a hex string for some reason
                    assertEquals(toUtf8String(cursor.getString(columnIndex.get("t_lexical_uuid"))), String.format("0x%032X", rowId));

                    long newCompletedBytes = cursor.getCompletedBytes();
                    assertTrue(newCompletedBytes >= completedBytes);
                    completedBytes = newCompletedBytes;
                }
            }
        }
        assertEquals(rowNumber, 9);
    }

    private String toUtf8String(byte[] keys)
    {
        return new String(keys, Charsets.UTF_8);
    }

    private static void assertReadFields(RecordCursor cursor, List<ColumnMetadata> schema)
    {
        for (int columnIndex = 0; columnIndex < schema.size(); columnIndex++) {
            ColumnMetadata column = schema.get(columnIndex);
            if (!cursor.isNull(columnIndex)) {
                switch (column.getType()) {
                    case BOOLEAN:
                        cursor.getBoolean(columnIndex);
                        break;
                    case LONG:
                        cursor.getLong(columnIndex);
                        break;
                    case DOUBLE:
                        cursor.getDouble(columnIndex);
                        break;
                    case STRING:
                        try {
                            cursor.getString(columnIndex);
                        }
                        catch (RuntimeException e) {
                            throw new RuntimeException("column " + column, e);
                        }
                        break;
                    default:
                        fail("Unknown primitive type " + columnIndex);
                }
            }
        }
    }

    private TableHandle getTableHandle(SchemaTableName tableName)
    {
        TableHandle handle = metadata.getTableHandle(tableName);
        checkArgument(handle != null, "table not found: %s", tableName);
        return handle;
    }

    private static List<Split> getAllSplits(SplitSource splitSource)
            throws InterruptedException
    {
        ImmutableList.Builder<Split> splits = ImmutableList.builder();
        while (!splitSource.isFinished()) {
            List<Split> batch = splitSource.getNextBatch(1000);
            splits.addAll(batch);
        }
        return splits.build();
    }

    private static ImmutableMap<String, Integer> indexColumns(List<ColumnHandle> columnHandles)
    {
        ImmutableMap.Builder<String, Integer> index = ImmutableMap.builder();
        int i = 0;
        for (ColumnHandle columnHandle : columnHandles) {
            checkArgument(columnHandle instanceof CassandraColumnHandle, "columnHandle is not an instance of CassandraColumnHandle");
            CassandraColumnHandle hiveColumnHandle = (CassandraColumnHandle) columnHandle;
            index.put(hiveColumnHandle.getName(), i);
            i++;
        }
        return index.build();
    }

    public static void createTestData()
    {
        String clusterName = "TestCluster";
        String host = "localhost:9171";

        Cluster cluster = HFactory.getOrCreateCluster(clusterName, host);
        Keyspace keyspace = HFactory.createKeyspace("beautifulKeyspaceName", cluster);
        assertNotNull(keyspace);

        String keyspaceName = "presto_database";
        String columnFamilyName = "presto_test";
        List<ColumnFamilyDefinition> columnFamilyDefinitions = createColumnFamilyDefinitions(keyspaceName, columnFamilyName);
        KeyspaceDefinition keyspaceDefinition = HFactory.createKeyspaceDefinition(
                keyspaceName,
                StrategyModel.SIMPLE_STRATEGY.value(),
                1,
                columnFamilyDefinitions);

        if (cluster.describeKeyspace(keyspaceName) != null) {
            cluster.dropKeyspace(keyspaceName, true);
        }
        cluster.addKeyspace(keyspaceDefinition, true);
        keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());

        long timestamp = System.currentTimeMillis();
        for (int rowNumber = 1; rowNumber < 10; rowNumber++) {
            addRow(columnFamilyName, mutator, timestamp, rowNumber);
        }
        mutator.execute();
    }

    private static void addRow(String columnFamilyName, Mutator<String> mutator, long timestamp, int rowNumber)
    {
        String key = String.format("key %04d", rowNumber);
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_utf8",
                        "utf8 " + rowNumber,
                        timestamp,
                        StringSerializer.get(),
                        StringSerializer.get()));
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_bytes",
                        Ints.toByteArray(rowNumber),
                        timestamp,
                        StringSerializer.get(),
                        BytesArraySerializer.get()));
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_integer",
                        rowNumber,
                        timestamp,
                        StringSerializer.get(),
                        IntegerSerializer.get()));
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_long",
                        1000L + rowNumber,
                        timestamp,
                        StringSerializer.get(),
                        LongSerializer.get()));
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_uuid",
                        UUID.fromString(String.format("00000000-0000-0000-0000-%012d", rowNumber)),
                        timestamp,
                        StringSerializer.get(),
                        UUIDSerializer.get()));
        mutator.addInsertion(
                key,
                columnFamilyName,
                HFactory.createColumn(
                        "t_lexical_uuid",
                        UUID.fromString(String.format("00000000-0000-0000-0000-%012d", rowNumber)),
                        timestamp,
                        StringSerializer.get(),
                        UUIDSerializer.get()));
    }

    private static List<ColumnFamilyDefinition> createColumnFamilyDefinitions(String keyspaceName, String columnFamilyName)
    {
        List<ColumnFamilyDefinition> columnFamilyDefinitions = new ArrayList<>();

        ImmutableList.Builder<ColumnDefinition> columnsDefinition = ImmutableList.builder();

        columnsDefinition.add(createColumnDefinition("t_utf8", ComparatorType.UTF8TYPE));
        columnsDefinition.add(createColumnDefinition("t_bytes", ComparatorType.BYTESTYPE));
        columnsDefinition.add(createColumnDefinition("t_integer", ComparatorType.INTEGERTYPE));
        columnsDefinition.add(createColumnDefinition("t_int32", ComparatorType.INT32TYPE));
        columnsDefinition.add(createColumnDefinition("t_long", ComparatorType.LONGTYPE));
        columnsDefinition.add(createColumnDefinition("t_boolean", ComparatorType.BOOLEANTYPE));
        columnsDefinition.add(createColumnDefinition("t_uuid", ComparatorType.UUIDTYPE));
        columnsDefinition.add(createColumnDefinition("t_lexical_uuid", ComparatorType.LEXICALUUIDTYPE));

        ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(
                keyspaceName,
                columnFamilyName,
                ComparatorType.UTF8TYPE,
                columnsDefinition.build());

        cfDef.setColumnType(ColumnType.STANDARD);
        cfDef.setComment("presto test table");

        cfDef.setKeyValidationClass(ComparatorType.UTF8TYPE.getTypeName());

        columnFamilyDefinitions.add(cfDef);

        return columnFamilyDefinitions;
    }

    private static BasicColumnDefinition createColumnDefinition(String columnName, ComparatorType type)
    {
        BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
        columnDefinition.setName(ByteBuffer.wrap(columnName.getBytes(Charsets.UTF_8)));
        columnDefinition.setValidationClass(type.getClassName());
        return columnDefinition;
    }
}
