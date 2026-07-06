package org.tron.trident.abi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tron.trident.abi.datatypes.DynamicStruct;
import org.tron.trident.abi.datatypes.StaticStruct;
import org.tron.trident.abi.datatypes.Type;
import org.tron.trident.abi.datatypes.Utf8String;
import org.tron.trident.abi.datatypes.generated.StaticArray2;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.abi.datatypes.reflection.Parameterized;

/**
 * Regression tests for the constructor-reflection decode path
 * ({@code new TypeReference<MyStruct>() {}} with no innerTypes) on struct classes
 * whose fields previously broke it: StaticArrayN members, nested static structs
 * with non-public Java fields, and doubly-nested static structs.
 */
public class StructFieldReflectionDecodeTest {

  /** Solidity: struct Order { uint256[2] limits; string name; } */
  public static class Order extends DynamicStruct {
    public Order(@Parameterized(type = Uint256.class) StaticArray2<Uint256> limits,
        Utf8String name) {
      super(limits, name);
    }
  }

  /** Static struct whose Java fields are intentionally non-public. */
  public static class Point extends StaticStruct {
    private final BigInteger x;
    private final BigInteger y;

    public Point(Uint256 x, Uint256 y) {
      super(x, y);
      this.x = x.getValue();
      this.y = y.getValue();
    }

    public BigInteger getX() {
      return x;
    }

    public BigInteger getY() {
      return y;
    }
  }

  /** Solidity: struct Person { Point location; string name; } */
  public static class Person extends DynamicStruct {
    public Person(Point location, Utf8String name) {
      super(location, name);
    }
  }

  public static class Inner extends StaticStruct {
    public Inner(Uint256 x, Uint256 y) {
      super(x, y);
    }
  }

  /** Nested static struct spanning 3 words, not 2 (its constructor parameter count). */
  public static class Mid extends StaticStruct {
    public Mid(Inner inner, Uint256 d) {
      super(inner, d);
    }
  }

  public static class Outer extends StaticStruct {
    public Outer(Mid mid, Uint256 c) {
      super(mid, c);
    }
  }

  private static final String DYNAMIC_STRUCT_HEAD =
      "0000000000000000000000000000000000000000000000000000000000000020";

  @Test
  public void testDecodeStructWithStaticArrayFieldViaReflection() {
    Order order = new Order(
        new StaticArray2<>(Uint256.class,
            new Uint256(BigInteger.ONE), new Uint256(BigInteger.TEN)),
        new Utf8String("hi"));

    String returnedData = DYNAMIC_STRUCT_HEAD + TypeEncoder.encode(order);

    List<Type> decoded = FunctionReturnDecoder.decode(
        returnedData,
        Utils.convert(Arrays.asList(new TypeReference<Order>() {})));

    assertEquals(1, decoded.size());
    assertTrue(decoded.get(0) instanceof Order);
    Order decodedOrder = (Order) decoded.get(0);

    @SuppressWarnings("unchecked")
    StaticArray2<Uint256> limits = (StaticArray2<Uint256>) decodedOrder.getValue().get(0);
    assertEquals(
        Arrays.asList(new Uint256(BigInteger.ONE), new Uint256(BigInteger.TEN)),
        limits.getValue());
    assertEquals(new Utf8String("hi"), decodedOrder.getValue().get(1));
  }

  @Test
  public void testDecodeStructWithPrivateFieldNestedStaticStruct() {
    Person person = new Person(
        new Point(new Uint256(BigInteger.valueOf(3)), new Uint256(BigInteger.valueOf(7))),
        new Utf8String("alice"));

    String returnedData = DYNAMIC_STRUCT_HEAD + TypeEncoder.encode(person);

    List<Type> decoded = FunctionReturnDecoder.decode(
        returnedData,
        Utils.convert(Arrays.asList(new TypeReference<Person>() {})));

    assertEquals(1, decoded.size());
    assertTrue(decoded.get(0) instanceof Person);
    Person decodedPerson = (Person) decoded.get(0);

    Point location = (Point) decodedPerson.getValue().get(0);
    assertEquals(BigInteger.valueOf(3), location.getX());
    assertEquals(BigInteger.valueOf(7), location.getY());
    assertEquals(new Utf8String("alice"), decodedPerson.getValue().get(1));
  }

  @Test
  public void testDecodeDoublyNestedStaticStruct() {
    Outer outer = new Outer(
        new Mid(
            new Inner(new Uint256(BigInteger.ONE), new Uint256(BigInteger.valueOf(2))),
            new Uint256(BigInteger.valueOf(3))),
        new Uint256(BigInteger.valueOf(4)));

    String returnedData = TypeEncoder.encode(outer);

    List<Type> decoded = FunctionReturnDecoder.decode(
        returnedData,
        Utils.convert(Arrays.asList(new TypeReference<Outer>() {})));

    assertEquals(1, decoded.size());
    assertTrue(decoded.get(0) instanceof Outer);
    Outer decodedOuter = (Outer) decoded.get(0);

    Mid mid = (Mid) decodedOuter.getValue().get(0);
    Inner inner = (Inner) mid.getValue().get(0);
    assertEquals(new Uint256(BigInteger.ONE), inner.getValue().get(0));
    assertEquals(new Uint256(BigInteger.valueOf(2)), inner.getValue().get(1));
    assertEquals(new Uint256(BigInteger.valueOf(3)), mid.getValue().get(1));
    assertEquals(new Uint256(BigInteger.valueOf(4)), decodedOuter.getValue().get(1));
  }
}
