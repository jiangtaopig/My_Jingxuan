package com.bestv.ott.jingxuan.data;

import java.io.Serializable;

/**
 * @ClassName: VideoClip
 * @Description: TODO
 * @author Jaink
 * @date 2013-9-10 下午4:28:56
 */
public class VideoClip implements Serializable{

    public static final int TRACK_TYPE_OTHER = 0; // 其他
    public static final int TRACK_TYPE_MONAURAL = 1; // Monaural 单声道
    public static final int TRACK_TYPE_STEREO = 2; // Stereo 多声道
    public static final int TRACK_TYPE_TWO_NATION_MONAURAL = 3; // Two-nation monaural 双单声道
    public static final int TRACK_TYPE_TWO_NATION_STEREO = 4; // Two-nation stereo 双多声道
    public static final int TRACK_TYPE_AC3 = 5; // AC3(5:1 channel) AC3声道

    public static final int VIDEO_TYPE_OTHERS = 0;
    public static final int VIDEO_TYPE_H264 = 1;
    public static final int VIDEO_TYPE_MPEG4 = 2;
    public static final int VIDEO_TYPE_AVS = 3;
    public static final int VIDEO_TYPE_MPEG2 = 4;
    public static final int VIDEO_TYPE_MP3 = 5;
    public static final int VIDEO_TYPE_WMV = 6;
    public static final int VIDEO_TYPE_MP4 = 7;
    public static final int VIDEO_TYPE_FLV = 8;

    public static final int AUDIO_TYPE_MP2 = 1;
    public static final int AUDIO_TYPE_AAC = 2;
    public static final int AUDIO_TYPE_AMR = 3;

    public static final int RESOLUTION_QCIF = 1;
    public static final int RESOLUTION_QVGA = 2;
    public static final int RESOLUTION_TWO_THIRDS_D1 = 3;
    public static final int RESOLUTION_THREE_QUARTERS_D1 = 4;
    public static final int RESOLUTION_D1 = 5;
    public static final int RESOLUTION_720P = 6;
    public static final int RESOLUTION_1080I = 7;
    public static final int RESOLUTION_1080P = 8;

    private String VideoCode; // 唯一标识
    private String Type; // 媒体类型1:正片 2:预览片。默认为1
    private int EpisodeIndex; // 如果对应的Program为多剧集，本字段表示剧集集数，默认为0，0表示为整部（默认），1：第一集
    private int FileSize; // 文件大小，单位为Byte，格式为NNNNNNN
    private int Duration; // 播放时长HHMISSFF （时分秒），格式为NNNNNN
    private int TrackType; // 声道类型
    private int IsDRM; // 是否为DRM加密，0: 没有 1：是。默认为0
    private int CanDownload; // 是否可以下载，0: 不是 1：是。默认为1
    private int CanOnlinePlay; // 是否可以在线播放，0: 不是 1：是。默认为1
    private int ScreenFormat; // 0: 4x3，1: 16x9(Wide)
    private int Captioning; // 字幕标志，0:无字幕，1:有字幕
    private int BitRateType; // 码流，700:700Kbps，1300:1.3Mbps，1800:1.8Mbps，2500:2.5Mbps
    private int VideoType; // 视频编码格式
    private int AudioType; // 音频编码格式
    private int Resolution; // 分辨率类型
    private int Is3dvideo; // 是否3d视频，0:非3d视频，1:3d视频
    private int Format3d; // 3d格式：0：上下格式，1：左右格式
    private String Uri; // 视频文件的URI资源串

    public String getVideoCode() {
        return VideoCode;
    }

