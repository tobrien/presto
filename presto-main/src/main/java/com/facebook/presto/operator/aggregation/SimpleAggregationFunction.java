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
import com.facebook.presto.operator.GroupByIdBlock;
import com.facebook.presto.operator.Page;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.TupleInfo.Type;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class SimpleAggregationFunction
        implements AggregationFunction
{
    private final TupleInfo finalTupleInfo;
    private final TupleInfo intermediateTupleInfo;
    private final ImmutableList<Type> parameterTypes;

    public SimpleAggregationFunction(TupleInfo finalTupleInfo, TupleInfo intermediateTupleInfo, Type parameterType)
    {
        this.finalTupleInfo = finalTupleInfo;
        this.intermediateTupleInfo = intermediateTupleInfo;
        this.parameterTypes = ImmutableList.of(parameterType);
    }

    @Override
    public final List<Type> getParameterTypes()
    {
        return parameterTypes;
    }

    @Override
    public final TupleInfo getFinalTupleInfo()
    {
        return finalTupleInfo;
    }

    @Override
    public final TupleInfo getIntermediateTupleInfo()
    {
        return intermediateTupleInfo;
    }

    @Override
    public final GroupedAccumulator createGroupedAggregation(Optional<Integer> maskChannel, int... argumentChannels)
    {
        checkArgument(argumentChannels.length == 1, "Expected one argument channel, but got %s", argumentChannels.length);

        return createGroupedAccumulator(maskChannel, argumentChannels[0]);
    }

    @Override
    public final GroupedAccumulator createGroupedIntermediateAggregation()
    {
        return createGroupedAccumulator(Optional.<Integer>absent(), -1);
    }

    protected abstract GroupedAccumulator createGroupedAccumulator(Optional<Integer> maskChannel, int valueChannel);

    public abstract static class SimpleGroupedAccumulator
            implements GroupedAccumulator
    {
        private final int valueChannel;
        private final TupleInfo finalTupleInfo;
        private final TupleInfo intermediateTupleInfo;
        private final Optional<Integer> maskChannel;

        public SimpleGroupedAccumulator(int valueChannel, TupleInfo finalTupleInfo, TupleInfo intermediateTupleInfo, Optional<Integer> maskChannel)
        {
            this.valueChannel = valueChannel;
            this.finalTupleInfo = finalTupleInfo;
            this.intermediateTupleInfo = intermediateTupleInfo;
            this.maskChannel = maskChannel;
        }

        @Override
        public final TupleInfo getFinalTupleInfo()
        {
            return finalTupleInfo;
        }

        @Override
        public final TupleInfo getIntermediateTupleInfo()
        {
            return intermediateTupleInfo;
        }

        @Override
        public final void addInput(GroupByIdBlock groupIdsBlock, Page page)
        {
            checkArgument(valueChannel != -1, "Raw input is not allowed for a final aggregation");

            processInput(groupIdsBlock, page.getBlock(valueChannel), maskChannel.transform(page.blockGetter()));
        }

        protected abstract void processInput(GroupByIdBlock groupIdsBlock, Block valuesBlock, Optional<Block> maskBlock);

        @Override
        public final void addIntermediate(GroupByIdBlock groupIdsBlock, Block block)
        {
            checkArgument(valueChannel == -1, "Intermediate input is only allowed for a final aggregation");

            processIntermediate(groupIdsBlock, block);
        }

        protected void processIntermediate(GroupByIdBlock groupIdsBlock, Block valuesBlock)
        {
            processInput(groupIdsBlock, valuesBlock, Optional.<Block>absent());
        }

        @Override
        public void evaluateIntermediate(int groupId, BlockBuilder output)
        {
            evaluateFinal(groupId, output);
        }

        @Override
        public abstract void evaluateFinal(int groupId, BlockBuilder output);
    }

    @Override
    public final Accumulator createAggregation(Optional<Integer> maskChannel, int... argumentChannels)
    {
        checkArgument(argumentChannels.length == 1, "Expected one argument channel, but got %s", argumentChannels.length);

        return createAccumulator(maskChannel, argumentChannels[0]);
    }

    @Override
    public final Accumulator createIntermediateAggregation()
    {
        return createAccumulator(Optional.<Integer>absent(), -1);
    }

    protected abstract Accumulator createAccumulator(Optional<Integer> maskChannel, int valueChannel);

    public abstract static class SimpleAccumulator
            implements Accumulator
    {
        private final int valueChannel;
        private final TupleInfo finalTupleInfo;
        private final TupleInfo intermediateTupleInfo;
        private final Optional<Integer> maskChannel;

        public SimpleAccumulator(int valueChannel, TupleInfo finalTupleInfo, TupleInfo intermediateTupleInfo, Optional<Integer> maskChannel)
        {
            this.valueChannel = valueChannel;
            this.finalTupleInfo = finalTupleInfo;
            this.intermediateTupleInfo = intermediateTupleInfo;
            this.maskChannel = maskChannel;
        }

        @Override
        public final TupleInfo getFinalTupleInfo()
        {
            return finalTupleInfo;
        }

        @Override
        public final TupleInfo getIntermediateTupleInfo()
        {
            return intermediateTupleInfo;
        }

        public final void addInput(Page page)
        {
            checkArgument(valueChannel != -1, "Raw input is not allowed for a final aggregation");

            processInput(page.getBlock(valueChannel), maskChannel.isPresent() ? Optional.of(page.getBlock(maskChannel.get())) : Optional.<Block>absent());
        }

        protected abstract void processInput(Block block, Optional<Block> maskBlock);

        @Override
        public final void addIntermediate(Block block)
        {
            checkArgument(valueChannel == -1, "Intermediate input is only allowed for a final aggregation");

            processIntermediate(block);
        }

        protected void processIntermediate(Block block)
        {
            processInput(block, Optional.<Block>absent());
        }

        @Override
        public final Block evaluateIntermediate()
        {
            BlockBuilder out = new BlockBuilder(intermediateTupleInfo);
            evaluateIntermediate(out);
            return out.build();
        }

        @Override
        public final Block evaluateFinal()
        {
            BlockBuilder out = new BlockBuilder(finalTupleInfo);
            evaluateFinal(out);
            return out.build();
        }

        protected void evaluateIntermediate(BlockBuilder out)
        {
            evaluateFinal(out);
        }

        protected abstract void evaluateFinal(BlockBuilder out);
    }
}
