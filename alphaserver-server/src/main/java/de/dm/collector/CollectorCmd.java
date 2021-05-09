package de.dm.collector;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.thoughtworks.xstream.XStream;

import de.df.jutils.util.StringTools;
import de.dm.ares.data.Heat;
import de.dm.ares.data.event.HeatListener;
import de.dm.ares.data.util.XStreamUtil;
import de.dm.comm.*;

public class CollectorCmd implements Runnable {

    private CollectorDataListener dl1;

    private AlphaHttpServer       http;
    MessageReader                 mr;

    private String                name;

    StringBuilder                  sb = new StringBuilder();

    private CollectorCmd() {
    }

    void close() {
        if (dl1 != null) {
            try {
                dl1.close();
            } catch (IOException e) {
                // TODO: Implementation
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    void heatToLog(Heat heat) {
        sb.append(heat.toString());
        sb.append("\n-- -- -- -- -- --\n");

        System.out.println(sb.toString());

        sb.setLength(0);
    }

    public static void main(String[] args)
            throws TooManyListenersException, IOException, PortInUseException, UnsupportedCommOperationException {
        CollectorCmd collector = new CollectorCmd();
        collector.run();
    }

    @Override
    public void run() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date today = Calendar.getInstance().getTime();

        // Print what date is today!
        name = df.format(today) + ".ez";

        mr = new MessageReader();
        Heat[] heats = readHeats();
        if (heats != null) {
            mr.getTimeStorage().setHeats(heats);
        }

        http = new AlphaHttpServer(mr.getTimeStorage());
        http.start();

        connect();
    }

    private final class CollectorDataListener implements DataListener {

        private final PortReader  in;
        private final Collector    coll;
        private long               last  = 0;
        private int                count = 0;
        private final OutputStream os;

        public CollectorDataListener(PortReader p, OutputStream o, String name) throws IOException {
            in = p;
            name = null;
            os = o;
            coll = new Collector(name);
        }

        @Override
        public void dataAvailable() {
            while (in.isAvailable()) {
                try {
                    byte b = in.read();
                    long curr = System.currentTimeMillis() / 1000;
                    coll.collect(b);
                    System.out.println("Text: " + StringTools.byteToHex(b));
                    if (os != null) {
                        os.write(b);
                    }
                    if (mr != null) {
                        try {
                            mr.push(b);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // fos.write(b);
                    if (curr != last) {
                        System.out.println("Time: " + toTime(curr));
                        last = curr;
                    }
                    count++;
                    System.out.println("Counter: " + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String toTime(long curr) {
            curr = curr % (60 * 60 * 24);
            int h = (int) (curr / (60 * 60));
            int m = (int) ((curr / (60)) % 60);
            int s = (int) (curr % 60);
            return "" + h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
        }

        public void close() throws IOException {
            // fos.close();
            coll.close();
        }
    }

    Heat[] readHeats() {
        if (name != null) {
            XStream x = XStreamUtil.getXStream();
            try {
                FileInputStream fis = new FileInputStream(name);
                Heat[] heats = (Heat[]) x.fromXML(fis);
                if (heats == null) {
                    System.err.println("File " + name + " could not be read.");
                }
                fis.close();
                // mr.getTimeStorage().setHeats(heats);
                return heats;
            } catch (IOException io) {
                // io.printStackTrace();
            }
        }
        return null;
    }

    void writeHeats() {
        if (name != null) {
            try {
                Heat[] heats = mr.getTimeStorage().getHeats();
                XStream stream = XStreamUtil.getXStream();
                FileOutputStream fos = new FileOutputStream(name);
                stream.toXML(heats, fos);
                fos.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    void connect() {
        try {
            String[] portsx = CommFactory.getPorts();

            System.out.println("Available ports:");
            for (String port1 : portsx) {
                System.out.println("  " + port1);
            }

            String port = portsx[0];
            System.out.println("Connecting to port " + port);

            PortReader pr = null;

            mr.addHeatListener(new HeatListener() {

                @Override
                public void newHeat(Heat heat) {
                    // Nothing to do
                }

                @Override
                public void finishedHeat(Heat heat) {
                    heatToLog(heat);
                    writeHeats();
                }
            });

            if (port.length() > 0) {
                pr = new NRJavaSerialPortReader(port, CommunicationMode.ARES21);
            }
            if (pr != null) {
                String pname = port.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                dl1 = new CollectorDataListener(pr, new FileOutputStream("binary-" + pname + ".dat", true), port);
                pr.addDataListener(dl1);

            }
            return;
        } catch (PortInUseException piu) {
            piu.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }
}