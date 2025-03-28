package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.DelegatedResourceAccountIndex;
import org.tron.trident.proto.Response.DelegatedResourceList;

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

  @Test
  void testGetCanDelegatedMaxSize() {
    long maxSizeSolidity = client.getCanDelegatedMaxSize(testAddress, 0, NodeType.SOLIDITY_NODE);
    assertTrue(maxSizeSolidity >= 0);
  }

  @Test
  void testGetDelegatedResourceV2() {
    DelegatedResourceList resourceListSolidity = client.getDelegatedResourceV2(
        testAddress, testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(resourceListSolidity);
    assertTrue(resourceListSolidity.getDelegatedResourceCount() >= 0);
  }

  @Test
  void testGetDelegatedResourceAccountIndexV2() throws IllegalException {
    DelegatedResourceAccountIndex indexSolidity = client.getDelegatedResourceAccountIndexV2(
        testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(indexSolidity);
    assertTrue(indexSolidity.getToAccountsCount() >= 0 );
  }

}
