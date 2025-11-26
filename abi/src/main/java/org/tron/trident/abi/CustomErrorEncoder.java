package org.tron.trident.abi;

import java.util.List;
import java.util.stream.Collectors;
import org.tron.trident.abi.datatypes.CustomError;
import org.tron.trident.abi.datatypes.Type;

/**
 * Ethereum custom error encoding. Further limited details are available <a
 * href="https://docs.soliditylang.org/en/develop/abi-spec.html#errors">here</a>.
 */
public class CustomErrorEncoder {

  private CustomErrorEncoder() {
  }

  public static String encode(CustomError error) {
    return EventEncoder.buildEventSignature(
        buildErrorSignature(error.getName(), error.getParameters()));
  }

  static <T extends Type> String buildErrorSignature(
      String errorName, List<TypeReference<T>> parameters) {

    StringBuilder result = new StringBuilder();
    result.append(errorName);
    result.append("(");
    String params =
        parameters.stream().map(Utils::getTypeName).collect(Collectors.joining(","));
    result.append(params);
    result.append(")");
    return result.toString();
  }

  public static String calculateSignatureHash(String errorSignature) {
    return EventEncoder.buildEventSignature(errorSignature);
  }
}
