package com.bestv.ott.jingxuan.util;

/**
 * the constants are used for application
 * 
 * @author hu.fuyi
 * 
 */
public class Constants {
	public static final int timeOut = 30000;
	public static final int success = 0;
	
	//the max channel count for each query schedule. 
	public static final int REQUEST_SCHEDULE_CHANNEL_COUNT = 100;
	
	public static final int TEST_MAX_WATCH_TIME = 60000;
	public static final int TEST_EXIT_APP_TIME = 10000;
	
	//indicates media source is live TV or look back
	public static final int IOTV_PLAY_LIVE_SOURCE = 0;
	public static final int IOTV_PLAYTYPE_LOOKBACK_SOURCE = 1;
	public static final int IOTV_PLAY_JINGXUAN = 2;
	
	//indicates channel type is iotv or lianlian-kan
	public static final int CHANNEL_IOTV = 0;
	public static final int CHANNEL_LIANLIANKAN = 1;
	
	public static final String JX_SUBSCRP_CHANNEL_CODE = "live_subscription";
	public static final String JX_LATEST_CHANNEL_CODE = "live_new";
	public static final String JX_HOT_CHANNEL_CODE = "live_hot";
	//ten seconds
	public static final int DISTANCE2SHOW_COMPLETEVIEW = 10;
	
	public static final String CHANNEL_FILE = "channels_list";
	public static final String JX_ALL_SCHEDULE_FILE = "jx_all_schedules_";
	public static final String JX_SUBSCRIBE_FILE = "JXSubscription";
	
	public static final String BASESERVICES_PACKAGENAME = "com.bestv.ott.baseservices";
	
	//live tv time shift test source
	public static final int MAX_TIMESHIFT_VALUE = 358 * 10 * 1000;
	public static final boolean TEST_LIVE_SHIFT = false;
	//public static final String TEST_LIVE_SRC = "http://10.50.118.33/live/test/live.m3u8?&se=mobile&ct=1";
	
	// the time distance to check and start download tommorrow's data
	public static final int CHECK_START_TIME_HOUR = 20; // 20:00
	public static final int CHECK_END_TIME_HOUR = 23; // 23:00
	public static final int CHECK_JINGXUAN_START_TIME_HOUR = 0; // 00:00
	public static final int CHECK_JINGXUAN_END_TIME_HOUR = 3; // 3:00
	// the monitor interval time is random between below two values
	public static final int MONITOR_INTERVAL_MIN = 5 * 1000 * 60; // 4 minutes
	public static final int MONITOR_INTERVAL_MAX = 60 * 1000 * 60; // 8 minutes
	// 1 is support live shift 
	public static final int IS_LIVE_SHIFT_SUPPORT = 1;
	
	public class Keys {
		public static final String CHANNEL_LIST = "CHANNEL_LIST";
		public static final String CDNToken = "CDNToken";
		public static final String ALL_CHANNEL = "ALL_CHANNEL";
		public static final String ISIPTV = "ISIPTV";
		public static final String LAST_CACHE_TIME = "LAST_CACHE_TIME";
		public static final String UserID = "UserID";
		public static final String TVID = "TVID";
		public static final String TVProfile = "TVProfile";
		public static final String UserGroup = "UserGroup";
		public static final String PageIndex = "PageIndex";
		public static final String PageSize = "PageSize";
		public static final String ChannelNo = "ChannelCode";
		public static final String StartDate = "StartDate";
		public static final String EndDate = "EndDate";
		public static final String UserToken = "UserToken";
	}

	/**
	 * 值定义
	 * 
	 * @author hu.fuyi
	 * 
	 */
	public class Values {
		// 单位：秒
		public static final int speedInterval = 30;
		// 缓存过期(秒)
		public static final int cacheExpired = 30 * 60;
		// 节目缓冲最大时长，单位：秒
		public static final int maxSeekTime = 120;
		// 用户观看记录的最大条数
		public static final int USER_RECORD_COUNT = 10;
		public static final String NullPlayTime = "00:00:00";
		public static final int TAG_SCHEDULE_ITEM = 2;
		public static final int EXECUTE_WHOLE_PROCESS = 1;
		public static final int CHANNEL_PAGE_SIZE = 11;
		public static final String APP_ID = "SERVICE_LIVETV";
		public static final String Cache_Key = "ChannelsKey";
		public static final String Cache_File_Path = "Channels.json";
		public static final String ExpiredTime = "ExpiredTime";
		public static final String LastPlayChannelID = "LastPlayChannelID";
		public static final String CurrentTime = "CurrentTime";
		public static final String Channels = "Channels";
		public static final String KEY_CLEAR = "0001";
		public static final String KEY_SHOW_VERSION = "0002";
		public static final String TEST_REST = "0003";
		public static final String QueryChannel_URL_OTT1 = "http://app.bbtv.cn/ServerAPI/index.php";
		public static final String QuerySchedule_URL_OTT1 = "http://app.bbtv.cn/ServerAPI/index.php";
		public static final String ModuleServiceChannelUrl = "http://appservice.bbtv.cn/APPService/index.php";
	}

