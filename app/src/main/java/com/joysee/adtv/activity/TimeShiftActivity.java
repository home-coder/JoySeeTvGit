package com.joysee.adtv.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DisplayMode;
import com.joysee.adtv.common.DefaultParameter.NotificationAction.TunerStatus;
import com.joysee.adtv.common.DvbUtil;
import com.joysee.adtv.controller.TimeShiftController;
import com.joysee.adtv.controller.TimeShiftController.MediaInfoUpdateListener;
import com.joysee.adtv.controller.TimeShiftController.OnPlayListener;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.DVBPlayManager.OnMonitorListener;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MediaInfo;
import com.joysee.adtv.ui.PlayControlPopupWindow;
import com.joysee.adtv.ui.PlayControlPopupWindow.CallBackInterface;

public class TimeShiftActivity extends Activity implements OnMonitorListener,
		OnPlayListener, CallBackInterface ,MediaInfoUpdateListener{
	public static final String ISTHIMESHIFT = "istimeshifit";
	private static final String TAG = TimeShiftActivity.class
			.getCanonicalName();
	/**无信号*/
	private static final int SHOW_SYMBOL_WINDOW = 1;
	private static final int SHOW_BLANK_DIALOG = 2;
	private static final int SHOW_NOT_START_DIALOG = 3;
	private static final int SHOW_SELECT_DIALOG = 4;
	private static final int SHOW_STARTING_PLAY_DIALOG = 5;
	private static final int TIME_SHIFT_PLAY_FAILE = 6;
	private static final int SHOW_EXIT_DIALOG = 7;

	private static final int SHOW_NET_ERROR_DIALOG = 8;
	private static final int SHOW_NET_DISCONNECTED_DIALOG = 9;
	private static final int SHOW_EXE_STOP_DIALOG = 10;
	private static final int SHOW_OPERATE_FAILE_DIALOG = 11;
	private static final int SHOW_SEEKTO_SUCCESS_DIALOG = 12;
	private static final int SHOW_PLAY_BACK_BEGIN_DIALOG = 14;
	private static final int SHOW_PLAY_TO_END_DIALOG = 15;
	private static final int SHOW_NO_NEXT_DIALOG = 16;

	private static final int MSG_SET_INTERLACE_FALSE = 4;
	private static final int MSG_SET_INTERLACE_TRUE = 5;
	private static final int MSG_DISMISS_NOT_START_DIALOG = 6;

	private static final int NO_SINGLE_EXIT_PLAY_TIME = 10000;
	private boolean isSeekToBegin = false;
	private DVBPlayManager mDvbPlayManager;
	private VideoView mVideoView;
	private ChannelManager mChannelManager;
	private static int mScreenWidth = 490;
	private static int mScreenHeight = 810;
	private TimeShiftController mTimeShiftController;
	private PlayControlPopupWindow mPlayControlPopupWindow;

	private boolean isTimeShift = true;
	private boolean isFinished = false;
	private boolean isBackToHome = false;
	private DvbService service;
	private ScreenBroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle arg0) {
		Log.d(TAG, "onCreate -------------------------");
		super.onCreate(arg0);
		setContentView(R.layout.time_shift_activity);
		// Intent intent=getIntent();
		// if(intent!=null){
		// isTimeShift=intent.getBooleanExtra(ISTHIMESHIFT, true);
		// }
		managerInit();
		initMetrics();
		initView();
        broadcastReceiver = new ScreenBroadcastReceiver();
        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, filter);
	}
	
	private void initMetrics() {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(mDisplayMetrics);
		mScreenHeight = mDisplayMetrics.heightPixels;
		mScreenWidth = mDisplayMetrics.widthPixels;
	}
	
	private void showTimeShiftWindow() {
		Log.d(TAG,"showTimeShiftWindow()");
		DvbService service = new DvbService();
		mChannelManager.nativeGetCurrentService(service);
		if(mPlayControlPopupWindow == null){
			mPlayControlPopupWindow = new PlayControlPopupWindow(
					this, mScreenWidth, mScreenHeight,isTimeShift,this);
		}
		mPlayControlPopupWindow.setService(service);
		mPlayControlPopupWindow.isDestory(false);
		if(isTimeShift){
			setSingle();
			if(!haveSignal){
				showDialog(SHOW_SYMBOL_WINDOW);
//				Message message=Message.obtain();
//				message.what=NO_SINGLE_EXIT_PLAY;
//				mHandler.sendMessageDelayed(message, NO_SINGLE_EXIT_PLAY_TIME);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        removeDialog(SHOW_SYMBOL_WINDOW);
                        exitTimeShift();
                    }
                }, 3000);
			}else{
				showDialog(SHOW_STARTING_PLAY_DIALOG);
			}
			mTimeShiftController.play(service.getFrequency()+"",service.getServiceId()+"",this);
			System.out.println("play  ------Frequency="+service.getFrequency()+"  ServiceId=  "+service.getServiceId());
			/*new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mPlayControlPopupWindow.show();
				}
			}, 1000);*/
		}
		
