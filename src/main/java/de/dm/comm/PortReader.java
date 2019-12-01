package de.dm.comm;

import java.io.IOException;

import gnu.io.SerialPort;

public interface PortReader {

    SerialPort getPort();

    void close();

    void addDataListener(DataListener dl);

    boolean isAvailable();

    byte read() throws IOException;

}