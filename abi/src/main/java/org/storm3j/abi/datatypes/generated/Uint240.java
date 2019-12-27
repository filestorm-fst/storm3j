package org.storm3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.storm3j.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.storm3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/storm3j/storm3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint240 extends Uint {
    public static final Uint240 DEFAULT = new Uint240(BigInteger.ZERO);

    public Uint240(BigInteger value) {
        super(240, value);
    }

    public Uint240(long value) {
        this(BigInteger.valueOf(value));
    }
}
