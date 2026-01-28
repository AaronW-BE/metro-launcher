package org.dpdns.argv.metrolauncher.util;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

public class LauncherHelper {

    public static boolean isDefaultLauncher(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // Should not happen. Does exist a home app?
            return false; 
        } else if ("android".equals(res.activityInfo.packageName)) {
            // No default selected
            return false;
        } else {
             return context.getPackageName().equals(res.activityInfo.packageName);
        }
    }

    public static void requestSetDefaultLauncher(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                    activity.startActivityForResult(intent, 1234);
                }
            } else {
               // Fallback
               openSettings(activity);
            }
        } else {
            openSettings(activity);
        }
    }
    
    // For manual navigation or fallback
    public static void openSettings(Context context) {
         Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
         try {
             context.startActivity(intent);
         } catch (Exception e) {
             Intent intent2 = new Intent(Settings.ACTION_SETTINGS);
             context.startActivity(intent2);
         }
    }
}
