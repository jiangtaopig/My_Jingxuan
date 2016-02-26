package com.bestv.ott.jingxuan.data;

import java.util.HashMap;
import java.util.List;

/**
 * @author hu.fuyi
 * 
 */
public class ScheduleResult extends BaseResult {
    private int TotalCount;
    private int Count;
    private int PageIndex;
    private int PageSize;
    private HashMap<String, List<Schedule>> Items;
    
    private String RecID;
    
    public HashMap<String, List<Schedule>> getItems() {
		return Items;
	}
	
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
