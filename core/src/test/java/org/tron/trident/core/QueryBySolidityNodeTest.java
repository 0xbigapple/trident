package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.tron.trident.proto.Response.Account;

class QueryBySolidityNodeTest extends BaseTest {

  @Test
  void testGetAccount() {

    Account accountSolidity = client.getAccount(testAddress, NodeType.SOLIDITY_NODE);

    Account accountSolidityOld = client.getAccountSolidity(testAddress);

    assertEquals(accountSolidity, accountSolidityOld);
  }

}
