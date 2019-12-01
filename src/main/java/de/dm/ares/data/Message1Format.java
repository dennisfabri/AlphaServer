package de.dm.ares.data;


public class Message1Format {

    private final LaneStatus   status;
    private final TimeType timetype;
    private final Index    index;
    private final int      laps;
    private final int      event;
    private final int      heat;
    private final int      place;

    public Message1Format(LaneStatus s, TimeType t, Index i, int l, int e, int h,
            int p) {
        status = s;
        timetype = t;
        index = i;
        laps = l;
        event = e;
        heat = h;
        place = p;
    }

    public LaneStatus getStatus() {
        return status;
    }

    public TimeType getTimeType() {
        return timetype;
    }

    public Index getIndex() {
        return index;
    }

    public int getLaps() {
        return laps;
    }

    public int getEvent() {
        return event;
    }

    public int getHeat() {
        return heat;
    }

    public int getPlace() {
        return place;
    }

}
