package com.sergeysav.bignum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author sergeys
 */
public class Float128Test {
    @Test
    void add() {
        Float128 zero = new Float128();

        Float128 half = Float128.bytesOf(4611123068473966592L, 0);

        Float128 one = Float128.ONE;
        Assertions.assertEquals(one, new Float128(zero).add(one));
        Assertions.assertEquals(one, new Float128(half).add(half));
        Assertions.assertEquals(one, new Float128(one).add(zero));

        Float128 two = Float128.bytesOf(4611686018427387904L, 0);
        Assertions.assertEquals(two, new Float128(zero).add(two));
        Assertions.assertEquals(two, new Float128(one).add(one));
        Assertions.assertEquals(two, new Float128(two).add(zero));

        Float128 three = Float128.bytesOf(4611826755915743232L, 0);
        Assertions.assertEquals(three, new Float128(zero).add(three));
        Assertions.assertEquals(three, new Float128(one).add(two));
        Assertions.assertEquals(three, new Float128(two).add(one));
        Assertions.assertEquals(three, new Float128(three).add(zero));

        Float128 four = Float128.bytesOf(4611967493404098560L, 0);
        Assertions.assertEquals(four, new Float128(zero).add(four));
        Assertions.assertEquals(four, new Float128(one).add(three));
        Assertions.assertEquals(four, new Float128(two).add(two));
        Assertions.assertEquals(four, new Float128(three).add(one));
        Assertions.assertEquals(four, new Float128(four).add(zero));

        Float128 five = Float128.bytesOf(4612037862148276224L, 0);
        Assertions.assertEquals(five, new Float128(zero).add(five));
        Assertions.assertEquals(five, new Float128(one).add(four));
        Assertions.assertEquals(five, new Float128(two).add(three));
        Assertions.assertEquals(five, new Float128(three).add(two));
        Assertions.assertEquals(five, new Float128(four).add(one));
        Assertions.assertEquals(five, new Float128(five).add(zero));

        Float128 six = Float128.bytesOf(4612108230892453888L, 0);
        Assertions.assertEquals(six, new Float128(zero).add(six));
        Assertions.assertEquals(six, new Float128(one).add(five));
        Assertions.assertEquals(six, new Float128(two).add(four));
        Assertions.assertEquals(six, new Float128(three).add(three));
        Assertions.assertEquals(six, new Float128(four).add(two));
        Assertions.assertEquals(six, new Float128(five).add(one));
        Assertions.assertEquals(six, new Float128(six).add(zero));

        Float128 seven = Float128.bytesOf(4612178599636631552L, 0);
        Assertions.assertEquals(seven, new Float128(zero).add(seven));
        Assertions.assertEquals(seven, new Float128(one).add(six));
        Assertions.assertEquals(seven, new Float128(two).add(five));
        Assertions.assertEquals(seven, new Float128(three).add(four));
        Assertions.assertEquals(seven, new Float128(four).add(three));
        Assertions.assertEquals(seven, new Float128(five).add(two));
        Assertions.assertEquals(seven, new Float128(six).add(one));
        Assertions.assertEquals(seven, new Float128(seven).add(zero));

        Float128 negativeHalf = Float128.bytesOf(-4612248968380809216L, 0);
        Float128 negativeOneAndAHalf = Float128.bytesOf(-4611826755915743232L, 0);
        Assertions.assertEquals(negativeHalf, new Float128(one).add(negativeOneAndAHalf));
        Assertions.assertEquals(negativeHalf, new Float128(negativeOneAndAHalf).add(one));

        Float128 negativeOne = Float128.bytesOf(-4611967493404098560L, 0);
        Assertions.assertEquals(zero, new Float128(negativeOne).add(one));
        Assertions.assertEquals(zero, new Float128(one).add(negativeOne));

        Float128 oneHalf = Float128.bytesOf(4611826755915743232L, 0);
        Float128 threeNew = Float128.bytesOf(4612108230892453888L, 0);
        Assertions.assertEquals(threeNew, Float128.add(oneHalf, oneHalf));
    }

    @Test
    void addSubnormal() {
        Float128 smallest = Float128.bytesOf(0, 1);
        Float128 smallest2 = Float128.bytesOf(0, 2);
        Float128 smallest3 = Float128.bytesOf(0, 3);

        Assertions.assertEquals(smallest2, Float128.add(smallest, smallest));
        Assertions.assertEquals(smallest3, Float128.add(smallest2, smallest));
        Assertions.assertEquals(smallest3, Float128.add(smallest, smallest2));

        Float128 biggest = Float128.bytesOf(281474976710655L, -1L);
        Float128 smallestNormal = Float128.bytesOf(281474976710656L, 0);
        Assertions.assertEquals(smallestNormal, Float128.add(biggest, smallest));
        Assertions.assertEquals(smallestNormal, Float128.add(smallest, biggest));

        Float128 sNormP1 = Float128.bytesOf(281474976710656L, 1);
        Assertions.assertEquals(sNormP1, Float128.add(smallestNormal, smallest));
        Assertions.assertEquals(sNormP1, Float128.add(smallest, smallestNormal));
    }

