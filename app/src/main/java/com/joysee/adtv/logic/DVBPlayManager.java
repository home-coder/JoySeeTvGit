package com.joysee.adtv.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.joysee.adtv.common.ChannelTypeNumUtil;
import com.joysee.adtv.common.ChannelVolumeCache;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ScreenModeUtil;
import com.joysee.adtv.common.VideoLayerUtils;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.server.ADTVService;


/**
 * 控制播放，获取频道信息的管理类
 * @author songwenxuan
 *
 */
public class DVBPlayManager {
	static{
		System.loadLibrary("dtvplayer_jni");
	}
    
    private static DVBPlayManager dvbPlayManager;
    private DVBPlayManager(){}
    public static DVBPlayManager getInstance(Context context){
        if(dvbPlayManager == null){
            dvbPlayManager = new DVBPlayManager(context);
        }
        return dvbPlayManager;
    }
    static final DvbLog log = new DvbLog(
            "DVBPlayManager",DvbLog.DebugType.D);
    
    private boolean mIsInit = false;

    private boolean mIsStart = false;

    private Context mAppContext;
    private SettingManager mSettingManager;
    private static AudioManager mAudioManager;
    
    protected static final int MONITOR_CALLBACK = 1;
    protected static final int PF_CALLBACK = 2;
    protected static final int EPG_CALLBACK = 3;
    
    private boolean mIsRegisterLis = false;
    public static final int NATIVE_INDEX = 0;
    public static final int LIST_INDEX = 1;
    
    public String token = "";
    
    public static int mCurrentTVChannelIndex = -1;
    
    public static int mLastTVChannelIndex = -1;
    
    public static int mCurrentBCChannelIndex = -1;
    
    public static int mLastBCChannelIndex = -1;
    
    private Object lock = new Object();
    
    private static ChannelManager mChannelManager;
    
    private static Handler mHandler = new Handler();
    
    /**
     * 电视频道角标列表
     */
    public static ArrayList<Integer> mTvIndexList = new ArrayList<Integer>();
    /**
     * 广播频道角标列表
     */
    public static ArrayList<Integer> mBcIndexList = new ArrayList<Integer>();
    
//    /**
//     * 电视喜爱频道角标列表
//     */
//    public static ArrayList<Integer> mFavoriteTvIndexList = new ArrayList<Integer>();
//    /**
//     * 广播喜爱频道角标列表
//     */
//    public static ArrayList<Integer> mFavoriteBcIndexList = new ArrayList<Integer>();
    public static ArrayList<Integer> mFavoriteIndexList = new ArrayList<Integer>();

	private int mLastTVChannelNum;

	private int mLastBCChannelNum;
    
    private DVBPlayManager(Context context){
        mAppContext = context;
        mChannelVolumeConfig = getChannelVolumeConfig();
    }
    
    /**
     * 初始化播放资源，在应用开始的时候必须调用，也是最先调用的接口。
     */
    public int init(){
        log.D("init()");
        long begin = System.currentTimeMillis();
        int ret = initWrap();
        log.D("time=========================== " + (System.currentTimeMillis()-begin));
        mAudioManager = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        mSettingManager = SettingManager.getSettingManager();
        mChannelManager = ChannelManager.getInstance();
        return ret;
    }

    /**
     * 释放播放资源,在应用退出的时候必须调用。
     */
    public void uninit(){
        log.D("uninit()");
        uninitWrap();
    }

