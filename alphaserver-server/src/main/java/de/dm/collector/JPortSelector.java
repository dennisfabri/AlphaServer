package de.dm.collector;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.TooManyListenersException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jutils.gui.util.WindowUtils;
import de.dm.comm.CommFactory;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

public final class JPortSelector extends JDialog {

    private static final long serialVersionUID = 2361900701695504086L;

    JComboBox<String> port1;
    JButton ok;

    private JPortSelector(JFrame parent) throws TooManyListenersException,
            IOException, PortInUseException, UnsupportedCommOperationException {
        super(parent, "Port ausw�hlen", ModalityType.APPLICATION_MODAL);

        this.setMinimumSize(new Dimension(200, 50));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        String[] portsx = CommFactory.getPorts();
        String[] ports = new String[portsx.length + 1];
        ports[0] = "";
        System.arraycopy(portsx, 0, ports, 1, portsx.length);
        port1 = new JComboBox<>(ports);

        ActionListener al = e -> {
            ok.setEnabled(port1.getSelectedIndex() > 0);
        };
        port1.addActionListener(al);

        ok = new JButton("Ok");
        ok.addActionListener(e -> {
            setVisible(false);
        });
        ok.setEnabled(false);

        setLayout(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu"));
        CellConstraints cc = new CellConstraints();
        add(new JLabel("Port"), cc.xy(2, 2));
        add(port1, cc.xy(4, 2));
        add(ok, cc.xyw(2, 4, 3, "right,fill"));
    }

    public String getPort() {
        return port1.getSelectedItem().toString();
    }

    public static String selectPort(JFrame parent)
            throws TooManyListenersException, IOException, PortInUseException,
            UnsupportedCommOperationException {
        JPortSelector ps = new JPortSelector(parent);
        ps.pack();
        WindowUtils.center(ps);
        ps.setVisible(true);
        return ps.getPort();
    }
}
