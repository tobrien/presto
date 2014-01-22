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
package com.facebook.presto.operator.aggregation;

import com.facebook.presto.operator.aggregation.ApproximateAverageAggregation.ApproximateAverageAccumulator;
import com.facebook.presto.operator.aggregation.ApproximateAverageAggregation.ApproximateAverageGroupedAccumulator;
import com.facebook.presto.operator.aggregation.SimpleAggregationFunction.SimpleAccumulator;
import com.facebook.presto.operator.aggregation.SimpleAggregationFunction.SimpleGroupedAccumulator;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.google.common.base.Throwables;

import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;

public final class ApproximateAverageAggregations
{
    public static final AggregationFunction LONG_APPROXIMATE_AVERAGE_AGGREGATION = createIsolatedAggregation(FIXED_INT_64);
    public static final AggregationFunction DOUBLE_APPROXIMATE_AVERAGE_AGGREGATION = createIsolatedAggregation(DOUBLE);

    private ApproximateAverageAggregations() {}

    private static AggregationFunction createIsolatedAggregation(Type parameterType)
    {
        Class<? extends AggregationFunction> functionClass = IsolatedClass.isolateClass(
                AggregationFunction.class,

                ApproximateAverageAggregation.class,
                SimpleAggregationFunction.class,

                ApproximateAverageGroupedAccumulator.class,
                SimpleGroupedAccumulator.class,

                ApproximateAverageAccumulator.class,
                SimpleAccumulator.class);

        try {
            return functionClass
                    .getConstructor(Type.class)
                    .newInstance(parameterType);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
