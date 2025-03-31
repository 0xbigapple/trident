package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Contract.AssetIssueContract;
import org.tron.trident.proto.Response.Account;
import org.tron.trident.proto.Response.AssetIssueList;
import org.tron.trident.proto.Response.BlockExtention;
import org.tron.trident.proto.Response.DelegatedResourceAccountIndex;
import org.tron.trident.proto.Response.DelegatedResourceList;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfoList;

class QueryBySolidityNodeTest extends BaseTest {

  @Test
  void testGetAccount() {
    Account accountSolidity = client.getAccount(testAddress, NodeType.SOLIDITY_NODE);
    assertTrue(accountSolidity.getAssetCount() >= 0);
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
    assertTrue(indexSolidity.getToAccountsCount() >= 0);
  }

  @Test
  void testGetNowBlock() throws IllegalException {
    Block blockSolidity = client.getNowBlock(NodeType.SOLIDITY_NODE);
    assertNotNull(blockSolidity);
    assertTrue(blockSolidity.getBlockHeader().getRawData().getNumber() > 0);
  }

  @Test
  void testGetBlockByNum() throws IllegalException {
    BlockExtention blockSolidity = client.getBlockByNum(55157371, NodeType.SOLIDITY_NODE);
    assertNotNull(blockSolidity);
    assertEquals(55157371, blockSolidity.getBlockHeader().getRawData().getNumber());
  }

  @Test
  void testGetTransactionInfoByBlockNum() throws IllegalException {
    TransactionInfoList infoListSolidity = client.getTransactionInfoByBlockNum(55157371, NodeType.SOLIDITY_NODE);
    assertNotNull(infoListSolidity);
    assertTrue(infoListSolidity.getTransactionInfoCount() > 0);
  }

  @Test
  void testGetTransactionInfoById() throws IllegalException {
    //usdt transfer tx
    String txId = "d3c0afaf7db3ca7a6713d15331b397be781d6e57356ced46324ad1dc66b6c4c0";
    TransactionInfo infoSolidity = client.getTransactionInfoById(txId, NodeType.SOLIDITY_NODE);
    assertNotNull(infoSolidity);
    assertTrue(infoSolidity.getFee() > 0);
    assertTrue(infoSolidity.getLogCount() > 0);
  }

  @Test
  void testGetTransactionById() throws IllegalException {
    String txId = "d3c0afaf7db3ca7a6713d15331b397be781d6e57356ced46324ad1dc66b6c4c0";
    Transaction txnSolidity = client.getTransactionById(txId, NodeType.SOLIDITY_NODE);
    assertNotNull(txnSolidity);
    assertTrue(txnSolidity.getRawData().getTimestamp() > 0);
  }

  @Test
  void testGetAccountById() {
    String accountId = "test1743388741490"; //TFzqPiME2TSY9akvpPbFijt7QMrU2y2Jaz
    Account accountSolidity = client.getAccountById(accountId, NodeType.SOLIDITY_NODE);
    assertNotNull(accountSolidity);
    assertTrue(accountSolidity.isInitialized());
  }

  @Test
  void testGetDelegatedResource() {
    DelegatedResourceList resourceListSolidity = client.getDelegatedResource(
        testAddress, testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(resourceListSolidity);
    assertTrue(resourceListSolidity.getDelegatedResourceCount() >= 0);
  }

  @Test
  void testGetDelegatedResourceAccountIndex() {
    DelegatedResourceAccountIndex indexSolidity = client.getDelegatedResourceAccountIndex(
        testAddress, NodeType.SOLIDITY_NODE);
    assertNotNull(indexSolidity);
    assertTrue(indexSolidity.getToAccountsCount() >= 0);
  }

//  @Test
//  void testGetAssetIssueList() {
//    AssetIssueList assetIssueList = client.getAssetIssueList(NodeType.SOLIDITY_NODE);
//    assertTrue(assetIssueList.getAssetsCount() > 0);
//  }

  @Test
  void testGetPaginatedAssetIssueList() {
    AssetIssueList assetIssueList = client.getPaginatedAssetIssueList(0,10,NodeType.SOLIDITY_NODE);
    assertTrue(assetIssueList.getAssetsCount() > 0);

  }

  @Test
  void testGetAssetIssueById() {
    AssetIssueContract assetIssueContract
        = client.getAssetIssueById(tokenId, NodeType.SOLIDITY_NODE);
    assertEquals(assetIssueContract.getId(), tokenId);

  }

  @Test
  void testGetAssetIssueListByName() {
    AssetIssueList assetIssueList
        = client.getAssetIssueListByName("KKK", NodeType.SOLIDITY_NODE);
    assertTrue(assetIssueList.getAssetsCount() > 0);
  }


}
