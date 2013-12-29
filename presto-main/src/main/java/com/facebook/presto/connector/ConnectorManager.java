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
package com.facebook.presto.connector;

import com.facebook.presto.metadata.HandleResolver;
import com.facebook.presto.metadata.MetadataManager;
import com.facebook.presto.metadata.OutputTableHandleResolver;
import com.facebook.presto.operator.RecordSinkManager;
import com.facebook.presto.spi.Connector;
import com.facebook.presto.spi.ConnectorFactory;
import com.facebook.presto.spi.ConnectorHandleResolver;
import com.facebook.presto.spi.ConnectorMetadata;
import com.facebook.presto.spi.ConnectorOutputHandleResolver;
import com.facebook.presto.spi.ConnectorRecordSetProvider;
import com.facebook.presto.spi.ConnectorRecordSinkProvider;
import com.facebook.presto.spi.ConnectorSplitManager;
import com.facebook.presto.split.ConnectorDataStreamProvider;
import com.facebook.presto.split.DataStreamManager;
import com.facebook.presto.split.RecordSetDataStreamProvider;
import com.facebook.presto.split.SplitManager;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javax.annotation.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ConnectorManager
{
    private final MetadataManager metadataManager;
    private final SplitManager splitManager;
    private final DataStreamManager dataStreamManager;
    private final RecordSinkManager recordSinkManager;
    private final HandleResolver handleResolver;
    private final OutputTableHandleResolver outputTableHandleResolver;

    private final ConcurrentMap<String, ConnectorFactory> connectorFactories = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Connector> connectors = new ConcurrentHashMap<>();

    @Inject
    public ConnectorManager(MetadataManager metadataManager,
            SplitManager splitManager,
            DataStreamManager dataStreamManager,
            RecordSinkManager recordSinkManager,
            HandleResolver handleResolver,
            OutputTableHandleResolver outputTableHandleResolver,
            Map<String, ConnectorFactory> connectorFactories,
            Map<String, Connector> globalConnectors)
    {
        this.metadataManager = metadataManager;
        this.splitManager = splitManager;
        this.dataStreamManager = dataStreamManager;
        this.recordSinkManager = recordSinkManager;
        this.handleResolver = handleResolver;
        this.outputTableHandleResolver = outputTableHandleResolver;
        this.connectorFactories.putAll(connectorFactories);

        // add the global connectors
        for (Entry<String, Connector> entry : globalConnectors.entrySet()) {
            addGlobalConnector(entry.getKey(), entry.getValue());
        }
    }

    public void addConnectorFactory(ConnectorFactory connectorFactory)
    {
        ConnectorFactory existingConnectorFactory = connectorFactories.putIfAbsent(connectorFactory.getName(), connectorFactory);
        checkArgument(existingConnectorFactory == null, "Connector %s is already registered", connectorFactory.getName());
    }

    public synchronized void createConnection(String catalogName, String connectorName, Map<String, String> properties)
    {
        checkNotNull(catalogName, "catalogName is null");
        checkNotNull(connectorName, "connectorName is null");
        checkNotNull(properties, "properties is null");

        // for now connectorId == catalogName
        String connectorId = catalogName;
        checkState(!connectors.containsKey(connectorId), "A connector %s already exists", connectorId);

        ConnectorFactory connectorFactory = connectorFactories.get(connectorName);
        Preconditions.checkArgument(connectorFactory != null, "No factory for connector %s", connectorName);

        Connector connector = connectorFactory.create(connectorId, properties);
        connectors.put(connectorId, connector);

        addConnector(catalogName, connectorId, connector);
    }

    public void addGlobalConnector(String connectorId, Connector connector)
    {
        addConnector(null, connectorId, connector);
    }

    private void addConnector(@Nullable String catalogName, String connectorId, Connector connector)
    {
        ConnectorMetadata connectorMetadata = connector.getService(ConnectorMetadata.class);
        checkState(connectorMetadata != null, "Connector %s can not provide metadata", connectorId);

        ConnectorSplitManager connectorSplitManager = connector.getService(ConnectorSplitManager.class);
        checkState(connectorSplitManager != null, "Connector %s does not have a split manager", connectorId);

        ConnectorDataStreamProvider connectorDataStreamProvider = connector.getService(ConnectorDataStreamProvider.class);
        if (connectorDataStreamProvider == null) {
            ConnectorRecordSetProvider connectorRecordSetProvider = connector.getService(ConnectorRecordSetProvider.class);
            checkState(connectorRecordSetProvider != null, "Connector %s does not have a data stream provider", connectorId);
            connectorDataStreamProvider = new RecordSetDataStreamProvider(connectorRecordSetProvider);
        }

        ConnectorHandleResolver connectorHandleResolver = connector.getService(ConnectorHandleResolver.class);

        if (catalogName != null) {
            metadataManager.addConnectorMetadata(connectorId, catalogName, connectorMetadata);
        }
        else {
            metadataManager.addInternalSchemaMetadata(connectorId, connectorMetadata);
        }

        handleResolver.addHandleResolver(connectorId, connectorHandleResolver);
        splitManager.addConnectorSplitManager(connectorSplitManager);
        dataStreamManager.addConnectorDataStreamProvider(connectorDataStreamProvider);

        ConnectorRecordSinkProvider connectorRecordSinkProvider = connector.getService(ConnectorRecordSinkProvider.class);
        if (connectorRecordSinkProvider != null) {
            recordSinkManager.addConnectorRecordSinkProvider(connectorRecordSinkProvider);
        }

        ConnectorOutputHandleResolver connectorOutputHandleResolver = connector.getService(ConnectorOutputHandleResolver.class);
        if (connectorOutputHandleResolver != null) {
            outputTableHandleResolver.addHandleResolver(connectorId, connectorOutputHandleResolver);
        }
    }
}
