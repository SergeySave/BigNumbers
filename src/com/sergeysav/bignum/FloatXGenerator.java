package com.sergeysav.bignum;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author sergeys
 */
public class FloatXGenerator {

    private int longs;
    private int bits;
    private int signBits;
    private int exponentBits;
    private int mantissaBits;

    public FloatXGenerator(int signBits, int exponentBits, int mantissaBits) {
        if (signBits != 1) throw new IllegalArgumentException("Must have exactly 1 sign bit");

        this.bits = signBits + exponentBits + mantissaBits;

        if (this.bits % 64 != 0) throw new IllegalArgumentException("Must have a multiple of 64 bits.");

        this.longs = this.bits / 64;
        this.signBits = signBits;
        this.exponentBits = exponentBits;
        this.mantissaBits = mantissaBits;
    }

    public String generateClass() {
        return "package com.sergeysav.bignum;\n" +
               "\n" +
               "import com.sergeysav.bignum.CommonUtils.ExpString;\n" +
               "\n" +
               "import java.util.Arrays;\n" +
               "import java.util.Comparator;\n" +
               "\n" +
               "/**\n" +
               " * Represents a " + bits + " bit floating point number with\n" +
               " * " + signBits + " sign bit\n" +
               " * " + exponentBits + " exponent bits\n" +
               " * " + mantissaBits + " mantissa bits\n" +
               " *\n" +
               " * @author sergeys\n" +
               " */\n" +
               "public final class Float" + bits + " implements MutableNumber<Float" + bits + "> {\n" +
               "\n" +
               "    /**\n" +
               "     * A constant used for converting base 2 logarithms to base 10 logarithms (used in toString methods)\n" +
               "     */\n" +
               "    private static final double LOG_10_OF_2 = Math.log10(2);\n" +
               "\n" +
               "    /**\n" +
               "     * The number of sign bits\n" +
               "     */\n" +
               "    private static final int SIGN_BITS = " + signBits + ";\n" +
               "    /**\n" +
               "     * The number of exponent bits\n" +
               "     */\n" +
               "    private static final int EXPONENT_BITS = " + exponentBits + ";\n" +
               "    /**\n" +
               "     * The number of mantissa bits\n" +
               "     */\n" +
               "    private static final int MANTISSA_BITS = " + mantissaBits + ";\n" +
               "\n" +
               "    /**\n" +
               "     * The number of longs used to store the full data\n" +
               "     */\n" +
               "    private static final int LONGS = " + longs + "; // " + bits + " bits\n" +
               "\n" +
               "    /**\n" +
               "     * The start of the sign section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int SIGN_START = 0; //INCLUSIVE\n" +
               "    /**\n" +
               "     * The end of the sign section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int SIGN_END = SIGN_START + SIGN_BITS - 1; //INCLUSIVE\n" +
               "    /**\n" +
               "     * The start of the exponent section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int EXPONENT_START = SIGN_END + 1; //INCLUSIVE\n" +
               "    /**\n" +
               "     * The end of the exponent section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int EXPONENT_END = EXPONENT_START + EXPONENT_BITS - 1; //INCLUSIVE\n" +
               "    /**\n" +
               "     * The start of the mantissa section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int MANTISSA_START = EXPONENT_END + 1; //INCLUSIVE\n" +
               "    /**\n" +
               "     * The end of the mantissa section (INCLUSIVE)\n" +
               "     */\n" +
               "    private static final int MANTISSA_END = MANTISSA_START + MANTISSA_BITS - 1; //INCLUSIVE\n" +
               "\n" +
               "    /**\n" +
               "     * The exponent offset (the value of the exponent section when the exponent is 0)\n" +
               "     */\n" +
               "    private static final long EXPONENT_OFFSET = CommonUtils.pow(2, EXPONENT_BITS - 1) - 1;\n" +
               "    /**\n" +
               "     * A bitmask for the exponent section\n" +
               "     */\n" +
               "    private static final long EXPONENT_FULL_MASK = ~(-1L << EXPONENT_BITS);\n" +
               "\n" +
               "    /**\n" +
               "     * The mantissa array for a mantissa representing a zero value\n" +
               "     */\n" +
               "    private static final long[] ZERO_MANTISSA = new long[(MANTISSA_BITS - 1)/64 + 1];\n" +
               "    /**\n" +
               "     * The full mantissa array for a mantissa representing a zero value\n" +
               "     */\n" +
               "    private static final long[] ZERO_MANTISSA_FULL = new long[MANTISSA_BITS/64 + 1];\n" +
               "\n" +
               "\n" +
               "    /**\n" +
               "     * The comparator used for comparing two positive values\n" +
               "     */\n" +
               "    private static final Comparator<Float" + bits + "> POSITIVE_COMPARATOR = Comparator\n" +
               "            .comparing(Float" + bits + "::getExponentBits)\n" +
               "            .thenComparing((a, b) -> compareMantissas(a.getMantissaBits(), b.getMantissaBits()));\n" +
               "    /**\n" +
               "     * The comparator used for comparing two negative values\n" +
               "     */\n" +
               "    private static final Comparator<Float" + bits + "> NEGATIVE_COMPARATOR = Comparator\n" +
               "            .comparing(Float" + bits + "::getExponentBits)\n" +
               "            .thenComparing((a, b) -> compareMantissas(a.getMantissaBits(), b.getMantissaBits()))\n" +
               "            .reversed();\n" +
               "\n" +
               "    /**\n" +
               "     * A constant equal to 0\n" +
               "     */\n" +
               "    public static final Float" + bits + " ZERO = new Float" + bits + "();\n" +
               "    /**\n" +
               "     * A constant equal to 1\n" +
               "     */\n" +
               "    public static final Float" + bits + " ONE = from(1);\n" +
               "    /**\n" +
               "     * A constant equal to 10\n" +
               "     */\n" +
               "    public static final Float" + bits + " TEN = from(10);\n" +
               "    /**\n" +
               "     * A constant equal to positive infinity\n" +
               "     */\n" +
               "    public static final Float" + bits + " POSITIVE_INFINITY = ONE.copy().divide(ZERO);\n" +
               "    /**\n" +
               "     * A constant equal to negative infinity\n" +
               "     */\n" +
               "    public static final Float" + bits + " NEGATIVE_INFINITY = ONE.copy().negate().divide(ZERO);\n" +
               "    /**\n" +
               "     * A constant equal to NaN\n" +
               "     */\n" +
               "    public static final Float" + bits + " NAN = ZERO.copy().divide(ZERO);\n" +
               "    /**\n" +
               "     * A constant equal to the minimum normal value representable\n" +
               "     */\n" +
               "    public static final Float" + bits + " MIN_NORMAL = fromStructure(false, 1, new long[(MANTISSA_BITS - 1)/64 + 1]);\n" +
               "    /**\n" +
               "     * A constant equal to the maximum normal value representable\n" +
               "     */\n" +
               "    public static final Float" + bits + " MAX_VALUE = fromStructure(false, EXPONENT_FULL_MASK - 1, new long[]{" +
               IntStream.range(0, (mantissaBits - 1) / 64 + 1)
                       .mapToObj((unused) -> "-1L")
                       .collect(Collectors.joining(", ")) + "});\n" +
               "    /**\n" +
               "     * A constant equal to the minimum subnormal value representable\n" +
               "     */\n" +
               "    public static final Float" + bits + " MIN_VALUE = fromStructure(false, 0, new long[]{" +
               IntStream.range(1, (mantissaBits - 1) / 64 + 1)
                       .mapToObj((unused) -> "0L, ")
                       .collect(Collectors.joining()) + "1L});\n" +
               "    /**\n" +
               "     * A constant equal to the maximum subnormal value representable\n" +
               "     */\n" +
               "    public static final Float" + bits + " MAX_SUBNORMAL = fromStructure(false, 0, new long[]{" +
               IntStream.range(0, (mantissaBits - 1) / 64 + 1)
                       .mapToObj((unused) -> "-1L")
                       .collect(Collectors.joining(", ")) + "});\n" +
               "\n" +
               "    /**\n" +
               "     * The longs representing the internal data\n" +
               "     */\n" +
               "    private long[] data;\n" +
               "\n" +
               "    static {\n" +
               "        if (EXPONENT_BITS > 64) {\n" +
               "            throw new IllegalStateException(\"Cannot represent float with over 64 exponent bits\");\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates a new floating point with a value of zero\n" +
               "     */\n" +
               "    public Float" + bits + "() {\n" +
               "        data = new long[LONGS];\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates a copy of the given floating point\n" +
               "     * \n" +
               "     * @param src the floating point to copy\n" +
               "     */\n" +
               "    public Float" + bits + "(Float" + bits + " src) {\n" +
               "        data = new long[LONGS];\n" +
               "        System.arraycopy(src.data, 0, this.data, 0, LONGS);\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates a floating point from a given double's binary data\n" +
               "     *\n" +
               "     * WARNING: this creates it from the data and not to string representation\n" +
               "     * using 1.0/3 will result in 0.333333333333333314829616256247390992939472198486328125\n" +
               "     * instead of the more accurate result that could be represented with this data structure\n" +
               "     *\n" +
               "     * @param original the double to create the floating point from\n" +
               "     * @return a new floating point\n" +
               "     */\n" +
               "    public static Float" + bits + " from(double original) {\n" +
               "        Float" + bits + " num = new Float" + bits + "();\n" +
               "        long bits = Double.doubleToRawLongBits(original);\n" +
               "\n" +
               "        long[] mantissa = new long[(MANTISSA_BITS - 1)/64 + 1];\n" +
               "        mantissa[0] = (((bits & 4503599627370495L) << 12) >>> MANTISSA_START);\n" +
               "        mantissa[1] = ((bits & ~(-1L << (MANTISSA_START - 12))) << (64 - (MANTISSA_START - 12)));\n" +
               "\n" +
               "        num.setMantissaBits(mantissa);\n" +
               "\n" +
               "        if ((original < 0) ^ num.isNegative()) {\n" +
               "            num.negate(); //Invert if needed\n" +
               "        }\n" +
               "\n" +
               "        if ((bits & 9223090561878065152L) == 0) {\n" +
               "            num.setExponentBits(0);\n" +
               "        } else {\n" +
               "            num.setExponentBits(((bits & 9223090561878065152L) >>> 52) - 1023 + EXPONENT_OFFSET);\n" +
               "        }\n" +
               "\n" +
               "        return num;\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * Creates a floating point from a given set of binary data\n" +
               "     * \n" +
               "     * WARNING: Advanced method: use with caution\n" +
               "     * \n" +
               "     * @param parts an array of " + longs + " longs representing the binary data\n" +
               "     * @return a floating point representing the given data\n" +
               "     */\n" +
               "    public static Float" + bits + " bytesOf(long... parts) {\n" +
               "        if (parts.length != LONGS) {\n" +
               "            throw new IllegalArgumentException(\"Incorrect number of bytes\");\n" +
               "        }\n" +
               "\n" +
               "        Float" + bits + " val = new Float" + bits + "();\n" +
               "\n" +
               "        System.arraycopy(parts, 0, val.data, 0, LONGS);\n" +
               "\n" +
               "        return val;\n" +
               "    }\n" +
               "\n" +
               "    private static Float" + bits + " fromStructure(boolean negative, long exponent, long[] mantissa) {\n" +
               "        Float" + bits + " result = new Float" + bits + "();\n" +
               "        result.setExponentBits(exponent);\n" +
               "        result.setMantissaBits(mantissa);\n" +
               "        if (negative) {\n" +
               "            result.negate();\n" +
               "        }\n" +
               "        return result;\n" +
               "    }\n" +
               "\n" +
               "    private boolean isNegative() {\n" +
               "        return data[0] < 0;\n" +
               "    }\n" +
               "\n" +
               "    private long getExponentBits() {\n" +
               "        if (EXPONENT_BITS == 64) {\n" +
               "            //First 63 bits after sign bit and the next bit\n" +
               "            return (data[0] << 1) | (data[1] >>> 63);\n" +
               "        }\n" +
               "        //Remove sign bit and shift right until all we have is exponent bits\n" +
               "        return (data[0] & Long.MAX_VALUE) >>> (63 - EXPONENT_BITS);\n" +
               "    }\n" +
               "\n" +
               "    private void setExponentBits(long exponentBits) {\n" +
               "        if (EXPONENT_BITS == 64) {\n" +
               "            //First 63 bits after sign bit and the next bit\n" +
               "            data[0] = (data[0] & Long.MIN_VALUE) | (exponentBits >>> 1);\n" +
               "            data[1] = (data[1] & Long.MAX_VALUE) | (exponentBits << 63);\n" +
               "        }\n" +
               "        //Take sign bit and bits after the end of the exponent and combine them with the exponent's bits\n" +
               "        data[0] = (data[0] & Long.MIN_VALUE) | (data[0] & ~(-1L << (63 - EXPONENT_END)))\n" +
               "                  | ((exponentBits & ~(-1L << EXPONENT_BITS)) << (63 - EXPONENT_END));\n" +
               "    }\n" +
               "\n" +
               "    private boolean isMantissaZero() {\n" +
               "        if (MANTISSA_START % 64 != 0) { //Partial long at the start\n" +
               "            if (data[MANTISSA_START / 64] << (MANTISSA_START % 64) != 0) {\n" +
               "                return false;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        //Full longs\n" +
               "        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {\n" +
               "            if (data[i] != 0) {\n" +
               "                return false;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        //Partial long at the end\n" +
               "        if ((MANTISSA_END + 1) % 64 != 0) {\n" +
               "            if (data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64)) != 0) {\n" +
               "                return false;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return true;\n" +
               "    }\n" +
               "\n" +
               "    private long[] getMantissaBits() {\n" +
               "        long[] mantissa = new long[(MANTISSA_BITS - 1)/64 + 1];\n" +
               "        int pos = 0;\n" +
               "\n" +
               "        if (MANTISSA_START % 64 != 0) { //Partial long at the start\n" +
               "            mantissa[pos++] = data[MANTISSA_START / 64] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "        }\n" +
               "\n" +
               "        //Full longs\n" +
               "        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {\n" +
               "            mantissa[pos++] = data[i];\n" +
               "        }\n" +
               "\n" +
               "        //Partial long at the end\n" +
               "        if ((MANTISSA_END + 1) % 64 != 0) {\n" +
               "            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {\n" +
               "                //The unset data to the right will be set here\n" +
               "                mantissa[mantissa.length - 1] = data[MANTISSA_END / 64];\n" +
               "                //Shift stuff as far to the right as it must go\n" +
               "                mantissa = CommonUtils.shiftRightUnsigned(mantissa, 64 - ((MANTISSA_END + 1) % 64));\n" +
               "            } else {\n" +
               "                //Shift left in order to fit the extra data\n" +
               "                mantissa = CommonUtils.shiftLeft(mantissa, (MANTISSA_END + 1) % 64);\n" +
               "                //Set the newly created unset data on the right\n" +
               "                mantissa[mantissa.length - 1] |= data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64));\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return mantissa;\n" +
               "    }\n" +
               "\n" +
               "    private long[] getMantissaBitsExtra() {\n" +
               "        long[] mantissa = new long[MANTISSA_BITS/64 + 1];\n" +
               "        int pos = 0;\n" +
               "\n" +
               "        if (MANTISSA_START % 64 != 0) { //Partial long at the start\n" +
               "            mantissa[pos++] = data[MANTISSA_START / 64] & ~(-1L << (64 - (MANTISSA_START % 64)))\n" +
               "                              | (getExponentBits() != 0 ? (1L << (64 - (MANTISSA_START % 64))) : 0);\n" +
               "        } else {\n" +
               "            mantissa[pos++] = getExponentBits() != 0 ? 1 : 0;\n" +
               "        }\n" +
               "\n" +
               "        //Full longs\n" +
               "        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {\n" +
               "            mantissa[pos++] = data[i];\n" +
               "        }\n" +
               "\n" +
               "        //Partial long at the end\n" +
               "        if ((MANTISSA_END + 1) % 64 != 0) {\n" +
               "            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {\n" +
               "                //The unset data to the right will be set here\n" +
               "                mantissa[mantissa.length - 1] = data[MANTISSA_END / 64];\n" +
               "                //Shift stuff as far to the right as it must go\n" +
               "                mantissa = CommonUtils.shiftRightUnsigned(mantissa, 64 - ((MANTISSA_END + 1) % 64));\n" +
               "            } else {\n" +
               "                //Shift left in order to fit the extra data\n" +
               "                mantissa = CommonUtils.shiftLeft(mantissa, (MANTISSA_END + 1) % 64);\n" +
               "                //Set the newly created unset data on the right\n" +
               "                mantissa[mantissa.length - 1] |= data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64));\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return mantissa;\n" +
               "    }\n" +
               "\n" +
               "    private void setMantissaBits(long[] mantissa) {\n" +
               "        //Partial long at the end\n" +
               "        if ((MANTISSA_END + 1) % 64 != 0) {\n" +
               "            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {\n" +
               "                //Shift stuff back\n" +
               "                mantissa = CommonUtils.shiftLeft(mantissa, 64 - ((MANTISSA_END + 1) % 64));\n" +
               "\n" +
               "                //Set data\n" +
               "                data[MANTISSA_END / 64] = mantissa[mantissa.length - 1];\n" +
               "            } else {\n" +
               "                //Set the newly created unset data on the right\n" +
               "                data[MANTISSA_END / 64] = (mantissa[mantissa.length - 1] & ~(-1L << ((MANTISSA_END + 1) % 64))) << (64 - ((MANTISSA_END + 1) % 64));\n" +
               "\n" +
               "                //Shift left in order to fit the extra data\n" +
               "                mantissa = CommonUtils.shiftRightUnsigned(mantissa, (MANTISSA_END + 1) % 64);\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        int pos = 0;\n" +
               "\n" +
               "        if (MANTISSA_START % 64 != 0) { //Partial long at the start\n" +
               "            data[MANTISSA_START / 64] = (data[MANTISSA_START / 64] & (-1L << (64 - (MANTISSA_START % 64)))) |\n" +
               "                                        (mantissa[pos++] & ~(-1L << (64 - (MANTISSA_START % 64))));\n" +
               "        }\n" +
               "\n" +
               "        //Full longs\n" +
               "        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {\n" +
               "            data[i] = mantissa[pos++];\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    private void setMantissaBitsExtra(long[] mantissa) {\n" +
               "        //Partial long at the end\n" +
               "        if ((MANTISSA_END + 1) % 64 != 0) {\n" +
               "            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {\n" +
               "                //Shift stuff back\n" +
               "                mantissa = CommonUtils.shiftLeft(mantissa, 64 - ((MANTISSA_END + 1) % 64));\n" +
               "\n" +
               "                //Set data\n" +
               "                data[MANTISSA_END / 64] = mantissa[mantissa.length - 1];\n" +
               "            } else {\n" +
               "                //Set the newly created unset data on the right\n" +
               "                data[MANTISSA_END / 64] = (mantissa[mantissa.length - 1] & ~(-1L << ((MANTISSA_END + 1) % 64))) << (64 - ((MANTISSA_END + 1) % 64));\n" +
               "\n" +
               "                //Shift left in order to fit the extra data\n" +
               "                mantissa = CommonUtils.shiftRightUnsigned(mantissa, (MANTISSA_END + 1) % 64);\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        int pos = 0;\n" +
               "\n" +
               "        if (MANTISSA_START % 64 != 0) { //Partial long at the start\n" +
               "            data[MANTISSA_START / 64] = (data[MANTISSA_START / 64] & (-1L << (64 - (MANTISSA_START % 64)))) |\n" +
               "                                        (mantissa[pos++] & ~(-1L << (64 - (MANTISSA_START % 64))));\n" +
               "        } else {\n" +
               "            pos++;\n" +
               "        }\n" +
               "\n" +
               "        //Full longs\n" +
               "        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {\n" +
               "            data[i] = mantissa[pos++];\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    private static void negateMantissa(long[] mantissa) {\n" +
               "        for (int i = 0; i < mantissa.length; i++) {\n" +
               "            mantissa[i] = ~mantissa[i];\n" +
               "        }\n" +
               "        mantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "\n" +
               "        //Simple add where the carry in is 1\n" +
               "        long carry = 1;\n" +
               "\n" +
               "        for (int i = mantissa.length - 1; i >= 0; i--) {\n" +
               "            long temp = mantissa[i] + carry;\n" +
               "\n" +
               "            if (carry == 1) {\n" +
               "                carry = (Long.compareUnsigned(temp, mantissa[i]) <= 0) ? 1 : 0;\n" +
               "            } else {\n" +
               "                carry = (Long.compareUnsigned(temp, mantissa[i]) < 0) ? 1 : 0;\n" +
               "            }\n" +
               "\n" +
               "            mantissa[i] = temp;\n" +
               "        }\n" +
               "//        long topBit = mantissa[0] & (-1L << (64 - (MANTISSA_START % 64)));\n" +
               "\n" +
               "        //Clear mantissa\n" +
               "        mantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "//        return carry == 1 ? 1 : (topBit != 0 ? 1 : 0);\n" +
               "    }\n" +
               "\n" +
               "    private static long[] negateMantissaCopy(long[] mantissa) {\n" +
               "        long[] newMantissa = new long[mantissa.length];\n" +
               "        for (int i = 0; i < mantissa.length; i++) {\n" +
               "            newMantissa[i] = ~mantissa[i];\n" +
               "        }\n" +
               "//        newMantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "\n" +
               "        //Simple add where the carry in is 1\n" +
               "        long carry = 1;\n" +
               "\n" +
               "        for (int i = newMantissa.length - 1; i >= 0; i--) {\n" +
               "            long temp = newMantissa[i] + carry;\n" +
               "\n" +
               "            if (carry == 1) {\n" +
               "                carry = (Long.compareUnsigned(temp, newMantissa[i]) <= 0) ? 1 : 0;\n" +
               "            } else {\n" +
               "                carry = (Long.compareUnsigned(temp, newMantissa[i]) < 0) ? 1 : 0;\n" +
               "            }\n" +
               "\n" +
               "            newMantissa[i] = temp;\n" +
               "        }\n" +
               "//        long topBit = mantissa[0] & (-1L << (64 - (MANTISSA_START % 64)));\n" +
               "\n" +
               "        //Clear mantissa\n" +
               "//        newMantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "//        return carry == 1 ? 1 : (topBit != 0 ? 1 : 0);\n" +
               "        return newMantissa;\n" +
               "    }\n" +
               "\n" +
               "    private static int compareMantissas(long[] mantissa1, long[] mantissa2) {\n" +
               "        for (int i = 0; i < mantissa1.length; i++) {\n" +
               "            int compareUnsigned = Long.compareUnsigned(mantissa1[i], mantissa2[i]);\n" +
               "            if (compareUnsigned != 0) {\n" +
               "                return compareUnsigned;\n" +
               "            }\n" +
               "        }\n" +
               "        return 0;\n" +
               "    }\n" +
               "\n" +
               "    private Type getType() {\n" +
               "        long exponentBits = getExponentBits();\n" +
               "        if (exponentBits == 0) {\n" +
               "            if (isMantissaZero()) {\n" +
               "                return Type.ZERO;\n" +
               "            } else {\n" +
               "                return Type.SUBNORMAL;\n" +
               "            }\n" +
               "        } else if (exponentBits == EXPONENT_FULL_MASK) {\n" +
               "            if (isMantissaZero()) {\n" +
               "                return Type.INFINITY;\n" +
               "            } else {\n" +
               "                return Type.NAN;\n" +
               "            }\n" +
               "        }\n" +
               "        return Type.NORMAL;\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " negate() {\n" +
               "        if (isNegative()) {\n" +
               "            data[0] &= Long.MAX_VALUE;\n" +
               "        } else {\n" +
               "            data[0] |= Long.MIN_VALUE;\n" +
               "        }\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " negate(Float" + bits + " num) {\n" +
               "        return new Float" + bits + "(num).negate();\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " abs() {\n" +
               "        if (isNegative()) {\n" +
               "            return negate();\n" +
               "        }\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " abs(Float" + bits + " num) {\n" +
               "        if (num.isNegative()) {\n" +
               "            return Float" + bits + ".negate(num);\n" +
               "        }\n" +
               "        return new Float" + bits + "(num);\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " add(Float" + bits + " that) {\n" +
               "        Type thisType = getType();\n" +
               "        if (thisType == Type.NAN || thisType == Type.INFINITY) {\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thisType == Type.ZERO) {\n" +
               "            //zero plus anything is that thing\n" +
               "            System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "            return this;\n" +
               "        }\n" +
               "        Type thatType = that.getType();\n" +
               "        if (thatType == Type.NAN || thatType == Type.INFINITY) {\n" +
               "            //anything plus NaN or Infinity is NaN or Infinity\n" +
               "            System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.ZERO) {\n" +
               "            //Anything plus zero is same thing\n" +
               "            return this;\n" +
               "        }\n" +
               "        //Both this and that are either Normal or Subnormal\n" +
               "\n" +
               "        long expThis = getExponentBits();\n" +
               "        long expThat = that.getExponentBits();\n" +
               "        long[] mantissaThis = this.getMantissaBits();\n" +
               "        long[] mantissaThat = that.getMantissaBits();\n" +
               "        boolean negativeCase = this.isNegative() != that.isNegative();\n" +
               "        boolean invertFinalResult = false;\n" +
               "        boolean signFlip = false;\n" +
               "\n" +
               "        int compareUnsigned = Long.compareUnsigned(expThis, expThat);\n" +
               "        long finalExp;\n" +
               "        int norms = 0;\n" +
               "\n" +
               "        if (compareUnsigned > 0) {\n" +
               "            long deltaExp = expThis - expThat;\n" +
               "            if (thatType == Type.SUBNORMAL) {\n" +
               "                deltaExp--;\n" +
               "            }\n" +
               "            if (deltaExp > MANTISSA_BITS + 1) { //Essentially adding a zero to this\n" +
               "                //Anything plus zero is same thing\n" +
               "                return this;\n" +
               "            }\n" +
               "            finalExp = expThis;\n" +
               "            mantissaThat = CommonUtils.shiftRightUnsigned(mantissaThat, (int) (deltaExp));\n" +
               "            if (thatType == Type.NORMAL) { //If it is normal we need to introduce an extra one\n" +
               "                long toChange = MANTISSA_BITS - deltaExp;\n" +
               "                if (toChange >= 0) {\n" +
               "                    CommonUtils.setBit(mantissaThat, (int) toChange, 1);\n" +
               "                }\n" +
               "            }\n" +
               "            if (thisType == Type.NORMAL) {\n" +
               "                norms = 1;\n" +
               "            }\n" +
               "        } else if (compareUnsigned < 0) {\n" +
               "            long deltaExp = expThat - expThis;\n" +
               "            if (thisType == Type.SUBNORMAL) {\n" +
               "                deltaExp--;\n" +
               "            }\n" +
               "            if (expThat - expThis > MANTISSA_BITS + 1) { //Essentially adding something to zero\n" +
               "                //zero plus anything is that thing\n" +
               "                System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "                return this;\n" +
               "            }\n" +
               "            finalExp = expThat;\n" +
               "            mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, (int) (deltaExp));\n" +
               "            if (thisType == Type.NORMAL) { //If it is normal we need to introduce an extra one\n" +
               "                long toChange = MANTISSA_BITS - deltaExp;\n" +
               "                if (toChange >= 0) {\n" +
               "                    CommonUtils.setBit(mantissaThis, (int) toChange, 1);\n" +
               "                }\n" +
               "            }\n" +
               "            if (thatType == Type.NORMAL) {\n" +
               "                norms = 1;\n" +
               "            }\n" +
               "        } else {\n" +
               "            finalExp = expThis;\n" +
               "            if (thisType == Type.NORMAL) {\n" +
               "                norms = 1;\n" +
               "            }\n" +
               "            if (thatType == Type.NORMAL) {\n" +
               "                norms++;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        if (negativeCase) {\n" +
               "            int compareMantissas = compareMantissas(mantissaThis, mantissaThat);\n" +
               "            if (compareMantissas > 0) {\n" +
               "                if (this.isNegative()){\n" +
               "                    invertFinalResult = true;\n" +
               "                }\n" +
               "            } else if (compareMantissas < 0) {\n" +
               "                long[] temp = mantissaThis;\n" +
               "                mantissaThis = mantissaThat;\n" +
               "                mantissaThat = temp;\n" +
               "                if (that.isNegative()) {\n" +
               "                    invertFinalResult = true;\n" +
               "                }\n" +
               "                signFlip = true;\n" +
               "            }\n" +
               "\n" +
               "            negateMantissa(mantissaThat);\n" +
               "        }\n" +
               "\n" +
               "        //At this point the mantissas in mantissaThis and mantissaThat both have an exponent of finalExp\n" +
               "        //They can be simply added now\n" +
               "        //We also know how many normalized numbers we had at the start so we know how much we have to compensate\n" +
               "        long carry = 0;\n" +
               "\n" +
               "        for (int i = mantissaThis.length - 1; i >= 0; i--) {\n" +
               "            long temp = mantissaThis[i] + mantissaThat[i] + carry;\n" +
               "\n" +
               "            if (carry == 1) {\n" +
               "                carry = (Long.compareUnsigned(temp, mantissaThis[i]) <= 0 &&\n" +
               "                         Long.compareUnsigned(temp, mantissaThat[i]) <= 0) ? 1 : 0;\n" +
               "            } else {\n" +
               "                carry = (Long.compareUnsigned(temp, mantissaThis[i]) < 0 &&\n" +
               "                         Long.compareUnsigned(temp, mantissaThat[i]) < 0) ? 1 : 0;\n" +
               "            }\n" +
               "\n" +
               "            mantissaThis[i] = temp;\n" +
               "        }\n" +
               "        long topBit = mantissaThis[0] & (-1L << (64 - (MANTISSA_START % 64)));\n" +
               "\n" +
               "        //Mantissas have been added but the normal assumed ones have been ignored\n" +
               "        if (topBit != 0) {\n" +
               "            //Overflow into the top of the mantissa (shift right and change exponent to fix)\n" +
               "            //Clear the overflow for easier stuff later\n" +
               "            mantissaThis[0] = mantissaThis[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "            norms++;\n" +
               "        } else if (carry == 1) {\n" +
               "            //Overflow past top of the mantissa (shift right, change exponent, and add the extra one)\n" +
               "            norms++;\n" +
               "        }\n" +
               "\n" +
               "        if (negativeCase) {\n" +
               "            if (invertFinalResult) {\n" +
               "                negateMantissa(mantissaThis);\n" +
               "            }\n" +
               "            if (norms == 2) {\n" +
               "                long steps = 1;\n" +
               "                for (int i = MANTISSA_BITS - 1; i >= 0 && CommonUtils.getBit(mantissaThis, i) == 0; i--, steps++);\n" +
               "                if (steps < MANTISSA_BITS) {\n" +
               "                    if (Long.compareUnsigned(steps, finalExp) > 0) {\n" +
               "                        steps = finalExp;\n" +
               "                    }\n" +
               "                    mantissaThis = CommonUtils.shiftLeft(mantissaThis, (int) steps);\n" +
               "                    mantissaThis[0] = mantissaThis[0] & ~(-1L << (64 - (MANTISSA_START % 64)));\n" +
               "                    finalExp -= steps;\n" +
               "                } else {\n" +
               "                    finalExp = 0;\n" +
               "                }\n" +
               "            }\n" +
               "        } else {\n" +
               "            if (norms == 1) {\n" +
               "                if (finalExp == 0) {\n" +
               "                    finalExp = 1;\n" +
               "                }\n" +
               "            } else if (norms == 2) {\n" +
               "                mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, 1);\n" +
               "                finalExp += 1;\n" +
               "            } else if (norms == 3) {\n" +
               "                mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, 1);\n" +
               "                CommonUtils.setBit(mantissaThis, MANTISSA_BITS - 1, 1);\n" +
               "                finalExp += 1;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        //At this point mantissaThis contains the summed mantissa\n" +
               "        //finalExp is the final exponent value\n" +
               "        setMantissaBits(mantissaThis);\n" +
               "        setExponentBits(finalExp);\n" +
               "        if (signFlip) {\n" +
               "            negate();\n" +
               "        }\n" +
               "\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public boolean isFinite() {\n" +
               "        return getType() == Type.ZERO || getType() == Type.NORMAL || getType() == Type.SUBNORMAL;\n" +
               "    }\n" +
               "    \n" +
               "    public boolean isInfinite() {\n" +
               "        return getType() == Type.INFINITY;\n" +
               "    }\n" +
               "    \n" +
               "    public boolean isNaN() {\n" +
               "        return getType() == Type.NAN;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " add(Float" + bits + " a,  Float" + bits + " b) {\n" +
               "        return new Float" + bits + "(a).add(b);\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " subtract(Float" + bits + " other) {\n" +
               "        return add(Float" + bits + ".negate(other));\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " copy() {\n" +
               "        return new Float" + bits + "(this);\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " subtract(Float" + bits + " a,  Float" + bits + " b) {\n" +
               "        return Float" + bits + ".add(a, Float" + bits + ".negate(b));\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " multiply(Float" + bits + " that) {\n" +
               "        Type thisType = getType();\n" +
               "        if (thisType == Type.NAN || thisType == Type.INFINITY || thisType == Type.ZERO) {\n" +
               "            return this;\n" +
               "        }\n" +
               "        Type thatType = that.getType();\n" +
               "        if (thatType == Type.NAN || thatType == Type.INFINITY) {\n" +
               "            //anything plus NaN or Infinity is NaN or Infinity\n" +
               "            System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.ZERO) {\n" +
               "            setExponentBits(0);\n" +
               "            setMantissaBits(ZERO_MANTISSA);\n" +
               "            return this;\n" +
               "        }\n" +
               "        //Both this and that are either Normal or Subnormal\n" +
               "        int norms = 0;\n" +
               "\n" +
               "        long thisExp;\n" +
               "        if (thisType == Type.SUBNORMAL) {\n" +
               "            thisExp = 1;\n" +
               "        } else {\n" +
               "            thisExp = this.getExponentBits();\n" +
               "        }\n" +
               "        long thatExp;\n" +
               "        if (thatType == Type.SUBNORMAL) {\n" +
               "            thatExp = 1;\n" +
               "        } else {\n" +
               "            thatExp = that.getExponentBits();\n" +
               "        }\n" +
               "\n" +
               "        long finalExp = thisExp - EXPONENT_OFFSET + thatExp;\n" +
               "\n" +
               "        long[] newMantissa = new long[MANTISSA_BITS / 64 + 1];\n" +
               "        long[] thisMantissa = this.getMantissaBitsExtra();\n" +
               "        long[] thatMantissa = that.getMantissaBitsExtra();\n" +
               "\n" +
               "        boolean sign = this.isNegative() ^ that.isNegative();\n" +
               "\n" +
               "        int shiftAmount = 0;\n" +
               "        for (int i = MANTISSA_BITS; i >= 0; i--) {\n" +
               "            if (CommonUtils.getBit(thisMantissa, i) != 0) {\n" +
               "                thatMantissa = CommonUtils.shiftRightUnsigned(thatMantissa, shiftAmount);\n" +
               "\n" +
               "                long carry = 0;\n" +
               "                for (int j = newMantissa.length - 1; j >= 0; j--) {\n" +
               "                    long temp = newMantissa[j] + thatMantissa[j] + carry;\n" +
               "\n" +
               "                    if (carry == 1) {\n" +
               "                        carry = (Long.compareUnsigned(temp, newMantissa[j]) <= 0 &&\n" +
               "                                 Long.compareUnsigned(temp, thatMantissa[j]) <= 0) ? 1 : 0;\n" +
               "                    } else {\n" +
               "                        carry = (Long.compareUnsigned(temp, newMantissa[j]) < 0 &&\n" +
               "                                 Long.compareUnsigned(temp, thatMantissa[j]) < 0) ? 1 : 0;\n" +
               "                    }\n" +
               "\n" +
               "                    newMantissa[j] = temp;\n" +
               "                }\n" +
               "                \n" +
               "                shiftAmount = 0;\n" +
               "            }\n" +
               "            shiftAmount++;\n" +
               "        }\n" +
               "\n" +
               "        int firstOne = 0;\n" +
               "        for (int i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);\n" +
               "        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;\n" +
               "        if (shiftRight > 0) {\n" +
               "            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);\n" +
               "            finalExp += shiftRight;\n" +
               "        } else if (shiftRight < 0) {\n" +
               "            shiftRight = -shiftRight;\n" +
               "            if (shiftRight <= MANTISSA_BITS) {\n" +
               "                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {\n" +
               "                    shiftRight = finalExp;\n" +
               "                }\n" +
               "                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);\n" +
               "                finalExp -= shiftRight;\n" +
               "            } else {\n" +
               "                finalExp = 0;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        setMantissaBitsExtra(newMantissa);\n" +
               "        setExponentBits(finalExp);\n" +
               "        if (this.isNegative() != sign) {\n" +
               "            negate();\n" +
               "        }\n" +
               "\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " multiply(Float" + bits + " a,  Float" + bits + " b) {\n" +
               "        return new Float" + bits + "(a).multiply(b);\n" +
               "    }\n" +
               "\n" +
               "    private static long[] mantissaDivision(long[] n, long[] d) {\n" +
               "        long[] q = new long[n.length]; //q = 0\n" +
               "        long[] nn = new long[n.length]; //r = 0\n" +
               "        System.arraycopy(n, 0, nn, 0, n.length);\n" +
               "\n" +
               "        boolean notEmpty = compareMantissas(nn, ZERO_MANTISSA_FULL) != 0;\n" +
               "\n" +
               "        for (int i = MANTISSA_BITS; i >= 0 && compareMantissas(d, ZERO_MANTISSA_FULL) != 0 && notEmpty; i--) {\n" +
               "            if (compareMantissas(nn, d) >= 0) {\n" +
               "                CommonUtils.setBit(q,i, 1);\n" +
               "                long[] toSubtract = negateMantissaCopy(d);\n" +
               "                toSubtract[0] = toSubtract[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));\n" +
               "\n" +
               "                long carry = 0;\n" +
               "\n" +
               "                for (int j = toSubtract.length - 1; j >= 0; j--) {\n" +
               "                    long temp = nn[j] + toSubtract[j] + carry;\n" +
               "\n" +
               "                    if (carry == 1) {\n" +
               "                        carry = (Long.compareUnsigned(temp, nn[j]) <= 0 &&\n" +
               "                                 Long.compareUnsigned(temp, toSubtract[j]) <= 0) ? 1 : 0;\n" +
               "                    } else {\n" +
               "                        carry = (Long.compareUnsigned(temp, nn[j]) < 0 &&\n" +
               "                                 Long.compareUnsigned(temp, toSubtract[j]) < 0) ? 1 : 0;\n" +
               "                    }\n" +
               "\n" +
               "                    nn[j] = temp;\n" +
               "                }\n" +
               "                nn[0] = nn[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));\n" +
               "                notEmpty = compareMantissas(nn, ZERO_MANTISSA_FULL) != 0;\n" +
               "            }\n" +
               "            if (notEmpty) {\n" +
               "                d = CommonUtils.shiftRightUnsigned(d, 1);\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return q;\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " divide(Float" + bits + " that) {\n" +
               "        Type thisType = getType();\n" +
               "        if (thisType == Type.NAN) {\n" +
               "            return this;\n" +
               "        }\n" +
               "        Type thatType = that.getType();\n" +
               "        if (thatType == Type.NAN) {\n" +
               "            System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.ZERO) {\n" +
               "            setExponentBits(EXPONENT_FULL_MASK);\n" +
               "            if (thisType == Type.ZERO) {\n" +
               "                CommonUtils.setBit(data,0, 1); //NaN\n" +
               "            } else {\n" +
               "                setMantissaBits(new long[MANTISSA_BITS/64 + 1]); //Infinity\n" +
               "            }\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.INFINITY) {\n" +
               "            if (thisType == Type.INFINITY) {\n" +
               "                setExponentBits(EXPONENT_FULL_MASK);\n" +
               "                CommonUtils.setBit(data,0, 1); //NaN\n" +
               "            } else {\n" +
               "                setExponentBits(0);\n" +
               "                setMantissaBits(new long[MANTISSA_BITS/64 + 1]); //Zero\n" +
               "            }\n" +
               "            return this;\n" +
               "        }\n" +
               "\n" +
               "        long thisExp;\n" +
               "        if (thisType == Type.SUBNORMAL) {\n" +
               "            thisExp = 1;\n" +
               "        } else {\n" +
               "            thisExp = this.getExponentBits();\n" +
               "        }\n" +
               "        long thatExp;\n" +
               "        if (thatType == Type.SUBNORMAL) {\n" +
               "            thatExp = 1;\n" +
               "        } else {\n" +
               "            thatExp = that.getExponentBits();\n" +
               "        }\n" +
               "\n" +
               "        long finalExp = thisExp + EXPONENT_OFFSET - thatExp;\n" +
               "\n" +
               "        long[] thisMantissa = this.getMantissaBitsExtra();\n" +
               "        long[] thatMantissa = that.getMantissaBitsExtra();\n" +
               "\n" +
               "        boolean sign = this.isNegative() ^ that.isNegative();\n" +
               "\n" +
               "        long[] newMantissa = mantissaDivision(thisMantissa, thatMantissa);\n" +
               "\n" +
               "        int firstOne = 0;\n" +
               "        int i;\n" +
               "        for (i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);\n" +
               "        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;\n" +
               "        if (shiftRight > 0) {\n" +
               "            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);\n" +
               "            finalExp += shiftRight;\n" +
               "        } else if (shiftRight < 0) {\n" +
               "            shiftRight = -shiftRight;\n" +
               "            if (shiftRight <= MANTISSA_BITS) {\n" +
               "                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {\n" +
               "                    shiftRight = finalExp;\n" +
               "                }\n" +
               "                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);\n" +
               "                finalExp -= shiftRight;\n" +
               "            } else {\n" +
               "                finalExp = 0;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        setMantissaBitsExtra(newMantissa);\n" +
               "        setExponentBits(finalExp);\n" +
               "        if (this.isNegative() != sign) {\n" +
               "            negate();\n" +
               "        }\n" +
               "\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " divide(Float" + bits + " a,  Float" + bits + " b) {\n" +
               "        return new Float" + bits + "(a).divide(b);\n" +
               "    }\n" +
               "\n" +
               "    private static long[] mantissaModulo(long[] n, long[] d) {\n" +
               "        long[] q = new long[n.length];\n" +
               "        long[] r = new long[n.length];\n" +
               "        long[] t = new long[n.length];\n" +
               "\n" +
               "        boolean isFirstN = false;\n" +
               "\n" +
               "        for (int i = n.length * 64 - 1; i >= 0; i--) {\n" +
               "            if (isFirstN || CommonUtils.getBit(n, i) != 0) {\n" +
               "                isFirstN = true;\n" +
               "                r = CommonUtils.shiftLeft(r, 1);\n" +
               "                CommonUtils.setBit(r, 0, CommonUtils.getBit(n, i));\n" +
               "                if (compareMantissas(r, d) >= 0) {\n" +
               "                    for (int j = 0; j < d.length; j++) {\n" +
               "                        t[j] = ~d[j];\n" +
               "                    }\n" +
               "\n" +
               "                    long carry = 1;\n" +
               "\n" +
               "                    for (int j = n.length - 1; j >= 0; j--) {\n" +
               "                        long temp = r[j] + t[j] + carry;\n" +
               "\n" +
               "                        if (carry == 1) {\n" +
               "                            carry = (Long.compareUnsigned(temp, r[j]) <= 0 &&\n" +
               "                                     Long.compareUnsigned(temp, t[j]) <= 0) ? 1 : 0;\n" +
               "                        } else {\n" +
               "                            carry = (Long.compareUnsigned(temp, r[j]) < 0 &&\n" +
               "                                     Long.compareUnsigned(temp, t[j]) < 0) ? 1 : 0;\n" +
               "                        }\n" +
               "\n" +
               "                        r[j] = temp;\n" +
               "                    }\n" +
               "                    r[0] = r[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));\n" +
               "\n" +
               "                    CommonUtils.setBit(q, i, 1);\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        return r;\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public Float" + bits + " modulo(Float" + bits + " that) {\n" +
               "        Type thisType = getType();\n" +
               "        if (thisType == Type.NAN) {\n" +
               "            return this;\n" +
               "        }\n" +
               "        Type thatType = that.getType();\n" +
               "        if (thatType == Type.NAN) {\n" +
               "            System.arraycopy(that.data, 0, this.data, 0, LONGS);\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.ZERO || thisType == Type.INFINITY) {\n" +
               "            setExponentBits(EXPONENT_FULL_MASK);\n" +
               "            CommonUtils.setBit(data,0, 1); //NaN\n" +
               "            return this;\n" +
               "        }\n" +
               "        if (thatType == Type.INFINITY) {\n" +
               "            return this;\n" +
               "        }\n" +
               "\n" +
               "        long thisExp;\n" +
               "        if (thisType == Type.SUBNORMAL) {\n" +
               "            thisExp = 1;\n" +
               "        } else {\n" +
               "            thisExp = this.getExponentBits();\n" +
               "        }\n" +
               "        long thatExp;\n" +
               "        if (thatType == Type.SUBNORMAL) {\n" +
               "            thatExp = 1;\n" +
               "        } else {\n" +
               "            thatExp = that.getExponentBits();\n" +
               "        }\n" +
               "\n" +
               "        long finalExp = thisExp;\n" +
               "\n" +
               "        long[] thisMantissa = this.getMantissaBitsExtra();\n" +
               "        long[] thatMantissa = that.getMantissaBitsExtra();\n" +
               "        if (thisExp > thatExp) {\n" +
               "            if (thisExp - thatExp > MANTISSA_BITS + 1) {\n" +
               "                Arrays.fill(thisMantissa, 0);\n" +
               "            } else {\n" +
               "                thatMantissa = CommonUtils.shiftRightUnsigned(thatMantissa, (int) (thisExp - thatExp));\n" +
               "            }\n" +
               "        }\n" +
               "        if (thisExp < thatExp) {\n" +
               "            finalExp = thatExp;\n" +
               "            if (thatExp - thisExp > MANTISSA_BITS + 1) {\n" +
               "                Arrays.fill(thatMantissa, 0);\n" +
               "            } else {\n" +
               "                thisMantissa = CommonUtils.shiftRightUnsigned(thisMantissa, (int) (thatExp - thisExp));\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        boolean sign = this.isNegative();\n" +
               "\n" +
               "        long[] newMantissa = mantissaModulo(thisMantissa, thatMantissa);\n" +
               "\n" +
               "        int firstOne = 0;\n" +
               "        int i;\n" +
               "        for (i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);\n" +
               "        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;\n" +
               "        if (shiftRight > 0) {\n" +
               "            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);\n" +
               "            finalExp += shiftRight;\n" +
               "        } else if (shiftRight < 0) {\n" +
               "            shiftRight = -shiftRight;\n" +
               "            if (shiftRight <= MANTISSA_BITS) {\n" +
               "                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {\n" +
               "                    shiftRight = finalExp;\n" +
               "                }\n" +
               "                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);\n" +
               "                finalExp -= shiftRight;\n" +
               "            } else {\n" +
               "                finalExp = 0;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        setMantissaBitsExtra(newMantissa);\n" +
               "        setExponentBits(finalExp);\n" +
               "        if (this.isNegative() != sign) {\n" +
               "            negate();\n" +
               "        }\n" +
               "\n" +
               "        return this;\n" +
               "    }\n" +
               "\n" +
               "    public static Float" + bits + " modulo(Float" + bits + " a, Float" + bits + " b) {\n" +
               "        return new Float" + bits + "(a).modulo(b);\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public boolean equals(Object o) {\n" +
               "        if (this == o) return true;\n" +
               "        if (o == null || getClass() != o.getClass()) return false;\n" +
               "        Float" + bits + " float128 = (Float" + bits + ") o;\n" +
               "        if (float128.getType() == Type.NAN || this.getType() == Type.NAN || float128.getType() == Type.INFINITY || this.getType() == Type.INFINITY) {\n" +
               "            return false;\n" +
               "        }\n" +
               "        if (float128.getType() == Type.ZERO && this.getType() == Type.ZERO) {\n" +
               "            return true;\n" +
               "        }\n" +
               "        return Arrays.equals(data, float128.data);\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public int hashCode() {\n" +
               "        if (this.getType() == Type.ZERO) {\n" +
               "            return Type.ZERO.hashCode(); // Since ZEROs are always equal: they all share the same hashcode\n" +
               "        }\n" +
               "        return Arrays.hashCode(data);\n" +
               "    }\n" +
               "\n" +
               "\n" +
               "    @Override\n" +
               "    public int compareTo(Float" + bits + " o) {\n" +
               "        Float" + bits + " a = this;\n" +
               "        Float" + bits + " b = o;\n" +
               "        Type aType = a.getType();\n" +
               "        Type bType = b.getType();\n" +
               "        if (aType == bType) {\n" +
               "            if (aType == Type.ZERO) {\n" +
               "                return 0;\n" +
               "            } else if (aType == Type.INFINITY) {\n" +
               "                return a.isNegative() ? (b.isNegative() ? 0 : -1) : (b.isNegative() ? 1 : 0);\n" +
               "            } else if (aType == Type.NAN) {\n" +
               "                return 0;\n" +
               "            } else {\n" +
               "                return a.isNegative() ?\n" +
               "                        (b.isNegative() ? NEGATIVE_COMPARATOR.compare(a, b) : -1) :\n" +
               "                        (b.isNegative() ? 1 : POSITIVE_COMPARATOR.compare(a, b));\n" +
               "            }\n" +
               "        } else {\n" +
               "            if (bType == Type.NAN) {\n" +
               "                return 0;\n" +
               "            }\n" +
               "            // Types are different\n" +
               "            if (aType == Type.ZERO) {\n" +
               "                return b.isNegative() ? 1 : -1;\n" +
               "            } else if (aType == Type.INFINITY) {\n" +
               "                return a.isNegative() ? -1 : 1;\n" +
               "            } else if (aType == Type.NAN) {\n" +
               "                return 0;\n" +
               "            } else if (bType == Type.INFINITY) {\n" +
               "                return b.isNegative() ? 1 : -1;\n" +
               "            } else { // SUBNORMAL | NORMAL\n" +
               "                if (bType == Type.ZERO) {\n" +
               "                    return a.isNegative() ? -1 : 1;\n" +
               "                } else { // SUBNORMAL | NORMAL\n" +
               "                    return a.isNegative() ?\n" +
               "                            (b.isNegative() ? NEGATIVE_COMPARATOR.compare(a, b) : -1) :\n" +
               "                            (b.isNegative() ? 1 : POSITIVE_COMPARATOR.compare(a, b));\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public String toString() {\n" +
               "        StringBuilder builder = new StringBuilder();\n" +
               "\n" +
               "        String negative = \"\";\n" +
               "        if (isNegative()) {\n" +
               "            negative = \"-\";\n" +
               "        }\n" +
               "\n" +
               "        Type type = getType();\n" +
               "\n" +
               "        if (type == Type.ZERO) {\n" +
               "            builder.append('0');\n" +
               "        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {\n" +
               "            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);\n" +
               "            long base10 = (long) Math.floor(base10s);\n" +
               "            if (base10 <= 7 && base10 >= -3) {\n" +
               "                StringBuilder fullString = new StringBuilder(toFullString(10));\n" +
               "                while (fullString.charAt(fullString.length() - 1) == '0') {\n" +
               "                    fullString.deleteCharAt(fullString.length() - 1);\n" +
               "                }\n" +
               "                if (fullString.charAt(fullString.length() - 1) == '.') {\n" +
               "                    fullString.deleteCharAt(fullString.length() - 1);\n" +
               "                }\n" +
               "                return fullString.toString();\n" +
               "            } else {\n" +
               "                return toSciString(10);\n" +
               "            }\n" +
               "        } else  if (type == Type.INFINITY) {\n" +
               "            builder.append(negative);\n" +
               "            builder.append(\"INFINITY\");\n" +
               "        } else if (type == Type.NAN) {\n" +
               "            builder.append(\"NAN\");\n" +
               "        }\n" +
               "\n" +
               "        return builder.toString();\n" +
               "    }\n" +
               "\n" +
               "    public String toSciString(int precision) {\n" +
               "        StringBuilder builder = new StringBuilder();\n" +
               "\n" +
               "        String negative = \"\";\n" +
               "        if (isNegative()) {\n" +
               "            negative = \"-\";\n" +
               "        }\n" +
               "\n" +
               "        Type type = getType();\n" +
               "\n" +
               "        if (type == Type.ZERO) {\n" +
               "            builder.append('0');\n" +
               "        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {\n" +
               "            builder.append(negative);\n" +
               "            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);\n" +
               "            long base10 = (long) Math.floor(base10s);\n" +
               "            long exponent = getExponentBits() - EXPONENT_OFFSET;\n" +
               "            ManRes mantissa = getMantissa(type == Type.NORMAL, exponent, precision);\n" +
               "            // Remove trailing whitespace\n" +
               "            while (mantissa.result.length() > 0 && mantissa.result.charAt(mantissa.result.length() - 1) == '0') {\n" +
               "                mantissa.result.deleteCharAt(mantissa.result.length() - 1);\n" +
               "            }\n" +
               "            // Remove starting whitespace\n" +
               "            while (mantissa.result.length() > 0 && mantissa.result.charAt(0) == '0') {\n" +
               "                mantissa.result.deleteCharAt(0);\n" +
               "                base10--;\n" +
               "            }\n" +
               "            if (precision > 0) {\n" +
               "                if (mantissa.result.length() > precision) {\n" +
               "                    // round to precision\n" +
               "                    char c = mantissa.result.charAt(precision);\n" +
               "                    mantissa.result.delete(precision, mantissa.result.length());\n" +
               "                    if (c >= '5') { // round up\n" +
               "                        String newMantissa = CommonUtils.addStrings(mantissa.result.toString(), \"1\", 0);\n" +
               "                        mantissa.result.delete(0, mantissa.result.length());\n" +
               "                        mantissa.result.append(newMantissa);\n" +
               "                        mantissa.result.delete(precision, mantissa.result.length());\n" +
               "                    }\n" +
               "                }\n" +
               "                // Increase to precision\n" +
               "                while (precision > mantissa.result.length()) {\n" +
               "                    mantissa.result.append('0');\n" +
               "                }\n" +
               "            }\n" +
               "            mantissa.result.insert(1, '.');\n" +
               "            builder.append(mantissa.result);\n" +
               "            builder.append(\"E\");\n" +
               "            builder.append(base10 + mantissa.baseOffset);\n" +
               "        } else  if (type == Type.INFINITY) {\n" +
               "            builder.append(negative);\n" +
               "            builder.append(\"INFINITY\");\n" +
               "        } else if (type == Type.NAN) {\n" +
               "            builder.append(\"NAN\");\n" +
               "        }\n" +
               "\n" +
               "        return builder.toString();\n" +
               "    }\n" +
               "\n" +
               "    public String toFullString(int precision) {\n" +
               "        StringBuilder builder = new StringBuilder();\n" +
               "\n" +
               "        String negative = \"\";\n" +
               "        if (isNegative()) {\n" +
               "            negative = \"-\";\n" +
               "        }\n" +
               "\n" +
               "        Type type = getType();\n" +
               "\n" +
               "        if (type == Type.ZERO) {\n" +
               "            builder.append('0');\n" +
               "            while (precision > builder.length()) {\n" +
               "                builder.append('0');\n" +
               "            }\n" +
               "            if (builder.length() > 1) {\n" +
               "                builder.insert(1, '.');\n" +
               "            }\n" +
               "        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {\n" +
               "            builder.append(negative);\n" +
               "            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);\n" +
               "            long base10 = (long) Math.floor(base10s);\n" +
               "            long exponent = getExponentBits() - EXPONENT_OFFSET;\n" +
               "            ManRes mantissa = getMantissa(type == Type.NORMAL, exponent, precision);\n" +
               "            base10 += mantissa.baseOffset;\n" +
               "            while (base10 < 0) {\n" +
               "                mantissa.result.insert(0, '0');\n" +
               "                base10++;\n" +
               "            }\n" +
               "            base10++;\n" +
               "            if (precision > 0) {\n" +
               "                if (mantissa.result.length() > precision) {\n" +
               "                    // round to precision\n" +
               "                    char c = mantissa.result.charAt(precision);\n" +
               "                    mantissa.result.delete(precision, mantissa.result.length());\n" +
               "                    if (c >= '5') { // round up\n" +
               "                        String newMantissa = CommonUtils.addStrings(mantissa.result.toString(), \"1\", 0);\n" +
               "                        mantissa.result.delete(0, mantissa.result.length());\n" +
               "                        mantissa.result.append(newMantissa);\n" +
               "                        mantissa.result.delete(precision, mantissa.result.length());\n" +
               "                    }\n" +
               "                }\n" +
               "                // Increase to precision\n" +
               "                while (precision > mantissa.result.length()) {\n" +
               "                    mantissa.result.append('0');\n" +
               "                }\n" +
               "            }\n" +
               "            if (mantissa.result.length() > base10) {\n" +
               "                mantissa.result.insert((int) base10, '.');\n" +
               "            }\n" +
               "            builder.append(mantissa.result);\n" +
               "        } else  if (type == Type.INFINITY) {\n" +
               "            builder.append(negative);\n" +
               "            builder.append(\"INFINITY\");\n" +
               "        } else if (type == Type.NAN) {\n" +
               "            builder.append(\"NAN\");\n" +
               "        }\n" +
               "\n" +
               "        return builder.toString();\n" +
               "    }\n" +
               "\n" +
               "    private ManRes getMantissa(boolean isNormal, long exponent, int precision) {\n" +
               "        if (!isNormal) {\n" +
               "            exponent = 1 - EXPONENT_OFFSET;\n" +
               "        }\n" +
               "        ExpString scaleString = CommonUtils.getExponentString(exponent, null, 0);\n" +
               "        long[] mantissaBits = getMantissaBits();\n" +
               "        StringBuilder result = new StringBuilder();\n" +
               "        // The index of the location of the 10^0s place in the string\n" +
               "        int resultZero = scaleString.zeroIndex;\n" +
               "        if (isNormal) {\n" +
               "            result.append(scaleString.string);\n" +
               "        } else {\n" +
               "            result.append('0');\n" +
               "        }\n" +
               "\n" +
               "        ExpString lastMemo = scaleString;\n" +
               "        long lastExp = exponent;\n" +
               "\n" +
               "        int additionalBase = 0;\n" +
               "        for (int i = 0; i < MANTISSA_BITS; i++) {\n" +
               "            exponent--;\n" +
               "\n" +
               "            int mantissaIndex = MANTISSA_START + i;\n" +
               "            if ((mantissaBits[mantissaIndex / 64] & (1L << (63 - (mantissaIndex % 64)))) != 0) {\n" +
               "                ExpString nowScale = CommonUtils.getExponentString(exponent, lastMemo, lastExp);\n" +
               "                int stringZero = nowScale.zeroIndex;\n" +
               "                lastMemo = nowScale;\n" +
               "                lastExp = exponent;\n" +
               "\n" +
               "                if (result.length() == 1 && result.charAt(0) == '0') {\n" +
               "                    additionalBase += stringZero - resultZero;\n" +
               "                    resultZero = stringZero;\n" +
               "                }\n" +
               "\n" +
               "                if (precision > 0 && resultZero - stringZero > precision + 1) {\n" +
               "                    break;\n" +
               "                }\n" +
               "\n" +
               "                char[] chars = nowScale.string.toCharArray();\n" +
               "\n" +
               "                int partOffset = resultZero - stringZero;\n" +
               "\n" +
               "                // Ensure proper length\n" +
               "                for (int j = result.length(); j < chars.length + partOffset; j++) {\n" +
               "                    result.append('0');\n" +
               "                }\n" +
               "                //Sum strings\n" +
               "                int carry = 0;\n" +
               "                for (int j = chars.length - 1; (j >= 0 || carry != 0) && (j + partOffset >= 0); j--) {\n" +
               "                    int toAdd = (j >= 0) ? (chars[j] - '0') : 0;\n" +
               "                    int current = result.charAt(j + partOffset) - '0';\n" +
               "                    int here = toAdd + current + carry;\n" +
               "                    carry = here >= 10 ? 1 : 0;\n" +
               "                    result.setCharAt(j + partOffset, (char) ((here % 10) + '0'));\n" +
               "                }\n" +
               "                if (carry == 1) {\n" +
               "                    resultZero++;\n" +
               "                    additionalBase++;\n" +
               "                    result.insert(0, '1');\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "        return new ManRes(additionalBase, result);\n" +
               "    }\n" +
               "\n" +
               "    private static double base2To10Exp(long base2Exponent) {\n" +
               "        return base2Exponent * LOG_10_OF_2;\n" +
               "    }\n" +
               "\n" +
               "    private enum Type {\n" +
               "        ZERO, SUBNORMAL, NORMAL, INFINITY, NAN\n" +
               "    }\n" +
               "\n" +
               "    private class ManRes {\n" +
               "        int baseOffset;\n" +
               "        StringBuilder result;\n" +
               "\n" +
               "        ManRes(int baseOffset, StringBuilder result) {\n" +
               "            this.baseOffset = baseOffset;\n" +
               "            this.result = result;\n" +
               "        }\n" +
               "    }\n" +
               "}\n";
    }
}
