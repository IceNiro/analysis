package com.iceniro.ticket.dataProcess;

import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gan.jiangwei
 * @since 2018/9/3 0003.
 */
public class FileUitl {

    private final static Logger logger = LogManager.getLogger(FileUitl.class);
    static String path = "E:/stations";

    public static List<String> readFile() {
        try {
            List<String> lines = new ArrayList<String>();
            File file = new File(path);
            BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = is.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeFile(String content) {
        try {
            File file = new File(path);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.newLine();
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<String> list = readFile();
        for (String item : list) {
            String[] detail = item.split("\\|");
            for (int i = 0; i < detail.length; i = i + 5) {
                String name = detail[i + 1];
                String code = detail[i + 2];
                writeFile(code + ":" + name);
            }
        }
    }
}
