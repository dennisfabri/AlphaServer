package de.dm.collector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import de.df.jutils.http.HttpServerThread;
import de.df.jutils.http.IHttpDataProvider;
import de.df.jutils.http.Request;
import de.dm.ares.data.Heat;
import de.dm.ares.data.TimeStorage;

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