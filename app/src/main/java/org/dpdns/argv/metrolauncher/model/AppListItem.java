package org.dpdns.argv.metrolauncher.model;

public class AppListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_APP = 1;

    public int type;

    // header
    public String letter;

    // app
    public AppInfo app;

    public static AppListItem header(String letter) {
        AppListItem item = new AppListItem();
        item.type = TYPE_HEADER;
        item.letter = letter;
        return item;
    }

    public static AppListItem app(AppInfo app) {
        AppListItem item = new AppListItem();
        item.type = TYPE_APP;
        item.app = app;
        return item;
    }
}
