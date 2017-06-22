package com.joysee.adtv.controller;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.AudioIndex;
import com.joysee.adtv.common.DefaultParameter.DisplayMode;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;
import com.joysee.adtv.common.DefaultParameter.EmailStatus;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DefaultParameter.NotificationAction;
import com.joysee.adtv.common.DefaultParameter.NotificationAction.TunerStatus;
import com.joysee.adtv.common.DefaultParameter.OsdStatus;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DvbUtil;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.CaManager;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.DVBPlayManager.OnMonitorListener;
import com.joysee.adtv.logic.EPGManager;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.AudioTrackMode;
import com.joysee.adtv.logic.bean.CaFinger;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.OsdInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.logic.bean.Transponder;
/**
 * 用于播放控制
 * @author wangguohua
 */
	final class DvbController extends BaseController implements OnMonitorListener, OnAudioFocusChangeListener {
    
	private static final String TAG = "DvbController";
	private static final String MTAG = "DvbControllerEpg";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
	
	private Activity mContext;
	private DVBPlayManager mDvbPlayManager;
	private ChannelManager mChannelManager;
	private EPGManager mEPGManager;
	private CaManager mCaManager;
	private AudioManager mAudioManager;
	private int mSystemVolumeIndex;
	private SettingManager mSettingManager;
	private static int mCurrentChannelIndex = 0;
	private static int mCurrentPlayType = ServiceType.TV; 
    private int mLastChannelIndex;
	private static HandlerThread mSwitchChannelThread;
	private static SChannelHandler mSwitchChannelHandler;
	private static boolean mPause;
	private ViewController mViewController;
	private double mTimeZone;
	private boolean mIsBackSeeing = false;
	private boolean mIsSwitchPlayMode = false;
	private static final int CHANGE_MODE_TIMEOUT = 2500;//防止连续切换TV/BC
	
	private static final int SWITCH_CHANNEL_SPECIAL = 0;
	private static final int SWITCH_CHANNEL_NOW = 1;
	private static final int CHANNEL_NUM_OR_INFO = 2;
	private static final int DISPATCH_MESSAGE = 3;
	private static final long MSWITCHCHANNELDELAY = 300L;
	
	private static final int delay_time = 300;
	private boolean mDvbInitRet;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			//因为native都放在子线程里去做了，获取数据后，在此主线程切换右上角台标
		    switch (msg.what) {
                case CHANNEL_NUM_OR_INFO:
                    int count  = mViews.size();
                    for(int i=0;i<count;i++){
                        mViews.get(i).processMessage(mViewController,DvbMessage.obtain(ViewMessage.SHOW_EPG_INFO_ONEMORE , msg.arg1));//右上角频道号
                    }
                    break;
                case DISPATCH_MESSAGE:
                    if(msg.arg1 == ViewMessage.START_PLAY_TV)
                        DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
                    else if(msg.arg1 == ViewMessage.START_PLAY_BC)
                        DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));
                    break;
                default:
                    break;
            }
		};
	};
	
	private boolean isTunerEnable = true;
//	private boolean isCAEnable = true;
	
	private int mLastCAParam = -1;
	private int mLastTunerParam = -1;
	
//	private static long switchChannelbegin = -1;
//	private static boolean isTurnOnDeinterlace = true;
//	private static boolean isTurnOffDeinterlace = true;
	
	//防止回看和数字键延迟连续切台
	private static final int BACKSEE_TIMEOUT = 3000;
	/* 预约广播接收 */
	private ProgramReservesBroadcastReceiver programReservesBroadcastReceiver;
	
	
    public DvbController(Activity context) {
        this.mContext = context;
        mDvbPlayManager = DVBPlayManager.getInstance(context);
        mChannelManager = ChannelManager.getInstance();
        mEPGManager = EPGManager.getInstance();
        mCaManager = CaManager.getCaManager();
        mChannelVolumeConfig = getChannelVolumeConfig();
    }

    
    public long TimeOffset;
    public void init() {
    	log.D("Controller init begin -----");
    	mViewController = new ViewController(this);
        mSettingManager = SettingManager.getSettingManager();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mDvbInitRet = initDVBPlayer();
        mEPGManager = EPGManager.getInstance();
        mEPGManager.nativeSetEPGSourceMode(true);
		mSwitchChannelThread = new HandlerThread("switch-channel");
		mSwitchChannelThread.start();
		mSwitchChannelHandler = new SChannelHandler(mSwitchChannelThread.getLooper());
		
		log.D("Controller init end -----");
    }

	private boolean initDVBPlayer() {
		int ret = mDvbPlayManager.init();
		log.D("mDvbPlayManager.init()   =    " + ret);
		if(ret < 0){
			log.D("mDvbPlayManager.init()<0  *******************************************");
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.DVB_INIT_FAILED));
			return false;
		}
		mDvbPlayManager.initChannels();
		mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
		TimeOffset=(long)((8-mTimeZone)*3600*1000);
		return true;
	}

	private boolean isPlayBC;
	public void playFromEpg(final int l,final int t,final int r,final int b,int screenMode){
		log.D("l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
		mDvbPlayManager.setOnMonitorListener(this);
    	mCurrentPlayType = ServiceType.TV;
    	mPause = false;
    	mLastChannelIndex = -1;
    	mSettingManager.nativeSetVideoWindow(l,t,r,b);
    	if(screenMode == DefaultParameter.DisplayMode.DISPLAYMODE_16TO9){
    	    log.D("DISPLAYMODE_16TO9");
    	    resetScreenMode();
    	}else{
    		String cur_output = DvbUtil.getCurrentOutputResolution();
    		if ("576i".equals(cur_output) || "576p".equals(cur_output)) {
    			mDvbPlayManager.setDisplayMode(DefaultParameter.DisplayMode.DISPLAYMODE_4TO3);
    		} else {
    	    mSettingManager.nativeSetVideoAspectRatio(mDvbPlayManager.getDisplayMode());
    		}
    	    Log.d("sognwenxuan","mDvbPlayManager.getDisplayMode() = " + mDvbPlayManager.getDisplayMode());
    	}
    	DvbService service = mDvbPlayManager.playLast(mCurrentPlayType);
		if ((service.getServiceType()&0x0f) == ServiceType.TV){
			mCurrentChannelIndex = DVBPlayManager.mCurrentTVChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
		}else{
//			mCurrentChannelIndex = DVBPlayManager.mCurrentBCChannelIndex;
		    dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));
		}
		log.D("isTunerEnable = " + isTunerEnable);
		isTunerEnable = mDvbPlayManager.getTunerSignalStatus() == 0 ? true : false;//判断信号
		log.D( "after refreshTunerStatus isTunerEnable = " + isTunerEnable);
		Object[] objs = {getmCurrentPlayType(),isDVBChannelEnable(getmCurrentPlayType()), isTunerEnable, mLastCAParam};
		if(mDvbInitRet)
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.EPG_RECEIVE_NOTIFY, objs));
		mHandler.postDelayed(new Runnable() {
            public void run() {
                showOsdFromXml();
                showEmailIconFromXml();
            }
        }, 1000);
//		byPassDeinterlace(true);
	}
	
