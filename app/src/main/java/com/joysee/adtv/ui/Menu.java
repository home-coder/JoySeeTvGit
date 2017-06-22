package com.joysee.adtv.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.controller.ViewController;

public class Menu implements IDvbBaseView  {
	private Activity mActivity;
	private Context mContext;
	private PopupWindow mPopupMenu;
	private View mMenuView;
	private ViewFlipper mMenuCotent;
	private LayoutInflater mInflater;
	private static final int MENU_PROGRAM_GUIDE = 0;
	private static final int MENU_CHANNEL_LIST = 1;
	private static final int MENU_SETTINGS = 2;
	private MenuProgramGuide mProgramGuideView;
	private MenuChannelList mChannelListView;
	private MenuSetting mSettingView;
	private MenuSettingSub mSettingSubView;
//	private MenuSystemInfo mMenuSysInfoView;
	private MenuChannelCategory mMenuChannelCategory;
	private int mIndex;
	private ViewController mController;
	private LinearLayout mMenuTitle;
	private ImageView mProgramGuideImg;
	private TextView  mProgramGuideTv;
	private ImageView mChannelListImg;
	private TextView  mChannelListTv;
	private ImageView mSettingsImg;
	private TextView  mSettingsTv;
	private int parentIndex;
	private TextView mSubTitle;
	private View mLastTitileFocusView;
	private ImageView[] mTitleBar = new ImageView[3];
	private MenuReservationList mMenuReservationList;
	private MenuFavoriteList mMenuFavoriteList;
	
