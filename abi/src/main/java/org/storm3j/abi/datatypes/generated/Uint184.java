package org.storm3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.storm3j.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.storm3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/storm3j/storm3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint184 extends Uint {
    public static final Uint184 DEFAULT = new Uint184(BigInteger.ZERO);

    public Uint184(BigInteger value) {
        super(184, value);
    }

    public Uint184(long value) {
        this(BigInteger.valueOf(value));
    }
}
