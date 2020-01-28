package com.sergeysav.bignum;

import com.sergeysav.bignum.Int512;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author sergeys
 */
class Int512Test {

    @Test
    void add() {
        Assertions.assertEquals(Int512.from(0), Int512.add(Int512.from(-1), Int512.from(1)));
        Assertions.assertEquals(Int512.from(2), Int512.add(Int512.from(1), Int512.from(1)));
        Assertions.assertEquals(Int512.bytesOf(0, 0, 0, 0, 0, 0, 0, Long.MIN_VALUE),
                                Int512.add(Int512.from(Long.MAX_VALUE), Int512.from(1)));
    }

    @Test
    void negate() {
        Assertions.assertEquals(Int512.from(-1), Int512.from(1).negate());
        Assertions.assertEquals(Int512.from(1), Int512.from(-1).negate());
    }

    @Test
    void toStringTest() {
        Assertions.assertEquals("-1", Int512.from(-1).toString());
        Assertions.assertEquals("0", Int512.from(0).toString());
        Assertions.assertEquals("1", Int512.from(1).toString());
        Assertions.assertEquals("2", Int512.from(2).toString());
        Assertions.assertEquals("10", Int512.from(10).toString());
        Assertions.assertEquals("19", Int512.from(19).toString());
        Assertions.assertEquals("1241240003671", Int512.from(1241240003671L).toString());
    }

