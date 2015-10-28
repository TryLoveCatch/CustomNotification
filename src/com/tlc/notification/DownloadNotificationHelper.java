package com.tlc.notification;

import com.tlc.notification.util.NumberUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
public class DownloadNotificationHelper {
	private final static String CLICK_INTENT_ACTION = "com.tlc.notification.click.intent";
	private final static String DELETE_INTENT_ACTION = "com.tlc.notification.delete.intent";
	private final static Object mLock = new Object();
	public final static int STYLE_SYSTEM = 1;
	public final static int STYLE_CUSTOM = 2;
	
	private static DownloadNotificationHelper mInstatnce;
	private Context mContext;
	private NotificationController mController;
	private RemoteViews mRemoteViews;
	private DeleteBroadcast mBroadcast;
	
	
	public int mStyle;
	private boolean mIsShowing;
	private boolean mIsCompleted;
	private String mFilePath;
	
	private DownloadNotificationHelper(Context pContext){
		
		//4.0 之后的用自定义的 4.0 之前的用系统自带的
//		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH){
//			mStyle = STYLE_SYSTEM;
//		}else{
//			mStyle = STYLE_CUSTOM;
//		}
		
		//统一使用自定义view
		mStyle = STYLE_CUSTOM;
		mContext = pContext;
		mController = new NotificationController(pContext);
		
		mBroadcast = new DeleteBroadcast();
		IntentFilter tFilter = new IntentFilter();
		tFilter.addAction(DELETE_INTENT_ACTION);
		tFilter.addAction(CLICK_INTENT_ACTION);
		mContext.registerReceiver(mBroadcast, tFilter);
	}
	public static DownloadNotificationHelper getInstatnce(Context pContext){
		if(mInstatnce==null){
			synchronized (mLock) {
				if(mInstatnce==null){
					mInstatnce = new DownloadNotificationHelper(pContext);
				}
			}
		}
		return mInstatnce;
	}

	/**
	 * 显示状态栏，在开始下载的时候
	 * @param @param pFilePath 下载地址，用于点击状态栏安装更新
	 * @return void
	 * @throws
	 */
	public void show(String pFilePath){
		if(mIsShowing) return;
		if(pFilePath==null || pFilePath.trim().equals("")) return;
		
		mFilePath = pFilePath;
		switch(mStyle){
		case STYLE_CUSTOM:
			showCustomNotifi();
			break;
		case STYLE_SYSTEM:
			showSystemNotifi();
			break;
		default:
			return;
		}
		
		mIsShowing = true;
	}
	
	/**
	 * 隐藏状态栏并取消任务 应该在app退出的时候调用 
	 * @param 
	 * @return void
	 * @throws
	 */
	public void hide(){
		if(!mIsShowing) return;
		Toast.makeText(mContext, "通知被取消了", 0).show();
		mContext.unregisterReceiver(mBroadcast);
		mController.cancel();
		clear();
	}
	
	private int tProgress;
	/**
	 * 更新进度
	 * @param @param pTotalLength 文件大小
	 * @param @param pCurrentLength 当前大小
	 * @return void
	 * @throws
	 */
	public void update(long pTotalLength, long pCurrentLength){
		if(mIsCompleted || !mIsShowing) return;
		
		switch(mStyle){
		case STYLE_CUSTOM:
			if(pTotalLength==pCurrentLength && pTotalLength > 0){
			}else{
				tProgress = pTotalLength == 0? 0 :(int)(pCurrentLength*1000/pTotalLength);
				mRemoteViews.setProgressBar(R.id.notification_app_update_prg, 100, (int)(tProgress/10), false);
				mRemoteViews.setTextViewText(R.id.notification_app_update_txt_percent, ((int)(tProgress/10))+"%");
				mRemoteViews.setTextViewText(R.id.notification_app_update_txt_size, NumberUtil.getSizeWithGMKB(pCurrentLength)+"/"+NumberUtil.getSizeWithGMKB(pTotalLength));
			}
			break;
		case STYLE_SYSTEM:
			if(pTotalLength==pCurrentLength && pTotalLength > 0){
			}else{
				tProgress = pTotalLength == 0? 0 :(int)(pCurrentLength*1000/pTotalLength);
				mController.setProgress((int)(tProgress/10));
				mController.setMessage(NumberUtil.getSizeWithGMKB(pCurrentLength)+"/"+NumberUtil.getSizeWithGMKB(pTotalLength));
			}
			break;
		default:
			return;
		}
		mController.show();
	}
	/**
	 * 下载完成调用 更改状态栏为点击安装提示
	 * @param 
	 * @return void
	 * @throws
	 */
	public void complete(){
		mIsCompleted = true;
		
		switch(mStyle){
		case STYLE_CUSTOM:
			showCustomResult(R.string.upgrade_download_ok);
			mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_install, View.VISIBLE);
			mRemoteViews.setTextViewText(R.id.notification_app_update_txt_install, mContext.getResources().getText(R.string.upgrade_download_install));
			break;
		case STYLE_SYSTEM:
			mController.setLabel(mContext.getResources().getText(R.string.upgrade_download_ok));
			mController.setMessage(mContext.getResources().getText(R.string.upgrade_download_install));
//			mController.setMax(0);
//			mController.setProgress(0);
			mController.setProgressInvisibile();
			break;
		default:
			return;
		}
		
//		Intent tIntent = NumberUtil.getApkInstallIntent(mFilePath);
//		mController.setPendingIntent(tIntent, true);
		mController.setPendingIntent(clickIntent(), false);
		
