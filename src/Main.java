import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author sergeys
 */
public class Main {

    public static void main(String[] args) {
        //        System.out.println("byte");
        //        System.out.println("Min: " + Byte.MIN_VALUE);
        //        System.out.println("Max: " + Byte.MAX_VALUE);
        //        System.out.println();
        //
        //        System.out.println("short");
        //        System.out.println("Min: " + Short.MIN_VALUE);
        //        System.out.println("Max: " + Short.MAX_VALUE);
        //        System.out.println();
        //
        //        System.out.println("int");
        //        System.out.println("Min: " + Integer.MIN_VALUE);
        //        System.out.println("Max: " + Integer.MAX_VALUE);
        //        System.out.println();

        System.out.println("long");
        System.out.printf("Min: %,d\n", Long.MIN_VALUE);
        System.out.printf("Max:  %,d\n", Long.MAX_VALUE);
        System.out.println();

        System.out.println("Int128");
        System.out.println("Min: " + Int128.bytesOf(Long.MIN_VALUE, 0).toStringCommas());
        System.out.println("Max:  " + Int128.bytesOf(Long.MAX_VALUE, -1L).toStringCommas());
        System.out.println();

        System.out.println("Int256");
        System.out.println("Min: " + Int256.bytesOf(Long.MIN_VALUE, 0, 0, 0).toStringCommas());
        System.out.println("Max:  " + Int256.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L).toStringCommas());
        System.out.println();

        System.out.println("Int512");
        System.out.println("Min: " + Int512.bytesOf(Long.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0).toStringCommas());
        System.out.println(
                "Max:  " + Int512.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L, -1L, -1L, -1L, -1L).toStringCommas());
        System.out.println();

        System.out.println("Int1024");
        System.out.println("Min: " + Int1024.bytesOf(Long.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                .toStringCommas());
        System.out.println("Max:  " +
                           Int1024.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
                                           -1L, -1L, -1L).toStringCommas());
        System.out.println();

        System.out.println("Int2048");
        System.out.println("Min: " +
                           Int2048.bytesOf(Long.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).toStringCommas());
        System.out.println("Max:  " +
                           Int2048.bytesOf(Long.MAX_VALUE, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
                                           -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
                                           -1L, -1L, -1L, -1L).toStringCommas());
        System.out.println();

        try {
            int bits = 1024;
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("Int" + bits + ".java")));
            writer.write(new IntXGenerator(bits).generateClass());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
