package com.bestv.ott.jingxuan.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.bestv.ott.framework.proxy.authen.UserProfile;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.jingxuan.R;

public class ChannelIconMgr {
	static final boolean DEBUG_NET_TEST = false;
	static final boolean USE_LOCAL_ICONS = true;
		
	Thread mThread = null;
	boolean quit = false;
	List<String> mIconList = new ArrayList<String>();
	String imgSrcAddress;

	public ChannelIconMgr() {
	}
	
	public void start(List<String> iconList){
		mIconList = iconList;
		
		UserProfile up = OttContextMgr.getInstance().getUserProfile();
		
		if (up == null){
			return;
		}
		
		imgSrcAddress = up.getIMGSrvAddress();

		if (mIconList == null || mIconList.size() == 0 || utils.isNull(imgSrcAddress)) {
			return;
		}

		mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				clearUnusedIconCache();
				
				do {
					try {
						String absolutepath = mIconList.get(0);
						String filename = absolutepath.substring(absolutepath.lastIndexOf(File.separator) + 1);
						String filepath = DataConfig.getNetIconDir() + File.separator + filename;
						File file = new File(filepath);
						
						if (file.exists()){
							mIconList.remove(0);
							continue;
						}
						
						int MAX_TRY_TIME = 3;
						int index = 0;
						
						while(index <= MAX_TRY_TIME){
							index++;
							
						    byte[] data = getImageFromNetByUrl(imgSrcAddress + File.separator + absolutepath);
						    //download failed, try again
						    if (data == null){
						    	continue;
						    }else{
						    	writeImageToLocal(data, filepath);
						    	break;
						    }
						}
					} catch (Exception e) {
					}
					
					//remove first no matter download success or not.
					if(mIconList.size() > 0){
						mIconList.remove(0);
					}
				} while (mIconList.size() > 0 && !quit);
			}
		});
		
		mThread.start();
	}

	public void quit() {
		quit = true;
	}
	
	private void clearUnusedIconCache(){
		Logger.d("start clear unused icon cache");
		try{
			File cachedir = new File(DataConfig.getNetIconDir());
			if (!cachedir.exists()){
				return;
			}
			
			for (String filename : cachedir.list()){
				boolean find = false;
				
				for (String iconpath : mIconList){
					String tmpname = iconpath.substring(iconpath.lastIndexOf(File.separator) + 1);
					
					if(tmpname.equals(filename)){
						find = true;
						break;
					}
				}
				
				//delete the file which is not found in icon list
				if (!find){
					File dfile = new File(DataConfig.getNetIconDir() + File.separator + filename);
					dfile.delete();
					
					Logger.d("delete file = " + filename);
				}
			}
		}catch(Exception e){
			
		}
	}

	/**
	 * 将图片写入到磁盘
	 * 
	 * @param img
	 *            图片数据流
	 * @param fileName
	 *            文件保存时的路径
	 */
	public static void writeImageToLocal(byte[] img, String filepath) {
		try {
			Logger.d("IOTV2", "get channel icon write to local file path = " + filepath);
			File fileTmp = new File(filepath + ".tmp");
			File file = new File(filepath);
			FileOutputStream fops = new FileOutputStream(fileTmp);
			fops.write(img);
			fops.flush();
			fops.close();

			fileTmp.renameTo(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据地址获得数据的字节流
	 * 
	 * @param strUrl
	 *            网络连接地址
	 * @return
	 */
	public static byte[] getImageFromNetByUrl(String strUrl) {
		try {
			Logger.d("IOTV2", "get channel icon = " + strUrl);
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(Constants.timeOut);
			InputStream inStream = conn.getInputStream();// 通过输入流获取图片数据
			byte[] btImg = readInputStream(inStream);// 得到图片的二进制数据
			return btImg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从输入流中获取数据
	 * 
	 * @param inStream
	 *            输入流
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}
	
	public static Drawable getChannelIcon(String channelName, String abusolutepath) {
		BitmapDrawable iconDrawable = null;
		Context context = OttContextMgr.getInstance().getContext();
		
		try {
			String filename = abusolutepath.substring(abusolutepath.lastIndexOf(File.separator) + 1);
			String filepath = DataConfig.getNetIconDir() + File.separator + filename;
			File f = new File(filepath);
			
			if (utils.isNotNull(abusolutepath) && f.exists()){
				iconDrawable = new BitmapDrawable(filepath);
			}else{
				iconDrawable = null;
			}
		} catch (Exception e) {
			iconDrawable = null;
		}
		
		try{
			if (iconDrawable == null && USE_LOCAL_ICONS) {
				String[] icons = context.getResources().getStringArray(
						R.array.iconnames);

				int target = -1;
				for (int index = 0; index < icons.length; index++) {
					String tmp = icons[index];

					if (channelName.indexOf(tmp) == 0) {
						target = index;
						break;
					}
				}

				if (target != -1) {
					target++;

					AssetManager assetManager = context.getAssets();
					InputStream dataSource;
					try {
						dataSource = assetManager.open("icons/" + "c" + target
								+ ".png");
						iconDrawable = new BitmapDrawable(dataSource);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}catch(Exception e){
			iconDrawable = null;
		}
		
		//Logger.d("IOTV2", "get Channel Icon = " + channelName + " icon path = " + abusolutepath + "bitmapdrawable = " + iconDrawable);
		
		return iconDrawable;
	}

	public static void initTestChannelList(List<Channel> list) {
		if (DEBUG_NET_TEST) {
			String[] test_imgs = { "/20140425/20140425045155092.jpg",
					"/20140425/20140425102623817.jpg",
					"/20140425/20140425104427735.jpg",
					"/20140425/20140425053202931.jpg",
					"/20140425/20140425111143959.jpg",
					"/20140425/20140425103233925.jpg",
					"/20140425/20140425050250097.jpg",
					"/20140425/20140425052459023.jpg",
					"/20140427/20140427012804000.jpg",
					"/20140428/20140428110004706.jpg",
					"/20140428/20140428104541662.jpg",
					"/20140428/20140428105602094.jpg",
					"/20140428/20140428104243686.jpg",
					"/20140429/20140429101924382.jpg",
					"/20140429/20140429102113997.jpg",
					"/20140429/20140429101919726.jpg",
					"/20140429/20140429104145489.jpg",
					"/20140429/20140429104021826.jpg",
					"/20140430/20140430023131751.jpg",
					"/20140430/20140430051859735.jpg",
					"/20140430/20140430023137691.jpg",
					"/20140430/20140430053632445.jpg",
					"/20140430/20140430022220330.jpg",
					"/20140430/20140430050125432.jpg",
					"/20140430/20140430022405945.jpg",
					"/20140430/20140430022530792.jpg",
					"/20140430/20140430103903031.jpg",
					"/20140430/20140430053404424.jpg",
					"/20140430/20140430030643092.jpg",
					"/20140430/20140430023742597.jpg",
					"/20140430/20140430021900378.jpg",
					"/20140430/20140430104620452.jpg",
					"/20140430/20140430053017819.jpg",
					"/20140430/20140430053626327.jpg",
					"/20140430/20140430042442568.jpg",
					"/20140430/20140430041602225.jpg",
					"/20140430/20140430021711744.jpg",
					"/20140430/20140430051924093.jpg",
					"/20140430/20140430030328998.jpg",
					"/20140430/20140430050116813.jpg",
					"/20140430/20140430042450095.jpg",
					"/20140430/20140430025517503.jpg",
					"/20140430/20140430103256351.jpg",
					"/20140430/20140430053011303.jpg",
					"/20140430/20140430030322832.jpg",
					"/20140430/20140430030927325.jpg",
					"/20140430/20140430010816590.jpg",
					"/20140430/20140430095904698.jpg",
					"/20140430/20140430011322974.jpg", };
			
			for (int index = 0; index < list.size(); index++){
				if (index >= test_imgs.length){
					break;
				}
				
				Channel c = list.get(index);
				c.setIcon1(test_imgs[index]);
			}
		}
	}
}
