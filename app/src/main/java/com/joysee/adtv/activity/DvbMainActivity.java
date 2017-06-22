package com.joysee.adtv.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ActivityController;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.ui.BCMainBackground;
import com.joysee.adtv.ui.ChannelInfoView;
import com.joysee.adtv.ui.ChannelNumView;
import com.joysee.adtv.ui.DVBErrorNotify;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.Menu;
import com.joysee.adtv.ui.OsdPopupWindow;
import com.joysee.adtv.ui.ProgramReserveAlertWindow;
import com.joysee.adtv.ui.TimeShiftIcon;
import com.joysee.adtv.ui.VAINotifyWindow;
import com.joysee.adtv.webview.LookBackActivity;
/**
 * 播放主界面
 * @author wangguohua
 */
public class DvbMainActivity extends Activity {
	private static final String TAG = "DvbMainActivity-WuLEI";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
    private ActivityController mController;

	private boolean isKeyRepeating = false;
	private final int mKeyRepeatInterval = 150;
	private long mLastKeyDownTime = -1;
	
	/** 频道号 */
	private ChannelNumView mDVBChannelNumView;
	
	private static int mScreenWidth = 490;
	private static int mScreenHeight = 810;
	private boolean isTimeShift=false;
	
	private boolean isUserinputing;
	private int mUserInputNumber;
	public boolean isBackToHome;
	
	private Menu mMenu;
	private ImageView timeShiftIcon;
	private OsdPopupWindow mOsdPopupWindow;	
	private static final int NUM_CHANNEL_SWITCH=1;
	private static final int NUM_CHANNEL_SWITCH_NOW=2;
	private static final int SWITCH_CHANNEL_TIMEOUT=3000;
	

    private static final int MSG_UPDATE_WIFI_STATE = 3;
    private static final int MSG_UPDATE_WIFI_TIMEOUT = 5000;


