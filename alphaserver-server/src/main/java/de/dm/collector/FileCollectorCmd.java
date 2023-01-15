package de.dm.collector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TooManyListenersException;

import com.thoughtworks.xstream.XStream;

import de.dm.ares.data.Heat;
import de.dm.ares.data.TimeStorage;
import de.dm.ares.data.event.HeatListener;
import de.dm.ares.data.util.XStreamUtil;

public final class FileCollectorCmd {
    private AlphaHttpServer http;
    MessageReader mr;

    private String name;

    StringBuilder sb = new StringBuilder();

    private FileCollectorCmd() {
    }

    void close() {
        System.exit(0);
    }

    void heatToLog(Heat heat) {
        sb.append(heat.toString());
        sb.append("\n-- -- -- -- -- --\n");

        System.out.println(sb.toString());

        sb.setLength(0);
    }

    public static void main(String[] args) throws TooManyListenersException, IOException {
        FileCollectorCmd collector = new FileCollectorCmd();
        collector.run(args[0]);
    }

    public void run(String file) {
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

        connect(file);
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

    void connect(String file) {
        try {
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

            byte[] data = Files.readAllBytes(Path.of(file));
            for (byte d : data) {
                mr.push(d);
            }
            TimeStorage ts = mr.getTimeStorage();
            Heat[] heats = ts.getHeats();
            try (FileOutputStream os = new FileOutputStream(name + ".xml")) {
                XStreamUtil.getXStream().toXML(heats, os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