    /**
     * 播放最后一次播放的电视节目
     * @return DvbService(播放VO) 如果有异常返回 null
     */
    public DvbService playLast(int type) {
		log.D("playLast()   type " + type + " mCurrentTVChannelIndex = "
				+ mCurrentTVChannelIndex + " mLastTVChannelIndex = "
				+ mLastTVChannelIndex);
		long begin = System.currentTimeMillis();
		playWrap();
		
		long begin1 = SystemClock.uptimeMillis();
		DvbService service = new DvbService();
		synchronized (lock) {
			switch (type) {
			case DefaultParameter.ServiceType.TV:
			    if(mTvIndexList.size() == 0){
			        return null;
			    }
				if (mCurrentTVChannelIndex == -1) {
					mCurrentTVChannelIndex = mChannelManager.nativeGetNextDVBService(mCurrentTVChannelIndex, ServiceType.TV);
					mChannelManager.nativeGetServiceByIndex(mCurrentTVChannelIndex, service);
                    log.D("mCurrentTVChannelIndex == -1 , service name = " + service.getChannelName());
				}else{
				    mChannelManager.nativeGetCurrentService(service);
				    if(service.getServiceId() == 0){
				    	mCurrentTVChannelIndex = -1; 
				    	mCurrentTVChannelIndex = mChannelManager.nativeGetNextDVBService(mCurrentTVChannelIndex, ServiceType.TV);
						mChannelManager.nativeGetServiceByIndex(mCurrentTVChannelIndex, service);
				    }else{
//                        mCurrentTVChannelIndex = mChannelManager.nativeGetService(service.getLogicChNumber(), new DvbService(), ServiceType.ALL);
				    	mCurrentTVChannelIndex = mChannelManager.nativeGetService(service.getLogicChNumber(), service, ServiceType.ALL);
				    	log.D("mCurrentTVChannelIndex = " + mCurrentTVChannelIndex);
				    }
				}
				log.D("playLast() before setService() : " + (SystemClock.uptimeMillis() - begin1));
				begin1 = SystemClock.uptimeMillis();
				setService(service);
				log.D("playLast() setService() take: " + (SystemClock.uptimeMillis() - begin1));
				log.D("play time = " + (System.currentTimeMillis() - begin));
				return service;
			case DefaultParameter.ServiceType.BC:
				if (mCurrentBCChannelIndex == -1) {
					mCurrentBCChannelIndex = 0;
				}
				if (mBcIndexList.size() == 0)
					return null;
				mChannelManager.nativeGetServiceByIndex(mCurrentBCChannelIndex, service);
				log.D("playLast() before setService() : " + (SystemClock.uptimeMillis() - begin1));
				begin1 = SystemClock.uptimeMillis();
				setService(service);
				log.D("playLast() setService() take: " + (SystemClock.uptimeMillis() - begin1));
				return service;
			default:
				return null;
			}
		}
	}
    
    
    public void playLast(int type,int lastChannelNum) {
    	playWrap();
    	DvbService service = new DvbService();
    	mChannelManager.nativeGetService(lastChannelNum, service,type);
    	setService(service);
    }

    /**
     *  停止播放，在停止播放的时候调用
     */
    public void stop(){
        log.D("stop()");
        long begin = System.currentTimeMillis();
        stopWrap();
        log.D("stop time = " + (System.currentTimeMillis() - begin));
    }
    /**
     *  设置播放窗口大小
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setWinSize(int x,int y,int width,int height){
//    	String display = VideoLayerUtils.getVideoScreen();
//        log.D("videolayerutils.getvideoscreen ="+display);
//        if(display!=null){
//            if(display.equals("720p")||display.equals("720i")){
//                x = (int)(((float)x / (float)1920) * 1280);
//                y = (int)(((float)y / (float)1080) * 720);
//                width = (int)(((float)width / (float)1920) * 1280);
//                height = (int)(((float)height / (float)1080) * 720);
//                log.D("videolayerutils.getvideoscreen  = 1280-720 ");
//            }else if (display.equals("576i")||display.equals("576p")){
//                x = (int)(((float)x / (float)1920) * 720);
//                y = (int)(((float)y / (float)1080) * 576);
//                width = (int)(((float)width / (float)1920) * 720);
//                height = (int)(((float)height / (float)1080) * 576);
//                log.D("videolayerutils.getvideoscreen = 1024-576 ");
//            }else{//1080p 1080i
//                log.D("videolayerutils.getvideoscreen  = 1920-1080 ");
//            }
//        }else{
//            log.D("videolayerutils.getvideoscreen  =  null");
//        }
//
//        log.D("set win size x=" + x+" y="+y+" width="+width+" height="+height);
        mSettingManager.nativeSetVideoWindow(x, y, width, height);
    }

    private static OnMonitorListener onMonitorListener;

    public void setOnMonitorListener(OnMonitorListener lis){
        onMonitorListener = lis;
    }
    
	private void changeSystemVolumeWhenSetService(DvbService service){
		final int volume = ChannelVolumeCache.getVolume(mAppContext, service);
		log.D("changeSystemVolumeWhenSetService()  channelVol = " + volume);
		setVolume(volume, 0);
	}
	
	public void setVolume(int volume) {
		setVolume(volume, AudioManager.FLAG_SHOW_UI);
	}
	
	public void setVolume(int volume, int flag) {
		if (flag != 0) {
			boolean mute = false;
//			mute = ((Object) mAudioManager).isStreamMute(AudioManager.STREAM_MUSIC);//TODO 厂商定义的静音判断方法
			if (mute) {
				mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			}
		}
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume == 0 ? 0 : volume, flag);
		log.D("setVolume volume = " + volume);
	}
	
	public void saveCurrentChannelVolume(Context context, DvbService service, int volume) {
		ChannelVolumeCache.saveVolume(context, service, volume);
	}

    public int setService(DvbService service){
    	log.D("call native set service." + service.toString());
    	log.D("set service end = " + System.currentTimeMillis());
    	
    	if (mChannelVolumeConfig == 1) {
//    		mAudioManager.hideVolumePanel();//TODO 
    		changeSystemVolumeWhenSetService(service);
    	}
    	
    	long begin = System.currentTimeMillis();
        int ret = nativeChangeService(service);
        log.D("nativesetService end ~ time = " + (System.currentTimeMillis() - begin));
        return ret;
    }
    
    /**
     * 获取所有频道数目 根据广播还是电视类型
     * @param type 广播或者电视
     * @return 所有频道数量
     */
    public static int getTotalChannelSize(int type){
    	switch(type){
		case DefaultParameter.ServiceType.TV:
			return mTvIndexList.size();
		case DefaultParameter.ServiceType.BC:
			return mBcIndexList.size();
		default:
			throw new RuntimeException();
		}
    }
    
