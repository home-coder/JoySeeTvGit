
package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;
import com.joysee.adtv.ui.adapter.MenuChannelListAdapter;

public class MenuChannelList extends LinearLayout implements MenuListener {
	DvbLog log = new DvbLog("MenuChannelList", DvbLog.DebugType.D);

	private ListView mChannelListView;
	private MenuChannelListAdapter mMenuChannelListAdapter;

	private View mLastFocusView;
	private int mLastIndex = -1;

	private int mPosition;
	private int mMaxPosition = 5;

	private ArrayList<Integer> mTVBC_ChannelList = new ArrayList<Integer>();
	private ArrayList<Integer> mTvChannelList = new ArrayList<Integer>();
	private ArrayList<Integer> mBcChannelList = new ArrayList<Integer>();
	private List<ArrayList<Integer>> mChild = new ArrayList<ArrayList<Integer>>();; // 子列表
	private int mLastFavorite = -2;

	private boolean mKeyRepeat = false;
	private int mKeyRepeatInterval = 100;
	private long mLastKeyTime;
	private boolean mExpanded;

	private ViewController mController;
	private Context mContext;
	private Handler mHandler = new Handler();
	private int mFirstSelection;

	private int mLastExpandGroup;
	private ImageView mExpandIcon;

	private boolean mFromCLKey;

	public MenuChannelList(Context context) {
		super(context);
	}