	private static final int DISMISS_MENU_DELAYED = 1;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == DISMISS_MENU_DELAYED){
//				dismiss();
			}
		};
	};
	
	public Menu(Activity activity){
		mActivity = activity;
		mInflater = activity.getLayoutInflater();
		mContext = mActivity.getApplicationContext();
	}
	
	public void showMenu(int index) {
		/** for A18 UI */
		if(mPopupMenu == null){
			mPopupMenu = new PopupWindow();
		}
		if(mMenuView == null){
			mMenuView = mActivity.getLayoutInflater().inflate(R.layout.menu, null);
			mProgramGuideImg = (ImageView) mMenuView.findViewById(R.id.munu_title_program_guide_img);
			mProgramGuideTv  = (TextView) mMenuView.findViewById(R.id.menu_pg_textview);
			mChannelListImg  = (ImageView) mMenuView.findViewById(R.id.menu_titile_channel_list_img);
			mChannelListTv   = (TextView) mMenuView.findViewById(R.id.channel_list_tv);
			mSettingsImg     = (ImageView) mMenuView.findViewById(R.id.menu_title_settings_img);
			mSettingsTv		 = (TextView) mMenuView.findViewById(R.id.settings_text);
			
			mTitleBar[0]   =  (ImageView) mMenuView.findViewById(R.id.programguide_title_move_bar);
			mTitleBar[1]   =  (ImageView) mMenuView.findViewById(R.id.channellist_title_move_bar);
			mTitleBar[2]   =  (ImageView) mMenuView.findViewById(R.id.settings_title_move_bar);
			
			mMenuTitle  = (LinearLayout) mMenuView.findViewById(R.id.menu_title);
			mMenuCotent = (ViewFlipper) mMenuView.findViewById(R.id.main_menu_container);
			mSubTitle = (TextView) mMenuView.findViewById(R.id.sub_title);
		}
		
		mProgramGuideView = (MenuProgramGuide) mInflater.inflate(R.layout.menu_program_guide, null);
		mChannelListView = (MenuChannelList) mInflater.inflate(R.layout.menu_channel_list, null);
		mSettingView = (MenuSetting) mInflater.inflate(R.layout.menu_setting, null);
		mSettingSubView = (MenuSettingSub) mInflater.inflate(R.layout.menu_setting_sub, null);
		
		mChannelListView.fillData(mController);
		mSettingView.fillData(mController);
		mSettingSubView.setMenuSetting(mSettingView);
		mProgramGuideView.setController(mController);
		
		mProgramGuideView.setInterceptKeyListener(mInterceptKeyListener);
		mChannelListView.setInterceptKeyListener(mInterceptKeyListener);
		mSettingView.setInterceptKeyListener(mInterceptKeyListener);
		mSettingSubView.setInterceptKeyListener(mInterceptKeyListener);
		
		mMenuCotent.removeAllViews();
 		mMenuCotent.addView(mProgramGuideView);
		mMenuCotent.addView(mChannelListView);
		mMenuCotent.addView(mSettingView);
		mMenuCotent.addView(mSettingSubView);
		
		int width = (int) mContext.getResources().getDimension(R.dimen.menu_width);
		int height = (int) mContext.getResources().getDimension(R.dimen.menu_height);
		mPopupMenu.setWidth(width);
		mPopupMenu.setHeight(height);
		mPopupMenu.setFocusable(true);
		mPopupMenu.setContentView(mMenuView);
		if(index == MENU_CHANNEL_LIST){
			mChannelListView.setFromChannelListKey();
		}
		enterSubMenu(index);
		mPopupMenu.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.RIGHT, 0, 0);
		handler.removeMessages(DISMISS_MENU_DELAYED);
		handler.sendEmptyMessageDelayed(DISMISS_MENU_DELAYED,10000);
	}
	
	private void enterSubMenu(int index) {
		if(mIndex == 3)
			mMenuTitle.setAlpha(1.0f);
		mIndex = index;
		mMenuCotent.setDisplayedChild(index);
		setTitleFocus(index);
		Log.d("xubin", "now index : "+index);
		((MenuListener)mMenuCotent.getChildAt(index)).getFocus();
	}
	
	private void leaveMenu(int index) {
		((MenuListener)mMenuCotent.getChildAt(index)).loseFocus();
	}
	
	private void setSubTitle(String title){
		mMenuTitle.setVisibility(View.GONE);
//		mTitleBar.setVisibility(View.INVISIBLE);
//		mTitleBar.clearAnimation();
		mSubTitle.setVisibility(View.VISIBLE); 
		mSubTitle.setText(title);
	}
	
	private void resetTitle(){
		mMenuTitle.setVisibility(View.VISIBLE);
//		mTitleBar.setVisibility(View.VISIBLE);
		mSubTitle.setVisibility(View.GONE);
		mSubTitle.setText("");
		mMenuView.invalidate();
	}

	private InterceptKeyListener mInterceptKeyListener = new InterceptKeyListener() {

		@Override
		public boolean onKeyEvent(int keyCode,int action) {
		    if(keyCode == 268){
		        dismiss();
		        return true;
		    }
		    if(keyCode == KeyEvent.KEYCODE_HOME){
		        dismiss();
		        mController.finish();
		    }
			if (action == KeyEvent.ACTION_UP) {
				if (keyCode == KeyEvent.KEYCODE_ESCAPE
						|| keyCode == KeyEvent.KEYCODE_BACK
						|| keyCode == KeyEvent.KEYCODE_HOME) {
					if (mIndex == 3) {
						backSettingParent(parentIndex);
					} else {
						dismiss();
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					System.out.println(" mIndex " + mIndex);
					if (mIndex > 0 && mIndex <=2) {
						leaveMenu(mIndex);
						mIndex--;
						mMenuCotent.setDisplayedChild(mIndex);
						enterSubMenu(mIndex);
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					System.out.println(" mIndex " + mIndex);
					if (mIndex>=0 & mIndex <=1 ) {
						leaveMenu(mIndex);
						mIndex++;
						mMenuCotent.setDisplayedChild(mIndex);
						enterSubMenu(mIndex);
					}
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_MENU) {
					mIndex = 0;
					dismiss();
					return true;
				} else if (keyCode == DvbKeyEvent.KEYCODE_LIST) {
					if (mIndex != MENU_CHANNEL_LIST) {
						enterSubMenu(MENU_CHANNEL_LIST);
					} else {
						dismiss();
					}
					return true;
				}
			} else if (action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
					mController.changeVolume(-1);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
					mController.changeVolume(1);
					return true;
				}
			}

			return false;
		}

		@Override
		public void showSettingSub(int index) {
			Log.d("Menu", "enter showSettingSub start index="+index);
			switch (index) {
			case MenuSetting.SHOW_APPOINTMENT://2
				mMenuReservationList = (MenuReservationList) mInflater.inflate(R.layout.menu_reservation_list, null);
				mMenuReservationList.fillData(mActivity,mController);
				mMenuReservationList.setInterceptKeyListener(mInterceptKeyListener);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mMenuReservationList);
				setSubTitle(mContext.getResources().getString(R.string.menu_sub_title_reversion));
				mMenuCotent.showNext();
				parentIndex = 2;
				mMenuReservationList.getFocus();
				break;
			case MenuSetting.SHOW_FAVORITE_CHANNEL://4
				if(mMenuFavoriteList == null){
					mMenuFavoriteList = (MenuFavoriteList) mInflater.inflate(R.layout.menu_favorite_list_layout, null);
				}
				mMenuFavoriteList.fillData(mActivity ,mController);
				mMenuFavoriteList.setInterceptKeyListener(mInterceptKeyListener);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mMenuFavoriteList,3);
				setSubTitle(mContext.getResources().getString(R.string.menu_sub_title_fav));
				mMenuCotent.setDisplayedChild(3);
				parentIndex = 0;
				mMenuFavoriteList.getFocus();
				break;
			case MenuSetting.SHOW_SOUND_TRACK:
//			case MenuSetting.SHOW_AUDIO_INDEX:
			case MenuSetting.SHOW_PICTURE_PROPORTION:
				if(mSettingSubView == null){
					mSettingSubView = (MenuSettingSub) mInflater.inflate(R.layout.menu_setting_sub, null);
				}
				mSettingSubView.setInterceptKeyListener(mInterceptKeyListener);
				mSettingSubView.setController(mController);
				if(mMenuCotent.getChildCount() > 3){
					mMenuCotent.removeViewAt(3);
				}
				mMenuCotent.addView(mSettingSubView);
				if(index == MenuSetting.SHOW_SOUND_TRACK){
					Log.d("Menu", "enter MenuSetting.SHOW_SOUND_TRACK start=====");
					setSubTitle(mContext.getResources().getString(R.string.menu_sub_title_audio));
					mSettingSubView.showSoundTrackSettingWindow();
					Log.d("Menu", "enter MenuSetting.SHOW_SOUND_TRACK end=====");
				}
				if(index == MenuSetting.SHOW_AUDIO_INDEX){
					setSubTitle(mContext.getResources().getString(R.string.menu_sub_title_audio_index));
					mSettingSubView.showAudioIndexSettingWindow();
				}
				if(index == MenuSetting.SHOW_PICTURE_PROPORTION){
					setSubTitle(mContext.getResources().getString(R.string.menu_sub_title_pic));
					mSettingSubView.showScreenSettingWindow();
				}
				mMenuCotent.showNext();
				parentIndex = 2;
				mSettingSubView.getFocus();
				break;
            case MenuSetting.SHOW_CHANNEL_CATEGORY:
                Log.d("-----showSettingSub----", " SHOW_CHANNEL_CATEGORY ");
                if (mMenuChannelCategory == null) {
                    mMenuChannelCategory = (MenuChannelCategory) mInflater.inflate(
                            R.layout.menu_channel_category, null);
                }
                if (mMenuCotent.getChildCount() > 3) {
                    mMenuCotent.removeViewAt(3);
                }
                mMenuCotent.addView(mMenuChannelCategory,3);
                mMenuChannelCategory.setInterceptKeyListener(mInterceptKeyListener);
                mMenuChannelCategory.setController(mController);
                mMenuChannelCategory.notifyDataChange();
                mMenuCotent.setDisplayedChild(3);
                mMenuChannelCategory.getFocus();
                setSubTitle(mContext.getResources().getString(R.string.program_guide) + ">" + MenuProgramGuide.mNowMenuTitle);
                parentIndex = 0;
                break;
			default:
				break;
			}
			mIndex = 3;
		}

		@Override
		public void exitMenu() {
			dismiss();
		}

		@Override
		public void handleKeyEvent() {
			Log.d("Menu", "DISMISS_MENU_DELAYED");
			handler.removeMessages(DISMISS_MENU_DELAYED);
			handler.sendEmptyMessageDelayed(DISMISS_MENU_DELAYED,10000);
		}
	};
	private void dismiss(){
		mIndex = 0;
		if(mPopupMenu !=null && mPopupMenu.isShowing()){
			mPopupMenu.dismiss();
			if(mMenuTitle!=null){
				resetTitle();
			}
		}
		if(mMenuReservationList !=null){
			mMenuReservationList.clear();
		}
	}
	interface InterceptKeyListener {
		boolean onKeyEvent(int keyCode,int action);
		void showSettingSub(int index);
		void exitMenu();
		void handleKeyEvent();
	}
	
	private void backSettingParent(int parentIndex) {
		mMenuCotent.setDisplayedChild(parentIndex);
		if(parentIndex == 2){
			mSettingView.refreshData();
			mSettingView.getFocus();
			mIndex = 2;
		}else{
			mProgramGuideView.getFocus();
			mIndex = 0;
		}
		resetTitle();
	}
	
	interface MenuListener {
		void getFocus();
		void loseFocus();
	}
	
	@Override
	public void processMessage(Object sender, DvbMessage msg) {
		switch (msg.what) {
		case ViewMessage.SHOW_MAIN_MENU:
			mController = (ViewController) sender;
			showMenu(MENU_PROGRAM_GUIDE);
			break;
		case ViewMessage.SHOW_CHANNEL_LIST_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_CHANNEL_LIST);
			break;
		case ViewMessage.SHOW_FAVORITE_CHANNEL_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_PROGRAM_GUIDE);
			mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_FAVORITE_CHANNEL);
			break;
		case ViewMessage.SHOW_SOUNDTRACK_WINDOWN:
			mController = (ViewController) sender;
			showMenu(MENU_SETTINGS);
			mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_SOUND_TRACK);
			break;
		case ViewMessage.SHOW_PROGRAM_RESERVE_ALERT:
		case ViewMessage.SWITCH_PLAY_MODE:
		case ViewMessage.STOP_PLAY:
			dismiss();
			break;
		}
	}
	
	public void setTitleFocus(final int index){
		switch (index) {
		case MENU_PROGRAM_GUIDE:
			mProgramGuideImg.setBackgroundResource(R.drawable.menu_title_pg_focus);
			mProgramGuideTv.setTextColor(mContext.getResources().getColor(R.color.focus_text));
			if(mLastTitileFocusView!=null)
				mLastTitileFocusView.setVisibility(View.INVISIBLE);
			mTitleBar[index].setVisibility(View.VISIBLE);
			mLastTitileFocusView = mTitleBar[index];
			mChannelListImg.setBackgroundResource(R.drawable.menu_title_cl_nofocus);
			mChannelListTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			mSettingsImg.setBackgroundResource(R.drawable.menu_title_settings_nofocus);
			mSettingsTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			break;
		case MENU_CHANNEL_LIST:
			mChannelListImg.setBackgroundResource(R.drawable.menu_title_cl_focus);
			mChannelListTv.setTextColor(mContext.getResources().getColor(R.color.focus_text));
			if(mLastTitileFocusView!=null)
				mLastTitileFocusView.setVisibility(View.INVISIBLE);
			mTitleBar[index].setVisibility(View.VISIBLE);
			mLastTitileFocusView = mTitleBar[index];
			mProgramGuideImg.setBackgroundResource(R.drawable.menu_title_pg_nofocus);
			mProgramGuideTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			mSettingsImg.setBackgroundResource(R.drawable.menu_title_settings_nofocus);
			mSettingsTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			break;
		case MENU_SETTINGS:
			mSettingsImg.setBackgroundResource(R.drawable.menu_title_settings_focus);
			mSettingsTv.setTextColor(mContext.getResources().getColor(R.color.focus_text));
			if(mLastTitileFocusView!=null)
				mLastTitileFocusView.setVisibility(View.INVISIBLE);
			mTitleBar[index].setVisibility(View.VISIBLE);
			mLastTitileFocusView = mTitleBar[index];
			
			mProgramGuideImg.setBackgroundResource(R.drawable.menu_title_pg_nofocus);
			mProgramGuideTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			mChannelListImg.setBackgroundResource(R.drawable.menu_title_cl_nofocus);
			mChannelListTv.setTextColor(mContext.getResources().getColor(R.color.no_focus_text));
			break;
		}
	}
}
