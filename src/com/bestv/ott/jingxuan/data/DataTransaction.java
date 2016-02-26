package com.bestv.ott.jingxuan.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.bestv.ott.framework.beans.JsonResult;
import com.bestv.ott.framework.utils.JsonUtils;
import com.bestv.ott.jingxuan.livetv.OttContextMgr;
import com.bestv.ott.jingxuan.net.AllJXScheduleInvoker;
import com.bestv.ott.jingxuan.net.BesTVHttpResult;
import com.bestv.ott.jingxuan.net.CDNTokenInvoker;
import com.bestv.ott.jingxuan.net.ChannelInvoker;
import com.bestv.ott.jingxuan.net.IHttpInvoker;
import com.bestv.ott.jingxuan.net.IOTVNetException;
import com.bestv.ott.jingxuan.net.JXCategoryInvoker;
import com.bestv.ott.jingxuan.net.JXScheduleInvoker;
import com.bestv.ott.jingxuan.net.ScheduleInvoker;
import com.bestv.ott.jingxuan.util.Constants;
import com.bestv.ott.jingxuan.util.DataConfig;
import com.bestv.ott.jingxuan.util.IOTV_Date;
import com.bestv.ott.jingxuan.util.Logger;
import com.bestv.ott.jingxuan.util.TimeUseDebugUtils;
import com.bestv.ott.jingxuan.util.utils;
import com.bestv.ott.jingxuan.util.Constants.Errors;
import com.bestv.ott.jingxuan.util.Constants.Values;
import com.bestv.jingxuan.R;

public class DataTransaction {
	public static final String TAG = "DataTransaction";
	/**所有非直播的节目单**/
	public static List<JxAllScheduleItem>jxAllSchedules = null;
	public static  List<Channel> channelDateList = null;
	public static List<Channel> channel_list;
	public static ChannelResult jxCategorys = null;
	
	// modified by hufuyi 2014/3/14
	public static class newScheduleRunnable implements Runnable {
		private List<Channel> channels = new ArrayList<Channel>();
		private ScheduleRequest req = null;

		public newScheduleRunnable(List<Channel> chs, ScheduleRequest request) {
			channels.clear();
			this.channels = chs;
			this.req = request;
		}

		public newScheduleRunnable(Channel channel, ScheduleRequest request) {
			channels.clear();
			this.channels.add(channel);
			this.req = request;
		}

		public void run() {
			Logger.d(TAG, "run , channels size = "+ channels.size());
			StringBuilder codes = new StringBuilder();
			for (Channel c : channels) {
				codes.append(c.getChannelCode());
				codes.append("$");
			}

			req.setChannelCode(codes.toString());
			ScheduleResult result = getNewSchedules(req);

			if (result != null && result.getItems() != null) {
				for (Channel c : channels) {
					List<Schedule> slist = result.getItems().get(c.getChannelCode());
					if(slist == null){
						slist = new ArrayList<Schedule>();
					}
//					for (Schedule s : result.getItems()) {
//						if (c.getChannelCode().equals(s.getChannelCode())) {
//							slist.add(s);
//						}
//					}

					c.setSchedules(slist);
				}
			}
		}
	}

