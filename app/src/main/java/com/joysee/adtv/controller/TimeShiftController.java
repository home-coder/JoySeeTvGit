package com.joysee.adtv.controller;



import com.google.gson.Gson;
import com.joysee.adtv.common.DateTools;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.TimeShiftJniManager;
import com.joysee.adtv.logic.TimeShiftJniManager.OnMonitorListener;
import com.joysee.adtv.logic.bean.MediaInfo;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TimeShiftController implements OnMonitorListener{
    private static final String TAG = TimeShiftController.class
            .getCanonicalName();
    public static final int SEEKTO_COMPLIETE=1;
    private  static String vod_url="timeshift://mainfreq=%1$s,serviceid=%2$s";
    private static String test_url="dvb://localhost/info?ByPlay=0&TuningParam=%1$s,6875,2&service_id=%2$s";
    /**
     * 参数错误
     */
    public static final int PARA_ERROR=-1;
    /**
     * 播放初始化
     */
    public static final int INIT = 0;
    /**
     * 播放
     */
    public static final int PLAY = 1;
    /**
     * 暂停
     */
    public static final int PAUSE = 2;
    /**
     * 快进
     */
    public static final int FAST_PLAY_FORWARD = 3;
    /**
     * 快退
     */
    public static final int FAST_PLAY_BACK = 4;
    /**
     * 停止
     */
    public static final int STOP =5;
    
    /**
     * 服务没启
     */
    public static final int STATUS_UNINITED=6;
    /**
     * 0级倍速 正常播放
     */
    public static final int SPEED_0=0;
    
    /**
     * 1级倍速 
     */
    public static final int SPEED_1=1;
    /**
     * 2级倍速
     */
    public static final int SPEED_2=2;
    /**
     * 3级倍速
     */
    public static final int SPEED_3=3;
    /**
     * 最大倍速
     */
    public static final int SPEED_MAX=8;
    
    /**
     * 当前倍速
     */
    private volatile int current_speed=SPEED_0;
    private static final int SPEED=3;
    /**
     * 当前状态
     */
    private volatile int current_state=INIT;
    
    /**
     * 需要跳转的位置
     */
    private static String needToSeekPosition=null;
    /**
     * 最新一次跳转成功后的位置
     */
    private static String seekLastTimePosition=null;
    
    private Object lock=new Object();

    private static final int DELAY_TIME=2000;
    public static final int MAX_SEEKBAR_LENGTH = 1000;
    private MediaInfo mMediaInfo;
    private volatile long mCurrentRecordTime=0;
    private volatile long mCurrentPlayTime=0;
    private volatile long beginTime=0;
    private volatile long endTime=0;
    
    private boolean isPlaying=false;
    private boolean isTimeShift=true;
    
    private static TimeShiftController mTimeShiftController;
    
    private static OnPlayListener mOnPlayListener;
    private AudioManager mAudioManager = null;
    private boolean isMuteON = false;

    private void setOnPlayListener(OnPlayListener lis){
    	mOnPlayListener = lis;
    }
    
    public static TimeShiftController getInstance(){
        if(mTimeShiftController==null){
            synchronized (TimeShiftJniManager.class) {
                if(mTimeShiftController==null){
                	mTimeShiftController=new TimeShiftController();
                    return mTimeShiftController;
                }else{
                    return mTimeShiftController;
                }
            }
        }else{
            return mTimeShiftController;
        }
    }
    
    public int getPlayState() {
        Log.d(TAG, " getPlayState before = " + current_state);
        int tempState = TimeShiftJniManager.getInstance().getPlayState();
        Log.d(TAG, " getPlayState after = " + tempState);
        return tempState;
    }

	/**
	 * 快进
	 * 
	 * @return
	 */
	public synchronized int fastPlayForward() {
		if (current_state == PLAY) {
			current_speed = SPEED_1;
		} else if (current_state == PAUSE) {
			current_speed = SPEED_1;
		} else if (current_state == FAST_PLAY_BACK) {
			current_speed = SPEED_1;
		} else if (current_state == FAST_PLAY_FORWARD) {
			if (current_speed < 8) {
				current_speed++;
			}
		}
		if (current_speed >= SPEED_MAX) {
			current_speed = SPEED_MAX;
			current_state = FAST_PLAY_FORWARD;
			return current_state;
		}
		if (!isMuteON) {
			setStreamMute(true);// 静音
		}
        /**
         * 调jni执行快进
         */
        isPlaying = true;
        TimeShiftJniManager.getInstance().fastPlay(
                TimeShiftJniManager.PLAY_FORWARD);
    
//		new Thread() {
//
//			@Override
//			public void run() {
//				/**
//				 * 调jni执行快进
//				 */
//				isPlaying = true;
//				TimeShiftJniManager.getInstance().fastPlay(
//						TimeShiftJniManager.PLAY_FORWARD);
//			}
//
//		}.start();

		current_state = FAST_PLAY_FORWARD;
		return current_state;
	}

	/**
	 * 快退
	 * 
	 * @return
	 */
	public synchronized int fastPlayBack() {
		if (current_state == PLAY) {
			current_speed = SPEED_1;
		} else if (current_state == PAUSE) {
			current_speed = SPEED_1;
		} else if (current_state == FAST_PLAY_FORWARD) {
			current_speed = SPEED_1;
		} else if (current_state == FAST_PLAY_BACK) {
			if (current_speed < SPEED_MAX) {
				current_speed++;
			}
		}
		if (current_speed >= SPEED_MAX) {
			current_speed = SPEED_MAX;
			current_state = FAST_PLAY_BACK;
			return current_state;
		}

        isPlaying = true;
		if (!isMuteON) {
			setStreamMute(true);// 静音
		}
        /**
         * 调jni执行快退
         */
        TimeShiftJniManager.getInstance().fastPlay(
                TimeShiftJniManager.PLAY_BACK);
    
//		new Thread() {
//
//			@Override
//			public void run() {
//				isPlaying = true;
//				/**
//				 * 调jni执行快退
//				 */
//				TimeShiftJniManager.getInstance().fastPlay(
//						TimeShiftJniManager.PLAY_BACK);
//			}
//
//		}.start();

		current_state = FAST_PLAY_BACK;
		return FAST_PLAY_BACK;
	}

	/**
	 * 播放
	 * 
	 * @return
	 */
	public synchronized int play(final String mainfreq, final String serviceid,
			OnPlayListener lis) {
		mMediaInfo = null;
		initRecordPos = false;
		current_state = getCurrent_state();
        Log.d(TAG, " play mainfreq = " + mainfreq + " serviceid = " + serviceid
                + " current_state = " + current_state + " isPlaying = " + isPlaying);
		this.isTimeShift = true;
		if (mainfreq == null || serviceid == null) {
			return PARA_ERROR;
		}
		if (isPlaying && current_state == PLAY) {
			current_speed = SPEED_0;
			current_state = PLAY;
			return PLAY;
		} else {
			current_speed = SPEED_0;
			current_state = PLAY;
			/**
			 * 调jni执行播放
			 */
			isPlaying = true;
			setOnPlayListener(lis);
			if (isMuteON) {
				setStreamMute(false);
			}
            // DvbController.switchDeinterlace(true);
            TimeShiftJniManager.getInstance().setOnMonitorListener(
                    TimeShiftController.this);
            TimeShiftJniManager.getInstance().timeShiftPlay(
                    getPlayUrl(mainfreq, serviceid));
        
//			new Thread() {
//
//				@Override
//				public void run() {
//					// DvbController.switchDeinterlace(true);
//					TimeShiftJniManager.getInstance().setOnMonitorListener(
//							TimeShiftController.this);
//					TimeShiftJniManager.getInstance().timeShiftPlay(
//							getPlayUrl(mainfreq, serviceid));
//				}
//
//			}.start();

			/**
			 * 测试代码
			 */
			// onMonitor(NotificationAction.PLAY_SUCCESS, null);
		}
		return PLAY;
	}

	public synchronized void play_look_back(final String url, OnPlayListener lis) {
		initRecordPos = false;
		mMediaInfo = null;
		current_speed = SPEED_0;
		current_state = PLAY;
		/**
		 * 调jni执行播放
		 */
		isPlaying = true;
		this.isTimeShift = false;// 回看
		setOnPlayListener(lis);

        // DvbController.switchDeinterlace(true);
        TimeShiftJniManager.getInstance().setOnMonitorListener(
                TimeShiftController.this);
        TimeShiftJniManager.getInstance().timeShiftPlay(url);
    
//		new Thread() {
//
//			@Override
//			public void run() {
//				// DvbController.switchDeinterlace(true);
//				TimeShiftJniManager.getInstance().setOnMonitorListener(
//						TimeShiftController.this);
//				TimeShiftJniManager.getInstance().timeShiftPlay(url);
//			}
//
//		}.start();
		Log.d(TAG, " play_look_back url = " + url + " mMediaInfo = "
				+ mMediaInfo);
	}

	public synchronized void play_next() {
		initRecordPos = false;
		mMediaInfo = null;
		current_speed = SPEED_0;
		current_state = PLAY;
		/**
		 * 调jni执行播放
		 */
		isPlaying = true;
		this.isTimeShift = true;// 时移

        TimeShiftJniManager.getInstance().playNext();
        Log.d(TAG, " play_next--------------");
//		new Thread() {
//
//			@Override
//			public void run() {
//				TimeShiftJniManager.getInstance().playNext();
//				Log.d(TAG, " play_next--------------");
//			}
//
//		}.start();

	}

	public synchronized void play_previous() {
		initRecordPos = false;
		mMediaInfo = null;
		current_speed = SPEED_0;
		current_state = PLAY;
		/**
		 * 调jni执行播放
		 */
		isPlaying = true;
		this.isTimeShift = true;// 时移

        TimeShiftJniManager.getInstance().playPrevious();
        Log.d(TAG, " play_previous--------------");
    
//		new Thread() {
//
//			@Override
//			public void run() {
//				TimeShiftJniManager.getInstance().playPrevious();
//				Log.d(TAG, " play_previous--------------");
//			}
//
//		}.start();

	}

    public synchronized void stop() {
		Log.d(TAG, " begin stop--------------");
		Long begintime = System.currentTimeMillis();
        isPlaying = false;
        current_state = STOP;
        current_speed = SPEED_0;
		if (isMuteON) {
			setStreamMute(false);
		}
        TimeShiftJniManager.getInstance().timeShiftStop();
		Log.d(TAG,
				" end stop--------------use time = "
						+ (System.currentTimeMillis() - begintime));
//        new Thread() {
//            @Override
//            public void run() {
//                
//            }
//        }.start();
    }

	/**
	 * 暂停
	 * 
	 * @return
	 */
	public synchronized int pause() {
		isPlaying = false;
		if (current_state == PAUSE) {
			current_speed = SPEED_0;
			current_state = PAUSE;
			return PAUSE;
		} else {
			current_speed = SPEED_0;
			current_state = PAUSE;
            /**
             * 调jni执行暂停
             */
            TimeShiftJniManager.getInstance().timeShiftPause();
            Log.d(TAG, " pause--------------");
//			new Thread() {
//
//				@Override
//				public void run() {
//					/**
//					 * 调jni执行暂停
//					 */
//					TimeShiftJniManager.getInstance().timeShiftPause();
//					Log.d(TAG, " pause--------------");
//				}
//
//			}.start();

		}
		return PAUSE;
	}

	public synchronized void play_or_pause() {
	    current_state = getCurrent_state();
	    Log.d(TAG, "  play_or_pause  current_state = " + current_state);
	    switch (current_state) {
            case PLAY:
                pause();
                break;
            case FAST_PLAY_BACK:
            case PAUSE:
            case FAST_PLAY_FORWARD:
                resume_play();
                break;
            default:
                break;
        }
	}

	public synchronized void resume_play() {
		current_speed = SPEED_0;
		current_state = PLAY;
		isPlaying = true;
		if (isMuteON) {
			setStreamMute(false);
		}
        /** 恢复播放 */
        TimeShiftJniManager.getInstance().timeShiftResumePlay();
        sendAddTimeMessage();
        Log.d(TAG, "------- resume_play -----");
//		new Thread() {
//
//			@Override
//			public void run() {
//				/** 恢复播放 */
//				TimeShiftJniManager.getInstance().timeShiftResumePlay();
//				sendAddTimeMessage();
//				Log.d(TAG, "------- resume_play -----");
//			}
//
//		}.start();
	}

	/**
	 * 跳转
	 * 
	 * @param position
	 *            跳转位置
	 */
	private void doSeekTo(String position) {
		needToSeekPosition = position;
		Log.d(TAG,
				" doSeekTo seekLastTimePosition="
						+ seekLastTimePosition
						+ " doSeekTo needToSeekPosition = "
						+ needToSeekPosition
						+ " transStringToTimeInMillis = "
						+ DateTools
								.transStringToTimeInMillis(needToSeekPosition)
						+ " mCurrentPlayTime = " + mCurrentPlayTime);
		/** 跳转 */
        TimeShiftJniManager.getInstance().timeShiftSeekTo(
                needToSeekPosition);
        mCurrentPlayTime = DateTools.transStringToTimeInMillis(needToSeekPosition);
        isPlaying = true;
        sendAddTimeMessage();
//		synchronized (lock) {
//			new Thread() {
//
//				@Override
//				public void run() {
//					/** 跳转 */
//					TimeShiftJniManager.getInstance().timeShiftSeekTo(
//							needToSeekPosition);
//					// sendAddTimeMessage();
//				}
//
//			}.start();
//
////            if (!needToSeekPosition.equals(seekLastTimePosition)) {
////                String tempPos = needToSeekPosition;
////                int flag = TimeShiftJniManager.getInstance().timeShiftSeekTo(needToSeekPosition);
////                if (flag == 0) {// 0是跳转成功
////                    seekLastTimePosition = tempPos;
////                    Log.d(TAG,
////                            "seekLastTimePosition=" + seekLastTimePosition
////                                    + "  needToSeekPosition=" + needToSeekPosition);
////                }
////            } else {
////                Log.d(TAG,
////                        "过滤------seekLastTimePosition=" + seekLastTimePosition
////                                + "  needToSeekPosition=" + needToSeekPosition);
////            }
//		}
	}

	private String json_str = "{begintime:'20100329140525',endtime:'20100329150529',curtime:'20100329150535',preprg:'zhoujunhua',nextprg:'xuxuelei',curprg:'leiyoufa'}";

	/**
	 * 获取节目信息
	 * 
	 * @return
	 */
    private void getMediaInfo() {
        // mediaInfo = json_str;
        // new Thread() {
        // @Override
        // public void run() {
        String mediaInfo = TimeShiftJniManager.getInstance().getMediaInfo();
        Log.d(TAG, " 11 getMediaInfo mediaInfo = " + mediaInfo);
        if (mediaInfo != null && !mediaInfo.equals("")) {
            try {
                Gson gson = new Gson();
                MediaInfo mi = gson.fromJson(mediaInfo, MediaInfo.class);
                Log.d(TAG, " getMediaInfo CurrentPlayPosition = "
                        + getCurrentPlayPositionHourMinuteSecond());
                if (mMediaInfo != null) {
                    Log.d(TAG,
                            " 22 getMediaInfo mMediaInfo = "
                                    + mMediaInfo.toString());
                }
                mMediaInfo = mi;
                if (mi != null) {
                    initTime(mi);
                    Log.d(TAG, " getMediaInfo CurrentPlayPosition = "
                            + getCurrentPlayPositionHourMinuteSecond());
                    sendAddTimeMessage();
                    if (mediaInfoUpdateListener != null) {
                        mediaInfoUpdateListener.onGetMediaInfo(mi);
                    }
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // }
        // }.start();
    }

	public MediaInfo getLocalMediaInfo() {
		Log.d(TAG,"getmMediaInfo   mMediaInfo = " + mMediaInfo);
		return mMediaInfo;
	}

	private void sendAddTimeMessage() {
	    Log.d(TAG, "  sendAddTimeMessage ---------- isPlaying = " + isPlaying);
		if (isPlaying) {
			Message msg = Message.obtain();
			msg.what = UPDATE_TIME;
			mHandler.removeMessages(UPDATE_TIME);
			mHandler.sendMessageDelayed(msg, DELAY_TIME);
			Log.d(TAG, "  sendAddTimeMessage ----------");
		}
	}

    private void removeTimeMessage() {
        if (!isPlaying) {
            Log.d(TAG, "  removeTimeMessage ----------");
            mHandler.removeMessages(UPDATE_TIME);
            // mHandler.sendMessageDelayed(msg, DELAY_TIME);
        }
    }

    /**
     * 获得当前倍速
     * 
     * @return
     */
    public int getCurrent_speed() {
        Log.d(TAG, " getCurrent_speed current_speed=" + current_speed);
        return current_speed * SPEED;
    }

	/**
	 * 获得当前的播放状态
	 * 
	 * @return
	 */
	public int getCurrent_state() {
		return getPlayState();
	}

	public String getCurrentStateName() {

		return null;
	}

	/**
	 * 获得当前录制进度
	 * 
	 * @return
	 */
	public int getCurrentRecordPosition() {
		long pos = 0;
		if (endTime - beginTime > 0) {
			pos = (mCurrentRecordTime - beginTime) * MAX_SEEKBAR_LENGTH
					/ (endTime - beginTime);
		}
		return (int) pos;
	}

	/**
	 * 以21:06的格式返回当前的录制进度
	 * 
	 * @return 21:06
	 */
	public String getCurrentRecordPositionHourMinute() {
		return DateTools.getHourMinute(mCurrentRecordTime);
	}

	/**
	 * 返回录制时间
	 * 
	 * @return
	 */
	public long getCurrentRecordTime() {
		return mCurrentRecordTime;
	}

	public String getCurrentRecordTimeString() {
		if (isTimeShift) {
			long tstime = getTimeFromTs();
			if (tstime > endTime) {
				mCurrentRecordTime = endTime;
			} else {
				mCurrentRecordTime = tstime;
			}
		}
		return DateTools.transTimeInMillisToString(mCurrentRecordTime);
	}

	/**
	 * 以21:06的格式返回当前的录制进度
	 * 
	 * @return 21:06
	 */
	public String getEndPositionHourMinute() {
		return DateTools.getHourMinute(endTime);
	}

    /**
     * 获得当前的播放进度
     * 
     * @return
     */
    public int getCurrentPlayPosition() {
        long pos = 0;
        if (endTime - beginTime > 0) {
            pos = (mCurrentPlayTime - beginTime) * MAX_SEEKBAR_LENGTH
                    / (endTime - beginTime);
            Log.d(TAG, " getCurrentPlayPosition pos = " + pos
                    + "  mCurrentPlayTime = " + mCurrentPlayTime
                    + "  beginTime = " + beginTime + "  endTime = " + endTime);
            if (mMediaInfo != null) {
                Log.d(TAG,
                        " getCurrentPlayPosition mMediaInfo = "
                                + mMediaInfo.toString());
            } else {
                Log.d(TAG, " getCurrentPlayPosition mMediaInfo = " + mMediaInfo);
            }
        }
        return Math.round(pos);
    }

    public boolean isSeekToBegin() {
        Log.d(TAG, " isSeekToBegin mCurrentPlayTime = " + mCurrentPlayTime
                + " beginTime = " + beginTime
                + " mCurrentPlayTime-beginTime = "
                + (mCurrentPlayTime - beginTime));
        if (mCurrentPlayTime - beginTime < 2000) {
            return true;
        }
        return false;
    }

	/**
	 * 以21:06的格式返回当前的播放进度
	 * 
	 * @return 21:06
	 */
	public String getCurrentPlayPositionHourMinute() {
		return DateTools.getHourMinute(mCurrentPlayTime);
	}

    /**
     * 以21:06:00的格式返回当前的播放进度
     * 
     * @return 21:06:00 时分秒
     */
    public String getCurrentPlayPositionHourMinuteSecond() {
        Log.d(TAG,
                " getCurrentPlayPositionHourMinuteSecond mCurrentPlayTime = "
                        + mCurrentPlayTime);
        return DateTools.getHourMinuteSecond(mCurrentPlayTime);
    }

	/**
	 * @param url
	 *            vod_url = "timeshift://mainfreq=682000,serviceid=100";
	 * @param service
	 * @return
	 */
	private String getPlayUrl(String mainfreq, String serviceid) {
		if (mainfreq == null || serviceid == null) {
			return null;
		}

		return String.format(vod_url, mainfreq, serviceid);// 正式用
		// String url=String.format(test_url, mainfreq,serviceid);
		// System.out.println("时移  url---->"+url);
		// return url;//测试用
	}

	public long getMinTime() {
		return 0;// 单位：秒
	}

	public long getMaxTime() {
		return 0;// 单位：秒
	}

	public static class NotificationAction {
		private static final int OPERATE = 0x0000FFFF;
		private static final int RESULT_FLAG = 0xFFFF0000;
		public static final int SUCCESS = 0;
		public static final int FAIL = -1;
		public static final short PLAY = 1;// 首次进入播放
		public static final short PLAY_FAIL = -1;
		public static final short PAUSE = 2;// 暂停
		public static final short REPLAY = 3;// 恢复播放
		public static final short SEEKTO = 6;// 跳转
		public static final short STOP = 7;// 停止
		public static final short FAST_PLAY = 8;// 快进/快退
		public static final short PGUP = 9;// 播放上一个
		public static final short PGDOWN = 10;// 播放下一个

		public static final short VOD_TO_END = 12;// 影片结束
		public static final short PLAY_BACK_BEGIN = 13;// 快退到头
		public static final short PLAY_TO_CURRENT_RECORD = 14;// 快进到录制点
		public static final short NET_ERROR = 15;// 网路错误、断开（操作中，土司）
		public static final short EXE_STOP = 16;// 需要客户端停止服务
		public static final short NET_DISCONNECTED = 17;// 心跳断开
		// public static final short VOD_TO_END=12;//影片结束
	}

	private boolean initRecordPos = false;

    private void initTime(MediaInfo mediaInfo) {
        mMediaInfo = mediaInfo;
        Log.d(TAG, " initTime mediaInfo = " + mediaInfo);
        if (mediaInfo == null) {
            return;
        }
        Log.d(TAG, " initTime mediaInfo = " + mediaInfo.toString());
        // if(!initRecordPos){
        //
        // mCurrentRecordTime=DateTools.transStringToTimeInMillis(mediaInfo.getCurtime());
        // initRecordPos=true;
        // System.out.println("beginTime="+beginTime+"  mediaInfo.getBegintime()="+mediaInfo.getBegintime());
        // }
        // mCurrentRecordTime=DateTools.transStringToTimeInMillis(mediaInfo.getCurtime());
        beginTime = DateTools.transStringToTimeInMillis(mediaInfo
                .getBegintime());
        endTime = DateTools.transStringToTimeInMillis(mediaInfo.getEndtime());
        mCurrentPlayTime = DateTools.transStringToTimeInMillis(mediaInfo
                .getCurtime());

        if (isTimeShift) {
            long tstime = getTimeFromTs();
            if (tstime > endTime) {
                mCurrentRecordTime = endTime;
            } else {
                mCurrentRecordTime = tstime;
            }
        }
    }

	private long getTimeFromTs() {
		String tsTime = SettingManager.getSettingManager()
				.nativeGetTimeFromTs();
		String[] strings = tsTime.split(":");
		long utcTime = Long.valueOf(strings[0]) * 1000;
		utcTime = System.currentTimeMillis();
		Log.d(TAG, " getTimeFromTs tsTime = " + tsTime + " utcTime = "
				+ utcTime);
		return utcTime;
	}

	public boolean isCanSeekTo(String day_hour_minute_second) {
		long time = getSeekToTime(day_hour_minute_second);
		if (isTimeShift) {
			if (time >= beginTime && time <= mCurrentRecordTime) {
				return true;
			}
		} else {
			if (time >= beginTime && time <= endTime) {
				return true;
			}
		}
		return false;
	}

	public boolean haveNext() {
		if (mCurrentRecordTime < endTime) {
			return false;
		}
		return true;
	}

	public long getSeekToTime(String day_hour_minute_second) {
		if (mMediaInfo == null || day_hour_minute_second == null) {
			return -1;
		}
		String beginTimeString = mMediaInfo.getBegintime();
		if (beginTimeString != null) {
			beginTime = DateTools.transStringToTimeInMillis(mMediaInfo
					.getBegintime());
			endTime = DateTools.transStringToTimeInMillis(mMediaInfo
					.getEndtime());
			String seekToTime = beginTimeString.substring(0, LEN
					- day_hour_minute_second.length())
					+ day_hour_minute_second;
			long time = DateTools.transStringToTimeInMillis(seekToTime);
			// mCurrentRecordPos=time;
			return time;
		}
		return -1;
	}

	/*
	 * public void seekTo(String hour_minute_second){ if(mMediaInfo==null ||
	 * hour_minute_second==null){ return ; } String
	 * beginTimeString=mMediaInfo.getBegintime(); if(beginTimeString!=null){
	 * String seekToTime=beginTimeString.substring(0, 8)+hour_minute_second;
	 * doSeekTo(seekToTime); } }
	 */
	public static final int LEN = 14;// 20130517091011共14位

    public void seekTo(String day_hour_minute_second) {
        if (mMediaInfo == null || day_hour_minute_second == null) {
            return;
        }
        String beginTimeString = mMediaInfo.getBegintime();
        if (beginTimeString != null) {
            String seekToTime = beginTimeString.substring(0, LEN
                    - day_hour_minute_second.length())
                    + day_hour_minute_second;
            doSeekTo(seekToTime);
        }
    }

    public void seekTo(int pos) {
            Log.d(TAG, " seekTo pos = " + pos + "  pos/1000=" + pos
                    / MAX_SEEKBAR_LENGTH);
        if (pos > 0) {
            long time = pos * (endTime - beginTime) / MAX_SEEKBAR_LENGTH
                    + beginTime;
            String seekToTime = DateTools.transTimeInMillisToString(time);
            doSeekTo(seekToTime);
        } else if (pos == 0) {
            // String seekToTime=DateTools.transTimeInMillisToString(beginTime);
            if (mMediaInfo != null) {
                doSeekTo(mMediaInfo.getBegintime());
            }
        }
    }

    private void updateCurrentRecordTime() {
    	Log.d(TAG, " updateCurrentRecordTime isTimeShift = " + isTimeShift
				+ " endTime = " + endTime + " mCurrentRecordTime = "
				+ mCurrentRecordTime);
        if (isTimeShift) {
            if (mCurrentRecordTime < endTime - DELAY_TIME) {
                mCurrentRecordTime = mCurrentRecordTime + DELAY_TIME;
            } else {
                mCurrentRecordTime = endTime;
            }
        } else {
            mCurrentRecordTime = endTime;
        }
		Log.d(TAG, " endTime = " + endTime + " mCurrentRecordTime = "
				+ mCurrentRecordTime);
    }

    private void updateCurrentPlayTime() {
    	Log.d(TAG, " updateCurrentPlayTime isTimeShift = " + isTimeShift
				+ " endTime = " + endTime + " mCurrentRecordTime = "
				+ mCurrentRecordTime + " mCurrentPlayTime = " + mCurrentPlayTime);
        if (isTimeShift) {
            if (mCurrentPlayTime < mCurrentRecordTime - DELAY_TIME) {
                mCurrentPlayTime = mCurrentPlayTime + DELAY_TIME;
            } else {
                mCurrentPlayTime = mCurrentRecordTime;
            }
        } else {
            if (mCurrentPlayTime < endTime - DELAY_TIME) {
                mCurrentPlayTime = mCurrentPlayTime + DELAY_TIME;
                Log.d(TAG, "updateCurrentPlayTime 111111111111111");
            } else {
                Log.d(TAG, "updateCurrentPlayTime 222222222222222");
                mCurrentPlayTime = endTime;
            }
            String test = DateTools.transTimeInMillisToString(mCurrentPlayTime);
            if (mMediaInfo != null) {
                Log.d(TAG, " updateCurrentPlayTime mCurrentPlayTime = " + mCurrentPlayTime
                        + " mMediaInfo = " + mMediaInfo.toString());
            } else {
                Log.d(TAG, " Error updateCurrentPlayTime " + " mMediaInfo = "
                        + mMediaInfo);
            }
        }
        Log.d(TAG, " endTime = " + endTime + " mCurrentRecordTime = "
				+ mCurrentRecordTime + " mCurrentPlayTime = " + mCurrentPlayTime);
    }

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    if (isPlaying) {
                        Log.d(TAG, " handleMessage UPDATE_TIME current_state = " + current_state);
                        if (current_state == PLAY) {
                            updateCurrentRecordTime();
                            updateCurrentPlayTime();
                        } else if (current_state == PAUSE) {
                            ;
                        } else if (current_state == FAST_PLAY_BACK) {
                            // mCurrentRecordTime=mCurrentRecordTime+DELAY_TIME;
                            updateCurrentRecordTime();
                            long de = (long) (DELAY_TIME * current_speed * SPEED);
                            if (mCurrentPlayTime - de <= beginTime) {
                                mCurrentPlayTime = beginTime;
                            } else {
                                mCurrentPlayTime = mCurrentPlayTime - de;
                            }
                        } else if (current_state == FAST_PLAY_FORWARD) {
                            updateCurrentRecordTime();
                            // mCurrentRecordTime=mCurrentRecordTime+DELAY_TIME;
                            long de = (long) (DELAY_TIME * current_speed * SPEED);
                            Log.d(TAG, " handleMessage de/1000--------------->" + (de / 1000));
                            if (isTimeShift) {
                                if (mCurrentPlayTime + de >= mCurrentRecordTime) {
                                    mCurrentPlayTime = mCurrentRecordTime;
                                } else {
                                    mCurrentPlayTime = mCurrentPlayTime + de;
                                }
                            } else {
                                if (mCurrentPlayTime + de >= endTime) {
                                    mCurrentPlayTime = endTime;
                                } else {
                                    mCurrentPlayTime = mCurrentPlayTime + de;
                                }
                                Log.d(TAG, " handleMessage FAST_PLAY_FORWARD   mCurrentPlayTime= "
                                        + mCurrentPlayTime + "  endTime= "
                                        + endTime + "  de= " + de);
                            }
                        }
                        sendAddTimeMessage();
                        Log.d(TAG, " handleMessage current_state= " + current_state);
                    } else {
                        removeTimeMessage();
                    }
                    break;
                case UPDATE_MEDIA:
                    getMediaInfo();
                    break;
            }
        }

    };

    private static final int UPDATE_TIME = 1;
    private static final int UPDATE_MEDIA = 2;

    public interface OnPlayListener {
        void onMonitorTimeShift(int operate, int result);
    }

    @Override
    public void onMonitor(final int monitorType, final Object message) {
        Log.d(TAG, "monitorType=" + monitorType + "   message=" + message);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                
//            }
//        });
        doMonitor(monitorType, message);
    }
        });
    }

    private void doMonitor(int monitorType, Object message) {
        int msg = (Integer) message;
        int operate = msg & TimeShiftController.NotificationAction.OPERATE;
        int result_flag = msg >> 16;
        switch ((int) operate) {
            case TimeShiftController.NotificationAction.PLAY:
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                if (result_flag == TimeShiftController.NotificationAction.SUCCESS) {
                    Log.d(TAG, " System.currentTimeMillis() = " + System.currentTimeMillis());
                    mHandler.sendEmptyMessage(UPDATE_MEDIA);
                    // getMediaInfo();
                    if (mMediaInfo != null) {
                        Log.d(TAG,
                                "play success  mMediaInfo = "
                                        + mMediaInfo.toString());
                    } else {
                        Log.d(TAG, "play success  mMediaInfo = " + mMediaInfo);
                    }
                } else if (result_flag == TimeShiftController.NotificationAction.FAIL) {
                    Log.d(TAG, "play fail");
                    // 通知上层播放失败
                }
                break;
            case TimeShiftController.NotificationAction.PGUP:
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                if (result_flag == TimeShiftController.NotificationAction.SUCCESS) {
                    // getMediaInfo();
                    mHandler.sendEmptyMessage(UPDATE_MEDIA);
                } else if (result_flag == TimeShiftController.NotificationAction.FAIL) {
                    // 通知上层播放失败
                }
                break;
            case TimeShiftController.NotificationAction.PGDOWN:
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                if (result_flag == TimeShiftController.NotificationAction.SUCCESS) {
                    // getMediaInfo();
                    mHandler.sendEmptyMessage(UPDATE_MEDIA);
                } else if (result_flag == TimeShiftController.NotificationAction.FAIL) {
                    // 通知上层播放失败
                }
                break;
            case TimeShiftController.NotificationAction.REPLAY:
                // getMediaInfo();
//                mHandler.sendEmptyMessage(UPDATE_MEDIA);
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                break;
            case TimeShiftController.NotificationAction.FAST_PLAY:
                // getMediaInfo();
//                mHandler.sendEmptyMessage(UPDATE_MEDIA);
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                current_speed = result_flag;
                if (current_speed > 0) {// 快进
                    current_state = FAST_PLAY_FORWARD;
                } else if (current_speed < 0) {// 快退
                    current_state = FAST_PLAY_BACK;
                    current_speed = Math.abs(current_speed);
                } else {
                    // 操作失败，界面处理
                    pause();
                }
                break;
            case TimeShiftController.NotificationAction.SEEKTO:
                // getMediaInfo();
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                Log.d(TAG, " current_state = " + current_state);
                if (result_flag == 0) {// 跳转成功
                    // getMediaInfo();
//                    mHandler.sendEmptyMessage(UPDATE_MEDIA);
                }
                break;
            case TimeShiftController.NotificationAction.VOD_TO_END:// 影片结束
                current_state = TimeShiftJniManager.getInstance().getPlayState();
                if (isTimeShift) {
                    mCurrentPlayTime = endTime;
                    // pause();
                    // play_next();//自动播放下一个
                }
                break;
            case TimeShiftController.NotificationAction.PLAY_TO_CURRENT_RECORD:// 快进到录制点
                current_state = PLAY;// 转正常播放
                current_speed = SPEED_0;
                resume_play();
                break;
            case TimeShiftController.NotificationAction.PLAY_BACK_BEGIN:
                if (!isTimeShift) {// 如果是回看转正常播放，如果是时移，交给上层处理
                    current_state = PLAY;// 转正常播放
                    current_speed = SPEED_0;
                    resume_play();
                } else {
                    mCurrentPlayTime = beginTime;
                    pause();
                }
                break;
        }
        Log.d(TAG, " doMonitor operate = " + operate + "   result_flag = "
                + result_flag + " msg = " + msg + " current_state = " + current_state);
        if (mOnPlayListener != null) {
            mOnPlayListener.onMonitorTimeShift(operate, result_flag);
        }
    }

    private MediaInfoUpdateListener mediaInfoUpdateListener;

    public void setMediaInfoUpdateListener(
            MediaInfoUpdateListener mediaInfoUpdateListener) {
        this.mediaInfoUpdateListener = mediaInfoUpdateListener;
    }

    public void removeMediaInfoUpdateListener() {
        this.mediaInfoUpdateListener = null;
    }

    public interface MediaInfoUpdateListener {
        void onGetMediaInfo(MediaInfo mediaInfo);
    }
    public void initAudioManager( Context context) {
    	mAudioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
//    	mAudioManager = new AudioManager(context);
    	isMuteON = false;
	}
	public void setStreamMute(boolean isOpen) {
		isMuteON = isOpen;
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isOpen);
		Log.d(TAG, " setStreamMute isOpen = " + isOpen);
	}
}