//		mDvbPlayManager.playLast(ServiceType.TV);
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int tKeyCode = event.getKeyCode();
        Log.d(TAG, " dispatchKeyEvent tKeyCode = " + tKeyCode);
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (tKeyCode == KeyEvent.KEYCODE_HOME) {
                Log.d(TAG,
                        " KEYCODE_HOME ----------------------------------- finish()");
                finish();
            }
            if (tKeyCode == 268) {
                TimeShiftController.getInstance().pause();
                exitTimeShift();
            }
            if (tKeyCode == KeyEvent.KEYCODE_PROG_RED) {
                Log.d(TAG,
                        " KEYCODE_PROG_RED ----------------------------------- finish()");
                finish();
            }
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (tKeyCode == KeyEvent.KEYCODE_HOME) {
                isBackToHome = true;
            }
        }
        if (mPlayControlPopupWindow != null) {
            if (mPlayControlPopupWindow.isShowing()) {
                if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK || event
                        .getKeyCode() == KeyEvent.KEYCODE_ESCAPE)) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        TimeShiftController.getInstance().pause();
                        showDialog(SHOW_EXIT_DIALOG);
                    }
                    return true;
                } else {
                    mPlayControlPopupWindow.dispatchKeyEvent(event);
                }
            } else {
                if ((tKeyCode == KeyEvent.KEYCODE_BACK || tKeyCode == KeyEvent.KEYCODE_ESCAPE)) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        TimeShiftController.getInstance().pause();
                        showDialog(SHOW_EXIT_DIALOG);
                    }
                    return true;
                }
                if (tKeyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || tKeyCode == KeyEvent.KEYCODE_ENTER
                        || tKeyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        || tKeyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                        || tKeyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || tKeyCode == KeyEvent.KEYCODE_DPAD_UP
                        || tKeyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                    if (mPlayControlPopupWindow.isShowing()) {
                        mPlayControlPopupWindow.dispatchKeyEvent(event);
                    } else {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            mPlayControlPopupWindow.show();
                        }
                    }
                    return true;
                }
            }
        } else {
            mPlayControlPopupWindow = new PlayControlPopupWindow(this,
                    mScreenWidth, mScreenHeight, isTimeShift, this);
            mPlayControlPopupWindow.show();
            Log.d(TAG, "show mPlayControlPopupWindow");
        }
        return super.dispatchKeyEvent(event);
    }

	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, " onKeyUp keyCode = " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                showDialog(SHOW_NOT_START_DIALOG);
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                if (mPlayControlPopupWindow != null
                        && !mPlayControlPopupWindow.isShowing()) {
                    TimeShiftController.getInstance().pause();
                    this.showDialog(SHOW_EXIT_DIALOG);
                } else if (mPlayControlPopupWindow != null) {
                    mPlayControlPopupWindow.dismiss();
                }
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

	private void initView() {
		mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                mController.resetScreenMode();
                Log.d(TAG,"surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
//                mController.resetWindowSize();
            	Log.d(TAG,"surfaceCreated");
                initSurface(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            	Log.d(TAG,"surfaceChanged");
            }

            private void initSurface(SurfaceHolder h) {
                Canvas c = null;
                try {
                	Log.d(TAG,"initSurface");
                    c = h.lockCanvas();
                } finally {
                    if (c != null) {
                        h.unlockCanvasAndPost(c);
                    }
                }
            }
        });
        mVideoView.getHolder().setFormat(PixelFormat.VIDEO_HOLE);
	}


	private void managerInit() {
		mDvbPlayManager = DVBPlayManager.getInstance(this);
		mDvbPlayManager.setOnMonitorListener(this);
		mDvbPlayManager.init();
		mChannelManager = ChannelManager.getInstance();
        service = new DvbService();
        mChannelManager.nativeGetCurrentService(service);
		mTimeShiftController = TimeShiftController.getInstance();
		mTimeShiftController.initAudioManager(this);
	}
	
	
	@Override
    protected void onResume() {
        Log.d(TAG, "onResume -------------------------");
        DvbUtil.resetWindowSize();
        DvbUtil.switchDeinterlace(true);
        DvbUtil.setRealVideoOnOff(true);
        showDialog(SHOW_STARTING_PLAY_DIALOG);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showDialog(SHOW_BLANK_DIALOG);
                showTimeShiftWindow();
            }
        }, 300);
        TimeShiftController.getInstance().setMediaInfoUpdateListener(this);
        isFinished = false;
        super.onResume();
    }

    private static final String BACK_TO_LAUNCHER = "/cache/launcher_needplay";
    private static final String DVB_PLAYED = "/cache/dvb_played";
    @Override
    protected void onPause() {
        super.onPause();
        isFinished = true;
        mPlayControlPopupWindow.isDestory(true);
        Log.d(TAG, "onPause ------------------------- isBackToHome = " + isBackToHome);
        TimeShiftController.getInstance().stop();
        if (isBackToHome) {
            SettingManager.getSettingManager().nativeSetVideoAspectRatio(
                    DisplayMode.DISPLAYMODE_16TO9);
            DvbUtil.writeFile(DVB_PLAYED, "1");
            DvbUtil.writeFile(BACK_TO_LAUNCHER, "0");
            DvbUtil.switchDeinterlace(false);
            DvbUtil.setRealVideoOnOff(false);
        }
        mChannelManager.nativeSetCurrentService(service);
        Log.d(TAG, "onPause -------------------------end ");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "-----onStop---");
    }

    private boolean isUninited = false;

    private void uninit() {
        Log.d(TAG, " ---uninit--- isUninited = " + isUninited);
        if (!isUninited) {
//            mTimeShiftController.stop();
            mPlayControlPopupWindow.isDestory(true);
            isUninited = true;
        }
    }

	private void setDeinterlace(boolean b){
		DvbUtil.switchDeinterlace(b);
	    DvbUtil.setRealVideoOnOff(b);
	}
	public static final int NO_SINGLE_EXIT_PLAY = 1;
	public static final int EXIT_PLAY = 2;
	public static final int DISMIS_DIALOG = 3;
	public static final int DISMIS_SEEKTO_SUCCESS_DIALOG=7;
	public static final int REMOVE_NO_NEXT_DIALOG=8;
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_SET_INTERLACE_FALSE:
				setDeinterlace(false);
				break;
			case MSG_SET_INTERLACE_TRUE:
				setDeinterlace(true);
				break;
			case MSG_DISMISS_NOT_START_DIALOG:
				dismissDialog(SHOW_NOT_START_DIALOG);
				exitTimeShift();
				break;
			case EXIT_PLAY:
				disDialog();
				exitTimeShift();
				break;
			case DISMIS_DIALOG:
				removeDialog(SHOW_OPERATE_FAILE_DIALOG);
				break;
			case DISMIS_SEEKTO_SUCCESS_DIALOG:
				removeDialog(SHOW_SEEKTO_SUCCESS_DIALOG);
				break;
			case REMOVE_NO_NEXT_DIALOG:
				System.out.println("remove_no_next_dialog");
				removeDialog(SHOW_NO_NEXT_DIALOG);
				TimeShiftController.getInstance().resume_play();//恢复播放
				break;
			case NO_SINGLE_EXIT_PLAY:
                removeDialog(SHOW_SYMBOL_WINDOW);
                exitTimeShift();
                break;
			}
		}
		
	};
	private boolean setSingle(){
		if(mDvbPlayManager.getTunerSignalStatus()==0){
			//有信号
			haveSignal=true;
		}else if(mDvbPlayManager.getTunerSignalStatus()==1){
			haveSignal=false;
		}else{
			//失败
		    haveSignal=false;
		}
		return haveSignal;
	}

	private boolean haveSignal = true;

	@Override
	public void onMonitor(int monitorType, Object message) {
		Log.d(TAG, " onMonitor monitorType = " + monitorType + "   message = "
				+ message);
		switch (monitorType) {
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TUNER_SIGNAL:
			int code = (Integer) message;
			if(code == TunerStatus.ACTION_TUNER_UNLOCKED){/** tuner 有信号*/
				haveSignal=true;
				mHandler.removeMessages(NO_SINGLE_EXIT_PLAY);
				removeDialog(SHOW_SYMBOL_WINDOW);
			}else{/** tuner 无信号*/
				haveSignal=false;
				this.showDialog(SHOW_SYMBOL_WINDOW);
				Message msg=Message.obtain();
				msg.what=NO_SINGLE_EXIT_PLAY;
				mHandler.sendMessageDelayed(msg, NO_SINGLE_EXIT_PLAY_TIME);
			}
			break;
		}
	}