    public int getCurrentAudioIndexSum(){
    	return mSettingManager.nativeGetAudioLanguage();
    }
    
    /**
     *  0 有信号
     *  1 无信号
     *  -1 失败
     * @return
     */
    public int getTunerSignalStatus(){
       long begin = System.currentTimeMillis();
    	int ret = nativeGetTunerSignalStatus();
    	log.D("get tuner status cost time = "+(System.currentTimeMillis() - begin));
    	return ret;
    }

    /**
     * 开始播放，供dvb用
     */
    public void play(){
        playWrap();
    }
    
    /** 不缓存取喜爱频道列表 */
    public ArrayList<Integer> getFavouriteIndex(int type){
//    	switch(type){
//		case ServiceType.TV:
//			return mFavoriteTvIndexList;
//		case ServiceType.BC:
//			return mFavoriteBcIndexList;
//    	}
    	return mFavoriteIndexList;
    }
    
    public void reInitChannels(){
    	log.D("reInitChannels begin");
    	initChannels();
    	ChannelTypeNumUtil.savePlayChannel(mAppContext, ServiceType.TV, 0);
    	ChannelTypeNumUtil.savePlayChannel(mAppContext, ServiceType.BC, 0);
    	mCurrentTVChannelIndex = -1;
    	mCurrentBCChannelIndex = -1;
    	ADTVService.getService().epg.removoAllReservedPrograms();
		log.D("reInitChannels end");
    }
    
    /**
     * 初始化频道信息到内存中 分为广播和电视两种  之后更改为调用本地方法获取全部频道信息后初始化
     * @param context
     */
	public void initChannels() {
		initChannelIndexList();
		mLastTVChannelNum = ChannelTypeNumUtil.getPlayChannelNum(mAppContext, ServiceType.TV);
		log.D("mlastTvChannelNum  = " + mLastTVChannelNum);
		mCurrentTVChannelIndex = mChannelManager.nativeGetService(mLastTVChannelNum, new DvbService(),ServiceType.ALL);
		log.D("initChannels(),  mCurrentTVChannelIndex = " + mCurrentTVChannelIndex);
		mLastBCChannelNum = ChannelTypeNumUtil.getPlayChannelNum(mAppContext, ServiceType.BC);
		mCurrentBCChannelIndex = mChannelManager.nativeGetService(mLastBCChannelNum, new DvbService(),ServiceType.BC);
	}

