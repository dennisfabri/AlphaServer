package de.dm.ares.data;

import java.util.LinkedList;

import de.dm.ares.data.event.HeatListener;

public class TimeStorage {

    private final LinkedList<Heat> heats;
    private Heat current;
    private transient LinkedList<HeatListener> listeners = new LinkedList<HeatListener>();

    public TimeStorage() {
        heats = new LinkedList<Heat>();
        current = null;
    }

    public void setHeats(Heat[] newheats) {
        for (Heat h : newheats) {
            heats.addLast(h);
        }
    }

    private void fireNewHeatEvent(Heat heat) {
        try {
            for (HeatListener l : listeners) {
                l.newHeat(heat);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    public Heat[] getHeats() {
        return heats.toArray(new Heat[heats.size()]);
    }

    private void fireFinishedHeatEvent(Heat heat) {
        try {
            for (HeatListener l : listeners) {
                l.finishedHeat(heat);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    public void newHeat(int event, int heat) {
        if (current != null) {
            System.out.println(current.toString());
            fireFinishedHeatEvent(current);
        }
        synchronized (heats) {
            current = new Heat("" + (1 + heats.size()), event, heat);
            heats.addLast(current);
            fireNewHeatEvent(current);
        }
    }

    public void addHeatListener(HeatListener hl) {
        listeners.addLast(hl);
    }

    public void moveListeners(TimeStorage ts) {
        ts.listeners.addAll(listeners);
        listeners.clear();
    }

    public void store(Index index, TimeType type, int event, int heat, int lane, long time, LaneStatus status) {
        if ((current == null) || (current.getHeat() != heat) || (current.getEvent() != event)
                || (type == TimeType.Start)) {
            newHeat(event, heat);
        }
        if (time <= 0) {
            return;
        }
        synchronized (current) {
            current.store(lane, time, status);
        }
    }

    public Heat getHeat(String heatid) {
        synchronized (heats) {
            for (Heat heat : heats) {
                if (heat.getID().equals(heatid)) {
                    return heat;
                }
            }
        }
        return null;
    }

    public long[][] getTimes(String heatid) {
        Heat heat = getHeat(heatid);
        if (heat == null) {
            return null;
        }
        return heat.getTimes();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Heat heat : heats) {
            sb.append(heat.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void clear() {
        heats.clear();
        current = null;
    }
}