		mController.setAutoCancel(true);
		mController.setOngoing(false);
		mController.show();
	}
	
	/**
	 * 完成 用于再次点击更新，并且apk已经下载完毕，无需重新下载
	 * @param @param pFilePath
	 * @return void
	 * @throws
	 */
	public void complete(String pFilePath){
		switch(mStyle){
		case STYLE_CUSTOM:
			if(!mIsShowing){
				showCustomNotifi();
			}
			break;
		case STYLE_SYSTEM:
			if(!mIsShowing){
				showSystemNotifi();
			}
			break;
		default:
			return;
		}
		
		mFilePath = pFilePath;
		
		complete();
	}
	
	/**
	 * 下载出错时调用
	 * @param 
	 * @return void
	 * @throws
	 */
	public void error(){
		switch(mStyle){
		case STYLE_CUSTOM:
			showCustomResult(R.string.upgrade_download_error);
			break;
		case STYLE_SYSTEM:
			mController.setLabel(mContext.getResources().getText(R.string.upgrade_download_error));
			mController.setMessage("");
//			mController.setMax(0);
//			mController.setProgress(0);
			mController.setProgressInvisibile();
			break;
		default:
			return;
		}
		mController.setAutoCancel(true);
		mController.setOngoing(false);
		mController.show();
	}
	
	
	
	
	
	private void clear(){
		mIsShowing = false;
		mIsCompleted = false;
		mContext = null;
		mController = null;
		mRemoteViews = null;
		mInstatnce = null;
	}
	private void showSystemNotifi(){
		mController.setIcon(R.drawable.ic_launcher);
		mController.setLabel(mContext.getResources().getString(R.string.upgrade_download_progress));
		mController.setTickerText(mContext.getResources().getString(R.string.upgrade_download_progress));
		mController.setMessage("0/0");
		mController.setMax(100);
		mController.setProgress(0);
		mController.setPendingIntent(new Intent(), true);
		mController.show();
	}
	
	private void showCustomNotifi(){
		mController.setIcon(R.drawable.ic_launcher);
//		mController.setLabel(mContext.getResources().getString(R.string.upgrade_download_progress));
		mController.setTickerText(mContext.getResources().getString(R.string.upgrade_download_progress));
		mRemoteViews = new RemoteViews(mContext.getPackageName(), 
				R.layout.notification_app_update);
		hideCustomResult();
		mRemoteViews.setTextViewText(R.id.notification_app_update_txt_title, mContext.getResources().getText(R.string.upgrade_download_progress));
		mController.setOngoing(true);
		mController.setPendingIntent(new Intent(), true);
		mController.setDeleteIntent(deleteIntent(), false);
		mController.setContent(mRemoteViews);
		mController.show();
	}
	
	private void showCustomResult(int pId){
		mRemoteViews.setTextViewText(R.id.notification_app_update_txt_title, mContext.getResources().getText(pId));
		//2.2的bug，progress不能自己隐藏，隐藏的话 只能放到一个layout里面
//		mRemoteViews.setViewVisibility(R.id.notification_app_update_prg, View.GONE);
		mRemoteViews.setViewVisibility(R.id.notification_app_update_lin_prg, View.GONE);
		mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_percent, View.GONE);
		mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_size, View.GONE);
	}
	
	private void hideCustomResult(){
		mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_install, View.GONE);
		//2.2的bug，progress不能自己隐藏，隐藏的话 只能放到一个layout里面
//		mRemoteViews.setViewVisibility(R.id.notification_app_update_prg, View.VISIBLE);
		mRemoteViews.setViewVisibility(R.id.notification_app_update_lin_prg, View.VISIBLE);;
		mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_percent, View.VISIBLE);
		mRemoteViews.setViewVisibility(R.id.notification_app_update_txt_size, View.VISIBLE);
	}
	
	private Intent deleteIntent(){
		Intent tIntent = new Intent();
		tIntent.setAction(DELETE_INTENT_ACTION);
		return tIntent;
	}
	private Intent clickIntent(){
		Intent tIntent = new Intent();
		tIntent.setAction(CLICK_INTENT_ACTION);
		return tIntent;
	}
	private class DeleteBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(DELETE_INTENT_ACTION.equals(intent.getAction())){
				hide();
			}else if(CLICK_INTENT_ACTION.equals(intent.getAction())){
				//监听到点击事件，在跳转到apk安装
				//这样，可以做一些清除操作
				Intent tIntent = NumberUtil.getApkInstallIntent(mFilePath);
				context.startActivity(tIntent);
				hide();
			}
		}
		
	}
}
