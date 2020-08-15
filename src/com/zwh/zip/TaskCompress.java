package com.zwh.zip;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Deacription 多线程压缩文件夹
 **/
public class TaskCompress implements Runnable{

    private CountDownLatch countDownLatch;

    private File file;

    private String zipSavePath;

    public TaskCompress(CountDownLatch countDownLatch, File file, String zipSavePath) {
        this.countDownLatch=countDownLatch;
        this.file = file;
        this.zipSavePath = zipSavePath;
    }


    @Override
    public void run() {
        zipCompress(zipSavePath, file);
        deleteFile(file);
        System.out.println("==**==:"+Thread.currentThread().getName());
        countDownLatch.countDown();
    }

    public static void zipCompress(String zipSavePath, File sourcesFile) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipSavePath));
            compress(zos, sourcesFile, sourcesFile.getName());
            zos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compress(ZipOutputStream zos, File sourcesFile, String fileNme) throws IOException {
        if (sourcesFile.isDirectory()){
            File[] fileList = sourcesFile.listFiles();
            if (fileList.length == 0){
                fileNme = fileNme.substring(fileNme.indexOf("/")+1,fileNme.length());
                zos.putNextEntry(new ZipEntry(fileNme+"/"));
            }else {
                for (File file: fileList) {
                    compress(zos,file,fileNme+"/"+file.getName());
                }
            }
        }else {
            if (!sourcesFile.exists()){
                zos.putNextEntry(new ZipEntry("/"));
                zos.closeEntry();
            }else {
                fileNme = fileNme.substring(fileNme.indexOf("/")+1,fileNme.length());
                zos.putNextEntry(new ZipEntry(fileNme));
                FileInputStream fis = new FileInputStream(sourcesFile);
                byte[] bytes = new byte[4 * 1024];
                int len;
                while ((len=fis.read(bytes))!=-1){
                    zos.write(bytes,0,len);
                }
                zos.closeEntry();
                fis.close();
            }
        }
    }

    private static boolean deleteFile(File dirFIle) {
        if (!dirFIle.exists()){
            return false;
        }
        if (dirFIle.isFile()){
            return dirFIle.delete();
        }else {
            for (File file:dirFIle.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFIle.delete();
    }

}
