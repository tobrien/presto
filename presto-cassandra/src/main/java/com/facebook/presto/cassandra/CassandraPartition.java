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

import com.facebook.presto.spi.Partition;
import com.facebook.presto.spi.TupleDomain;

import java.nio.ByteBuffer;

public class CassandraPartition
        implements Partition
{
    static final String UNPARTITIONED_ID = "<UNPARTITIONED>";
    public static final CassandraPartition UNPARTITIONED = new CassandraPartition();

    private final String partitionId;
    private final byte[] key;
    private final TupleDomain tupleDomain;

    private CassandraPartition()
    {
        partitionId = UNPARTITIONED_ID;
        tupleDomain = TupleDomain.all();
        key = null;
    }

    public CassandraPartition(byte[] key, String partitionId, TupleDomain tupleDomain)
    {
        this.key = key;
        this.partitionId = partitionId;
        this.tupleDomain = tupleDomain;
    }

    public boolean isUnpartitioned()
    {
        return partitionId.equals(UNPARTITIONED_ID);
    }

    @Override
    public TupleDomain getTupleDomain()
    {
        return tupleDomain;
    }

    public String getPartitionId()
    {
        return partitionId;
    }

    @Override
    public String toString()
    {
        return partitionId;
    }

    public ByteBuffer getKeyAsByteBuffer()
    {
        return ByteBuffer.wrap(key);
    }
}
