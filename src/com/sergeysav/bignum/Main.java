package com.sergeysav.bignum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author sergeys
 */
public class Main {

    public static void main(String[] args) {

        System.out.println(Float128.MIN_VALUE);
        System.out.println(Float128.MAX_SUBNORMAL);
        System.out.println(Float128.MIN_NORMAL);
        System.out.println(Float128.ZERO);
        System.out.println(Float128.from(0.3123));
        System.out.println(Float128.from(1f/3).toFullString(-1));
        System.out.println(Float128.from(1.0/3).toFullString(-1));
        System.out.println(Float128.ONE.copy().divide(Float128.from(3)).toFullString(-1));
        String oneThird = Float128.ONE.copy().divide(Float128.from(3)).toBase64();
        System.out.println(oneThird);
        System.out.println(Float128.fromBase64(oneThird).toFullString(-1));
        System.out.println(Float256.ONE.copy().divide(Float256.from(3)).toFullString(-1));
        System.out.println(Float128.ONE);
        System.out.println(Float128.from(1.5));
        System.out.println(Float128.TEN);
        System.out.println(Float128.MAX_VALUE);

//        System.out.println();

//        System.out.println(Float128.MIN_VALUE.copy().add(Float128.MAX_SUBNORMAL));

//        System.out.println();

//        System.out.println(Float128.MIN_NORMAL);


        if (false) {
            try {
                int bits = 2048;
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Int" + bits + ".java")));
                writer.write(new IntXGenerator(bits).generateClass());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (true) {
            try {
                int bits = 256;

                // 128 -> 15, 256 -> 19
                int exp = 19;
                int mant = bits - 1 - exp;
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Float" + (exp + mant + 1) + ".java")));
                writer.write(new FloatXGenerator(1, exp, mant).generateClass());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