//	private void updateByPassDeinterlace(DvbService service) {
//        final boolean isHD = isHDChannel(service);
//        byPassDeinterlace(isHD);
//    }
    
    public void byPassDeinterlace(boolean pass) {
        String val = pass ? "1" : "0";
        try {
            File f = new File("/sys/module/di/parameters/bypass_all");
            FileWriter fw = new FileWriter(f);
            BufferedWriter buf = new BufferedWriter(fw);
            buf.write(val);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private static final int FLAG_SERVICE_HD = 0x02000000;
//    private boolean isHDChannel(DvbService service) {
//        boolean result = false;
//        if (service != null) {
//            final int videoStreamType = service.getVideoType();
//            final int audioStreamType = service.getAudioType0();
//            switch (audioStreamType) {
//            case 15:
//            case 0x7B:
//            case 0x8A:
//            case 0x81:
//            case 0x6a:
//            case 0x7A:
//            case 6:
//                result = true;
//                break;
//            }
//            switch (videoStreamType) {
//            case 0x1b:
//                result = true;
//                break;
//            }
//            int serviceType = service.getServiceType();
//            if (!result) {
//                result = (serviceType & FLAG_SERVICE_HD) == FLAG_SERVICE_HD ? true : false;
//            }
//            if(service.getChannelName().contains(mContext.getResources().getString(R.string.hd))||
//                    service.getChannelName().contains(mContext.getResources().getString(R.string.HD))
//                    ){
//                result = true;
//            }
//        }
//        return result;
//    }

	private final String ACTION_REALVIDEO_ON = "android.intent.action.REALVIDEO_ON";
	private static final String SYSPROP_BOOTANIM_KEY = "init.svc.bootanim";
    private static final String SYSPROP_BOOTANIM_STOPED = "stopped";
//	private boolean mIsNeedStop;
    public void play(Intent intent) {
        String ret = intent.getStringExtra(DefaultParameter.DvbIntent.INTENT_KEY);
        final String subRet = intent.getStringExtra(DefaultParameter.DvbIntent.INTENT_SUB_KEY);
    	log.D("Controller play begin -------");
    	mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    	if (mChannelVolumeConfig == 1) {
//    		mAudioManager.hideVolumePanel();//厂商实现的隐藏音量Bar
    		mSystemVolumeIndex = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    	}

    	mDvbPlayManager.setOnMonitorListener(this);
    	mCurrentPlayType = ServiceType.TV;
//    	mCurrentPlayType = ServiceType.ALL;
    	
    	mPause = false;
		mLastChannelIndex = -1;
		DvbUtil.resetWindowSize();
		resetDvbDisplayMode();
		if(ret == null){
		    log.D("ret == null");
//		    mIsNeedStop = true;//解决其他应用按电视键进入电视，返回时电视没停的问题。
		}else{
		    log.D("intent ret = " + ret);
//		    mIsNeedStop = true;
		}
		int delayed = 0;
		boolean stoped = false;
		while (!stoped) {
			log.D("SystemProperties.get(SYSPROP_BOOTANIM_KEY)" + SystemProperties.get(SYSPROP_BOOTANIM_KEY));
			stoped = SYSPROP_BOOTANIM_STOPED.equals(SystemProperties.get(SYSPROP_BOOTANIM_KEY)) ? true : false;
			if (!stoped) {
				delayed = 500;
				log.D("Runnable mDVBPlayerPlay bootAnimation is not stoped, wait 100ms...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mDvbPlayManager.nativeGetPlayState() != 4) {
					log.D("intent ret == null || !ret.equals(DvbIntent.FROM_LAUNCHER)");
					DvbUtil.switchDeinterlace(true);
					DvbUtil.setRealVideoOnOff(true);
					// mHandler.sendEmptyMessage(555);
					if (subRet == null || !subRet.equals(DvbIntent.WEEK_EPG)) {
						log.D("enter dvb play ");
						final DvbService retService = mDvbPlayManager
								.playLast(getmCurrentPlayType());
						mCurrentChannelIndex = DVBPlayManager.mCurrentTVChannelIndex;
						log.D("play(),mCurrentChannelIndex = "
								+ mCurrentChannelIndex);
						if (retService != null) {
							if ((retService.getServiceType() & 0x01) == ServiceType.TV) {
								log.D( "ServiceType.TV   ///");
								dispatchMessage(mViewController, DvbMessage
										.obtain(ViewMessage.START_PLAY_TV));
							} else {
								log.D( "ServiceType.BC   ///");
								dispatchMessage(mViewController, DvbMessage
										.obtain(ViewMessage.START_PLAY_BC));
								// mCurrentChannelIndex =
								// DVBPlayManager.mCurrentBCChannelIndex;
							}
							showChannelInfo();
							DvbUtil.dismissTransitionDialog();
						}
					} else {
						log.D("enter epg play");
					}
				} else {
					log.D("play(),from launcher.............................................");
					if (getmCurrentPlayType() == ServiceType.TV) {
						log.D( "ServiceType.TV   ///");
						int chlNum = mChannelManager.nativeGetLastTVChlNum();
						mCurrentChannelIndex = mChannelManager
								.nativeGetService(chlNum, new DvbService(),
										ServiceType.TV);
						DVBPlayManager.mCurrentTVChannelIndex = mCurrentChannelIndex;
						// mCurrentChannelIndex =
						// DVBPlayManager.mCurrentTVChannelIndex;
						dispatchMessage(mViewController,
								DvbMessage.obtain(ViewMessage.START_PLAY_TV));
					} else {
						// log.D("ServiceType.BC   ///");
						// mCurrentChannelIndex =
						// DVBPlayManager.mCurrentBCChannelIndex;
					}
					if (null == subRet || !subRet.equals(DvbIntent.WEEK_EPG))
						showChannelInfo();
				}
				log.D("isTunerEnable = " + isTunerEnable);
				isTunerEnable = mDvbPlayManager.getTunerSignalStatus() == 0 ? true
						: false;// 判断信号
				log.D("after refreshTunerStatus isTunerEnable = "
						+ isTunerEnable);
				int caStatus = mCaManager
						.nativeQueryMsgType(NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG);
				Object[] objs = { getmCurrentPlayType(),
						isDVBChannelEnable(getmCurrentPlayType()),
						isTunerEnable, caStatus };
				if (mDvbInitRet)
					dispatchMessage(mViewController, DvbMessage.obtain(
							ViewMessage.RECEIVED_ERROR_NOTIFY, objs));

				regitsterReserveProgram();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						showOsdFromXml();
						showEmailIconFromXml();
					}
				}, 1000);
//				byPassDeinterlace(false);
				log.D("Controller play end -------");
			}
		}, delayed);
