package org.tron.trident.core.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.hash.Hashing;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.tron.trident.proto.Chain.Transaction;

public class Proto2Json {
  public static final String PERMISSION_ID = "Permission_id";
  public static final String VALUE = "value";
  public static final String PARAMETER = "parameter";
  public static final String METHOD = "method";

  public static JSONObject printTransactionToJSON(Transaction transaction) {
    JSONObject jsonTransaction = JSONObject
        .parseObject(JsonFormat.printToString(transaction));
    JSONArray contracts = new JSONArray();
    transaction.getRawData().getContractList().stream().forEach(contract -> {
      try {
        JSONObject contractJson = null;
        Any contractParameter = contract.getParameter();
        Class<? extends Message> clazz =
            ContractRouter.route(contract.getType());
        if (clazz != null) {
          contractJson = JSONObject
              .parseObject(JsonFormat.printToString(contractParameter.unpack(clazz)));
        }

        JSONObject parameter = new JSONObject();
        parameter.put(VALUE, contractJson);
        parameter.put("type_url", contract.getParameterOrBuilder().getTypeUrl());
        JSONObject jsonContract = new JSONObject();
        jsonContract.put(PARAMETER, parameter);
        jsonContract.put("type", contract.getType());
        if (contract.getPermissionId() > 0) {
          jsonContract.put(PERMISSION_ID, contract.getPermissionId());
        }
        contracts.add(jsonContract);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    JSONObject rawData = JSONObject.parseObject(jsonTransaction.get("raw_data").toString());
    rawData.put("contract", contracts);
    jsonTransaction.put("raw_data", rawData);
    String rawDataHex = ByteArray.toHexString(transaction.getRawData().toByteArray());
    jsonTransaction.put("raw_data_hex", rawDataHex);

    byte[] rawBytes = transaction.getRawData().toByteArray();
    String txID = Hashing.sha256().hashBytes(rawBytes).toString();
    jsonTransaction.put("txID", txID);
    return jsonTransaction;
  }

  public static JSONObject wrapResponse(JSONObject jsonObject, String method) {
    JSONObject wrapper = new JSONObject();
    wrapper.put(METHOD, method);
    wrapper.put(PARAMETER, jsonObject);
    return wrapper;
  }

}

