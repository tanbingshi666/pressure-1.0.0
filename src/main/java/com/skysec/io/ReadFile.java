package com.skysec.io;

import com.skysec.queue.MessageQueue;
import com.skysec.queue.Termination;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ReadFile {

    private long count = 0L;

    private final String path;
    private final File file;

    public ReadFile(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public void read() {
        System.out.println("开始读取 path = " + path + " 路径数据...");
        long startTs = System.currentTimeMillis();
        doRead(file);
        MessageQueue.put(new Termination());
        long endTs = System.currentTimeMillis();
        System.out.println("开始读取 path = " + path + " 路径数据总共 " + count + " 花费时间 " + (endTs - startTs));
    }

    private void doRead(File file) {
        if (file.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    MessageQueue.put(line);
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File[] fs = file.listFiles();
            if (fs == null) {
                return;
            }
            for (File f : fs) {
                if (f.isDirectory())
                    doRead(f);
                if (f.isFile()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            MessageQueue.put(line);
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