	public class Errors {
		public static final String ModuleServiceException = "210001";
		public static final String QueryChannelException = "210002";
		public static final String QueryScheduleException = "210003";
		public static final String RequestException = "211004";
		public static final String GetCDNTokenException = "210005";
		public static final String PlayException = "210006";
	}

	public class Urls {
		public static final String GetModuleService = "/OttService/QueryAPPAdress";
	}

	public class Messages {
		public static final int CHANGE_CHANNEL = 1;
		public static final int DISPLAY_EPG = 2;
	}

	/**
	 * app 服务方
	 * 
	 * @author xu.jian
	 * 
	 */
	public class Apps {
		public static final String TARGET_OEM_B2C = "B2C";
		public static final String TARGET_OEM_HJDX = "HJDX"; // 福建电信
		public static final String TARGET_OEM_TELECOMM = "TELECOMM";
		public static final String TARGET_OEM_SHDX = "SHDX";
		public static final String TARGET_OEM_SHYD = "SHYD"; // 上海移动
		public static final String TARGET_OEM_JSYD = "JSYD";
		public static final String TARGET_OEM_GZDX = "GZDX";
		public static final String TARGET_OEM_AHYD = "AHYD";
		public static final String TARGET_OEM_HBLT = "HBLT";
	}
	
	private static final String BESTV_CONF_PATH = "BESTV_CONF_PATH";
	private static final String BESTV_DATA_PATH = "BESTV_DATA_PATH";
	private static final String BESTV_UPGRADE_PATH = "BESTV_UPGRADE_PATH";

	public static final String OEM_FILE_NAME = "oem.properties";
	public static final String USER_FILE_NAME = "user.properties";
	public static final String BCTI_VERSION = "1201";

	public static final int HTTP_DEF_TIMEOUT = 10 * 1000;

	public final static String AUTH = "/OttService/Auth";
	public final static String ORDER = "/OttService/Order";
	public final static String PLAY = "/OttService/Play";
	public final static String GETUSERPROFILE = "/OttService/GetUserProfile";
	public final static String GETORDERRECORD = "/OttService/GetOrderRecord";

	private static final String DEF_CONFIG_PROFILE = "/cus_config/bestvConfig.properties";
	private static final String DEF_AAA_PROFILE = "/cus_config/bestvauth.properties";
	private static final String DEF_DATA_PATH = "/rs_data/bestv";

	public static final int RESULT_FAILURE_SERVICE = -12;
	public static final int RESULT_FAILURE_JSON_IMPERFECT = -11;
	public static final int RESULT_FAILURE_JSON_INVALID = -10;
	public static final int RESULT_FAILURE_TIMEOUT = -9;
	public static final int RESULT_FAILURE_PARAMETER_ERROR = -6;
	public static final int RESULT_FAILURE_ALREADYINITED = -5;
	public static final int RESULT_FAILURE_NOTINITED = -4;
	public static final int RESULT_FAILURE_NOTOPENED = -3;
	public static final int RESULT_FAILURE_NOTLOGINED = -2;
	public static final int RESULT_FAILURE = -1;
	public static final int RESULT_SUCCESS = 0;

	public static final String RESULT_MSG_FAILURE_TIMEOUT = "网络请求超时";
	public static final String RESULT_MSG_FAILURE_PARAMETER_ERROR = "请求参数出错";
	public static final String RESULT_MSG_FAILURE_EXCEPTION = "网络请求异常";
	public static final String RESULT_MSG_FAILURE_JSON_INVALID = "服务端返回json语法无效";
	public static final String RESULT_MSG_FAILURE_JSON_IMPERFECT = "服务端返回的json结构不完整";
	public static final String RESULT_MSG_FAILURE_SERVICE = "服务端服务返回失败";

