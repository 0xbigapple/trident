package org.tron.trident.core.utils;

import com.google.protobuf.Message;
import java.util.EnumMap;
import java.util.Map;
import org.checkerframework.checker.units.qual.C;
import org.tron.trident.proto.Chain.Transaction.Contract.ContractType;
import org.tron.trident.proto.Contract.DelegateResourceContract;
import org.tron.trident.proto.Contract.TransferContract;
import org.tron.trident.proto.Contract.TriggerSmartContract;
import org.tron.trident.proto.Contract.UnDelegateResourceContract;

public final class ContractRouter {

  private static final Map<ContractType, Class<? extends Message>> ROUTER =
      new EnumMap<>(ContractType.class);

  static {
    ROUTER.put(ContractType.TransferContract, TransferContract.class);
    ROUTER.put(ContractType.DelegateResourceContract, DelegateResourceContract.class);
    ROUTER.put(ContractType.UnDelegateResourceContract, UnDelegateResourceContract.class);
    // todo add more contract types
  }

  private ContractRouter() {
  }

  public static Class<? extends Message> route(ContractType type) {
    return ROUTER.get(type);
  }
}
