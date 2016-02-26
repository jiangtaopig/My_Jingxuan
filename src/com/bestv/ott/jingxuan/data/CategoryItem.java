package com.bestv.ott.jingxuan.data;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName: CategoryItem
 * @Description: TODO(对Category做了封装)
 * @author liujiao
 * @date 2013-9-16 上午11:57:48
 * 
 */
public class CategoryItem {

    private int TotalCount;// 总共的子项数目

    private int Count;// 文件包含的数目

    private int PageIndex;// 第几页

    private int PageSize;// 每页大小

    private List<Category> Categorys; // Categroy对象集合
    
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

    public List<Category> getCategorys() {
        return Categorys;
    }

    public void setCategorys(List<Category> categorys) {
        Categorys = categorys;
    }

    public void addCategory(Category value) 
    {
        if(Categorys==null)
        {
            Categorys=new ArrayList<Category>();
        }
        Categorys.add(value);
        TotalCount++;
        Count++;
    } 
    
    public boolean isEmpty(){
        if (null != Categorys) {
            return Categorys.size() > 0 ? false : true;
        } else {
            return true;
        }
    }

}
