package com.bestv.ott.jingxuan.iotvlogservice.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Enumeration;

import org.json.JSONObject;

import com.bestv.ott.jingxuan.iotvlogservice.beans.JsonResult;

import android.util.Log;

public class utils {
	static boolean mbLOGD=true;
	static boolean mbLOGI=true;
	static boolean mbLOGW=true;
	static boolean mbLOGE=true;
	static boolean mbPrintByteArray=true;
	
	public static String LOG_UPLOAD_VERSION = "002";
	public static String LOG_SEPARATOR = "|";
	public static String LOG_GROUP_SEPARATOR = "\r";
	
	public static boolean POST_TEST = false;
	public static String POST_URL_TEST = "http://10.61.7.22";
	
	public static String FILE_SEPARATOR = File.separator;
	
	public static String CHARSET = "UTF-8";
	public static String LOG_UPLOAD_ADDRESS = "http://logcloud.bbtv.cn";
	public static String BESTV_CONF_PATH = "BESTV_CONF_PATH";
	
	public static void setPrintByteArrayFlag(boolean flag){
		mbPrintByteArray = flag;
	}
	
	public static void closeLog(){
		mbLOGD = false;
		mbLOGI = false;
		mbLOGW = false;
		mbLOGE = false;
	}
	
	public static void openLog(){
		mbLOGD = true;
		mbLOGI = true;
		mbLOGW = true;
		mbLOGE = true;
	}
	
	public static int LOGD(String tag, String log){
		int ret=0;
		if (mbLOGD){
			/*
			Time t=new Time();
			t.setToNow();
			String time="(" + t.hour + ":" + t.minute + ":" + t.second + ")";
			ret = Log.d(time + " " + tag + "[" + Thread.currentThread().getId() + "]", log);
			*/
			ret = Log.d(tag, log);
		}
		return ret;
	}
	
	public static int LOGI(String tag, String log){
		int ret=0;
		if (mbLOGI){
			ret = Log.i(tag, log);
		}
		return ret;
	}
	
	public static int LOGW(String tag, String log){
		int ret=0;
		if (mbLOGW){
			ret = Log.w(tag, log);
		}
		return ret;
	}
	
	public static int LOGE(String tag, String log){
		int ret=0;
		if (mbLOGE){
			ret = Log.e(tag, log);
		}
		return ret;
	}
	
	
	public static void intToByteArray(int source, byte dest[], int offset){
		if (null == dest) return;
		
		if (dest.length < 4+offset){
			throw new IndexOutOfBoundsException();
		}
		
		dest[0+offset] = (byte)(source>>24 & 0xFF);
		dest[1+offset] = (byte)(source>>16 & 0xFF);
		dest[2+offset] = (byte)(source>>8 & 0xFF);
		dest[3+offset] = (byte)(source & 0xFF);		
	}
	
	public static int stringToByteArray(String source, byte dest[], int offset){
		int len=source.length();
		if ((null==dest) || (len<=0)) return 0;		
		if (dest.length < len+offset){
			len = dest.length - offset;
		}
		
		byte strByte[] = source.getBytes();
		
		for (int index=0; index<len; index++){
			dest[index+offset] = strByte[index];
		}
		
		//source.getBytes(0, len-1, dest, offset);
		return len;
	}
	
	public static String ByteArrayToPrintString(byte source[], int offset, int size){
		String dest="", strTmp=null;
		if (!mbPrintByteArray) return dest;
		if ((null==source)|| (source.length<=0)) return dest;		
		
		for (int tmp=offset; tmp<source.length && tmp<offset+size; tmp++){
			strTmp=String.format("%02x ", source[tmp]);
			dest += strTmp;
		}

		return dest;
	}
	
