package com.joysee.adtv.webview;

import android.app.Instrumentation;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.joysee.adtv.common.HardwareTools;
import com.joysee.adtv.controller.TimeShiftController;

public class LookBackJs implements JsInterface{
	public static final String TAG="com.joysee.adtv.webview.LookBackJs";
	private LookBackActivity mContext;
	public LookBackJs(LookBackActivity context){
		mContext=context;
	}
	
	public void play(){
	    Log.d(TAG, " ---play----");
		TimeShiftController.getInstance().play(586000+"",106+"", null);
//		mContext.mHandler.sendEmptyMessage(LookBackActivity.PLAY);
	}

	/**
	 *url的例子：contentid=-1&tm_move=channel_20130423121500&mtv_vodip=192.168.0.62&return_url=http://192.168.0.63/eums
	 *return_url=后面的http://192.168.0.63/eums你们存下来，回看结束之后去跳转到这个链接
	 *contentid=后面到&return_url之前的部分“-1&tm_move=channel_20130423121500&mtv_vodip=192.168.0.62”作为evtc_vodplay接口的movieid参数传进来
	 */
	
	@Override
	public void setplayurl(String url) {
		Log.d(TAG, " ---setplayurl----url = " + url);
//		TimeShiftController.getInstance().play_look_back(url, null);
		if(url!=null){
			Message msg=new Message();
			msg.obj=url;
			msg.what=LookBackActivity.PLAY;
			mContext.mHandler.sendMessage(msg);
			Log.d(TAG, "url="+url);
		}else{
			Log.d(TAG, "url is null");
		}
		
	}

	@Override
	public String GetMACAddress() {
		String mac=HardwareTools.getMac(mContext);
		if(mac!=null){
			Log.d(TAG, " GetMACAddress  mac = "+mac);
			return mac.toLowerCase();
			
		}else{
			return null;
		}
	}

	@Override
	public String GetTSID(int freq, int sym, int qam) {
		// TODO Auto-generated method stub
		Log.d(TAG, " GetTSID freq-->"+freq+"  sym-->"+sym+"  qam-->"+qam);
		return mContext.getService().getTsId()+"";
	}

	@Override
	public void ExitBrowser() {
	    Log.d(TAG, " ExitBrowser  ");
		Message msg=Message.obtain();
		msg.what=LookBackActivity.EXIT_BROWSER;
		mContext.mHandler.sendMessage(msg);
	}

	@Override
	public void onMainPageReady() {
//		new Thread(new Runnable() {
//			public void run() {
//				Instrumentation inst = new Instrumentation();
//				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
//			}
//		}).start();
	}
	

//	@Override
//	public void TVplayForWindow(int freq, int symbolrate, int qam,
//			int videopid, int audiopid, int pcrpid, int x, int y, int width,
//			int height) {
//		// TODO Auto-generated method stub
//		Log.d(TAG, "TVplayForWindow");
//		DVBPlayManager.getInstance(mContext).setWinSize(x, y, width, height);
////		DVBPlayManager.getInstance(mContext).playLast(type, lastChannelNum);
//	}

}
