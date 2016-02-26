package com.bestv.ott.jingxuan.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Loader.ForceLoadContentObserver;
import android.util.Log;

import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.utils;
import com.google.gson.reflect.TypeToken;

/* 
 * @author  ding.jian 
 * @date 创建时间：2015年6月3日 下午2:13:32 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public class JXSubscription {
	private static final String TAG = "JXSubscription";
	public static final int SUBSCRIBE_STATUS_UPDATE = 1;
	public static final int SUBSCRIBE_STATUS_NORMAL = 2;
	public static final int SUBSCRIBE_STATUS_INVALID = 3;
	
	private int subcribeStatus = SUBSCRIBE_STATUS_NORMAL; 
	private Channel ch;
	
	private int MAXSCHEDULES = 8 ;    //订阅数
	public static String CHANNELNAME = "订阅";
	public static String CHANNECODE = Constants.JX_SUBSCRP_CHANNEL_CODE;
	private boolean isNeedUpdate = false; // 是否需要更新
	String jsonString = null;

	public List<Schedule> subscriptionSchs = null;   //订阅的的节目
//	private long timeOfSubscribe = 0; //用户订阅动作时间
//	private int scheduleIndex = 0;    //订阅频道中的Index
//	private boolean forceToSync = true; // 强制更新gson文件

	
	/**
	 * 订阅，构造函数
	 */
	private JXSubscription(){
		Log.d(TAG,"Enter JXSubscription");
	}
	

	/**
	 * 饿汉式单例,初始化类时生成。
	 */
	private static JXSubscription instance = new JXSubscription();
	
	public static JXSubscription getInstance(){
		return instance;
	}
	
	/**
	 * 初始化订阅频道
	 * @param name 订阅name
	 * @param code 订阅ChannelCode
	 */
	public void setChannel(String name, String code) {
		Channel thisChannel = new Channel();
		thisChannel.setChannelName(name);
		thisChannel.setChannelCode(code);
		thisChannel.setChannelNo("0");  //add by default,ding.jian 2015/6/4
		thisChannel.setType(2);  //add by default,ding.jian 2015/6/4
		this.ch = thisChannel;
	}

	/**
	 * 返回订阅频道对象
	 */
	public Channel getChannel() {
		return ch;
	}

	public void setNeedUpdate(boolean isNeedUpdate) {
		this.isNeedUpdate = isNeedUpdate;
	}

	public void setMaxSubscribe(int MAXSCHEDULES){
		this.MAXSCHEDULES = MAXSCHEDULES;
	}
	
	public int getMaxSubscribe(){
		return MAXSCHEDULES;
	}
	
	public void setSubscribeSchs(List<Schedule> subscriptionSchs) {
		Logger.d(TAG, "enter setSubscribeSchs");
		if(subscriptionSchs == null)
			return;
		this.subscriptionSchs = subscriptionSchs;
		setIndex(this.subscriptionSchs); // set new schedule index in Subscription, channel.from 0.
		writeToFile(this.subscriptionSchs);
	}

	public void setIndex(List<Schedule> schList){
		int i = 0;
		for(Schedule ch:schList){
			ch.setScheduleIndex(i);
			i++;
		}
		
	}
	/**
	 * 从Json文件中获得订阅所有schedules
	 * 
	 * @return List<Schedule>所有订阅数据
	 */
	public List<Schedule> getSubscriptionSchs() {
//		Log.d(TAG, "getSubscriptionSchs");
//		subscriptionSchs = readFromFile();
		return subscriptionSchs;
	}
	
	public List<Schedule> getSubsFromFile(){
		Log.d(TAG, "getSubscriptionSchs");
		subscriptionSchs = readFromFile();
		return subscriptionSchs;
	}


	/**
	 * 以单个Schedule的方式存入订阅Schedule列表
	 * @param  要存入的schedule
	 * @return 是否能订阅成功（若达到最大订阅数，订阅失败）
	 */
	public boolean setSubscribeSchBySchedule(Schedule schedule) {
		Log.d(TAG,"setSubscribeSchBySchedule,schedule name: "+ schedule.getName());
		boolean ret = false;
		/*modify by xubin 2015.11.10 begin*/
		if (subscriptionSchs != null && subscriptionSchs.size() >= MAXSCHEDULES) {
			Log.d(TAG, "arrive the MAX subcription :" + MAXSCHEDULES);
			ret = false;
			return ret;
		}
		if(subscriptionSchs != null){
		ret = subscriptionSchs.add(schedule);
		}
		setIndex(subscriptionSchs); // set new schedule index in Subscription, channel.from 0.
		/*modify by xubin 2015.11.10 end*/
		return ret;
	}
	
	/**
	 * 以特定Schedule的方式取消订阅
	 * @param  要取消的schedule
	 * @return 取消结果
	 */
	public boolean cancelSubscribeSchBySchedule(Schedule schedule) {
		Log.d(TAG,"cancelSubscribeSchBySchedule,schedule name: "+ schedule.getName());
		Schedule removedSch = null;
		int index;

		index = getListIndex(subscriptionSchs,schedule);
		if(index < 0){
			Log.d(TAG,"no Subscribed Channel found");
			return false;  //no need write to file.
		}else{
			/*modify by xubin 2015.11.10 begin*/
			if(subscriptionSchs != null){
				removedSch = subscriptionSchs.remove(index);
			}
			/*modify by xubin 2015.11.10 end*/
			setIndex(subscriptionSchs); // set new schedule index in Subscription,channels.from 0.
		}
		
		if (removedSch != null){
			return true;
		}else {
			Log.d(TAG, "cancel subscription shdule error!");
		}
		return false;
	}

	/**
	 * 从默认文件中读出List<Schedule>
	 * @return 列表List<Schedule>
	 */
	public List<Schedule> readFromFile() {
		Logger.d(TAG,"enter readFromFile");
		List<Schedule> schs = new ArrayList<Schedule>();
		int readBytes = 0;
		try {
			String filename = DataConfig.getFilePath(Constants.JX_SUBSCRIBE_FILE);

			File file = new File(filename);
			if (file.exists()) {
				Log.d(TAG, "read data from: " + filename);

				FileInputStream input = new FileInputStream(file);

				byte data[] = new byte[input.available()];

				readBytes = input.read(data);
				input.close();
				
				if (readBytes > 0) {
					jsonString = new String(data);
					schs = utils.gson.fromJson(jsonString,
							new TypeToken<List<Schedule>>() {
							}.getType());

					for (Schedule sch : schs) {
						Log.d(TAG, "read Schedule name: " + sch.getName()+" last:"+sch.getLastVisitedTime()+" startTime:"+sch.getStartTime()+" status:"+sch.getSubscribeStatus());
					}
				}else{
					Log.d(TAG,"read noting in :" + filename);
				}
			}else{
				Log.d(TAG, "read data from file ,no file found ,create it!" + filename);
				file.createNewFile(); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return schs;

	}

	
	/**
	 * 把Schedule写入文件（Json格式）
	 * @param schs 要写入的List<Schedule>
	 */
	private void writeToFile(List<Schedule> schs) {
		Logger.d(TAG, "enter writeToFile");
		if(schs == null){
			Log.d(TAG,"schedule list is null .writeToFile return");
			return;
		}
		jsonString = utils.gson.toJson(schs);
		
		try {
			String filename = DataConfig.getFilePath(Constants.JX_SUBSCRIBE_FILE);
			Log.d(TAG, ">>>sync Subcription data to file... " + filename);
			File ftmp = new File(filename + ".tmp");
			File f = new File(filename);

			if (ftmp.exists()) {
				ftmp.delete();
			}

			ftmp.createNewFile();

			FileOutputStream out = new FileOutputStream(ftmp);
			out.write(jsonString.getBytes());
			out.close();

			ftmp.renameTo(f);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public boolean hasUpdated(List<Schedule> schs){
		for(Schedule sch : schs){
			if(sch.getSubscribeStatus() == Schedule.SUBSCRIBE_STATUS_UPDATE){
				this.subcribeStatus = sch.getSubscribeStatus();
				return true;
			}		
		}		
		return false;
	}
	
	
	public int getListIndex(List<Schedule> schs, Schedule schedule) {
		int i = 0;
		if (schs == null)
			return -1;
		for (Schedule sch : schs) {
			if (sch.equalsForSubscribe(schedule))
				return i;
			i++;
		}
		return -1;
	}
	
	
	//退出 将订阅文件保存到本地
	public void save(){
		Logger.d(TAG, "enter save");
		if(subscriptionSchs != null && subscriptionSchs.size() > 0){
			for(Schedule sch:subscriptionSchs){
				Log.d(TAG, "read Schedule name: " + sch.getName()+" last:"+sch.getLastVisitedTime()+" startTime:"+sch.getStartTime()+" status:"+sch.getSubscribeStatus());
			}
			//writeToFile(subscriptionSchs);
		}
		writeToFile(subscriptionSchs);
	}
	
	
	public void initSubPrograms(){
		subscriptionSchs = readFromFile();
	}
}