	/* 错误信息 */
	public static final String RESULT_MSG_FAILURE_TEST_OPEN = "测试激活失败";// 071000
	public static final String RESULT_MSG_FAILURE_TEST_LOGIN = "测试登录失败";// 071010
	public static final String RESULT_MSG_FAILURE_TEST_3ASERVICE = "3A服务器访问失败";// 071100
	public static final String RESULT_MSG_FAILURE_TEST_EPGSERVICE = "EPG服务器访问失败";// 071101
	public static final String RESULT_MSG_FAILURE_TEST_IMGSERVICE = "图片服务器访问失败";// 071102
	public static final String RESULT_MSG_FAILURE_TEST_PSSERVICE = "PS服务器访问失败";// 071103
	public static final String RESULT_MSG_FAILURE_TEST_LOGSERVICE = "Log服务器访问失败";// 071104
	public static final String RESULT_MSG_FAILURE_TEST_UPGRADESERVICE = "升级服务器访问失败";// 071105
	public static final String RESULT_MSG_FAILURE_TEST_WEBSERVICE = "web应用入口访问失败";// 071106
	public static final String RESULT_MSG_FAILURE_TEST_THIRDPARTYSERVICE = "第三方功能服务器访问失败";// 071107
	public static final String RESULT_MSG_FAILURE_TEST_YYSSERVICE = "运营商服务器访问失败";// 071108
	public static final String RESULT_MSG_FAILURE_TEST_EPGRQSERVICE = "获取EPG容器失败";// 071200
	public static final String RESULT_MSG_FAILURE_TEST_TJW = "获取推荐位失败";// 071210
	public static final String RESULT_MSG_FAILURE_TEST_DSTJW = "获取导视推荐位失败";// 071220
	public static final String RESULT_MSG_FAILURE_TEST_LMXX = "获取栏目信息失败";// 071230
	public static final String RESULT_MSG_FAILURE_TEST_LMBPJMXX = "获取栏目编排节目信息失败";// 071231
	public static final String RESULT_MSG_FAILURE_TEST_DETAIL = "获取节目详细信息失败";// 071232
	public static final String RESULT_MSG_FAILURE_TEST_MSG = "获取消息失败";// 071300
	public static final String RESULT_MSG_FAILURE_TEST_PLAYURL = "获取播放地址失败";// 071400
	public static final String RESULT_MSG_FAILURE_TEST_PLAYURLLIST = "批量获取播放地址失败";// 071401
	public static final String RESULT_MSG_FAILURE_TEST_USERORDERS = "获取用户订购信息失败";// 071500
	public static final String RESULT_MSG_FAILURE_TEST_CPJQ = "产品鉴权异常";// 071501
	public static final String RESULT_MSG_FAILURE_TEST_ORDER = "产品订购异常";// 071502
	public static final String RESULT_MSG_FAILURE_TEST_SDHUPGRADE = "获取属地化升级信息失败";// 071900
	public static final String RESULT_MSG_FAILURE_TEST_UPGRADEMSG = "获取升级信息失败";// 071901
	public static final String RESULT_MSG_FAILURE_TEST_RESOURCEFILE = "获取开机动画等资源文件更新信息失败";// 071902
	public static final String RESULT_MSG_FAILURE_GET_PROGRAMS = "搜索节目列表信息失败";
	
	/* 错误代码 */
	public static final String RESULT_ERRORCODE_OPEN = "071000";
	public static final String RESULT_ERRORCODE_LOGIN = "071010";
	public static final String RESULT_ERRORCODE_3ASERVICE = "071000";
	public static final String RESULT_ERRORCODE_EPGSERVICE = "071101";
	public static final String RESULT_ERRORCODE_IMGSERVICE = "071102";
	public static final String RESULT_ERRORCODE_PSSERVICE = "071103";
	public static final String RESULT_ERRORCODE_LOGSERVICE = "071104";
	public static final String RESULT_ERRORCODE_UPGRADESERVICE = "071105";
	public static final String RESULT_ERRORCODE_WEBSERVICE = "071106";
	public static final String RESULT_ERRORCODE_THIRDPARTYSERVICE = "071107";
	public static final String RESULT_ERRORCODE_YYSSERVICE = "071108";
	public static final String RESULT_ERRORCODE_EPGRQSERVICE = "071200";
	public static final String RESULT_ERRORCODE_TJW = "071210";
	public static final String RESULT_ERRORCODE_DSTJW = "071220";
	public static final String RESULT_ERRORCODE_LMXX = "071230";
	public static final String RESULT_ERRORCODE_LMBPJMXX = "071231";
	public static final String RESULT_ERRORCODE_DETAIL = "071232";
	public static final String RESULT_ERRORCODE_SEARCH = "071233";
	public static final String RESULT_ERRORCODE_MSG = "071300";
	public static final String RESULT_ERRORCODE_PLAYURL = "071400";
	public static final String RESULT_ERRORCODE_PLAYURLLIST = "071401";
	public static final String RESULT_ERRORCODE_USERORDERS = "071500";
	public static final String RESULT_ERRORCODE_CPJQ = "071501";
	public static final String RESULT_ERRORCODE_ORDER = "071502";
	public static final String RESULT_ERRORCODE_SDHUPGRADE = "071900";
	public static final String RESULT_ERRORCODE_UPGRADEMSG = "071901";
	public static final String RESULT_ERRORCODE_RESOURCEFILE = "071902";

	public static final int PLAY_URL_ERROR = -2003;

	private static Constants mInstance = null;

	public static Constants getInstance() {
		if (null == mInstance) {
			mInstance = new Constants();
		}
		return mInstance;
	}

	private Constants() {
	}

	public String getConfPath() {
		return System.getenv(BESTV_CONF_PATH);
	}

	public String getAuthProfile() {
		return DEF_AAA_PROFILE;
	}

	public String getConfigProfile() {
		return DEF_CONFIG_PROFILE;
	}
}