	private long onResumeEndTime;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(mUserInputNumber<0)
				return;
			switch (msg.what) {
				case NUM_CHANNEL_SWITCH:
					Log.d("songwenxuan","NUM_CHANNEL_SWITCH");
					mController.switchChannelFromNum(mUserInputNumber,false);
					setTimeShiftImg();
					break;
				case NUM_CHANNEL_SWITCH_NOW:
					mController.switchChannelFromNum(mUserInputNumber,true);
					setTimeShiftImg();
					break;
			}
			isUserinputing=false;
			mUserInputNumber = 0;
		};
	};

    private Handler mWifiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_WIFI_STATE:
                    updateWifiState(mWifiManager.getWifiState());
                    break;
            }
        }
    };

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private WifiManager mWifiManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        long begin = System.currentTimeMillis();
        Log.d("WL", "DvbMainActivity onCreate()------------------------------------------");
        log.D("DvbMainActivity onCreate()------------------------------------------");
        super.onCreate(savedInstanceState);
        ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
        getWindow().setBackgroundDrawable(colorDrawable);
        setContentView(R.layout.dvb_main);
        mController = new ActivityController(this);
        Log.d("type", "--- type = " + mController.getClass());
        initViews();
        mController.init();
        isTurnOffDeinterlace = true;

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
        log.D("DvbMainActivity onCreate()------------------------------------------  time = "  + (System.currentTimeMillis() - begin));
    }

    private void updateWifiState(int enabledState) {
        Log.i(TAG, "------------ updateWifiState " + " enabledState = "
                + enabledState);
        switch (enabledState) {
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_ENABLED:
                mWifiManager.setWifiEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
                break;
        }
    }

    private void updateConnectionState(DetailedState state) {
        Log.d(TAG, "-----------updateConnectionState DetailedState = "
                + state);
        if (state == DetailedState.OBTAINING_IPADDR
                || state == DetailedState.CONNECTED
                || state == DetailedState.CONNECTING
                || state == DetailedState.AUTHENTICATING) {
            mWifiHandler.removeMessages(MSG_UPDATE_WIFI_STATE);
        } else if (state == DetailedState.SCANNING
                || state == DetailedState.IDLE
                || state == DetailedState.DISCONNECTED
                || state == DetailedState.DISCONNECTING) {
            mWifiHandler.sendEmptyMessageDelayed(MSG_UPDATE_WIFI_STATE,
                    MSG_UPDATE_WIFI_TIMEOUT);
        }
    }

    private void handleEvent(Context context, Intent intent) {
        DetailedState state = WifiInfo.getDetailedStateOf((SupplicantState) intent
                .getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
        updateConnectionState(state);
    }
    /**
     * 必须在切台播放后才能进行此函数调用
     */
    public void setTimeShiftImg(){
    	if(isTimeShift()){
        	System.out.println("0000000000000000000000");
			timeShiftIcon.setVisibility(View.VISIBLE);
		}else{
			System.out.println("11111111111111111");
			timeShiftIcon.setVisibility(View.GONE);
		}
    }
    private void initViews() {
        mVideoView = (VideoView) findViewById(R.id.dvb_videoview);
        mVideoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                log.D("surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                log.D("surfaceCreated");
                initSurface(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                log.D("surfaceChanged");
            }

            private void initSurface(SurfaceHolder h) {
                Canvas c = null;
                try {
                    log.D("initSurface");
                    c = h.lockCanvas();
                } finally {
                    if (c != null) {
                        h.unlockCanvasAndPost(c);
                    }
                }
            }
        });
        mVideoView.getHolder().setFormat(PixelFormat.VIDEO_HOLE);
        
    	ChannelInfoView mDVBChannelInfoView = (ChannelInfoView) findViewById(R.id.dvb_channelinfo_View);
		mDVBChannelNumView = (ChannelNumView) findViewById(R.id.dvb_channelnum_View);
		VAINotifyWindow mVAINotifyWindow = new VAINotifyWindow(this);
		DVBErrorNotify mDvbErrorNotify = new DVBErrorNotify(this);
		mOsdPopupWindow = new OsdPopupWindow(this);
		FrameLayout bcMainLayout = (FrameLayout) findViewById(R.id.dvb_bc_main_layout);
		EpgGuideWindow epgGuideView = new EpgGuideWindow(this); 
		ProgramReserveAlertWindow programReserveAlertWindow = new ProgramReserveAlertWindow(this);
		mMenu = new Menu(this);
		timeShiftIcon = (ImageView) findViewById(R.id.dvb_mainlayout_timeshift_icon_iv);
		TimeShiftIcon timeShiftIcon = new TimeShiftIcon(this);
		
		mController.registerView(timeShiftIcon);
		mController.registerView(mDVBChannelInfoView);
		mController.registerView(mDVBChannelNumView);
		mController.registerView(mVAINotifyWindow);
		mController.registerView(mDvbErrorNotify);
		mController.registerView(mOsdPopupWindow);
		mController.registerView(mMenu);
		mController.registerView(new BCMainBackground(bcMainLayout));
		mController.registerView(epgGuideView);
		mController.registerView(programReserveAlertWindow);
		
		ImageView emailIcon = (ImageView) findViewById(R.id.dvb_mainlayout_email_icon_iv);
		TextView fingerInfoTv = (TextView) findViewById(R.id.dvb_main_fingerinfo_tv);
		mOsdPopupWindow.setEmailcon(emailIcon);
		mOsdPopupWindow.setFingerInfoTv(fingerInfoTv);
		initMetrics();
	}

    private void initMetrics() {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(mDisplayMetrics);
		mScreenHeight = mDisplayMetrics.heightPixels;
		mScreenWidth = mDisplayMetrics.widthPixels;
	}
    public boolean isTurnOffDeinterlace = true;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        long time = SystemClock.uptimeMillis();
        Log.d(TAG, " onResumeBeginTime = " + onResumeEndTime + " time = " + time);
        //延时两秒解决在从回看回到看电视时接收到已经消耗的按键事件
        if ((time - onResumeEndTime) < 2000) {
            return true;
        }
        //如果弹出nit变化窗口屏蔽所有按键
        if(!mOsdPopupWindow.canDispatchKey()){
            return true;
        }
    	// 如果时移窗口isShowing按键事件交给时移窗口处理，否则，按正常逻辑走
        if(event.getKeyCode() == 183 && event.getAction() == KeyEvent.ACTION_UP){
            isTurnOffDeinterlace = true;
            finish();
        }
        if(event.getKeyCode() == KeyEvent.KEYCODE_HOME && event.getAction() == KeyEvent.ACTION_UP){
            Log.d("songwenxuan","event.getKeyCode() == KeyEvent.KEYCODE_HOME && event.getAction() == KeyEvent.ACTION_UP");
            isTurnOffDeinterlace = true;
            finish();
        }
		{
			int keyCode = event.getKeyCode();
			int action = event.getAction();
			log.D(" keyCode "+keyCode +" action "+action);
			switch (keyCode) {
			case 268:
			    Log.d("songwenxuan","27 dispatchkey ----------------------------------------");
			    isTurnOffDeinterlace = true;
			return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				 if (action == KeyEvent.ACTION_DOWN) {
						if (mController.isChannelEnable()) {
							final long currenKeyDownTime = SystemClock.uptimeMillis();
							final long interval = currenKeyDownTime - mLastKeyDownTime;
							if (isKeyRepeating && interval < mKeyRepeatInterval) {
								return true;
							}
							mLastKeyDownTime = currenKeyDownTime;
							isKeyRepeating = true;
							if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
								mController.changeVolume(-1);
							else
								mController.changeVolume(1);
							return true;
						}
					} else {
						if (mController.isChannelEnable()) {
							isKeyRepeating = false;
							return true;
						}
					}
				break;
			default:
				break;
			}
		}
    	
		return super.dispatchKeyEvent(event);
   }
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_F12){
			finish();
			return true;
		}
		
    	if (keyCode == KeyEvent.KEYCODE_HOME) {
    		
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_PAGE_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			final long currentKeyDownTime = SystemClock.uptimeMillis();
			final long interval = currentKeyDownTime - mLastKeyDownTime;
			if (isKeyRepeating && interval < mKeyRepeatInterval) {
				return true;
			}
			mUserInputNumber = 0;
			mLastKeyDownTime = currentKeyDownTime;
			isKeyRepeating = true;
			if (keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				Log.d("songwenxuan","next service begin = " + System.currentTimeMillis());
				mController.nextService();
			}else{
				mController.previousService();
			}
			setTimeShiftImg();
		}else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			executeNumKey(keyCode - KeyEvent.KEYCODE_0);
		} else if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
			if (event.isNumLockOn()) {
				executeNumKey(keyCode - KeyEvent.KEYCODE_NUMPAD_0);
			}
		} 
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onStart() {
    	super.onStart();
    	log.D("DvbMainActivity onStart()------------------------------------------");
    }
    @Override
    protected void onResume() {
        super.onResume();
        log.D("DvbMainActivity onResume()------------------------------------------");
        SystemProperties.set("tv.joysee.custom_video_axis", "1");
        Intent stopMusic = new Intent("com.android.music.musicservicecommand.pause");
        stopMusic.putExtra("command", "stop");
        sendBroadcast(stopMusic);
        mController.play(getIntent());
        mController.showBlankView();
        setTimeShiftImg();
        onResumeEndTime = SystemClock.uptimeMillis();
        registerReceiver(mReceiver, mFilter);
    }
    @Override
	public void onAttachedToWindow() {
        log.D("onAttachedToWindow()--------------------------------------------------");
		super.onAttachedToWindow();
		resolveIntent(getIntent());
	}
    
    @Override
    protected void onNewIntent(Intent intent) {
        log.D("onNewIntent()-----------------------------------------------------------------");
        setIntent(intent);
        resolveIntent(intent);
        super.onNewIntent(intent);
    }
    @Override
    protected void onPause() {
    	long begin = System.currentTimeMillis();
    	super.onPause();
    	log.D("DvbMainActivity onPause()------------------------------------------");
    	SystemProperties.set("tv.joysee.custom_video_axis", "0");
    	mController.exitDvb();
    	mHandler.removeMessages(NUM_CHANNEL_SWITCH);
    	mController.exitProgramGuide();
	    mController.stop(isTurnOffDeinterlace);
	    mController.dismissBlankView();
//    	mController.uninit();
        unregisterReceiver(mReceiver);
    	log.D("on pause   time = " +  (System.currentTimeMillis()-begin) + "----------------------------------");
    }
    @Override
    protected void onStop() {
    	log.D("DvbMainActivity onStop()------------------------------------------");
    	super.onStop();
    	mController.uninit();
    }
    @Override
    protected void onDestroy() {
        log.D("DvbMainActivity onDestroy()------------------------------------------");
        mController = null;
        super.onDestroy();
    }
    
    private void resolveIntent(Intent intent) {
    	final String ret = intent.getStringExtra(DvbIntent.INTENT_SUB_KEY);
    	log.D("resolve intent ret = " + ret);
		if(ret != null && ret.length() > 0){
			if(ret.equals(DvbIntent.INTENT_BROADCAST)){
				mController.switchPlayMode(ServiceType.BC);
			}else if(ret.equals(DvbIntent.INTENT_TELEVISION)){
				mController.switchPlayMode(ServiceType.TV);
			}else if(ret.equals(DvbIntent.WEEK_EPG)){
				boolean tag = mController.getTotalChannelSize()==0?false:true;
				if(tag){
					mController.showProgramGuide();
				}
			}else if(ret.equals(DvbIntent.CHANNEL_CATEGORY)){
				boolean tag = mController.getTotalChannelSize()==0?false:true;
				if(tag){
					mController.showMainMenu();
				}
            } else if (ret.equals(DvbIntent.LOOK_BACK)) {

            }
		} 
	}
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:// 菜单键
			mController.showMainMenu();
			break;
		case DvbKeyEvent.KEYCODE_TV_BC:// 58 电视广播键:
