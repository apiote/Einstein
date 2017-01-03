package ml.adamsprogs.einstein.mobile.utilities;

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


}
