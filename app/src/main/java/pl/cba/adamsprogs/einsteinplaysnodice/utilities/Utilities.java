package pl.cba.adamsprogs.einsteinplaysnodice.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.Random;

public class Utilities {
    public static boolean isRunningMarshmallowOrNewer() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isRunningLollipopOrNewer() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static int getColour(Context context, int id) {
        if (isRunningMarshmallowOrNewer())
            return context.getResources().getColor(id, null);
        else
            //noinspection deprecation
            return context.getResources().getColor(id);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getDrawable(Context context, int id) {
        if (isRunningLollipopOrNewer())
            return context.getResources().getDrawable(id, null);
        else
            //noinspection deprecation
            return context.getResources().getDrawable(id);
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

    public static int opponent(int id) {
        return 1 - id;
    }
}
