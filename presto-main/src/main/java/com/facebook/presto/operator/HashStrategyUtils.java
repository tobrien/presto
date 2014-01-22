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

import com.facebook.presto.tuple.TupleInfo.Type;
import com.google.common.primitives.Longs;
import io.airlift.slice.Slice;

import static io.airlift.slice.SizeOf.SIZE_OF_BYTE;

public final class HashStrategyUtils
{
    private HashStrategyUtils()
    {
    }

    public static int addToHashCode(int result, int hashCode)
    {
        result = 31 * result + hashCode;
        return result;
    }

    private static int getVariableBinaryLength(Slice slice, int offset)
    {
        return slice.getInt(offset + SIZE_OF_BYTE);
    }

    public static boolean valueEquals(Type type, Slice leftSlice, int leftOffset, Slice rightSlice, int rightOffset)
    {
        // check if null flags are the same
        boolean leftIsNull = leftSlice.getByte(leftOffset) != 0;
        boolean rightIsNull = rightSlice.getByte(rightOffset) != 0;
        if (leftIsNull != rightIsNull) {
            return false;
        }

        // if values are both null, they are equal
        if (leftIsNull) {
            return true;
        }

        if (type == Type.FIXED_INT_64 || type == Type.DOUBLE) {
            long leftValue = leftSlice.getLong(leftOffset + SIZE_OF_BYTE);
            long rightValue = rightSlice.getLong(rightOffset + SIZE_OF_BYTE);
            return leftValue == rightValue;
        }
        else if (type == Type.BOOLEAN) {
            boolean leftValue = leftSlice.getByte(leftOffset + SIZE_OF_BYTE) != 0;
            boolean rightValue = rightSlice.getByte(rightOffset + SIZE_OF_BYTE) != 0;
            return leftValue == rightValue;
        }
        else if (type == Type.VARIABLE_BINARY) {
            int leftLength = getVariableBinaryLength(leftSlice, leftOffset);
            int rightLength = getVariableBinaryLength(rightSlice, rightOffset);
            return leftSlice.equals(leftOffset, leftLength,
                    rightSlice, rightOffset, rightLength);
        }
        else {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
    }

    public static int valueHashCode(Type type, Slice slice, int offset)
    {
        boolean isNull = slice.getByte(offset) != 0;
        if (isNull) {
            return 0;
        }

        if (type == Type.FIXED_INT_64) {
            return Longs.hashCode(slice.getLong(offset + SIZE_OF_BYTE));
        }
        else if (type == Type.DOUBLE) {
            long longValue = Double.doubleToLongBits(slice.getDouble(offset + SIZE_OF_BYTE));
            return Longs.hashCode(longValue);
        }
        else if (type == Type.BOOLEAN) {
            return slice.getByte(offset + SIZE_OF_BYTE) != 0 ? 1 : 0;
        }
        else if (type == Type.VARIABLE_BINARY) {
            int sliceLength = getVariableBinaryLength(slice, offset);
            return slice.hashCode(offset, sliceLength);
        }
        else {
            throw new IllegalArgumentException("Unsupported type " + type);
        }
    }
}
