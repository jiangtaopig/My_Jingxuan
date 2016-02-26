package com.bestv.ott.jingxuan.data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: ItemDetail
 * @Description: TODO(节目详细信息)
 * @author Jaink
 * @date 2013-9-10 下午4:29:07
 */
public class ItemDetail implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SUBTITLE_TYPE_NO = 0; // 没有外挂字幕
    public static final int SUBTITLE_TYPE_VOB = 1; // vob格式
    public static final int SUBTITLE_TYPE_SUB = 2; // sub格式
    public static final int SUBTITLE_TYPE_SRT = 3; // srt格式

    private String Name; // 名称

    private String Code; // 唯一标识

    private int Type; // 内容类型。0：单剧集类节目。1：连续剧类节目

    private int EpisodeNum; // 剧集数目，Type=1的时候才有效，默认为1

    private String Actor; // 演员

    private String Director; // 导演

    private String Region; // 出品地区

    private String IssueYear; // 年代

    private String Language; // 语言

    private String ProgramType; // 节目内容类型：电影，体育，财经….

    private String Keywords; // 关键字

    private String RatingLevel; // 评分星级 0 – 10

    private int RatinglevelCount; // 评分总数

    private String Desc; // 描述信息

    private int Length; // 节目时长，单位秒

    private int SubtitleType; // 字幕格式

    private String SubtitleURL; // 字幕文件URL

    private String SmallImage1; // 小图片1,格式为： /1/1.jpg，使用相对地址

    private String SmallImage2; // 小图片2

    private String BigImage1; // 海报图片1

    private String BigImage2; // 海报图片2

    private String ServiceCodes; // 内容所属服务，有可能会有多个，通过”;”进行分割

    private List<VideoClip> VideoClip; // 该节目相关的视频信息

    private ItemComment ItemComment;// 该节目相关的评论信息

    private String MarkImageUrl; // 角标图片地址

    private String MarkPosition; // 角标位置

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

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

    public int getEpisodeNum() {
        return EpisodeNum;
    }

    public void setEpisodeNum(int episodeNum) {
        EpisodeNum = episodeNum;
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

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public String getIssueYear() {
        return IssueYear;
    }

    public void setIssueYear(String issueYear) {
        IssueYear = issueYear;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public String getProgramType() {
        return ProgramType;
    }

    public void setProgramType(String programType) {
        ProgramType = programType;
    }

    public String getKeywords() {
        return Keywords;
    }

    public void setKeywords(String keywords) {
        Keywords = keywords;
    }

    public String getRatingLevel() {
        return RatingLevel;
    }

    public void setRatingLevel(String ratingLevel) {
        RatingLevel = ratingLevel;
    }

    public int getRatinglevelCount() {
        return RatinglevelCount;
    }

    public void setRatinglevelCount(int ratinglevelCount) {
        RatinglevelCount = ratinglevelCount;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public int getLength() {
        return Length;
    }

    public void setLength(int length) {
        Length = length;
    }

    public int getSubtitleType() {
        return SubtitleType;
    }

    public void setSubtitleType(int subtitleType) {
        SubtitleType = subtitleType;
    }

    public String getSubtitleURL() {
        return SubtitleURL;
    }

    public void setSubtitleURL(String subtitleURL) {
        SubtitleURL = subtitleURL;
    }

    public String getSmallImage1() {
        return SmallImage1;
    }

    public void setSmallImage1(String smallImage1) {
        SmallImage1 = smallImage1;
    }

    public String getSmallImage2() {
        return SmallImage2;
    }

    public void setSmallImage2(String smallImage2) {
        SmallImage2 = smallImage2;
    }

    public String getBigImage1() {
        return BigImage1;
    }

    public void setBigImage1(String bigImage1) {
        BigImage1 = bigImage1;
    }

    public String getBigImage2() {
        return BigImage2;
    }

    public void setBigImage2(String bigImage2) {
        BigImage2 = bigImage2;
    }

    public String getServiceCodes() {
        return ServiceCodes;
    }

    public void setServiceCodes(String serviceCodes) {
        ServiceCodes = serviceCodes;
    }

    public List<VideoClip> getVideoClip() {
        return VideoClip;
    }

    public void setVideoClip(List<VideoClip> videoClip) {
        VideoClip = videoClip;
    }

    public ItemComment getItemComment() {
        return ItemComment;
    }

    public void setItemComment(ItemComment itemComment) {
        ItemComment = itemComment;
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

    @Override
    public String toString() {
        return "ItemDetail [name=" + Name + ", code=" + Code + ", type=" + Type + ", episodeNum=" + EpisodeNum
                + ", actor=" + Actor + ", director=" + Director + ", region=" + Region + ", issueYear=" + IssueYear
                + ", language=" + Language + ", programType=" + ProgramType + ", keywords=" + Keywords
                + ", ratingLevel=" + RatingLevel + ", ratinglevelCount=" + RatinglevelCount + ", desc=" + Desc
                + ", length=" + Length + ", subtitleType=" + SubtitleType + ", subtitleURL=" + SubtitleURL
                + ", smallImage1=" + SmallImage1 + ", smallImage2=" + SmallImage2 + ", bigImage1=" + BigImage1
                + ", bigImage2=" + BigImage2 + ", serviceCodes=" + ServiceCodes + ", videoClip=" + VideoClip
                + ", itemComment=" + ItemComment + ", markImageUrl=" + MarkImageUrl + ", markPosition=" + MarkPosition
                + "]";
    }

}
