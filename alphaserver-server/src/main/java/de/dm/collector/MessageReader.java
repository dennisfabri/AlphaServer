package de.dm.collector;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import de.dm.ares.data.Index;
import de.dm.ares.data.LaneStatus;
import de.dm.ares.data.Message1Format;
import de.dm.ares.data.TimeStorage;
import de.dm.ares.data.TimeType;
import de.dm.ares.data.event.HeatListener;

public class MessageReader {

    private TimeStorage           times  = new TimeStorage();

    private ByteArrayOutputStream bos    = new ByteArrayOutputStream();
    private Message1Format        finish = null;

    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("dm2015-dat.dat");
        MessageReader mr = new MessageReader();
        int byt = fis.read();
        while (byt >= 0) {
            mr.push(byt);
            byt = fis.read();
        }
        fis.close();
        System.out.println(mr.times.toString());
    }

    public TimeStorage getTimeStorage() {
        return times;
    }

    public synchronized void setTimeStorage(TimeStorage ts) {
        if (ts == null) {
            throw new IllegalArgumentException("TimeStorage must not be null.");
        }
        times.moveListeners(ts);
        times = ts;
    }

    public void addHeatListener(HeatListener hl) {
        times.addHeatListener(hl);
    }

    private boolean active = false;

    public void push(int data) {
        if (data < 0) {
            return;
        }
        if (data != 4) {
            if (data == 1) {
                active = true;
            }
            if (active) {
                bos.write(data);
            }
        } else {
            if (bos.size() > 0) {
                finish = processMessage(bos.toByteArray(), finish);
            }
            bos.reset();
            active = false;
        }
    }

    public static Message1Format process1stMessage(byte[] data) {
        System.out.println("### 1st ################");
        boolean invalid = false;
        
        LaneStatus status;
        System.out.print("Status: ");
        switch (data[3]) {
        case '0':
            status = LaneStatus.NotUsed;
            System.out.print("not used");
            break;
        case '1':
            status = LaneStatus.OfficialEnd;
            System.out.print("Official End");
            invalid = true;
            break;
        case '2':
            status = LaneStatus.RaceTimes;
            System.out.print("Race times");
            break;
        case '3':
            status = LaneStatus.ResultsOfTheRace;
            System.out.print("Results of the race");
            break;
        case '4':
            status = LaneStatus.ResultsWithBackupTimes;
            System.out.print("Results with back up times");
            break;
        case '5':
            status = LaneStatus.ResultsOfThePreviousRace;
            System.out.print("Results of the previous race");
            break;
        case '6':
            status = LaneStatus.BackupOfThePreviousRace;
            System.out.print("Back up of the previous race");
            break;
        case '9':
            status = LaneStatus.RunningTime;
            System.out.print("Running time");
            invalid = true;
            break;
        case ' ':
            status = LaneStatus.Emtpy;
            System.out.print("<Empty>");
            invalid = true;
            break;
        default:
            status = LaneStatus.Unkown;
            System.out.print("Unknown " + data[3]);
            invalid = true;
            break;
        }
        System.out.print(", ");

        TimeType timetype;
        System.out.print("Time type: ");
        switch (data[4]) {
        case 'S':
            timetype = TimeType.Start;
            System.out.print("Start");
            invalid = true;
            break;
        case 'I':
            timetype = TimeType.Intermediate;
            System.out.print("Intermediate");
            invalid = true;
            break;
        case 'A':
            timetype = TimeType.Finish;
            System.out.print("Finish");
            break;
        case 'D':
            timetype = TimeType.StartingBlock;
            System.out.print("Starting block");
            invalid = true;
            break;
        case 'R':
            timetype = TimeType.ReactionTime;
            System.out.print("Reaction time");
            invalid = true;
            break;
        default:
            timetype = TimeType.Unkown;
            System.out.print("Unknown " + data[4]);
            invalid = true;
            break;
        }
        System.out.print(", ");

        Index index;
        System.out.print("Index: ");
        switch (data[5]) {
        case 'C':
            index = Index.TimeCorrected;
            System.out.print("Time corrected");
            break;
        case 'E':
            index = Index.TimeErased;
            System.out.print("Time erased");
            break;
        case 'I':
            index = Index.TimeInserted;
            System.out.print("Time inserted");
            break;
        case 'M':
            index = Index.ManualTime;
            System.out.print("Manual time");
            break;
        case 'N':
            index = Index.NoTime;
            System.out.print("No Time");
            break;
        case 'T':
            index = Index.MissingPushButton;
            System.out.print("Missing push button");
            break;
        case '+':
            index = Index.StartingBlockAfterTouchPad;
            System.out.print("Starting block after touch pad");
            break;
        case '-':
            index = Index.StartingBlockBeforeTouchPad;
            System.out.print("Starting block before touch pad (False start)");
            break;
        case ' ':
            index = Index.Empty;
            System.out.print("<Emtpy>");
            break;
        default:
            index = Index.Unkown;
            System.out.print("Unknown" + data[5]);
            invalid = true;
            break;
        }
        System.out.println();

        processLanes(data);

        String laps = "" + ((char) data[8]) + ((char) data[9]);
        String event = "" + ((char) data[10]) + ((char) data[11]) + ((char) data[12]);
        String heat = "" + ((char) data[13]) + ((char) data[14]);
        String place = "" + ((char) data[17]) + ((char) data[18]);

        System.out.print("Laps: " + laps);
        System.out.print(", ");
        System.out.print("Event: " + event);
        System.out.print(", ");

        System.out.print("Heat: " + heat);
        System.out.print(", ");
        System.out.print("Place: " + place);
        System.out.println();
        
        if (invalid) {
            return null;
        }
        
        return new Message1Format(status, timetype, index, toInt(laps), toInt(event), toInt(heat), toInt(place));
    }

    private static int toInt(String i) {
        try {
            return Integer.parseInt(i.trim());
        } catch (RuntimeException re) {
            return -1;
        }
    }

    private static double toDouble(String i) {
        try {
            return Double.parseDouble(i.trim());
        } catch (RuntimeException re) {
            return -1;
        }
    }

    private static int toTime(String t) {
        int result = 0;

        int i = t.indexOf(':');
        String m = "0";
        int min = 0;
        if (i > 0) {
            m = t.substring(0, i);
            min = toInt(m);
            if (min > 0) {
                result += 60 * min * 1000;
            }
        }
        String rest = t.substring(i + 1, t.length() - 1);
        double sek = toDouble(rest);
        if (sek > 0) {
            result += Math.round(sek * 1000);
        }

        // System.err.println(t + " -> " + result + " = " + StringTools.zeitString(result / 10));
        // System.err.println(m + " : " + rest + " -> " + min + ":" + sek);

        return result;
    }

    public static void processLanes(byte[] data) {
        System.out.print("Lanes used: ");
        for (int x = 6; x < 8; x++) {
            byte b = (byte) (data[x] % 32);
            String s = "";
            for (int y = 0; y < 5; y++) {
                s = (b % 2 > 0 ? "+" : "-") + s;
                b = (byte) (b / 2);
            }
            System.out.print(s + " ");
        }
        System.out.println();
    }

    public void process2ndMessage(byte[] data, Message1Format message1) {
        String time = new String(data, 8, 12);
        String lane = "" + ((char) data[4]);
        String lap = "" + ((char) data[5]) + ((char) data[6]);

        System.out.println("--- 2nd ----------------");
        System.out.print("Lane: " + lane);
        System.out.print(", ");
        System.out.print("Lap:  " + lap);
        System.out.print(", ");
        System.out.println("Time: " + time);
        times.store(message1.getIndex(), message1.getTimeType(), message1.getEvent(), message1.getHeat(),
                toInt(lane) - 1, toTime(time), message1.getStatus());
    }

    public Message1Format processMessage(byte[] data, Message1Format message) {
        // System.out.println(StringTools.bytesToString2(data));
        // System.out.println(StringTools.bytesAsChar(data));
        if (data.length < 4) {
            System.out.println("Not enough data");
            return null;
        }
        if ((data[0] == 1) && (data[1] == 18) && (data[2] == 57)) {
            // System.out.println("Received Ping");
            return null;
        }

        if ((data[0] != 1) || (data[1] != 2) || (data[2] != 8)) {
            System.out.println("Unknown Header");
            return null;
        }
        if (data[3] == 10) {
            if (message != null) {
                process2ndMessage(data, message);
            }
        } else {
            return process1stMessage(data);
        }
        return null;
    }
}
