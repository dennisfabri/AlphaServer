package de.dm.ares.data;

import java.io.Serializable;
import java.util.Arrays;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jutils.util.StringTools;

public class Lane implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7389645818914972121L;

    @XStreamAsAttribute
    private final int laneindex;

    private long[] times = new long[0];
    private LaneStatus[] stati = new LaneStatus[0];

    public Lane(int index) {
        laneindex = index;
    }

    public void store(long time, LaneStatus status) {
        synchronized (this) {
            long[] newtimes = Arrays.copyOf(times, times.length + 1);
            LaneStatus[] newstati = Arrays.copyOf(stati, stati.length + 1);
            newtimes[times.length] = time;
            newstati[stati.length] = status;
            times = newtimes;
            stati = newstati;
        }
    }

    public void updateTimes(int factor) {
        for (int x = 0; x < times.length; x++) {
            times[x] = times[x] / factor;
        }
    }

    public long[] getTimes() {
        return Arrays.copyOf(times, times.length);
    }

    public long getLastTime() {
        if (times.length == 0) {
            return -1;
        }
        return times[times.length - 1];
    }

    public LaneStatus[] getStati() {
        return Arrays.copyOf(stati, stati.length);
    }

    public int getLaneNr() {
        return laneindex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(laneindex);
        sb.append(":");
        for (long time : times) {
            sb.append(" ");
            sb.append(StringTools.zeitString(Math.round(time / 10)));
        }
        return sb.toString();
    }
}