    @Test
    void shiftLeft() {
        Assertions.assertEquals(Int512.from(1), Int512.from(1).shiftLeft(0));
        Assertions.assertEquals(Int512.from(2), Int512.from(1).shiftLeft(1));
        Assertions.assertEquals(Int512.from(4), Int512.from(1).shiftLeft(2));
        Assertions.assertEquals(Int512.from(8), Int512.from(1).shiftLeft(3));
        Assertions.assertEquals(Int512.from(16), Int512.from(1).shiftLeft(4));
        Assertions.assertEquals(Int512.from(32), Int512.from(1).shiftLeft(5));
        Assertions.assertEquals(Int512.from(64), Int512.from(1).shiftLeft(6));
        Assertions.assertEquals(Int512.from(128), Int512.from(1).shiftLeft(7));
        Assertions.assertEquals(Int512.from(256), Int512.from(1).shiftLeft(8));

        Assertions.assertEquals(Int512.from(255), Int512.from(255).shiftLeft(0));
        Assertions.assertEquals(Int512.from(255 * 2), Int512.from(255).shiftLeft(1));
        Assertions.assertEquals(Int512.from(255 * 4), Int512.from(255).shiftLeft(2));
        Assertions.assertEquals(Int512.from(255 * 8), Int512.from(255).shiftLeft(3));
        Assertions.assertEquals(Int512.from(255 * 16), Int512.from(255).shiftLeft(4));
        Assertions.assertEquals(Int512.from(255 * 32), Int512.from(255).shiftLeft(5));
        Assertions.assertEquals(Int512.from(255 * 64), Int512.from(255).shiftLeft(6));
        Assertions.assertEquals(Int512.from(255 * 128), Int512.from(255).shiftLeft(7));
        Assertions.assertEquals(Int512.from(255 * 256), Int512.from(255).shiftLeft(8));

        Assertions.assertEquals(Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L & ~(0x1)),
                                Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L).shiftLeft(1));
    }

    @Test
    void shiftRightUnsigned() {
        Assertions.assertEquals(Int512.from(256), Int512.from(256).shiftRightUnsigned(0));
        Assertions.assertEquals(Int512.from(128), Int512.from(256).shiftRightUnsigned(1));
        Assertions.assertEquals(Int512.from(64), Int512.from(256).shiftRightUnsigned(2));
        Assertions.assertEquals(Int512.from(32), Int512.from(256).shiftRightUnsigned(3));
        Assertions.assertEquals(Int512.from(16), Int512.from(256).shiftRightUnsigned(4));
        Assertions.assertEquals(Int512.from(8), Int512.from(256).shiftRightUnsigned(5));
        Assertions.assertEquals(Int512.from(4), Int512.from(256).shiftRightUnsigned(6));
        Assertions.assertEquals(Int512.from(2), Int512.from(256).shiftRightUnsigned(7));
        Assertions.assertEquals(Int512.from(1), Int512.from(256).shiftRightUnsigned(8));
        Assertions.assertEquals(Int512.from(0), Int512.from(256).shiftRightUnsigned(9));

        Assertions.assertEquals(Int512.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L, -1L, -1L, -1L, -1L),
                                Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L).shiftRightUnsigned(1));
        Assertions.assertEquals(Int512.bytesOf(Long.MAX_VALUE >> 1, -1L, -1L, -1L, -1L, -1L, -1L, -1L),
                                Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L).shiftRightUnsigned(2));
    }

    @Test
    void shiftRightSigned() {
        Assertions.assertEquals(Int512.from(256), Int512.from(256).shiftRightSigned(0));
        Assertions.assertEquals(Int512.from(128), Int512.from(256).shiftRightSigned(1));
        Assertions.assertEquals(Int512.from(64), Int512.from(256).shiftRightSigned(2));
        Assertions.assertEquals(Int512.from(32), Int512.from(256).shiftRightSigned(3));
        Assertions.assertEquals(Int512.from(16), Int512.from(256).shiftRightSigned(4));
        Assertions.assertEquals(Int512.from(8), Int512.from(256).shiftRightSigned(5));
        Assertions.assertEquals(Int512.from(4), Int512.from(256).shiftRightSigned(6));
        Assertions.assertEquals(Int512.from(2), Int512.from(256).shiftRightSigned(7));
        Assertions.assertEquals(Int512.from(1), Int512.from(256).shiftRightSigned(8));
        Assertions.assertEquals(Int512.from(0), Int512.from(256).shiftRightSigned(9));

        Assertions.assertEquals(Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L),
                                Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L).shiftRightSigned(1));
        Assertions.assertEquals(Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L),
                                Int512.bytesOf(-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L).shiftRightSigned(2));
    }

    @Test
    void testGetBit() {
        Assertions.assertEquals(1, Int512.from(255).getBit(0));
        Assertions.assertEquals(1, Int512.from(255).getBit(1));
        Assertions.assertEquals(1, Int512.from(255).getBit(2));
        Assertions.assertEquals(1, Int512.from(255).getBit(3));
        Assertions.assertEquals(1, Int512.from(255).getBit(4));
        Assertions.assertEquals(1, Int512.from(255).getBit(5));
        Assertions.assertEquals(1, Int512.from(255).getBit(6));
        Assertions.assertEquals(1, Int512.from(255).getBit(7));
        Assertions.assertEquals(0, Int512.from(255).getBit(8));
        Assertions.assertEquals(0, Int512.from(255).getBit(9));
        Assertions.assertEquals(0, Int512.from(255).getBit(10));
    }

    @Test
    void testMultiply() {
        Assertions.assertEquals(Int512.from(2), Int512.from(2).multiply(Int512.from(1)));
        Assertions.assertEquals(Int512.from(2), Int512.from(1).multiply(Int512.from(2)));
        Assertions.assertEquals(Int512.from(4), Int512.from(2).multiply(Int512.from(2)));
        Assertions.assertEquals(Int512.from(6), Int512.from(2).multiply(Int512.from(3)));
        Assertions.assertEquals(Int512.from(6), Int512.from(3).multiply(Int512.from(2)));

        Assertions.assertEquals(Int512.from(255 * 255), Int512.from(255).multiply(Int512.from(255)));

        Assertions.assertEquals(Int512.bytesOf(0, 0, 0, 0, 0, 0, 1, -1 & ~(0x1)),
                                Int512.bytesOf(0, 0, 0, 0, 0, 0, 0, -1).multiply(Int512.from(2)));
    }

    @Test
    void testDivision() {
        Assertions.assertEquals(Int512.from(3), Int512.from(7).divide(Int512.from(2)));
        Assertions.assertEquals(Int512.from(1), Int512.from(7).remainder(Int512.from(2)));

        Assertions.assertEquals(Int512.from(1), Int512.from(7).divide(Int512.from(5)));
        Assertions.assertEquals(Int512.from(2), Int512.from(7).remainder(Int512.from(5)));
    }
}