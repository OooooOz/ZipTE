package com.zwh.zip;

import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Deacription 多线程解压缩
 **/
public class TaskUnZip implements Runnable{

    private CountDownLatch countDownLatch;

    private File file;

    public TaskUnZip(CountDownLatch countDownLatch, File file) {
        this.countDownLatch=countDownLatch;
        this.file = file;
    }


    @Override
    public void run() {
        try {
            unZip(file,ZipTE.path);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("====:"+Thread.currentThread().getName());
        countDownLatch.countDown();
    }

    public static void unZip(String sourceFilename, String targetDir) throws IOException {
        unZip(new File(sourceFilename), targetDir);
    }

    /**
     * 将sourceFile解压到targetDir
     * @param sourceFile
     * @param targetDir
     * @throws RuntimeException
     */
    private static void unZip(File sourceFile, String targetDir) throws IOException {
        String newDir = sourceFile.getName().substring(0,sourceFile.getName().indexOf("."));
        String filePath = sourceFile.getParent();
        File newFile = new File(filePath + File.separator + newDir);
        if (!newFile.exists()){
            newFile.mkdir();
        }
        targetDir = newFile.getPath();
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("cannot find the file = " + sourceFile.getPath());
        }
        ZipFile zipFile = null;
        try{
            zipFile = new ZipFile(sourceFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    String dirPath = targetDir + "/" + entry.getName();
                    createDirIfNotExist(dirPath);
                } else {
                    File targetFile = new File(targetDir + "/" + entry.getName());
                    createFileIfNotExist(targetFile);
                    InputStream is = null;
                    FileOutputStream fos = null;
                    try {
                        is = zipFile.getInputStream(entry);
                        fos = new FileOutputStream(targetFile);
                        int len;
                        byte[] buf = new byte[4*1024];
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                    }finally {
                        try{
                            fos.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        try{
                            is.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            if(zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createFileIfNotExist(File file) throws IOException {
        createParentDirIfNotExist(file);
        file.createNewFile();
    }

    public static void createParentDirIfNotExist(File file){
        createDirIfNotExist(file.getParentFile());
    }

    public static void createDirIfNotExist(String path){
        File file = new File(path);
        createDirIfNotExist(file);
    }

    public static void createDirIfNotExist(File file){
        if(!file.exists()){
            file.mkdirs();
        }
    }
}