	/**
	 * 根据根据广播或者电视类型 及频道列表下标取得频道号
	 * 
	 * @param type
	 *            广播或者电视
	 * @param index
	 *            频道列表下标
	 * @return 频道号
	 */
	public int getChannelNum(int type, int index) {
		DvbService service = new DvbService();
		switch (type) {
		case DefaultParameter.ServiceType.TV:
			if (mTvIndexList.size() == 0 || index == -1) {
				return -1;
			}
			mChannelManager.nativeGetServiceByIndex(index, service);
			return service.getLogicChNumber();
		case DefaultParameter.ServiceType.BC:
			if (mBcIndexList.size() == 0 || index == -1) {
				return -1;
			}
			mChannelManager.nativeGetServiceByIndex(index, service);
			return service.getLogicChNumber();
		default:
			throw new RuntimeException();
		}
		
	}
	
	public int getDisplayMode(){
		return ScreenModeUtil.getScreenMode(mAppContext);
	}
	
	public void setDisplayMode(int mode){
		ScreenModeUtil.saveScreenMode(mAppContext, mode);
		mSettingManager.nativeSetVideoAspectRatio(mode);
	}
	
	/**
	 * 设置伴音
	 * @param type 当前播放类型 1 电视 2 广播
	 * @param index
	 * @param audioIndex
	 */
	public void setAudioIndex(int type, int index, int audioIndex) {
		if (audioIndex == -1)
			throw new RuntimeException("Invalid AudioIndex");
		int lastAudioIndex = -1;
		DvbService service = getChannelByIndex(type, index ,NATIVE_INDEX);
		lastAudioIndex = service.getAudioIndex();
		try {
			log.D("DVBPlayer setAudioIndex channelNum = " + service.getLogicChNumber() + " audioIndex = " + audioIndex);
			mSettingManager.nativeSetAudioLanguage(audioIndex);
			if (lastAudioIndex != audioIndex) {
				service.setAudioIndex(audioIndex);
				if(type == ServiceType.TV)
					nativeSyncServiceToProgram(index, service);
				else if(type == ServiceType.BC)
					nativeSyncServiceToProgram(index, service);
			}
		} catch (Exception e) {
			service.setAudioIndex(lastAudioIndex);
			mSettingManager.nativeSetAudioLanguage(lastAudioIndex);
			throw new RuntimeException("setAudioIndex catch " + e.getMessage());
		}
	}
	/**
	 * 不缓存频道的情况下 获取伴音信息
	 * @param channelIndex
	 * @return
	 */
	public int getAudioIndex(int type, int channelIndex){
		DvbService service = getChannelByIndex(type, channelIndex ,NATIVE_INDEX);
		if (service == null)
			throw new RuntimeException("INVALID CHANNELNUM");
		return service.getAudioIndex();
	}
	
	/**
	 * 不缓存频道的情况下  设置声道
	 * @param type: 1 电视 2 广播
	 * @param index
	 * @param audioChannel
	 */
	public void setSoundTrack(int type, int index, int soundTrack) {
		int lastSoundTrack = -1;
		DvbService service = getChannelByIndex(type, index,NATIVE_INDEX);
		if (service.getLogicChNumber() == 0)
			return;
		lastSoundTrack = service.getSoundTrack();
		try {
			log.D("DVBPlayer setSoundTrack channelNum = " + service.getLogicChNumber() + " soundTrack = " + soundTrack);
			mSettingManager.nativeSetSoundTrackMode(soundTrack);
			if (lastSoundTrack != soundTrack) {
				service.setSoundTrack(soundTrack);
				log.D("setSoundTrack(): "+ service.toString());
				if(type == ServiceType.TV){
					nativeSyncServiceToProgram(index, service);
				}else if(type == ServiceType.BC){
					nativeSyncServiceToProgram(index, service);
				}
			}
		} catch (Exception e) {
			service.setSoundTrack(lastSoundTrack);
			mSettingManager.nativeSetSoundTrackMode(lastSoundTrack);
			throw new RuntimeException("setSoundTrack catch " + e.getMessage());
		}
	}
	
