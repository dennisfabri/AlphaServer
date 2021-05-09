package de.dm.collector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import de.dm.ares.data.Heat;
import de.dm.ares.data.TimeStorage;
import de.dm.ares.data.util.XStreamUtil;
import de.dm.collector.http.HttpServerThread;
import de.dm.collector.http.IHttpDataProvider;
import de.dm.collector.http.Request;

public class AlphaHttpServer {

    private HttpServerThread server;
    private TimeStorage      times;

    public AlphaHttpServer(TimeStorage t) {
        times = t;
        try {
            server = new HttpServerThread(1999, new IHttpDataProvider() {
                @Override
                public byte[] sendData(Request name) throws IOException {
                    return getData(name);
                }
            });
        } catch (IOException ioe) {
            ioe.printStackTrace();
            server = null;
        }
    }

    public boolean start() {
        if (server == null) {
            return false;
        }
        server.start();
        return true;
    }

    byte[] getData(Request name) {
        Heat[] heats = times.getHeats();
        return toData(heats);
    }

    public static byte[] toData(Heat[] heats) {
        XStream stream = XStreamUtil.getXStream();
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        stream.toXML(heats, fos);
        return fos.toByteArray();
    }

}