package com.joysee.adtv.ui;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateTools;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.controller.TimeShiftController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.MediaInfo;
import java.util.Calendar;

public class PlayControlPopupWindow extends PopupWindow implements IDvbBaseView {
	private static final String TAG = PlayControlPopupWindow.class
			.getCanonicalName();
	private static final int DAY = 0;
	private static final int HOUR = 1;
	private static final int MINUTE = 2;
	private static final int SECOND = 3;
	private static final int DISPLAY_TIME = 5000 * 1 ;
	private static final int JUMP_VIEW_DISPLAY_TIME = 10000;
	private static final int UPDATE_PROGRESS_TIME = 2000;
	private static final int STEP_LENTH=10;
	private static final int DISMISS_POPUPWINDOW = 1;
	private static final int DISMISS_JUMP_VIEW = 2;
	private static final int MSG_UPDATE_PROGRESS = 3;
	private static final int DOUBLE_CLICK=2;
	private static final int CLICK=1;
	private int mLeftKeyClickCount=0;
	private int mRightKeyClickCount=0;
	private Activity mContext;
	private View contentView;
	private TextView channel_name_text;
	private TextView program_name_text;
	private TextView play_state_text;
	private TextView current_time_text;
	private TextView time_range_textview;
	private EditText day_et;
	private EditText hour_et;
	private EditText minute_et;
	private EditText second_et;
	public SeekBar mSeekBar;
	private View jump_view;
	private RelativeLayout control_layout;
	private ImageView play_pause_img;
	private ImageView fast_back_head_img;
	private ImageView fast_forward_last_img;
	private ImageView fast_back_img;
	private ImageView fast_forward_img;
	private ImageView look_back_btn;
	private float startY=0;
	private float endY=-10;
	private float startAlpha=0;
	private float endAlpha=1;
	private boolean isJumpViewShow = false;
	private boolean isJumpViewShowing = false;
	private boolean isJumpViewHiding = false;
	private EditText[] edit_texts = new EditText[4];
	private int index = 4;
	private boolean mIsTimeShift=true;
	private TimeShiftController mTimeShiftController;
	private DvbService service;
//	private ViewController mViewController;
	private boolean isKeyRepeating = false;
	private final int mKeyRepeatInterval = 150;
	private long mLastKeyDownTime = -1;
	private long mLastKeyUpTime = -1;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
		    Log.d(TAG, " handleMessage " + msg.toString());
			switch (msg.what) {
			case DISMISS_POPUPWINDOW:
				System.out.println("Current_state()="+TimeShiftController.getInstance().getCurrent_state());
				if (!isJumpViewShow && TimeShiftController.getInstance().getCurrent_state()==TimeShiftController.PLAY) {
					hide();
				}else{
					handler.removeMessages(DISMISS_POPUPWINDOW);
					addMessage(DISMISS_POPUPWINDOW, DISPLAY_TIME);
				}
				break;
			case DISMISS_JUMP_VIEW:
				if (isJumpViewShow) {
					hideJumpView(200);
				}
				break;
			case MSG_UPDATE_PROGRESS:
				if (isShowing()) {
					mMediaInfo=mTimeShiftController.getLocalMediaInfo();
					if(mMediaInfo!=null){
						Log.d(TAG, " MSG_UPDATE_PROGRESS mMediaInfo " + mMediaInfo.toString());
						updateView(mMediaInfo);
					}
					addMessage(MSG_UPDATE_PROGRESS, UPDATE_PROGRESS_TIME);//更新进度显示
				} else {
					handler.removeMessages(MSG_UPDATE_PROGRESS);
				}
				break;
			}
		
		}
		
	};
	private MediaInfo mMediaInfo;

    public void setMediaInfo(MediaInfo mediaInfo) {
        Log.d(TAG, " setMediaInfo mediaInfo = " + mediaInfo);
        mMediaInfo = mediaInfo;
        updateView(mediaInfo);
    }
	
	public interface CallBackInterface{
		public static final int SHOW_SELECT_DIALOG=12;
		public static final int SHOW_PLAY_BEGIN_TIME_DIALOG=13;
		public void callBack(int flag);
	}
	private CallBackInterface mCallBackInterface=null;
	public PlayControlPopupWindow(final Activity a, final int width,
			final int height,boolean isTimeShift,CallBackInterface callBackInterface) {
		super();
		mContext =  a;
		mIsTimeShift=isTimeShift;
		mCallBackInterface=callBackInterface;
		mTimeShiftController = TimeShiftController.getInstance();
		LayoutInflater inflater = (LayoutInflater) a
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.timeshift_layout, null);
		contentView.setFocusable(true);
		contentView.setFocusableInTouchMode(true);
		setContentView(contentView);
		this.setWidth(width);
		this.setHeight(height);
		this.setFocusable(false);
		this.setTouchable(false);
		findViews();
		initView();
		try {
			setBackgroundDrawable(new BitmapDrawable());
		} catch (Exception e) {

		}
		contentView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "contentView---keyCode=  " + keyCode + "  KeyEvent="
						+ event.getAction());
				return false;
			}
		});
	}
	
	private void doGuize(int min, int max, boolean isAdd) {
		String dec = edit_texts[index].getText().toString();
		if (dec != null && !dec.equals("")) {
			int num = Integer.parseInt(dec);
			if (isAdd) {
				if (num == max) {
					edit_texts[index].setText(min + "");
				} else {
					num++;
					edit_texts[index].setText(num + "");
				}
			} else {
				if (num == min) {
					edit_texts[index].setText(max + "");
				} else {
					num--;
					edit_texts[index].setText(num + "");
				}
			}
		}

	}

	private void addOrDecNum(boolean isAdd) {
		if (index == DAY) {// 时的规则1-31
			doGuize(1, 31, isAdd);
		}else if (index == HOUR) {// 时的规则0-23
			doGuize(0, 23, isAdd);
		} else if (index == MINUTE) {// 分的规则0-59
			doGuize(0, 59, isAdd);
		} else if (index == SECOND) {// 秒的规则0-59
			doGuize(0, 59, isAdd);
		}
	}

	public void dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG,"  dispatchKeyEvent  "+event.getKeyCode() + " isJumpViewShow = " + isJumpViewShow);
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK 
				|| event.getKeyCode()==KeyEvent.KEYCODE_ESCAPE
				|| event.getKeyCode()==KeyEvent.KEYCODE_HOME){
			removeMessage(MSG_UPDATE_PROGRESS);
			removeMessage(DISMISS_POPUPWINDOW);
			removeMessage(DISMISS_JUMP_VIEW);
			this.dismiss();
		}
		if (isJumpViewShow) {// 如果是跳转窗口
			dispatchKeyEventToJumpView(event);
		} else {
			dispatchKeyEventToControlView(event);
		}

	}
	
	public void disMissJumpView(int time){
		addMessage(DISMISS_JUMP_VIEW, time);
	}
	
	private void dispatchKeyEventToJumpView(KeyEvent event){
		addMessage(DISMISS_JUMP_VIEW, JUMP_VIEW_DISPLAY_TIME);
		jump_view.dispatchKeyEvent(event);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				addOrDecNum(false);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				addOrDecNum(true);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (index > 1) {
					index--;
				} else {
					index = edit_texts.length - 1;
				}
				edit_texts[index].requestFocus();
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (index < edit_texts.length - 1) {
					index++;
				} else {
					index = 1;
				}
				edit_texts[index].requestFocus();
				break;
			}

		}else if(event.getAction() == KeyEvent.ACTION_UP){
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_PROG_GREEN:
			case KeyEvent.KEYCODE_F2:
				System.out.println("isJumpViewHiding---------->"+isJumpViewHiding);
				if(!isJumpViewHiding){
					hideJumpView(200);
				}else{
					System.out.println("ddddddddddd");
					
				}
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				seekTo();
				break;
			}
		}
	}
	
	private void dispatchKeyEventToControlView(KeyEvent event){
//		System.out.println("event.getKeyCode()=========="+event.getKeyCode());
		Log.d(TAG, " dispatchKeyEventToControlView " + event.toString() + " isKeyRepeating = " + isKeyRepeating );
		Log.d(TAG," getRepeatCount = " + event.getRepeatCount() + " isLongPress = " + event.isLongPress());
		removeMessage(DISMISS_POPUPWINDOW);
		addMessage(DISMISS_POPUPWINDOW, DISPLAY_TIME);
		final long currenKeyDownTime;
		final long interval;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
				fast_forward_last_img.setBackgroundResource(R.drawable.fast_forward_last_focus);
				if(mIsTimeShift && mCallBackInterface!=null){
					mCallBackInterface.callBack(CallBackInterface.SHOW_SELECT_DIALOG);
				}
				Log.d(TAG, "up");
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				Log.d(TAG, "down");
				fast_back_head_img.setBackgroundResource(R.drawable.fast_back_head_focus);
				if(mIsTimeShift && mCallBackInterface!=null){
					mCallBackInterface.callBack(CallBackInterface.SHOW_PLAY_BEGIN_TIME_DIALOG);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				currenKeyDownTime = SystemClock.uptimeMillis();
				interval = currenKeyDownTime - mLastKeyDownTime;
				if (isKeyRepeating && interval < mKeyRepeatInterval) {
					break;
				}
				mLastKeyDownTime = currenKeyDownTime;
				if (event.getRepeatCount() > 0) {
					removeMessage(MSG_UPDATE_PROGRESS);
					isKeyRepeating = true;
				}
				fast_back_img.setBackgroundResource(R.drawable.fast_back_focus);
				if (isKeyRepeating) {
					if(mSeekBar.getProgress() > STEP_LENTH){
						mSeekBar.setProgress(mSeekBar.getProgress() - STEP_LENTH);
					}else{
						mSeekBar.setProgress(mSeekBar.getProgress());
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				currenKeyDownTime = SystemClock.uptimeMillis();
				interval = currenKeyDownTime - mLastKeyDownTime;
				if (isKeyRepeating && interval < mKeyRepeatInterval) {
					break;
				}
				mLastKeyDownTime = currenKeyDownTime;
				if (event.getRepeatCount() > 0) {
					removeMessage(MSG_UPDATE_PROGRESS);
					isKeyRepeating = true;
				}
                    fast_forward_img
                            .setBackgroundResource(R.drawable.fast_forward_focus);
                    if (mIsTimeShift) {
                        if (isKeyRepeating) {
                            if (mSeekBar.getProgress() + STEP_LENTH < mSeekBar.getSecondaryProgress()) {
                                mSeekBar.setProgress(mSeekBar.getProgress() + STEP_LENTH);
                            } else {
                                mSeekBar.setProgress(mSeekBar.getSecondaryProgress());
                            }
                        }
                    } else {
                        if (isKeyRepeating) {
                            if (mSeekBar.getProgress() + STEP_LENTH < TimeShiftController.MAX_SEEKBAR_LENGTH) {
                                mSeekBar.setProgress(mSeekBar.getProgress() + STEP_LENTH);
                            } else {
                                mSeekBar.setProgress(TimeShiftController.MAX_SEEKBAR_LENGTH);// 进度从0开始，进入下一个时段
                            }
                        }
                    }
                    Log.d(TAG,
                            " dispatchKeyEventToControlView "
                                    + " mSeekBar.getProgress() = "
                                    + mSeekBar.getProgress()
                                    + " mSeekBar.getSecondaryProgress() = "
                                    + mSeekBar.getSecondaryProgress()
                                    + " mIsTimeShift = " + mIsTimeShift);
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				play_pause_img.setBackgroundResource(R.drawable.play_pause_focus);
				break;
			}
		}else if(event.getAction() == KeyEvent.ACTION_UP){
            final long currenKeyUpTime;
            final long duringTime;
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_UP:
				fast_forward_last_img.setBackgroundResource(R.drawable.fast_forward_last);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				fast_back_head_img.setBackgroundResource(R.drawable.fast_back_head);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				fast_back_img.setBackgroundResource(R.drawable.fast_back);
				if (isKeyRepeating) {// long press
					// 执行seekto
					mTimeShiftController.seekTo(mSeekBar.getProgress());
				} else {// 单击 if (mLeftKeyClickCount == CLICK) 
					removeMessage(MSG_UPDATE_PROGRESS);
					mTimeShiftController.fastPlayBack();// 执行快退
					// updateState();//更新状态显示
					addMessage(MSG_UPDATE_PROGRESS, 500);// 更新进度显示
				}
				isKeyRepeating = false;
//				mLeftKeyClickCount = 0;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
//				long interval_right=SystemClock.uptimeMillis()-onRightKeyDownTime;
				fast_forward_img.setBackgroundResource(R.drawable.fast_forward);
				if(isKeyRepeating){//long press
					//执行seekto
					mTimeShiftController.seekTo(mSeekBar.getProgress());
				}else {//单击if(mRightKeyClickCount==CLICK)
					removeMessage(MSG_UPDATE_PROGRESS);
					mTimeShiftController.fastPlayForward();// 执行快进
					// updateState();//更新状态显示
					addMessage(MSG_UPDATE_PROGRESS, 500);// 更新进度显示
				}
//				mRightKeyClickCount = 0;
				isKeyRepeating = false;
				break;
			case KeyEvent.KEYCODE_PROG_GREEN:
			case KeyEvent.KEYCODE_F2:
				Log.d(TAG, "onkeyup isJumpViewShowing="+isJumpViewShowing);
				if(!isJumpViewShowing){
					initEditText();
					showJumpView(300);
				}else{
				}
				break;
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_ESCAPE:
				this.dismiss();
				break;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
                currenKeyUpTime = SystemClock.uptimeMillis();
                duringTime = currenKeyUpTime - mLastKeyUpTime;
                if (duringTime < mKeyRepeatInterval) {
                    break;
                }
                mLastKeyUpTime = currenKeyUpTime;
				play_pause_img.setBackgroundResource(R.drawable.play_pause);
				// 暂停/播放
				mTimeShiftController.play_or_pause();
				updateState();
				break;
			}
		}
	}

	public void setSeekBar(int progress){
		if(mSeekBar!=null){
			mSeekBar.setProgress(progress);
		}
	}

    private void updateState() {
        int current_speed = mTimeShiftController.getCurrent_speed();
        int current_state = mTimeShiftController.getCurrent_state();
        Log.d(TAG, " updateState current_speed = " + current_speed
                + " current_state = " + current_state);
        String text = mContext.getResources().getString(R.string.play_state);
        if (current_state == TimeShiftController.PLAY) {
            String _str = mContext.getResources().getString(R.string.play);
            play_state_text.setText(text + _str);
        } else if (current_state == TimeShiftController.PAUSE) {
            String _str = mContext.getResources().getString(R.string.pause);
            play_state_text.setText(text + _str);
        } else if (current_state == TimeShiftController.FAST_PLAY_BACK) {
            String _str = mContext.getResources().getString(
                    R.string.play_back);
            String speed_str = mContext.getResources().getString(
                    R.string.speed);
            speed_str = String.format(speed_str, current_speed + "");
            if (current_speed == 0 || current_speed > 24) {
                play_state_text.setText("   ");
            }else{
                play_state_text.setText(text + _str + "   " + speed_str);
            }
        } else if (current_state == TimeShiftController.FAST_PLAY_FORWARD) {
            String _str = mContext.getResources().getString(
                    R.string.play_forward);
            String speed_str = mContext.getResources()
                    .getString(R.string.speed);
            speed_str = String.format(speed_str, current_speed + "");
            Log.d(TAG, text + _str + "   " + speed_str);
            if (current_speed == 0 || current_speed > 24) {
                play_state_text.setText("   ");
            }else{
                play_state_text.setText(text + _str + "   " + speed_str);
            }
        } else {
            /**
             * 其他情况不显示
             */
            play_state_text.setText("   ");
        }
    }

	private void seekTo() {
		int hour_int = 0;
		int minute_int = 0;
		int day_int = 1;
		// 判断天是否合法
		String day_str = day_et.getText().toString();
		if (day_str != null && !day_str.trim().equals("")) {
			day_int = Integer.parseInt(day_str);
			if (day_int >= 1 && day_int <= 31) {
				;
			} else {
				ToastUtil.showToast(mContext, R.string.jump_input_day_err);
				day_et.requestFocus();
				return;
			}
		} else {
			ToastUtil.showToast(mContext, R.string.jump_input_day);
			day_et.requestFocus();
			return;
		}
		// 判断小时是否合法
		String hour_str = hour_et.getText().toString();
		if (hour_str != null && !hour_str.trim().equals("")) {
			hour_int = Integer.parseInt(hour_str);
			if (hour_int >= 0 && hour_int < 24) {
				;
			} else {
				ToastUtil.showToast(mContext, R.string.jump_input_hour_err);
				hour_et.requestFocus();
				return;
			}
		} else {
			ToastUtil.showToast(mContext, R.string.jump_input_hour);
			hour_et.requestFocus();
			return;
		}
		// 判断分钟是否合法
		String minute_str = minute_et.getText().toString();
		if (minute_str != null && !minute_str.trim().equals("")) {
			minute_int = Integer.parseInt(minute_str);
			if (minute_int >= 0 && minute_int < 60) {

			} else {
				ToastUtil.showToast(mContext,
						R.string.jump_input_minute_err);
				minute_et.requestFocus();
				return;
			}
		} else {
			ToastUtil.showToast(mContext, R.string.jump_input_minute);
			minute_et.requestFocus();
			return;
		}
		// 判断秒是否合法
		String second_str = second_et.getText().toString();
		if (second_str != null && !second_str.trim().equals("")) {
			int second_int = Integer.parseInt(second_str);
			if (second_int >= 0 && second_int < 60) {
//				long time = hour_int * 60 * 60 + minute_int * 60 + second_int;
				int h=Integer.parseInt(hour_str);
				int m=Integer.parseInt(minute_str);
				int s=Integer.parseInt(second_str);
				if(day_int<10 && day_str.length()==1){
					day_str="0"+day_str;
				}
				if(h<10 && hour_str.length()==1){
					hour_str="0"+hour_str;
				}
				if(m<10 && minute_str.length()==1){
					minute_str="0"+minute_str;
				}
				if(s<10 && second_str.length()==1){
					second_str="0"+second_str;
				}
				String seekToPos=day_str+hour_str+minute_str+second_str;
				if (mTimeShiftController.isCanSeekTo(seekToPos)) {
					// 开始跳转
					mTimeShiftController.seekTo(seekToPos);
					return;
				} else {
					ToastUtil.showToast(mContext, R.string.jump_input_err);
					return;
				}
			} else {
				ToastUtil.showToast(mContext,
						R.string.jump_input_second_err);
				second_et.requestFocus();
				return;
			}
		} else {
			ToastUtil.showToast(mContext, R.string.jump_input_second);
			second_et.requestFocus();
			return;
		}
	}

	private void findViews() {
		play_pause_img = (ImageView) contentView.findViewById(R.id.play_pause_img);
		fast_back_head_img = (ImageView) contentView.findViewById(R.id.fast_back_head_img);
		fast_forward_last_img = (ImageView) contentView.findViewById(R.id.fast_forward_last_img);
		fast_back_img = (ImageView) contentView.findViewById(R.id.fast_back_img);
		fast_forward_img = (ImageView) contentView.findViewById(R.id.fast_forward_img);
		look_back_btn=(ImageView)contentView.findViewById(R.id.look_back_btn);
		
		channel_name_text = (TextView) contentView
				.findViewById(R.id.channel_name_text);
		program_name_text = (TextView) contentView
				.findViewById(R.id.program_name_text);
		play_state_text = (TextView) contentView
				.findViewById(R.id.play_state_text);
		current_time_text = (TextView) contentView
				.findViewById(R.id.current_time_text);
		time_range_textview = (TextView) contentView
				.findViewById(R.id.time_range_textview);
		jump_view = contentView.findViewById(R.id.jump_view);
//		root_view = (RelativeLayout) contentView.findViewById(R.id.root_view);
		control_layout = (RelativeLayout) contentView
				.findViewById(R.id.control_layout);
		mSeekBar = (SeekBar) contentView.findViewById(R.id.seekBar);

		day_et = (EditText) contentView.findViewById(R.id.day_et);
		hour_et = (EditText) contentView.findViewById(R.id.hour_et);
		minute_et = (EditText) contentView.findViewById(R.id.minute_et);
		second_et = (EditText) contentView.findViewById(R.id.second_et);
		edit_texts[DAY] = day_et;
		edit_texts[HOUR] = hour_et;
		edit_texts[MINUTE] = minute_et;
		edit_texts[SECOND] = second_et;

		day_et.addTextChangedListener(new DVBTextWatcher(day_et, 2));
		hour_et.addTextChangedListener(new DVBTextWatcher(hour_et, 2));
		minute_et.addTextChangedListener(new DVBTextWatcher(minute_et, 2));
		second_et.addTextChangedListener(new DVBTextWatcher(second_et, 2));

		if(mIsTimeShift){
			fast_back_head_img.setVisibility(View.VISIBLE);
			fast_forward_last_img.setVisibility(View.VISIBLE);
		}else{
			fast_back_head_img.setVisibility(View.INVISIBLE);
			fast_forward_last_img.setVisibility(View.INVISIBLE);
		}
		// jump_view.setAlpha(0.0f);
		// jump_view.setVisibility(View.GONE);
	}

	private class DVBTextWatcher implements TextWatcher {
		String st = "";
		private EditText mEditText;
		private int length;

		public DVBTextWatcher(EditText edittext, int len) {
			mEditText = edittext;
			length = len;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {

			int start = mEditText.getSelectionStart();
//			int end = mEditText.getSelectionEnd();
			if (s != null && s.length() > length) {
				st = "";
				if (start - 1 >= 0) {
					String ss = s.charAt(start - 1) + "";
					st = st + ss;
					mEditText.setText(st);
					// s.clear();
				} else {
					String ss = s.charAt(s.length() - 1) + "";
					st = st + ss;
					mEditText.setText(st);
				}

			} else {
				if (start - 1 >= 0) {
					String ss = s.charAt(start - 1) + "";
					st = st + ss;
					mEditText.setText(st);

				}
			}

		}

	}

	/**
	 * 动画
	 */
	private void showJumpView(long duration) {
		// jump_view.setVisibility(View.VISIBLE);
		Log.d(TAG,
				" showJumpView----control_layout.getY()="
						+ control_layout.getY() + "  startY=" + startY
						+ "  endY" + endY);
		index = MINUTE;
		edit_texts[index].requestFocus();
		isJumpViewShowing = true;
//		startY = 0;
//		endY = -10;
		AnimationSet as = new AnimationSet(false);
		Animation myAnimation_Translate = new TranslateAnimation(0.0f, 0.0f,
				0, -10);
		as.addAnimation(myAnimation_Translate);
//		Animation alpha = AnimationUtils.loadAnimation(playActivity,
//				R.anim.show_jump_view_alpha);
		Animation alpha=new AlphaAnimation(0, 1);
		as.addAnimation(alpha);
		as.setFillAfter(true);
		as.setDuration(duration);
		as.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				jump_view.setVisibility(View.VISIBLE);
//				startAlpha=0;
//				endAlpha=1;
//				startY = 0;
//				endY = -10;
//				Log.d(TAG, "*showJumpView**********onAnimationStart*********startAlpha="+startAlpha+"  endAlpha="+endAlpha+"   isJumpViewShow="+isJumpViewShow);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				isJumpViewShow = true;
				isJumpViewShowing = false;
				minute_et.requestFocus();
				addMessage(DISMISS_JUMP_VIEW, JUMP_VIEW_DISPLAY_TIME);
				Log.d(TAG,
						"*showJumpView**********onAnimationEnd*********startAlpha="
								+ startAlpha + "  endAlpha=" + endAlpha
								+ "   isJumpViewShow=" + isJumpViewShow);
			}
		});
		jump_view.setAnimation(as);
		jump_view.startAnimation(as);

	}

	private void hideJumpView(long duration) {
		Log.d(TAG, " hideJumpView ");
		isJumpViewHiding = true;

		AnimationSet as = new AnimationSet(false);
		Animation myAnimation_Translate = new TranslateAnimation(0.0f, 0.0f,
				-10, 0);
		as.addAnimation(myAnimation_Translate);
		// Animation alpha = AnimationUtils.loadAnimation(playActivity,
		// R.anim.hide_jump_view_alpha);
		// as.addAnimation(getAlhpaAnimation(duration));
		Animation alpha = new AlphaAnimation(1, 0);
		as.addAnimation(alpha);
		as.setFillAfter(true);
		as.setDuration(duration);
		as.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// startAlpha=1;
				// endAlpha=0;
				// startY = -10;
				// endY = 0;
				// Log.d(TAG,
				// "hideJumpView----onAnimationStart---------startY="+startY+"  endY="+endY+"  isJumpViewShow="+isJumpViewShow);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				isJumpViewShow = false;
				isJumpViewHiding = false;
				minute_et.requestFocus();
				Log.d(TAG,
						"hideJumpView---------onAnimationEnd-------isJumpViewShow="
								+ isJumpViewShow);
				addMessage(DISMISS_POPUPWINDOW, DISPLAY_TIME);
			}
		});
		jump_view.setAnimation(as);
		jump_view.startAnimation(as);
	}

	private void initView() {
		// final Calendar c = Calendar.getInstance();
		initEditText();
		mSeekBar.setMax(TimeShiftController.MAX_SEEKBAR_LENGTH);

		// if(!mIsTimeShift){
		// String s=mContext.getResources().getString(R.string.look_back);
		// look_back_btn.setText(s);
		// }else{
		// String s=mContext.getResources().getString(R.string.time_shift);
		// look_back_btn.setText(s);
		// }
	}

	private void initEditText() {
		MediaInfo m = mTimeShiftController.getInstance().getLocalMediaInfo();
		if (m != null) {
			String time = m.getBegintime();
			if (time != null && time.length() == 14) {
				day_et.setText(time.substring(6, 8));
				hour_et.setText(time.substring(8, 10));
				minute_et.setText(time.substring(10, 12));
				second_et.setText(time.substring(12, 14));
			}
		} else {
			final Calendar c = Calendar.getInstance();
			day_et.setText(c.get(Calendar.DAY_OF_MONTH) + "");
			hour_et.setText(c.get(Calendar.HOUR_OF_DAY) + "");
			minute_et.setText(c.get(Calendar.MINUTE) + "");
			second_et.setText(c.get(Calendar.SECOND) + "");
		}

	}

	private void updateChannelName() {
		// MiniEpgNotify info = getNETEventInfo();
		// if (info != null) {
		if (service != null && service.getChannelName() != null) {
			channel_name_text.setText(service.getChannelName());
		} else {
			channel_name_text.setText("");
		}
		// }
		addMessage(MSG_UPDATE_PROGRESS, UPDATE_PROGRESS_TIME);
	}

	private void updateView(MediaInfo mediaInfo) {
	    Log.d(TAG, " updateView mIsTimeShift = " + mIsTimeShift);
		updateState();// 更新状态显示
		updateTextView(mediaInfo);
		updateSeekBar();
		updateProgramName(mediaInfo);
	}

    private void updateTextView(MediaInfo mediaInfo) {
        Log.d(TAG, " updateTextView mediaInfo = " + mediaInfo);
        if (mediaInfo == null) {
            return;
        }
        String current_time = mTimeShiftController
                .getCurrentPlayPositionHourMinuteSecond();
        if (current_time != null) {
            current_time_text.setText(current_time);
        }
        // String
        // range=DateTools.formatTimeRange(mediaInfo.getBegintime(),mTimeShiftController.getCurrentRecordPositionHourMinute());
        String range = null;
        if (mIsTimeShift) {
            // range=DateTools.getStartTime(mediaInfo.getBegintime())+"-"+mTimeShiftController.getCurrentRecordPositionHourMinute();
            range = DateTools.getStartTime(mediaInfo.getBegintime())
                    + "-"
                    + DateTools.getStartTime(mTimeShiftController
                            .getCurrentRecordTimeString());
        } else {
            range = DateTools.getStartTime(mediaInfo.getBegintime()) + "-"
                    + DateTools.getStartTime(mediaInfo.getEndtime());
        }

        if (range != null && !range.equals("")) {
            time_range_textview.setText(range);
        }
    }

	private void updateSeekBar() {
		Log.d(TAG, " updateSeekBar getCurrentRecordPosition() = "
				+ mTimeShiftController.getCurrentRecordPosition());
		if (mIsTimeShift) {
			mSeekBar.setSecondaryProgress(mTimeShiftController
					.getCurrentRecordPosition());
		} else {
			mSeekBar.setSecondaryProgress(0);
		}
		mSeekBar.setProgress(mTimeShiftController.getCurrentPlayPosition());
		Log.d(TAG,
				" updateSeekBar CurrentRecordPosition()="
				+ mTimeShiftController.getCurrentRecordPosition()
				+ "  CurrentPlayPosition()="
				+ mTimeShiftController.getCurrentPlayPosition());
	}

    private void updateProgramName(MediaInfo mediaInfo) {
        Log.d(TAG, " updateProgramName mediaInfo = " + mediaInfo);
        if (mediaInfo == null) {
            return;
        }
        String ename = mediaInfo.getCurprg();
        if (ename != null) {
            if (ename.length() > 12) {
                program_name_text.setText(ename.substring(0, 12) + "...");
            } else {
                program_name_text.setText(ename);
            }
        } else {
            program_name_text.setText("");
        }
    }

	public void hide() {
		if (isShowing()) {
			dismiss();
		}
	}

	private boolean mIsDestory = false;
	
    public void isDestory(boolean yes) {
        Log.d(TAG, " isDestory mIsDestory = " + mIsDestory);
        mIsDestory = yes;
        if (mIsDestory) {
            dismiss();
            mMediaInfo = null;
//            mIsDestory = false;
        }
    }

    public void show() {
        Log.d(TAG, "  show isShowing() = " + isShowing() + " mIsDestory = " + mIsDestory);
        Log.d(TAG, " isJumpViewShow = " + isJumpViewShow + " isJumpViewShow = " + isJumpViewShow);
        if (isShowing()) {
            return;
        }
        jump_view.setVisibility(View.GONE);
        if (mIsDestory) {
            return;
        }
        updateChannelName();
        mMediaInfo = TimeShiftController.getInstance().getLocalMediaInfo();
        if (mMediaInfo != null) {
            updateView(mMediaInfo);
            Log.d(TAG, "not    null--------------"
                    + mMediaInfo.getPreprg() + "  "
                    + mMediaInfo.getBegintime());
        } else {
            Log.d(TAG, "null--------------");
        }
        this.showAtLocation(mContext.getWindow().getDecorView(), Gravity.BOTTOM
                | Gravity.CENTER_HORIZONTAL, 0, 0);
        Message msg = handler.obtainMessage(DISMISS_POPUPWINDOW);
        handler.removeMessages(DISMISS_POPUPWINDOW);
        handler.sendMessageDelayed(msg, DISPLAY_TIME);
    }

	private void addMessage(int what, int timeout) {
		Message msg = handler.obtainMessage(what);
		msg.what = what;
		handler.removeMessages(what);
		handler.sendMessageDelayed(msg, timeout);
	}

	private void removeMessage(int what) {
		Message msg = handler.obtainMessage(what);
		msg.what = what;
		handler.removeMessages(what);
	}

	// @Override
	// public void processMessage(Object sender, DvbMessage msg) {
	// if (msg.what == ViewMessage.SHOW_TIME_SHIFT_POPUWINDOW) {
	// mViewController = (ViewController) sender;
	// if(mViewController!=null){
	// //播放时移
	// DvbService service=mViewController.getCurrentChannel();
	// if(service!=null){
	// mTimeShiftController.play(service.getFrequency()+"",service.getServiceId()+"",this);
	// }
	// }
	// show();
	// }
	//
	// }

	// private MiniEpgNotify getNETEventInfo() {
	// if (mViewController != null) {
	// service = mViewController.getCurrentChannel();
	// MiniEpgNotify miniEpg = mViewController.getPfFromEPG(service);
	// return miniEpg;
	// }
	//
	// return null;
	// }

	public void setService(DvbService service2) {
		service = service2;
	}

	@Override
	public void processMessage(Object sender, DvbMessage msg) {
		
	}
	
	
    @Override
    public void dismiss() {
        try {
            jump_view.setVisibility(View.GONE);
            isJumpViewShow = false;
            isJumpViewShow = false;
            removeMessage(MSG_UPDATE_PROGRESS);
            removeMessage(DISMISS_POPUPWINDOW);
            removeMessage(DISMISS_JUMP_VIEW);
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void resetView(){
        Log.d(TAG, " resetView  -----");
        channel_name_text.setText("");
        program_name_text.setText("");
        play_state_text.setText("");
        current_time_text.setText("");
        time_range_textview.setText("");
        mSeekBar.setProgress(0);
        mMediaInfo = null;
    }
}
