package ml.adamsprogs.einstein.engine.utils;

import java.util.Random;

public class Utils {
    public static int opponent(int id) {
        return 1 - id;
    }

    public static void shuffleArray(int[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static String positionToString(Point p) {
        return p.x + "_" + p.y;
    }

    public static Point pointToString(String s) {
        String[] split = s.split("_");
        return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}
