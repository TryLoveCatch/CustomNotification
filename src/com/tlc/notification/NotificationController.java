package com.tlc.notification;

import java.util.Date;

import com.tlc.notification.util.ClassLoadHelper;
import com.tlc.notification.util.ObjectHelper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.widget.RemoteViews;

public class NotificationController {
	
	private Context mContext;
	private NotificationManager mManager;
	private Notification mNotification;
	private NotificationCompat.Builder mBuilder;
	private Object mBuilderInvoke;
	private RemoteViews mContentView;
	
	
	private boolean mIshowing;
	
	private int id;
	private CharSequence mTitle;
	private CharSequence mMessage;
	private PendingIntent mPendingIntent;
	private PendingIntent mDeleteIntent;
	private long mProgress=-1;
	private long max=-1;
	private TextPaint paint=new TextPaint();
	private int mFlags;
	public long last_progress_time = 0;
	public long last_message_time = 0;
	private boolean isCanceled=false;

	public NotificationController(Context context) {
		mContext=context;
		mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mIshowing = false;
		id = (int)SystemClock.elapsedRealtime();		
		paint.setTextSize(16);
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			mBuilderInvoke=ClassLoadHelper.createObject("android.app.Notification$Builder", 
					new Class<?>[]{Context.class}, new Context[]{mContext});
		else
			mBuilder=new NotificationCompat.Builder(mContext);
		
	}
	
	public void setIcon(int iconId){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setSmallIcon",int.class, iconId);
		else
			mBuilder.setSmallIcon(iconId);
	}
	
	@TargetApi(value = 11) 
	public void setLargeIcon(Bitmap bitmap){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setLargeIcon",Bitmap.class, bitmap);
		else {
			//do nothing
		}
	}
	
	public void setTickerText(CharSequence tickerText){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setTicker", CharSequence.class, tickerText);
		else
			mBuilder.setTicker(tickerText);
	}
	
	public void setLabel(CharSequence pTitle){
		mTitle = pTitle;
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setContentTitle", CharSequence.class, mTitle);
		else
			mBuilder.setContentTitle(mTitle);
	}
	
	public void setMessage(CharSequence message){
		//in lower version, message need to set ellipsize
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mMessage=message;
		} else {
			if(message!=null)
				mMessage=TextUtils.ellipsize(message, paint, 200, TruncateAt.MIDDLE);
			else
				mMessage="";
		}
		
		Long now = new Date().getTime();
		if(now - last_message_time < 300 && mProgress!=max)
			return;
		last_message_time = now;
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setContentText", CharSequence.class, mMessage);
		else{
			if (mProgress!=-1) {
				mBuilder.setContentText(mMessage+"  "+(mProgress*100/max)+"%");
			} else {
				mBuilder.setContentText(mMessage);
			}
		}			
	}
	
	public void setProgressInvisibile(){
		if (mIshowing) {
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				ObjectHelper.get(mBuilderInvoke).m("setProgress", 
						new Class[]{int.class, int.class, boolean.class}, 
						new Object[]{0 , 0 , false});
			}
			else{
				mBuilder.setContentText(mMessage);
			}
		}		
	}

	public void setPendingIntent(Intent intent, boolean isActivity){
//		if(intent.getComponent()!=null){
//			if(!intent.getComponent().getClassName().equals(FirstActivity.class.getName()))
//				intent.setAction(Long.toString(System.currentTimeMillis()));
//			else {
//				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			}
//		}
		if (isActivity) {
			mPendingIntent=PendingIntent.getActivity(mContext, 
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			mPendingIntent=PendingIntent.getBroadcast(mContext, 
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setContentIntent", mPendingIntent);
		else
			mBuilder.setContentIntent(mPendingIntent);
	}

	public void setDeleteIntent(Intent intent, boolean isActivity){
		if (isActivity) {
			mDeleteIntent=PendingIntent.getActivity(mContext, 
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			mDeleteIntent=PendingIntent.getBroadcast(mContext, 
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setDeleteIntent", mDeleteIntent);
		else
			mBuilder.setDeleteIntent(mDeleteIntent);
	}
	
	/**
	 * build之后调用
	 * 
	 * 兼容2.3
	 * 
	 * @param @param content
	 * @return void
	 * @throws
	 */
	public void setContent(RemoteViews content){
		mContentView = content;
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setContent", content);
		else
			mBuilder.setContent(content);
		
	}
	
	@TargetApi(value = 16)
	public void addAction(int icon, CharSequence title, PendingIntent intent){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) 
			ObjectHelper.get(mBuilderInvoke).m("addAction",
					new Class[]{int.class, CharSequence.class, PendingIntent.class}, 
					new Object[]{icon, title, intent});
		else {
			
		}			
	}
	
	public void setOngoing(boolean onGoing){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			ObjectHelper.get(mBuilderInvoke).m("setOngoing", boolean.class, onGoing);
		else
			mBuilder.setOngoing(onGoing);
	}

	public void show(){
		try{
			if(isCanceled)
				return;
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
				mNotification=(Notification) ObjectHelper.get(mBuilderInvoke).m("getNotification");
			else
				mNotification=mBuilder.getNotification();
			
			if(mContentView!=null){
				mNotification.contentView = mContentView;
			}
			mNotification.flags |= mFlags;
			mManager.notify(id, mNotification);
			if(isCanceled) {
				//to avoid show again after notification cancelled. 
				mManager.cancel(id);
				return;
			}
			mIshowing=true;
		}catch(Exception e) //An known Android 4.3 bug, notify may throw SecurityException on some device.
		{
			e.printStackTrace();
		}
	}
	
	public void build(){
		if(isCanceled)
			return;
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
			mNotification=(Notification) ObjectHelper.get(mBuilderInvoke).m("getNotification");
		else
			mNotification=mBuilder.build();
		
		mNotification.flags |= mFlags;
	}
	
	public void notifyNotifi(){
		if(isCanceled)
			return;
		mManager.notify(id, mNotification);
		mIshowing = true;
	}
	
	public void setMax(int max){
		this.max=max;
	}
	
	public void setProgress(int progress){
		mProgress=progress;
		Long now = new Date().getTime();
		if(now - last_progress_time < 300 && mProgress!=max)
			return;
		last_progress_time = now;
		if (mIshowing) {
//			if (max==-1) {
//				throw new IllegalArgumentException("Set total value for progress first!");
//			}
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
				ObjectHelper.get(mBuilderInvoke).m("setProgress", 
						new Class[]{int.class, int.class, boolean.class}, 
						new Object[]{(int)max , progress , false});
			else{
//				mBuilder.setContentInfo(""+progress+"%");
				long p=mProgress*100/max;
				mBuilder.setContentText(mMessage+"  "+p+"%");
			}
		}		
	}
	
	
	/**
	 * call this to hide and remove a notification
	 */
	public void cancel(){
		isCanceled=true;
		mManager.cancel(id);
		mIshowing = false;
		mContentView = null;
	}
	
	public void setAutoCancel(boolean autoCancel){
		setFlag(Notification.FLAG_AUTO_CANCEL,autoCancel);
	}
	
	private void setFlag(int mask, boolean value) {
        if (value) {
            mFlags |= mask;
        } else {
            mFlags &= ~mask;
        }
    }

	public int getId() {
		return id;
	}
	
	public boolean isShowing()
	{
		return mIshowing;
	}
	
	public boolean isCanceled()
	{
		return isCanceled;
	}
}
