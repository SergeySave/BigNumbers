package com.sergeysav.bignum;

import com.sergeysav.bignum.CommonUtils.ExpString;

import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Comparator;

/**
 * Represents a 128 bit floating point number with
 * 1 sign bit
 * 15 exponent bits
 * 112 mantissa bits
 *
 * @author sergeys
 */
public final class Float128 implements MutableNumber<Float128> {

    /**
     * A constant used for converting base 2 logarithms to base 10 logarithms (used in toString methods)
     */
    private static final double LOG_10_OF_2 = Math.log10(2);

    /**
     * The number of sign bits
     */
    private static final int SIGN_BITS = 1;
    /**
     * The number of exponent bits
     */
    private static final int EXPONENT_BITS = 15;
    /**
     * The number of mantissa bits
     */
    private static final int MANTISSA_BITS = 112;

    /**
     * The number of longs used to store the full data
     */
    private static final int LONGS = 2; // 128 bits

    /**
     * The start of the sign section (INCLUSIVE)
     */
    private static final int SIGN_START = 0; //INCLUSIVE
    /**
     * The end of the sign section (INCLUSIVE)
     */
    private static final int SIGN_END = SIGN_START + SIGN_BITS - 1; //INCLUSIVE
    /**
     * The start of the exponent section (INCLUSIVE)
     */
    private static final int EXPONENT_START = SIGN_END + 1; //INCLUSIVE
    /**
     * The end of the exponent section (INCLUSIVE)
     */
    private static final int EXPONENT_END = EXPONENT_START + EXPONENT_BITS - 1; //INCLUSIVE
    /**
     * The start of the mantissa section (INCLUSIVE)
     */
    private static final int MANTISSA_START = EXPONENT_END + 1; //INCLUSIVE
    /**
     * The end of the mantissa section (INCLUSIVE)
     */
    private static final int MANTISSA_END = MANTISSA_START + MANTISSA_BITS - 1; //INCLUSIVE

    /**
     * The exponent offset (the value of the exponent section when the exponent is 0)
     */
    private static final long EXPONENT_OFFSET = CommonUtils.pow(2, EXPONENT_BITS - 1) - 1;
    /**
     * A bitmask for the exponent section
     */
    private static final long EXPONENT_FULL_MASK = ~(-1L << EXPONENT_BITS);

    /**
     * The mantissa array for a mantissa representing a zero value
     */
    private static final long[] ZERO_MANTISSA = new long[(MANTISSA_BITS - 1)/64 + 1];
    /**
     * The full mantissa array for a mantissa representing a zero value
     */
    private static final long[] ZERO_MANTISSA_FULL = new long[MANTISSA_BITS/64 + 1];


    /**
     * The comparator used for comparing two positive values
     */
    private static final Comparator<Float128> POSITIVE_COMPARATOR = Comparator
            .comparing(Float128::getExponentBits)
            .thenComparing((a, b) -> compareMantissas(a.getMantissaBits(), b.getMantissaBits()));
    /**
     * The comparator used for comparing two negative values
     */
    private static final Comparator<Float128> NEGATIVE_COMPARATOR = Comparator
            .comparing(Float128::getExponentBits)
            .thenComparing((a, b) -> compareMantissas(a.getMantissaBits(), b.getMantissaBits()))
            .reversed();

    /**
     * A constant equal to 0
     */
    public static final Float128 ZERO = new Float128();
    /**
     * A constant equal to 1
     */
    public static final Float128 ONE = from(1);
    /**
     * A constant equal to 10
     */
    public static final Float128 TEN = from(10);
    /**
     * A constant equal to positive infinity
     */
    public static final Float128 POSITIVE_INFINITY = ONE.copy().divide(ZERO);
    /**
     * A constant equal to negative infinity
     */
    public static final Float128 NEGATIVE_INFINITY = ONE.copy().negate().divide(ZERO);
    /**
     * A constant equal to NaN
     */
    public static final Float128 NAN = ZERO.copy().divide(ZERO);
    /**
     * A constant equal to the minimum normal value representable
     */
    public static final Float128 MIN_NORMAL = fromStructure(false, 1, new long[(MANTISSA_BITS - 1)/64 + 1]);
    /**
     * A constant equal to the maximum normal value representable
     */
    public static final Float128 MAX_VALUE = fromStructure(false, EXPONENT_FULL_MASK - 1, new long[]{-1L, -1L});
    /**
     * A constant equal to the minimum subnormal value representable
     */
    public static final Float128 MIN_VALUE = fromStructure(false, 0, new long[]{0L, 1L});
    /**
     * A constant equal to the maximum subnormal value representable
     */
    public static final Float128 MAX_SUBNORMAL = fromStructure(false, 0, new long[]{-1L, -1L});

    /**
     * The longs representing the internal data
     */
    private long[] data;

    static {
        if (EXPONENT_BITS > 64) {
            throw new IllegalStateException("Cannot represent float with over 64 exponent bits");
        }
    }

    /**
     * Creates a new floating point with a value of zero
     */
    public Float128() {
        data = new long[LONGS];
    }

    /**
     * Creates a copy of the given floating point
     * 
     * @param src the floating point to copy
     */
    public Float128(Float128 src) {
        data = new long[LONGS];
        System.arraycopy(src.data, 0, this.data, 0, LONGS);
    }

    /**
     * Creates a floating point from a given double's binary data
     *
     * WARNING: this creates it from the data and not to string representation
     * using 1.0/3 will result in 0.333333333333333314829616256247390992939472198486328125
     * instead of the more accurate result that could be represented with this data structure
     *
     * @param original the double to create the floating point from
     * @return a new floating point
     */
    public static Float128 from(double original) {
        Float128 num = new Float128();
        long bits = Double.doubleToRawLongBits(original);

        long[] mantissa = new long[(MANTISSA_BITS - 1)/64 + 1];
        mantissa[0] = (((bits & 4503599627370495L) << 12) >>> MANTISSA_START);
        mantissa[1] = ((bits & ~(-1L << (MANTISSA_START - 12))) << (64 - (MANTISSA_START - 12)));

        num.setMantissaBits(mantissa);

        if ((original < 0) ^ num.isNegative()) {
            num.negate(); //Invert if needed
        }

        if ((bits & 9223090561878065152L) == 0) {
            num.setExponentBits(0);
        } else {
            num.setExponentBits(((bits & 9223090561878065152L) >>> 52) - 1023 + EXPONENT_OFFSET);
        }

        return num;
    }

