package org.dpdns.argv.metrolauncher;

import org.dpdns.argv.metrolauncher.model.OnTilesChangedListener;

import java.util.ArrayList;
import java.util.List;

public class TileChangeBus {

    private static final List<OnTilesChangedListener> listeners =
            new ArrayList<>();

    public static void register(OnTilesChangedListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public static void unregister(OnTilesChangedListener l) {
        listeners.remove(l);
    }

    public static void notifyChanged() {
        for (OnTilesChangedListener l : new ArrayList<>(listeners)) {
            l.onTilesChanged();
        }
    }
}