//	private Dialog mErrorNotifyWindow;
//	private View mSymbolNotifyView;
	private Dialog showSymbolWindow(){
		View mSymbolNotifyView;
		Dialog mErrorNotifyWindow;
		mErrorNotifyWindow = new Dialog(this, R.style.dvbErrorDialogTheme);
		mSymbolNotifyView = this.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		final int windowHeight = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
//		mErrorNotifyWindow.show();
		return mErrorNotifyWindow;
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    disDialog();
	    dismissDialog();
		switch(id){
		case SHOW_SYMBOL_WINDOW:
			return showSymbolWindow();
		case SHOW_EXIT_DIALOG:
			return showExitDialog();
		case SHOW_BLANK_DIALOG:
			return showBlankView();
		case SHOW_NOT_START_DIALOG:
			Message msg=Message.obtain();
			msg.what=MSG_DISMISS_NOT_START_DIALOG;
			mHandler.sendMessageDelayed(msg, 3000);
			return showNotStartDialog();
		case SHOW_SELECT_DIALOG:
			if(mPlayControlPopupWindow!=null && !mPlayControlPopupWindow.isShowing()){
				mPlayControlPopupWindow.show();
			}
			return showSelectDialog();
		case PlayControlPopupWindow.CallBackInterface.SHOW_PLAY_BEGIN_TIME_DIALOG:
			if(mPlayControlPopupWindow!=null && !mPlayControlPopupWindow.isShowing()){
				mPlayControlPopupWindow.show();
			}
			return showPlayBeginTimeDialog();
		case SHOW_STARTING_PLAY_DIALOG:
			String start_play=this.getResources().getString(R.string.start_play);
			return showMessageDialog(start_play);
		case TIME_SHIFT_PLAY_FAILE:
			String time_shift_play_faile=this.getResources().getString(R.string.time_shift_play_faile);
			return showMessageDialog(time_shift_play_faile);
		case SHOW_NET_ERROR_DIALOG:
//			show_net_error_dialog=true;
			String text=this.getResources().getString(R.string.net_errr);
			return showMessageDialog(text);
		case SHOW_NET_DISCONNECTED_DIALOG:
			String net_stop=this.getResources().getString(R.string.net_stop);
			return showMessageDialog(net_stop);
		case SHOW_EXE_STOP_DIALOG:
			net_stop=this.getResources().getString(R.string.net_stop);
			return showMessageDialog(net_stop);
		case SHOW_OPERATE_FAILE_DIALOG:
			String operate_faile=this.getResources().getString(R.string.timeshift_operate_faile);
			return showMessageDialog(operate_faile);
		case SHOW_SEEKTO_SUCCESS_DIALOG:
			String timeshift_seekto_success=null;
			if(isSeekToBegin){
				timeshift_seekto_success=this.getResources().getString(R.string.timeshift_replay_success);
				isSeekToBegin=false;
			}else{
//				isSeekToBegin=false;
				timeshift_seekto_success=this.getResources().getString(R.string.timeshift_seekto_success);
			}
//			String timeshift_seekto_success=this.getResources().getString(R.string.timeshift_seekto_success);
			return showMessageDialog(timeshift_seekto_success);
		case SHOW_PLAY_BACK_BEGIN_DIALOG:
			if(mPlayControlPopupWindow!=null && !mPlayControlPopupWindow.isShowing()){
				mPlayControlPopupWindow.show();
			}
			return show_play_back_begin_dialog(false);
		case SHOW_PLAY_TO_END_DIALOG:
			if(mPlayControlPopupWindow!=null && !mPlayControlPopupWindow.isShowing()){
				mPlayControlPopupWindow.show();
			}
			return show_play_back_begin_dialog(true);
		case SHOW_NO_NEXT_DIALOG:
			String no_next=this.getResources().getString(R.string.no_next);
			return showMessageDialog(no_next);
		}
		return super.onCreateDialog(id);
	}
	
	private Dialog showMessageDialog(String msg){
		if(msg==null){
			return null;
		}
		View mSymbolNotifyView;
		Dialog mErrorNotifyWindow;
		mErrorNotifyWindow = new Dialog(this, R.style.dvbErrorDialogTheme);
		mSymbolNotifyView = this.getLayoutInflater().inflate(R.layout.dvb_symbol_notify_layout, null);
		TextView textview=(TextView)mSymbolNotifyView.findViewById(R.id.test);
		textview.setText(msg);
		final int windowHeight = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_height);
		final int windowWidth = (int) this.getResources().getDimension(R.dimen.alert_dialog_no_button_width);
		mErrorNotifyWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		mErrorNotifyWindow.setContentView(mSymbolNotifyView, new LayoutParams(windowWidth, windowHeight));
		mErrorNotifyWindow.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
		return mErrorNotifyWindow;
	}
	
	private Dialog showExitDialog() {
		final Dialog mExitDialog;
    	mExitDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.timeshift_exit_dialog_layout, null);
    	TextView tv = (TextView) view.findViewById(R.id.alert_title);
    	tv.setText(R.string.confirm_exit_timeshift);
    	final Button btOk = (Button) view.findViewById(R.id.alert_confirm_btn);
    	Button btCancel = (Button) view.findViewById(R.id.alert_cancle_btn);
    	btOk.setText(R.string.time_shift_confirm);
    	btCancel.setText(R.string.time_shift_cancel);
    	btOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				isTimeShift = false;
                // 退出时移，恢复直播
