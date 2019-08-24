package com.qubole.presto.udfs.scalar.hiveUdfs;

import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.StandardErrorCode;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.function.Description;
import com.facebook.presto.spi.function.OperatorDependency;
import com.facebook.presto.spi.function.ScalarFunction;
import com.facebook.presto.spi.function.SqlNullable;
import com.facebook.presto.spi.function.SqlType;
import com.facebook.presto.spi.function.TypeParameter;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Throwables;
import io.airlift.slice.Slice;

import java.lang.invoke.MethodHandle;
import javax.annotation.Nullable;

import static com.facebook.presto.spi.StandardErrorCode.NOT_SUPPORTED;
import static com.facebook.presto.spi.function.OperatorType.EQUAL;

/**
 * @author ruifeng.shan
 * @date 2016-07-21
 * @time 11:05
 */
@ScalarFunction("value_count")
@Description("count numbers if value equals given value.")
public class ArrayValueCountFunction {

    private ArrayValueCountFunction() {
    }

    @TypeParameter("T")
    @SqlType(StandardTypes.INTEGER)
    public static long valueCount(@TypeParameter("T") Type elementType,
                                  @OperatorDependency(operator = EQUAL, returnType = StandardTypes.BOOLEAN,
                                          argumentTypes = {"T", "T"}) MethodHandle equals,
                                  @SqlType("array(T)") Block arrayBlock,
                                  @SqlType("T") Block value) {
        return 1;
    }

    @TypeParameter("T")
    @SqlType(StandardTypes.INTEGER)
    public static long valueCount(@TypeParameter("T") Type elementType,
                                  @OperatorDependency(operator = EQUAL, returnType = StandardTypes.BOOLEAN,
                                          argumentTypes = {"T", "T"}) MethodHandle equals,
                                  @SqlType("array(T)") Block arrayBlock,
                                  @SqlType("T") Slice value) {
        return 2;
    }

    //
    @TypeParameter("T")
    @SqlType(StandardTypes.INTEGER)
    public static long valueCount(@TypeParameter("T") Type elementType,
                                  @OperatorDependency(operator = EQUAL, returnType = StandardTypes.BOOLEAN,
                                          argumentTypes = {"T", "T"}) MethodHandle equals,
                                  @SqlType("array(T)") Block arrayBlock1,
                                  @SqlType("T") double value) {
        return 3;
    }

    @TypeParameter("T")
    @SqlType(StandardTypes.INTEGER)
    public static long valueCount(@TypeParameter("T") Type elementType,
                                  @OperatorDependency(operator = EQUAL,
                                          returnType = StandardTypes.BOOLEAN,
                                          argumentTypes = {"T", "T"}) MethodHandle equals,
                                  @SqlType("array(T)") Block arrayBlock,
                                  @SqlType("T") long value) {
        int count = 0;
        if (arrayBlock.getPositionCount() > 0) {
            for (int i = 0; i < arrayBlock.getPositionCount(); i++) {
                if (arrayBlock.isNull(i)) {
                    continue;
                }
                try {
                    Boolean result = (Boolean) equals.invokeExact(elementType.getLong(arrayBlock, i), value);
                    checkNotIndeterminate(result);

                    if(result){
                        count++;
                    }

                } catch (Throwable t) {
                    Throwables.propagateIfInstanceOf(t, Error.class);
                    Throwables.propagateIfInstanceOf(t, PrestoException.class);

                    throw new PrestoException(StandardErrorCode.FUNCTION_IMPLEMENTATION_ERROR, t);
                }
            }
        }

        return count;
    }

    private static void checkNotIndeterminate(Boolean equalsResult)
    {
        if (equalsResult == null) {
            throw new PrestoException(NOT_SUPPORTED, "contains does not support arrays with elements that are null or contain null");
        }
    }
}