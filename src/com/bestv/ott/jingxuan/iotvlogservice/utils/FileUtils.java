package com.bestv.ott.jingxuan.iotvlogservice.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtils {
	public static String readByInputStream(InputStream is) {
		StringBuffer sb = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			sb = new StringBuffer();
			String line = "";
			while (null != (line = br.readLine())) {
				sb.append(line);
			}
		} catch (IOException e) {
			sb = null;
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != isr) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (null != sb) {
			return sb.toString();
		}

		return null;
	}
	
	public static void deleteDir(String path, boolean bDelRoot) {
		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()) {
			File[] tmp = dir.listFiles();
			if (null != tmp){
				for (int i = 0; i < tmp.length; i++) {
					if (tmp[i].isDirectory()) {
						deleteDir(path + "/" + tmp[i].getName(), true);
					} else {
						tmp[i].delete();
					}
				}
			}
			if (bDelRoot){
				dir.delete();
			}
		}
	}
	
	public static boolean fileExisted(String path){
		return new File(path).exists();
	}
	
	public static void deleteFile(String file) {
		File f=new File(file);
		f.delete();
	}
	
	public static BufferedOutputStream getOutputStream(File file)
			throws IOException {
		return new BufferedOutputStream(new FileOutputStream(file),
				64*1024);
	}
	
	public static BufferedOutputStream getOutputStream(String file)
			throws IOException {
		return getOutputStream(new File(file));
	}
	
	public static void close(OutputStream out) {
		if (null != out) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out = null;
		}
	}
	
	public static void close(InputStream in) {
		if (null != in) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			in = null;
		}
	}
	
	private static void createDirIfNotExist(File file) {
		if (null == file) {
			return;
		}
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		}
	}

	public static void createDirIfNotExist(String filePath) {
		File file = new File(filePath);
		createDirIfNotExist(file);
	}
	
	public static void writeFile(String str, String descFile, boolean append)
			throws Exception {
		if ((null==str) || (null==descFile)) {
			return;
		}
		
		createDirIfNotExist(descFile);

		BufferedOutputStream out = null;

		try {
			byte[] src = str.getBytes("UTF-8");
			out = new BufferedOutputStream(new FileOutputStream(descFile, append),
					1024 * 64);
			out.write(src);
			out.flush();
		} finally {
			close(out);
		}
	}
	
	public static void appendFile(String str, String descFile) throws Exception{
		writeFile(str, descFile, true);
	}

}
