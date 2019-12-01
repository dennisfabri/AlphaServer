package de.dm.collector;

import com.thoughtworks.xstream.XStream;

public class XStreamUtil {
    public static XStream getXStream() {
        XStream x = createXStream();
        addAliasesToStream(x);
        return x;
    }
    
    private static void addAliasesToStream(XStream stream) {
        stream.aliasType("AlphaServer.Heat", de.dm.ares.data.Heat.class);
        stream.aliasType("AlphaServer.Lane", de.dm.ares.data.Lane.class);
        stream.aliasType("AlphaServer.LaneStatus", de.dm.ares.data.LaneStatus.class);
    }
    
    // Source
    // https://github.com/x-stream/xstream/issues/101#issuecomment-514760040
    private static XStream createXStream() {
        return new XStream();
    }
}