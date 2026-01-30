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

        // 1. Time / Clock - 2x2
        addSystemApp(context, results, addedPackages, new String[]{"clock", "alarm", "deskclock"}, 2, 2);

        // 2. Calendar - 2x2
        addSystemApp(context, results, addedPackages, new String[]{"calendar", "agenda"}, 2, 2);

        // 3. Four 1x1 Tiles: Phone, Contacts, Camera, Browser
        
        // (1) Phone - 1x1
        String defaultDialer = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.telecom.TelecomManager tm = (android.telecom.TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                defaultDialer = tm.getDefaultDialerPackage();
            }
        }
        if (defaultDialer != null && !defaultDialer.isEmpty()) {
            addAppByPackage(context, results, addedPackages, defaultDialer, 1, 1);
        }
        addSystemApp(context, results, addedPackages, new String[]{"dialer", "phone"}, 1, 1);

        // (2) Contacts - 1x1
        addSystemApp(context, results, addedPackages, new String[]{"contacts", "people", "addressbook"}, 1, 1);
        
        // (3) Camera - 1x1
        addSystemApp(context, results, addedPackages, new String[]{"camera", "cam", "android.camera"}, 1, 1);

        // (4) Browser - 1x1
        String defaultBrowser = null;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://www.google.com"));
        ResolveInfo browserInfo = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (browserInfo != null && browserInfo.activityInfo != null) {
            defaultBrowser = browserInfo.activityInfo.packageName;
            if (!"android".equals(defaultBrowser)) {
                addAppByPackage(context, results, addedPackages, defaultBrowser, 1, 1);
            }
        }
        addSystemApp(context, results, addedPackages, new String[]{"browser", "chrome", "firefox", "edge"}, 1, 1);


        // 4. Photos - 4x2 (Wide)
        addSystemApp(context, results, addedPackages, new String[]{"gallery", "photos", "image"}, 4, 2); 

        // 5. High Frequency Apps (Top 10)
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
