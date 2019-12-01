package de.dm.ares.data.event;

import de.dm.ares.data.Heat;

public interface HeatListener {
    void newHeat(Heat heat);
    void finishedHeat(Heat heat);
}