	/**
	 * 不缓存频道存喜爱列表
	 * @param type
	 * @param index
	 * @param favorite
	 */
	public void setChannelFavorite(int type, int index, int favorite) {
		int lastChannelFavorite = -1;
		DvbService service = getChannelByIndex(type, index,NATIVE_INDEX);
		if (service == null)
			return;
		lastChannelFavorite = service.getFavorite();
		try {
			log.D("DVBPlayer setChannelFavorite channelNum = " + service.getLogicChNumber() + " favorite = " + favorite);
			if (lastChannelFavorite != favorite) {
				log.D("lastChannelFavorite != favorite, so save to xml");
				switch (favorite) {
				case FavoriteFlag.FAVORITE_YES:
					switch (service.getServiceType() & 0x0F) {
					case ServiceType.TV:
						log.D("set fav begin...tv...service type = " + service.getServiceType());
						service.setServiceType(service.getServiceType() | ServiceType.FAVORITE);
						log.D("set fav end...tv...service type = " + service.getServiceType());
						if(!mFavoriteIndexList.contains(index)){
							mFavoriteIndexList.add(index);
//							Collections.sort(mFavoriteTvIndexList);
						}
						nativeSyncServiceToProgram(index, service);
						break;
					case ServiceType.BC:
						log.D("set fav begin...bc...service type = " + service.getServiceType());
						service.setServiceType(service.getServiceType() | ServiceType.FAVORITE);
						log.D("set fav end...bc...service type = " + service.getServiceType());
						if(!mFavoriteIndexList.contains(index)){
							mFavoriteIndexList.add(index);
//							Collections.sort(mFavoriteBcIndexList);
						}
						nativeSyncServiceToProgram(index, service);
						break;
					}
					break;
				case FavoriteFlag.FAVORITE_NO:
					switch (service.getServiceType() & 0x0F) {
					case ServiceType.TV:
						log.D("del fav begin...tv...service type = " + service.getServiceType());
						service.setServiceType(service.getServiceType() & 0xFFFFFE01);
						log.D("del fav end...tv...service type = " + service.getServiceType());
						if(mFavoriteIndexList.contains((Object)index)){
							mFavoriteIndexList.remove((Object)index);
//							Collections.sort(mFavoriteTvIndexList);
						}
						nativeSyncServiceToProgram(index, service);
						break;
					case ServiceType.BC:
						log.D("del fav begin...bc...service type = " + service.getServiceType());
						service.setServiceType(service.getServiceType() & 0xFFFFFE02);
						log.D("del fav end...bc...service type = " + service.getServiceType());
						if(mFavoriteIndexList.contains((Object)index)){
							mFavoriteIndexList.remove((Object)index);
//							Collections.sort(mFavoriteBcIndexList);
						}
						nativeSyncServiceToProgram(index, service);
						break;
					}
					break;
				}
			}
		} catch (Exception e) {
			service.setFavorite(lastChannelFavorite);
			throw new RuntimeException("setChannelFavorite catch " + e.getMessage());
		}
	}
	
    public interface OnMonitorListener{
        void onMonitor(int monitorType,Object message);
    }

    public boolean visibleVideoLayer(boolean visible){
        log.D("visibleVideoLayer visible = "+visible);
        return nativeVisibleVideoLayer(visible);
    }
    public synchronized int initWrap() {
        return nativeInit();
    }
    public synchronized int uninitWrap() {
        return nativeUninit();
    }

    public synchronized int playWrap() {
//        if (mIsStart) {
//            log.D("playStart() second call play");
//            return -1;
//        } else {
            log.D("playStart() first call play");
//            mIsStart = true;
            nativeDisableKeepLastFrame(false);
            return nativePlay();
//        }
    }
    private int serviceId = -1;
    public synchronized int stopWrap() {
        log.D("stopWrap()");
        mIsStart = false;
        serviceId = -1;
        nativeDisableKeepLastFrame(false);
        return nativeStop();
    }
    
    public synchronized int setServiceWrap(DvbService service, boolean force) {

        if (!force && serviceId == service.getServiceId()) {
            log.D("setServiceWrap serviceId is same so return -1");
            return -1;
        }
        serviceId = service.getServiceId();
        int ret = nativeChangeService(service);
        return ret;
    }
    
