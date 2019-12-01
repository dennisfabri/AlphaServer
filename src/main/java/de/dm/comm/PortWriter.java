package de.dm.comm;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

public class PortWriter {

    private SerialPort   serialPort;
    private OutputStream outputStream;

    public PortWriter(String port, CommunicationMode mode) throws IOException, PortInUseException, UnsupportedCommOperationException {
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier portId = null;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(port)) {
                    System.out.println("Found port: " + port);
                    break;
                }
            }
            portId = null;
        }

        if (portId == null) {
            throw new IOException("port " + port + " not found.");
        }

        serialPort = (SerialPort) portId.open("PortWriter", 2000);
        outputStream = serialPort.getOutputStream();
        switch (mode) {
        case ARES21:
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
            break;
        case Quantum:
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            break;
        }
        serialPort.notifyOnOutputEmpty(true);

        System.out.println("Writing to port: " + port);
    }

    public void write(byte b) throws IOException {
        synchronized (outputStream) {
            outputStream.write(b);
        }
    }

    public void write(byte[] b) throws IOException {
        synchronized (outputStream) {
            outputStream.write(b);
        }
    }

    public void close() {
        synchronized (outputStream) {
            serialPort.close();
            outputStream = null;
            serialPort = null;
        }
    }
}
