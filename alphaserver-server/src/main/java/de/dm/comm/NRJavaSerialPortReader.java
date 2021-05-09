package de.dm.comm;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TooManyListenersException;

public class NRJavaSerialPortReader implements PortReader {

    private SerialPort serialPort;
    InputStream        inputStream;
    ByteContainer      data = new ByteContainer();

    public NRJavaSerialPortReader(String port, CommunicationMode mode) throws TooManyListenersException, IOException, PortInUseException, UnsupportedCommOperationException {
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

        serialPort = (SerialPort) portId.open("PortReader", 2000);
        inputStream = serialPort.getInputStream();
        serialPort.addEventListener(new SerialPortEventListener() {
            @Override
            public void serialEvent(SerialPortEvent event) {
                switch (event.getEventType()) {
                case SerialPortEvent.BI:
                case SerialPortEvent.OE:
                case SerialPortEvent.FE:
                case SerialPortEvent.PE:
                case SerialPortEvent.CD:
                case SerialPortEvent.CTS:
                case SerialPortEvent.DSR:
                case SerialPortEvent.RI:
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;
                case SerialPortEvent.DATA_AVAILABLE:
                    byte[] readBuffer = new byte[1024];
                    try {
                        while (inputStream.available() > 0) {
                            int numBytes = inputStream.read(readBuffer);
                            for (int x = 0; x < numBytes; x++) {
                                data.write(readBuffer[x]);
                            }
                            fireDataAvailableNotification();
                        }
                    } catch (IOException e) {
                        // Nothing to do
                    }
                    break;
                }
            }
        });

        serialPort.notifyOnDataAvailable(true);
        switch (mode) {
        case ARES21:
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
            break;
        case Quantum:
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            break;
        }
        System.out.println("Reading from port: " + port);
    }
    
    @Override
    public SerialPort getPort() {
        return serialPort;
    }

    @Override
    public void close() {
        serialPort.removeEventListener();
        serialPort.close();
        inputStream = null;
        serialPort = null;
    }

    private LinkedList<DataListener> listeners = new LinkedList<DataListener>();

    @Override
    public void addDataListener(DataListener dl) {
        listeners.addLast(dl);
    }

    @Override
    public boolean isAvailable() {
        return data.isAvailable();
    }

    @Override
    public byte read() throws IOException {
        return data.read();
    }

    void fireDataAvailableNotification() {
        for (DataListener listener : listeners) {
            try {
                listener.dataAvailable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}