    /**
     * JNI 回调的函数 各种监控 包括pf、搜索的回调 都在这里进行。
     * @param monitorType
     * @param message
     */
    public synchronized static void onDTVPlayerCallBack(final int monitorType, final Object message) {
        if(onMonitorListener != null){
            log.D("MONITOR_CALLBACK' onMonitorListener != null monitorType = " + monitorType);
            mHandler.post(new Runnable() {
				public void run() {
					onMonitorListener.onMonitor(monitorType, message);
				}
			});
        }
    }
    
    
//test    
//    private static List<DvbService> testServices = new ArrayList<DvbService>();
    
    /**
     * 初始化频道角标列表
     */
    public static void initChannelIndexList(){
    	long start = System.currentTimeMillis();
    	log.D("initchannelIndexList-----------start");
    	mTvIndexList.clear();
    	mBcIndexList.clear();
//    	mFavoriteTvIndexList.clear();
//    	mFavoriteBcIndexList.clear();
    	mFavoriteIndexList.clear();
    	int tempIndex = -1;
    	while(true){
//    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.TV);
    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.ALL);
//    		log.D("tv tempindex = " + tempIndex);
    		if(tempIndex < 0 || (mTvIndexList.size() > 0 && tempIndex == mTvIndexList.get(0))){
    			break;
    		}
    		mTvIndexList.add(tempIndex);
    	}
//    	tempIndex = -1;
//    	while(true){
//    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.BC);
////    		log.D("bc tempindex = " + tempIndex);
//    		if(tempIndex < 0 || (mBcIndexList.size() > 0 && tempIndex == mBcIndexList.get(0))){
//    			break;
//    		}
//    		mBcIndexList.add(tempIndex);
//    	}
//    	tempIndex = -1;
//    	while(true){
//    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.TV | ServiceType.FAVORITE);
////    		log.D("ftv tempindex = " + tempIndex);
//    		if(tempIndex < 0 || (mFavoriteTvIndexList.size() > 0 && tempIndex == mFavoriteTvIndexList.get(0))){
//    			break;
//    		}
//    		mFavoriteTvIndexList.add(tempIndex);
//    	}
//    	tempIndex = -1;
//    	while(true){
//    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.BC | ServiceType.FAVORITE);
////    		log.D("fbc tempindex = " + tempIndex);
//    		if(tempIndex < 0 || (mFavoriteBcIndexList.size() > 0 && tempIndex == mFavoriteBcIndexList.get(0))){
//    			break;
//    		}
//    		mFavoriteBcIndexList.add(tempIndex);
//    	}
    	tempIndex = -1;
    	while(true){
    		tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, ServiceType.FAVORITE);
//    		log.D("fbc tempindex = " + tempIndex);
    		if(tempIndex < 0 || (mFavoriteIndexList.size() > 0 && tempIndex == mFavoriteIndexList.get(0))){
    			break;
    		}
    		mFavoriteIndexList.add(tempIndex);
    	}
    	log.D("initchannelIndexList-----------end  initIndexTime = " +  (System.currentTimeMillis() - start));
