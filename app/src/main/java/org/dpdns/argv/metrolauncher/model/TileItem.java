package org.dpdns.argv.metrolauncher.model;

import android.content.ComponentName;

public class TileItem {

    public static final int TYPE_SMALL = 1;   // 1x1
    public static final int TYPE_WIDE  = 2;   // 2x1
    public static final int TYPE_MEDIUM = 3;  // 2x2
    public static final int TYPE_LARGE = 4;   // 4x2

    public long id;
    public int type;
    public int spanX;
    public int spanY;

    public String title;
    public ComponentName component;
    public int color;

    public TileItem(long id, int type, int spanX, int spanY,
                    String title, ComponentName component, int color) {
        this.id = id;
        this.type = type;
        this.spanX = spanX;
        this.spanY = spanY;
        this.title = title;
        this.component = component;
        this.color = color;
    }

    public static TileItem fromPinned(android.content.Context context, PinnedTile pinned, long id) {
        int color = 0xFF00ACC1; // Default Cyan
        // A simple hash for random color
        int hash = pinned.packageName.hashCode();
        int[] colors = {0xFFD32F2F, 0xFFC2185B, 0xFF7B1FA2, 0xFF512DA8, 0xFF303F9F,
                        0xFF1976D2, 0xFF0288D1, 0xFF0097A7, 0xFF00796B, 0xFF388E3C,
                        0xFF689F38, 0xFFAFB42B, 0xFFFBC02D, 0xFFFFA000, 0xFFF57C00,
                        0xFFE64A19, 0xFF5D4037, 0xFF616161};
        color = colors[Math.abs(hash) % colors.length];

        android.content.Intent intent = new android.content.Intent();
        intent.setComponent(new android.content.ComponentName(pinned.packageName, pinned.className));
        
        // Match user requirement: 6 columns total.
        // Default pinned apps to only one column (1x1).
        
        int spanX = 1; 
        int spanY = 1;
        int type = TYPE_SMALL;

        String pkg = pinned.packageName.toLowerCase();
        
        if (pkg.contains("calendar") || pkg.contains("photo") || pkg.contains("gallery") ||
                   pkg.contains("google.android.apps.photos")) {
            // Force special apps to be Large (4x2)
            spanX = 4;
            spanY = 2;
            type = TYPE_LARGE;
        } else if (pkg.contains("clock") || pkg.contains("alarm")) {
            spanX = 2;
            spanY = 2;
            type = TYPE_MEDIUM;
        }
        
        // Final fallback for id-based variations if not already set to something special
        if (type == TYPE_SMALL) {
            if (id % 15 == 0) {
                // Occasional Large tile for variety
                type = TYPE_LARGE;
                spanX = 4;
                spanY = 2;
            } else if (id % 4 == 0) {
                // Occasional Medium tile
                type = TYPE_MEDIUM;
                spanX = 2;
                spanY = 2;
            }
        }

        // Overwrite with saved spans if available
        if (pinned.spanX > 0 && pinned.spanY > 0) {
            spanX = pinned.spanX;
            spanY = pinned.spanY;
            
            // Map back to type for internal logic (simplified)
            if (spanX == 1) type = TYPE_SMALL;
            else if (spanX == 2 && spanY == 1) type = TYPE_WIDE;
            else if (spanX == 4) type = TYPE_LARGE;
            else type = TYPE_MEDIUM;
        }

        return new TileItem(
            id,
            type, 
            spanX, spanY, 
            pinned.label,
            intent.getComponent(),
            color
        );
    }
}
