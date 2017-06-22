package com.joysee.adtv.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.activity.DvbMainActivity;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.controller.ViewController;


public class DVBErrorNotify implements IDvbBaseView {

	private static final String TAG = "com.joysee.adtv.ui.DVBErrorNotify";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
	private Activity mActivity;
	private Dialog mErrorNotifyWindow;
	private View mNoChannelNotifyView;
	private Button mNoChannelNotifySearchBtn = null;
	private Button mNoChannelNotifyCancleBtn = null;
	
	private View mSymbolNotifyView;
	private View mCaNotifyView;
	private View mBlankView;
	private TextView mCaNotifyText;
	private boolean mLastTunerStatus = true;
	private int mLastCaStatus = -1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_CHANNEL:
                    Intent intent = new Intent();
                    intent.setClassName("com.lenovo.settings",
                            "com.lenovo.settings.LenovoSettingsActivity");
                    Bundle bundle = new Bundle();
                    bundle.putInt("curChoice", 2);
                    bundle.putString("subChoice", "cctv-search");
                    intent.putExtras(bundle);
                    DvbMainActivity dvbMainActivity = (DvbMainActivity)mActivity;
                    dvbMainActivity.isTurnOffDeinterlace = true;
                    mActivity.startActivity(intent);
                    mActivity.finish();
                    break;
                case DISMISS_ERROR_CHANNEL_DIALOG:
                    if(mErrorChannelDialog != null && mErrorChannelDialog.isShowing()){
                        mErrorChannelDialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
	
	private ViewController mViewController;
	
	public DVBErrorNotify(Activity activity){
		this.mActivity = activity;
	}
	
	private static final int SEARCH_CHANNEL = 0x101;
	private void showNoChannelDialog(final int type) {
		if (mNoChannelNotifyView == null) {
			mNoChannelNotifyView = mActivity.getLayoutInflater().inflate(R.layout.alert_dialog_include_button_layout,
					null);
			mNoChannelNotifyCancleBtn = (Button) mNoChannelNotifyView.findViewById(R.id.cancle_btn);
			mNoChannelNotifySearchBtn = (Button) mNoChannelNotifyView.findViewById(R.id.confirm_btn);
			mNoChannelNotifySearchBtn.setText(R.string.dvb_nochannel_notify_search_btn);
			mNoChannelNotifyCancleBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					DVBErrorNotify.this.removeAllNotify();
					mActivity.finish();
				}
			});
			OnKeyListener keyLis = new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					mHandler.removeMessages(SEARCH_CHANNEL);
					int action = event.getAction();
					switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
					case KeyEvent.KEYCODE_ESCAPE:
						if (action == KeyEvent.ACTION_DOWN) {
							DVBErrorNotify.this.removeAllNotify();
							mActivity.finish();
						}
						return true;
					default:
						break;
					}
					return false;
				}
			};
			mNoChannelNotifyCancleBtn.setOnKeyListener(keyLis);
			mNoChannelNotifySearchBtn.setOnKeyListener(keyLis);
			mNoChannelNotifySearchBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mHandler.removeMessages(SEARCH_CHANNEL);
					DVBErrorNotify.this.removeAllNotify();