//test
//    	testServices.clear();
//    	for(int i = 0;i<100;i++){
//    		DvbService destService = new DvbService();
//	        destService.setLogicChNumber(i+1);
//	        destService.setChannelName("测试电视台"+(i+1));
//	        if(i<8)
//	        	destService.setFavorite(1);
//	        destService.setVideoPid(1);
//	        destService.setAudioEcmPid0(1);
//	        destService.setAudioType0(6);
//	        destService.setAudioIndex(i%3);
//	        destService.setAudioChannel(i%3);
//	        testServices.add(destService);
//    	}
//    	for(int i=0;i < 100; i++){
//    		if(i%10==0){
//    			bcIndexList.add(i);
//    			DvbService dvbService = testServices.get(i);
//    			bcIndexNumMap.put(dvbService.getLogicChNumber(), bcIndexList.size()-1);
//
//    		}else{
//    			tvIndexList.add(i);
//    			DvbService dvbService = testServices.get(i);
//    			tvIndexNumMap.put(dvbService.getLogicChNumber(), tvIndexList.size()-1);
//    		}
//    	}
    }
    /**
     * 通过角标获取到DvbService
     * @param type 类型
     * @param index 上层按类型维护的角标
     * @return
     */
    public DvbService getChannelByIndex(int type,int index,int from){
    	DvbService service = new DvbService();
    	switch (type) {
			case DefaultParameter.ServiceType.TV:
				if(from == NATIVE_INDEX){
					mChannelManager.nativeGetServiceByIndex(index, service);
				}else{
				    if (index < 0 || index >= mTvIndexList.size())
				        return null;
					mChannelManager.nativeGetServiceByIndex(mTvIndexList.get(index), service);
				}
				break;
			case DefaultParameter.ServiceType.BC:
				if(from == NATIVE_INDEX){
					mChannelManager.nativeGetServiceByIndex(index, service);
				}else{
				    if (index < 0 || index >= mTvIndexList.size())
                        return null;
					mChannelManager.nativeGetServiceByIndex(mBcIndexList.get(index), service);
				}
				break;
			default:
				break;
		}
    	return service;
    }
    
	/**
     * 根据播放类型（广播或者电视）及频道号切换到指定的频道
     * @param type
     * @return 
     */
	public int switchToSpecialChannel(int type, DvbService service ,int index) {
		int ret = -1;
		switch (type) {
		case DefaultParameter.ServiceType.TV:
			synchronized (lock) {
				mLastTVChannelIndex = mCurrentTVChannelIndex;
				mCurrentTVChannelIndex = index;
				ret = setService(service);
			}
			break;
		case DefaultParameter.ServiceType.BC:
			synchronized (lock) {
				mLastBCChannelIndex = mCurrentBCChannelIndex;
				mCurrentBCChannelIndex = index;
				ret = setService(service);
			}
			break;
//		default:
//			return -1;
		}
		if (service != null) {
			log.D("DVBPlayer switchToSpecialChannel before savePlayChannel");
			ChannelTypeNumUtil.savePlayChannel(mAppContext, type, service.getLogicChNumber());
		}
		return ret;
	}
//test	
//	public DvbService testGetServiceByIndex(int index){
//		return testServices.get(index);
//	}
	/**
     * 初始化
     * @return
     */
    private native int nativeInit();
    
    /**
     * 释放资源的本地方法
     * @return
     */
    private native int nativeUninit();
    /**
     * 控制播放的本地方法
     * @return
     */
    public native int nativePlay();

    /**
     * 控制播放停止的本地方法
     * @return
     */
    public native int nativeStop();
    
    /**
     * 获取信号线状态的本地方法
     * @return
     */
    public native int nativeGetTunerSignalStatus();

    /**
     * 切换频道的本地方法
     * @param service
     */
    public native int nativeChangeService(DvbService service);
    
    /**
     * 控制视频层是否可见的本地方法
     * @param visible
     */
    public native boolean nativeVisibleVideoLayer(boolean visible);
    
    /**
     * 关闭结束时保持上一视频帧功能
     * @param bEnable  false:不保留，true:保留
     */
    public native int nativeDisableKeepLastFrame(boolean bEnable);
    
    /**
     * 获取播放状态
     * @param state
     */
    public native int nativeGetPlayState();
    
    /**
     * 获取指定频点，指定时间段的节目信息
     * @param serviceId 频道的serviceId
     * @param epglist 传入后由底层赋值。当作返回值使用
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return 是否成功
     */
    public native int nativeGetEpgDataByDuration(int serviceId,ArrayList<EpgEvent> epglist,long startTime,long endTime);
    
    /**
     * 获取pf信息
     * @param serviceId 当前频道的serviceId
     * @param epgNotify 封装MiniEpg的类，传进去让底层赋值，当作返回值使用
     * @return
     */
    public native int nativeGetPFEventInfo(int serviceId,MiniEpgNotify epgNotify);
    
    /**
     * 根据角标将service同步到底层
     * @param tvIndex 频道角标
     * @param service 
     * @return
     */
    public native int nativeSyncServiceToProgram(int tvIndex,DvbService service);
    
	public void getAllChannelIndex(ArrayList<Integer> mFavList,ArrayList<Integer> mTvChannelList,
			ArrayList<Integer> mBcChannelList) {
		int size = mFavoriteIndexList.size();
		if(size > 0){
			for(int i = size-1;i>=0;i--){
				mFavList.add(mFavoriteIndexList.get(i));
			}
		}
		mTvChannelList.addAll(mTvIndexList);
		mBcChannelList.addAll(mBcIndexList);
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
    
}