//                mController.switchToCurentChannel();
				exitTimeShift();
				mExitDialog.dismiss();
			}
		});
    	btCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mExitDialog.dismiss();
//				TimeShiftController.getInstance().resume_play();
                if (mPlayControlPopupWindow != null
                        && !mPlayControlPopupWindow.isShowing()) {
                    mPlayControlPopupWindow.show();
                }
			}
		});
    	mExitDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				btOk.requestFocus();
				System.out.println("onShow  ------------");
			}
		});
    	mExitDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.timeshift_exit_width);
        int height = (int) getResources().getDimension(R.dimen.timeshift_exit_heigth);
    	mExitDialog.setContentView(view, new LayoutParams(width, height));
//    	mExitDialog.show();
    	return mExitDialog;
	}
	
	private Dialog show_play_back_begin_dialog(final boolean isEnd) {
		final Dialog mExitDialog;
    	mExitDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.alert_exit_dialog_layout, null);
    	TextView tv = (TextView) view.findViewById(R.id.alert_title);
    	if(isEnd){
    		tv.setText(R.string.play_to_end_dialog_msg);
    	}else{
    		tv.setText(R.string.play_back_begin_dialog_msg);
    	}
    	final Button btOk = (Button) view.findViewById(R.id.alert_confirm_btn);
    	Button btCancel = (Button) view.findViewById(R.id.alert_cancle_btn);
    	btOk.setText(R.string.time_shift_confirm);
    	btCancel.setText(R.string.time_shift_cancel);
    	btOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isEnd){
					isSeekToBegin=true;
					TimeShiftController.getInstance().seekTo(0);
				}else{
					TimeShiftController.getInstance().play_or_pause();
				}
				mExitDialog.dismiss();
			}
		});
    	btCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mExitDialog.dismiss();
				exitTimeShift();
			}
		});
    	mExitDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				btOk.requestFocus();
				System.out.println("onShow  ------------");
			}
		});
    	mExitDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.dvb_exit_width);
        int height = (int) getResources().getDimension(R.dimen.dvb_exit_heigth);
    	mExitDialog.setContentView(view, new LayoutParams(width, height));
