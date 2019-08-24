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
package com.qubole.presto.udfs.scalar.hiveUdfs;


import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.function.*;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;

import java.lang.invoke.MethodHandle;

import static com.facebook.presto.spi.StandardErrorCode.NOT_SUPPORTED;
import static com.facebook.presto.spi.function.OperatorType.EQUAL;
import static com.facebook.presto.util.Failures.internalError;

@Description("Determines whether given value exists in the array")
@ScalarFunction("array_sum")
public final class ArraySum {
    private ArraySum() {
    }


    @TypeParameter("T")
    @SqlType(StandardTypes.INTEGER)
    @SqlNullable
    public static Long sum(
            @TypeParameter("T") Type elementType,
            @OperatorDependency(operator = EQUAL,
                    returnType = StandardTypes.BOOLEAN,
                    argumentTypes = {"T", "T"}) MethodHandle equals,
            @SqlType("array(T)") Block arrayBlock) {

        long sum = 0;
        boolean foundNull = false;
        for (int i = 0; i < arrayBlock.getPositionCount(); i++) {
            if (arrayBlock.isNull(i)) {
                foundNull = true;
                continue;
            }
            try {
                long arrayValue = elementType.getLong(arrayBlock, i);

                System.out.println("****** current position ****** ==> " + arrayValue);
                sum = sum + arrayValue;
            } catch (Throwable t) {
                throw internalError(t);
            }
        }
        if (foundNull) {
            return null;
        }
        return sum;
    }

    private static void checkNotIndeterminate(Boolean equalsResult) {
        if (equalsResult == null) {
            throw new PrestoException(NOT_SUPPORTED, "contains does not support arrays with elements that are null or contain null");
        }
    }
}