//					final Intent intent = new Intent();
//					intent.setClass(mActivity, SearchMenuActivity.class);
					Intent intent = new Intent();
					intent.setClassName("com.lenovo.settings", "com.lenovo.settings.LenovoSettingsActivity");
					Bundle bundle = new Bundle();
					bundle.putInt("curChoice", 2);
					intent.putExtras(bundle);
					DvbMainActivity dvbMainActivity = (DvbMainActivity)mActivity;
					dvbMainActivity.isTurnOffDeinterlace = true;
					mActivity.startActivity(intent);
					mActivity.finish();
				}
			});
		}
		
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_include_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_include_button_width);
		mErrorNotifyWindow.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mNoChannelNotifyView, new LayoutParams(windowWidth, windowHeight));
		if(mErrorNotifyWindow != null && mErrorNotifyWindow.isShowing())
			return;
		mErrorNotifyWindow.show();
		mNoChannelNotifySearchBtn.requestFocus();
		mHandler.sendEmptyMessageDelayed(SEARCH_CHANNEL, 5000);
	}
	
	private void showSymbolWindow(){
		if (mSymbolNotifyView == null) {
			mSymbolNotifyView = mActivity.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		}
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.show();
	}
	private Dialog mBlankDialog;
	private void showBlankView(){
	    if(mBlankDialog != null && mBlankDialog.isShowing()){
	        mBlankDialog.dismiss();
	    }
	    if(mBlankDialog == null){
	        mBlankDialog = new Dialog(mActivity, R.style.dvbErrorDialogTheme);
	    }
	    if(mBlankView == null){
	        mBlankView = mActivity.getLayoutInflater().inflate(R.layout.blank, null);
	    }
        final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
        final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
        mBlankDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mBlankDialog.setContentView(mBlankView, new LayoutParams(windowWidth, windowHeight));
        mBlankDialog.show();
    }
	
    private Dialog mErrorChannelDialog;
    private View mErrorChannelView;
	public boolean isTunerEnable;
    private static final int DISMISS_ERROR_CHANNEL_DIALOG = 0x100;
    private void showErrorChannelDialog(int text) {
        if(mErrorChannelDialog != null && mErrorChannelDialog.isShowing()){
            mErrorChannelDialog.dismiss();
        }
        if(mErrorChannelDialog == null){
            mErrorChannelDialog = new Dialog(mActivity, R.style.dvbErrorDialogTheme);
        }
        if (mErrorChannelView == null) {
            mErrorChannelView = mActivity.getLayoutInflater().inflate(R.layout.error_channel_layout, null);
        }
        TextView textView = (TextView) mErrorChannelView.findViewById(R.id.notify_dialog_tv);
        textView.setText(mActivity.getResources().getString(R.string.dvb_without_this_channel, text));
        final int windowHeight = (int) mActivity.getResources().getDimension(
                R.dimen.alert_dialog_no_button_height);
        final int windowWidth = (int) mActivity.getResources().getDimension(
                R.dimen.alert_dialog_no_button_width);
        mErrorChannelDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mErrorChannelDialog.setContentView(mErrorChannelView, new LayoutParams(windowWidth, windowHeight));
        mErrorChannelDialog.show();
        mHandler.removeMessages(DISMISS_ERROR_CHANNEL_DIALOG);
        mHandler.sendEmptyMessageDelayed(DISMISS_ERROR_CHANNEL_DIALOG,3000);
    }
	
	private void showCAWindow(String notifyString){
		if (mCaNotifyView == null || mCaNotifyText == null) {
			mCaNotifyView = mActivity.getLayoutInflater().inflate(R.layout.dvb_ca_notify_layout, null);
			mCaNotifyText = (TextView) mCaNotifyView.findViewById(R.id.notify_text);
		}
		mCaNotifyText.setText(notifyString);
		final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mCaNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.show();
		
	}
	
    private void removeAllNotify() {
        if (mErrorNotifyWindow != null && mErrorNotifyWindow.isShowing()) {
            mErrorNotifyWindow.dismiss();
        }
        if (mErrorChannelDialog != null && mErrorChannelDialog.isShowing())
            mErrorChannelDialog.dismiss();
//        if(mBlankDialog != null &&  mBlankDialog.isShowing())
//            mBlankDialog.dismiss();
        mHandler.removeCallbacks(mHideNotifyTmporarily);
        log.D("removeAllNotify.!");
    }

	private void refreshDVBNotify(int type, boolean channel, boolean tuner, int ca) {
		log.D("tuner = " + tuner + " ; ca = " + ca + "; mLastCaStatus = " + mLastCaStatus + "; mLastTunerStatus = " + mLastTunerStatus);
		if(channel && mLastTunerStatus == false && mLastTunerStatus == tuner) {
			return;
		}
		removeAllNotify();
		if (channel && tuner && (ca==0)) {
			log.D( "DVBErrorNotify. dismiss ErrorWindow");
		} else {
			if (mErrorNotifyWindow == null) {
				mErrorNotifyWindow = new Dialog(mActivity, R.style.dvbErrorDialogTheme);
			}
			if (!channel) {
				showNoChannelDialog(type);
			} else if (!tuner) {
//				if (mViewController.getLastTunerParam() != -1) {
					showSymbolWindow();
//				}
			} else if (ca!=0 && ca != -1) {
//				if (mViewController.getLastCAParam() != -1) {
					String notifyStr = getCaNotifyString(ca);
					showCAWindow(notifyStr);
//				}
		}

			log.D( "DVBErrorNotify. show ErrorWindow");
		}
		mLastCaStatus = ca;
		mLastTunerStatus = tuner;
		if(!tuner){
		    mHandler.postDelayed(runnableImplementation, 1000);
		}
	}
	
	
	private void refreshDVBbackTv(int type, boolean channel, boolean tuner, int ca) {//chaidandan
		System.out.println("-------------------refreshDVBbackTv");
		System.out.println("tuner = " + tuner + " ; ca = " + ca + "; mLastCaStatus = " + mLastCaStatus + "; mLastTunerStatus = " + mLastTunerStatus);
		
		removeAllNotify();
		if (!channel) {
			showNoChannelDialog(type);
		} else if (!tuner) {
				showSymbolWindow();
		}
	}
	
	
	private RunnableImplementation runnableImplementation = new RunnableImplementation();
	private RunnablebackFormTv runnablebackFormTv = new RunnablebackFormTv();//chaidandan
	private final class RunnablebackFormTv implements Runnable{//chaidandan

		@Override
		public void run() {
			// TODO Auto-generated method stub
			 int tuner = mViewController.getTunerStatus();
			 int caStatus = mViewController.queryMsgType(DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG);
			if(tuner!= 0){
				refreshDVBbackTv(ServiceType.TV,true,isTunerEnable,caStatus);
			}
		}
		
	}
	
	private final class RunnableImplementation implements Runnable {
        @Override
        public void run() {
            isTunerEnable = mViewController.getTunerStatus()==0?true:false;
            int caStatus = mViewController.queryMsgType(DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG);
            if(!isTunerEnable){
                Log.d("songwenxuan","!isTunerEnable..........................");
                mHandler.postDelayed(this, 1000);
            }else{
                Log.d("songwenxuan","isTunerEnable..........................,remove notify");
                refreshDVBNotify(ServiceType.TV,true,isTunerEnable,caStatus);
            }
        }
    }
	
	public void hideNotifyViewTemporarily(long hideNotifyViewTemporarily){
		if (mErrorNotifyWindow != null && mErrorNotifyWindow.isShowing()) {
			mErrorNotifyWindow.dismiss();
			mHandler.postDelayed(mHideNotifyTmporarily, hideNotifyViewTemporarily);
		}
	}
	
	Runnable mHideNotifyTmporarily = new Runnable() {
		public void run() {
			try {
				if (mErrorNotifyWindow != null && !mErrorNotifyWindow.isShowing()) {
					mErrorNotifyWindow.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
    public String getCaNotifyString(int notify){
        
        switch(notify){
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_badcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE:
            
            return mActivity.getString(R.string.ca_message_blackout_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE:
            
            return mActivity.getString(R.string.ca_message_callback_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_cancel_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE:
            
            return mActivity.getString(R.string.ca_message_decryptfail_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_errcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE:
            
            return mActivity.getString(R.string.ca_message_errregion_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_expicard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_freeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE:
            
            return mActivity.getString(R.string.ca_message_insertcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE:
            
            return mActivity.getString(R.string.ca_message_lowcardver_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE:
            
            return mActivity.getString(R.string.ca_message_maxrestart_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE:
            
            return mActivity.getString(R.string.ca_message_needfeed_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE:
            
            return mActivity.getString(R.string.ca_message_noentitle_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE:
            
            return mActivity.getString(R.string.ca_message_nomoney_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE:
            
            return mActivity.getString(R.string.ca_message_nooper_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE:
            
            return mActivity.getString(R.string.ca_message_outworktime_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE:
            
            return mActivity.getString(R.string.ca_message_pairing_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE:
            
            return mActivity.getString(R.string.ca_message_stbfreeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE:
            
            return mActivity.getString(R.string.ca_message_stblocked_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE:
            
            return mActivity.getString(R.string.ca_message_update_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE:
            
            return mActivity.getString(R.string.ca_message_viewlock_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE:
            
            return mActivity.getString(R.string.ca_message_watchlevel_type);
        }
        
        return null;
    }
    private static final int TEMP_SHOW_TIME = 2700;
	private Object[] objs;
	@Override
	public void processMessage(Object sender,DvbMessage msg) {
		mViewController = (ViewController) sender;
		switch (msg.what) {
		case ViewMessage.STOP_PLAY: 
			Log.d("songwenxuan","STOP_PLAY");
			removeAllNotify();
			break;
		case ViewMessage.DVB_INIT_FAILED: 
			Log.d("songwenxuan","DVB_INIT_FAILED");
			ToastUtil.showToast(mActivity, R.string.dvb_init_failed);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mActivity.finish();
				}
			},2000);
			break;
		case ViewMessage.SHOW_EPG_TUNER_UNABLE: 
//            ToastUtil.showToast(mActivity, R.string.epg_tuner_unable);
//            showErrorChannelDialog(R.string.epg_tuner_unable);
            break;
		case ViewMessage.ERROR_WITHOUT_CHANNEL: 
			Log.d("songwenxuan","ERROR_WITHOUT_CHANNEL");
//			hideNotifyViewTemporarily(TEMP_SHOW_TIME);
//			ToastUtil.showToast(mActivity, R.string.dvb_without_this_channel);
			showErrorChannelDialog(msg.arg1);
			break;
		case ViewMessage.RECEIVED_ERROR_NOTIFY:
			Log.d("songwenxuan","RECEIVED_ERROR_NOTIFY*************************************************");
			objs = (Object[]) msg.obj;
			refreshDVBNotify((Integer)objs[0],(Boolean) objs[1], (Boolean)objs[2], (Integer)objs[3]);
			break;
//		case ViewMessage.SHOW_LIVE_GUIDE:
		case ViewMessage.SHOW_PROGRAM_GUIDE:
			Log.d("songwenxuan","SHOW_PROGRAM_GUIDE");
			removeAllNotify();
			break;
//		case ViewMessage.EXIT_LIVE_GUIDE:
//		case ViewMessage.EXIT_PROGRAM_GUIDE:
//			Log.d("songwenxuan","EXIT_PROGRAM_GUIDE");
//			if(objs != null){
//				refreshDVBNotify((Integer)objs[0],(Boolean) objs[1], (Boolean)objs[2], (Boolean)objs[3]);
//			}
//			break;
		case ViewMessage.SHOW_BLANK_VIEW:
		    showBlankView();
		    break;
		case ViewMessage.DISMISS_BLANK_VIEW:
		    if(mBlankDialog!=null && mBlankDialog.isShowing()){
                mBlankDialog.dismiss();
            }
		    break;
		    
		case ViewMessage.EXIT_DVB:
			mHandler.removeCallbacks(runnableImplementation);
			break;
			//chaidandan
		case ViewMessage.REFRESH_DVB_NOTIFY:
			Log.d("chaidandan", "==========================REFRESH_DVB_NOTIFY");
			mHandler.post(runnablebackFormTv);
			
		}
		
	}
}
