package org.trdient.benchmark;

import io.grpc.stub.StreamObserver;
import org.tron.trident.api.GrpcAPI.BlockReq;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.api.GrpcAPI.NumberMessage;
import org.tron.trident.api.WalletGrpc;
import org.tron.trident.api.WalletSolidityGrpc;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.BlockHeader;
import org.tron.trident.proto.Response.BlockExtention;

// in-process mock server
public class MockSolidityFullNodeService extends WalletSolidityGrpc.WalletSolidityImplBase {

  @Override
  public void getNowBlock(EmptyMessage request, StreamObserver<Block> responseObserver) {
    Block block = Block.newBuilder()
        .setBlockHeader(BlockHeader.newBuilder()
            .setRawData(BlockHeader.raw.newBuilder()
                .setNumber(12345)))
        .build();

    responseObserver.onNext(block);
    responseObserver.onCompleted();
  }

  @Override
  public void getBlock(BlockReq request, StreamObserver<BlockExtention> responseObserver) {
    BlockExtention block = BlockExtention.newBuilder()
        .setBlockHeader(BlockHeader.newBuilder()
            .setRawData(BlockHeader.raw.newBuilder()
                .setNumber(12345)))
        .build();
    responseObserver.onNext(block);
    responseObserver.onCompleted();
  }

  @Override
  public void getBlockByNum2(NumberMessage numberMessage,
      StreamObserver<BlockExtention> responseObserver) {

    BlockExtention block = BlockExtention.newBuilder()
        .setBlockHeader(BlockHeader.newBuilder()
            .setRawData(BlockHeader.raw.newBuilder()
                .setNumber(12345)))
        .build();

    responseObserver.onNext(block);
    responseObserver.onCompleted();
  }
}