    public void setVideoCode(String videoCode) {
        VideoCode = videoCode;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public int getEpisodeIndex() {
        return EpisodeIndex;
    }

    public void setEpisodeIndex(int episodeIndex) {
        EpisodeIndex = episodeIndex;
    }

    public int getFileSize() {
        return FileSize;
    }

    public void setFileSize(int fileSize) {
        FileSize = fileSize;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public int getTrackType() {
        return TrackType;
    }

    public void setTrackType(int trackType) {
        TrackType = trackType;
    }

    public int getIsDRM() {
        return IsDRM;
    }

    public void setIsDRM(int isDRM) {
        IsDRM = isDRM;
    }

    public int getCanDownload() {
        return CanDownload;
    }

    public void setCanDownload(int canDownload) {
        CanDownload = canDownload;
    }

    public int getCanOnlinePlay() {
        return CanOnlinePlay;
    }

    public void setCanOnlinePlay(int canOnlinePlay) {
        CanOnlinePlay = canOnlinePlay;
    }

    public int getScreenFormat() {
        return ScreenFormat;
    }

    public void setScreenFormat(int screenFormat) {
        ScreenFormat = screenFormat;
    }

    public int getCaptioning() {
        return Captioning;
    }

    public void setCaptioning(int captioning) {
        Captioning = captioning;
    }

    public int getBitRateType() {
        return BitRateType;
    }

    public void setBitRateType(int bitRateType) {
        BitRateType = bitRateType;
    }

    public int getVideoType() {
        return VideoType;
    }

    public void setVideoType(int videoType) {
        VideoType = videoType;
    }

    public int getAudioType() {
        return AudioType;
    }

    public void setAudioType(int audioType) {
        AudioType = audioType;
    }

    public int getResolution() {
        return Resolution;
    }

    public void setResolution(int resolution) {
        Resolution = resolution;
    }

    public int getIs3dvideo() {
        return Is3dvideo;
    }

    public void setIs3dvideo(int is3dvideo) {
        Is3dvideo = is3dvideo;
    }

    public int getFormat3d() {
        return Format3d;
    }

    public void setFormat3d(int format3d) {
        Format3d = format3d;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public static int getTrackTypeOther() {
        return TRACK_TYPE_OTHER;
    }

    public static int getTrackTypeMonaural() {
        return TRACK_TYPE_MONAURAL;
    }

    public static int getTrackTypeStereo() {
        return TRACK_TYPE_STEREO;
    }

    public static int getTrackTypeTwoNationMonaural() {
        return TRACK_TYPE_TWO_NATION_MONAURAL;
    }

    public static int getTrackTypeTwoNationStereo() {
        return TRACK_TYPE_TWO_NATION_STEREO;
    }

    public static int getTrackTypeAc3() {
        return TRACK_TYPE_AC3;
    }

    public static int getVideoTypeOthers() {
        return VIDEO_TYPE_OTHERS;
    }

    public static int getVideoTypeH264() {
        return VIDEO_TYPE_H264;
    }

    public static int getVideoTypeMpeg4() {
        return VIDEO_TYPE_MPEG4;
    }

    public static int getVideoTypeAvs() {
        return VIDEO_TYPE_AVS;
    }

    public static int getVideoTypeMpeg2() {
        return VIDEO_TYPE_MPEG2;
    }

    public static int getVideoTypeMp3() {
        return VIDEO_TYPE_MP3;
    }

    public static int getVideoTypeWmv() {
        return VIDEO_TYPE_WMV;
    }

    public static int getVideoTypeMp4() {
        return VIDEO_TYPE_MP4;
    }

    public static int getVideoTypeFlv() {
        return VIDEO_TYPE_FLV;
    }

    public static int getAudioTypeMp2() {
        return AUDIO_TYPE_MP2;
    }

    public static int getAudioTypeAac() {
        return AUDIO_TYPE_AAC;
    }

    public static int getAudioTypeAmr() {
        return AUDIO_TYPE_AMR;
    }

    public static int getResolutionQcif() {
        return RESOLUTION_QCIF;
    }

    public static int getResolutionQvga() {
        return RESOLUTION_QVGA;
    }

    public static int getResolutionTwoThirdsD1() {
        return RESOLUTION_TWO_THIRDS_D1;
    }

    public static int getResolutionThreeQuartersD1() {
        return RESOLUTION_THREE_QUARTERS_D1;
    }

    public static int getResolutionD1() {
        return RESOLUTION_D1;
    }

    public static int getResolution720p() {
        return RESOLUTION_720P;
    }

    public static int getResolution1080i() {
        return RESOLUTION_1080I;
    }

    public static int getResolution1080p() {
        return RESOLUTION_1080P;
    }

    @Override
    public String toString() {
        return "VideoClip [videoCode=" + VideoCode + ", type=" + Type + ", episodeIndex=" + EpisodeIndex
                + ", fileSize=" + FileSize + ", duration=" + Duration + ", trackType=" + TrackType + ", isDRM=" + IsDRM
                + ", canDownload=" + CanDownload + ", canOnlinePlay=" + CanOnlinePlay + ", screenFormat="
                + ScreenFormat + ", captioning=" + Captioning + ", bitRateType=" + BitRateType + ", videoType="
                + VideoType + ", audioType=" + AudioType + ", resolution=" + Resolution + ", is3dvideo=" + Is3dvideo
                + ", format3d=" + Format3d + ", uri=" + Uri + "]";
    }
}
