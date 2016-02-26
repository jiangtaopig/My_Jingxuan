/**
 * 
 */
package com.bestv.ott.jingxuan.data;

import java.util.List;

/**
 * @ClassName: Item
 * @Description:
 * @author Jaink
 * @date 2013-9-10 上午11:09:27
 */
public class Item {

    public static final int TYPE_EPISODE_SINGLE = 0; // 单剧集视频
    public static final int TYPE_EPISODE_SEVERAL = 1; // 多剧集(电视剧)
    public static final int TYPE_BROADCAST = 2; // 直播频道
    public static final int TYPE_BROADCAST_EPG = 3; // 直播回看节目单
    public static final int TYPE_CATEGORY = 4; // 分类
    public static final int TYPE_URL = 5; // URL
    public static final int TYPE_ALBUM = 6; // 专辑
    public static final int TYPE_POSTION = 7; // 推荐位
    public static final int TYPE_APP = 8; // 应用
    public static final int TYPE_PROMOTION = 9; // 促销活动
    public static final int TYPE_FUNCTION_FORESHOW = 10;// 新功能预告
    public static final int TYPE_ADVERTISEMENT = 11; // 广告

    public static final int FLAG_COMMON = 0; // 普通展示
    public static final int FLAG_TOP = 1; // 置顶
    public static final int FLAG_HOT = 2; // 火

    private String Code; // Item的code
    private int Type; // 编排节目类型
    private String ParentCode; // item所属的分类Code
    private String Title; // 编排元素的显示名称
    private String Icon1; // 小图标, 缩略图。图片规格: 125*170
    private String Icon2; // 小图标2,海报。图片规格: 300*408
    private String Ratinglevel; // 评分星级 0 – 10
    private int RatinglevelCount; // 评分次数
    private String Starttime; // YYYYMMDDHH24MISS
    private String Endtime; // YYYYMMDDHH24MISS
    private String Summary; // 编排元素的简介
    private String Actor; // 演员
    private String Director; // 导演
    private int PlayCount; // 播放次数
    private String Url; // 定义为推荐位元素的资源字串。用户收藏/书签进行保存时的资源串。供下次用户进入到收藏/书签列表时进入到具体资源所使用
    private int Flag; // 展示标识，默认为0
    private int Score; // 内容分数
    private List<VideoClip> VideoClips; // 所以的视频资源
    private ItemDetail ItemDetail; // 节目详细信息
    private String MarkImageUrl; // 角标图片地址
    private String MarkPosition; // 角标位置
    //private String RecID;//智能推荐接口返回的参数，默认为空,智能推荐算法模式ID
    private String ModeID;//智能推荐接口返回的参数，默认为空,智能推荐ID



    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public String getParentCode() {
        return ParentCode;
    }

    public void setParentCode(String parentCode) {
        ParentCode = parentCode;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getIcon1() {
        return Icon1;
    }

    public void setIcon1(String icon1) {
        Icon1 = icon1;
    }

    public String getIcon2() {
        return Icon2;
    }

    public void setIcon2(String icon2) {
        Icon2 = icon2;
    }

    public String getRatinglevel() {
        return Ratinglevel;
    }

    public void setRatinglevel(String ratinglevel) {
        Ratinglevel = ratinglevel;
    }

    public int getRatinglevelCount() {
        return RatinglevelCount;
    }

    public void setRatinglevelCount(int ratinglevelCount) {
        RatinglevelCount = ratinglevelCount;
    }

    public String getStarttime() {
        return Starttime;
    }

    public void setStarttime(String starttime) {
        Starttime = starttime;
    }

    public String getEndtime() {
        return Endtime;
    }

    public void setEndtime(String endtime) {
        Endtime = endtime;
    }

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public String getActor() {
        return Actor;
    }

    public void setActor(String actor) {
        Actor = actor;
    }

    public String getDirector() {
        return Director;
    }

    public void setDirector(String director) {
        Director = director;
    }

    public int getPlayCount() {
        return PlayCount;
    }

    public void setPlayCount(int playCount) {
        PlayCount = playCount;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public int getFlag() {
        return Flag;
    }

    public void setFlag(int flag) {
        Flag = flag;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public List<VideoClip> getVideoClips() {
        return VideoClips;
    }

    public void setVideoClips(List<VideoClip> videoClips) {
        VideoClips = videoClips;
    }

    public ItemDetail getItemDetail() {
        return ItemDetail;
    }

    public void setItemDetail(ItemDetail itemDetail) {
        ItemDetail = itemDetail;
    }

    public String getMarkImageUrl() {
        return MarkImageUrl;
    }

    public void setMarkImageUrl(String markImageUrl) {
        MarkImageUrl = markImageUrl;
    }

    public String getMarkPosition() {
        return MarkPosition;
    }

    public void setMarkPosition(String markPosition) {
        MarkPosition = markPosition;
    }

    public static int getTypeEpisodeSingle() {
        return TYPE_EPISODE_SINGLE;
    }

    public static int getTypeEpisodeSeveral() {
        return TYPE_EPISODE_SEVERAL;
    }

    public static int getTypeBroadcast() {
        return TYPE_BROADCAST;
    }

    public static int getTypeBroadcastEpg() {
        return TYPE_BROADCAST_EPG;
    }

    public static int getTypeCategory() {
        return TYPE_CATEGORY;
    }

    public static int getTypeUrl() {
        return TYPE_URL;
    }

    public static int getTypeAlbum() {
        return TYPE_ALBUM;
    }

    public static int getTypePostion() {
        return TYPE_POSTION;
    }

    public static int getTypeApp() {
        return TYPE_APP;
    }

    public static int getTypePromotion() {
        return TYPE_PROMOTION;
    }

    public static int getTypeFunctionForeshow() {
        return TYPE_FUNCTION_FORESHOW;
    }

    public static int getTypeAdvertisement() {
        return TYPE_ADVERTISEMENT;
    }

    public static int getFlagCommon() {
        return FLAG_COMMON;
    }

    public static int getFlagTop() {
        return FLAG_TOP;
    }

    public static int getFlagHot() {
        return FLAG_HOT;
    }

 

    @Override
    public String toString() {
        return "Item [code=" + Code + ", type=" + Type + ", parentCode=" + ParentCode + ", title=" + Title + ", icon1="
                + Icon1 + ", icon2=" + Icon2 + ", ratinglevel=" + Ratinglevel + ", ratinglevelCount="
                + RatinglevelCount + ", starttime=" + Starttime + ", endtime=" + Endtime + ", summary=" + Summary
                + ", actor=" + Actor + ", director=" + Director + ", playCount=" + PlayCount + ", url=" + Url
                + ", flag=" + Flag + ", score=" + Score + ", videoClips=" + VideoClips + ", itemDetail=" + ItemDetail
                + ", markImageUrl=" + MarkImageUrl + ", markPosition=" + MarkPosition + "]";
    }

	public String getModuleId() {
		return ModeID;
	}

	public void setModuleId(String moduleId) {
		ModeID = moduleId;
	}
}
