package com.bestv.ott.jingxuan.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.List;

/**
 * @author xu.jian
 * 
 */
public class ChannelResult extends BaseResult implements Serializable {
	private int TotalCount;
	private int Count;
	private int PageIndex;
	private int PageSize;
	private List<Channel> Items;

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

	public List<Channel> getItems() {
		return Items;
	}

	public void setItems(List<Channel> items) {
		Items = items;
	}

	public Object deepClone() {
		// 将对象写到流里
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo;
		try {
			oo = new ObjectOutputStream(bo);
			oo.writeObject(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 从流里读出来
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		ObjectInputStream oi;
		try {
			oi = new ObjectInputStream(bi);
			return (oi.readObject());
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
}
