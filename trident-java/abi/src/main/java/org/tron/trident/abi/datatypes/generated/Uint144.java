package org.tron.trident.abi.datatypes.generated;

import java.math.BigInteger;
import org.tron.trident.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.trident.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Uint144 extends Uint {

  public static final Uint144 DEFAULT = new Uint144(BigInteger.ZERO);

  public Uint144(BigInteger value) {
    super(144, value);
  }

  public Uint144(long value) {
    this(BigInteger.valueOf(value));
  }
}
