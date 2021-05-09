package de.dm.collector;

import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.TooManyListenersException;

import de.df.jutils.gui.util.DesignInit;
import de.dm.comm.*;

public class DataSender {

    public static boolean[] finished = new boolean[2];

    public static void main(String[] args) throws TooManyListenersException,
            IOException, PortInUseException, UnsupportedCommOperationException {
        DesignInit.init();
        String[] ports = JPortsSelector.selectPorts();
        Thread t1 = new Thread(new Sender(ports[0], 0));
        // Thread t2 = new Thread(new Sender(ports[1], 1));
        t1.start();
        // t2.start();
        finished[1] = true;
        while (!(finished[0] & finished[1])) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO: Implementation
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public static class Sender implements Runnable {
        private String     port;
        private int        index;
        private PortReader pr;
        private SerialPort sp;

        public Sender(String p, int i) throws TooManyListenersException,
                IOException, PortInUseException,
                UnsupportedCommOperationException {
            port = p;
            index = i;
            pr = new NRJavaSerialPortReader(port, CommunicationMode.ARES21);
            sp = pr.getPort();
        }

        @Override
        public void run() {
            try {
                sendData(sp);
            } catch (TooManyListenersException e) {
                // TODO: Implementation
                e.printStackTrace();
            } catch (IOException e) {
                // TODO: Implementation
                e.printStackTrace();
            } catch (PortInUseException e) {
                // TODO: Implementation
                e.printStackTrace();
            } catch (UnsupportedCommOperationException e) {
                // TODO: Implementation
                e.printStackTrace();
            }
            finished[index] = true;
        }
    }

    public static void sendData(SerialPort sp)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {
        File dir = new File("data/DM");
        if (dir.exists() && dir.isDirectory()) {
            System.out
                    .println("Directory " + dir.getAbsolutePath() + " found.");
            String[] files = dir.list();
            Arrays.sort(files);
            for (String file : files) {
                if (file.endsWith(".dat")) {
                    System.out.println(dir.getAbsolutePath() + File.separator + file);
                    FileInputStream fis = new FileInputStream(dir
                            .getAbsolutePath()
                            + File.separator + file);
                    int byt = fis.read();
                    while (byt >= 0) {
                        sp.getOutputStream().write(byt);
                        byt = fis.read();
                    }
                    fis.close();
                }
            }
        } else {
            Random r = new Random();
            for (int x = 0; x < 1000; x++) {
                byte b = (byte) r.nextInt(120);
                sp.getOutputStream().write(b);
            }
        }
    }
}
