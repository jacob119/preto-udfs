package com.qubole.presto.udfs.scalar.hiveUdfs;


import com.facebook.presto.spi.PageBuilder;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.StandardErrorCode;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.function.*;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import static com.facebook.presto.util.Failures.internalError;

@ScalarFunction("wilcox_test")
@Description("Intersects elements of the two given arrays")
public class WilcoxTest {
    private final PageBuilder pageBuilder;

    @TypeParameter("E")
    public WilcoxTest(@TypeParameter("E") Type elementType) {
        pageBuilder = new PageBuilder(ImmutableList.of(elementType));
    }

    /*
    url : https://github.com/apache/commons-math/blob/eb57d6d457002a0bb5336d789a3381a24599affe/src/test/java/org/apache/commons/math4/stat/inference/WilcoxonSignedRankTestTest.java
    ex) query for test as below
    select wilcox_test(array[1.83, 0.50, 1.62, 2.48, 1.68, 1.88, 1.55, 3.06, 1.30], array[0.878, 0.647, 0.598, 2.05, 1.06, 1.29, 1.06, 3.14, 1.29]);
     */
    @TypeParameter("E")
    @SqlType(StandardTypes.DOUBLE)
    @SqlNullable
    public static Double wilcox_test(
            @TypeParameter("E") Type type,
            @SqlType("array(E)") Block leftArray,
            @SqlType("array(E)") Block rightArray) {
        int lcnt = leftArray.getPositionCount();
        int rcnt = rightArray.getPositionCount();

        if (lcnt != rcnt)
            throw new PrestoException(StandardErrorCode.INVALID_FUNCTION_ARGUMENT, "mismatched size of two arguments");
        WilcoxonSignedRankTest testStatistic = new WilcoxonSignedRankTest();
        final double[] lValues = toDouble(leftArray, type);
        final double[] rValues = toDouble(rightArray, type);
        return testStatistic.wilcoxonSignedRankTest(lValues, rValues, true);
    }

    private static double[] toDouble(Block block,  Type type){
        final double[] toDouble = new double[block.getPositionCount()];
        for (int i = 0; i < block.getPositionCount(); i++) {
            if (block.isNull(i)) {
                continue;
            }
            try {
                long leftValue = type.getLong(block, i);
                toDouble[i] = leftValue;
            } catch (Throwable t) {
                throw internalError(t);
            }
        }
        return toDouble;
    }
}
