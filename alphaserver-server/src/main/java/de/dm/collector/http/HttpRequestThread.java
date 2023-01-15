/*
 * Created on 21.01.2005
 */

package de.dm.collector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

class HttpRequestThread extends Thread {

    private final byte[] empty = new byte[0];

    private Socket socket;
    private IHttpDataProvider source;

    // Konstruktor
    public HttpRequestThread(Socket s, IHttpDataProvider so) {
        setName("HttpRequestThread");
        socket = s;
        source = so;
    }

    // Verarbeiten der Requests
    @Override
    public void run() {
        try {
            PrintStream os = new PrintStream(socket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // erste Zeile lesen; z.B. "GET /hallo.html HTTP/1.0"
            String get = br.readLine();
            if (get == null) {
                socket.close();
                return;
            }
            StringTokenizer st = new StringTokenizer(get);
            String method = st.nextToken(); // Methode
            String filename = st.nextToken(); // Request-URI
            if (filename.endsWith("/")) {
                // evt. Indexfile anhaengen
                filename += "index.html";
            }

            String ct = guessContentType(filename); // Content-Type
            String version = "";
            if (st.hasMoreTokens()) {
                version = st.nextToken();
            }
            if ("POST".equals(method)) {
                String line = br.readLine();
                while ((line != null) && (line.trim().length() > 0)) {
                    line = br.readLine();
                }
                if (line != null) {
                    line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            filename += "?" + line;
                        }
                    }
                }
            }
            // restliche Sendung ignorieren
            socket.getInputStream().skip(socket.getInputStream().available());
            try {
                // die gefragte Datei lesen
                byte[] data = source.sendData(new Request(filename));
                if (data == null) {
                    data = empty;
                }

                // geg. Status-Zeile und Header senden
                if (version.startsWith("HTTP/1")) {
                    os.print("HTTP/1.0 200 OK\r\n");
                    os.print("Date: " + DateFormat.getInstance().format(new Date()) + "\r\n");
                    os.print("Server: BasicHttp/1.2\r\n");
                    os.print("Content-length: " + data.length + "\r\n");
                    os.print("Content-type: " + ct + "\r\n\r\n");
                }
                // geg. Datei senden
                if (!"HEAD".equals(method)) {
                    os.write(data);
                    // os.println(new String(data));
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                // geg. File Not Found-Meldung senden
                // geg. Status-Zeile und Header senden
                if (version.startsWith("HTTP/1")) {
                    os.print("HTTP/1.0 404 File Not Found\r\n");
                    os.print("Date: " + DateFormat.getInstance().format(new Date()) + "\r\n");
                    os.print("Server: BasicHttp/1.2\r\n");
                    os.print("Content-type: text/html\r\n\r\n");
                }
                os.print("<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD>"
                        + "<BODY><H1>HTTP Error 404: File Not Found</H1>" + "</BODY></HTML>");
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Nothing to do
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Nothing to do
        }
    }

    private static String guessContentType(String name) { // erraet den
        // MIME-Type
        // anhand der Extension
        String result = URLConnection.guessContentTypeFromName(name);
        if (result != null) {
            return result;
        }

        name = name.toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".txt") || name.endsWith(".java")) {
            return "text/plain";
        } else if (name.endsWith(".xml")) {
            return "text/xml";
        } else if (name.endsWith(".gif")) {
            return "image/gif";
        } else if (name.endsWith(".xls")) {
            return "application/octet-stream";
        } else if (name.endsWith(".class")) {
            return "application/octet-stream";
        } else if (name.endsWith(".pdf")) {
            return "application/pdf";
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "text/plain";
        }
    }
}
