package com.tlc.notification;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button mBtn;
	private boolean mIsDowning;
	private DownloadThread mThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBtn = (Button)this.findViewById(R.id.btn_show);
		mBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mIsDowning) return;
				DownloadNotificationHelper.getInstatnce(getApplicationContext()).show(new File(getCacheDir(), "xxx.apk").getAbsolutePath());
				startDownload();
			}
		});
	}

	
	private void startDownload(){
		mIsDowning = true;
		mThread = new DownloadThread();
		mThread.start();
		
	}
	
	/**
	 * 下载线程
	 */
	class DownloadThread extends Thread {

		@Override
		public void run() {
			int now_progress = 0;
			// Do the "lengthy" operation 20 times
			while (now_progress < 100) {
				now_progress += 10;
				try {
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {
				}
				DownloadNotificationHelper.getInstatnce(getApplicationContext()).update(100, now_progress);
			}
			
			mIsDowning = false;
			DownloadNotificationHelper.getInstatnce(getApplicationContext()).complete();
		}
	}
}