	public static int stringToInt(String src){
		int ret=0;
		try{
			ret = Integer.parseInt(src);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}
		
	public static String byteArrayToHexString(byte source[], int offset, int size){
		String dest="", strTmp=null;
		int len=source.length;
		if ((null==source) || (len<=0)) return dest;		
		
		for (int tmp=offset; tmp<len && tmp<offset+size; tmp++){
			strTmp=String.format("%02x", source[tmp]);
			dest += strTmp;
		}

		return dest;
	}
	
	public static boolean isNull(String arg) {
		boolean result = false;
		if (null == arg || "".equals(arg.trim()))
			result = true;
		return result;
	}
	
	public static boolean isNotNull(String arg) {
		boolean result = true;
		if (null == arg || "".equals(arg.trim()))
			result = false;
		return result;
	}
	
	
	
	public static String getInternalSDCardDir(){
		String ret="/mnt/sdcard";
		File file=new File(ret);
		if (!(file.exists() && file.canWrite())) {
			ret = null;
		}
		return ret;
	}
	
	public static String getExternalSDCardDir(){
		String ret="/mnt/sdcard/external_sdcard";
		File file=new File(ret);
		if (!(file.exists() && file.canWrite() && (doExec("df").indexOf(ret)>=0))) {
			ret = null;
		}
		return ret;
	}
	
	public static String getUsbDir(){
		String ret=null;
		File file=new File("/mnt");
		if (file.isDirectory()) {
			String[] subfiles=file.list();
			for (int index=0; index<subfiles.length; index++){
				if (subfiles[index].toLowerCase().startsWith("sd") &&
					(0!=subfiles[index].compareToIgnoreCase("sdcard"))){
					ret = subfiles[index];
					break;
				}
			}			
		}
		if (null != ret) {
			ret = "/mnt/" + ret;
		}
		return ret;
	}
	
	public static String doExec(String cmd) {   
        String s = "";   
        try {   
            Process p = Runtime.getRuntime().exec(cmd);   
            BufferedReader in = new BufferedReader(   
                                new InputStreamReader(p.getInputStream()));   
            String line = null;   
            while ((line = in.readLine()) != null) {   
                s += line + "\n";                  
            }   
        } catch (IOException e) {   
            // TODO Auto-generated catch block   
            e.printStackTrace();   
        }   
        LOGD("utils", "doExec '" + cmd + "' return " + s);
        return s;        
    }   
	
	/*public static String getMacAddress(){
		String mac="";
		try {  
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (null != en){
				for (; en.hasMoreElements();) {  
				    NetworkInterface intf = en.nextElement();  
				    if ((null!=intf) && (intf.getName().equalsIgnoreCase("eth0"))){
					    byte hardwareAddr[] = intf.getHardwareAddress();
					    mac = byteArrayToHexString(hardwareAddr, 0, hardwareAddr.length);
					    break;
				    }
				}  
			}
	    } catch (Throwable e) {  
	    	e.printStackTrace();
		}  
		LOGD("utils", "getMacAddress : " + mac);
		return mac;
	}*/
	
	public static String getIPAddress(){
		String ip="";
		try {  
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			if (null != en){
				for (; en.hasMoreElements();) {  
				    NetworkInterface intf = en.nextElement();  
				    if ((null!=intf) && (intf.getName().equalsIgnoreCase("eth0"))){
					    Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
					    if (null != enumIpAddr){
						    for (; enumIpAddr.hasMoreElements();) {  
						        InetAddress inetAddress = enumIpAddr.nextElement();  
						        if (!inetAddress.isLoopbackAddress()) {  
						            ip = inetAddress.getHostAddress().toString();  
						            LOGD("utils", "getIPAddress : " + ip);
						            return ip;
						        }  
						    }  
					    }
				    }
				}  
			}
	    } catch (Throwable e) {  
	    	e.printStackTrace();
		}  
		LOGD("utils", "getIPAddress : " + ip);
		return ip;
	}
	
	public static String safeString(String value){
		String ret=value;
		if (null == ret){
			ret = "";
		}
		return ret;
	}
	
	 public static JsonResult checkJsonValid(String json){
			JsonResult result=new JsonResult();
			
			try{
				JSONObject jsonObject = new JSONObject(json); 
				result.setValid(true);
				JSONObject responseObj = jsonObject.getJSONObject("Response");
				JSONObject headerObj = responseObj.getJSONObject("Header");
				JSONObject bodyObj = responseObj.getJSONObject("Body");
				result.setCompleted(true);
				if (null !=  headerObj){
					result.setRetCode(headerObj.getInt("RC"));
					result.setRetMsg(headerObj.getString("RM"));
					if (0 == result.getRetCode()) {
						result.setRetOK(true);
					}
					result.setObj(bodyObj);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				result.setRetCode(ResultDef.RESULT_FAILURE);
				result.setRetMsg(e.getStackTrace().toString());
			}
			
			return result;
		}
	 
	// hufuyi
	public static void saveReceviedData(String param) {
		File f = new File("/data/logmgr/" + "iotv_received.txt");
		
		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		saveFile(f, param);
	}
	
	public static void saveInsertFailedData(String param) {
		File f = new File("/data/logmgr/" + "iotv_insert_failed.txt");
		
		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		saveFile(f, param);
	}


	public static void saveUploadFinish(String param) {
		File f = new File("/data/logmgr/" + "iotv_uploaded.txt");

		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			param = URLEncoder.encode(param, utils.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		saveFile(f, param);
	}

	public static void saveFile(File f, String str) {
		FileWriter wri = null;
		try {
			try {
				wri = new FileWriter(f, true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				wri.write(str + "\r\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			try {
				wri.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	//if /data/logmgr this directory is exist, means in debug mode
	public static boolean checkDebug(){
		File f = new File("/data/logmgr");
		return f.exists();
	}
	// end
}
