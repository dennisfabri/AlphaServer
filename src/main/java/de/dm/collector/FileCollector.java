package de.dm.collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileCollector {
    public static void main(String[] args) throws IOException {
        File dir = new File("data");
        String[] files = dir.list();
        Arrays.sort(files);
        FileOutputStream fos = new FileOutputStream("Freitag.dat");
        long sum = 0;
        for (String file1 : files) {
            System.out.println(file1);
            File file = new File("data\\" + file1);
            byte[] data = new byte[1024];
            long size = 0;
            FileInputStream fis = new FileInputStream(file);
            while (file.length() > size) {
                // byte[] data = new byte[1024];
                if (file.length() < size + 1024) {
                    data = new byte[(int) (file.length() - size)];
                }
                fis.read(data);
                fos.write(data);
                size += data.length;
            }
            sum += size;
            fis.close();
        }
        fos.close();
        File f = new File("Freitag.dat");
        System.out.println(f.length() + " " + sum);
    }
}