	private static ScheduleResult getNewSchedules(ScheduleRequest req) {
		ScheduleResult scheduleResult = new ScheduleResult();
		try {
			req.setPageIndex(1);
			req.setPageSize(Integer.MAX_VALUE);

			IHttpInvoker<BesTVHttpResult> invoker = new ScheduleInvoker(req);
			BesTVHttpResult result = invoker.invoke();
			Logger.d("ScheduleInvoker got result");
			// hufuyi
			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();
				scheduleResult = utils.gson.fromJson(jResult.getObj().toString(), ScheduleResult.class);
				
//				JSONObject joitems = jo.getJSONObject("Items");
//				List<Schedule> l = new ArrayList<Schedule>();
//				Logger.d("ScheduleInvoker got joitems");
//				String[] codes = req.getChannelCode().split("\\$");
//				Logger.d("ScheduleInvoker got codes");
//				for (String c : codes) {
//					Logger.d("ScheduleInvoker Start " + c);
//					JSONArray ja = null;
//					try{
//						ja = joitems.getJSONArray(c);
//					}catch(Exception e){
//						ja = null;
//					}
//
//					if (ja == null) {
//						continue;
//					}
//
//					for (int i = 0; i < ja.length(); i++) {
//						JSONObject j = ja.getJSONObject(i);
//						l.add(utils.gson.fromJson(j.toString(), Schedule.class));
//					}
//					Logger.d("ScheduleInvoker End " + c);
//				}
//
//				scheduleResult.setItems(l);
				Logger.d("ScheduleInvoker Leave");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.d("getNewSchedules:" + e.getMessage());
			utils.send(OttContextMgr.getInstance().getContext(),
					Errors.QueryScheduleException);
		}

		if (scheduleResult.getItems() != null) {
			Logger.d("getNewSchedules total count:"
					+ scheduleResult.getItems().size());
		}

		return scheduleResult;
	}
	
	/**
	 * 下载各个频道的节目单，如 东方卫视今天的所有节目
	 * @param channelResult
	 * @param days
	 * @return
	 */
	public static ChannelResult requestSchedules(ChannelResult channelResult, List<String> days) {
		if (days == null || days.size() == 0) {
			return channelResult;
		}

		for(String dd : days){
			Logger.d(TAG, "dd = "+dd);
		}
		
		Logger.d(TAG, "requestSchedules , channelResult size = "+channelResult.getItems().size());
		try {
			// sync schedules
			if (channelResult != null && channelResult.getItems() != null) {
				for (String day : days) {
					List<Channel> channels_all = channelResult.getItems();
					List<Channel> channels_livetv = new ArrayList<Channel>();
					List<Channel> channels_lllook = new ArrayList<Channel>();
					
					for (Channel c : channels_all) {
						c.setSchedules(null);
						
						if (c.getType() == Constants.CHANNEL_IOTV){
							channels_livetv.add(c);
						}else if(c.getType() == Constants.CHANNEL_LIANLIANKAN){
							channels_lllook.add(c);
						}
					}

					int initSize = Values.CHANNEL_PAGE_SIZE;
					if (channels_all.size() < initSize)
						initSize = channels_all.size();
					
					//first work for live tv
					List<Channel> tempChannel = new ArrayList<Channel>();
					int size = channels_livetv.size();
					
					for (int index = 0; index < size; index++){
						tempChannel.add(channels_livetv.get(index));
						
						if (index == size - 1 || tempChannel.size() == Constants.REQUEST_SCHEDULE_CHANNEL_COUNT){
							//start request for this channels
							ScheduleRequest scheduleReq = new ScheduleRequest();
							scheduleReq.setStartDate(day);
							scheduleReq.setEndDate(day);
							scheduleReq.setType(Constants.CHANNEL_IOTV);
							new newScheduleRunnable(tempChannel, scheduleReq)
									.run();
							
							tempChannel.clear();
						}
					}
					
					//then for lianlian-look
					tempChannel.clear();
					size = channels_lllook.size();
					
					for (int index = 0; index < size; index++){
						tempChannel.add(channels_lllook.get(index));
						
						if (index == size - 1 || tempChannel.size() == Constants.REQUEST_SCHEDULE_CHANNEL_COUNT){
							//start request for this channels
							ScheduleRequest scheduleReq = new ScheduleRequest();
							scheduleReq.setStartDate(day);
							scheduleReq.setEndDate(day);
							scheduleReq.setType(Constants.CHANNEL_LIANLIANKAN);
							new newScheduleRunnable(tempChannel, scheduleReq)
									.run();
							
							tempChannel.clear();
						}
					}
					
					if (channels_all.get(0).getSchedules() != null && channels_all.get(0).getSchedules().size() > 0){
						 Logger.d(TAG, "zjt , day = "+day);
					    saveChannelData(day, channelResult);
					    getCacheData(day);// add by zjt 11/4  
					}
				}
			}
		} catch (Exception e) {
			Logger.d("requestSchedules has a exception");
		}

		return channelResult;
	}

