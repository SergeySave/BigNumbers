/**
 * @author sergeys
 */
public class CommonUtils {

    public static long[] shiftLeft(long[] original, int bits) {
        if (bits < 0) {
            throw new IllegalArgumentException("Cannot shift by negative amount");
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
}
