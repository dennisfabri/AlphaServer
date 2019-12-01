package de.dm.collector;

import gnu.io.*;

import java.awt.event.*;
import java.io.IOException;
import java.util.TooManyListenersException;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import de.df.jutils.gui.util.*;
import de.dm.comm.CommFactory;

public class JPortsSelector extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -273137907841131901L;
	JComboBox<String> port1;
    JComboBox<String> port2;
    JButton   ok;

    private JPortsSelector(JFrame parent) throws TooManyListenersException,
            IOException, PortInUseException, UnsupportedCommOperationException {
        super("Ports auswählen");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        String[] portsx = CommFactory.getPorts();
        String[] ports = new String[portsx.length + 1];
        ports[0] = "";
        System.arraycopy(portsx, 0, ports, 1, portsx.length);
        port1 = new JComboBox<String>(ports);
        port2 = new JComboBox<String>(ports);

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled((port1.getSelectedIndex() != port2
                        .getSelectedIndex())
                        && (port1.getSelectedIndex() > 0));
            }
        };
        port1.addActionListener(al);
        port2.addActionListener(al);

        ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        ok.setEnabled(false);

        setLayout(new FormLayout("4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu"));
        CellConstraints cc = new CellConstraints();
        add(new JLabel("Port 1"), cc.xy(2, 2));
        add(port1, cc.xy(4, 2));
        add(new JLabel("Port 2"), cc.xy(2, 4));
        add(port2, cc.xy(4, 4));
        add(ok, cc.xyw(2, 6, 3, "right,fill"));
    }

    public String[] getPorts() {
        return new String[] { port1.getSelectedItem().toString(),
                port2.getSelectedItem().toString() };
    }

    public static String[] selectPorts() throws TooManyListenersException,
            IOException, PortInUseException, UnsupportedCommOperationException {
        return selectPorts(null);
    }

    private static String[] selectPorts(JFrame parent)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {
        JPortsSelector ps = new JPortsSelector(parent);
        ps.pack();
        WindowUtils.center(ps);
        EDTUtils.setVisible(ps, true);
        while (ps.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
        return ps.getPorts();
    }
}