	public MenuChannelList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MenuChannelList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		mChannelListView = (ListView) findViewById(R.id.menu_cl_list);
		mChannelListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int index = position % mTVBC_ChannelList.size();
				log.D("onItem : " + index + " tv:" + mTvChannelList.size() + "  bc:"
						+ mBcChannelList.size() + " total:" + mTVBC_ChannelList.size());
				Integer nativeChannelIndex = mTVBC_ChannelList.get(index);
//				if (mBcChannelList.contains(nativeChannelIndex)) {
//					mController.switchChannelFromIndex(ServiceType.BC, nativeChannelIndex);
//				}else{
					mController.switchChannelFromIndex(ServiceType.TV, nativeChannelIndex);
//				}
			}
		});

		mChannelListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int arg2, long arg3) {
//				int location[] = new int[2];
//				view.getLocationInWindow(location);
//				log.D("mPosition  " + mPosition
//						+ " OnItemSelectedListener " + location[1]);
//				changeFocusView(view, arg2, true);
//				if (mLastFocusView != null)
//					changeFocusView(mLastFocusView, mLastIndex, false);
//				mLastFocusView = view;
//				mLastIndex = arg2;

			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		mChannelListView.setOnKeyListener(new View.OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
        			if(mChannelListView.getFirstVisiblePosition() == mChannelListView.getSelectedItemPosition()){
        				mChannelListView.setSelection(mChannelListView.getFirstVisiblePosition() -1);
        				return true;
        			}
        		}
        		return false;
        	}
        });
		super.onFinishInflate();
	}

	public void fillData(int startIndex) {
		getNativeData();
		if (mMenuChannelListAdapter == null) {
			mMenuChannelListAdapter = new MenuChannelListAdapter(mContext, mTVBC_ChannelList,
					mController);
			mChannelListView.setAdapter(mMenuChannelListAdapter);
		} else {
			mMenuChannelListAdapter.setLastFavorite(mLastFavorite);
			mMenuChannelListAdapter.setLastTvIndex(mTvChannelList.size()+mFavList.size()-1);
			mMenuChannelListAdapter.notifyDataSetChanged();
			log.D(" notifyDataSetChanged ");
		}
		mChannelListView.setSelection(mFirstSelection);
	}
    private ArrayList<Integer> mFavList = new ArrayList<Integer>();
	private void getNativeData() {
		mTvChannelList.clear();
		mBcChannelList.clear();
		mFavList.clear();
		mTVBC_ChannelList.clear();
		mChild.clear();
		mLastFavorite = mController.getFavouriteIndex().size() - 1;
		mController.getAllChannelIndex(mFavList,mTvChannelList, mBcChannelList);
		if (mTvChannelList.size() <= 0) {
			return;
		}
		mTVBC_ChannelList.addAll(mFavList);
		mTVBC_ChannelList.addAll(mTvChannelList);
//		mTVBC_ChannelList.addAll(mBcChannelList);

		mFirstSelection = Integer.MAX_VALUE / 2;
		int totalPosition = mTVBC_ChannelList.size();

		mFirstSelection = totalPosition * 1000000;
		int type = mController.getCurrentPlayType();
		int currentPosition = mController.getCurrentPosition();
		if(type == ServiceType.TV){
		    mFirstSelection = mFirstSelection + currentPosition + mFavList.size();
		}else{
		    mFirstSelection = mFavList.size() + mTvChannelList.size() + mFirstSelection + currentPosition;
		}
		log.D("Integer.MAX_VALUE = " + Integer.MAX_VALUE);

		// for (int i = 0; i < totalPosition + 1; i++) {
		// if ((mFirstSelection + i) % mTvChannelList.size() == 0){
		// mFirstSelection = mFirstSelection + i;
		// }
		// }
	}

	public float getInterpolation(float input) {
		return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
	}
	private int pageSize = 7;
	public boolean dispatchKeyEvent(KeyEvent event) {
		mInterceptKeyListener.handleKeyEvent();
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		if(keyCode == 268){
		    return true;
		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			final long currenKeyDownTime = SystemClock.uptimeMillis();
			final long interval = currenKeyDownTime - mLastKeyTime;
			if (mKeyRepeat && interval < mKeyRepeatInterval) {
				log.D(mKeyRepeat + " 抛弃");
				return true;
			}
			mKeyRepeat = true;
			mLastKeyTime = currenKeyDownTime;
			if(keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
				int curPosition = mChannelListView.getFirstVisiblePosition();
				mChannelListView.setSelection(pageSize+curPosition);
				return true;
			}
			if(keyCode == KeyEvent.KEYCODE_PAGE_UP){
				int curPosition = mChannelListView.getFirstVisiblePosition();
				mChannelListView.setSelection(curPosition-pageSize);
				return true;
			}
//			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//				if (mLastFocusView != null) {
//					changeFocusView(mLastFocusView, mLastIndex, false);
//				}
//				if (mPosition < mMaxPosition)
//					mPosition++;
//				if (mPosition == mMaxPosition) {// 长按时保持最后一个条目字体颜色为绿色
//					int selectedItemPosition = mChannelListView.getSelectedItemPosition();
//					int firstVisiblePosition = mChannelListView.getFirstVisiblePosition();
//					View view = mChannelListView.getChildAt(selectedItemPosition
//							- firstVisiblePosition + 1);
//					log.D("selectedPosition " + selectedItemPosition + " firstPosition"
//							+ firstVisiblePosition + " childAt "
//							+ (selectedItemPosition - firstVisiblePosition) + " view " + view);
//					if (view != null) {
//						changeFocusView(view, selectedItemPosition - firstVisiblePosition + 1, true);
//					}
//					log.D(" down long action down");
//				}
//			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
//				if (mLastFocusView != null) {
//					changeFocusView(mLastFocusView, mLastIndex, false);
//				}
//				if (mPosition > 0)
//					mPosition--;
//				if (mPosition == 0) {// 长按时保持第一个条目字体颜色为绿色
//					int selectedItemPosition = mChannelListView.getSelectedItemPosition();
//					int firstVisiblePosition = mChannelListView.getFirstVisiblePosition();
//					View view = mChannelListView.getChildAt(selectedItemPosition
//							- firstVisiblePosition - 1);
//					if (view != null) {
//						changeFocusView(view, selectedItemPosition - firstVisiblePosition - 1, true);
//					}
//				}
//			} else 
			if (keyCode == KeyEvent.KEYCODE_F9) {
				int count = mChannelListView.getChildCount();
				for (int i = 0; i < count; i++) {
					int[] location = new int[2];
					View v = mChannelListView.getChildAt(i);
					v.getLocationInWindow(location);
					log.D(" i " + i + " item top " + location[1]);
				}
			}
			if (mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action)) {
				mKeyRepeat = false;
				return true;
			}
			return super.dispatchKeyEvent(event);

		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			log.D("onKey up : " + mPosition);
			mKeyRepeat = false;
		}
		if (mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action)) {
			mKeyRepeat = false;
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	private InterceptKeyListener mInterceptKeyListener;

	public void setInterceptKeyListener(
			InterceptKeyListener interceptKeyListener) {
		mInterceptKeyListener = interceptKeyListener;
	}

	@Override
	public void getFocus() {
		fillData(0);
		mChannelListView.requestFocus();
//		if (mFromCLKey) {// 直接按频道列表键时列表被挡住一块
//		mChannelListView.setSelectionFromTop(mFirstSelection, 3);
//		mFromCLKey = false;
//		} else {
			mChannelListView.setSelection(mFirstSelection);
//		}
		mExpanded = false;
	}

	public void fillData(ViewController controller) {
		mController = controller;
		fillData(0);
	}

	public void changeFocusView(View view, int index, boolean isFocus) {
		TextView channelNumTv = (TextView) view.findViewById(R.id.menu_clitem_channel_num);
		TextView channelNameTv = (TextView) view.findViewById(R.id.menu_clitem_channel_name);
		if (isFocus) {
			channelNumTv.setTextColor(getResources().getColor(R.color.menu_list_focus));
			channelNameTv.setTextColor(getResources().getColor(R.color.menu_list_focus));
			index = index % mTvChannelList.size();
		} else {
			channelNumTv.setTextColor(getResources().getColor(R.color.no_focus_text));
			channelNameTv.setTextColor(getResources().getColor(R.color.no_focus_text));
			index = index % mTvChannelList.size();
		}
	}

	public void setFromChannelListKey() {
		mFromCLKey = true;
	}

	@Override
	public void loseFocus() {

	}

}
