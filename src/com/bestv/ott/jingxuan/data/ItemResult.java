package com.bestv.ott.jingxuan.data;

import java.util.List;


/**
 * @ClassName ItemResult 
 * @Description TODO (对Item做了封装)
 * @author Jaink
 * @date 2013-11-21 上午11:36:45
 */
public class ItemResult{

    private int TotalCount;// 总共的子项数目

    private int Count;// 文件包含的数目

    private int PageIndex;// 第几页

    private int PageSize;// 每页大小

    private List<Item> Items; // Item对象集合
    
    private String RecID;



    public int getTotalCount() {
        return TotalCount;
    }



    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }



    public int getCount() {
        return Count;
    }



    public void setCount(int count) {
        Count = count;
    }



    public int getPageIndex() {
        return PageIndex;
    }



    public void setPageIndex(int pageIndex) {
        PageIndex = pageIndex;
    }



    public int getPageSize() {
        return PageSize;
    }



    public void setPageSize(int pageSize) {
        PageSize = pageSize;
    }



    public List<Item> getItems() {
        return Items;
    }



    public void setItems(List<Item> items) {
        Items = items;
    }



    public boolean isEmpty() {
        if (this.Items != null) {
            return (this.Items.size() > 0) ? false : true;
        } else {
            return true;
        }
    }



	public String getRecID() {
		return RecID;
	}



	public void setRecID(String recID) {
		RecID = recID;
	}
}
