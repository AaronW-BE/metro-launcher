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
}
