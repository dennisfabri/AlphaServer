package de.dm.comm;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TooManyListenersException;

public final class CommFactory {

    private CommFactory() {
        // hide constructor
    }

    public static CommPort openPort(String port)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getName().equals(port)) {
                return portId.open("PortReader", 2000);
            }
        }
        return null;
    }

    public static SerialPort openSerialPort(String port)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(port)) {
                    return (SerialPort) portId.open("PortReader", 2000);
                }
            }
        }
        return null;
    }

    public static void listPorts(PrintStream ps)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {

        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            switch (portId.getPortType()) {
            case CommPortIdentifier.PORT_I2C:
                ps.print("I2C:          ");
                break;
            case CommPortIdentifier.PORT_PARALLEL:
                ps.print("Parallel:     ");
                break;
            case CommPortIdentifier.PORT_RAW:
                ps.print("Raw:          ");
                break;
            case CommPortIdentifier.PORT_RS485:
                ps.print("RS485:        ");
                break;
            case CommPortIdentifier.PORT_SERIAL:
                ps.print("RS232:        ");
                break;
            default:
                ps.print("Unknown type: ");
                break;
            }
            ps.print(portId.getName());
            ps.print("\n");
        }
    }

    public static String[] getPorts()
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {

        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

        LinkedList<String> ports = new LinkedList<String>();
        CommPortIdentifier portId;
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            ports.addLast(portId.getName());
        }
        return ports.toArray(new String[ports.size()]);
    }
}