    @Test
    void multiply() {
        Float128 zero = new Float128();

        Float128 half = Float128.bytesOf(4611123068473966592L, 0);
        Assertions.assertEquals(zero, new Float128(half).multiply(zero));
        Assertions.assertEquals(zero, new Float128(zero).multiply(half));

        Float128 one = Float128.ONE;
        Assertions.assertEquals(one, new Float128(one).multiply(one));
        Assertions.assertEquals(half, new Float128(half).multiply(one));
        Assertions.assertEquals(half, new Float128(one).multiply(half));

        Float128 two = Float128.bytesOf(4611686018427387904L, 0);
        Assertions.assertEquals(two, new Float128(two).multiply(one));
        Assertions.assertEquals(two, new Float128(one).multiply(two));
        Assertions.assertEquals(half, new Float128(half).multiply(one));
        Assertions.assertEquals(half, new Float128(one).multiply(half));

        Float128 oneAndAHalf = Float128.bytesOf(4611545280939032576L, 0);
        Float128 twoAndTwoFive = Float128.bytesOf(4611721202799476736L, 0);
        Assertions.assertEquals(twoAndTwoFive, new Float128(oneAndAHalf).multiply(oneAndAHalf));
    }

    @Test
    void divide() {
        Float128 four = Float128.bytesOf(4611967493404098560L, 0);
        Float128 two = Float128.bytesOf(4611686018427387904L, 0);
        Assertions.assertEquals(two, new Float128(four).divide(two));
        Assertions.assertEquals(Float128.ONE, new Float128(two).divide(two));

        Float128 oneAndAHalf = Float128.bytesOf(4611545280939032576L, 0);
        Float128 twoAndTwoFive = Float128.bytesOf(4611721202799476736L, 0);
        Assertions.assertEquals(oneAndAHalf, new Float128(twoAndTwoFive).divide(oneAndAHalf));
    }

    @Test
    void modulo() {
        Float128 four = Float128.bytesOf(4611967493404098560L, 0);
        Float128 two = Float128.bytesOf(4611686018427387904L, 0);
        Assertions.assertEquals(Float128.ZERO, new Float128(four).modulo(two));
        Assertions.assertEquals(Float128.ZERO, new Float128(two).modulo(two));

        Float128 pointSevenFive = Float128.bytesOf(4611263805962321920L, 0);
        Float128 oneAndAHalf = Float128.bytesOf(4611545280939032576L, 0);
        Float128 twoAndTwoFive = Float128.bytesOf(4611721202799476736L, 0);
        Assertions.assertEquals(pointSevenFive, new Float128(twoAndTwoFive).modulo(oneAndAHalf));
    }

    @Test
    void from() {
        Assertions.assertEquals(new Float128(), Float128.from(0.0));
        Assertions.assertEquals(Float128.bytesOf(4611123068473966592L, 0), Float128.from(0.5));
        Assertions.assertEquals(Float128.ONE, Float128.from(1.0));
        Assertions.assertEquals(Float128.bytesOf(4611686018427387904L, 0), Float128.from(2.0));
        Assertions.assertEquals(Float128.bytesOf(4611826755915743232L, 0), Float128.from(3.0));
        Assertions.assertEquals(Float128.bytesOf(4611967493404098560L, 0), Float128.from(4.0));
        Assertions.assertEquals(Float128.bytesOf(4612037862148276224L, 0), Float128.from(5.0));
        Assertions.assertEquals(Float128.bytesOf(4612108230892453888L, 0), Float128.from(6.0));
        Assertions.assertEquals(Float128.bytesOf(4612178599636631552L, 0), Float128.from(7.0));
        Assertions.assertEquals(Float128.bytesOf(-4612248968380809216L, 0), Float128.from(-0.5));
        Assertions.assertEquals(Float128.bytesOf(-4611826755915743232L, 0), Float128.from(-1.5));
        Assertions.assertEquals(Float128.bytesOf(-4611967493404098560L, 0), Float128.from(-1.0));
        Assertions.assertEquals(Float128.bytesOf(4611721202799476736L, 0), Float128.from(2.25));
    }

    @Test
    void testToFullString() {
        for (int i = -10_000; i < 10_000; i++) {
            Float128 float128 = Float128.from(i);
            Assertions.assertEquals(Integer.toString(i), float128.toFullString(-1));
        }
        for (int i = 0; i < 63; i++) {
            Float128 float128 = Float128.from(1L << i);
            Assertions.assertEquals(Long.toString(1L << i), float128.toFullString(-1), "2^" + i + " not equal");
        }
    }

    @Test
    void testCompare() {
        for (int i = -100_000; i < 100_000; i++) {
            Float128 float128a = Float128.from(i);
            Float128 float128b = Float128.from(i + 1);
            Assertions.assertTrue(float128a.compareTo(float128b) < 0, i + " < " + (i + 1));
        }
    }
}
