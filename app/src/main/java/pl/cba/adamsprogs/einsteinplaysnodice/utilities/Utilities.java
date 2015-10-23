package pl.cba.adamsprogs.einsteinplaysnodice.utilities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

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
}
