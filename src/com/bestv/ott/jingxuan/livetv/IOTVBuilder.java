package com.bestv.ott.jingxuan.livetv;

import com.bestv.ott.jingxuan.JXController;
import com.bestv.ott.jingxuan.JXDataModel;
import com.bestv.ott.jingxuan.livetv.controller.IOTVControllerBase;
import com.bestv.ott.jingxuan.model.IDataModel;
import com.bestv.ott.jingxuan.model.IOTVDataModel;
import com.bestv.ott.jingxuan.view.IOTVViewFactory;
import com.bestv.ott.jingxuan.view.IViewFactoryBase;


public class IOTVBuilder {
	public static final int BUILD_TYPE_IOTV = 1;
	public static final int BUILD_TYPE_JX = 2;
	
	public static IDataModel createDataModel(int type){
		IDataModel model = null;
		
		if (type == BUILD_TYPE_IOTV){
		    model = new IOTVDataModel();
		}else if (type == BUILD_TYPE_JX){
			model = new JXDataModel();
		}
		
		return model;
	}
	
	public static IViewFactoryBase createViews(){
		IViewFactoryBase b = new IOTVViewFactory();
		return b;
	}
	
	public static IOTVControllerBase createController(int type){
		IOTVControllerBase b = null;
		
		if (type == BUILD_TYPE_JX){
			b = new JXController();
		}
		
		return b;
	}
}
