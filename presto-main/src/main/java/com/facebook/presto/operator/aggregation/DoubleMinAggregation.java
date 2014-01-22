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

import com.facebook.presto.block.Block;
import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.BlockCursor;
import com.facebook.presto.operator.GroupByIdBlock;
import com.facebook.presto.util.array.BooleanBigArray;
import com.facebook.presto.util.array.DoubleBigArray;
import com.google.common.base.Optional;

import static com.facebook.presto.tuple.TupleInfo.SINGLE_DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.google.common.base.Preconditions.checkState;

public class DoubleMinAggregation
        extends SimpleAggregationFunction
{
    public static final DoubleMinAggregation DOUBLE_MIN = new DoubleMinAggregation();

    public DoubleMinAggregation()
    {
        super(SINGLE_DOUBLE, SINGLE_DOUBLE, DOUBLE);
    }

    @Override
    protected GroupedAccumulator createGroupedAccumulator(Optional<Integer> maskChannel, int valueChannel)
    {
        // Min/max are not effected by distinct, so ignore it.
        return new DoubleMinGroupedAccumulator(valueChannel);
    }

    public static class DoubleMinGroupedAccumulator
            extends SimpleGroupedAccumulator
    {
        private final BooleanBigArray notNull;
        private final DoubleBigArray minValues;

        public DoubleMinGroupedAccumulator(int valueChannel)
        {
            super(valueChannel, SINGLE_DOUBLE, SINGLE_DOUBLE, Optional.<Integer>absent());

            this.notNull = new BooleanBigArray();

            this.minValues = new DoubleBigArray(Double.POSITIVE_INFINITY);
        }

        @Override
        public long getEstimatedSize()
        {
            return notNull.sizeOf() + minValues.sizeOf();
        }

        @Override
        protected void processInput(GroupByIdBlock groupIdsBlock, Block valuesBlock, Optional<Block> maskBlock)
        {
            notNull.ensureCapacity(groupIdsBlock.getGroupCount());
            minValues.ensureCapacity(groupIdsBlock.getGroupCount(), Double.POSITIVE_INFINITY);

            BlockCursor values = valuesBlock.cursor();

            for (int position = 0; position < groupIdsBlock.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());

                long groupId = groupIdsBlock.getGroupId(position);

                if (!values.isNull()) {
                    notNull.set(groupId, true);

                    double value = values.getDouble();
                    value = Math.min(value, minValues.get(groupId));
                    minValues.set(groupId, value);
                }
            }
            checkState(!values.advanceNextPosition());
        }

        @Override
        public void evaluateFinal(int groupId, BlockBuilder output)
        {
            if (notNull.get((long) groupId)) {
                double value = minValues.get((long) groupId);
                output.append(value);
            }
            else {
                output.appendNull();
            }
        }
    }

    @Override
    protected Accumulator createAccumulator(Optional<Integer> maskChannel, int valueChannel)
    {
        // Min/max are not effected by distinct, so ignore it.
        return new DoubleMinAccumulator(valueChannel);
    }

    public static class DoubleMinAccumulator
            extends SimpleAccumulator
    {
        private boolean notNull;
        private double min = Double.POSITIVE_INFINITY;

        public DoubleMinAccumulator(int valueChannel)
        {
            super(valueChannel, SINGLE_DOUBLE, SINGLE_DOUBLE, Optional.<Integer>absent());
        }

        @Override
        protected void processInput(Block block, Optional<Block> maskBlock)
        {
            BlockCursor values = block.cursor();

            for (int position = 0; position < block.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());
                if (!values.isNull()) {
                    notNull = true;
                    min = Math.min(min, values.getDouble());
                }
            }
        }

        @Override
        public void evaluateFinal(BlockBuilder out)
        {
            if (notNull) {
                out.append(min);
            }
            else {
                out.appendNull();
            }
        }
    }
}
