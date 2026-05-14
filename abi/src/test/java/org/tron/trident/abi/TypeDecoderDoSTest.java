/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicArray;
import org.tron.trident.abi.datatypes.DynamicBytes;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.Uint256;

/**
 * Regression tests for decoder hardening against malicious ABI inputs.
 */
public class TypeDecoderDoSTest {

  /** A 32-byte slot whose value is 2^255 — far above Integer.MAX_VALUE. */
  private static final String HUGE_UINT_SLOT =
      "8000000000000000000000000000000000000000000000000000000000000000";

  @Test
  public void decodeUintAsInt_rejectsValueExceedingIntMax() {
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUintAsInt(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeDynamicArray_rejectsHugeLength() {
    // Length prefix = 2^255. Without the bound, intValue() truncates to
    // a negative int and new ArrayList<>(negative) throws — or worse, a
    // value just above Int.MAX would OOM the JVM on pre-allocation.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            HUGE_UINT_SLOT, 0, new TypeReference<DynamicArray<Uint256>>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsLengthExceedingRemainingInput() {
    // Length = 1000 fits in int (passes the bitLength check) but cannot
    // possibly fit in this 1-slot input. The decodeArrayElements cap
    // rejects before new ArrayList<>(1000) allocates.
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8"; // length=1000
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<Uint256>>() {}));
  }

  @Test
  public void decodeDynamicBytes_rejectsHugePayloadLength() {
    // Length prefix = 2^255 — without the bound, substring tries to read
    // 2^255 hex chars and throws SIOOBE.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeUtf8String_rejectsHugePayloadLength() {
    // Inherits the same protection via decodeDynamicBytes.
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUtf8String(HUGE_UINT_SLOT, 0));
  }

  @Test
  public void decodeDynamicArray_acceptsLegitimateArray() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000002"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "0000000000000000000000000000000000000000000000000000000000000002";
    DynamicArray<Uint256> result = TypeDecoder.decodeDynamicArray(
        legit, 0, new TypeReference<DynamicArray<Uint256>>() {});
    assertEquals(2, result.getValue().size());
  }

  @Test
  public void decodeDynamicBytes_acceptsLegitimateBytes() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000004"
            + "deadbeef00000000000000000000000000000000000000000000000000000000";
    DynamicBytes result = TypeDecoder.decodeDynamicBytes(legit, 0);
    assertEquals(4, result.getValue().length);
    assertEquals((byte) 0xde, result.getValue()[0]);
    assertEquals((byte) 0xef, result.getValue()[3]);
  }

  @Test
  public void decodeUtf8String_acceptsLegitimateString() {
    String legit =
        "0000000000000000000000000000000000000000000000000000000000000005"
            + "68656c6c6f000000000000000000000000000000000000000000000000000000";
    Utf8String result = TypeDecoder.decodeUtf8String(legit, 0);
    assertEquals("hello", result.getValue());
  }

  @Test
  public void decodeDynamicBytes_rejectsLengthExceedingRemainingInput() {
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8"; // length=1000
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(malicious, 0));
  }

  @Test
  public void decodeDynamicBytes_rejectsLengthCausingShiftOverflow() {
    // length = 2^30, shifting left by 1 produces 2^31 (= Integer.MIN_VALUE).
    String malicious =
        "0000000000000000000000000000000000000000000000000000000040000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicBytes(malicious, 0));
  }

  @Test
  public void decodeUtf8String_rejectsLengthExceedingRemainingInput() {
    String malicious =
        "00000000000000000000000000000000000000000000000000000000000003e8";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeUtf8String(malicious, 0));
  }

  public static class TwoStrings extends DynamicStruct {
    public String a;
    public String b;

    public TwoStrings(Utf8String a, Utf8String b) {
      super(a, b);
      this.a = a.getValue();
      this.b = b.getValue();
    }
  }

  @Test
  public void decodeDynamicStruct_rejectsParameterOffsetCausingMulOverflow() {
    // First dynamic-parameter offset = 0x40000001 (slightly above 2^30);
    // 0x40000001 * 2 + 64 overflows into a negative int.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000040000001"
            + "00000000000000000000000000000000000000000000000000000000000000c0"
            + "0000000000000000000000000000000000000000000000000000000000000001"
            + "6100000000000000000000000000000000000000000000000000000000000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicStruct(
            malicious, 0, new TypeReference<TwoStrings>() {}));
  }

  @Test
  public void decodeDynamicArray_rejectsElementLengthCausingOffsetOverflow() {
    // length=2; element0.length prefix = 0x7fffffe0 (~2^31 - 32, still ≤
    // Integer.MAX_VALUE). (length/32 + 2) * 64 then overflows int.
    String malicious =
        "0000000000000000000000000000000000000000000000000000000000000002"
            + "000000000000000000000000000000000000000000000000000000007fffffe0"
            + "0000000000000000000000000000000000000000000000000000000000000000";
    assertThrows(
        IllegalArgumentException.class,
        () -> TypeDecoder.decodeDynamicArray(
            malicious, 0, new TypeReference<DynamicArray<DynamicBytes>>() {}));
  }
}
