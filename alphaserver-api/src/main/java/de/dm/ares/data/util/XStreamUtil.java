package de.dm.ares.data.util;

import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ArrayTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import de.dm.ares.data.Heat;
import de.dm.ares.data.Lane;
import de.dm.ares.data.LaneStatus;

public final class XStreamUtil {

    public static XStream getXStream() {
        XStream x = createXStream();
        addAliasesToStream(x);
        setupPermissions(x);
        return x;
    }

    private static void addAliasesToStream(XStream stream) {
        stream.aliasType("AlphaServer.Heat", de.dm.ares.data.Heat.class);
        stream.aliasType("AlphaServer.Lane", de.dm.ares.data.Lane.class);
        stream.aliasType("AlphaServer.LaneStatus", de.dm.ares.data.LaneStatus.class);

        stream.useAttributeFor(de.dm.ares.data.Lane.class, "laneindex");
        stream.useAttributeFor(de.dm.ares.data.Heat.class, "event");
        stream.useAttributeFor(de.dm.ares.data.Heat.class, "heat");
        stream.useAttributeFor(de.dm.ares.data.Heat.class, "id");
    }

    // Source
    // https://github.com/x-stream/xstream/issues/101#issuecomment-514760040
    private static XStream createXStream() {
        return new XStream();
    }

    /*
     * Source: http://x-stream.github.io/security.html#example
     */
    private static void setupPermissions(XStream xstream) {
        // clear out existing permissions and start a whitelist
        xstream.addPermission(NoTypePermission.NONE);
        // allow some basics
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.addPermission(ArrayTypePermission.ARRAYS);
        xstream.allowTypeHierarchy(Collection.class);
        // allow any type from the same package
        xstream.allowTypes(new Class[] { Heat.class, Lane.class, LaneStatus.class });
    }

    private XStreamUtil() {
    }
}
