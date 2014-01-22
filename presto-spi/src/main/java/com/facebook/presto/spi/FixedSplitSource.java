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
package com.facebook.presto.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedSplitSource
        implements SplitSource
{
    private final String dataSourceName;
    private final List<Split> splits;
    private int offset;

    public FixedSplitSource(String dataSourceName, Iterable<? extends Split> splits)
    {
        this.dataSourceName = dataSourceName;
        if (splits == null) {
            throw new NullPointerException("splits is null");
        }
        List<Split> splitsList = new ArrayList<>();
        for (Split split : splits) {
            splitsList.add(split);
        }
        this.splits = Collections.unmodifiableList(splitsList);
    }

    @Override
    public String getDataSourceName()
    {
        return dataSourceName;
    }

    @Override
    public List<Split> getNextBatch(int maxSize)
            throws InterruptedException
    {
        int remainingSplits = splits.size() - offset;
        int size = Math.min(remainingSplits, maxSize);
        List<Split> results = splits.subList(offset, offset + size);
        offset += size;
        return results;
    }

    @Override
    public boolean isFinished()
    {
        return offset >= splits.size();
    }
}