//    	mExitDialog.show();
    	return mExitDialog;
	}
	
	private Dialog showNotStartDialog() {
		final Dialog notStartDialog;
    	notStartDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.timeshift_not_start_dialog_layout, null);
    	Button previous_btn = (Button) view.findViewById(R.id.previous_btn);
    	Button cancle_btn = (Button) view.findViewById(R.id.cancle_btn);
    	/*previous_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//播放上一段
				TimeShiftController.getInstance().play_previous();
				notStartDialog.dismiss();
			}
		});
    	cancle_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitTimeShift();
				notStartDialog.dismiss();
			}
		});*/
    	
    	int width = (int) getResources().getDimension(R.dimen.time_shift_not_start_width);
        int height = (int) getResources().getDimension(R.dimen.time_shift_not_start_heigth);
    	notStartDialog.setContentView(view, new LayoutParams(width, height));
    	return notStartDialog;
	}
	
	private Dialog showPlayBeginTimeDialog() {
		final Dialog notStartDialog;
    	notStartDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.timeshift_not_start_dialog_layout, null);
    	TextView time_shift_text= (TextView) view.findViewById(R.id.time_shift_text);
    	time_shift_text.setText(this.getResources().getString(R.string.confirm_play_begintime_timeshift));
    	LinearLayout btn_layout= (LinearLayout) view.findViewById(R.id.btn_layout);
    	btn_layout.setVisibility(View.VISIBLE);
    	final Button previous_btn = (Button) view.findViewById(R.id.previous_btn);
    	previous_btn.setText(this.getResources().getString(R.string.time_shift_confirm));
    	Button cancle_btn = (Button) view.findViewById(R.id.cancle_btn);
    	previous_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//从头播放
				if(mPlayControlPopupWindow!=null){
					mPlayControlPopupWindow.setSeekBar(0);
				}
				isSeekToBegin=true;
				TimeShiftController.getInstance().seekTo(0);//跳到开始位置
				notStartDialog.dismiss();
			}
		});
    	cancle_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				exitTimeShift();
				notStartDialog.dismiss();
				TimeShiftController.getInstance().resume_play();
			}
		});
    	notStartDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				previous_btn.requestFocus();
				System.out.println("onShow  ------------");
			}
		});
    	notStartDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.time_shift_not_start_width);
        int height = (int) getResources().getDimension(R.dimen.time_shift_not_start_heigth);
    	notStartDialog.setContentView(view, new LayoutParams(width, height));
    	return notStartDialog;
	}
	
	private Dialog showSelectDialog() {
		final Dialog selectDialog;
    	selectDialog = new Dialog(this,R.style.alertDialogTheme);
    	View view  = View.inflate(this, R.layout.timeshift_select_dialog_layout, null);
    	final Button play_previous_btn = (Button) view.findViewById(R.id.play_previous_btn);
    	Button play_next_btn = (Button) view.findViewById(R.id.play_next_btn);
    	Button stop_play_btn = (Button) view.findViewById(R.id.stop_play_btn);
    	Button cancle_btn = (Button) view.findViewById(R.id.cancle_btn);
    	play_previous_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//播放上一段
				TimeShiftController.getInstance().play_previous();
				selectDialog.dismiss();
				showDialog(SHOW_STARTING_PLAY_DIALOG);
			}
		});
    	play_next_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(TimeShiftController.getInstance().haveNext()){
					//播放下一段
					TimeShiftController.getInstance().play_next();
					showDialog(SHOW_STARTING_PLAY_DIALOG);
				}else{
					showDialog(SHOW_NO_NEXT_DIALOG);//显示没有下一个提示
					Message msg=Message.obtain();
					msg.what=REMOVE_NO_NEXT_DIALOG;
					mHandler.sendMessageDelayed(msg, 3000);
				}
				selectDialog.dismiss();
				
			}
		});
    	stop_play_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitTimeShift();
				selectDialog.dismiss();
			}
		});
    	cancle_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectDialog.dismiss();
				TimeShiftController.getInstance().resume_play();
			}
		});
    	selectDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_ESCAPE){
					return true;
				}
				return false;
			}
		});
    	selectDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				play_previous_btn.requestFocus();
				System.out.println("onShow  ------------");
			}
		});
    	int width = (int) getResources().getDimension(R.dimen.time_shift_select_width);
        int height = (int) getResources().getDimension(R.dimen.time_shift_select_heigth);
        selectDialog.setContentView(view, new LayoutParams(width, height));
    	return selectDialog;
	}
	
    private void exitTimeShift() {
        Log.d(TAG, "------ exitTimeShift ");
        dismissDialog();
        disDialog();
        uninit();
        // mDvbPlayManager.playLast(ServiceType.TV);
        Intent intent = new Intent(TimeShiftActivity.this, DvbMainActivity.class);
        startActivity(intent);
        TimeShiftActivity.this.finish();
    }
	
	
    private Dialog showBlankView() {
        Dialog mBlankDialog;
        View mBlankView;
        Log.d(TAG, "showBlankView----------------");
        mBlankDialog = new Dialog(this, R.style.dvbErrorDialogTheme);
        mBlankView = this.getLayoutInflater().inflate(R.layout.blank, null);
        final int windowHeight = (int) this.getResources().getDimension(
                R.dimen.alert_dialog_no_button_height);
        final int windowWidth = (int) this.getResources().getDimension(
                R.dimen.alert_dialog_no_button_width);
        mBlankDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mBlankDialog.setContentView(mBlankView, new LayoutParams(windowWidth, windowHeight));
        // mBlankDialog.show();
        return mBlankDialog;
    }
	/*private void dismissBlankView(){
		if(mBlankDialog != null && mBlankDialog.isShowing()){
			mBlankDialog.dismiss();
		}
	}*/
	private void sendMessage(int what,int time){
		Message msg=Message.obtain();
		msg.what=what;
		mHandler.sendMessageDelayed(msg, time);
	}
	private void disDialog(){
		try{
			removeDialog(SHOW_STARTING_PLAY_DIALOG);//5
			removeDialog(TIME_SHIFT_PLAY_FAILE);//6
			removeDialog(SHOW_SYMBOL_WINDOW);//1
			removeDialog(SHOW_NET_ERROR_DIALOG);//8
			removeDialog(SHOW_NET_DISCONNECTED_DIALOG);//9
			removeDialog(SHOW_EXE_STOP_DIALOG);//10
			removeDialog(SHOW_OPERATE_FAILE_DIALOG);//11
			removeDialog(SHOW_SEEKTO_SUCCESS_DIALOG);//12
			removeDialog(SHOW_BLANK_DIALOG);//2
			removeDialog(SHOW_NOT_START_DIALOG);//3
			removeDialog(SHOW_NO_NEXT_DIALOG);//16
		}catch(IllegalArgumentException e){
			e.printStackTrace();
		}
		
	}
	/**
	 * 清除屏蔽返回键的对话框
	 */
	private void dismissDialog(){
		removeDialog(SHOW_EXIT_DIALOG);//7
		removeDialog(SHOW_SELECT_DIALOG);//4
		removeDialog(SHOW_PLAY_BACK_BEGIN_DIALOG);//14
		removeDialog(SHOW_PLAY_TO_END_DIALOG);//15
	}
	@Override
	public void onMonitorTimeShift(int operate, int result) {
		Log.d(TAG, " onMonitorTimeShift operate = " + operate + " result = "
				+ result);
		if (isFinished) {
			Log.d(TAG, " onMonitor isFinished = " + isFinished);
			return;
		}
		switch (operate) {
		case TimeShiftController.NotificationAction.PLAY:
		case TimeShiftController.NotificationAction.PGDOWN:
		case TimeShiftController.NotificationAction.PGUP:
			if (result == TimeShiftController.NotificationAction.SUCCESS) {
				removeDialog(SHOW_STARTING_PLAY_DIALOG);
				MediaInfo mediaInfo = TimeShiftController.getInstance()
						.getLocalMediaInfo();
				mPlayControlPopupWindow.setMediaInfo(mediaInfo);
				if (mPlayControlPopupWindow != null
						&& !mPlayControlPopupWindow.isShowing()) {
					mPlayControlPopupWindow.show();
				}
				if (mediaInfo != null) {
					Log.d(TAG,
							"play success  mMediaInfo = "
									+ mediaInfo.toString());
				} else {
					Log.d(TAG, "play success  mMediaInfo = " + mediaInfo);
				}
			} else if (result == TimeShiftController.NotificationAction.FAIL) {
				Log.d(TAG, "play fail");
				removeDialog(SHOW_STARTING_PLAY_DIALOG);
				showDialog(TIME_SHIFT_PLAY_FAILE);
				sendMessage(EXIT_PLAY,3000);
			}
			break;
		case TimeShiftController.NotificationAction.FAST_PLAY:
			/**
			 * 快进、快退时，0是失败，非0是成功，成功时result就是当前的倍速，result<0是快退，result>0是快进
			 */
			if(result==TimeShiftController.NotificationAction.SUCCESS){
				showDialog(SHOW_OPERATE_FAILE_DIALOG);
				sendMessage(DISMIS_DIALOG,5000);
			}
			break;
		case TimeShiftController.NotificationAction.NET_ERROR://网路错误、断开（操作中，土司）
			showDialog(SHOW_NET_ERROR_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.NET_DISCONNECTED://心跳断开
			/*String net_stop=this.getResources().getString(R.string.net_stop);
			ToastUtil.showToast(this, net_stop);
			exitTimeShift();*/
			showDialog(SHOW_NET_DISCONNECTED_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.PLAY_BACK_BEGIN:////快退到头
			TimeShiftController.getInstance().pause();
			showDialog(SHOW_PLAY_BACK_BEGIN_DIALOG);
			break;
		case TimeShiftController.NotificationAction.VOD_TO_END://影片结束
			Log.d(TAG,
					" getProgress =  "
							+ mPlayControlPopupWindow.mSeekBar.getProgress()
							+ " getSecondaryProgress = "
							+ mPlayControlPopupWindow.mSeekBar.getSecondaryProgress());
//			if(mPlayControlPopupWindow.mSeekBar.getProgress()>900){
				TimeShiftController.getInstance().pause();
				showDialog(SHOW_PLAY_TO_END_DIALOG);
//			}
			break;
		case TimeShiftController.NotificationAction.PLAY_TO_CURRENT_RECORD://快进到录制点
			
			break;
		case TimeShiftController.NotificationAction.EXE_STOP://需要客户端停止服务
			showDialog(SHOW_EXE_STOP_DIALOG);
			sendMessage(EXIT_PLAY,3000);
			break;
		case TimeShiftController.NotificationAction.SEEKTO:
			if(result==0){//跳转成功
				showDialog(SHOW_SEEKTO_SUCCESS_DIALOG);
				sendMessage(DISMIS_SEEKTO_SUCCESS_DIALOG,3000);
				if(mPlayControlPopupWindow!=null && mPlayControlPopupWindow.isShowing()){
					mPlayControlPopupWindow.disMissJumpView(5000);
				}
				MediaInfo mediaInfo = TimeShiftController.getInstance()
						.getLocalMediaInfo();
				mPlayControlPopupWindow.setMediaInfo(mediaInfo);
			}else{
				showDialog(SHOW_OPERATE_FAILE_DIALOG);
				sendMessage(DISMIS_DIALOG,3000);
			}
			break;
		}
	}

	@Override
	public void callBack(int flag) {
		if(flag==PlayControlPopupWindow.CallBackInterface.SHOW_SELECT_DIALOG){
		    TimeShiftController.getInstance().pause();
			showDialog(SHOW_SELECT_DIALOG);
		}else if(flag==PlayControlPopupWindow.CallBackInterface.SHOW_PLAY_BEGIN_TIME_DIALOG){
			TimeShiftController.getInstance().pause();
			showDialog(PlayControlPopupWindow.CallBackInterface.SHOW_PLAY_BEGIN_TIME_DIALOG);
		}
	}
	@Override
    public void onGetMediaInfo(MediaInfo mediaInfo) {
        Log.d(TAG, " onGetMediaInfo mediaInfo = " + mediaInfo);
        if (mPlayControlPopupWindow != null) {
            mPlayControlPopupWindow.setMediaInfo(mediaInfo);
        }
    }
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, " onDestroy");
	    unregisterReceiver(broadcastReceiver);
	}
	
	class ScreenBroadcastReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        Log.d("ScreenBroadcastReceiver", " onReceive action = " + action);
	        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
	            TimeShiftActivity.this.finish();
	        }
	    }
	}
}