//			mController.switchPlayMode();
			break;
		case DvbKeyEvent.KEYCODE_LIST:// 频道列表
			mController.showChannelList();
			break;
		case DvbKeyEvent.KEYCODE_FAVORITE:// 喜爱键
			mController.showFavoriteChannel();
			break;
		case DvbKeyEvent.KEYCODE_BACK_SEE:// 回看键
			log.D("KEYCODE_BACK_SEE,start activity -------------------------");
			//进回看界面
			Intent lookBackIntent=new Intent();
			Bundle lookBackBundle = new Bundle();
			lookBackBundle.putInt(LookBackActivity.FROM_WHERE, LookBackActivity.DVB_MAIN_ACTIVITY);
			lookBackIntent.putExtras(lookBackBundle);
			lookBackIntent.setClass(this, LookBackActivity.class);
			startActivity(lookBackIntent);
			finish();
			return true;
		case DvbKeyEvent.KEYCODE_INFO:// 信息键
			mController.showChannelInfo();
			break;
		case DvbKeyEvent.KEYCODE_PROGRAM_GUIDE:// 节目指南键
			mController.showProgramGuide();
			break;
		case DvbKeyEvent.KEYCODE_SOUNDTRACK_SET:// 声道设置键
			mController.showSoundTrackSetting();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_PAGE_UP:
			isKeyRepeating = false;
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(isUserinputing){
				mHandler.removeMessages(NUM_CHANNEL_SWITCH);
				mHandler.sendEmptyMessage(NUM_CHANNEL_SWITCH_NOW);
			}else{
				if(isTimeShift()){
					isTurnOffDeinterlace =false;
					getTimeFromTs();
					Intent intent = new Intent(this,TimeShiftActivity.class);
					startActivity(intent);
					finish();
				}
			}
		    break;
        case KeyEvent.KEYCODE_PROG_BLUE:
            Intent intent1 = new Intent();
            intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent1.setClassName("com.lenovo.settings",
                    "com.lenovo.settings.LenovoSettingsActivity");
            Bundle bundle = new Bundle();
            bundle.putInt("curChoice", 12);
            bundle.putString("subChoice", "cctv_email");
            intent1.putExtras(bundle);
            startActivity(intent1);
            isTurnOffDeinterlace = true;
            finish();
            break;
        case KeyEvent.KEYCODE_BACK:
        case KeyEvent.KEYCODE_ESCAPE:
            mController.backSee();
            setTimeShiftImg();
            return true;
		}
    	return super.onKeyUp(keyCode, event);
    }
    
    private boolean isTimeShift(){
    	DvbService service=mController.getCurrentChannel();
		if(service!=null){
			int flag=service.getTimeShiftFlag();
			System.out.println("ServiceType="+service.getServiceType()+"  flag="+flag);
			if(flag==DefaultParameter.IS_TIMESHIFT){
				return true;
			}
		}
		return false;
    }
    
    private void getTimeFromTs(){
    	String tsTime = SettingManager.getSettingManager().nativeGetTimeFromTs();
    	String[] strings = tsTime.split(":");
		long utcTime = Long.valueOf(strings[0]) * 1000;
		System.out.println("dvbMain  tsTime--->"+tsTime+"   utcTime-->"+utcTime);
    }
    
    
    public int currentNumber;
    private VideoView mVideoView;
	private void executeNumKey(int inputNum) {
		isUserinputing = true;
		if (mDVBChannelNumView.getVisibility() != View.VISIBLE) {
			mUserInputNumber = 0;;
		}
		mUserInputNumber = mUserInputNumber * 10 + inputNum;
		mUserInputNumber = mUserInputNumber%1000;
		mController.inputNumber(mUserInputNumber);
		if(mUserInputNumber > 99){
		    mHandler.removeMessages(NUM_CHANNEL_SWITCH);
	        mHandler.sendEmptyMessage(NUM_CHANNEL_SWITCH);
	        return;
		}
		mHandler.removeMessages(NUM_CHANNEL_SWITCH);
		mHandler.sendEmptyMessageDelayed(NUM_CHANNEL_SWITCH,SWITCH_CHANNEL_TIMEOUT);
	}

}
