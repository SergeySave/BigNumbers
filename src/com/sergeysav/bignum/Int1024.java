package com.sergeysav.bignum;

import java.util.Arrays;

/**
 * Represents a 1024 bit 2's complement integer
 *
 * @author sergeys
 */
public final class Int1024 implements Comparable<Int1024> {

    /**
     * The number of longs used to store the data for this type
     */
    private static final int LONGS = 16;

    /**
     * A constant equal to zero
     */
    public static final Int1024 ZERO = new Int1024();
    /**
     * A constant equal to one
     */
    public static final Int1024 ONE = Int1024.from(1);
    /**
     * A constant equal to ten
     */
    public static final Int1024 TEN = Int1024.from(10);
    /**
     * The maximum value
     */
    public static final Int1024 MAX_VALUE = Int1024.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L);

    /**
     * The minimum value
     */
    public static final Int1024 MIN_VALUE = Int1024.bytesOf(Long.MIN_VALUE, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);

    /**
     * The backing bits (stored as longs)
     */
    private long[] data;

    /**
     * Create a new integer equaling zero
     */
    public Int1024() {
        data = new long[LONGS];
    }

    /**
     * Create a new integer with the same value as another
     *
     * @param other the integer value to copy
     */
    public Int1024(Int1024 other) {
        this();
        
        System.arraycopy(other.data, 0, this.data, 0, LONGS);
    }

    /**
     * Creates an integer from a given long
     *
     * The returned integer will be sign extended to this integer's length
     *
     * @param num the long value
     * @return an integer representing the same number as the given long
     */
    public static Int1024 from(long num) {
        Int1024 val = new Int1024();

        //Copy the long
        val.data[LONGS - 1] = num;

        //Sign extension
        if (num < 0) {
            for (int i = 0; i < LONGS - 1; i++) {
                val.data[i] = -1L;
            }
        }
        
        return val;
    }

    /**
     * Creates a new integer given the binary data for the integer
     *
     * WARNING: Advanced method
     *
     * @param parts the data to use (must be length 16)
     * @return a new integer of the given data
     */
    public static Int1024 bytesOf(long... parts) {
        if (parts.length != 16) {
            throw new IllegalArgumentException("Incorrect number of bytes");
        }

        Int1024 val = new Int1024();

        System.arraycopy(parts, 0, val.data, 0, LONGS);

        return val;
    }

    /**
     * Add two integers returning a new integer object
     *
     * @param a the first integer
     * @param b the second integer
     * @return the result of the addition
     */
    public static Int1024 add(Int1024 a, Int1024 b) {
        return new Int1024(a).add(b);
    }

    /**
     * Adds another integer to this one
     *
     * This modifies the current integer
     *
     * @param b the other integer
     * @return this for chaining
     */
    public Int1024 add(Int1024 b) {
        long carry = 0;
    
        for (int i = LONGS - 1; i >= 0; i--) {
            long temp = this.data[i] + b.data[i] + carry;

            if (carry == 1){
                carry = (Long.compareUnsigned(temp, this.data[i]) <= 0 && Long.compareUnsigned(temp, b.data[i]) <= 0)
                                        ? 1 : 0;
            } else {
                carry = (Long.compareUnsigned(temp, this.data[i]) < 0 && Long.compareUnsigned(temp, b.data[i]) < 0)
                                        ? 1 : 0;
            }
            
            data[i] = temp;
        }
        
        return this;
    }

    /**
     * Negate a given integer
     *
     * @param a the integer to negate
     * @return the result of the negation
     */
    public static Int1024 negate(Int1024 a) {
        return new Int1024(a).negate();
    }

    /**
     * Negate this integer
     *
     * This modifies the current integer
     *
     * @return this for chaining
     */
    public Int1024 negate() {
        for (int i = 0; i < LONGS; i++) {
            data[i] = ~data[i];
        }
        
        return add(ONE);
    }

    /**
     * Subtract two integers returning a new integer object
     *
     * @param a the first integer
     * @param b the second integer
     * @return the result of the subtraction
     */
    public static Int1024 subtract(Int1024 a, Int1024 b) {
        return new Int1024(a).subtract(b);
    }

    /**
     * Subtracts another integer from this one
     *
     * This modifies the current integer
     *
     * @param b the other integer
     * @return this for chaining
     */
    public Int1024 subtract(Int1024 b) {
        return add(Int1024.negate(b));
    }

    /**
     * Shift this integer left by a given number of bits
     *
     * @param bits the number of bits to shift by
     * @return this for chaining
     */
    public Int1024 shiftLeft(int bits) {
        this.data = com.sergeysav.bignum.CommonUtils.shiftLeft(this.data, bits);
        return this;    }

    /**
     * Shift this integer right by a given number of bits
     *
     * This will fill the new left bits with 0s
     *
     * @param bits the number of bits to shift by
     * @return this for chaining
     */
    public Int1024 shiftRightUnsigned(int bits) {
        this.data = com.sergeysav.bignum.CommonUtils.shiftRightUnsigned(this.data, bits);
        return this;    }

    /**
     * Shift this integer right by a given number of bits
     *
     * This will fill the new left bits with the sign bit
     *
     * @param bits the number of bits to shift by
     * @return this for chaining
     */
    public Int1024 shiftRightSigned(int bits) {
        this.data = com.sergeysav.bignum.CommonUtils.shiftRightSigned(this.data, bits);
        return this;
    }

    /**
     * Shift an integer left by a given number of bits
     *
     * @param num the integer to shift
     * @param bits the number of bits to shift by
     * @return an integer that is shifted by the given amount
     */
    public static Int1024 shiftLeft(Int1024 num, int bits) {
        return new Int1024(num).shiftLeft(bits);
    }

    /**
     * Shift an integer right by a given number of bits
     *
     * This will fill the new left bits with 0s
     *
     * @param num the integer to shift
     * @param bits the number of bits to shift by
     * @return an integer that is shifted by the given amount
     */
    public static Int1024 shiftRightUnsigned(Int1024 num, int bits) {
        return new Int1024(num).shiftRightUnsigned(bits);
    }

    /**
     * Shift an integer right by a given number of bits
     *
     * This will fill the new left bits with the sign bit
     *
     * @param num the integer to shift
     * @param bits the number of bits to shift by
     * @return an integer that is shifted by the given amount
     */
    public static Int1024 shiftRightSigned(Int1024 num, int bits) {
        return new Int1024(num).shiftRightSigned(bits);
    }

    /**
     * Get a given bit of this integer
     *
     * @param bit the bit to get
     * @return 0 if the bit is 0, 1 if the bit is 1
     */
    public int getBit(int bit) {
        return getBit(data, bit);
    }

    /**
     * Set a given bit of this integer
     *
     * @param bit the bit to set
     * @param val the value to set (either 0 or 1)
     */
    public void setBit(int bit, int val) {
        setBit(data, bit, val);
    }

    private static int getBit(long[] longs, int bit) {
        return (longs[longs.length - 1 - (bit / 64)] & (1L << (bit % 64))) != 0 ? 1 : 0;
    }

    private static void setBit(long[] longs, int bit, int val) {
        if (val == 0) {
            longs[longs.length - 1 - (bit / 64)] &= ~(1L << (bit % 64));
        } else {
            longs[longs.length - 1 - (bit / 64)] |= (1L << (bit % 64));
        }
    }

    /**
     * Multiply a given integer by this one
     *
     * This modifies the current integer
     *
     * @param b the integer to multiply by
     * @return this for chaining
     */
    public Int1024 multiply(Int1024 b) {
        long[] oldData = data;
        this.data = new long[LONGS];

        Int1024 shifting = new Int1024(b);
        int shiftAmount = 0;

        for (int i = LONGS - 1; i >= 0; i--) {
            for (int j = 0; j < 64; j++) {
                if (getBit(oldData, j +  64 * (LONGS - 1 - i)) != 0) {
                    shifting.shiftLeft(shiftAmount);
                    add(shifting);
                    shiftAmount = 0;
                }
                shiftAmount++;
            }
        }

        return this;
    }

    /**
     * Multiply two integers together
     *
     * @param a the first integer
     * @param b the second integer
     * @return the result of the multiplication
     */
    public static Int1024 multiply(Int1024 a, Int1024 b) {
        return new Int1024(a).multiply(b);
    }

    /**
     * Divides this integer by the given one
     *
     * This modifies the current integer
     *
     * @param divisor the integer to divide into this one
     * @return this for chaining
     */
    public Int1024 divide(Int1024 divisor) {
        this.data = division(this, divisor)[0].data;
        return this;
    }

    /**
     * Divide an integer by another
     *
     * @param dividend the numerator or dividend integer
     * @param divisor the denominator or divisor integer
     * @return the result of the division
     */
    public static Int1024 divide(Int1024 dividend, Int1024 divisor) {
        return division(dividend, divisor)[0];
    }

    /**
     * Divides this integer by the given one and get the remainder
     *
     * This modifies the current integer
     *
     * @param divisor the integer to divide into this one
     * @return this for chaining
     */
    public Int1024 remainder(Int1024 divisor) {
        this.data = division(this, divisor)[1].data;
        return this;
    }

    /**
     * Divide an integer by another and get the remainder
     *
     * @param dividend the numerator or dividend integer
     * @param divisor the denominator or divisor integer
     * @return the remainder of the division
     */
    public static Int1024 remainder(Int1024 dividend, Int1024 divisor) {
        return division(dividend, divisor)[1];
    }

    /**
     * Combined division and remainder method
     *
     * @param n the numerator or dividend integer
     * @param d the denominator or divisor integer
     * @return an array where the first element is the quotient and the second is the remainder
     */
    public static Int1024[] division(Int1024 n, Int1024 d) {
        Int1024 q = new Int1024(); //q = 0
        Int1024 r = new Int1024(); //r = 0

        boolean isFirstN = false;

        boolean flipSigns = false;
        if (n.compareTo(ZERO) < 0) {
            flipSigns = true;
            n = Int1024.negate(n);
        }
        if (d.compareTo(ZERO) < 0) {
            flipSigns = !flipSigns;
            d = Int1024.negate(d);
        }

        for (int i = LONGS * 64 - 1; i >= 0; i--) {
            if (isFirstN || n.getBit(i) != 0) {
                isFirstN = true;
                r.shiftLeft(1);
                r.setBit(0, n.getBit(i));
                if (r.compareTo(d) >= 0) {
                    r.subtract(d);
                    q.setBit(i, 1);
                }
            }
        }

        if (flipSigns) {
            return new Int1024[] {q.negate(), r.negate()};
        }
        return new Int1024[] {q, r};
    }

    /**
     * Gets the absolute value of this integer
     *
     * Modifies this integer
     *
     * @return this for chaining
     */
    public Int1024 abs() {
        if (compareTo(ZERO) < 0) {
            return negate();
        }
        return this;
    }

    /**
     * Gets the absolute value of an integer
     *
     * @param num the integer to get the absolute value of
     * @return the absolute value of the integer
     */
    public static Int1024 abs(Int1024 num) {
        return new Int1024(num).abs();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Int1024)) return false;
        Int1024 int128 = (Int1024) o;
        return Arrays.equals(data, int128.data);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
    
    @Override
    public String toString() {
        if (this.equals(ZERO)) {
            return "0";
        }

        StringBuilder builder = new StringBuilder();

        Int1024[] divisionResults = new Int1024[] {this};
        do {
            divisionResults = division(divisionResults[0], TEN);
            builder.append(Math.abs(divisionResults[1].data[LONGS - 1]));
        } while (!divisionResults[0].equals(ZERO));

        if (compareTo(ZERO) < 0) {
            builder.append('-');
        }

        return builder.reverse().toString();
    }

    /**
     * Convert this number to a string adding commas for separation between each group of three digits
     *
     * @return a decimal representation of this number
     */
    public String toStringCommas() {
        if (this.equals(ZERO)) {
            return "0";
        }

        StringBuilder builder = new StringBuilder();

        Int1024[] divisionResults = new Int1024[] {this};
        int seps = 0;
        do {
            if (seps == 3) {
                seps = 0;
                builder.append(',');
            }
            divisionResults = division(divisionResults[0], TEN);
            builder.append(Math.abs(divisionResults[1].data[LONGS - 1]));
            seps++;
        } while (!divisionResults[0].equals(ZERO));

        if (compareTo(ZERO) < 0) {
            builder.append('-');
        }

        return builder.reverse().toString();
    }

    @Override
    public int compareTo(Int1024 o) {
        if (this.equals(o)) {
            return 0;
        }
        if (o.equals(ZERO)) {
            if (this.getBit(64 * LONGS - 1) != 0) { //If negative
                return -1;
            }
            return 1;
        }
        return subtract(this, o).compareTo(ZERO);
    }
}
