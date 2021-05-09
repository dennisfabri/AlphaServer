package de.dm.collector;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import de.df.jutils.gui.layout.FormLayoutUtils;

public class JMultiLabel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7584668506694301078L;
	private final JLabel[] text;

    public JMultiLabel(int size) {
        FormLayout layout = new FormLayout(FormLayoutUtils
                .createGrowingLayoutString(size, 1), "0dlu,fill:default");
        setLayout(layout);

        CellConstraints cc = new CellConstraints();
        text = new JLabel[size];
        for (int x = 0; x < size; x++) {
            text[x] = new JLabel();
            add(text[x], cc.xy(2 * x + 2, 2));
        }
    }

    void addText(String string) {
        for (int x = text.length - 1; x > 0; x--) {
            text[x].setText(text[x - 1].getText());
        }
        text[0].setText(string);
    }
}
