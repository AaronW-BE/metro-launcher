package org.dpdns.argv.metrolauncher;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsageHelper {

    /**
     * returns true if the user has granted the usage stats permission
     */
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Get top N used apps in the last 30 days
     */
    public static List<String> getTopUsedApps(Context context, int limit) {
        if (!hasUsageStatsPermission(context)) {
            return new ArrayList<>();
        }

        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -30); // 30 days
        long startTime = calendar.getTimeInMillis();

        Map<String, UsageStats> statsMap = usm.queryAndAggregateUsageStats(startTime, endTime);

        if (statsMap == null || statsMap.isEmpty()) return new ArrayList<>();
        
        // Filter system apps / launcher itself if needed, but for now just sort by total time
        return statsMap.values().stream()
                .filter(stats -> stats.getTotalTimeInForeground() > 0)
                // Filter out ourselves
                .filter(stats -> !stats.getPackageName().equals(context.getPackageName())) 
                .sorted((a, b) -> Long.compare(b.getTotalTimeInForeground(), a.getTotalTimeInForeground()))
                .limit(limit)
                .map(UsageStats::getPackageName)
                .collect(Collectors.toList());
    }
}