//		    try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
    }

    public void resetDvbDisplayMode() {
    	String cur_output = DvbUtil.getCurrentOutputResolution();
		if ("576i".equals(cur_output) || "576p".equals(cur_output)) {
			mDvbPlayManager.setDisplayMode(DefaultParameter.DisplayMode.DISPLAYMODE_4TO3);
		} else {
			mDvbPlayManager.setDisplayMode(mDvbPlayManager.getDisplayMode());
		}
    }
    
    public void stopFromEpg(){
    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.STOP_PLAY));
    	mSwitchChannelHandler.removeMessages(SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL);
    	while(mSwitchChannelRet == 1000){
    		log.D("mSwitchChannelRet == -1000------------------------------------------------------");
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	mDvbPlayManager.stop();
    }
    private static final String BACK_TO_LAUNCHER = "/cache/launcher_needplay";
    private static final String DVB_PLAYED = "/cache/dvb_played";
    public void stop(boolean isTurnOffDeinterlace) {
    	log.D("Controller stop begin -----------------------------------");
    	unRegisterReservePrograme();
    	mPause=true;
    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.STOP_PLAY));
    	mSwitchChannelHandler.removeMessages(SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL);
    	while(mSwitchChannelRet == -1000){
    		log.D("mSwitchChannelRet == -1000------------------------------------------------------");
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	mDvbPlayManager.stop();
    	if(isTurnOffDeinterlace){
    	    log.D("turn off deinterlace -------------------------------");
    	    resetScreenMode();
//    	    DvbUtil.writeFile("/sys/class/video/disable_video","1");
    	    DvbUtil.writeFile(DVB_PLAYED,"1");
    	    DvbUtil.writeFile(BACK_TO_LAUNCHER, "0");
    	    DvbUtil.switchDeinterlace(false);
    	    DvbUtil.setRealVideoOnOff(false);
    	}
    	mAudioManager.abandonAudioFocus(this);
    	if (mChannelVolumeConfig == 1) {
//    		mAudioManager.hideVolumePanel();//TODO 厂商实现隐藏音量Bar
	    	if (mSystemVolumeIndex != -1) {
	    		mDvbPlayManager.setVolume(mSystemVolumeIndex, 0);
	    	}
    	}
		log.D("Controller stop end --------------------------------------");
    }
    
    public void resetScreenMode(){
        mSettingManager.nativeSetVideoAspectRatio(DisplayMode.DISPLAYMODE_16TO9);
    }

    public void uninit() {
    	log.D("Controller uninit begin----");
    	if (mSwitchChannelThread != null && mSwitchChannelThread.isAlive()) {
    		log.D("sChannelThread.quit();");
    		mSwitchChannelThread.quit();
    	}else {
    		log.D("sChannelThread != null  = " + (mSwitchChannelThread != null));
    		log.D("sChannelThread.isAlive()  = " + (mSwitchChannelThread.isAlive()));
    	}
//		mDvbPlayManager.uninit();
		log.D("Controller uninit end----");
    }
    
    private void regitsterReserveProgram() {
    	IntentFilter filter = new IntentFilter();
		filter.addAction("program alarm");
		if (programReservesBroadcastReceiver == null) {
			programReservesBroadcastReceiver = new ProgramReservesBroadcastReceiver();
		}
		mContext.registerReceiver(programReservesBroadcastReceiver, filter);
		
	    mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
		mHandler.postDelayed(new Runnable() {
			public void run() {
				Cursor programReserveBindCursor = mContext.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null,
						null, Channel.TableReservesColumns.STARTTIME);
			/*	if (programReserveBindCursor == null || programReserveBindCursor.isClosed()
						|| programReserveBindCursor.getCount() <= 0) {
					return;
				}*/
				if(programReserveBindCursor==null||programReserveBindCursor.getCount() <= 0){
					if(null!=programReserveBindCursor&&!programReserveBindCursor.isClosed()){
						programReserveBindCursor.close();
					}
					return;
				}
				while (programReserveBindCursor.moveToNext()) {
					int id = programReserveBindCursor.getInt(programReserveBindCursor
							.getColumnIndex(Channel.TableReservesColumns.ID));
					long startTime = (long) programReserveBindCursor.getInt(programReserveBindCursor
							.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
					String realTimeStr = mSettingManager.nativeGetTimeFromTs();
					log.D("get UTC time is " + realTimeStr);
					String[] splitTime = realTimeStr.split(":");
					long realTime = Long.valueOf(splitTime[0])*1000 + (long)((8-mTimeZone)*3600*1000);
					long timeCompensate = System.currentTimeMillis() - realTime;
					log.D("timecompensate=" + timeCompensate);
					if (Long.valueOf(splitTime[0]) == 0) {
						log.D("time commpenstae is 0");
						timeCompensate = 0;
					}
					if ((startTime * 1000 + (long)((8-mTimeZone)*3600*1000)) < realTime) {
						continue;
					} else {
						addReServeProgramToAlam(id, startTime*1000 + (long)((8-mTimeZone)*3600*1000) + timeCompensate - 60 * 1000);
						log.D("reserve success! reserve date="+DateFormatUtil.getDateFromMillis(
                                startTime*1000+timeCompensate)+DateFormatUtil.getTimeFromMillis(startTime*1000+timeCompensate));
					}
				}
				programReserveBindCursor.close();
			}
		}, 0);
	}
    
	/** 添加预约闹钟 */
	private void addReServeProgramToAlam(int id, long startTime) {
		log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
		Intent intent = new Intent("program alarm");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
	}
	
	/** 删除预约闹钟 */
	private void removeReserveProgramFromAlarm(int id) {
		log.D("removeReserveProgramFromAlarm(), enter! reserveId=" + id);
		Intent intent = new Intent("program alarm");

		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}
	
	private void unRegisterReservePrograme() {
		try {
            log.D("unRegisterReservePrograme**************************************************************************");
            mContext.unregisterReceiver(programReservesBroadcastReceiver);
            Cursor programReserveUnbindCursor = mContext.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, null, null,
            		Channel.TableReservesColumns.STARTTIME);
            log.D("programReserveUnbindCursor  =  " + programReserveUnbindCursor);
            while (programReserveUnbindCursor != null && programReserveUnbindCursor.moveToNext()) {
            	int id = programReserveUnbindCursor.getInt(programReserveUnbindCursor
            			.getColumnIndex(Channel.TableReservesColumns.ID));
            	removeReserveProgramFromAlarm(id);
            }
            if (programReserveUnbindCursor != null)
            	programReserveUnbindCursor.close();
        } catch (Exception e) {
             log.D("unRegisterReservePrograme throws an exception" + e.toString());
        }
	}
	
	/** 预约BroadcastReceiver */
	class ProgramReservesBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			log.D( "---接受到预约提示广播--- intent action = " + intent.getAction());
			if(!mPause){
				if(intent.getAction().equals("program alarm"))
					dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE_ALERT));
			}
		}
	}

	private int mSwitchChannelRet = 0;
   /**
    *  切台Handler
    */
	private class SChannelHandler extends Handler {
		static final int SWITCH_TO_SPECIAL_CHANNEL = 333;
		public SChannelHandler(Looper looper) {
			super(looper);
		}
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case SWITCH_CHANNEL_SPECIAL: 
//				Log.d(MTAG, "--- SWITCH_TO_SPECIAL_CHANNEL ---");
//				if(!mPause){
//					DvbService destService = mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
//					mDvbPlayManager.switchToSpecialChannel(getmCurrentPlayType(),destService,mCurrentChannelIndex);
//				}
//				break;
//			case SWITCH_CHANNEL_NOW:
//				Log.d(MTAG, "--- SWITCH_CHANNEL_NOW ---");
//				DvbService destService = mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
//				mDvbPlayManager.switchToSpecialChannel(getmCurrentPlayType(),destService,mCurrentChannelIndex);
//				break;
			case SWITCH_TO_SPECIAL_CHANNEL:
				log.D("SWITCH_TO_SPECIAL_CHANNEL");
				if (!mPause) {
					log.D("!mPause");
					mSwitchChannelRet = -1000;
					DvbService service = (DvbService)msg.obj;
					mSwitchChannelRet = mDvbPlayManager.switchToSpecialChannel(mCurrentPlayType,service,msg.arg1);
					log.D("mSwitchChannelRet = " + mSwitchChannelRet);
				}
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	/** 设置喜爱频道后刷新频道信息 */
	public void refreshChannelInfo() {
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_FAVORITE_CHANNEL_SET));
	}
	
	public boolean isEpgReceive;
	/** 底层回调信息 */
	public void onMonitor(int monitorType, Object message) {
		log.D("DvbController onMonitor");
		log.D("onMonitor monitorType = " + monitorType);
		log.D("onMonitor message = " + message);
        if (mPause
                // add by wuhao for notify osd and email
                && (monitorType != DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_OSD &&
                monitorType != DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_MAIL_NOTIFY)) {
            return;
        }
		switch (monitorType) {
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TUNER_SIGNAL:
			int code = (Integer) message;
			mLastTunerParam = code;
			isTunerEnable = code == TunerStatus.ACTION_TUNER_UNLOCKED ? true : false;
			Object[] objs = {getmCurrentPlayType(),isDVBChannelEnable(getmCurrentPlayType()), isTunerEnable, mLastCAParam};
		    if(isEpgReceive){
		        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.EPG_RECEIVE_NOTIFY, objs));
		    }else{
		        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
		    }
			break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_BUYMSG:
			int mage = (Integer) message;
			mLastCAParam = mage;
