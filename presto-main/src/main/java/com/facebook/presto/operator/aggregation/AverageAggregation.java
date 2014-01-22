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
import com.facebook.presto.tuple.TupleInfo.Type;
import com.facebook.presto.util.array.DoubleBigArray;
import com.facebook.presto.util.array.LongBigArray;
import com.google.common.base.Optional;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import static com.facebook.presto.tuple.TupleInfo.SINGLE_DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_VARBINARY;
import static com.google.common.base.Preconditions.checkState;
import static io.airlift.slice.SizeOf.SIZE_OF_DOUBLE;
import static io.airlift.slice.SizeOf.SIZE_OF_LONG;

public class AverageAggregation
        extends SimpleAggregationFunction
{
    private final boolean inputIsLong;

    public AverageAggregation(Type parameterType)
    {
        super(SINGLE_DOUBLE, SINGLE_VARBINARY, parameterType);

        if (parameterType == Type.FIXED_INT_64) {
            this.inputIsLong = true;
        }
        else if (parameterType == Type.DOUBLE) {
            this.inputIsLong = false;
        }
        else {
            throw new IllegalArgumentException("Expected parameter type to be FIXED_INT_64 or DOUBLE, but was " + parameterType);
        }
    }

    @Override
    protected GroupedAccumulator createGroupedAccumulator(Optional<Integer> maskChannel, int valueChannel)
    {
        return new AverageGroupedAccumulator(valueChannel, inputIsLong, maskChannel);
    }

    public static class AverageGroupedAccumulator
            extends SimpleGroupedAccumulator
    {
        private final boolean inputIsLong;

        private final LongBigArray counts;
        private final DoubleBigArray sums;

        public AverageGroupedAccumulator(int valueChannel, boolean inputIsLong, Optional<Integer> maskChannel)
        {
            super(valueChannel, SINGLE_DOUBLE, SINGLE_VARBINARY, maskChannel);
            this.inputIsLong = inputIsLong;
            this.counts = new LongBigArray();
            this.sums = new DoubleBigArray();
        }

        @Override
        public long getEstimatedSize()
        {
            return counts.sizeOf() + sums.sizeOf();
        }

        @Override
        public void processInput(GroupByIdBlock groupIdsBlock, Block valuesBlock, Optional<Block> maskBlock)
        {
            counts.ensureCapacity(groupIdsBlock.getGroupCount());
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
                    counts.increment(groupId);

                    double value;
                    if (inputIsLong) {
                        value = values.getLong();
                    }
                    else {
                        value = values.getDouble();
                    }
                    sums.add(groupId, value);
                }
            }
            checkState(!values.advanceNextPosition());
        }

        @Override
        public void processIntermediate(GroupByIdBlock groupIdsBlock, Block block)
        {
            counts.ensureCapacity(groupIdsBlock.getGroupCount());
            sums.ensureCapacity(groupIdsBlock.getGroupCount());

            BlockCursor intermediateValues = block.cursor();

            for (int position = 0; position < groupIdsBlock.getPositionCount(); position++) {
                checkState(intermediateValues.advanceNextPosition());

                long groupId = groupIdsBlock.getGroupId(position);

                Slice value = intermediateValues.getSlice();
                long count = value.getLong(0);
                counts.add(groupId, count);

                double sum = value.getDouble(SIZE_OF_LONG);
                sums.add(groupId, sum);
            }
            checkState(!intermediateValues.advanceNextPosition());
        }

        @Override
        public void evaluateIntermediate(int groupId, BlockBuilder output)
        {
            long count = counts.get((long) groupId);
            double sum = sums.get((long) groupId);

            // todo replace this when general fixed with values are supported
            Slice value = Slices.allocate(SIZE_OF_LONG + SIZE_OF_DOUBLE);
            value.setLong(0, count);
            value.setDouble(SIZE_OF_LONG, sum);
            output.append(value);
        }

        @Override
        public void evaluateFinal(int groupId, BlockBuilder output)
        {
            long count = counts.get((long) groupId);
            if (count != 0) {
                double value = sums.get((long) groupId);
                output.append(value / count);
            }
            else {
                output.appendNull();
            }
        }
    }

    @Override
    protected Accumulator createAccumulator(Optional<Integer> maskChannel, int valueChannel)
    {
        return new AverageAccumulator(valueChannel, inputIsLong, maskChannel);
    }

    public static class AverageAccumulator
            extends SimpleAccumulator
    {
        private final boolean inputIsLong;

        private long count;
        private double sum;

        public AverageAccumulator(int valueChannel, boolean inputIsLong, Optional<Integer> maskChannel)
        {
            super(valueChannel, SINGLE_DOUBLE, SINGLE_VARBINARY, maskChannel);
            this.inputIsLong = inputIsLong;
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
                    count++;
                    if (inputIsLong) {
                        sum += values.getLong();
                    }
                    else {
                        sum += values.getDouble();
                    }
                }
            }
        }

        @Override
        protected void processIntermediate(Block block)
        {
            BlockCursor intermediates = block.cursor();

            for (int position = 0; position < block.getPositionCount(); position++) {
                checkState(intermediates.advanceNextPosition());
                Slice value = intermediates.getSlice();
                count += value.getLong(0);
                sum += value.getDouble(SIZE_OF_LONG);
            }
        }

        @Override
        public void evaluateIntermediate(BlockBuilder out)
        {
            // todo replace this when general fixed with values are supported
            Slice value = Slices.allocate(SIZE_OF_LONG + SIZE_OF_DOUBLE);
            value.setLong(0, count);
            value.setDouble(SIZE_OF_LONG, sum);
            out.append(value);
        }

        @Override
        public void evaluateFinal(BlockBuilder out)
        {
            if (count != 0) {
                out.append(sum / count);
            }
            else {
                out.appendNull();
            }
        }
    }
}
