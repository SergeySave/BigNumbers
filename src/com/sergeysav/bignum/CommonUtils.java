package com.sergeysav.bignum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sergeys
 */
public class CommonUtils {

    public static long[] shiftLeft(long[] original, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }
        if (bits == 0) {
            return original;
        }
        long[] result = new long[original.length];
        int longs = bits / 64;
        int singleBits = bits % 64;

        long mask = ~(-1L << singleBits);

        for (int i = original.length - 1; i >= 0; i--) {
            if (i + longs >= original.length) {
                result[i] = 0;
            } else if (i + longs + 1 >= original.length) {
                result[i] = (original[i + longs] << singleBits);
            } else {
                result[i] = ((original[i + longs] << singleBits) |
                             ((original[i + longs + 1] >> (64 - singleBits)) & mask));
            }
        }

        return result;
    }

    public static long[] shiftRightUnsigned(long[] original, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }
        if (bits == 0) {
            return original;
        }

        long[] result = new long[original.length];
        int longs = bits / 64;
        int singleBits = bits % 64;

        for (int i = original.length - 1; i >= 0; i--) {
            if (i - longs < 0) {
                result[i] = 0;
            } else if (i - longs < 1) { //i - bytes == 0
                result[i] = (original[i - longs] >>> singleBits);
            } else {
                result[i] = ((original[i - longs] >>> singleBits) |
                             (original[i - longs - 1] << (64 - singleBits)));
            }
        }

        return result;
    }

    public static long[] shiftRightSigned(long[] original, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
        }
        if (bits == 0) {
            return original;
        }

        long[] result = new long[original.length];
        int longs = bits / 64;
        int singleBits = bits % 64;

        long signLong = original[0] < 0 ? -1L : 0;

        for (int i = original.length - 1; i >= 0; i--) {
            if (i - longs < 0) {
                result[i] = signLong;
            } else if (i - longs < 1) { //i - bytes == 0
                result[i] = ((original[i - longs] >>> singleBits) |
                             (signLong << (64 - singleBits)));
            } else {
                result[i] = ((original[i - longs] >>> singleBits) |
                             (original[i - longs - 1] << (64 - singleBits)));
            }
        }

        return result;
    }

    //Big Endian
    public static void setBit(long[] longs, int bit, int val) {
        if (val == 0) {
            longs[longs.length - 1 - (bit / 64)] &= ~(1L << (bit % 64));
        } else {
            longs[longs.length - 1 - (bit / 64)] |= (1L << (bit % 64));
        }
    }

    //Big Endian
    public static int getBit(long[] longs, int bit) {
        return (longs[longs.length - 1 - (bit / 64)] & (1L << (bit % 64))) != 0 ? 1 : 0;
    }

    public static String doubleToSubplaceString(double input) {
        String original = Double.toString(input).replace(".", "");
        String[] parts = original.split("E");
        StringBuilder result = new StringBuilder(parts[0]);
        if (parts.length > 1) { // Only negative exponents supported
            int exp = Integer.parseInt(parts[1]);
            for (int i = exp; i < 0; i++) {
                result.insert(0, '0');
            }
        }
        while (result.charAt(result.length() - 1) == '0') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static final long EXPONENT_FREQ = 1000;
    private static final Map<Long, ExpString> expStringCache = new HashMap<>();
    /**
     * Get a string representation of 2^exponent
     *
     * WARNING: SLOW METHOD - Computes using string manipulation
     *
     * This method caches results to speed up future computations
     * Pass a previous result to further speed up halving computations.
     *
     * @param exponent the exponent to get the representation of
     * @param lastMemo the last returned value or null
     * @param lastExp the exponent of the last returned value (if lastMemo is null this is ignored)
     * @return the exponent string and its root index
     */
    public static ExpString getExponentString(long exponent, ExpString lastMemo, long lastExp) {
        if (expStringCache.containsKey(exponent)) {
            return expStringCache.get(exponent);
        }
        if (lastMemo != null && exponent == lastExp) {
            return lastMemo;
        }
        StringBuilder result = new StringBuilder("1");
        int rootIndex = 0;

        if (exponent > 0) {
            // Find closest memoized exponent
            long start = exponent;
            while (--start > 0) {
                if (expStringCache.containsKey(start) || (lastMemo != null && start == lastExp)) {
                    ExpString last = expStringCache.getOrDefault(start, lastMemo);
                    result = new StringBuilder(last.string);
                    rootIndex = last.zeroIndex;
                    break;
                }
            }

            while (start < exponent) {
                // Double the value of the string
                int carry = 0;
                for (int i = result.length() - 1; i >= 0; i--) {
                    int val = result.charAt(i) - '0';
                    int newVal = val * 2 + carry;
                    carry = newVal >= 10 ? 1 : 0;
                    result.setCharAt(i, (char) ((newVal % 10) + '0'));
                }
                if (carry != 0) {
                    result.insert(0, carry);
                    rootIndex = result.length();
                }
                start++;

                if (start % EXPONENT_FREQ == 0) {
                    expStringCache.put(start, new ExpString(rootIndex, result.toString()));
                }
            }
        } else if (exponent < 0) {
            long start = exponent;
            while (++start < 0) {
                if (expStringCache.containsKey(start) || (lastMemo != null && start == lastExp)) {
                    ExpString last = expStringCache.getOrDefault(start, lastMemo);
                    result = new StringBuilder(last.string);
                    rootIndex = last.zeroIndex;
                    break;
                }
            }

            while (start > exponent) {
                int carry = 0;
                for (int i = 0; i < result.length(); i++) {
                    int val = result.charAt(i) - '0' + carry;
                    int newVal = val / 2;
                    carry = val % 2 == 0 ? 0 : 10;
                    result.setCharAt(i, (char) (newVal + '0'));
                }
                if (carry != 0) {
                    result.append(carry / 2);
                }
                while (result.charAt(0) == '0') {
                    result.deleteCharAt(0);
                    rootIndex--; // Allows for negative root indicies
                }
                start--;

                if (start % EXPONENT_FREQ == 0) {
                    expStringCache.put(start, new ExpString(rootIndex, result.toString()));
                }
            }
        }

        return new ExpString(rootIndex, result.toString());
    }

    public static String addStrings(String a, String b, int carry) {
        StringBuilder result = new StringBuilder();
        for (int j = 0; j < b.length() || j < a.length(); j++) {
            int toAdd = (j < b.length()) ? (b.charAt(b.length() - j - 1) - '0') : 0;
            int current = (j < a.length()) ? (a.charAt(a.length() - j - 1) - '0') : 0;
            int here = toAdd + current + carry;
            carry = here >= 10 ? 1 : 0;
            result.append((char) ((here % 10) + '0'));
        }
        if (carry != 0) {
            result.append(carry);
        }
        return result.reverse().toString();
    }

    public static long pow(long base, long exp) {
        long y = 1;
        while (exp > 1) {
            if (exp % 2 != 0) {
                y *= base;
            }
            exp /= 2;
            base *= base;
        }
        return base * y;
    }

    static class ExpString {
        final int zeroIndex;
        final String string;

        private ExpString(int zeroIndex, String string) {
            this.zeroIndex = zeroIndex;
            this.string = string;
        }
    }
}
