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
import com.facebook.presto.util.array.LongBigArray;
import com.google.common.base.Optional;

import static com.facebook.presto.tuple.TupleInfo.SINGLE_LONG;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;
import static com.google.common.base.Preconditions.checkState;

public class LongSumAggregation
        extends SimpleAggregationFunction
{
    public static final LongSumAggregation LONG_SUM = new LongSumAggregation();

    public LongSumAggregation()
    {
        super(SINGLE_LONG, SINGLE_LONG, FIXED_INT_64);
    }

    @Override
    protected GroupedAccumulator createGroupedAccumulator(Optional<Integer> maskChannel, int valueChannel)
    {
        return new LongSumGroupedAccumulator(valueChannel, maskChannel);
    }

    public static class LongSumGroupedAccumulator
            extends SimpleGroupedAccumulator
    {
        private final BooleanBigArray notNull;
        private final LongBigArray sums;

        public LongSumGroupedAccumulator(int valueChannel, Optional<Integer> maskChannel)
        {
            super(valueChannel, SINGLE_LONG, SINGLE_LONG, maskChannel);

            this.notNull = new BooleanBigArray();

            this.sums = new LongBigArray();
        }

        @Override
        public long getEstimatedSize()
        {
            return notNull.sizeOf() + sums.sizeOf();
        }

        @Override
        protected void processInput(GroupByIdBlock groupIdsBlock, Block valuesBlock, Optional<Block> maskBlock)
        {
            notNull.ensureCapacity(groupIdsBlock.getGroupCount());
            sums.ensureCapacity(groupIdsBlock.getGroupCount());

            BlockCursor values = valuesBlock.cursor();
            BlockCursor masks = null;
            if (maskBlock.isPresent()) {
                masks = maskBlock.get().cursor();
            }

            for (int position = 0; position < groupIdsBlock.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());
                checkState(masks == null || masks.advanceNextPosition());

                long groupId = groupIdsBlock.getGroupId(position);

                if (!values.isNull() && (masks == null || masks.getBoolean())) {
                    notNull.set(groupId, true);

                    long value = values.getLong();
                    sums.add(groupId, value);
                }
            }
            checkState(!values.advanceNextPosition());
        }

        @Override
        public void evaluateFinal(int groupId, BlockBuilder output)
        {
            if (notNull.get((long) groupId)) {
                long value = sums.get((long) groupId);
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
        return new LongSumAccumulator(valueChannel, maskChannel);
    }

    public static class LongSumAccumulator
            extends SimpleAccumulator
    {
        private boolean notNull;
        private long sum;

        public LongSumAccumulator(int valueChannel, Optional<Integer> maskChannel)
        {
            super(valueChannel, SINGLE_LONG, SINGLE_LONG, maskChannel);
        }

        @Override
        protected void processInput(Block block, Optional<Block> maskBlock)
        {
            BlockCursor values = block.cursor();
            BlockCursor masks = null;
            if (maskBlock.isPresent()) {
                masks = maskBlock.get().cursor();
            }

            for (int position = 0; position < block.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());
                checkState(masks == null || masks.advanceNextPosition());
                if (!values.isNull() && (masks == null || masks.getBoolean())) {
                    notNull = true;
                    sum += values.getLong();
                }
            }
        }

        @Override
        public void evaluateFinal(BlockBuilder out)
        {
            if (notNull) {
                out.append(sum);
            }
            else {
                out.appendNull();
            }
        }
    }
}
