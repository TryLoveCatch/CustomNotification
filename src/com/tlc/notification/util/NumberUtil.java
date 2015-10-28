package com.tlc.notification.util;

import java.io.File;
import java.text.DecimalFormat;

import android.content.Intent;
import android.net.Uri;

/**
 * File utility class.
 */
public class NumberUtil {
	public static final DecimalFormat dfWithTwo = new DecimalFormat("0.00");
	public static final int FILE_SIZE_G = 1024*1024*1024 ;
	public static final int FILE_SIZE_M = 1024*1024 ;
	public static final int FILE_SIZE_K = 1024 ;
	
	public static long getReadableSpliter(long size){
		
		if (size >= FILE_SIZE_G)
			return FILE_SIZE_G;
		else if( size >= FILE_SIZE_M )
			return FILE_SIZE_M ;
		else if (size >= FILE_SIZE_K)
			return FILE_SIZE_K;
		else 
			return 1;
	}

	public static String getSizeMeter(long size){
		if (size >= FILE_SIZE_G)
			return "GB";
		else if (size >= FILE_SIZE_M)
			return "MB" ;
		else if (size >= FILE_SIZE_K)
			return "KB";
		else 
			return "B";
	}
	
    
    public static String getSizeWithGMKB(long size){
    	double dSzie = size ;
    	long dSpliter = NumberUtil.getReadableSpliter(size);
		return dfWithTwo.format(dSzie/dSpliter) + " "+getSizeMeter(dSpliter);
	}
    
    public static Intent getApkInstallIntent(final String path){
		Intent it = new Intent(Intent.ACTION_VIEW);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        return it ;
	}
}