    /**
     * Creates a floating point from a given set of binary data
     * 
     * WARNING: Advanced method: use with caution
     * 
     * @param parts an array of 2 longs representing the binary data
     * @return a floating point representing the given data
     */
    public static Float128 bytesOf(long... parts) {
        if (parts.length != LONGS) {
            throw new IllegalArgumentException("Incorrect number of bytes");
        }

        Float128 val = new Float128();

        System.arraycopy(parts, 0, val.data, 0, LONGS);

        return val;
    }

    private static Float128 fromStructure(boolean negative, long exponent, long[] mantissa) {
        Float128 result = new Float128();
        result.setExponentBits(exponent);
        result.setMantissaBits(mantissa);
        if (negative) {
            result.negate();
        }
        return result;
    }

    private boolean isNegative() {
        return data[0] < 0;
    }

    private long getExponentBits() {
        if (EXPONENT_BITS == 64) {
            //First 63 bits after sign bit and the next bit
            return (data[0] << 1) | (data[1] >>> 63);
        }
        //Remove sign bit and shift right until all we have is exponent bits
        return (data[0] & Long.MAX_VALUE) >>> (63 - EXPONENT_BITS);
    }

    private void setExponentBits(long exponentBits) {
        if (EXPONENT_BITS == 64) {
            //First 63 bits after sign bit and the next bit
            data[0] = (data[0] & Long.MIN_VALUE) | (exponentBits >>> 1);
            data[1] = (data[1] & Long.MAX_VALUE) | (exponentBits << 63);
        }
        //Take sign bit and bits after the end of the exponent and combine them with the exponent's bits
        data[0] = (data[0] & Long.MIN_VALUE) | (data[0] & ~(-1L << (63 - EXPONENT_END)))
                  | ((exponentBits & ~(-1L << EXPONENT_BITS)) << (63 - EXPONENT_END));
    }

    private boolean isMantissaZero() {
        if (MANTISSA_START % 64 != 0) { //Partial long at the start
            if (data[MANTISSA_START / 64] << (MANTISSA_START % 64) != 0) {
                return false;
            }
        }

        //Full longs
        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {
            if (data[i] != 0) {
                return false;
            }
        }

        //Partial long at the end
        if ((MANTISSA_END + 1) % 64 != 0) {
            if (data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64)) != 0) {
                return false;
            }
        }

        return true;
    }

    private long[] getMantissaBits() {
        long[] mantissa = new long[(MANTISSA_BITS - 1)/64 + 1];
        int pos = 0;

        if (MANTISSA_START % 64 != 0) { //Partial long at the start
            mantissa[pos++] = data[MANTISSA_START / 64] & ~(-1L << (64 - (MANTISSA_START % 64)));
        }

        //Full longs
        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {
            mantissa[pos++] = data[i];
        }

        //Partial long at the end
        if ((MANTISSA_END + 1) % 64 != 0) {
            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {
                //The unset data to the right will be set here
                mantissa[mantissa.length - 1] = data[MANTISSA_END / 64];
                //Shift stuff as far to the right as it must go
                mantissa = CommonUtils.shiftRightUnsigned(mantissa, 64 - ((MANTISSA_END + 1) % 64));
            } else {
                //Shift left in order to fit the extra data
                mantissa = CommonUtils.shiftLeft(mantissa, (MANTISSA_END + 1) % 64);
                //Set the newly created unset data on the right
                mantissa[mantissa.length - 1] |= data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64));
            }
        }

        return mantissa;
    }

    private long[] getMantissaBitsExtra() {
        long[] mantissa = new long[MANTISSA_BITS/64 + 1];
        int pos = 0;

        if (MANTISSA_START % 64 != 0) { //Partial long at the start
            mantissa[pos++] = data[MANTISSA_START / 64] & ~(-1L << (64 - (MANTISSA_START % 64)))
                              | (getExponentBits() != 0 ? (1L << (64 - (MANTISSA_START % 64))) : 0);
        } else {
            mantissa[pos++] = getExponentBits() != 0 ? 1 : 0;
        }

        //Full longs
        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {
            mantissa[pos++] = data[i];
        }

        //Partial long at the end
        if ((MANTISSA_END + 1) % 64 != 0) {
            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {
                //The unset data to the right will be set here
                mantissa[mantissa.length - 1] = data[MANTISSA_END / 64];
                //Shift stuff as far to the right as it must go
                mantissa = CommonUtils.shiftRightUnsigned(mantissa, 64 - ((MANTISSA_END + 1) % 64));
            } else {
                //Shift left in order to fit the extra data
                mantissa = CommonUtils.shiftLeft(mantissa, (MANTISSA_END + 1) % 64);
                //Set the newly created unset data on the right
                mantissa[mantissa.length - 1] |= data[MANTISSA_END / 64] >>> (64 - ((MANTISSA_END + 1) % 64));
            }
        }

        return mantissa;
    }

    private void setMantissaBits(long[] mantissa) {
        //Partial long at the end
        if ((MANTISSA_END + 1) % 64 != 0) {
            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {
                //Shift stuff back
                mantissa = CommonUtils.shiftLeft(mantissa, 64 - ((MANTISSA_END + 1) % 64));

                //Set data
                data[MANTISSA_END / 64] = mantissa[mantissa.length - 1];
            } else {
                //Set the newly created unset data on the right
                data[MANTISSA_END / 64] = (mantissa[mantissa.length - 1] & ~(-1L << ((MANTISSA_END + 1) % 64))) << (64 - ((MANTISSA_END + 1) % 64));

                //Shift left in order to fit the extra data
                mantissa = CommonUtils.shiftRightUnsigned(mantissa, (MANTISSA_END + 1) % 64);
            }
        }

        int pos = 0;

        if (MANTISSA_START % 64 != 0) { //Partial long at the start
            data[MANTISSA_START / 64] = (data[MANTISSA_START / 64] & (-1L << (64 - (MANTISSA_START % 64)))) |
                                        (mantissa[pos++] & ~(-1L << (64 - (MANTISSA_START % 64))));
        }

        //Full longs
        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {
            data[i] = mantissa[pos++];
        }
    }

    private void setMantissaBitsExtra(long[] mantissa) {
        //Partial long at the end
        if ((MANTISSA_END + 1) % 64 != 0) {
            if (((64 - (MANTISSA_START % 64)) + ((MANTISSA_END % 64) + 1)) > 64) {
                //Shift stuff back
                mantissa = CommonUtils.shiftLeft(mantissa, 64 - ((MANTISSA_END + 1) % 64));

                //Set data
                data[MANTISSA_END / 64] = mantissa[mantissa.length - 1];
            } else {
                //Set the newly created unset data on the right
                data[MANTISSA_END / 64] = (mantissa[mantissa.length - 1] & ~(-1L << ((MANTISSA_END + 1) % 64))) << (64 - ((MANTISSA_END + 1) % 64));

                //Shift left in order to fit the extra data
                mantissa = CommonUtils.shiftRightUnsigned(mantissa, (MANTISSA_END + 1) % 64);
            }
        }

        int pos = 0;

        if (MANTISSA_START % 64 != 0) { //Partial long at the start
            data[MANTISSA_START / 64] = (data[MANTISSA_START / 64] & (-1L << (64 - (MANTISSA_START % 64)))) |
                                        (mantissa[pos++] & ~(-1L << (64 - (MANTISSA_START % 64))));
        } else {
            pos++;
        }

        //Full longs
        for (int i = ((MANTISSA_START - 1) / 64) + 1; i < (MANTISSA_END + 1) / 64; i++) {
            data[i] = mantissa[pos++];
        }
    }

    private static void negateMantissa(long[] mantissa) {
        for (int i = 0; i < mantissa.length; i++) {
            mantissa[i] = ~mantissa[i];
        }
        mantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));

        //Simple add where the carry in is 1
        long carry = 1;

        for (int i = mantissa.length - 1; i >= 0; i--) {
            long temp = mantissa[i] + carry;

            if (carry == 1) {
                carry = (Long.compareUnsigned(temp, mantissa[i]) <= 0) ? 1 : 0;
            } else {
                carry = (Long.compareUnsigned(temp, mantissa[i]) < 0) ? 1 : 0;
            }

            mantissa[i] = temp;
        }
