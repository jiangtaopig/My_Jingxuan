/**
 * 
 */
package com.bestv.ott.jingxuan.data;


/**
 * @ClassName: Category
 * @Description: TODO
 * @author Jaink
 * @date 2013-9-10 上午10:54:16
 */
public class Category {

    public static final int SHOWWAY_NORMAL = 0; // 普通展示
    public static final int SHOWWAY_PRILLING = 1;// 颗粒化展示

    private String Count; // 子栏目数量
    private String Code; // 栏目唯一标识
    private String ParentCode; // 父栏目唯一标识
    private String Name; // 栏目名称
    private String Desc; // 栏目描述
    private String Icon1; // 小图标1
    private String Icon2; // 小图标2
    private String BGImage1; // 背景图片1
    private String BGImage2; // 背景图片2
    private int HasChild; // 是否有子节点
    private String TemplateCode;// 展示模板预定义代码，通过这个字段，可以供客户端来判断使用不同的模板，样式来显示
    private int Sequence; // 排列顺序
    private String UpdateTime; // 推荐位最后更新时间。格式：YYYYMMDDHH24MISS。说明：如果推荐位的任何元素有更新，该字段会随之更新
    private int ShowWay; // 展示方式（0： 普通展示1：颗粒化展示）
    private ItemResult result; // 此分类下对应的item选项

    public ItemResult getResult() {
        return result;
    }

    public void setResult(ItemResult result) {
        this.result = result;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        this.Code = code;
    }

    public String getParentCode() {
        return ParentCode;
    }

    public void setParentCode(String parentCode) {
        this.ParentCode = parentCode;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        this.Desc = desc;
    }

    public String getIcon1() {
        return Icon1;
    }

    public void setIcon1(String icon1) {
        this.Icon1 = icon1;
    }

    public String getIcon2() {
        return Icon2;
    }

    public void setIcon2(String icon2) {
        this.Icon2 = icon2;
    }

    public String getBgImage1() {
        return BGImage1;
    }

    public void setBgImage1(String bgImage1) {
        this.BGImage1 = bgImage1;
    }

    public String getBgImage2() {
        return BGImage2;
    }

    public void setBgImage2(String bgImage2) {
        this.BGImage2 = bgImage2;
    }

    public int getHasChild() {
        return HasChild;
    }

    public void setHasChild(int hasChild) {
        this.HasChild = hasChild;
    }

    public String getTemplateCode() {
        return TemplateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.TemplateCode = templateCode;
    }

    public int getSequence() {
        return Sequence;
    }

    public void setSequence(int sequence) {
        this.Sequence = sequence;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.UpdateTime = updateTime;
    }

    public int getShowWay() {
        return ShowWay;
    }

    public void setShowWay(int showWay) {
        this.ShowWay = showWay;
    }

    @Override
    public String toString() {
        return "Category [code=" + Code + ", parentCode=" + ParentCode + ", name=" + Name + ", desc=" + Desc
                + ", icon1=" + Icon1 + ", icon2=" + Icon2 + ", bgImage1=" + BGImage1 + ", bgImage2=" + BGImage2
                + ", hasChild=" + HasChild + ", templateCode=" + TemplateCode + ", sequence=" + Sequence
                + ", updateTime=" + UpdateTime + ", showWay=" + ShowWay + "]";
    }
}
