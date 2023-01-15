package de.dm.comm;

import java.io.IOException;
import java.util.LinkedList;

public class ByteContainer {

    private static final int SIZE = 1024;

    private LinkedList<byte[]> buffer = new LinkedList<>();
    private LinkedList<byte[]> data = new LinkedList<>();
    private int readpos;
    private int writepos = SIZE;

    public ByteContainer() {
        // nothing to do
    }

    public void write(byte byt) {
        // System.out.print(new String(new byte[] { byt }));
        synchronized (data) {
            byte[] d;
            if (writepos >= SIZE) {
                if (buffer.size() > 0) {
                    d = buffer.removeFirst();
                } else {
                    d = new byte[SIZE];
                }
                data.addLast(d);
                writepos = 0;
            } else {
                d = data.getLast();
            }
            d[writepos] = byt;
            writepos++;
        }
    }

    public void write(byte... bytes) {
        synchronized (data) {
            for (byte aByte : bytes) {
                write(aByte);
            }
        }
    }

    public byte read() throws IOException {
        byte result;
        synchronized (data) {
            if (data.isEmpty()) {
                throw new IOException("No data available");
            }
            byte[] d = data.getFirst();
            if ((data.size() == 1) && (readpos == writepos)) {
                throw new IOException("No data available");
            }
            result = d[readpos];
            d[readpos] = 0;
            readpos++;
            if (readpos >= SIZE) {
                readpos = 0;
                data.removeFirst();
                if (buffer.size() < 5) {
                    buffer.addLast(d);
                }
            }
        }
        return result;
    }

    public boolean isAvailable() {
        return (data.size() > 1)
                || ((data.size() == 1) && (readpos < writepos));
    }
}
