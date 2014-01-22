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
import com.facebook.presto.operator.Page;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.facebook.presto.util.array.ObjectBigArray;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.DynamicSliceOutput;
import io.airlift.slice.Slice;
import io.airlift.slice.SliceInput;
import io.airlift.stats.QuantileDigest;

import java.util.List;

import static com.facebook.presto.tuple.TupleInfo.SINGLE_DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_LONG;
import static com.facebook.presto.tuple.TupleInfo.SINGLE_VARBINARY;
import static com.facebook.presto.tuple.TupleInfo.Type.DOUBLE;
import static com.facebook.presto.tuple.TupleInfo.Type.FIXED_INT_64;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.airlift.slice.SizeOf.SIZE_OF_DOUBLE;

public class ApproximatePercentileWeightedAggregation
        implements AggregationFunction
{
    private final Type parameterType;

    public ApproximatePercentileWeightedAggregation(Type parameterType)
    {
        this.parameterType = parameterType;
    }

    @Override
    public List<Type> getParameterTypes()
    {
        return ImmutableList.of(parameterType, FIXED_INT_64, DOUBLE);
    }

    @Override
    public TupleInfo getFinalTupleInfo()
    {
        return getOutputTupleInfo(parameterType);
    }

    @Override
    public TupleInfo getIntermediateTupleInfo()
    {
        return SINGLE_VARBINARY;
    }

    @Override
    public ApproximatePercentileWeightedGroupedAccumulator createGroupedAggregation(Optional<Integer> maskChannel, int[] argumentChannels)
    {
        return new ApproximatePercentileWeightedGroupedAccumulator(argumentChannels[0], argumentChannels[1], argumentChannels[2], parameterType, maskChannel);
    }

    @Override
    public GroupedAccumulator createGroupedIntermediateAggregation()
    {
        return new ApproximatePercentileWeightedGroupedAccumulator(-1, -1, -1, parameterType, Optional.<Integer>absent());
    }

    public static class ApproximatePercentileWeightedGroupedAccumulator
            implements GroupedAccumulator
    {
        private final int valueChannel;
        private final int weightChannel;
        private final int percentileChannel;
        private final Type parameterType;
        private final ObjectBigArray<DigestAndPercentile> digests;
        private final Optional<Integer> maskChannel;
        private long sizeOfValues;

        public ApproximatePercentileWeightedGroupedAccumulator(int valueChannel, int weightChannel, int percentileChannel, Type parameterType, Optional<Integer> maskChannel)
        {
            this.digests = new ObjectBigArray<>();
            this.valueChannel = valueChannel;
            this.weightChannel = weightChannel;
            this.percentileChannel = percentileChannel;
            this.parameterType = parameterType;
            this.maskChannel = maskChannel;
        }

        @Override
        public long getEstimatedSize()
        {
            return digests.sizeOf() + sizeOfValues;
        }

        @Override
        public TupleInfo getFinalTupleInfo()
        {
            return getOutputTupleInfo(parameterType);
        }

        @Override
        public TupleInfo getIntermediateTupleInfo()
        {
            return SINGLE_VARBINARY;
        }

        @Override
        public void addInput(GroupByIdBlock groupIdsBlock, Page page)
        {
            checkArgument(percentileChannel != -1, "Raw input is not allowed for a final aggregation");

            digests.ensureCapacity(groupIdsBlock.getGroupCount());

            BlockCursor values = page.getBlock(valueChannel).cursor();
            BlockCursor weights = page.getBlock(weightChannel).cursor();
            BlockCursor percentiles = page.getBlock(percentileChannel).cursor();
            BlockCursor masks = null;
            if (maskChannel.isPresent()) {
                masks = page.getBlock(maskChannel.get()).cursor();
            }

            for (int position = 0; position < groupIdsBlock.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());
                checkState(weights.advanceNextPosition());
                checkState(percentiles.advanceNextPosition());
                checkState(masks == null || masks.advanceNextPosition());

                long groupId = groupIdsBlock.getGroupId(position);

                // skip null values
                if (!values.isNull() && !weights.isNull() && (masks == null || masks.getBoolean())) {
                    DigestAndPercentile currentValue = digests.get(groupId);
                    if (currentValue == null) {
                        currentValue = new DigestAndPercentile(new QuantileDigest(0.01));
                        digests.set(groupId, currentValue);
                        sizeOfValues += currentValue.getDigest().estimatedInMemorySizeInBytes();
                    }

                    sizeOfValues -= currentValue.digest.estimatedInMemorySizeInBytes();
                    addValue(currentValue.digest, values, weights.getLong(), parameterType);
                    sizeOfValues += currentValue.digest.estimatedInMemorySizeInBytes();

                    // use last non-null percentile
                    if (!percentiles.isNull()) {
                        currentValue.setPercentile(percentiles.getDouble());
                    }
                }
            }
            checkState(!values.advanceNextPosition());
            checkState(!weights.advanceNextPosition());
            checkState(!percentiles.advanceNextPosition());
        }

        @Override
        public void addIntermediate(GroupByIdBlock groupIdsBlock, Block block)
        {
            checkArgument(percentileChannel == -1, "Intermediate input is only allowed for a final aggregation");

            digests.ensureCapacity(groupIdsBlock.getGroupCount());

            BlockCursor intermediates = block.cursor();

            for (int position = 0; position < groupIdsBlock.getPositionCount(); position++) {
                checkState(intermediates.advanceNextPosition());

                if (!intermediates.isNull()) {
                    long groupId = groupIdsBlock.getGroupId(position);

                    DigestAndPercentile currentValue = digests.get(groupId);
                    if (currentValue == null) {
                        currentValue = new DigestAndPercentile(new QuantileDigest(0.01));
                        digests.set(groupId, currentValue);
                        sizeOfValues += currentValue.getDigest().estimatedInMemorySizeInBytes();
                    }

                    SliceInput input = intermediates.getSlice().getInput();

                    sizeOfValues -= currentValue.getDigest().estimatedInMemorySizeInBytes();
                    currentValue.getDigest().merge(QuantileDigest.deserialize(input));
                    sizeOfValues += currentValue.getDigest().estimatedInMemorySizeInBytes();

                    currentValue.setPercentile(input.readDouble());
                }
            }
        }

        @Override
        public void evaluateIntermediate(int groupId, BlockBuilder output)
        {
            DigestAndPercentile currentValue = digests.get((long) groupId);
            if (currentValue == null || currentValue.getDigest().getCount() == 0.0) {
                output.appendNull();
            }
            else {
                DynamicSliceOutput sliceOutput = new DynamicSliceOutput(currentValue.getDigest().estimatedSerializedSizeInBytes());
                currentValue.getDigest().serialize(sliceOutput);
                sliceOutput.appendDouble(currentValue.getPercentile());

                output.append(sliceOutput.slice());
            }
        }

        @Override
        public void evaluateFinal(int groupId, BlockBuilder output)
        {
            DigestAndPercentile currentValue = digests.get((long) groupId);
            if (currentValue == null) {
                output.appendNull();
            }
            else {
                evaluate(output, parameterType, currentValue.getDigest(), currentValue.getPercentile());
            }
        }
    }

    @Override
    public ApproximatePercentileWeightedAccumulator createAggregation(Optional<Integer> maskChannel, int... argumentChannels)
    {
        return new ApproximatePercentileWeightedAccumulator(argumentChannels[0], argumentChannels[1], argumentChannels[2], parameterType, maskChannel);
    }

    @Override
    public ApproximatePercentileWeightedAccumulator createIntermediateAggregation()
    {
        return new ApproximatePercentileWeightedAccumulator(-1, -1, -1, parameterType, Optional.<Integer>absent());
    }

    public static class ApproximatePercentileWeightedAccumulator
            implements Accumulator
    {
        private final int valueChannel;
        private final int weightChannel;
        private final int percentileChannel;
        private final Type parameterType;
        private final Optional<Integer> maskChannel;

        private final QuantileDigest digest = new QuantileDigest(0.01);
        private double percentile = -1;

        public ApproximatePercentileWeightedAccumulator(int valueChannel, int weightChannel, int percentileChannel, Type parameterType, Optional<Integer> maskChannel)
        {
            this.valueChannel = valueChannel;
            this.weightChannel = weightChannel;
            this.percentileChannel = percentileChannel;
            this.parameterType = parameterType;
            this.maskChannel = maskChannel;
        }

        @Override
        public TupleInfo getFinalTupleInfo()
        {
            return getOutputTupleInfo(parameterType);
        }

        @Override
        public TupleInfo getIntermediateTupleInfo()
        {
            return SINGLE_VARBINARY;
        }

        @Override
        public void addInput(Page page)
        {
            checkArgument(valueChannel != -1, "Raw input is not allowed for a final aggregation");

            BlockCursor values = page.getBlock(valueChannel).cursor();
            BlockCursor weights = page.getBlock(weightChannel).cursor();
            BlockCursor percentiles = page.getBlock(percentileChannel).cursor();
            BlockCursor masks = null;
            if (maskChannel.isPresent()) {
                masks = page.getBlock(maskChannel.get()).cursor();
            }

            for (int position = 0; position < page.getPositionCount(); position++) {
                checkState(values.advanceNextPosition());
                checkState(weights.advanceNextPosition());
                checkState(percentiles.advanceNextPosition());
                checkState(masks == null || masks.advanceNextPosition());

                if (!values.isNull() && !weights.isNull() && (masks == null || masks.getBoolean())) {
                    addValue(digest, values, weights.getLong(), parameterType);

                    // use last non-null percentile
                    if (!percentiles.isNull()) {
                        percentile = percentiles.getDouble();
                    }
                }
            }
        }

        @Override
        public void addIntermediate(Block block)
        {
            checkArgument(valueChannel == -1, "Intermediate input is only allowed for a final aggregation");

            BlockCursor intermediates = block.cursor();

            for (int position = 0; position < block.getPositionCount(); position++) {
                checkState(intermediates.advanceNextPosition());
                if (!intermediates.isNull()) {
                    SliceInput input = intermediates.getSlice().getInput();
                    // read digest
                    digest.merge(QuantileDigest.deserialize(input));
                    // read percentile
                    percentile = input.readDouble();
                }
            }
        }

        @Override
        public final Block evaluateIntermediate()
        {
            BlockBuilder out = new BlockBuilder(getIntermediateTupleInfo());

            if (digest.getCount() == 0.0) {
                out.appendNull();
            }
            else {
                DynamicSliceOutput sliceOutput = new DynamicSliceOutput(digest.estimatedSerializedSizeInBytes() + SIZE_OF_DOUBLE);
                // write digest
                digest.serialize(sliceOutput);
                // write percentile
                sliceOutput.appendDouble(percentile);

                Slice slice = sliceOutput.slice();
                out.append(slice);
            }

            return out.build();
        }

        @Override
        public final Block evaluateFinal()
        {
            BlockBuilder out = new BlockBuilder(getFinalTupleInfo());

            evaluate(out, parameterType, digest, percentile);

            return out.build();
        }
    }

    private static TupleInfo getOutputTupleInfo(Type parameterType)
    {
        if (parameterType == FIXED_INT_64) {
            return SINGLE_LONG;
        }
        else if (parameterType == DOUBLE) {
            return SINGLE_DOUBLE;
        }
        else {
            throw new IllegalArgumentException("Expected parameter type to be FIXED_INT_64 or DOUBLE");
        }
    }

    private static void addValue(QuantileDigest digest, BlockCursor values, long weight, Type parameterType)
    {
        long value;
        if (parameterType == FIXED_INT_64) {
            value = values.getLong();
        }
        else if (parameterType == DOUBLE) {
            value = doubleToSortableLong(values.getDouble());
        }
        else {
            throw new IllegalArgumentException("Expected parameter type to be FIXED_INT_64 or DOUBLE");
        }

        digest.add(value, weight);
    }

    public static void evaluate(BlockBuilder out, Type parameterType, QuantileDigest digest, double percentile)
    {
        if (digest.getCount() == 0.0) {
            out.appendNull();
        }
        else {
            checkState(percentile != -1.0, "Percentile is missing");

            long value = digest.getQuantile(percentile);

            if (parameterType == FIXED_INT_64) {
                out.append(value);
            }
            else if (parameterType == DOUBLE) {
                out.append(longToDouble(value));
            }
            else {
                throw new IllegalArgumentException("Expected parameter type to be FIXED_INT_64 or DOUBLE");
            }
        }
    }

    private static double longToDouble(long value)
    {
        if (value < 0) {
            value ^= 0x7fffffffffffffffL;
        }

        return Double.longBitsToDouble(value);
    }

    private static long doubleToSortableLong(double value)
    {
        long result = Double.doubleToRawLongBits(value);

        if (result < 0) {
            result ^= 0x7fffffffffffffffL;
        }

        return result;
    }

    public static final class DigestAndPercentile
    {
        private final QuantileDigest digest;
        private double percentile = -1;

        public DigestAndPercentile(QuantileDigest digest)
        {
            this.digest = digest;
        }

        public QuantileDigest getDigest()
        {
            return digest;
        }

        public double getPercentile()
        {
            return percentile;
        }

        public void setPercentile(double percentile)
        {
            this.percentile = percentile;
        }
    }
}
