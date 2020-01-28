package com.sergeysav.bignum;

/**
 * @author sergeys
 */
public class Timing {

    private static final Int2048 added = Int2048.from(13);
    private static final int     TIMES = 1_000_000_000;

    public static void main(String[] args) {
        Int2048 num = new Int2048(Int2048.ONE);
        long time128 = 0;
        {
            long before = System.nanoTime();
            for (int i = 0; i < TIMES; i++) {
                num.add(added);
                //                num.add(com.sergeysav.bignum.Int2048.ONE);
            }
            long after = System.nanoTime();
            time128 += (after - before);
        }
        long n = 1;
        long timelong = 0;
        {
            long before = System.nanoTime();
            for (int i = 0; i < TIMES; i++) {
                n += 13;
                //                n += 1;
            }
            long after = System.nanoTime();
            timelong += (after - before);
        }
        System.out.println("Int:  " + ((time128 / (double) TIMES) * 1e-6));
        System.out.println("Long: " + ((timelong / (double) TIMES) * 1e-6));
        System.out.println("Div:  " + ((time128 / (double) timelong)));
    }
}
