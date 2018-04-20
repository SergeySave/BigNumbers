import java.util.Arrays;

/**
 * Represents a 128 bit 2's complement integer
 *
 * @author sergeys
 */
public final class Int128 implements Comparable<Int128> {

    /**
     * A constant equal to zero
     */
    public static final  Int128 ZERO  = new Int128();
    /**
     * The number of longs used to store the data for this type
     */
    private static final int    LONGS = 2;
    /**
     * A constant equal to one
     */
    public static final  Int128 ONE   = Int128.from(1);
    /**
     * A constant equal to ten
     */
    public static final  Int128 TEN   = Int128.from(10);

    /**
     * The backing bits (stored as longs)
     */
    private long[] data;

    /**
     * Create a new integer with the same value as another
     *
     * @param other the integer value to copy
     */
    public Int128(Int128 other) {
        this();

        System.arraycopy(other.data, 0, this.data, 0, LONGS);
    }

    /**
     * Create a new integer equaling zero
     */
    public Int128() {
        data = new long[LONGS];
    }

    /**
     * Creates an integer from a given long
     *
     * The returned integer will be sign extended to this integer's length
     *
     * @param num the long value
     *
     * @return an integer representing the same number as the given long
     */
    public static Int128 from(long num) {
        Int128 val = new Int128();

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
     * @param parts the data to use (must be length 2)
     *
     * @return a new integer of the given data
     */
    public static Int128 bytesOf(long... parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("Incorrect number of bytes");
        }

        Int128 val = new Int128();

        System.arraycopy(parts, 0, val.data, 0, LONGS);

        return val;
    }

    /**
     * Add two integers returning a new integer object
     *
     * @param a the first integer
     * @param b the second integer
     *
     * @return the result of the addition
     */
    public static Int128 add(Int128 a, Int128 b) {
        return new Int128(a).add(b);
    }

    /**
     * Adds another integer to this one
     *
     * This modifies the current integer
     *
     * @param b the other integer
     *
     * @return this for chaining
     */
    public Int128 add(Int128 b) {
        long carry = 0;

        for (int i = LONGS - 1; i >= 0; i--) {
            long temp = this.data[i] + b.data[i] + carry;

            if (carry == 1) {
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
     *
     * @return the result of the negation
     */
    public static Int128 negate(Int128 a) {
        return new Int128(a).negate();
    }

    /**
     * Subtract two integers returning a new integer object
     *
     * @param a the first integer
     * @param b the second integer
     *
     * @return the result of the subtraction
     */
    public static Int128 subtract(Int128 a, Int128 b) {
        return new Int128(a).subtract(b);
    }

    /**
     * Shift an integer left by a given number of bits
     *
     * @param num  the integer to shift
     * @param bits the number of bits to shift by
     *
     * @return an integer that is shifted by the given amount
     */
    public static Int128 shiftLeft(Int128 num, int bits) {
        return new Int128(num).shiftLeft(bits);
    }

    /**
     * Shift this integer left by a given number of bits
     *
     * @param bits the number of bits to shift by
     *
     * @return this for chaining
     */
    public Int128 shiftLeft(int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }
        long[] result = new long[LONGS];
        int longs = bits / 64;
        int singleBits = bits % 64;

        long mask = ~(-1L << singleBits);

        for (int i = LONGS - 1; i >= 0; i--) {
            if (i + longs >= LONGS) {
                result[i] = 0;
            } else if (i + longs + 1 >= LONGS) {
                result[i] = (this.data[i + longs] << singleBits);
            } else {
                result[i] = ((this.data[i + longs] << singleBits) |
                             ((this.data[i + longs + 1] >> (64 - singleBits)) & mask));
            }
        }

        System.arraycopy(result, 0, this.data, 0, LONGS);
        return this;
    }

    /**
     * Shift an integer right by a given number of bits
     *
     * This will fill the new left bits with 0s
     *
     * @param num  the integer to shift
     * @param bits the number of bits to shift by
     *
     * @return an integer that is shifted by the given amount
     */
    public static Int128 shiftRightUnsigned(Int128 num, int bits) {
        return new Int128(num).shiftRightUnsigned(bits);
    }

    /**
     * Shift this integer right by a given number of bits
     *
     * This will fill the new left bits with 0s
     *
     * @param bits the number of bits to shift by
     *
     * @return this for chaining
     */
    public Int128 shiftRightUnsigned(int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }

        long[] result = new long[LONGS];
        int longs = bits / 64;
        int singleBits = bits % 64;

        for (int i = LONGS - 1; i >= 0; i--) {
            if (i - longs < 0) {
                result[i] = 0;
            } else if (i - longs < 1) { //i - bytes == 0
                result[i] = (this.data[i - longs] >>> singleBits);
            } else {
                result[i] = ((this.data[i - longs] >>> singleBits) |
                             (this.data[i - longs - 1] << (64 - singleBits)));
            }
        }

        System.arraycopy(result, 0, this.data, 0, LONGS);
        return this;
    }

    /**
     * Shift an integer right by a given number of bits
     *
     * This will fill the new left bits with the sign bit
     *
     * @param num  the integer to shift
     * @param bits the number of bits to shift by
     *
     * @return an integer that is shifted by the given amount
     */
    public static Int128 shiftRightSigned(Int128 num, int bits) {
        return new Int128(num).shiftRightSigned(bits);
    }

    /**
     * Shift this integer right by a given number of bits
     *
     * This will fill the new left bits with the sign bit
     *
     * @param bits the number of bits to shift by
     *
     * @return this for chaining
     */
    public Int128 shiftRightSigned(int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }

        long[] result = new long[LONGS];
        int longs = bits / 64;
        int singleBits = bits % 64;

        long signLong = data[0] < 0 ? -1L : 0;

        for (int i = LONGS - 1; i >= 0; i--) {
            if (i - longs < 0) {
                result[i] = signLong;
            } else if (i - longs < 1) { //i - bytes == 0
                result[i] = ((this.data[i - longs] >>> singleBits) |
                             (signLong << (64 - singleBits)));
            } else {
                result[i] = ((this.data[i - longs] >>> singleBits) |
                             (this.data[i - longs - 1] << (64 - singleBits)));
            }
        }

        System.arraycopy(result, 0, this.data, 0, LONGS);
        return this;
    }

