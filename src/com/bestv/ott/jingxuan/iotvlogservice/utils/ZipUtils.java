package com.bestv.ott.jingxuan.iotvlogservice.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Java utils
 *
 * @author ouy
 */
public class ZipUtils {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte 

    public  static List<File> zipFiles(File file, File zipFile) throws IOException {
    	if(!zipFile.exists()){
    		zipFile.createNewFile();
    	}
    	
    	List<File> successFile=new ArrayList<File>();
        ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(  zipFile), BUFF_SIZE));         
        zipFile(file, zipout, "",successFile);
        zipout.close();
        return successFile;
    }

    public  static  void zipFile(File resFile, ZipOutputStream zipout, String rootpath , List<File> successFile)
            throws FileNotFoundException, IOException {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName();
        rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                zipFile(file, zipout, rootpath,successFile);
            }
        } else {
            byte buffer[] = new byte[BUFF_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(resFile),  BUFF_SIZE);
            zipout.putNextEntry(new ZipEntry(rootpath));
            int realLength;
            while ((realLength = in.read(buffer)) != -1) {
                zipout.write(buffer, 0, realLength);
            }
            in.close();
            zipout.flush();
            zipout.closeEntry();
            successFile.add( resFile ); 
			// /resFile.delete();
        }
    }
}