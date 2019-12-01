package de.dm.collector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.SystemUtils;

public class Collector {

    private String                name;
    private ByteArrayOutputStream data    = new ByteArrayOutputStream();
    private long                  time    = 0;
    private ZipOutputStream       zip     = null;
    private long                  ziptime = 0;

    public Collector(String name) {
        this.name = name == null ? "null" : name.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    public synchronized void collect(byte dat) throws IOException {
        long curr = System.currentTimeMillis() / 1000;
        if (curr != time) {
            try {
                String userhome = SystemUtils.getUserHome().getCanonicalPath();
                new File(Paths.get(userhome, ".JAuswertungHome", "AlphaServer", "Data").toFile().getAbsolutePath()).mkdirs();

                write();
            } catch (IOException io) {
                io.printStackTrace();
            }
            time = curr;
        }
        data.write(dat);
    }

    public String toTime(long curr) {
        curr = curr % (60 * 60 * 24);
        int h = (int) (curr / (60 * 60));
        int m = (int) ((curr / (60)) % 60);
        int s = (int) (curr % 60);
        return "" + (h < 10 ? "0" : "") + h + "-" + (m < 10 ? "0" : "") + m + "-" + (s < 10 ? "0" : "") + s;
    }

    public synchronized void write() throws IOException {
        if (data.size() > 0) {
            if (zip != null) {
                if (ziptime + (60 * 60 * 10) <= time) {
                    zip.close();
                    zip = null;
                }
            }
            if (zip == null) {
                zip = new ZipOutputStream(new FileOutputStream("data/" + name + "-" + toTime(time) + ".zip"));
                ziptime = time;
            }

            zip.putNextEntry(new ZipEntry(name + "-" + toTime(time) + ".dat"));
            OutputStream fos = zip;
            fos.write(data.toByteArray());
            // fos.close();
            data.reset();
            time = 0;
        }
    }

    public synchronized void close() throws IOException {
        try {
            if (new File("data").exists() && !new File("data").isDirectory()) {
                new File("data").delete();
            }
            if (!new File("data").exists()) {
                new File("data").mkdir();
            }
            write();
        } catch (IOException io) {
            io.printStackTrace();
        }
        if (zip != null) {
            zip.close();
        }
    }
}
