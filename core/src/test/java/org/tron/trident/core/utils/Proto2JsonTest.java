package org.tron.trident.core.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.tron.trident.core.utils.Proto2Json.printTransactionToJSON;
import static org.tron.trident.core.utils.Proto2Json.wrapResponse;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.proto.Response.TransactionExtention;


class Proto2JsonTest {
  private static ApiWrapper client;
  private static String testAddress;

  @BeforeAll
  static void setUp() {
    client = ApiWrapper.ofNile(ApiWrapper.generateAddress().toPrivateKey());
    testAddress = client.keyPair.toBase58CheckAddress();
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  void testUndelegateResource() throws Exception {

    client.enableLocalCreate(new BlockId(Sha256Hash.ZERO_HASH, 0), 100000L);

    TransactionExtention transactionExtention
        = client.undelegateResource(client.keyPair.toBase58CheckAddress(),
        100_000L,
        1,
        testAddress);

    JSONObject result =
        wrapResponse(printTransactionToJSON(transactionExtention.getTransaction()),
            "wallet/undelegateResource");
    // System.out.println(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat));
    assertNotNull(result);
  }
}
