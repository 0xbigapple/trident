package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.tron.trident.proto.Response.Account;

class QueryBySolidityNodeTest extends BaseTest {

  @Test
  void testGetAccount() {
    Account accountSolidity = client.getAccount(testAddress, NodeType.SOLIDITY_NODE);
    Account accountSolidityOld = client.getAccountSolidity(testAddress);
    assertEquals(accountSolidity, accountSolidityOld);
  }

  @Test
  void testGetAvailableUnfreezeCount() {
    long countSolidity = client.getAvailableUnfreezeCount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(countSolidity >= 0);
  }

  @Test
  void testGetCanWithdrawUnfreezeAmount() {
    // Test query from latest block
    long amountSolidity = client.getCanWithdrawUnfreezeAmount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(amountSolidity >= 0);

    // Test query with timestamp
    long timestamp = System.currentTimeMillis();
    long amountWithTimeSolidity = client.getCanWithdrawUnfreezeAmount(testAddress, timestamp, NodeType.SOLIDITY_NODE);
    assertTrue(amountWithTimeSolidity >= 0);
  }

}
