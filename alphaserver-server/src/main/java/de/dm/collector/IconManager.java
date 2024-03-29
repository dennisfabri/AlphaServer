/*
 * Created on 17.02.2004
 */
package de.dm.collector;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import de.df.jutils.gui.util.AIconBundle;

/**
 * @author Dennis Fabri
 * @date 17.02.2004
 */
public final class IconManager {

    static ResourceBundle names;

    private static AIconBundle icons;

    private static ImageIcon[] titles;
    private static LinkedList<Image> iconimages;

    static {
        titles = null;
        iconimages = null;

        names = ResourceBundle.getBundle("alphaservericons");
        icons = new ManagerIconBundle();
    }

    private IconManager() {
        // Never called
    }

    private static synchronized ImageIcon getIcon(String name, boolean small) {
        try {
            synchronized (icons) {
                int size = small ? 16 : 32;
                return icons.getIcon(name, size);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    private static synchronized ImageIcon[] getTitleIcons() {
        if (titles != null) {
            return titles;
        }
        titles = new ImageIcon[] { getImageIcon("alphaserver-16"), getImageIcon("alphaserver-32"),
                getImageIcon("alphaserver-48"), getImageIcon("alphaserver-128"), getImageIcon("alphaserver-256") };
        return titles;
    }

    public static synchronized ImageIcon getImageIcon(String name) {
        try {
            synchronized (icons) {
                return new ImageIcon(Toolkit.getDefaultToolkit().getImage(getUserDir() + names.getString(name)));
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    private static String getUserDir() {
        String dir = System.getProperty("user.dir");
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    public static synchronized List<Image> getTitleImages() {
        if (iconimages == null) {
            ImageIcon[] iconx = getTitleIcons();
            if (iconx == null) {
                return null;
            }
            iconimages = new LinkedList<>();
            for (ImageIcon icon : iconx) {
                iconimages.addLast(icon.getImage());
            }
        }
        return iconimages;
    }

    public static synchronized Image getImage(String name) {
        ImageIcon icon = getImageIcon(name);
        if (icon == null) {
            return null;
        }
        return icon.getImage();
    }

    public static synchronized ImageIcon getSmallIcon(String name) {
        return getIcon(name, true);
    }

    public static synchronized ImageIcon getBigIcon(String name) {
        return getIcon(name, false);
    }

    public static synchronized ImageIcon getGrayIcon(String name, boolean small) {
        return toGrayIcon(getIcon(name, small));
    }

    public static synchronized ImageIcon toGrayIcon(ImageIcon icon) {
        BufferedImage i = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);

        i.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        return new ImageIcon(i);
    }

    public static AIconBundle getIconBundle() {
        return icons;
    }

    static final class ManagerIconBundle extends AIconBundle {

        @Override
        protected ImageIcon readIcon(String name, int size) {
            String id = names.getString(name);
            if (id == null) {
                throw new NullPointerException();
            }
            String fullname = "/images/" + size + "x" + size + "/" + id;
            return new ImageIcon(IconManager.class.getResource(fullname));
        }

        private ManagerIconBundle() {
        }
    }
}
