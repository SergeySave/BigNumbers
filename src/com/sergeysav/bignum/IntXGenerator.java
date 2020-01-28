package com.sergeysav.bignum;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author sergeys
 */
public class IntXGenerator {

    private int longs;
    private int bits;

    public IntXGenerator(int x) {
        if (x % 64 != 0) {
            throw new IllegalArgumentException("Parameter must be a multiple of 64");
        }

        longs = x / 64;
        bits = x;
    }

    public String generateClass() {
        return "package com.sergeysav.bignum;\n" +
               "\n" +
               "import java.util.Arrays;\n" +
               "\n" +
               "/**\n" +
               " * Represents a " + bits + " bit 2's complement integer\n" +
               " *\n" +
               " * @author sergeys\n" +
               " */\n" +
               "public final class Int" + bits + " implements Comparable<Int" + bits + "> {\n" +
               "\n" +
               "    /**\n" +
               "     * The number of longs used to store the data for this type\n" +
               "     */\n" +
               "    private static final int LONGS = " + longs + ";\n" +
               "\n" +
               "    /**\n" +
               "     * A constant equal to zero\n" +
               "     */\n" +
               "    public static final Int" + bits + " ZERO = new Int" + bits + "();\n" +
               "    /**\n" +
               "     * A constant equal to one\n" +
               "     */\n" +
               "    public static final Int" + bits + " ONE = Int" + bits + ".from(1);\n" +
               "    /**\n" +
               "     * A constant equal to ten\n" +
               "     */\n" +
               "    public static final Int" + bits + " TEN = Int" + bits + ".from(10);\n" +
               "    /**\n" +
               "     * The maximum value\n" +
               "     */\n" +
               "    public static final Int" + bits + " MAX_VALUE = Int" + bits + ".bytesOf(Long.MAX_VALUE, " + IntStream.rangeClosed(2, longs).mapToObj((unused) -> "-1L").collect(
                Collectors.joining(", ")) + ");\n" +
               "\n" +
               "    /**\n" +
               "     * The minimum value\n" +
               "     */\n" +
               "    public static final Int" + bits + " MIN_VALUE = Int" + bits + ".bytesOf(Long.MIN_VALUE, " + IntStream.rangeClosed(2, longs).mapToObj((unused) -> "0L").collect(
                Collectors.joining(", ")) + ");\n" +
               "\n" +
               "    /**\n" +
               "     * The backing bits (stored as longs)\n" +
               "     */\n" +
               "    private long[] data;\n" +
               "\n" +
               "    /**\n" +
               "     * Create a new integer equaling zero\n" +
               "     */\n" +
               "    public Int" + bits + "() {\n" +
               "        data = new long[LONGS];\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Create a new integer with the same value as another\n" +
               "     *\n" +
               "     * @param other the integer value to copy\n" +
               "     */\n" +
               "    public Int" + bits + "(Int" + bits + " other) {\n" +
               "        this();\n" +
               "        \n" +
               "        System.arraycopy(other.data, 0, this.data, 0, LONGS);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates an integer from a given long\n" +
               "     *\n" +
               "     * The returned integer will be sign extended to this integer's length\n" +
               "     *\n" +
               "     * @param num the long value\n" +
               "     * @return an integer representing the same number as the given long\n" +
               "     */\n" +
               "    public static Int" + bits + " from(long num) {\n" +
               "        Int" + bits + " val = new Int" + bits + "();\n" +
               "\n" +
               "        //Copy the long\n" +
               "        val.data[LONGS - 1] = num;\n" +
               "\n" +
               "        //Sign extension\n" +
               "        if (num < 0) {\n" +
               "            for (int i = 0; i < LONGS - 1; i++) {\n" +
               "                val.data[i] = -1L;\n" +
               "            }\n" +
               "        }\n" +
               "        \n" +
               "        return val;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates a new integer given the binary data for the integer\n" +
               "     *\n" +
               "     * WARNING: Advanced method\n" +
               "     *\n" +
               "     * @param parts the data to use (must be length " + longs + ")\n" +
               "     * @return a new integer of the given data\n" +
               "     */\n" +
               "    public static Int" + bits + " bytesOf(long... parts) {\n" +
               "        if (parts.length != " + longs + ") {\n" +
               "            throw new IllegalArgumentException(\"Incorrect number of bytes\");\n" +
               "        }\n" +
               "\n" +
               "        Int" + bits + " val = new Int" + bits + "();\n" +
               "\n" +
               "        System.arraycopy(parts, 0, val.data, 0, LONGS);\n" +
               "\n" +
               "        return val;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Add two integers returning a new integer object\n" +
               "     *\n" +
               "     * @param a the first integer\n" +
               "     * @param b the second integer\n" +
               "     * @return the result of the addition\n" +
               "     */\n" +
               "    public static Int" + bits + " add(Int" + bits + " a, Int" + bits + " b) {\n" +
               "        return new Int" + bits + "(a).add(b);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Adds another integer to this one\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @param b the other integer\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " add(Int" + bits + " b) {\n" +
               "        long carry = 0;\n" +
               "    \n" +
               "        for (int i = LONGS - 1; i >= 0; i--) {\n" +
               "            long temp = this.data[i] + b.data[i] + carry;\n" +
               "\n" +
               "            if (carry == 1){\n" +
               "                carry = (Long.compareUnsigned(temp, this.data[i]) <= 0 && Long.compareUnsigned(temp, b.data[i]) <= 0)\n" +
               "                                        ? 1 : 0;\n" +
               "            } else {\n" +
               "                carry = (Long.compareUnsigned(temp, this.data[i]) < 0 && Long.compareUnsigned(temp, b.data[i]) < 0)\n" +
               "                                        ? 1 : 0;\n" +
               "            }\n" +
               "            \n" +
               "            data[i] = temp;\n" +
               "        }\n" +
               "        \n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Negate a given integer\n" +
               "     *\n" +
               "     * @param a the integer to negate\n" +
               "     * @return the result of the negation\n" +
               "     */\n" +
               "    public static Int" + bits + " negate(Int" + bits + " a) {\n" +
               "        return new Int" + bits + "(a).negate();\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Negate this integer\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " negate() {\n" +
               "        for (int i = 0; i < LONGS; i++) {\n" +
               "            data[i] = ~data[i];\n" +
               "        }\n" +
               "        \n" +
               "        return add(ONE);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Subtract two integers returning a new integer object\n" +
               "     *\n" +
               "     * @param a the first integer\n" +
               "     * @param b the second integer\n" +
               "     * @return the result of the subtraction\n" +
               "     */\n" +
               "    public static Int" + bits + " subtract(Int" + bits + " a, Int" + bits + " b) {\n" +
               "        return new Int" + bits + "(a).subtract(b);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Subtracts another integer from this one\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @param b the other integer\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " subtract(Int" + bits + " b) {\n" +
               "        return add(Int" + bits + ".negate(b));\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift this integer left by a given number of bits\n" +
               "     *\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " shiftLeft(int bits) {\n" +
               "        this.data = com.sergeysav.bignum.CommonUtils.shiftLeft(this.data, bits);\n" +
               "        return this;" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift this integer right by a given number of bits\n" +
               "     *\n" +
               "     * This will fill the new left bits with 0s\n" +
               "     *\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " shiftRightUnsigned(int bits) {\n" +
               "        this.data = com.sergeysav.bignum.CommonUtils.shiftRightUnsigned(this.data, bits);\n" +
               "        return this;" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift this integer right by a given number of bits\n" +
               "     *\n" +
               "     * This will fill the new left bits with the sign bit\n" +
               "     *\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " shiftRightSigned(int bits) {\n" +
               "        this.data = com.sergeysav.bignum.CommonUtils.shiftRightSigned(this.data, bits);\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift an integer left by a given number of bits\n" +
               "     *\n" +
               "     * @param num the integer to shift\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return an integer that is shifted by the given amount\n" +
               "     */\n" +
               "    public static Int" + bits + " shiftLeft(Int" + bits + " num, int bits) {\n" +
               "        return new Int" + bits + "(num).shiftLeft(bits);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift an integer right by a given number of bits\n" +
               "     *\n" +
               "     * This will fill the new left bits with 0s\n" +
               "     *\n" +
               "     * @param num the integer to shift\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return an integer that is shifted by the given amount\n" +
               "     */\n" +
               "    public static Int" + bits + " shiftRightUnsigned(Int" + bits + " num, int bits) {\n" +
               "        return new Int" + bits + "(num).shiftRightUnsigned(bits);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Shift an integer right by a given number of bits\n" +
               "     *\n" +
               "     * This will fill the new left bits with the sign bit\n" +
               "     *\n" +
               "     * @param num the integer to shift\n" +
               "     * @param bits the number of bits to shift by\n" +
               "     * @return an integer that is shifted by the given amount\n" +
               "     */\n" +
               "    public static Int" + bits + " shiftRightSigned(Int" + bits + " num, int bits) {\n" +
               "        return new Int" + bits + "(num).shiftRightSigned(bits);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Get a given bit of this integer\n" +
               "     *\n" +
               "     * @param bit the bit to get\n" +
               "     * @return 0 if the bit is 0, 1 if the bit is 1\n" +
               "     */\n" +
               "    public int getBit(int bit) {\n" +
               "        return getBit(data, bit);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Set a given bit of this integer\n" +
               "     *\n" +
               "     * @param bit the bit to set\n" +
               "     * @param val the value to set (either 0 or 1)\n" +
               "     */\n" +
               "    public void setBit(int bit, int val) {\n" +
               "        setBit(data, bit, val);\n" +
               "    }\n" +
               "\n" +
               "    private static int getBit(long[] longs, int bit) {\n" +
               "        return (longs[longs.length - 1 - (bit / 64)] & (1L << (bit % 64))) != 0 ? 1 : 0;\n" +
               "    }\n" +
               "\n" +
               "    private static void setBit(long[] longs, int bit, int val) {\n" +
               "        if (val == 0) {\n" +
               "            longs[longs.length - 1 - (bit / 64)] &= ~(1L << (bit % 64));\n" +
               "        } else {\n" +
               "            longs[longs.length - 1 - (bit / 64)] |= (1L << (bit % 64));\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Multiply a given integer by this one\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @param b the integer to multiply by\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " multiply(Int" + bits + " b) {\n" +
               "        long[] oldData = data;\n" +
               "        this.data = new long[LONGS];\n" +
               "\n" +
               "        Int" + bits + " shifting = new Int" + bits + "(b);\n" +
               "        int shiftAmount = 0;\n" +
               "\n" +
               "        for (int i = LONGS - 1; i >= 0; i--) {\n" +
               "            for (int j = 0; j < 64; j++) {\n" +
               "                if (getBit(oldData, j +  64 * (LONGS - 1 - i)) != 0) {\n" +
               "                    shifting.shiftLeft(shiftAmount);\n" +
               "                    add(shifting);\n" +
               "                    shiftAmount = 0;\n" +
               "                }\n" +
               "                shiftAmount++;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Multiply two integers together\n" +
               "     *\n" +
               "     * @param a the first integer\n" +
               "     * @param b the second integer\n" +
               "     * @return the result of the multiplication\n" +
               "     */\n" +
               "    public static Int" + bits + " multiply(Int" + bits + " a, Int" + bits + " b) {\n" +
               "        return new Int" + bits + "(a).multiply(b);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Divides this integer by the given one\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @param divisor the integer to divide into this one\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " divide(Int" + bits + " divisor) {\n" +
               "        this.data = division(this, divisor)[0].data;\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Divide an integer by another\n" +
               "     *\n" +
               "     * @param dividend the numerator or dividend integer\n" +
               "     * @param divisor the denominator or divisor integer\n" +
               "     * @return the result of the division\n" +
               "     */\n" +
               "    public static Int" + bits + " divide(Int" + bits + " dividend, Int" + bits + " divisor) {\n" +
               "        return division(dividend, divisor)[0];\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Divides this integer by the given one and get the remainder\n" +
               "     *\n" +
               "     * This modifies the current integer\n" +
               "     *\n" +
               "     * @param divisor the integer to divide into this one\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " remainder(Int" + bits + " divisor) {\n" +
               "        this.data = division(this, divisor)[1].data;\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Divide an integer by another and get the remainder\n" +
               "     *\n" +
               "     * @param dividend the numerator or dividend integer\n" +
               "     * @param divisor the denominator or divisor integer\n" +
               "     * @return the remainder of the division\n" +
               "     */\n" +
               "    public static Int" + bits + " remainder(Int" + bits + " dividend, Int" + bits + " divisor) {\n" +
               "        return division(dividend, divisor)[1];\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Combined division and remainder method\n" +
               "     *\n" +
               "     * @param n the numerator or dividend integer\n" +
               "     * @param d the denominator or divisor integer\n" +
               "     * @return an array where the first element is the quotient and the second is the remainder\n" +
               "     */\n" +
               "    public static Int" + bits + "[] division(Int" + bits + " n, Int" + bits + " d) {\n" +
               "        Int" + bits + " q = new Int" + bits + "(); //q = 0\n" +
               "        Int" + bits + " r = new Int" + bits + "(); //r = 0\n" +
               "\n" +
               "        boolean isFirstN = false;\n" +
               "\n" +
               "        boolean flipSigns = false;\n" +
               "        if (n.compareTo(ZERO) < 0) {\n" +
               "            flipSigns = true;\n" +
               "            n = Int" + bits + ".negate(n);\n" +
               "        }\n" +
               "        if (d.compareTo(ZERO) < 0) {\n" +
               "            flipSigns = !flipSigns;\n" +
               "            d = Int" + bits + ".negate(d);\n" +
               "        }\n" +
               "\n" +
               "        for (int i = LONGS * 64 - 1; i >= 0; i--) {\n" +
               "            if (isFirstN || n.getBit(i) != 0) {\n" +
               "                isFirstN = true;\n" +
               "                r.shiftLeft(1);\n" +
               "                r.setBit(0, n.getBit(i));\n" +
               "                if (r.compareTo(d) >= 0) {\n" +
               "                    r.subtract(d);\n" +
               "                    q.setBit(i, 1);\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        if (flipSigns) {\n" +
               "            return new Int" + bits + "[] {q.negate(), r.negate()};\n" +
               "        }\n" +
               "        return new Int" + bits + "[] {q, r};\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Gets the absolute value of this integer\n" +
               "     *\n" +
               "     * Modifies this integer\n" +
               "     *\n" +
               "     * @return this for chaining\n" +
               "     */\n" +
               "    public Int" + bits + " abs() {\n" +
               "        if (compareTo(ZERO) < 0) {\n" +
               "            return negate();\n" +
               "        }\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Gets the absolute value of an integer\n" +
               "     *\n" +
               "     * @param num the integer to get the absolute value of\n" +
               "     * @return the absolute value of the integer\n" +
               "     */\n" +
               "    public static Int" + bits + " abs(Int" + bits + " num) {\n" +
               "        return new Int" + bits + "(num).abs();\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public boolean equals(Object o) {\n" +
               "        if (this == o) return true;\n" +
               "        if (!(o instanceof Int" + bits + ")) return false;\n" +
               "        Int" + bits + " int128 = (Int" + bits + ") o;\n" +
               "        return Arrays.equals(data, int128.data);\n" +
               "    }\n" +
               "    \n" +
               "    @Override\n" +
               "    public int hashCode() {\n" +
               "        return Arrays.hashCode(data);\n" +
               "    }\n" +
               "    \n" +
               "    @Override\n" +
               "    public String toString() {\n" +
               "        if (this.equals(ZERO)) {\n" +
               "            return \"0\";\n" +
               "        }\n" +
               "\n" +
               "        StringBuilder builder = new StringBuilder();\n" +
               "\n" +
               "        Int" + bits + "[] divisionResults = new Int" + bits + "[] {this};\n" +
               "        do {\n" +
               "            divisionResults = division(divisionResults[0], TEN);\n" +
               "            builder.append(Math.abs(divisionResults[1].data[LONGS - 1]));\n" +
               "        } while (!divisionResults[0].equals(ZERO));\n" +
               "\n" +
               "        if (compareTo(ZERO) < 0) {\n" +
               "            builder.append('-');\n" +
               "        }\n" +
               "\n" +
               "        return builder.reverse().toString();\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Convert this number to a string adding commas for separation between each group of three digits\n" +
               "     *\n" +
               "     * @return a decimal representation of this number\n" +
               "     */\n" +
               "    public String toStringCommas() {\n" +
               "        if (this.equals(ZERO)) {\n" +
               "            return \"0\";\n" +
               "        }\n" +
               "\n" +
               "        StringBuilder builder = new StringBuilder();\n" +
               "\n" +
               "        Int" + bits + "[] divisionResults = new Int" + bits + "[] {this};\n" +
               "        int seps = 0;\n" +
               "        do {\n" +
               "            if (seps == 3) {\n" +
               "                seps = 0;\n" +
               "                builder.append(',');\n" +
               "            }\n" +
               "            divisionResults = division(divisionResults[0], TEN);\n" +
               "            builder.append(Math.abs(divisionResults[1].data[LONGS - 1]));\n" +
               "            seps++;\n" +
               "        } while (!divisionResults[0].equals(ZERO));\n" +
               "\n" +
               "        if (compareTo(ZERO) < 0) {\n" +
               "            builder.append('-');\n" +
               "        }\n" +
               "\n" +
               "        return builder.reverse().toString();\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public int compareTo(Int" + bits + " o) {\n" +
               "        if (this.equals(o)) {\n" +
               "            return 0;\n" +
               "        }\n" +
               "        if (o.equals(ZERO)) {\n" +
               "            if (this.getBit(64 * LONGS - 1) != 0) { //If negative\n" +
               "                return -1;\n" +
               "            }\n" +
               "            return 1;\n" +
               "        }\n" +
               "        return subtract(this, o).compareTo(ZERO);\n" +
               "    }\n" +
               "}\n";
    }
}
