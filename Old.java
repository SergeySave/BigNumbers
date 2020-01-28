import java.util.Comparator;

/**
 * @author sergeys
 */
public class Old {

    public static final Comparator<Old> EXPONENT_COMPARATOR = ExponentComparator.INSTANCE;

    private static final boolean isSigned = true;
    private static final int exponentBits = 23;
    private static final int exponentEnd = 24;
    private static final int mantissaBits = 104;
    private static final int mantissaEnd = 128;

    private byte[] data;

    public Old(Old source) {
        this.data = new byte[source.data.length];

        System.arraycopy(source.data, 0, this.data, 0, data.length);
    }

    //data read as bytes
    public Old(int... data) {
        this.data = new byte[16];

        if (data.length < this.data.length) {
            throw new IllegalArgumentException("Not enough data provided to create the BigFloatingPoint");
        }

        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = (byte)(data[i] & 0xff);
        }
    }

    public static Old abs(Old fp) {
        Old floatingPoint = (new Old(fp));

        floatingPoint.abs();

        return floatingPoint;
    }

    public void abs() {
        if (isSigned) {
            data[0] |= 0x0b1000_000;
        }
    }

    public static Old negate(Old fp) {
        Old floatingPoint = (new Old(fp));

        floatingPoint.negate();

        return floatingPoint;
    }

    public void negate() {
        data[0] ^= 0x0b1000_000;
    }

    public void add(Old other) {
        byte[] thisData = new byte[this.data.length];
        byte[] otherData = new byte[other.data.length];

        System.arraycopy(data, 0, thisData, 0, data.length);
        System.arraycopy(other.data, 0, otherData, 0, other.data.length);
    }

    private static byte getBit(int bit, byte[] data) {
        return (byte)(data[bit / 8] & (1 << (7 - (bit % 8))));
    }

    private byte getBit(int bit) {
        return (byte)(data[bit / 8] & (1 << (7 - (bit % 8))));
    }

    private BitRunState getRunState(int start, int end) {
        BitRunState state = null;

        for (; start < end; start++) {
            int bit = getBit(start);
            if (state == null) {
                state = (bit == 0 ? BitRunState.ALL_ZERO : BitRunState.ALL_ONE);
            } else {
                if (bit == 0 && state == BitRunState.ALL_ONE) {
                    return BitRunState.MIXED;
                } else if (bit != 0 && state == BitRunState.ALL_ZERO) {
                    return BitRunState.MIXED;
                }
            }
        }

        return state;
    }

    private BitRunState getExponentState() {
        return getRunState(1, exponentEnd);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        boolean isNegative = isSigned && getBit(0) != 0;

        BitRunState state = getExponentState();

        switch (state) {
            case ALL_ONE:
                BitRunState significandState = getRunState(exponentEnd, mantissaEnd);
                if (significandState == BitRunState.ALL_ZERO) {
                    if (isNegative) {
                        return "-Infinity";
                    }
                    return "Infinity";
                } else {
                    return "NaN";
                }
            case MIXED: {
                if (isNegative) {
                    stringBuilder.append('-');
                }

                double d          = 1;
                double placeValue = 1;
                for (int i = exponentEnd; i < mantissaEnd; i++) {
                    placeValue /= 2;
                    if (getBit(i) != 0) {
                        double newD = d + placeValue;
                        if (newD == d) {
                            break;
                        }
                        d = newD;
                    }
                }

                long e  = 0;
                long pV = 1;
                for (int i = exponentEnd - 1; i > (isSigned ? 1 : 0); i--) {
                    if (getBit(i) == 0) {
                        e -= pV;
                    }
                    pV <<= 1;
                }
                if (getBit(isSigned ? 1 : 0) != 0) {
                    e += pV;
                }

                long b10Exp = (long) (e * Math.log10(2));
                double mult = Math.exp((e-b10Exp)*Math.log(2)- b10Exp*Math.log(5));

                d *= mult;

                if (d < 1) {
                    d *= 10;
                    b10Exp-=1;
                }

                stringBuilder.append(d);
                if (b10Exp != 0) {
                    stringBuilder.append('e');
                    stringBuilder.append(b10Exp);
                }
            }
            break;
            case ALL_ZERO: {
                if (isNegative) {
                    stringBuilder.append('-');
                }

                double d          = 0;
                double placeValue = 1;
                for (int i = exponentEnd; i < mantissaEnd; i++) {
                    placeValue /= 2;
                    if (getBit(i) != 0) {
                        double newD = d + placeValue;
                        if (newD == d) {
                            break;
                        }
                        d = newD;
                    }
                }

                if (d == 0) {
                    stringBuilder.append('0');
                    return stringBuilder.toString();
                }

                long e  = 2 - (1 << (exponentBits-1));

                long b10Exp = (long) (e * Math.log10(2));
                double mult = Math.exp((e-b10Exp)*Math.log(2)- b10Exp*Math.log(5));

                stringBuilder.append(d*mult);
                if (b10Exp != 0) {
                    stringBuilder.append('e');
                    stringBuilder.append(b10Exp);
                }
            }
            break;
        }

        return stringBuilder.toString();
    }

    private static class ExponentComparator implements Comparator<Old> {

        public static final ExponentComparator INSTANCE = new ExponentComparator();

        @Override
        public int compare(Old o1, Old o2) {
            int index1 = 1;
            int index2 = 1;

            if (o1.getBit(index1) != 0 && o2.getBit(index2) == 0) {
                return 1;
            } else if (o1.getBit(index1) == 0 && o2.getBit(index2) != 0) {
                return -1;
            }

            boolean invertO1 = o1.getBit(index1) != 0;

            index1++;
            index2++;

            for (; index1 < exponentEnd; index1++, index2++) {
                if (o1.getBit(index1) == 0 && o2.getBit(index2) != 0) {
                    return -1;
                } else if (o1.getBit(index1) != 0 & o2.getBit(index2) == 0) {
                    return 1;
                }
            }

            return 0;
        }

        public int compare(byte[] d1, byte[] d2) {
            int index1 = 1;
            int index2 = 1;

            if (getBit(index1, d1) != 0 && getBit(index2, d2) == 0) {
                return 1;
            } else if (getBit(index1, d1) == 0 && getBit(index2, d2) != 0) {
                return -1;
            }

            boolean invertO1 = getBit(index1, d1) != 0;

            index1++;
            index2++;

            for (; index1 < exponentEnd; index1++, index2++) {
                if (getBit(index1, d1) == 0 && getBit(index2, d2) != 0) {
                    return -1;
                } else if (getBit(index1, d1) != 0 & getBit(index2, d2) == 0) {
                    return 1;
                }
            }

            return 0;
        }
    }
}
