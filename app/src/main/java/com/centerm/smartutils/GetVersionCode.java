package com.centerm.smartutils;

import java.util.List;

import com.centerm.smartpos.util.PropertiesUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 不同进城之间进行写作往往需要版本正确
 * 该类通过插入包名从而返回目前机器上应用的版本号从而方便进行其他应用的版本判断
 * @author Administrator
 *
 */

public class GetVersionCode {
	public Context context;
	public static GetVersionCode instance;
	
	public  synchronized static GetVersionCode getInstance(Context context){
		if(instance==null){
			instance = new GetVersionCode(context);
		}
		return instance;
	}
	
	public GetVersionCode(Context context) {
		this.context = context;
	}
	
	public int getAllVersionCode(String packageName){  
        PackageManager packageManager=context.getPackageManager();  
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);  
        for(PackageInfo info:packageInfos){  
            String versionName1 = info.versionName;  
            int versionCode1 = info.versionCode;  
            String packageName1 = info.packageName;    
            if (info.packageName.equals(packageName)){  
                String versionName = info.versionName;  
                int versionCode = info.versionCode;   
                return versionCode;  
            }  
        }  
        return 0;  
    }  
	
	//返回当前机器型号
	public static String getTerminalModel(){
	 	  String model = PropertiesUtil.getSystemProperties("ro.product.model");
		    if(model!=null){
		    	return model;
		    }
			return "UNKNOW";
		} 
}
