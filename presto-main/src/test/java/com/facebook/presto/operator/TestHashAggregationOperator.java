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
package com.facebook.presto.operator;

import com.facebook.presto.execution.TaskId;
import com.facebook.presto.operator.HashAggregationOperator.HashAggregationOperatorFactory;
import com.facebook.presto.sql.analyzer.Session;
import com.facebook.presto.sql.planner.plan.AggregationNode.Step;
import com.facebook.presto.sql.tree.Input;
import com.facebook.presto.util.MaterializedResult;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import io.airlift.units.DataSize;
import io.airlift.units.DataSize.Unit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.facebook.presto.operator.AggregationFunctionDefinition.aggregation;
import static com.facebook.presto.operator.OperatorAssertion.assertOperatorEqualsIgnoreOrder;
import static com.facebook.presto.operator.OperatorAssertion.toPages;
import static com.facebook.presto.operator.RowPagesBuilder.rowPagesBuilder;
import static com.facebook.presto.operator.aggregation.AverageAggregations.LONG_AVERAGE;
import static com.facebook.presto.operator.aggregation.CountAggregation.COUNT;
import static com.facebook.presto.operator.aggregation.CountColumnAggregations.COUNT_BOOLEAN_COLUMN;
import static com.facebook.presto.operator.aggregation.CountColumnAggregations.COUNT_STRING_COLUMN;
import static com.facebook.presto.operator.aggregation.LongSumAggregation.LONG_SUM;
import static com.facebook.presto.operator.aggregation.VarBinaryMaxAggregation.VAR_BINARY_MAX;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_BOOLEAN;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_LONG;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_VARBINARY;
import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;
import static com.facebook.presto.tuple.TupleInfo.Type.VARIABLE_BINARY;
import static com.facebook.presto.util.MaterializedResult.resultBuilder;
import static com.facebook.presto.util.Threads.daemonThreadsNamed;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.testng.Assert.assertEquals;

public class TestHashAggregationOperator
{
    private ExecutorService executor;
    private DriverContext driverContext;

    @BeforeMethod
    public void setUp()
    {
        executor = newCachedThreadPool(daemonThreadsNamed("test"));
        Session session = new Session("user", "source", "catalog", "schema", "address", "agent");
        driverContext = new TaskContext(new TaskId("query", "stage", "task"), executor, session)
                .addPipelineContext(true, true)
                .addDriverContext();
    }

    @AfterMethod
    public void tearDown()
    {
        executor.shutdownNow();
    }

    @Test
    public void testHashAggregation()
            throws Exception
    {
        List<Page> input = rowPagesBuilder(SINGLE_VARBINARY, SINGLE_VARBINARY, SINGLE_VARBINARY, SINGLE_LONG, SINGLE_BOOLEAN)
                .addSequencePage(10, 100, 0, 100, 0, 500)
                .addSequencePage(10, 100, 0, 200, 0, 500)
                .addSequencePage(10, 100, 0, 300, 0, 500)
                .build();

        HashAggregationOperatorFactory operatorFactory = new HashAggregationOperatorFactory(
                0,
                ImmutableList.of(SINGLE_VARBINARY),
                Ints.asList(1),
                Step.SINGLE,
                ImmutableList.of(aggregation(COUNT, new Input(0)),
                        aggregation(LONG_SUM, new Input(3)),
                        aggregation(LONG_AVERAGE, new Input(3)),
                        aggregation(VAR_BINARY_MAX, new Input(2)),
                        aggregation(COUNT_STRING_COLUMN, new Input(0)),
                        aggregation(COUNT_BOOLEAN_COLUMN, new Input(4))),
                100_000);

        Operator operator = operatorFactory.createOperator(driverContext);

        MaterializedResult expected = resultBuilder(VARIABLE_BINARY, FIXED_INT_64, FIXED_INT_64, DOUBLE, VARIABLE_BINARY, FIXED_INT_64, FIXED_INT_64)
                .row("0", 3, 0, 0.0, "300", 3, 3)
                .row("1", 3, 3, 1.0, "301", 3, 3)
                .row("2", 3, 6, 2.0, "302", 3, 3)
                .row("3", 3, 9, 3.0, "303", 3, 3)
                .row("4", 3, 12, 4.0, "304", 3, 3)
                .row("5", 3, 15, 5.0, "305", 3, 3)
                .row("6", 3, 18, 6.0, "306", 3, 3)
                .row("7", 3, 21, 7.0, "307", 3, 3)
                .row("8", 3, 24, 8.0, "308", 3, 3)
                .row("9", 3, 27, 9.0, "309", 3, 3)
                .build();

        assertOperatorEqualsIgnoreOrder(operator, input, expected);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Task exceeded max memory size of 10B")
    public void testMemoryLimit()
    {
        List<Page> input = rowPagesBuilder(SINGLE_VARBINARY, SINGLE_VARBINARY, SINGLE_VARBINARY, SINGLE_LONG)
                .addSequencePage(10, 100, 0, 100, 0)
                .addSequencePage(10, 100, 0, 200, 0)
                .addSequencePage(10, 100, 0, 300, 0)
                .build();

        Session session = new Session("user", "source", "catalog", "schema", "address", "agent");
        DriverContext driverContext = new TaskContext(new TaskId("query", "stage", "task"), executor, session, new DataSize(10, Unit.BYTE))
                .addPipelineContext(true, true)
                .addDriverContext();

        HashAggregationOperatorFactory operatorFactory = new HashAggregationOperatorFactory(
                0,
                ImmutableList.of(SINGLE_VARBINARY),
                Ints.asList(1),
                Step.SINGLE,
                ImmutableList.of(aggregation(COUNT, new Input(0)),
                        aggregation(LONG_SUM, new Input(3)),
                        aggregation(LONG_AVERAGE, new Input(3)),
                        aggregation(VAR_BINARY_MAX, new Input(2))),
                100_000);

        Operator operator = operatorFactory.createOperator(driverContext);

        toPages(operator, input);
    }

    @Test
    public void testMultiSliceAggregationOutput()
    {
        long fixedWidthSize = SINGLE_LONG.getFixedSize() + SINGLE_DOUBLE.getFixedSize() + SINGLE_DOUBLE.getFixedSize();
        int multiSlicePositionCount = (int) (1.5 * PageBuilder.DEFAULT_MAX_PAGE_SIZE.toBytes() / fixedWidthSize);

        List<Page> input = rowPagesBuilder(SINGLE_VARBINARY, SINGLE_LONG)
                .addSequencePage(multiSlicePositionCount, 0, 0)
                .build();

        HashAggregationOperatorFactory operatorFactory = new HashAggregationOperatorFactory(
                0,
                ImmutableList.of(SINGLE_LONG),
                Ints.asList(1),
                Step.SINGLE,
                ImmutableList.of(aggregation(COUNT, new Input(0)),
                        aggregation(LONG_AVERAGE, new Input(1))),
                100_000);

        Operator operator = operatorFactory.createOperator(driverContext);

        assertEquals(toPages(operator, input).size(), 2);
    }
}