	public static ChannelResult getNewChannels(Context context, int pageIndex, List<String> days) throws IOTVNetException{
		return getNewChannels(context, pageIndex, days, false);
	}
	
	public static ChannelResult getNewChannels(Context context, int pageIndex, List<String> days, boolean requestSchedule) throws IOTVNetException{
		Logger.d("getNewChannels...page:" + pageIndex);
		ChannelResult channelResult = null;
		IOTVNetException netException = IOTVNetException.builder(context, IOTVNetException.TYPE_INIT_CHANNELLIST, R.string.take_channellist_failed);
		
		try {
			ChannelRequest req = new ChannelRequest();
			req.setPageIndex(pageIndex);
			req.setPageSize(Integer.MAX_VALUE);

			IHttpInvoker<BesTVHttpResult> invoker = new ChannelInvoker(req);
			BesTVHttpResult result = invoker.invoke();
            
			if (result.getHttpStatusCode() != HttpStatus.SC_OK && !requestSchedule){
				netException.setHttpResultCode(result.getHttpStatusCode());
				throw netException;
			}

			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();

				// added by hufuyi
				// get current data time
				try {
					JSONObject jo = (JSONObject) jResult.getObj();

					long currentdatetime = jo.getLong("CurrentDateTime");
					// the time is in second, so need convert it.
					IOTV_Date.instance().setFakeTime(currentdatetime * 1000);

					Logger.d("channels date = " + currentdatetime);
				} catch (Exception e) {
					Logger.d("find a exception while get current date time from querychannel json");
					// if parse current date time failed, set as current system
					// time
					IOTV_Date.instance().setFakeTime(new Date().getTime());
				}
				// end
				//try to init lookback day count 
				try {
					JSONObject jo = (JSONObject) jResult.getObj();

					long dayCount = jo.getLong("TvodDateCount");
					// the time is in second, so need convert it.
					DataConfig.setDataConfigValueL(context, DataConfig.TVOD_DATE_COUNT, dayCount);

					Logger.d("harish", "tvod date count = " + dayCount);
				} catch (Exception e) {
					Logger.d("harish", "find a exception while get TVOD date count from querychannel json");
				}
				// end
				
				//try to init LivePlayEndAct data
				//try to init lookback day count 
				try {
					JSONObject jo = (JSONObject) jResult.getObj();

					long JXplayAction = jo.getLong("LivePlayEndAct");
					// the time is in second, so need convert it.
					DataConfig.setDataConfigValueL(context, DataConfig.LIVE_PLAY_ENDACT, JXplayAction);

					Logger.d("harish", "JX play action = " + JXplayAction);
				} catch (Exception e) {
					Logger.d("harish", "find a exception while get JX play action from querychannel json");
				}
				
				Logger.d("channel data = " + jResult.getObj().toString());

				channelResult = utils.gson.fromJson(
						jResult.getObj().toString(), ChannelResult.class);
			} else {
				utils.send(OttContextMgr.getInstance().getContext(),
						Errors.QueryChannelException);
			}
			
			if (channelResult != null && channelResult.getItems() != null){
				Logger.d("getNewChannels, return count:" + channelResult.getItems().size());
				for (int index = 0; index < channelResult.getItems().size(); index++){
					Channel item = channelResult.getItems().get(index);
					
					if (item != null){
						item.setChannelNo((index + 1) + "");
					}
				}
				
				//just for channel icon test
				ChannelIconMgr.initTestChannelList(channelResult.getItems());

				if (requestSchedule){
					if (days == null){
						days = IOTV_Date.instance().getPrevDays();
					}
					
					requestSchedules(channelResult, days);
				}
			}
		} catch(IOTVNetException e){
			
		}catch (Exception e) {
			e.printStackTrace();
			Logger.d("getNewChannels:" + e.getMessage());
			utils.send(OttContextMgr.getInstance().getContext(),
					Errors.QueryChannelException);
		}
		
