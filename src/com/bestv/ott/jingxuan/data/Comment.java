package com.bestv.ott.jingxuan.data;

/**
 * @ClassName: ItemComment
 * @Description: TODO(节目内容评论)
 * @author Jaink
 * @date 2013-9-10 下午4:28:35
 */
public class Comment {

    private String Name; // 评论人，用户昵称
    private String CommentTime; // 评论时间,格式：YYYYMMDDHH24MISS
    private String Comment; // 评论内容

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCommentTime() {
        return CommentTime;
    }

    public void setCommentTime(String commentTime) {
        CommentTime = commentTime;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    @Override
    public String toString() {
        return "ItemComment [name=" + Name + ", commentTime=" + CommentTime + ", comment=" + Comment + "]";
    }
}