//        long topBit = mantissa[0] & (-1L << (64 - (MANTISSA_START % 64)));

        //Clear mantissa
        mantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));
//        return carry == 1 ? 1 : (topBit != 0 ? 1 : 0);
    }

    private static long[] negateMantissaCopy(long[] mantissa) {
        long[] newMantissa = new long[mantissa.length];
        for (int i = 0; i < mantissa.length; i++) {
            newMantissa[i] = ~mantissa[i];
        }
//        newMantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));

        //Simple add where the carry in is 1
        long carry = 1;

        for (int i = newMantissa.length - 1; i >= 0; i--) {
            long temp = newMantissa[i] + carry;

            if (carry == 1) {
                carry = (Long.compareUnsigned(temp, newMantissa[i]) <= 0) ? 1 : 0;
            } else {
                carry = (Long.compareUnsigned(temp, newMantissa[i]) < 0) ? 1 : 0;
            }

            newMantissa[i] = temp;
        }
//        long topBit = mantissa[0] & (-1L << (64 - (MANTISSA_START % 64)));

        //Clear mantissa
//        newMantissa[0] = mantissa[0] & ~(-1L << (64 - (MANTISSA_START % 64)));
//        return carry == 1 ? 1 : (topBit != 0 ? 1 : 0);
        return newMantissa;
    }

    private static int compareMantissas(long[] mantissa1, long[] mantissa2) {
        for (int i = 0; i < mantissa1.length; i++) {
            int compareUnsigned = Long.compareUnsigned(mantissa1[i], mantissa2[i]);
            if (compareUnsigned != 0) {
                return compareUnsigned;
            }
        }
        return 0;
    }

    private Type getType() {
        long exponentBits = getExponentBits();
        if (exponentBits == 0) {
            if (isMantissaZero()) {
                return Type.ZERO;
            } else {
                return Type.SUBNORMAL;
            }
        } else if (exponentBits == EXPONENT_FULL_MASK) {
            if (isMantissaZero()) {
                return Type.INFINITY;
            } else {
                return Type.NAN;
            }
        }
        return Type.NORMAL;
    }

    @Override
    public Float128 negate() {
        if (isNegative()) {
            data[0] &= Long.MAX_VALUE;
        } else {
            data[0] |= Long.MIN_VALUE;
        }
        return this;
    }

    public static Float128 negate(Float128 num) {
        return new Float128(num).negate();
    }

    @Override
    public Float128 abs() {
        if (isNegative()) {
            return negate();
        }
        return this;
    }

    public static Float128 abs(Float128 num) {
        if (num.isNegative()) {
            return Float128.negate(num);
        }
        return new Float128(num);
    }

    @Override
    public Float128 add(Float128 that) {
        Type thisType = getType();
        if (thisType == Type.NAN || thisType == Type.INFINITY) {
            return this;
        }
        if (thisType == Type.ZERO) {
            //zero plus anything is that thing
            System.arraycopy(that.data, 0, this.data, 0, LONGS);
            return this;
        }
        Type thatType = that.getType();
        if (thatType == Type.NAN || thatType == Type.INFINITY) {
            //anything plus NaN or Infinity is NaN or Infinity
            System.arraycopy(that.data, 0, this.data, 0, LONGS);
            return this;
        }
        if (thatType == Type.ZERO) {
            //Anything plus zero is same thing
            return this;
        }
        //Both this and that are either Normal or Subnormal

        long expThis = getExponentBits();
        long expThat = that.getExponentBits();
        long[] mantissaThis = this.getMantissaBits();
        long[] mantissaThat = that.getMantissaBits();
        boolean negativeCase = this.isNegative() != that.isNegative();
        boolean invertFinalResult = false;
        boolean signFlip = false;

        int compareUnsigned = Long.compareUnsigned(expThis, expThat);
        long finalExp;
        int norms = 0;

        if (compareUnsigned > 0) {
            long deltaExp = expThis - expThat;
            if (thatType == Type.SUBNORMAL) {
                deltaExp--;
            }
            if (deltaExp > MANTISSA_BITS + 1) { //Essentially adding a zero to this
                //Anything plus zero is same thing
                return this;
            }
            finalExp = expThis;
            mantissaThat = CommonUtils.shiftRightUnsigned(mantissaThat, (int) (deltaExp));
            if (thatType == Type.NORMAL) { //If it is normal we need to introduce an extra one
                long toChange = MANTISSA_BITS - deltaExp;
                if (toChange >= 0) {
                    CommonUtils.setBit(mantissaThat, (int) toChange, 1);
                }
            }
            if (thisType == Type.NORMAL) {
                norms = 1;
            }
        } else if (compareUnsigned < 0) {
            long deltaExp = expThat - expThis;
            if (thisType == Type.SUBNORMAL) {
                deltaExp--;
            }
            if (expThat - expThis > MANTISSA_BITS + 1) { //Essentially adding something to zero
                //zero plus anything is that thing
                System.arraycopy(that.data, 0, this.data, 0, LONGS);
                return this;
            }
            finalExp = expThat;
            mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, (int) (deltaExp));
            if (thisType == Type.NORMAL) { //If it is normal we need to introduce an extra one
                long toChange = MANTISSA_BITS - deltaExp;
                if (toChange >= 0) {
                    CommonUtils.setBit(mantissaThis, (int) toChange, 1);
                }
            }
            if (thatType == Type.NORMAL) {
                norms = 1;
            }
        } else {
            finalExp = expThis;
            if (thisType == Type.NORMAL) {
                norms = 1;
            }
            if (thatType == Type.NORMAL) {
                norms++;
            }
        }

        if (negativeCase) {
            int compareMantissas = compareMantissas(mantissaThis, mantissaThat);
            if (compareMantissas > 0) {
                if (this.isNegative()){
                    invertFinalResult = true;
                }
            } else if (compareMantissas < 0) {
                long[] temp = mantissaThis;
                mantissaThis = mantissaThat;
                mantissaThat = temp;
                if (that.isNegative()) {
                    invertFinalResult = true;
                }
                signFlip = true;
            }

            negateMantissa(mantissaThat);
        }

        //At this point the mantissas in mantissaThis and mantissaThat both have an exponent of finalExp
        //They can be simply added now
        //We also know how many normalized numbers we had at the start so we know how much we have to compensate
        long carry = 0;

        for (int i = mantissaThis.length - 1; i >= 0; i--) {
            long temp = mantissaThis[i] + mantissaThat[i] + carry;

            if (carry == 1) {
                carry = (Long.compareUnsigned(temp, mantissaThis[i]) <= 0 &&
                         Long.compareUnsigned(temp, mantissaThat[i]) <= 0) ? 1 : 0;
            } else {
                carry = (Long.compareUnsigned(temp, mantissaThis[i]) < 0 &&
                         Long.compareUnsigned(temp, mantissaThat[i]) < 0) ? 1 : 0;
            }

            mantissaThis[i] = temp;
        }
        long topBit = mantissaThis[0] & (-1L << (64 - (MANTISSA_START % 64)));

        //Mantissas have been added but the normal assumed ones have been ignored
        if (topBit != 0) {
            //Overflow into the top of the mantissa (shift right and change exponent to fix)
            //Clear the overflow for easier stuff later
            mantissaThis[0] = mantissaThis[0] & ~(-1L << (64 - (MANTISSA_START % 64)));
            norms++;
        } else if (carry == 1) {
            //Overflow past top of the mantissa (shift right, change exponent, and add the extra one)
            norms++;
        }

        if (negativeCase) {
            if (invertFinalResult) {
                negateMantissa(mantissaThis);
            }
            if (norms == 2) {
                long steps = 1;
                for (int i = MANTISSA_BITS - 1; i >= 0 && CommonUtils.getBit(mantissaThis, i) == 0; i--, steps++);
                if (steps < MANTISSA_BITS) {
                    if (Long.compareUnsigned(steps, finalExp) > 0) {
                        steps = finalExp;
                    }
                    mantissaThis = CommonUtils.shiftLeft(mantissaThis, (int) steps);
                    mantissaThis[0] = mantissaThis[0] & ~(-1L << (64 - (MANTISSA_START % 64)));
                    finalExp -= steps;
                } else {
                    finalExp = 0;
                }
            }
        } else {
            if (norms == 1) {
                if (finalExp == 0) {
                    finalExp = 1;
                }
            } else if (norms == 2) {
                mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, 1);
                finalExp += 1;
            } else if (norms == 3) {
                mantissaThis = CommonUtils.shiftRightUnsigned(mantissaThis, 1);
                CommonUtils.setBit(mantissaThis, MANTISSA_BITS - 1, 1);
                finalExp += 1;
            }
        }

        //At this point mantissaThis contains the summed mantissa
        //finalExp is the final exponent value
        setMantissaBits(mantissaThis);
        setExponentBits(finalExp);
        if (signFlip) {
            negate();
        }

        return this;
    }

    public boolean isFinite() {
        return getType() == Type.ZERO || getType() == Type.NORMAL || getType() == Type.SUBNORMAL;
    }
    
    public boolean isInfinite() {
        return getType() == Type.INFINITY;
    }
    
    public boolean isNaN() {
        return getType() == Type.NAN;
    }

    public static Float128 add(Float128 a,  Float128 b) {
        return new Float128(a).add(b);
    }

    @Override
    public Float128 subtract(Float128 other) {
        return add(Float128.negate(other));
    }

    @Override
    public Float128 copy() {
        return new Float128(this);
    }

    public static Float128 subtract(Float128 a,  Float128 b) {
        return Float128.add(a, Float128.negate(b));
    }

    @Override
    public Float128 multiply(Float128 that) {
        Type thisType = getType();
        if (thisType == Type.NAN || thisType == Type.INFINITY || thisType == Type.ZERO) {
            return this;
        }
        Type thatType = that.getType();
        if (thatType == Type.NAN || thatType == Type.INFINITY) {
            //anything plus NaN or Infinity is NaN or Infinity
            System.arraycopy(that.data, 0, this.data, 0, LONGS);
            return this;
        }
        if (thatType == Type.ZERO) {
            setExponentBits(0);
            setMantissaBits(ZERO_MANTISSA);
            return this;
        }
        //Both this and that are either Normal or Subnormal
        int norms = 0;

        long thisExp;
        if (thisType == Type.SUBNORMAL) {
            thisExp = 1;
        } else {
            thisExp = this.getExponentBits();
        }
        long thatExp;
        if (thatType == Type.SUBNORMAL) {
            thatExp = 1;
        } else {
            thatExp = that.getExponentBits();
        }

        long finalExp = thisExp - EXPONENT_OFFSET + thatExp;

        long[] newMantissa = new long[MANTISSA_BITS / 64 + 1];
        long[] thisMantissa = this.getMantissaBitsExtra();
        long[] thatMantissa = that.getMantissaBitsExtra();

        boolean sign = this.isNegative() ^ that.isNegative();

        int shiftAmount = 0;
        for (int i = MANTISSA_BITS; i >= 0; i--) {
            if (CommonUtils.getBit(thisMantissa, i) != 0) {
                thatMantissa = CommonUtils.shiftRightUnsigned(thatMantissa, shiftAmount);

                long carry = 0;
                for (int j = newMantissa.length - 1; j >= 0; j--) {
                    long temp = newMantissa[j] + thatMantissa[j] + carry;

                    if (carry == 1) {
                        carry = (Long.compareUnsigned(temp, newMantissa[j]) <= 0 &&
                                 Long.compareUnsigned(temp, thatMantissa[j]) <= 0) ? 1 : 0;
                    } else {
                        carry = (Long.compareUnsigned(temp, newMantissa[j]) < 0 &&
                                 Long.compareUnsigned(temp, thatMantissa[j]) < 0) ? 1 : 0;
                    }

                    newMantissa[j] = temp;
                }
                
                shiftAmount = 0;
            }
            shiftAmount++;
        }

        int firstOne = 0;
        for (int i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);
        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;
        if (shiftRight > 0) {
            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);
            finalExp += shiftRight;
        } else if (shiftRight < 0) {
            shiftRight = -shiftRight;
            if (shiftRight <= MANTISSA_BITS) {
                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {
                    shiftRight = finalExp;
                }
                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);
                finalExp -= shiftRight;
            } else {
                finalExp = 0;
            }
        }

        setMantissaBitsExtra(newMantissa);
        setExponentBits(finalExp);
        if (this.isNegative() != sign) {
            negate();
        }

        return this;
    }

    public static Float128 multiply(Float128 a,  Float128 b) {
        return new Float128(a).multiply(b);
    }

    private static long[] mantissaDivision(long[] n, long[] d) {
        long[] q = new long[n.length]; //q = 0
        long[] nn = new long[n.length]; //r = 0
        System.arraycopy(n, 0, nn, 0, n.length);

        boolean notEmpty = compareMantissas(nn, ZERO_MANTISSA_FULL) != 0;

        for (int i = MANTISSA_BITS; i >= 0 && compareMantissas(d, ZERO_MANTISSA_FULL) != 0 && notEmpty; i--) {
            if (compareMantissas(nn, d) >= 0) {
                CommonUtils.setBit(q,i, 1);
                long[] toSubtract = negateMantissaCopy(d);
                toSubtract[0] = toSubtract[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));

                long carry = 0;

                for (int j = toSubtract.length - 1; j >= 0; j--) {
                    long temp = nn[j] + toSubtract[j] + carry;

                    if (carry == 1) {
                        carry = (Long.compareUnsigned(temp, nn[j]) <= 0 &&
                                 Long.compareUnsigned(temp, toSubtract[j]) <= 0) ? 1 : 0;
                    } else {
                        carry = (Long.compareUnsigned(temp, nn[j]) < 0 &&
                                 Long.compareUnsigned(temp, toSubtract[j]) < 0) ? 1 : 0;
                    }

                    nn[j] = temp;
                }
                nn[0] = nn[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));
                notEmpty = compareMantissas(nn, ZERO_MANTISSA_FULL) != 0;
            }
            if (notEmpty) {
                d = CommonUtils.shiftRightUnsigned(d, 1);
            }
        }

        return q;
    }

    @Override
    public Float128 divide(Float128 that) {
        Type thisType = getType();
        if (thisType == Type.NAN) {
            return this;
        }
        Type thatType = that.getType();
        if (thatType == Type.NAN) {
            System.arraycopy(that.data, 0, this.data, 0, LONGS);
            return this;
        }
        if (thatType == Type.ZERO) {
            setExponentBits(EXPONENT_FULL_MASK);
            if (thisType == Type.ZERO) {
                CommonUtils.setBit(data,0, 1); //NaN
            } else {
                setMantissaBits(new long[MANTISSA_BITS/64 + 1]); //Infinity
            }
            return this;
        }
        if (thatType == Type.INFINITY) {
            if (thisType == Type.INFINITY) {
                setExponentBits(EXPONENT_FULL_MASK);
                CommonUtils.setBit(data,0, 1); //NaN
            } else {
                setExponentBits(0);
                setMantissaBits(new long[MANTISSA_BITS/64 + 1]); //Zero
            }
            return this;
        }

        long thisExp;
        if (thisType == Type.SUBNORMAL) {
            thisExp = 1;
        } else {
            thisExp = this.getExponentBits();
        }
        long thatExp;
        if (thatType == Type.SUBNORMAL) {
            thatExp = 1;
        } else {
            thatExp = that.getExponentBits();
        }

        long finalExp = thisExp + EXPONENT_OFFSET - thatExp;

        long[] thisMantissa = this.getMantissaBitsExtra();
        long[] thatMantissa = that.getMantissaBitsExtra();

        boolean sign = this.isNegative() ^ that.isNegative();

        long[] newMantissa = mantissaDivision(thisMantissa, thatMantissa);

        int firstOne = 0;
        int i;
        for (i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);
        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;
        if (shiftRight > 0) {
            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);
            finalExp += shiftRight;
        } else if (shiftRight < 0) {
            shiftRight = -shiftRight;
            if (shiftRight <= MANTISSA_BITS) {
                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {
                    shiftRight = finalExp;
                }
                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);
                finalExp -= shiftRight;
            } else {
                finalExp = 0;
            }
        }

        setMantissaBitsExtra(newMantissa);
        setExponentBits(finalExp);
        if (this.isNegative() != sign) {
            negate();
        }

        return this;
    }

    public static Float128 divide(Float128 a,  Float128 b) {
        return new Float128(a).divide(b);
    }

    private static long[] mantissaModulo(long[] n, long[] d) {
        long[] q = new long[n.length];
        long[] r = new long[n.length];
        long[] t = new long[n.length];

        boolean isFirstN = false;

        for (int i = n.length * 64 - 1; i >= 0; i--) {
            if (isFirstN || CommonUtils.getBit(n, i) != 0) {
                isFirstN = true;
                r = CommonUtils.shiftLeft(r, 1);
                CommonUtils.setBit(r, 0, CommonUtils.getBit(n, i));
                if (compareMantissas(r, d) >= 0) {
                    for (int j = 0; j < d.length; j++) {
                        t[j] = ~d[j];
                    }

                    long carry = 1;

                    for (int j = n.length - 1; j >= 0; j--) {
                        long temp = r[j] + t[j] + carry;

                        if (carry == 1) {
                            carry = (Long.compareUnsigned(temp, r[j]) <= 0 &&
                                     Long.compareUnsigned(temp, t[j]) <= 0) ? 1 : 0;
                        } else {
                            carry = (Long.compareUnsigned(temp, r[j]) < 0 &&
                                     Long.compareUnsigned(temp, t[j]) < 0) ? 1 : 0;
                        }

                        r[j] = temp;
                    }
                    r[0] = r[0] & ~(-1L << (64 - ((MANTISSA_START - 1) % 64)));

                    CommonUtils.setBit(q, i, 1);
                }
            }
        }

        return r;
    }

    @Override
    public Float128 modulo(Float128 that) {
        Type thisType = getType();
        if (thisType == Type.NAN) {
            return this;
        }
        Type thatType = that.getType();
        if (thatType == Type.NAN) {
            System.arraycopy(that.data, 0, this.data, 0, LONGS);
            return this;
        }
        if (thatType == Type.ZERO || thisType == Type.INFINITY) {
            setExponentBits(EXPONENT_FULL_MASK);
            CommonUtils.setBit(data,0, 1); //NaN
            return this;
        }
        if (thatType == Type.INFINITY) {
            return this;
        }

        long thisExp;
        if (thisType == Type.SUBNORMAL) {
            thisExp = 1;
        } else {
            thisExp = this.getExponentBits();
        }
        long thatExp;
        if (thatType == Type.SUBNORMAL) {
            thatExp = 1;
        } else {
            thatExp = that.getExponentBits();
        }

        long finalExp = thisExp;

        long[] thisMantissa = this.getMantissaBitsExtra();
        long[] thatMantissa = that.getMantissaBitsExtra();
        if (thisExp > thatExp) {
            if (thisExp - thatExp > MANTISSA_BITS + 1) {
                Arrays.fill(thisMantissa, 0);
            } else {
                thatMantissa = CommonUtils.shiftRightUnsigned(thatMantissa, (int) (thisExp - thatExp));
            }
        }
        if (thisExp < thatExp) {
            finalExp = thatExp;
            if (thatExp - thisExp > MANTISSA_BITS + 1) {
                Arrays.fill(thatMantissa, 0);
            } else {
                thisMantissa = CommonUtils.shiftRightUnsigned(thisMantissa, (int) (thatExp - thisExp));
            }
        }

        boolean sign = this.isNegative();

        long[] newMantissa = mantissaModulo(thisMantissa, thatMantissa);

        int firstOne = 0;
        int i;
        for (i = newMantissa.length * 64 - 1; i >= 0 && CommonUtils.getBit(newMantissa, i) == 0; i--, firstOne++);
        long shiftRight = (newMantissa.length * 64) - MANTISSA_BITS - firstOne - 1;
        if (shiftRight > 0) {
            newMantissa = CommonUtils.shiftRightUnsigned(newMantissa, (int) shiftRight);
            finalExp += shiftRight;
        } else if (shiftRight < 0) {
            shiftRight = -shiftRight;
            if (shiftRight <= MANTISSA_BITS) {
                if (Long.compareUnsigned(shiftRight, finalExp) > 0) {
                    shiftRight = finalExp;
                }
                newMantissa = CommonUtils.shiftLeft(newMantissa, (int) shiftRight);
                finalExp -= shiftRight;
            } else {
                finalExp = 0;
            }
        }

        setMantissaBitsExtra(newMantissa);
        setExponentBits(finalExp);
        if (this.isNegative() != sign) {
            negate();
        }

        return this;
    }

    public static Float128 modulo(Float128 a, Float128 b) {
        return new Float128(a).modulo(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Float128 float128 = (Float128) o;
        if (float128.getType() == Type.NAN || this.getType() == Type.NAN || float128.getType() == Type.INFINITY || this.getType() == Type.INFINITY) {
            return false;
        }
        if (float128.getType() == Type.ZERO && this.getType() == Type.ZERO) {
            return true;
        }
        return Arrays.equals(data, float128.data);
    }

    @Override
    public int hashCode() {
        if (this.getType() == Type.ZERO) {
            return Type.ZERO.hashCode(); // Since ZEROs are always equal: they all share the same hashcode
        }
        return Arrays.hashCode(data);
    }


    @Override
    public int compareTo(Float128 o) {
        Float128 a = this;
        Float128 b = o;
        Type aType = a.getType();
        Type bType = b.getType();
        if (aType == bType) {
            if (aType == Type.ZERO) {
                return 0;
            } else if (aType == Type.INFINITY) {
                return a.isNegative() ? (b.isNegative() ? 0 : -1) : (b.isNegative() ? 1 : 0);
            } else if (aType == Type.NAN) {
                return 0;
            } else {
                return a.isNegative() ?
                        (b.isNegative() ? NEGATIVE_COMPARATOR.compare(a, b) : -1) :
                        (b.isNegative() ? 1 : POSITIVE_COMPARATOR.compare(a, b));
            }
        } else {
            if (bType == Type.NAN) {
                return 0;
            }
            // Types are different
            if (aType == Type.ZERO) {
                return b.isNegative() ? 1 : -1;
            } else if (aType == Type.INFINITY) {
                return a.isNegative() ? -1 : 1;
            } else if (aType == Type.NAN) {
                return 0;
            } else if (bType == Type.INFINITY) {
                return b.isNegative() ? 1 : -1;
            } else { // SUBNORMAL | NORMAL
                if (bType == Type.ZERO) {
                    return a.isNegative() ? -1 : 1;
                } else { // SUBNORMAL | NORMAL
                    return a.isNegative() ?
                            (b.isNegative() ? NEGATIVE_COMPARATOR.compare(a, b) : -1) :
                            (b.isNegative() ? 1 : POSITIVE_COMPARATOR.compare(a, b));
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        String negative = "";
        if (isNegative()) {
            negative = "-";
        }

        Type type = getType();

        if (type == Type.ZERO) {
            builder.append('0');
        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {
            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);
            long base10 = (long) Math.floor(base10s);
            if (base10 <= 7 && base10 >= -3) {
                StringBuilder fullString = new StringBuilder(toFullString(10));
                while (fullString.charAt(fullString.length() - 1) == '0') {
                    fullString.deleteCharAt(fullString.length() - 1);
                }
                if (fullString.charAt(fullString.length() - 1) == '.') {
                    fullString.deleteCharAt(fullString.length() - 1);
                }
                return fullString.toString();
            } else {
                return toSciString(10);
            }
        } else  if (type == Type.INFINITY) {
            builder.append(negative);
            builder.append("INFINITY");
        } else if (type == Type.NAN) {
            builder.append("NAN");
        }

        return builder.toString();
    }

    public String toSciString(int precision) {
        StringBuilder builder = new StringBuilder();

        String negative = "";
        if (isNegative()) {
            negative = "-";
        }

        Type type = getType();

        if (type == Type.ZERO) {
            builder.append('0');
        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {
            builder.append(negative);
            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);
            long base10 = (long) Math.floor(base10s);
            long exponent = getExponentBits() - EXPONENT_OFFSET;
            ManRes mantissa = getMantissa(type == Type.NORMAL, exponent, precision);
            // Remove trailing whitespace
            while (mantissa.result.length() > 0 && mantissa.result.charAt(mantissa.result.length() - 1) == '0') {
                mantissa.result.deleteCharAt(mantissa.result.length() - 1);
            }
            // Remove starting whitespace
            while (mantissa.result.length() > 0 && mantissa.result.charAt(0) == '0') {
                mantissa.result.deleteCharAt(0);
                base10--;
            }
            if (precision > 0) {
                if (mantissa.result.length() > precision) {
                    // round to precision
                    char c = mantissa.result.charAt(precision);
                    mantissa.result.delete(precision, mantissa.result.length());
                    if (c >= '5') { // round up
                        String newMantissa = CommonUtils.addStrings(mantissa.result.toString(), "1", 0);
                        mantissa.result.delete(0, mantissa.result.length());
                        mantissa.result.append(newMantissa);
                        mantissa.result.delete(precision, mantissa.result.length());
                    }
                }
                // Increase to precision
                while (precision > mantissa.result.length()) {
                    mantissa.result.append('0');
                }
            }
            mantissa.result.insert(1, '.');
            builder.append(mantissa.result);
            builder.append("E");
            builder.append(base10 + mantissa.baseOffset);
        } else  if (type == Type.INFINITY) {
            builder.append(negative);
            builder.append("INFINITY");
        } else if (type == Type.NAN) {
            builder.append("NAN");
        }

        return builder.toString();
    }

    public String toFullString(int precision) {
        StringBuilder builder = new StringBuilder();

        String negative = "";
        if (isNegative()) {
            negative = "-";
        }

        Type type = getType();

        if (type == Type.ZERO) {
            builder.append('0');
            while (precision > builder.length()) {
                builder.append('0');
            }
            if (builder.length() > 1) {
                builder.insert(1, '.');
            }
        } else if (type == Type.SUBNORMAL || type == Type.NORMAL) {
            builder.append(negative);
            double base10s = base2To10Exp(getExponentBits() - EXPONENT_OFFSET);
            long base10 = (long) Math.floor(base10s);
            long exponent = getExponentBits() - EXPONENT_OFFSET;
            ManRes mantissa = getMantissa(type == Type.NORMAL, exponent, precision);
            base10 += mantissa.baseOffset;
            while (base10 < 0) {
                mantissa.result.insert(0, '0');
                base10++;
            }
            base10++;
            if (precision > 0) {
                if (mantissa.result.length() > precision) {
                    // round to precision
                    char c = mantissa.result.charAt(precision);
                    mantissa.result.delete(precision, mantissa.result.length());
                    if (c >= '5') { // round up
                        String newMantissa = CommonUtils.addStrings(mantissa.result.toString(), "1", 0);
                        mantissa.result.delete(0, mantissa.result.length());
                        mantissa.result.append(newMantissa);
                        mantissa.result.delete(precision, mantissa.result.length());
                    }
                }
                // Increase to precision
                while (precision > mantissa.result.length()) {
                    mantissa.result.append('0');
                }
            }
            if (mantissa.result.length() > base10) {
                mantissa.result.insert((int) base10, '.');
            }
            builder.append(mantissa.result);
        } else  if (type == Type.INFINITY) {
            builder.append(negative);
            builder.append("INFINITY");
        } else if (type == Type.NAN) {
            builder.append("NAN");
        }

        return builder.toString();
    }

    private ManRes getMantissa(boolean isNormal, long exponent, int precision) {
        if (!isNormal) {
            exponent = 1 - EXPONENT_OFFSET;
        }
        ExpString scaleString = CommonUtils.getExponentString(exponent, null, 0);
        long[] mantissaBits = getMantissaBits();
        StringBuilder result = new StringBuilder();
        // The index of the location of the 10^0s place in the string
        int resultZero = scaleString.zeroIndex;
        if (isNormal) {
            result.append(scaleString.string);
        } else {
            result.append('0');
        }

        ExpString lastMemo = scaleString;
        long lastExp = exponent;

        int additionalBase = 0;
        for (int i = 0; i < MANTISSA_BITS; i++) {
            exponent--;

            int mantissaIndex = MANTISSA_START + i;
            if ((mantissaBits[mantissaIndex / 64] & (1L << (63 - (mantissaIndex % 64)))) != 0) {
                ExpString nowScale = CommonUtils.getExponentString(exponent, lastMemo, lastExp);
                int stringZero = nowScale.zeroIndex;
                lastMemo = nowScale;
                lastExp = exponent;

                if (result.length() == 1 && result.charAt(0) == '0') {
                    additionalBase += stringZero - resultZero;
                    resultZero = stringZero;
                }

                if (precision > 0 && resultZero - stringZero > precision + 1) {
                    break;
                }

                char[] chars = nowScale.string.toCharArray();

                int partOffset = resultZero - stringZero;

                // Ensure proper length
                for (int j = result.length(); j < chars.length + partOffset; j++) {
                    result.append('0');
                }
                //Sum strings
                int carry = 0;
                for (int j = chars.length - 1; (j >= 0 || carry != 0) && (j + partOffset >= 0); j--) {
                    int toAdd = (j >= 0) ? (chars[j] - '0') : 0;
                    int current = result.charAt(j + partOffset) - '0';
                    int here = toAdd + current + carry;
                    carry = here >= 10 ? 1 : 0;
                    result.setCharAt(j + partOffset, (char) ((here % 10) + '0'));
                }
                if (carry == 1) {
                    resultZero++;
                    additionalBase++;
                    result.insert(0, '1');
                }
            }
        }
        return new ManRes(additionalBase, result);
    }

    public String toBase64() {
        return toBase64(Base64.getEncoder());
    }

    public String toBase64(Encoder encoder) {
        byte[] bytes = new byte[8*LONGS];

        for (int i = 0; i < LONGS; i++) {
            bytes[i * 8] = (byte) (data[i] >>> 56);
            bytes[i * 8 + 1] = (byte) (data[i] >>> 48);
            bytes[i * 8 + 2] = (byte) (data[i] >>> 40);
            bytes[i * 8 + 3] = (byte) (data[i] >>> 32);
            bytes[i * 8 + 4] = (byte) (data[i] >>> 24);
            bytes[i * 8 + 5] = (byte) (data[i] >>> 16);
            bytes[i * 8 + 6] = (byte) (data[i] >>> 8);
            bytes[i * 8 + 7] = (byte) (data[i]);
        }
        return encoder.encodeToString(bytes);
    }

    public static Float128 fromBase64(String base64) {
        return fromBase64(base64, Base64.getDecoder());
    }

    public static Float128 fromBase64(String base64, Decoder decoder) {
        byte[] bytes = decoder.decode(base64);

        Float128 result = new Float128();

        for (int i = 0; i < LONGS; i++) {
            result.data[i] = (((long)bytes[i * 8]) & 0xFF) << 56 |
                             (((long)bytes[i * 8 + 1]) & 0xFF) << 48 |
                             (((long)bytes[i * 8 + 2]) & 0xFF) << 40 |
                             (((long)bytes[i * 8 + 3]) & 0xFF) << 32 |
                             (((long)bytes[i * 8 + 4]) & 0xFF) << 24 |
                             (((long)bytes[i * 8 + 5]) & 0xFF) << 16 |
                             (((long)bytes[i * 8 + 6]) & 0xFF) << 8 |
                             (((long)bytes[i * 8 + 7]) & 0xFF);
        }

        return result;
    }

    private static double base2To10Exp(long base2Exponent) {
        return base2Exponent * LOG_10_OF_2;
    }

    private enum Type {
        ZERO, SUBNORMAL, NORMAL, INFINITY, NAN
    }

    private class ManRes {
        int baseOffset;
        StringBuilder result;

        ManRes(int baseOffset, StringBuilder result) {
            this.baseOffset = baseOffset;
            this.result = result;
        }
    }
}
