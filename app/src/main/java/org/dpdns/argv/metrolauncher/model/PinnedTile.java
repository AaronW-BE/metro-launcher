package org.dpdns.argv.metrolauncher.model;

public class PinnedTile {

    public String packageName;
    public String className;
    public String label;
    public int spanX;
    public int spanY;

    public String getKey() {
        return packageName + "/" + className;
    }
}
