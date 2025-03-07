package org.tron.trident.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Transaction;
import org.tron.trident.proto.Common.Key;
import org.tron.trident.proto.Common.Permission;
import org.tron.trident.proto.Contract.AccountPermissionUpdateContract;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Response.TransactionInfo;
import org.tron.trident.proto.Response.TransactionInfo.code;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("add private key to enable this case")
public class MultiSignTest extends BaseTest {

  private String ownerAddress;  // The owner account address
  private KeyPair ownerKeyPair;     // The owner account keypair
  private List<KeyPair> activeKeyPairs = new ArrayList<>();  // List of multi-sign account keypairs

  void transferTrx(String fromAddress, String toAddress, long amount, KeyPair signKeyPair)
      throws IllegalException, InterruptedException {
    TransactionExtention txnExt = client.transfer(fromAddress, toAddress, amount);
    Transaction signedTxn = client.signTransaction(txnExt, signKeyPair);
    String txid = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    Thread.sleep(3000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txid);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @Test
  @Order(1)
  void initAccount() throws Exception {
    // 1. Generate owner account
    ownerKeyPair = KeyPair.generate();
    ownerAddress = ownerKeyPair.toBase58CheckAddress();
    //    System.out.println(ownerKeyPair.toPrivateKey());

    // 2. Transfer TRX from test account to new account
    // 150 TRX = 150_000_000 SUN
    // - 100 TRX for permission update
    // - Some TRX for bandwidth and energy
    // - Remaining TRX for transfer test
    transferTrx(testAddress, ownerAddress, 150_000_000L, client.keyPair);

  }

  @Test
  @Order(2)
  void setupMultiSignAccount() throws Exception {
    // 1. Generate 3 keypairs for multi-sign accounts
    for (int i = 0; i < 3; i++) {
      activeKeyPairs.add(KeyPair.generate());
    }

    // Build owner permission with threshold 1 - no operations needed
    Permission ownerPermission = Permission.newBuilder()
        .setType(Permission.PermissionType.Owner)
        .setId(0)
        .setPermissionName("owner")
        .setThreshold(1)
        .addKeys(Key.newBuilder()
            .setAddress(ApiWrapper.parseAddress(ownerAddress))
            .setWeight(1)
            .build())
        .build();  // Remove operations for owner permission

    // Create operations bytes for active permission - only allow transfer
    byte[] operations = new byte[32];
    long transferPermission = 1L << 1;  // TransferContract is type 1

    // Convert permission to bytes
    operations[0] = (byte) (transferPermission & 0xFF);
    operations[1] = (byte) (0);
    operations[2] = (byte) (0);
    operations[3] = (byte) (0);

    // Build active permission requiring 2/3 signatures
    Permission.Builder activePermissionBuilder = Permission.newBuilder()
        .setType(Permission.PermissionType.Active)
        .setId(2)
        .setPermissionName("active")
        .setThreshold(2)  // Need 2 signatures
        .setParentId(0)   // Set parent to owner permission
        .setOperations(ByteString.copyFrom(operations));

    // Add 3 accounts with weight 1 each
    for (KeyPair key : activeKeyPairs) {
      activePermissionBuilder.addKeys(Key.newBuilder()
          .setAddress(ApiWrapper.parseAddress(key.toBase58CheckAddress()))
          .setWeight(1)
          .build());
    }
    Permission activePermission = activePermissionBuilder.build();

    // 3. Update account permissions
    AccountPermissionUpdateContract contract = AccountPermissionUpdateContract.newBuilder()
        .setOwnerAddress(ApiWrapper.parseAddress(ownerAddress))
        .setOwner(ownerPermission)
        .addActives(activePermission)
        .build();

    TransactionExtention txnExt = client.accountPermissionUpdate(contract);

    // Sign with owner private key
    Transaction signedTxn = client.signTransaction(txnExt, ownerKeyPair);
    String transaction = client.broadcastTransaction(signedTxn);

    // Wait for the transaction to be confirmed
    Thread.sleep(3000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(transaction);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

  @Test
  @Order(3)
  void testMultiSignTransfer() throws Exception {

    String toAddress = testAddress;

    // 1. Create transfer transaction, need setPermissionId in contract
    // transfer 1 TRX
    TransactionExtention txnExt = client.transfer(ownerAddress, testAddress, 1_000_000);

    Transaction transaction = txnExt.getTransaction();
    Transaction.Contract contract = transaction.getRawData().getContract(0);
    contract = contract.toBuilder()
        .setPermissionId(2)  // set active permission id
        .build();

    Transaction.raw rawData = transaction.getRawData().toBuilder()
        .setContract(0, contract)
        .build();

    Transaction transactionUpdate = transaction.toBuilder()
        .setRawData(rawData)
        .build();

    // 2. First account signs
    Transaction signedTxn1 = client.signTransaction(transactionUpdate, activeKeyPairs.get(0));

    // 3. Second account signs
    Transaction signedTxn2 = client.signTransaction(signedTxn1, activeKeyPairs.get(1));


    // 4. Broadcast transaction
    String txId = client.broadcastTransaction(signedTxn2);


    // Wait for the transaction to be confirmed
    Thread.sleep(3000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }
  
  @Test
  @Order(4)
  void testMultiOwnerSign() throws Exception {

    KeyPair owner1 = KeyPair.generate();
    KeyPair owner2 = KeyPair.generate();
    KeyPair owner3 = KeyPair.generate();

    //    System.out.println("owner1 " + owner1.toPrivateKey());
    // 150 TRX
    transferTrx(testAddress, owner1.toBase58CheckAddress(), 150_000_000, client.keyPair);

    // Build default owner permission  2/3
    Permission ownerPermission = Permission.newBuilder()
        .setType(Permission.PermissionType.Owner)
        .setId(0)
        .setPermissionName("owner")
        .setThreshold(2)
        .addKeys(Key.newBuilder()
            .setAddress(ApiWrapper.parseAddress(owner1.toBase58CheckAddress()))
            .setWeight(1)
            .build())
        .addKeys(Key.newBuilder()
            .setAddress(ApiWrapper.parseAddress(owner2.toBase58CheckAddress()))
            .setWeight(1)
            .build())
        .addKeys(Key.newBuilder()
            .setAddress(ApiWrapper.parseAddress(owner3.toBase58CheckAddress()))
            .setWeight(1)
            .build())
        .build();

    // Create operations bytes for active permission - only allow transfer
    byte[] operations = new byte[32];
    long transferPermission = 1L << 1;  // TransferContract is type 1

    // Convert permission to bytes
    operations[0] = (byte) (transferPermission & 0xFF);
    operations[1] = (byte) (0);
    operations[2] = (byte) (0);
    operations[3] = (byte) (0);

    Permission activePermission = Permission.newBuilder()
        .setType(Permission.PermissionType.Active)
        .setId(2)
        .setPermissionName("active")
        .setThreshold(1)
        .setParentId(0)
        .addKeys(Key.newBuilder()
            .setAddress(ApiWrapper.parseAddress(owner1.toBase58CheckAddress()))
            .setWeight(1)
            .build())
        .setOperations(ByteString.copyFrom(operations))
        .build();


    // Create permission update contract
    AccountPermissionUpdateContract contract = AccountPermissionUpdateContract.newBuilder()
        .setOwnerAddress(ApiWrapper.parseAddress(owner1.toBase58CheckAddress()))
        .setOwner(ownerPermission)
        .addActives(activePermission)
        .build();

    // Create transaction
    TransactionExtention txnExt = client.accountPermissionUpdate(contract);


    // Sign with owner private key
    Transaction signedTxn = client.signTransaction(txnExt, owner1);
    String txId = client.broadcastTransaction(signedTxn);

    //    System.out.println("transaction is " + txId);

    // Wait for the transaction to be confirmed
    Thread.sleep(3000);

    TransactionExtention transactionExtention =
        client.transfer(owner1.toBase58CheckAddress(), testAddress, 1_000_000L);

    Transaction signTransaction1 = client.signTransaction(transactionExtention, owner2);
    Transaction signTransaction2 = client.signTransaction(signTransaction1, owner3);


    // Broadcast transaction
    String txId2 = client.broadcastTransaction(signTransaction2);

    // Wait for transaction confirmation
    Thread.sleep(5000);

    // Verify if transaction is successful
    TransactionInfo info = client.getTransactionInfoById(txId2);
    assertNotNull(info);
    assertEquals(code.SUCESS, info.getResult());
  }

}
