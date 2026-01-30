package org.dpdns.argv.metrolauncher.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.dpdns.argv.metrolauncher.UsageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultTilesProvider {

    public static List<PinnedTile> getDefaults(Context context) {
        List<PinnedTile> results = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // 0. Phone / Dialer (Priority) - Size 2x2
        String defaultDialer = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.telecom.TelecomManager tm = (android.telecom.TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                defaultDialer = tm.getDefaultDialerPackage();
            }
        }
        if (defaultDialer != null && !defaultDialer.isEmpty()) {
            addAppByPackage(context, results, addedPackages, defaultDialer, 2, 2);
        }
        // Fallback for Phone
        addSystemApp(context, results, addedPackages, new String[]{"dialer", "phone"}, 2, 2);

        // 1. Mandatory Default Tiles
        // Contacts / People - Size 2x2 (Matches Phone usually, or 1x1. User asked for "Contacts App tile", typically next to phone)
        // Let's make it 2x2 based on Metro aesthetic, or 1x1. User didn't specify. I'll do 1x1 default.
        addSystemApp(context, results, addedPackages, new String[]{"contacts", "people", "addressbook"}, 1, 1);

        // Clock, Calendar, Photos
        addSystemApp(context, results, addedPackages, new String[]{"clock", "alarm", "deskclock"}, 2, 2); // Clock usually wide or medium
        addSystemApp(context, results, addedPackages, new String[]{"calendar", "agenda"}, 2, 2); // User requested 2x2
        addSystemApp(context, results, addedPackages, new String[]{"gallery", "photos", "image"}, 4, 2); // Photos usually wide

        // 2. High Frequency Apps (Top 10)
        List<String> topApps = UsageHelper.getTopUsedApps(context, 10);
        for (String pkg : topApps) {
            addAppByPackage(context, results, addedPackages, pkg, 0, 0); // 0 means default
        }

        // 3. Fallback: If we have very few tiles (e.g. no permission), add some common apps if present
        if (results.size() < 4) {
             addAppByPackage(context, results, addedPackages, "com.android.settings", 1, 1);
             addAppByPackage(context, results, addedPackages, "com.android.chrome", 1, 1);
             addAppByPackage(context, results, addedPackages, "com.google.android.youtube", 1, 1);
        }

        return results;
    }

    private static void addSystemApp(Context context, List<PinnedTile> list, Set<String> added, String[] keywords, int spanX, int spanY) {
        // Simple heuristic: find an installed app with "clock", "calendar" etc in package name
        // This is not perfect but works for many stock ROMs.
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo info : apps) {
            String pkg = info.activityInfo.packageName.toLowerCase();
            for (String kw : keywords) {
                if (pkg.contains(kw)) {
                    addAppByPackage(context, list, added, info.activityInfo.packageName, spanX, spanY);
                    return; // Found one matching app for this category, stop
                }
            }
        }
    }

    // Overload for backward compatibility / simple calls
    private static void addAppByPackage(Context context, List<PinnedTile> list, Set<String> added, String pkgName) {
        addAppByPackage(context, list, added, pkgName, 0, 0);
    }

    private static void addAppByPackage(Context context, List<PinnedTile> list, Set<String> added, String pkgName, int spanX, int spanY) {
        if (added.contains(pkgName)) return;

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(pkgName);
        if (intent != null && intent.getComponent() != null) {
            ComponentName cn = intent.getComponent();
            PinnedTile t = new PinnedTile();
            t.packageName = cn.getPackageName();
            t.className = cn.getClassName();
            t.spanX = spanX;
            t.spanY = spanY;
            
            try {
                CharSequence label = pm.getActivityInfo(cn, 0).loadLabel(pm);
                t.label = label.toString();
            } catch (PackageManager.NameNotFoundException e) {
                t.label = pkgName;
            }

            list.add(t);
            added.add(pkgName);
        }
    }
}
