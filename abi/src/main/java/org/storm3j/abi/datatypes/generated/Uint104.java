package org.storm3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.storm3j.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.storm3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/storm3j/storm3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint104 extends Uint {
    public static final Uint104 DEFAULT = new Uint104(BigInteger.ZERO);

    public Uint104(BigInteger value) {
        super(104, value);
    }

    public Uint104(long value) {
        this(BigInteger.valueOf(value));
    }
}
