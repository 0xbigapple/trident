package org.trdient.benchmark;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.BlockId;
import org.tron.trident.core.utils.Sha256Hash;

/**
 * Benchmark for trdient.getBlock(...) using in-process gRPC server.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Threads(5)
public class TridentBenchmark extends BaseBenchmark {

  private Server server;
  private Server serverSolidity;
  private ManagedChannel channel;
  private ManagedChannel channelSolidity;
  private ApiWrapper client;

  // start in-process server 和 client
  @Setup(Level.Trial)
  public void setup() throws IOException {
    String serverName = InProcessServerBuilder.generateName();

    // start mock server
    server = InProcessServerBuilder.forName(serverName)
        .directExecutor() // directExecutor is recommended in JMH benchmarks
        .addService(new MockFullNodeService())
        .build()
        .start();

    serverSolidity = InProcessServerBuilder.forName(serverName + "_solidity")
        .directExecutor()
        .addService(new MockSolidityFullNodeService())
        .build()
        .start();

    // client connect mock server
    channel = InProcessChannelBuilder.forName(serverName)
        .directExecutor()
        .build();

    channelSolidity = InProcessChannelBuilder.forName(serverName + "_solidity")
        .directExecutor()
        .build();

    client = new ApiWrapper(channel, channelSolidity, KeyPair.generate().toPrivateKey());

    // use local FullNode
    //    client = new ApiWrapper("127.0.0.1:16669", "127.0.0.1:16670",
    //        ApiWrapper.generateAddress().toPrivateKey());
    byte[] id = new byte[32];
    new Random().nextBytes(id);
    client.enableLocalCreate(new BlockId(Sha256Hash.wrap(id)), 1000000000000L);
  }

  @TearDown(Level.Trial)
  public void tearDownClient() {
    // if your client holds resources, optionally close / shutdown channel here.
    // but we shut down channel in BaseBenchmark#tearDownServer
    client.close();
    server.shutdownNow();
    serverSolidity.shutdownNow();
  }

//  @Benchmark
//  @Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.MILLISECONDS)
//  @Measurement(iterations = 5000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
//  @Threads(5)
//  public void benchmarkGetBlockByNumber() throws Exception {
//    // use a fixed address; mock service ignores the address content
//    client.getBlockByNum(1234);
//  }

  @Benchmark
  public void benchmarkGetNowBlock() throws Exception {
    client.getNowBlock();
  }

  @Benchmark
  public void benchmarkGetContract() throws Exception {
    client.getContract("TTBqynFsfmm3UQrotrZYP7i69Pp7QG44in");
  }

  @Benchmark
  public void benchmarkTriggerContract() throws Exception {
    // use a fixed address; mock service ignores the address content
    client.triggerContract(client.keyPair.toBase58CheckAddress(),
        client.keyPair.toBase58CheckAddress(),
        null, 0, 0, null, 123);

  }
}