//			isCAEnable = mage == CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE ? true : false;
			Object[] objects = {getmCurrentPlayType(),isDVBChannelEnable(getmCurrentPlayType()), isTunerEnable, mage};
		    if(isEpgReceive){
		        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.EPG_RECEIVE_NOTIFY, objects));
		    }else{
		        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objects));
		    }
			break;
		// 底层回调OSD信息
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_OSD:
			OsdInfo osdInfo = (OsdInfo) message;
			log.D(" --------------- getShowPosition = " + osdInfo.getShowPosition());
                try {
                    osdInfo.saveOsdStateToSharePre(mContext, osdInfo.getShowOrHide(),
                            osdInfo.getOsdMsg(),
                            osdInfo.getShowPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
			if (null != osdInfo) {
				int state = osdInfo.getShowOrHide();
				log.D(" ----- struct.getShowOrHide() = " + state);
				switch (state) {
				// 隐藏OSD
				case OsdStatus.OSD_HIDE:
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_HIDE, osdInfo.getShowPosition()));
					break;
				// 显示OSD
				case OsdStatus.OSD_SHOW:
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_SHOW, osdInfo.getShowPosition(),0,osdInfo.getOsdMsg()));
					break;
				}
			}
			break;
		// 底层回调邮件消息
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_MAIL_NOTIFY:
			int msgobject = (Integer) message;
			int msg = msgobject >> 24;
			int id = msgobject & 0x00ffffff;
                try {
                    mContext
                            .getSharedPreferences(
                                    DefaultParameter.PREFERENCE_NAME,
                                    Activity.MODE_PRIVATE)
                            .edit()
                            .putInt(DefaultParameter.TpKey.KEY_EMAIL_STATE, msg)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
			log.D(" ---------------  email notify msg =  " + msg + " id = " + id);
			switch (msg) {
			// 隐藏邮件通知
			case EmailStatus.EMAIL_HIDE:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_HIDE));
				break;
			// 显示邮件通知
			case EmailStatus.EMAIL_SHOW:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_SHOW));
				break;
			// 邮件空间已满
			case EmailStatus.EMAIL_NOSPACE:
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_BLINK));
				break;
			}
			break;
		// nit bat改变 搜台
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_UPDATE_PROGRAM:
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_UPDATE_PROGRAM_NB_CHANGE));
			break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_UPDATE_SERVICE:
			DvbService service = (DvbService) message;
			mDvbPlayManager.nativeChangeService(service);
			mDvbPlayManager.nativeSyncServiceToProgram(mCurrentChannelIndex, service);
			break;
		//指纹显示
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_SHOW_FINGERPRINT:
		    CaFinger finger = (CaFinger) message;
            if (finger != null){
                dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_FINGER_INFO_SHOW, finger.getCard_id(), finger.getEcmp_id(), null));
            }
		    break;
		
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_MINEPG:
			MiniEpgNotify pf = (MiniEpgNotify) message;
			DvbService currentService = mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
			if(currentService!= null){
				final int sid = currentService.getServiceId();
				final int pfSid = pf.getServiceId();
				log.D("showPf()   sid = " + sid + "  pfSid = " + pfSid);
				if (sid == pfSid) {
					dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_MINIEPG,sid,0,pf));
				}
			}
		    break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_EPGCOMPLETE:
			dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.RECEIVE_EPG_CALLBACK));
			break;
		case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_TS_EPG_SEARCH_COMPLETED:
			log.D("NOTIFICATION_TVNOTIFY_TS_EPG_SEARCH_COMPLETED -----------");
			dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.RECEIVED_SEARCH_EPG_COMPLETED,message));
			break;
        case DefaultParameter.NotificationAction.NOTIFICATION_TVNOTIFY_SHOW_PROGRESSSTRIP:
            int updateMsg = (Integer) message;
            int type = updateMsg >> 16;
            int progress = updateMsg & 0xffff;
            dispatchMessage(mViewController,
                    DvbMessage.obtain(ViewMessage.MSG_SHOW_CAUPDATE, type, progress, null));
		}
	}
	
	@Override
	public void onAudioFocusChange(int focusChange) {
	}

	public void switchPlayMode(int type) {
		switchPlayMode(type, -1);
	}
	
	private void switchPlayMode(int type, int channelNum) {
		if (type == getmCurrentPlayType()){
			mIsSwitchPlayMode = false;
			return;
		}
		mDvbPlayManager.stop();
		mCurrentPlayType = type;
		mLastChannelIndex = -1;
		DvbService retService = null;
		if (type == ServiceType.TV) {
			log.D("call mDvbPlayManager.playLast()");
			retService = mDvbPlayManager.playLast(mCurrentPlayType);
			mCurrentChannelIndex = DVBPlayManager.mCurrentTVChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
		} else if (type == ServiceType.BC) {
			retService = mDvbPlayManager.playLast(mCurrentPlayType);
			mCurrentChannelIndex = DVBPlayManager.mCurrentBCChannelIndex;
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));//显示BC背景
		} else {
			throw new RuntimeException("INVALID PLAYMODE!");
		}
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_PLAY_MODE));
		
		Object[] objs = {getmCurrentPlayType(),isDVBChannelEnable(getmCurrentPlayType()), isTunerEnable, mLastCAParam};
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
		if (retService != null) {
			showChannelInfoDynamic();
		}
	}
	
	/** 当回到看电视界面时从XML保存的OSD状态值中恢复OSD状态 */
    private void showOsdFromXml() {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(DefaultParameter.PREFERENCE_NAME,
                        Activity.MODE_PRIVATE);
        if (null != sharedPreferences) {
            int status = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_OSD_STATE,
                    OsdStatus.STATUS_INVALID);
            String msg = sharedPreferences.getString(
                    DefaultParameter.TpKey.KEY_OSD_MSG, "");
            int position = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_OSD_POSITION, 0);
            log.D(" showOsdFromXml status = " + status + " msg = " + msg
                    + " position = " + position);
            switch (status) {
            case OsdStatus.STATUS_INVALID:
                break;
            case OsdStatus.OSD_HIDE:
                break;
            case OsdStatus.OSD_SHOW:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_OSD_INFO_SHOW,position,0,msg));
                break;
            }
        }else {
            log.E("sharedPreferences is null , showOsdFromXml Error!");
        }
    }
    
    /** 当回到看电视界面时从XML保存的邮件状态值中恢复邮件状态 */
    private void showEmailIconFromXml() {
        SharedPreferences sharedPreferences = mContext
                .getSharedPreferences(DefaultParameter.PREFERENCE_NAME,
                        Activity.MODE_PRIVATE);
        if (null != sharedPreferences) {
            int status = sharedPreferences.getInt(
                    DefaultParameter.TpKey.KEY_EMAIL_STATE,
                    EmailStatus.STATUS_INVALID);
            log.D(" showEmailIconFromXml status = " + status);
            switch (status) {
            case EmailStatus.STATUS_INVALID:
                break;
            case EmailStatus.EMAIL_HIDE:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_HIDE));
                break;
            case EmailStatus.EMAIL_SHOW:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_SHOW));
                break;
            case EmailStatus.EMAIL_NOSPACE:
            	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_EMAIL_BLINK));
                break;
            }
        } else {
            log.E(" sharedPreferences is null , showEmailIconFromXml Error!");
        }
    }
	
	/**
	 * 发送指定键值的模拟按键
	 * @param key 	指定的键值
	 */
	public void doInjectKeyEvent(final int key) {
		log.D("doInjectKeyEvent key = " + key);
		mSwitchChannelHandler.post(new Runnable() {
			public void run() {
				Instrumentation inst = new Instrumentation();
				inst.sendKeyDownUpSync(key);
			}
		});
	}
	
	/** 判断是否有频道 */
	public boolean isChannelEnable(){
		return isDVBChannelEnable(getmCurrentPlayType());
	}
	
	public boolean isTVService(){
		return getmCurrentPlayType() == ServiceType.TV;
	}
	
	public boolean isBCService(){
		return getmCurrentPlayType() == ServiceType.BC;
	}
	public int getCurrentIndexInChannelWindow(){
		if(getmCurrentPlayType() == ServiceType.TV){
			return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
		}else if(getmCurrentPlayType() == ServiceType.BC){
			return DVBPlayManager.mBcIndexList.indexOf(mCurrentChannelIndex);
		}else{
			return 0;
		}
	}
	public int getChannelNum(){
		return mDvbPlayManager.getChannelNum(getmCurrentPlayType(), mCurrentChannelIndex);
	}
	/**
	 * 加入喜爱频道
	 * @param isAdd true添加 false删除
	 */
	public void setChannelFavorite(boolean isAdd){
		if(isAdd){
			mDvbPlayManager.setChannelFavorite(getmCurrentPlayType(),mCurrentChannelIndex, FavoriteFlag.FAVORITE_YES);
		}else{
			mDvbPlayManager.setChannelFavorite(getmCurrentPlayType(),mCurrentChannelIndex, FavoriteFlag.FAVORITE_NO);
		}
	}
	
	public DvbService getCurrentChannel(){
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType, mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
	}
	public int getDisplayMode(){
		return mDvbPlayManager.getDisplayMode();
	}

	public void setDisplayMode(int mode) {
		if (mode == 0) {
			mDvbPlayManager.setDisplayMode(DisplayMode.DISPLAYMODE_NORMAL);
		} else if (mode == 1) {
			mDvbPlayManager.setDisplayMode(DisplayMode.DISPLAYMODE_4TO3);
		}else if(mode == 2){
		    mDvbPlayManager.setDisplayMode(DisplayMode.DISPLAYMODE_16TO9);
		}
	}
	
	public int getSoundTrack(){
		DvbService service = mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), mCurrentChannelIndex,DVBPlayManager.NATIVE_INDEX);
		if(service!=null){
			log.D("getSoundTrack(): " + service.getSoundTrack() + "   channel number = " + service.getLogicChNumber());
			return service.getSoundTrack();
		}
		return 0;
	}
	public void setSoundTrack(int position){
		int resId = -1;
		int soundTrack = -1;
		if (position == 0) {
			soundTrack = AudioTrackMode.AUDIO_MODE_STEREO.ordinal();
			resId = R.string.dvb_sound_track_all;
		} else if (position == 1) {
			soundTrack = AudioTrackMode.AUDIO_MODE_LEFT.ordinal();
			resId = R.string.dvb_sound_track_left;
		} else if (position == 2) {
			soundTrack = AudioTrackMode.AUDIO_MODE_RIGHT.ordinal();
			resId = R.string.dvb_sound_track_right;
		}
		mDvbPlayManager.setSoundTrack(getmCurrentPlayType(), mCurrentChannelIndex, soundTrack);
		String content = mContext.getString(resId);
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET, 0, 0, content));
	}
	
	public void switchPlayMode() {
		if(mIsSwitchPlayMode){
			Log.d(MTAG, " --- switchPlayMode --- return ");
			return;
		}
		if (getmCurrentPlayType() == ServiceType.TV){
			switchPlayMode(ServiceType.BC);
		}
		else{
			switchPlayMode(ServiceType.TV);
		}
		mIsSwitchPlayMode = true;
		mHandler.postDelayed(new Runnable() {
			public void run() {
				Log.d(MTAG, "--- mIsBackSeeing = false; ---");
				mIsSwitchPlayMode = false;
			}
		},CHANGE_MODE_TIMEOUT);
	}

	public void backSee() {
		if (mLastChannelIndex != -1) {
			mIsBackSeeing = true;
			int tempChannelIndex = mCurrentChannelIndex;
			mCurrentChannelIndex = mLastChannelIndex;
			switchChannelNew(mLastChannelIndex);
			mLastChannelIndex = tempChannelIndex;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mIsBackSeeing = false;
				}
			},BACKSEE_TIMEOUT);
		}
	}
	
	/**
	 * 根据频道类型和角标切台，主线程调用
	 * @param type
	 * @param index 上层维护的角标
	 * @param delay
	 */
	public void switchChannelNew(int index){
		switchChannelNew(index, 0);
	}

	
	//  信息键、初始化显示
	public void showChannelInfo() {
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_INFO_KEY));
	}
	//  TV/BV切换
	public void showChannelInfoDynamic(){
		DvbService service = getCurrentChannel();
		Log.d("msg", "TV/BV切换 = "+service.getLogicChNumber());
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL , service.getLogicChNumber()));
	}
	//用户通过遥控器或键盘输入数字，但还没有按确定切台，或者没到3秒自动切台时，需要显示频道号时调用。
	public void inputNumber(int number){
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_NUMBER_KEY, number));
	}
	//根据输入的数字切到对应的台
    public void inputNumberChange(int number){
    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SHOW_EPG_INFO_ONEMORE, number));
    }
	//响应频道菜单、回看按键　的切台
	private void showBackSeeInfo(){
		DvbService service = getCurrentChannel();
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_EPG_INFO_ONEMORE , service.getLogicChNumber()));
	}
    
	
	
	public int getCurrentAudioIndexSum(){
		return mDvbPlayManager.getCurrentAudioIndexSum();
	}

	public int getAudioIndex() {
		return  mDvbPlayManager.getAudioIndex(getmCurrentPlayType(), mCurrentChannelIndex);
	}

	public void setAudioIndex(int position) {
		int audioIndex = -1;
		int resId = -1;
		if (position == 0) {
			audioIndex = AudioIndex.AUDIOINDEX_0;
			resId = R.string.dvb_audio_index_0;
		} else if (position == 1) {
			audioIndex = AudioIndex.AUDIOINDEX_1;
			resId = R.string.dvb_audio_index_1;
		} else if (position == 2) {
			audioIndex = AudioIndex.AUDIOINDEX_2;
			resId = R.string.dvb_audio_index_2;
		}
		mDvbPlayManager.setAudioIndex(getmCurrentPlayType(), mCurrentChannelIndex, audioIndex);
		String content = mContext.getString(resId);
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.FINISHED_SOUNDTRACK_AUDIOINDEX_SET, 0, 0, content));
	}

	public void changeVolume(int value) {
		boolean mute = false;
//		mute = mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);//TODO 厂商自己实现的静音判断
		
		if (mute){
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}
		int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + value;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
		if (mChannelVolumeConfig == 1) {
			mDvbPlayManager.saveCurrentChannelVolume(mContext, getCurrentChannel(), volume);
		}
	}
	
	
	public static final String LOCALIZATION_DVB_FILE = "/data/joysee-config/DVB/tvcore.conf";
	public static final String LOCALIZATION_DVB_VOLUME_KEY = "VolumeReserve";
    public int mChannelVolumeConfig = 0;
    
    private int getChannelVolumeConfig() {
    	try {
			String tVolumeConfig = readValue(LOCALIZATION_DVB_FILE, LOCALIZATION_DVB_VOLUME_KEY);
			mChannelVolumeConfig = Integer.parseInt(tVolumeConfig);
		} catch (NumberFormatException e) {
			Log.e("TVApplication", "getChannelVolumeConfig catch Exception...", e);
			mChannelVolumeConfig = 0;
		}
    	return mChannelVolumeConfig;
    }
    
	
	public static String readValue(String filePath, String key) {
		String ret = "";
		File file = new File(filePath);
		if (file.exists()) {
			Properties props = new Properties();
			try {
				InputStream in = new BufferedInputStream(new FileInputStream(file));
				props.load(in);
				ret = props.getProperty(key);
			} catch (Exception e) {
				Log.e("TVApplication", "readValue catch Exception...", e);
			}
		} else {
			log.D("readValue file " + filePath + " not found...");	
		}
		log.D("readValue key = " + key + " value = " + ret);
		return ret;
	}
    
	protected void dispatchMessage(Object sender,DvbMessage msg) {
    	  int count  = mViews.size();
          for(int i=0;i<count;i++){
        	  mViews.get(i).processMessage(sender,msg);
          }
	}
	/**
	 * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
	 * @param channelNum 频道号
	 * @param now true 立刻切台
	 */
	public void switchChannelFromNum(int channelNum,boolean now){
		if(!now && mIsBackSeeing){
			return;
		}
		switchChannelFromNum(channelNum);
	}
    /**
     * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
     * @param channelNum 频道号
     */
    public void switchChannelFromNum(int channelNum){
    	log.D("switchChannelFromNum");
    	int tempChannelIndex = mCurrentChannelIndex;
//    	mCurrentChannelIndex = mChannelManager.nativeGetService(channelNum, new DvbService(),mCurrentPlayType);
    	mCurrentChannelIndex = mChannelManager.nativeGetService(channelNum, new DvbService(),ServiceType.ALL);
    	if(mCurrentChannelIndex == tempChannelIndex){
    		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_INFO_KEY));
    		return;
    	}
    	log.D("switchChannelFromNum(): index  = " + mCurrentChannelIndex);
    	if (mCurrentChannelIndex < 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,channelNum));
			mCurrentChannelIndex = tempChannelIndex;
			return;
		}
    	mLastChannelIndex = tempChannelIndex;
    	mLastInputNumber = channelNum;
    	switchChannelNew(mCurrentChannelIndex, 0);
    }
    
    public void switchChannelFromEPG(int channelNum){
        log.D("switchChannelFromNum");
        int tempChannelIndex = mCurrentChannelIndex;
//      mCurrentChannelIndex = mChannelManager.nativeGetService(channelNum, new DvbService(),mCurrentPlayType);
        mCurrentChannelIndex = mChannelManager.nativeGetService(channelNum, new DvbService(),ServiceType.ALL);
        log.D("switchChannelFromNum(): index  = " + mCurrentChannelIndex);
        if (mCurrentChannelIndex < 0) {
            dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,channelNum));
            mCurrentChannelIndex = tempChannelIndex;
            return;
        }
        mLastChannelIndex = tempChannelIndex;
        mLastInputNumber = channelNum;
        switchChannelNew(mCurrentChannelIndex,400,false);
    }
    
    
    private int mLastInputNumber;
    
    public void switchChannelFromNum(int serviceType,int num) {
//		if(getmCurrentPlayType() != (serviceType & 0x0F))
//			switchPlayMode();
		switchChannelFromNum(num);
    }

    public void reInitChannels(){
    	mDvbPlayManager.reInitChannels();
    }
	public void showSoundTrackSetting() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_SOUNDTRACK_WINDOWN));
	}

	public void showMainMenu() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_MAIN_MENU));
	}

	public void showFavoriteChannel() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_FAVORITE_CHANNEL_WINDOWN));
	}

	public void showChannelList() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_CHANNEL_LIST_WINDOWN));
	}
	
	
	/**
	 * 获取当前频道数量 主菜单使用
	 */
	public int getTotalChannelSize(){
		return DVBPlayManager.getTotalChannelSize(getmCurrentPlayType());
	}
	
	/**
     * 获取当前频道数量 主菜单使用
     */
    public int getEpgTotalChannelSize(){
        return DVBPlayManager.getTotalChannelSize(ServiceType.TV);
    }
	/**
	 * 通过角标获取Service 0开始
	 */
	public DvbService getChannelByListIndex(int index){
		
//		return mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), index,DVBPlayManager.LIST_INDEX);
		
		return mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.LIST_INDEX);
	}
	
	public DvbService getEpgChannelByListIndex(int index){
        
//      return mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(), index,DVBPlayManager.LIST_INDEX);
        
        return mDvbPlayManager.getChannelByIndex(ServiceType.TV, index,DVBPlayManager.LIST_INDEX);
    }
	
	/**
	 * 通过角标获取Service 
	 * @param index 底层角标
	 * @return
	 */
	public DvbService getChannelByNativeIndex(int index){
		return mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(),index,DVBPlayManager.NATIVE_INDEX);
	}

	public ArrayList<Integer> getFavouriteIndex() {
		return mDvbPlayManager.getFavouriteIndex(getmCurrentPlayType());
	}
	
	/**
	 * 喜爱频道 使用
	 * @param channelIndex
	 * @return
	 */
	public DvbService getChannelByChannelIndex(int channelIndex) {
		return mDvbPlayManager.getChannelByIndex(getmCurrentPlayType(),channelIndex,DVBPlayManager.NATIVE_INDEX);
	}
	
	public MiniEpgNotify getPfByEpg(DvbService service){
		log.D("getMiniEpg()....");
		ArrayList<EpgEvent> epgList = new ArrayList<EpgEvent>();
		String tsTime = mSettingManager.nativeGetTimeFromTs();
		String[] strings = tsTime.split(":");
		long utcTime = Long.valueOf(strings[0]) * 1000;
		log.D("utcTime = "+ utcTime);
		mDvbPlayManager.nativeGetEpgDataByDuration(service.getServiceId(), epgList, utcTime, utcTime + 3600*8*1000);
		if(epgList.size() > 2){
			log.D("epgList.size()>2");
			MiniEpgNotify epgNotify = new MiniEpgNotify();
			epgNotify.setCurrentEventName(epgList.get(0).getProgramName());
			epgNotify.setCurrentEventStartTime(epgList.get(0).getStartTime());
			epgNotify.setCurrentEventEndTime(epgList.get(0).getEndTime());
			
			epgNotify.setNextEventName(epgList.get(1).getProgramName());
			epgNotify.setNextEventStartTime(epgList.get(1).getStartTime());
			epgNotify.setNextEventEndTime(epgList.get(1).getEndTime());
			log.D(epgNotify.toString());
			return epgNotify;
		}else{
			log.D("epgList.size() = " + epgList.size());
			return null;
		}
	}
	
	public int getPf(int serviceId,MiniEpgNotify pf){
		return mDvbPlayManager.nativeGetPFEventInfo(serviceId, pf);
	}

	private static final String VIDTYPE_INTERLACE = "/sys/module/amvdec_mpeg12/parameters/disable_jitter";
	public void showProgramGuide() {
	    mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.STOP_PLAY));
            }
        }, 300);
	    isEpgReceive = true;
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_GUIDE));
		DvbUtil.writeFile(VIDTYPE_INTERLACE,"1");
	}

	public void showProgramReserve() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE));
	}
	public void getEpgDataByDuration(ArrayList<EpgEvent> mEpgEvents,
			long startTime, long endTime) {
		int serviceId = getCurrentChannel().getServiceId();
		mDvbPlayManager.nativeGetEpgDataByDuration(serviceId, mEpgEvents, startTime, endTime);
	}

	/**
	 * 供Launcher使用
	 */
	public void init2() {
		mDvbPlayManager = DVBPlayManager.getInstance(mContext);
		mDvbPlayManager.init();
	}
	
	/**
	 * 供Launcher使用
	 */
	public void uninit2(){
		mDvbPlayManager.uninit();
	}
	
	/**
	 * 供Launcher使用
	 * @param lastChannelNum
	 */
	public void playlast2(int lastChannelNum){
		mDvbPlayManager.playLast(ServiceType.TV,lastChannelNum);
	}
	
	/**
	 * 供Launcher使用
	 */
	public void stop2(){
		mDvbPlayManager.stop();
	}
	
    public static boolean isDVBChannelEnable(int type){
    	int totalNum = DVBPlayManager.getTotalChannelSize(type);
    	return totalNum > 0 ? true : false;
    }
    
	public int getLastTunerParam() {
		return mLastTunerParam;
	}
	
	public void getAllChannelIndex(ArrayList<Integer> mFavList,ArrayList<Integer> mTvChannelList,
			ArrayList<Integer> mBcChannelList) {
		mDvbPlayManager.getAllChannelIndex(mFavList,mTvChannelList,mBcChannelList);
	}

	public void switchChannelFromIndex(int serviceType,int index) {
//		if(getmCurrentPlayType() !=serviceType)
//			switchPlayMode();
		mLastChannelIndex = mCurrentChannelIndex;
		mCurrentChannelIndex = index;
		if(mCurrentChannelIndex == mLastChannelIndex){
		    dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_INFO_KEY));
		    return;
		}
		switchChannelNew(index);
	}
	
	/**
	 * 获取当前节目信息 频道列表使用
	 * @param serviceId
	 * @return
	 */
	public NETEventInfo getCurrentProgramInfo(int serviceId) {
		long startTime = getUtcTime();
		ArrayList<Integer> programIdList = new ArrayList<Integer>();
		mEPGManager.nativeGetPFEvent(serviceId, startTime, programIdList);
		NETEventInfo eventInfo = new NETEventInfo();
		if(programIdList.size()>0){
			mEPGManager.nativeGetProgramInfo(programIdList.get(0), eventInfo);
			log.D(" dvbcontroller  "+eventInfo);
			return eventInfo;
		}
		else
			return null;
	}
	
	public String getChannelIconPath(int serviceId) {
		String path = mEPGManager.nativeGetTVIcons(serviceId);
		log.D(" dvbcontroller iconPath  "+path);
		if(path !=null && !path .equals(""))
			return path;
		return null;
	}
	
	/** 响应遥控数字键切台 */
	public void executeNumKey(int keyNum , boolean now){
		Log.d(MTAG, "--- executeNumKey ---");
		if(!now){
			//即时更新右上角台号
			inputNumber(keyNum);
		}else{
			//切台
			int tempChannelIndex = mCurrentChannelIndex;
	    	mCurrentChannelIndex = mChannelManager.nativeGetService(keyNum, new DvbService(),getmCurrentPlayType());
	    	log.D("switchChannelFromNum(): index  = " + mCurrentChannelIndex);
	    	if (mCurrentChannelIndex < 0) {
				dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL));
				mCurrentChannelIndex = tempChannelIndex;
				return;
			}
	    	mLastChannelIndex = tempChannelIndex;
	    	trunChannel();
	    	inputNumberChange(keyNum);
		}
	}
	
    
    public int surePositionTrue(int total , int index){
    	int position;
		if(index<0){
			position = total+index;
			return surePositionTrue(total, position);
		}else if(index==0){
			position = 0;
		}else if(index > total){
			position = index - total;
			return surePositionTrue(total, position);
		}
		else if(index == total){
			position = 0;
		}else{
			position = index;
		}
		
		return position;
	}
    
    /** 切台 */
    private void trunChannel(){
    	Log.d(TAG, "---trunChannel---");
		mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_NOW);
		mSwitchChannelHandler.sendEmptyMessage(SWITCH_CHANNEL_NOW);
    }
    /** 无动画切台 */
    private void trunChannelByNoAnimation(){
    	Log.d(TAG, "---trunChannelByNoAnimation---");
    	mSwitchChannelHandler.removeMessages(SWITCH_CHANNEL_NOW);
		mSwitchChannelHandler.sendEmptyMessageDelayed(SWITCH_CHANNEL_NOW,300);
    }
    /** getTime */
    public long getUtcTime() {
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
        log.D("UTC TIME = " + currentTimeMillis + ";utcTime[0] = "+ utcTime[0]);
        log.D(DateFormatUtil.getDateFromMillis(currentTimeMillis) + "  "+DateFormatUtil.getTimeFromMillis(currentTimeMillis));
        return currentTimeMillis;
    }

    
    
	
	public int getFavoriteCount(){
		return DVBPlayManager.mFavoriteIndexList.size();
	}
	
	public int getCurrentPosition(){
		if(mCurrentPlayType == ServiceType.BC){
			return DVBPlayManager.mBcIndexList.indexOf(mCurrentChannelIndex);
		}else{
			return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
		}
	}
	
	public int getCurrentPosition1(){
	    if(mCurrentPlayType == ServiceType.BC){
	        return 0;
	    }else{
	    	//修复开机未进入过直播，从launcher直接进入节目指南，显示epg信息为第一个频道而非当前播放频道的问题 by yuhongkun 20130904
//	        return DVBPlayManager.mTvIndexList.indexOf(mCurrentChannelIndex);
	    	return DVBPlayManager.mTvIndexList.indexOf(DVBPlayManager.mCurrentTVChannelIndex);
	    	
	    }
    }
	
	public ArrayList<Integer> getProgramIdListBySid(int serviceId, long startTime, long endTime){
	    ArrayList<Integer> programIdList=new ArrayList<Integer>();
	    mEPGManager.nativeGetProgramIdListBySid(serviceId, startTime, endTime, programIdList);
	    return programIdList;
	}
	
	public NETEventInfo getProgramInfo(int programId){
	    NETEventInfo eventInfo=new NETEventInfo();
	    mEPGManager.nativeGetProgramInfo(programId, eventInfo);
//	    eventInfo.setBegintime(eventInfo.getBegintime()*1000+EpgGuideWindow.TimeOffset);
	    eventInfo.setBegintime(eventInfo.getBegintime()*1000 + TimeOffset);
	    eventInfo.setDuration(eventInfo.getDuration()*1000);
	    return eventInfo;
	}
	
	public NETEventInfo getProgramInfo_LiveGuide(int programId){
        NETEventInfo eventInfo=new NETEventInfo();
        mEPGManager.nativeGetProgramInfo(programId, eventInfo);
        return eventInfo;
    }
	
	public String getTVIcons(int serviceId){
	    return mEPGManager.nativeGetTVIcons(serviceId);
	}
	
	public int getAllChannelCount(){
		return DVBPlayManager.mTvIndexList.size() + DVBPlayManager.mBcIndexList.size();
	}

	/**
     * 通过id获取分类列表
     * @param id 0xff 返回一级分类 ID 和分类名称;否则返回二级分类 ID 和分类名称
     * @param programTypeList 分类信息list，参数作为返回值
     * @return
     */
    public int getProgramTypes(int id, ArrayList<ProgramType> programTypeList) {
        return mEPGManager.nativeGetProgramTypes(id, programTypeList);
    }
    /**
     * 根据一级分类programId,获取正在播放或者即将播放的节目ID List
     * @param programId
     * @param startTime
     * @param endTime
     * @param programIdList 包含返回节目信息
     * @return
     */
    public int getProgramIdListByType(int programId, long startTime, long endTime, ArrayList<Integer> programIdList){
        return mEPGManager.nativeGetProgramIdListByType(programId, startTime, endTime, programIdList);
    }

	public void downF12() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_PROGRAM_RESERVE_ALERT));
	}

	public void showMenu() {
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_MAIN_MENU));
	}

	public int getmCurrentPlayType() {
		return mCurrentPlayType;
	}

	public boolean isBCPlaying() {
		return mCurrentPlayType == ServiceType.BC;
	}
	
	public void nextService(){
    	mLastChannelIndex = mCurrentChannelIndex;
    	log.D("nextService(), mLastChannelIndex = " + mLastChannelIndex);
//		mCurrentChannelIndex = mChannelManager.nativeGetNextDVBService(mCurrentChannelIndex, mCurrentPlayType);
		mCurrentChannelIndex = mChannelManager.nativeGetNextDVBService(mCurrentChannelIndex, ServiceType.ALL);
		switchChannelNew(mCurrentChannelIndex,MSWITCHCHANNELDELAY);
		log.D(" nextService(), mCurrentChannelIndex = "+ mCurrentChannelIndex);
    }
    
    public void previousService(){
    	mLastChannelIndex = mCurrentChannelIndex;
//    	mCurrentChannelIndex = mChannelManager.nativeGetLastDVBService(mCurrentChannelIndex, mCurrentPlayType);
    	mCurrentChannelIndex = mChannelManager.nativeGetLastDVBService(mCurrentChannelIndex, ServiceType.ALL);
		switchChannelNew(mCurrentChannelIndex,MSWITCHCHANNELDELAY);
		log.D(" previousService  index: "+ mCurrentChannelIndex);
    }
    
    /**
	 * 根据频道类型和角标切台，主线程调用
	 * @param type
	 * @param index 上层维护的角标
	 * @param delay
	 */
	public void switchChannelNew(int index,long delay){
		log.D( "switchChannelNew() index = " + index + "; mCurrentType = " + mCurrentPlayType);
		if (index < 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,mLastInputNumber));
			mCurrentChannelIndex = mLastChannelIndex;
			return;
		}
		DvbService destService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.NATIVE_INDEX);
		if (destService.getLogicChNumber() <= 0) {
			dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,mLastInputNumber));
			return;
		}
		if((destService.getServiceType() & ServiceType.BC) == ServiceType.BC){
            DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));
        }else{
            DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
        }
		dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL));
		if(mSwitchChannelThread == null || !mSwitchChannelThread.isAlive()){
			mSwitchChannelThread = new HandlerThread("switch-channel");
			mSwitchChannelThread.start();
			mSwitchChannelHandler = new SChannelHandler(mSwitchChannelThread.getLooper());
		}
		Message msg = new Message();
		msg.what = SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL;
		msg.obj = destService;
		msg.arg1 = index;
		mSwitchChannelHandler.removeMessages(SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL);
		mSwitchChannelHandler.sendMessageDelayed(msg, delay);
	}
	
	
	public void switchChannelNew(int index,long delay,boolean isShowPf){
        Log.d("sognwenxuan", "switchChannelNew()");
        if (index < 0) {
            dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,mLastInputNumber));
            mCurrentChannelIndex = mLastChannelIndex;
            return;
        }
        DvbService destService = mDvbPlayManager.getChannelByIndex(mCurrentPlayType, index,DVBPlayManager.NATIVE_INDEX);
        if (destService.getLogicChNumber() <= 0) {
            dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.ERROR_WITHOUT_CHANNEL,mLastInputNumber));
            return;
        }
        log.D("service type == " + destService.getServiceType());
        if((destService.getServiceType() & ServiceType.BC) == ServiceType.BC){
            log.D("BC----------------------------------------------");
            Message message = Message.obtain(mHandler, DISPATCH_MESSAGE);
            message.arg1 = ViewMessage.START_PLAY_BC;
            mHandler.removeMessages(DISPATCH_MESSAGE);
            mHandler.sendMessageDelayed(message,400);
            isPlayBC = true;
//            DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_BC));
        }else{
            Message message = Message.obtain(mHandler, DISPATCH_MESSAGE);
            message.arg1 = ViewMessage.START_PLAY_TV;
            mHandler.removeMessages(DISPATCH_MESSAGE);
            mHandler.sendMessageDelayed(message,400);
            isPlayBC = false;
            log.D("TV----------------------------------------------");
//            DvbController.this.dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.START_PLAY_TV));
        }
        if(isShowPf)
            dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.SWITCH_CHANNEL));
        if(!mSwitchChannelThread.isAlive()){
            mSwitchChannelThread = new HandlerThread("switch-channel");
            mSwitchChannelThread.start();
            mSwitchChannelHandler = new SChannelHandler(mSwitchChannelThread.getLooper());
        }
        Message msg = new Message();
        msg.what = SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL;
        msg.obj = destService;
        msg.arg1 = index;
        mSwitchChannelHandler.removeMessages(SChannelHandler.SWITCH_TO_SPECIAL_CHANNEL);
        mSwitchChannelHandler.sendMessageDelayed(msg, delay);
    }
	
	/**
	 * 开始搜索epg
	 * @param param
	 * @param type
	 * @return 0: 开始下载,等待完成通知
	 *		  -1: 主频点参数错误
     *        -2: 主频点锁频失败
     *        -3: 今天已经下载过
	 */
	public int startEPGSearch(Transponder param , int type){
		return mEPGManager.nativeStartEPGSearch(param, type);
	}
	
	public int cancelEPGSearch(){
		return mEPGManager.nativeCancelEPGSearch();
	}
	
	public int setEPGSourceMode(boolean isNetMode){
		return mEPGManager.nativeSetEPGSourceMode(isNetMode);
	}
	
	/**
	 * 弹出时移界面
	 */
	public void downTimeShiftKey() {
        dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_TIME_SHIFT_POPUWINDOW));
    }
