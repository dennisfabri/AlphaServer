package de.dm.ares.file;

import java.io.*;

import de.df.jutils.io.FileUtils;
import de.df.jutils.util.StringTools;
import de.dm.ares.data.*;

public class FileReader {
    public static String file1 = "C:\\Users\\dennis\\Desktop\\DM\\LSTRSLT.TXT";

    public static void main(String[] args) {
        read(file1, 1, new TimeStorage());
    }

    public static Heat[] readHeats(String file) {
        return readHeats(new String[] { file });
    }

    public static Heat[] readHeats(String[] files) {
        TimeStorage ts = new TimeStorage();
        if (files != null) {
            for (String file : files) {
                read(file, 1, ts);
            }
        }
        return ts.getHeats();
    }

    private static Object[][] readFile(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            return readIS(is);
        } catch (IOException ex) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // Nothing to do
                }
            }
        }
    }

    public static Object[][] readIS(InputStream name) {

        String[] lines = FileUtils.readTextFile(name);
        if ((lines == null) || (lines.length <= 1)) {
            return null;
        }
        char separator = identifySeparator(lines[0]);
        Object[][] data = new Object[lines.length][0];
        int maxline = 0;
        for (int x = 0; x < lines.length; x++) {
            String[] line = StringTools.separateCsvLine(lines[x], separator);
            if (line.length > maxline) {
                maxline = line.length;
            }
            data[x] = new Object[line.length];
            System.arraycopy(line, 0, data[x], 0, line.length);
        }

        for (int x = 0; x < data.length; x++) {
            if (data[x].length < maxline) {
                Object[] line = new Object[maxline];
                System.arraycopy(data[x], 0, line, 0, data[x].length);
                for (int y = data[x].length; y < line.length; y++) {
                    line[y] = "";
                }
                data[x] = line;
            }
        }
        return data;
    }

    private static final char[] SEPARATORS = { ',', ',', ';' };

    static char identifySeparator(String data) {
        for (char SEPARATOR : SEPARATORS) {
            int index = data.indexOf(SEPARATOR);
            if (index >= 0) {
                return SEPARATOR;
            }
        }
        return SEPARATORS[0];
    }

    // event;round;heat;lap;lane;idStatus;rank;time ;result ;mod ;btime;bresult; bmod;dtime;dresult;dmod
    // 1 ;0 ;0 ;0 ;0 ;0 ;0 ;57550360 ;" 15:59:10.36" ;" " ;0 ;"" ;"" ;0 ;"" ;"" ;
    // 1 ;0 ;0 ;50 ;7 ;0 ;1 ;33920 ;" 33.92" ;" " ;33920;" 33.92" ;" " ;0 ;"" ;"" ;

    public static void read(String file, int index, TimeStorage ts) {
        // ts.clear();
        // TimeStorage ts = new TimeStorage();
        // ts.store(Index.TimeInserted, TimeType.Finish, event, heat, lane, time, LaneStatus.OfficialEnd);

        Object[][] data = null;
        try {
            data = readFile(file);
        } catch (Exception ex) {
            return;
        }
        if (data == null) {
            return;
        }
        for (int r = 1; r < data.length; r++) {
            Object[] row = data[r];
            String evt = row[0].toString();
            // String rnd = row[1].toString();
            String ht = "" + index; // row[2].toString();
            String ln = row[4].toString();
            String tm = row[7].toString();
            String btm = row[10].toString();

            if (!isNumber(ln)) {
                continue;
            }

            int lane = Integer.parseInt(ln) - 1;
            if (lane < 0) {
                continue;
            }

            if (!isNumber(evt) || !isNumber(ht)) {
                continue;
            }

            int event = Integer.parseInt(evt);
            int heat = Integer.parseInt(ht);

            if (isNumber(btm) && !btm.equals("0")) {
                try {
                    ts.store(Index.TimeInserted, TimeType.Finish, event, heat, lane, Integer.parseInt(btm), LaneStatus.RaceTimes);
                } catch (NumberFormatException nfe) {
                    System.err.println("Backuptime in unknown format: " + btm);
                    // Nothing to do
                }
            }
            if (isNumber(tm)) {
                ts.store(Index.TimeInserted, TimeType.Finish, event, heat, lane, Integer.parseInt(tm), LaneStatus.RaceTimes);
            }
        }
    }

    private static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
