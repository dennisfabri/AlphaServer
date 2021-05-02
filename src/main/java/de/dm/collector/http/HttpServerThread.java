package de.dm.collector.http;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.IllegalBlockingModeException;

/*
 * Created on 21.01.2005
 */

/**
 * @author Dennis Mueller
 * @date 21.01.2005
 */
public class HttpServerThread extends Thread {

    private ServerSocket      socket          = null;
    private boolean           stopped         = false;
    private IHttpDataProvider source          = null;
    private int               connectionCount = 64;

    public HttpServerThread(int port, IHttpDataProvider s) throws IOException {
        if (port < 0 || port > 65535) {
            port = 80;
        }
        // Erzeugen eines Sockets
        socket = new ServerSocket(port);
        source = s;

        setName("HttpServerThread");
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
    }

    public void cancel() {
        stopped = true;
        try {
            URLConnection connection = new URL("http://127.0.0.1/index.html").openConnection();
            connection.connect();
            PrintStream ps = new PrintStream(connection.getOutputStream());
            ps.println("stop");
            ps.close();
            connection.getInputStream().close();
        } catch (MalformedURLException e) {
            // Nothing to do
        } catch (ProtocolException pe) {
            // Nothing to do
        } catch (IOException e) {
            // Nothing to do
        }
    }

    @Override
    public void run() {
        // Fast endlos auf Verbindungen warten ...
        while (!stopped) {
            while (Thread.activeCount() > connectionCount) {
                // Waiting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Nothing to do
                }
            }

            // max. 100 Threads starten, geg. warten
            try {
                Socket s = socket.accept();
                if (!stopped) {
                    new HttpRequestThread(s, source).start();
                }
            } catch (IOException e) {
                // Nothing to do
            } catch (IllegalBlockingModeException e) {
                // Nothing to do
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Nothing to do
        }
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        if (connectionCount < 2) {
            throw new IllegalArgumentException();
        }
        this.connectionCount = connectionCount;
    }
}