//	/**
//	 * 从时移回看中恢复到直播
//	 */
//	public void switchToCurentChannel() {
//		switchChannel(mCurrentChannelIndex);
//	}
	
	public void exitProgramGuide(){
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.EXIT_PROGRAM_GUIDE));
	}
	
	public int getTunerStatus(){
	    return mDvbPlayManager.getTunerSignalStatus();
	}
	
	public void showEpgErrorToast(){
	    dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_EPG_TUNER_UNABLE));
	}
    
    public void showBlankView(){
        dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.SHOW_BLANK_VIEW));
    }
    
    public void resetMonitor(){
        if(isPlayBC){
            mHandler.postDelayed((new Runnable() {
                @Override
                public void run() {
                    dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.START_PLAY_BC));
                    dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.RECEIVED_CHANNEL_INFO_KEY));
                }
            }),1000);
        }
        isEpgReceive = false;
        Object[] objs = {getmCurrentPlayType(),isDVBChannelEnable(getmCurrentPlayType()), isTunerEnable, mLastCAParam};
        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.RECEIVED_ERROR_NOTIFY, objs));
        DvbUtil.writeFile(VIDTYPE_INTERLACE,"0");
//        mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				byPassDeinterlace(false);
//			}
//		}, 300);
    }
    
    public void finish(){
        mContext.finish();
    }
    
    public void dismissBlankView(){
        dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.DISMISS_BLANK_VIEW));
    }
    
    public void exitDvb(){
    	dispatchMessage(mViewController,DvbMessage.obtain(ViewMessage.EXIT_DVB));
	}
	 public boolean isPause(){
        return mPause;
    }

	public int queryMsgType(int notificationTvnotifyBuymsg) {
		return mCaManager.nativeQueryMsgType(notificationTvnotifyBuymsg);
	}
	
	//chaidandan
	public void refreshDVBNotify()
	{
		dispatchMessage(mViewController, DvbMessage.obtain(ViewMessage.REFRESH_DVB_NOTIFY));
	}
}
