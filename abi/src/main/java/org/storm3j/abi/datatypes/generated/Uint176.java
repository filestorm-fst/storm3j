package org.storm3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.storm3j.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.storm3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/storm3j/storm3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint176 extends Uint {
    public static final Uint176 DEFAULT = new Uint176(BigInteger.ZERO);

    public Uint176(BigInteger value) {
        super(176, value);
    }

    public Uint176(long value) {
        this(BigInteger.valueOf(value));
    }
}
