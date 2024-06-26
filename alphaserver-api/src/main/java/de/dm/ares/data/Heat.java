package de.dm.ares.data;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import static java.lang.String.format;

public class Heat implements Serializable {

    private static final long serialVersionUID = 4925782095479969005L;

    private final Map<Integer, Lane> lanes;
    @XStreamAsAttribute
    private final int event;
    @XStreamAsAttribute
    private final int heat;
    @XStreamAsAttribute
    private final String id;

    public Heat(String id, int event, int heatname) {
        lanes = new Hashtable<>();
        this.event = event;
        this.heat = heatname;
        this.id = id;
    }

    public void store(int lanenr, long time, LaneStatus status) {
        synchronized (lanes) {
            for (int i = 0; i <= lanenr; i++) {
                Lane lane = lanes.get(i);
                if (lane == null) {
                    lane = new Lane(i);
                    lanes.put(i, lane);
                }
            }
            Lane lane = lanes.get(lanenr);
            if (lane != null) {
                lane.store(time, status);
            }
        }
    }

    public boolean hasMissingTime(int lanecount) {
        synchronized (lanes) {
            for (int i = 0; i < lanecount; i++) {
                if (lanes.get(i) == null) {
                    return true;
                }
                if (lanes.get(i).getTimes() == null) {
                    return true;
                }
                if (lanes.get(i).getTimes().length == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getMaxLane() {
        return lanes.keySet().stream().sorted((l1, l2) -> l2 - l1).findFirst().orElse(-1);
    }

    public long[][] getTimes() {
        synchronized (lanes) {
            int max = getMaxLane();
            if (max < 0) {
                return null;
            }
            long[][] result = new long[max + 1][0];
            for (Integer key : lanes.keySet()) {
                Lane lane = lanes.get(key);
                result[key] = lane.getTimes();
            }
            return result;
        }
    }

    public int getHeat() {
        return heat;
    }

    public int getEvent() {
        return event;
    }

    public String getID() {
        return id;
    }

    public void updateTimes(int factor) {
        for (Lane lane : lanes.values()) {
            lane.updateTimes(factor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(": ");
        sb.append(event);
        sb.append("/");
        sb.append(heat);
        int max = getMaxLane() + 1;
        for (int i = 0; i < max; i++) {
            Lane lane = lanes.get(i);
            sb.append("\n");
            sb.append(lane.toString());
        }
        return sb.toString();
    }

    public boolean fits(String heatname) {
        heatname = heatname.replace("-", "");
        if (heatname.equals("" + getEvent())) {
            return true;
        }
        if (heatname.equals("" + getHeat())) {
            return true;
        }
        while (heatname.startsWith("0")) {
            heatname = heatname.substring(1);
        }
        if (heatname.equals(getEvent() + "/" + getHeat())) {
            return true;
        }
        return heatname.equals(format("%d%2d", getEvent(), getHeat()));
    }
}
