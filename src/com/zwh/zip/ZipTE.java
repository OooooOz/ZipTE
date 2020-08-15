package com.zwh.zip;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * zip解压示例
 */
public class ZipTE {

	private static File xmlFile;

	private static String sequence;

    public static String path;

	private static int count;

	private static int corePoolSize;

	private static int maximumPoolSize;

	private static int workQueueNum;


    public static void main(String[] args) throws IOException, InterruptedException {
        initParameter();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize, 200
                , TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(workQueueNum));
        File sourcesPath = new File(path);
        File[] listFiles = sourcesPath.listFiles();
        CountDownLatch countDownLatch = new CountDownLatch(listFiles.length);
        //文件解压
        for (File file: listFiles) {
            if (!file.getName().endsWith("hwics")){
                file.delete();
                countDownLatch.countDown();
                continue;
            }
            executor.execute(new TaskUnZip(countDownLatch,file));
        }
        countDownLatch.await();
        System.out.println("======main========");
        //文件处理：处理完一个压缩一个
        listFiles = sourcesPath.listFiles();
        XMLWriter writer = null;
        String ownerFlag = null;
        countDownLatch = new CountDownLatch(listFiles.length);
        for (File file:listFiles) {
            File findZipFile = findZipFile(file);
            if (findZipFile == null) {
                continue;
            }

            if (count<10){
                ownerFlag = sequence + "00" +count;
            }else if (count<100){
                ownerFlag = sequence + "0" + count;
            }else {
                ownerFlag = sequence + count;
            }

            SAXReader saxReader = new SAXReader();
            try {
                Document document = saxReader.read(findZipFile);
                Element rootElement = document.getRootElement();
                List<Element> elements = rootElement.elements();
                int cow = 1;
                for (Element element : elements) {
                    if (element.getName().equals("language")) {
                        break;
                    }
                    element.setText(cow+ownerFlag);
                    cow++;
                }
                OutputFormat format = OutputFormat.createPrettyPrint();
                format.setEncoding("utf-8");
                writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(xmlFile)));
                writer.write(document);
                writer.flush();
                writer.close();
                count++;
                String zipSavePath = path + File.separator + ownerFlag + ".hwics";
                executor.execute(new TaskCompress(countDownLatch,file,zipSavePath));
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.await();
        executor.shutdown();
        System.out.println("执行完成");
    }

    private static void initParameter() {
        Properties properties = new Properties();
        try {
            InputStreamReader reader = new InputStreamReader(ZipTE.class.getClassLoader().getResourceAsStream("zip.properties"), "UTF-8");
            properties.load(reader);
            path = properties.getProperty("path");
            sequence = properties.getProperty("sequence");
            count = Integer.parseInt(properties.getProperty("count"));
            corePoolSize = Integer.parseInt(properties.getProperty("corePoolSize"));
            maximumPoolSize = Integer.parseInt(properties.getProperty("maximumPoolSize"));
            workQueueNum = Integer.parseInt(properties.getProperty("workQueueNum"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File findZipFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.getName().equals("profile.xml")) {
					xmlFile = f;
					break;
				}
				if (!f.isDirectory()) {
					continue;
				}
				findZipFile(f);
			}
		}
		return xmlFile;
	}
}
