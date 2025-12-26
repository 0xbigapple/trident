package org.trdient.benchmark;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.tron.trident.api.GrpcAPI.BlockReq;
import org.tron.trident.api.GrpcAPI.BytesMessage;
import org.tron.trident.api.GrpcAPI.EmptyMessage;
import org.tron.trident.api.GrpcAPI.NumberMessage;
import org.tron.trident.api.WalletGrpc;
import org.tron.trident.core.contract.abi.AbiUtils;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.proto.Chain.Block;
import org.tron.trident.proto.Chain.BlockHeader;
import org.tron.trident.proto.Common.SmartContract;
import org.tron.trident.proto.Common.SmartContract.ABI;
import org.tron.trident.proto.Response.BlockExtention;

// in-process mock server
public class MockFullNodeService extends WalletGrpc.WalletImplBase {

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

  @Override
  public void getContract(BytesMessage request, StreamObserver<SmartContract> responseObserver) {

    String abiStr =
        "{\"entrys\":[{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"initTotal\",\"type\":\"uint256\"}],\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"balanceOf\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"i\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"j\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]}";
    //bytecode
    String bytecode =
        "608060405260405161032a38038061032a83398181016040528101906100259190610069565b8060018190555050610094565b5f80fd5b5f819050919050565b61004881610036565b8114610052575f80fd5b50565b5f815190506100638161003f565b92915050565b5f6020828403121561007e5761007d610032565b5b5f61008b84828501610055565b91505092915050565b610289806100a15f395ff3fe608060405234801561000f575f80fd5b50d3801561001b575f80fd5b50d28015610027575f80fd5b506004361061004c575f3560e01c80631ab06ee5146100505780639cc7f7081461006c575b5f80fd5b61006a6004803603810190610065919061012f565b61009c565b005b6100866004803603810190610081919061016d565b6100e4565b60405161009391906101a7565b60405180910390f35b805f808481526020019081526020015f20546001546100bb91906101ed565b6100c59190610220565b600181905550805f808481526020019081526020015f20819055505050565b5f602052805f5260405f205f915090505481565b5f80fd5b5f819050919050565b61010e816100fc565b8114610118575f80fd5b50565b5f8135905061012981610105565b92915050565b5f8060408385031215610145576101446100f8565b5b5f6101528582860161011b565b92505060206101638582860161011b565b9150509250929050565b5f60208284031215610182576101816100f8565b5b5f61018f8482850161011b565b91505092915050565b6101a1816100fc565b82525050565b5f6020820190506101ba5f830184610198565b92915050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52601160045260245ffd5b5f6101f7826100fc565b9150610202836100fc565b925082820390508181111561021a576102196101c0565b5b92915050565b5f61022a826100fc565b9150610235836100fc565b925082820190508082111561024d5761024c6101c0565b5b9291505056fea26474726f6e58221220c1c1c36ad3fb4f0e2ffaffca14de80c05c6d478081e6eab843f189e2a5f7dfc964736f6c63430008160033";

    ABI.Builder abibuilder = ABI.newBuilder();
    AbiUtils.loadAbiFromJson(abiStr, abibuilder);
    SmartContract contract = SmartContract.newBuilder()
        .setAbi(abibuilder)
        .setBytecode(ByteString.copyFrom(bytecode.getBytes()))
        .setContractAddress(
            ByteString.copyFrom(KeyPair.generate().toBase58CheckAddress().getBytes()))
        .setName("testContract")
        .setOriginEnergyLimit(10)
        .build();
    responseObserver.onNext(contract);
    responseObserver.onCompleted();
  }
}