    private static void setBit(long[] longs, int bit, int val) {
        if (val == 0) {
            longs[longs.length - 1 - (bit / 64)] &= ~(1L << (bit % 64));
        } else {
            longs[longs.length - 1 - (bit / 64)] |= (1L << (bit % 64));
        }
    }

    /**
     * Multiply two integers together
     *
     * @param a the first integer
     * @param b the second integer
     *
     * @return the result of the multiplication
     */
    public static Int128 multiply(Int128 a, Int128 b) {
        return new Int128(a).multiply(b);
    }

    /**
     * Multiply a given integer by this one
     *
     * This modifies the current integer
     *
     * @param b the integer to multiply by
     *
     * @return this for chaining
     */
    public Int128 multiply(Int128 b) {
        long[] oldData = data;
        this.data = new long[LONGS];

        Int128 shifting = new Int128(b);
        int shiftAmount = 0;

        for (int i = LONGS - 1; i >= 0; i--) {
            for (int j = 0; j < 64; j++) {
                if (getBit(oldData, j + 64 * (LONGS - 1 - i)) != 0) {
                    shifting.shiftLeft(shiftAmount);
                    add(shifting);
                    shiftAmount = 0;
                }
                shiftAmount++;
            }
        }

        return this;
    }

    private static int getBit(long[] longs, int bit) {
        return (longs[longs.length - 1 - (bit / 64)] & (1L << (bit % 64))) != 0 ? 1 : 0;
    }

    /**
     * Divide an integer by another
     *
     * @param dividend the numerator or dividend integer
     * @param divisor  the denominator or divisor integer
     *
     * @return the result of the division
     */
    public static Int128 divide(Int128 dividend, Int128 divisor) {
        return division(dividend, divisor)[0];
    }

    /**
     * Divide an integer by another and get the remainder
     *
     * @param dividend the numerator or dividend integer
     * @param divisor  the denominator or divisor integer
     *
     * @return the remainder of the division
     */
    public static Int128 remainder(Int128 dividend, Int128 divisor) {
        return division(dividend, divisor)[1];
    }

    /**
     * Combined division and remainder method
     *
     * @param n the numerator or dividend integer
     * @param d the denominator or divisor integer
     *
     * @return an array where the first element is the quotient and the second is the remainder
     */
    public static Int128[] division(Int128 n, Int128 d) {
        Int128 q = new Int128(); //q = 0
        Int128 r = new Int128(); //r = 0

        boolean isFirstN = false;

        boolean flipSigns = false;
        if (n.compareTo(ZERO) < 0) {
            flipSigns = true;
            n = Int128.negate(n);
        }
        if (d.compareTo(ZERO) < 0) {
            flipSigns = !flipSigns;
            d = Int128.negate(d);
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
            return new Int128[]{q.negate(), r.negate()};
        }
        return new Int128[]{q, r};
    }

    /**
     * Gets the absolute value of an integer
     *
     * @param num the integer to get the absolute value of
     *
     * @return the absolute value of the integer
     */
    public static Int128 abs(Int128 num) {
        return new Int128(num).abs();
    }

    /**
     * Negate this integer
     *
     * This modifies the current integer
     *
     * @return this for chaining
     */
    public Int128 negate() {
        for (int i = 0; i < LONGS; i++) {
            data[i] = ~data[i];
        }

        return add(ONE);
    }

    /**
     * Subtracts another integer from this one
     *
     * This modifies the current integer
     *
     * @param b the other integer
     *
     * @return this for chaining
     */
    public Int128 subtract(Int128 b) {
        return add(Int128.negate(b));
    }

    /**
     * Get a given bit of this integer
     *
     * @param bit the bit to get
     *
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

    /**
     * Divides this integer by the given one
     *
     * This modifies the current integer
     *
     * @param divisor the integer to divide into this one
     *
     * @return this for chaining
     */
    public Int128 divide(Int128 divisor) {
        this.data = division(this, divisor)[0].data;
        return this;
    }

    /**
     * Divides this integer by the given one and get the remainder
     *
     * This modifies the current integer
     *
     * @param divisor the integer to divide into this one
     *
     * @return this for chaining
     */
    public Int128 remainder(Int128 divisor) {
        this.data = division(this, divisor)[1].data;
        return this;
    }

    /**
     * Gets the absolute value of this integer
     *
     * Modifies this integer
     *
     * @return this for chaining
     */
    public Int128 abs() {
        if (compareTo(ZERO) < 0) {
            return negate();
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Int128)) return false;
        Int128 int128 = (Int128) o;
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

        Int128[] divisionResults = new Int128[]{this};
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

        Int128[] divisionResults = new Int128[]{this};
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
    public int compareTo(Int128 o) {
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
