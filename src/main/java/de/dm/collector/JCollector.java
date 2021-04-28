package de.dm.collector;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;
import org.lisasp.swing.filechooser.jfx.FileChooserJFX;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.DesignInit;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.util.StringTools;
import de.dm.ares.data.Heat;
import de.dm.ares.data.event.HeatListener;
import de.dm.ares.file.FileReader;
import de.dm.comm.CommunicationMode;
import de.dm.comm.DataListener;
import de.dm.comm.NRJavaSerialPortReader;
import de.dm.comm.PortReader;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

public class JCollector extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -5714209912532213413L;
    private JLabel label1 = new JLabel();
    private JPanel port1;
    private JLabel time1 = new JLabel();
    private CollectorDataListener dl1;
    private JLabel counter1 = new JLabel();

    private AlphaHttpServer http;
    MessageReader mr;

    private String name;
    JButton connect;
    JComboBox<String> mode;

    JTextArea log = new JTextArea();

    StringBuffer sb = new StringBuffer();

    private JCollector() {
        super("Alpha-Server");
        setMinimumSize(new Dimension(400, 250));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImages(IconManager.getTitleImages());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        mode = new JComboBox<String>(new String[] { "ARES21", "Quantum" });
        mode.setSelectedIndex(0);

        connect = new JButton("Connect");
        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
            }
        });

        log.setAutoscrolls(true);
        log.setEditable(false);
        JScrollPane scroller = new JScrollPane(log);
        scroller.setBorder(BorderUtils.createLabeledBorder("Log"));

        port1 = createBox(label1, time1, counter1);
        CellConstraints cc = new CellConstraints();
        add(port1, cc.xyw(2, 2, 9));
        add(scroller, cc.xyw(2, 4, 9));

        add(new JLabel("Source:"), cc.xy(2, 6));
        add(mode, cc.xy(4, 6));
        add(connect, cc.xy(6, 6));
        add(close, cc.xy(10, 6));
    }

    private CommunicationMode getCommunicatonMode() {
        switch (mode.getSelectedIndex()) {
        case 0:
            return CommunicationMode.ARES21;
        case 1:
            return CommunicationMode.Quantum;
        default:
            return CommunicationMode.ARES21;
        }
    }

    void close() {
        if (dl1 != null) {
            try {
                dl1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private JPanel createBox(JLabel label, JLabel time, JLabel counter) {
        JPanel p = new JPanel(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu"));
        CellConstraints cc = new CellConstraints();
        p.add(new JLabel("Data"), cc.xy(2, 2));
        p.add(label, cc.xy(4, 2));
        p.add(new JLabel("Time"), cc.xy(2, 4));
        p.add(time, cc.xy(4, 4));
        p.add(new JLabel("Counter"), cc.xy(2, 6));
        p.add(counter, cc.xy(4, 6));
        return p;
    }

    private void start()
            throws TooManyListenersException, IOException, PortInUseException, UnsupportedCommOperationException {
        port1.setBorder(BorderUtils.createLabeledBorder("No Connection"));

        // TODO: Implement better version
        name = FileChooserUtils.saveFile(null, "Datei wählen", new SimpleFileFilter("Elektronische Zeitnahme", "ez"));

        mr = new MessageReader();
        Heat[] heats = readHeats();
        if (heats != null) {
            mr.getTimeStorage().setHeats(heats);
        }

        http = new AlphaHttpServer(mr.getTimeStorage());
        http.start();

        pack();
        if (getWidth() < 200) {
            setSize(200, getHeight());
        }
        WindowUtils.center(this);
        setVisible(true);
    }

    void heatToLog(Heat heat) {
        Document d = log.getDocument();
        if (d.getLength() > 1000) {
            try {
                d.remove(1000, d.getLength() - 1000);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        sb.append(heat.toString());
        sb.append("\n-- -- -- -- -- --\n");
        try {
            d.insertString(0, sb.toString(), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        sb.setLength(0);
    }

    public static void run() {
        try {
            main(null);
        } catch (Exception ex) {

        }
    }

    public static void main(String[] args)
            throws TooManyListenersException, IOException, PortInUseException, UnsupportedCommOperationException {
        DesignInit.init();
        FileChooserUtils.initialize(new FileChooserJFX());
        JCollector collector = new JCollector();
        collector.start();
    }

    private final class CollectorDataListener implements DataListener {

        private final PortReader in;
        private final JLabel text;
        // private final FileOutputStream fos;
        private final Collector coll;
        private final JLabel time;
        private long last = 0;
        private final JLabel counter;
        private int count = 0;
        private final OutputStream os;

        public CollectorDataListener(PortReader p, JLabel i, JLabel time, JLabel c, OutputStream o, String name)
                throws IOException {
            in = p;
            this.time = time;
            counter = c;
            name = null;
            text = i;
            os = o;
            coll = new Collector(name);
        }

        @Override
        public void dataAvailable() {
            while (in.isAvailable()) {
                try {
                    byte b = in.read();
                    long curr = System.currentTimeMillis() / 1000;
                    coll.collect(b);
                    text.setText(StringTools.byteToHex(b));
                    if (os != null) {
                        os.write(b);
                    }
                    if (mr != null) {
                        try {
                            mr.push(b);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (curr != last) {
                        time.setText(toTime(curr));
                        last = curr;
                    }
                    count++;
                    counter.setText("" + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String toTime(long curr) {
            curr = curr % (60 * 60 * 24);
            int h = (int) (curr / (60 * 60));
            int m = (int) ((curr / (60)) % 60);
            int s = (int) (curr % 60);
            return "" + h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
        }

        public void close() throws IOException {
            // fos.close();
            coll.close();
        }
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
            } catch (FileNotFoundException fnfe) {
                // Nothing to do
            } catch (IOException io) {
                io.printStackTrace();
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

    public static void readFile(JCollector collector, MessageReader mr) {
        while (true) {
            System.out.println("Read");
            mr.getTimeStorage().clear();
            // FileReader.read(FileReader.file1, 1, mr.getTimeStorage());
            // FileReader.read("Z:\\DM-Einzel1\\LSTRSLT.TXT", 0, mr.getTimeStorage());
            // FileReader.read("Z:\\DM-Einzel2\\LSTRSLT.TXT", 1, mr.getTimeStorage());

            // FileReader.read("Z:\\DM-Mannschaft1\\LSTRSLT.TXT", 0, mr.getTimeStorage());
            // FileReader.read("Z:\\DM-Mannschaft2\\LSTRSLT.TXT", 1, mr.getTimeStorage());
            // FileReader.read("Z:\\DM-Mannschaft3\\LSTRSLT.TXT", 2, mr.getTimeStorage());
            // FileReader.read("Z:\\DM-Mannschaft4\\LSTRSLT.TXT", 3, mr.getTimeStorage());
            FileReader.read("..\\data\\LSTRSLT-DM2016Mannschaft1.TXT", 0, mr.getTimeStorage());
            FileReader.read("..\\data\\LSTRSLT-DM2016Mannschaft2.TXT", 1, mr.getTimeStorage());
            FileReader.read("..\\data\\LSTRSLT-DM2016Mannschaft3.TXT", 2, mr.getTimeStorage());
            FileReader.read("..\\data\\LSTRSLT-DM2016Mannschaft4.TXT", 3, mr.getTimeStorage());
            collector.writeHeats();
            try {
                Thread.sleep(60 * 1000);
            } catch (Exception ex) {
                // Nothing to do
            }
        }
    }

    void connect() {
        connectI();
    }

    void connectI() {
        try {
            String port = JPortSelector.selectPort(this);
            System.out.println(port);

            port1.setBorder(BorderUtils.createLabeledBorder(port));

            PortReader pr = null;

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

            if (port.length() > 0) {
                pr = new NRJavaSerialPortReader(port, getCommunicatonMode());
            }
            if (pr != null) {
                String n = port.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

                String userhome = org.apache.commons.lang3.SystemUtils.getUserHome().getCanonicalPath();

                ArrayList<String> pathparts = new ArrayList<>();
                pathparts.add(".JAuswertungHome");
                pathparts.add("AlphaServer");

                new File(
                        Paths.get(userhome, pathparts.toArray(new String[pathparts.size()])).toFile().getAbsolutePath())
                                .mkdirs();

                pathparts.add("binary-" + n + ".dat");

                dl1 = new CollectorDataListener(pr, label1, time1, counter1, new FileOutputStream(
                        Paths.get(userhome, pathparts.toArray(new String[pathparts.size()])).toFile().getAbsolutePath(),
                        true), port);
                pr.addDataListener(dl1);

                return;
            }
        } catch (PortInUseException piu) {
            piu.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }
}