		Logger.d("requestSchedule = " + requestSchedule + " channelResult = " + channelResult + " http resonse code = " + netException.getHttpResultCode());
		//only throw exception at get channel
		if (!requestSchedule) {
			if (channelResult == null || channelResult.getItems() == null) {
				if (netException.getHttpResultCode() == 0) {
					netException.setErrorCode("96");
				}

				throw netException;
			} else if (channelResult.getItems().size() == 0) {
				netException.setErrorCode("96");
				throw netException;
			}
		}
		
		return channelResult;
	}
	
	/**
	 * 将从网络上获取的直播节目单以及直播频道的所有子节目的数据存到 channals_list.json或类似于20160120.json文件中
	 * @param name
	 * @param result
	 */
	public static void saveChannelData(String name, ChannelResult result) {
		try {
			String filename = DataConfig.getFilePath(name);
			Logger.d("IOTV1", ">>>sync channel to file... " + filename);
			File ftmp = new File(filename + ".tmp");
			File f = new File(filename);
			
			if (ftmp.exists()){
				ftmp.delete();
			}
		    
			ftmp.createNewFile();
			
			String data = utils.gson.toJson(result);
			
			FileOutputStream out = new FileOutputStream(ftmp);
			out.write(data.getBytes());
			out.close();
			
			ftmp.renameTo(f);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(name.equalsIgnoreCase(Constants.CHANNEL_FILE)){//add by zjt ,2015/11/09
			getCacheData(Constants.CHANNEL_FILE); 
			//setChannelData(result.getItems()); //add by zjt ,2015 /12/16 
		}
//		else{// add by zjt ,2015 /12/16
//			setChannelDateData(result.getItems());
//		}
		
	}
	
	public static void saveJxAllScheduleData(String name, JxAllScheduleResult result) {
		try {
			String filename = DataConfig.getFilePath(name);
			Logger.d("lhh", ">>>sync JxAllScheduleData to file... " + filename);
			File ftmp = new File(filename + ".tmp");
			File f = new File(filename);
			
			if (ftmp.exists()){
				ftmp.delete();
			}
		    
			ftmp.createNewFile();
			
			String data = utils.gson.toJson(result);
			
			FileOutputStream out = new FileOutputStream(ftmp);
			out.write(data.getBytes());
			out.close();
			
			ftmp.renameTo(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getCDNToken() {
		Logger.d("getCDNToken...");
		try {
			IHttpInvoker<BesTVHttpResult> invoker = new CDNTokenInvoker();
			BesTVHttpResult result = invoker.invoke();

			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();
				Logger.d("CDNToken result:" + jResult.getRetMsg());
				JSONObject token = new JSONObject(jResult.getObj().toString());
				String tokens = (token == null ? "" : token
						.getString("CDNToken"));
				Logger.d("getCDNToken:" + tokens);
				return tokens;
			} else {
				utils.send(OttContextMgr.getInstance().getContext(), Errors.GetCDNTokenException);
			}
		} catch (Exception e) {
			e.printStackTrace();
			utils.send(OttContextMgr.getInstance().getContext(), Errors.GetCDNTokenException);
		}

		return "";
	}
	
	
	public static void setJXCategorys(ChannelResult channelResult){
		Logger.d(TAG, ", enter  setJXCategorys");
		jxCategorys = channelResult;
	}
	
	/**
	 * 获取精选categorys数据
	 * @return
	 */
	public static ChannelResult getJXCategorys(){
		Logger.d(TAG, "enter getJXCategorys");
		return jxCategorys;
	}
	
	public static ChannelResult getJXCategorys(Context context, boolean requestSchedule) throws IOTVNetException{
		Log.d("harish", "get JX categorys");
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter getJXCategorys");
		ChannelResult channelResult = null;
		//IOTVNetException netException = IOTVNetException.builder(context, IOTVNetException.TYPE_INIT_CHANNELLIST, R.string.take_channellist_failed);
		
		try {
			ChannelRequest req = new ChannelRequest();

			IHttpInvoker<BesTVHttpResult> invoker = new JXCategoryInvoker(req);
			TimeUseDebugUtils.timeUsageDebug(TAG+",start http request");
			BesTVHttpResult result = invoker.invoke();
			TimeUseDebugUtils.timeUsageDebug(TAG+",http response");
            
			if (result.getHttpStatusCode() != HttpStatus.SC_OK && !requestSchedule){
				//netException.setHttpResultCode(result.getHttpStatusCode());
				//throw netException;
			}

			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();

				// added by hufuyi
				// get current data time
				try {
					JSONObject jo = (JSONObject) jResult.getObj();

					long currentdatetime = jo.getLong("CurrentDateTime");
					// the time is in second, so need convert it.
					IOTV_Date.instance().setFakeTime(currentdatetime * 1000);

					Logger.d("channels date = " + currentdatetime);
				} catch (Exception e) {
					Logger.d("find a exception while get current date time from querychannel json");
					// if parse current date time failed, set as current system
					// time
					IOTV_Date.instance().setFakeTime(new Date().getTime());
				}
				
				// end

				//Log.d("harish", "jx category data = " + jResult.getObj().toString());
//				CategoryItem catItem = utils.gson.fromJson(
//						jResult.getObj().toString(), CategoryItem.class);
				TimeUseDebugUtils.timeUsageDebug(TAG+",start json parse");
				CategoryItem catItem = JsonUtils.ObjFromJson((JSONObject)jResult.getObj(), CategoryItem.class);
				TimeUseDebugUtils.timeUsageDebug(TAG+",json parse finish");
				channelResult = new ChannelResult();
				List<Channel> channelL = new ArrayList<Channel>();
				//TimeUseDebugUtils.timeUsageDebug(TAG+",start  for loop");
				for (Category ce : catItem.getCategorys()){
					channelL.add(utils.category2Channel(ce));
				}
				//TimeUseDebugUtils.timeUsageDebug(TAG+",end  for loop");
				
				/* by ding.jian 2015/6/10		
				 * FORCE TO add Subscription Channel at the Top of Getting Channels.		
				 * What a Bad design here!!! We should add Subscription at the Server!
				 */
				JXSubscription jxSubscription = JXSubscription.getInstance();
				jxSubscription.setChannel(JXSubscription.CHANNELNAME,JXSubscription.CHANNECODE);
				addChannelItem(channelL, jxSubscription.getChannel(),0); //Top
				
				channelResult.setItems(channelL);
			} else {
				//utils.send(OttContextMgr.getInstance().getContext(),
				//		Errors.QueryChannelException);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			Logger.d("getNewChannels:" + e.getMessage());
			utils.send(OttContextMgr.getInstance().getContext(),
					Errors.QueryChannelException);
		}
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit getJXCategorys");
		setJXCategorys(channelResult);//add by zjt 2015/11/09/ 将分类数据添加到缓存中去
		return channelResult;
	}
	
	public static List<Schedule> getJXSchedules(Context context, String category) throws IOTVNetException{
		Log.d("harish", "get JX getJXSchedules");
		List<Schedule> listSchedule = null;
		//IOTVNetException netException = IOTVNetException.builder(context, IOTVNetException.TYPE_INIT_CHANNELLIST, R.string.take_channellist_failed);
		
		try {
			ChannelRequest req = new ChannelRequest();
			req.setPageIndex(1);
			req.setPageSize(Integer.MAX_VALUE);
			req.setExtraData(category);

			IHttpInvoker<BesTVHttpResult> invoker = new JXScheduleInvoker(req);
			BesTVHttpResult result = invoker.invoke();
            
			if (result.getHttpStatusCode() != HttpStatus.SC_OK){
				//netException.setHttpResultCode(result.getHttpStatusCode());
				//throw netException;
			}

			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();

				Log.d("harish", "jx getJXSchedules data = " + jResult.getObj().toString());
				JxScheduleResult catItem = utils.gson.fromJson(
						jResult.getObj().toString(), JxScheduleResult.class);
				
				listSchedule = catItem.getItems();
				
				int index = 0;
				for (Schedule sc : listSchedule){
					Log.d("harish", "schedule name = " + sc.getName());
					sc.setScheduleIndex(index);
					sc.setChannelCode(category);
					index++;
				}
			} else {
				//utils.send(OttContextMgr.getInstance().getContext(),
				//		Errors.QueryChannelException);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return listSchedule;
	}
	
	/**
	 * 将所有节目单添加到内存
	 * @param jxAllScheduleItem
	 */
	public static void setAllJXSchedules(List<JxAllScheduleItem> jxAllScheduleItem){
		jxAllSchedules = jxAllScheduleItem;
	}
	
	public static List<JxAllScheduleItem> getAllJXSchedules(){
		return jxAllSchedules;
	}
	
	
	
	public static List<JxAllScheduleItem> getAllJXCategorysAndSchedules(Context context, String day) throws IOTVNetException{
		Log.d("lhh", "getAllJXSchedules");
		List<JxAllScheduleItem> listCategory = null;
		//IOTVNetException netException = IOTVNetException.builder(context, IOTVNetException.TYPE_INIT_CHANNELLIST, R.string.take_channellist_failed);
		
		try {
			ChannelRequest req = new ChannelRequest();
			req.setPageIndex(1);
			req.setPageSize(Integer.MAX_VALUE);
			if(day != null){
				req.setExtraData(day);
			}

			IHttpInvoker<BesTVHttpResult> invoker = new AllJXScheduleInvoker(req);
			BesTVHttpResult result = invoker.invoke();
            
			if (result.getHttpStatusCode() != HttpStatus.SC_OK){
				//netException.setHttpResultCode(result.getHttpStatusCode());
				//throw netException;
			}

			if (result.getJsonResult() != null) {
				JsonResult jResult = (JsonResult) result.getJsonResult();

				Log.d("harish", "jx getAllJXSchedules data = " + jResult.getObj().toString());
				JxAllScheduleResult catItem = utils.gson.fromJson(
						jResult.getObj().toString(), JxAllScheduleResult.class);
				
				listCategory = catItem.getItems();
				/**
				 * close useless log by zjt 2015/12/16
				 */
//				for (JxAllScheduleItem item : listCategory){
//					Log.d("harish", "category name = " + item.getCategorycode());
//				}
				
				if(day == null){
					Date d = IOTV_Date.instance().getDateToday();
					day = IOTV_Date.getDateFormatNormal(d);
				}
				saveJxAllScheduleData(Constants.JX_ALL_SCHEDULE_FILE + day, catItem);//保存精选节目单数据
				
				//getJxDataFormJson(day);//add by zjt 将json数据存到内存中  close 2015/12/16
				setAllJXSchedules(listCategory); // add by zjt 2015/12/16 
				
			} else {
				//utils.send(OttContextMgr.getInstance().getContext(),
				//		Errors.QueryChannelException);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return listCategory;
	}
	
	
	private final static String openLocalFile(String filePath){
		File dir = new File(filePath);
		if (dir.exists()) {
			InputStreamReader reader = null;
			StringBuffer buffer = new StringBuffer();
			try {
				FileInputStream fis = new FileInputStream(dir);
				reader = new InputStreamReader(fis, "utf-8");
				char [] c = new char[1024];
				int length;
				while((length = reader.read(c)) != -1){
					buffer.append(c, 0, length);
				}
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
				//LogUtils.debug(e);
			} finally{
				try {
					reader.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return buffer.toString();
		} else {
			//LogUtils.debug("this file does not exist !!!");
			return null;
		}
	}
	
	/**
	 * 将本地缓存的jx_all_schedules_20151103.json数据解析从List<JxAllScheduleItem> 
	 * add by zjt 2015/11/3
	 * @param day
	 */
	public static void getJxDataFormJson(String day){
		
		Logger.d("start getJxCacheData, day :" + day);
		TimeUseDebugUtils.timeUsageDebug(TAG+",enter  getJxCacheData");
		String path = DataConfig.getFilePath(Constants.JX_ALL_SCHEDULE_FILE + day);

		List<JxAllScheduleItem> listCategory = null;
		
		try {
			
			String jsondata = openLocalFile(path);
			TimeUseDebugUtils.timeUsageDebug(TAG+",call  gson.fromJson");
			JxAllScheduleResult catItem = JsonUtils.ObjFromJson(jsondata, JxAllScheduleResult.class);
			TimeUseDebugUtils.timeUsageDebug(TAG+",after  gson.fromJson");
			
			listCategory = catItem.getItems();
		} catch (Exception e) {
			listCategory = null;
		}
		Logger.d("end getJxCacheData");
		TimeUseDebugUtils.timeUsageDebug(TAG+",exit  getJxCacheData");
		
		//jxAllSchedules = listCategory;
		setAllJXSchedules(listCategory);
		//return listCategory;
		
	}
	
	
	public static void setChannelDateData(List<Channel> channels){
		
		Date date = new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		Logger.d(TAG, "setChannelDateData , time = "+time);
		if(channels != null){
			Logger.d(TAG, "setChannelDateData size "+channels.size());
			
		}
		channelDateList = channels;
	}
	
	/**
	 * 获取解析好的 例如 20151104.json这样的数据
	 * @return
	 */
	public static List<Channel> getChannelDateData(){
		return channelDateList;
	}
	
	public static void setChannelData(List<Channel> lists){
		channel_list = lists;
		if(channel_list != null)
			Logger.d(TAG, "setChannelData ,channel_list size = "+channel_list.size());
	}
	
	/**
	 * 获取解析好的 channel_list.json数据
	 * @return
	 */
	public static List<Channel> getChannelData(){
		return channel_list;
	}
	
	/**
	 * 将本地的 20151103.json或者 channel_list.json  (表示当天的数据) 数据解析成 List<Channel>
	 * @param day
	 * @return
	 */
	public static void getCacheData(String day) {
		Logger.d(TAG, "enter getCacheData, day = "+day);
		String path = DataConfig.getFilePath(day);

		List<Channel> list = null;

		try {
			File f = new File(path);
			if (f.exists()) {
				FileInputStream input = new FileInputStream(f);

				byte data[] = new byte[input.available()];

				input.read(data);
				input.close();

				String jsondata = new String(data);
				ChannelResult result = utils.gson.fromJson(jsondata,
						ChannelResult.class);
				list = result.getItems();

				String setDay = day;

				if (setDay.equals(Constants.CHANNEL_FILE)) {
					setDay = IOTV_Date.getDateFormatNormal(IOTV_Date.getDate());
				}

				for (Channel channel : list) {
					channel.setDay(setDay);
				}

			}
		} catch (Exception e) {
			list = null;
		}
		if(day.equalsIgnoreCase(Constants.CHANNEL_FILE)){
			setChannelData(list);
		}else{
			setChannelDateData(list);
		}
		
		//return list;
	}
	
	
	public static void addChannelItem(List<Channel> chList, Channel ch,int index) {
		boolean hasItem = false;
		//check the existed channel .
		for (Channel c : chList) {
			if (c.getChannelCode().equals(ch.getChannelCode())) {
				hasItem = true;
			}
		}

		//if not exist,add it.
		if (!hasItem) {
			Log.d(TAG, "add : "+ch.getChannelCode());
			chList.add(index, ch);
		} else {
			Log.d(TAG, "Channel :" + ch.getChannelCode() +"has existed.");
			return;
		}
		
		//update the channalNo.for the display.
		for (int i = 0; i < chList.size(); i++) {
			String channelNo = (i + 1) + "";// Channel No. start from 1
			chList.get(i).setChannelNo(channelNo);
		}
	}
}
