package com.hopearena.autoreading.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import com.hopearena.autoreading.R;

/**
 * Created by Administrator on 2016/12/16.
 */

public class PermissionUtil {

    public static final int PERMISSION_REQUEST_CODE_RECORD_AUDIO = 0;

    public static boolean requestPermission(Activity activity, String permission, int requestCode) {
        if (!isGranted(activity, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Snackbar.make(activity.getCurrentFocus(), "您没有授权该权限，请在设置中打开授权", Snackbar.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
            return false;
        } else {
            return true;
        }
    }

    private static boolean isGranted(Activity activity, String permission) {
        return !isMarshmallow() || isGranted_(activity, permission);
    }

    private static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private static boolean isGranted_(Activity activity, String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
