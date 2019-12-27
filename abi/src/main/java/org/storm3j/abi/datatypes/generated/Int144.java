package org.storm3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.storm3j.abi.datatypes.Int;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.storm3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/storm3j/storm3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int144 extends Int {
    public static final Int144 DEFAULT = new Int144(BigInteger.ZERO);

    public Int144(BigInteger value) {
        super(144, value);
    }

    public Int144(long value) {
        this(BigInteger.valueOf(value));
    }
}
