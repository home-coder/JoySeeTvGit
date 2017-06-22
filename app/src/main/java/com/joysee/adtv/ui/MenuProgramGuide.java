
package com.joysee.adtv.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.bean.ProgramCatalog;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;

import java.util.ArrayList;
import java.util.Iterator;

public class MenuProgramGuide extends LinearLayout implements MenuListener {

   /* // A11
    private TextView mMenuLiveGuideTextView;
    private TextView mMenuWeekGuideTextView;
    private ImageView mFocusView;
    private int mOffset = (int) getResources().getDimension(R.dimen.menu_margin_top);*/

    private String TAG = "MenuProgramGuide";
    private ViewController mController;
    private String[] mChannelNameArray ;
    private ListView mMenuListView;
    private int mPosition = 0;
    private static ChannelManager mChannelManager;
    private ArrayList<ProgramCatalog> mProgramCatalog;
    public static ArrayList<Integer> mChannelIndex;
    public static String mNowMenuTitle;
    private int [] mChannelFilter;
    private String[] mTitleArray ;
    

    public MenuProgramGuide(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MenuProgramGuide(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuProgramGuide(Context context) {
        super(context);
        init();
    }
    /**
     * 初始化数据
     */
    public void init() {
        Log.d(TAG, "----- init----- begin");
        mTitleArray=this.getResources().getStringArray(R.array.program_guide);
        mChannelManager = ChannelManager.getInstance();
        mProgramCatalog = new ArrayList<ProgramCatalog>();
        try {
            mChannelManager.nativeGetProgramCatalogs(mProgramCatalog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mChannelFilter = new int[mProgramCatalog.size() + 2];
        mChannelNameArray = new String[mProgramCatalog.size() + 2];
        mChannelNameArray[0] = mTitleArray[0];
        mChannelNameArray[1] = mTitleArray[1];
        if (mProgramCatalog.size() > 0) {
            for (int i = 0; i < mProgramCatalog.size(); i++) {
                mChannelFilter[i + 2] = mProgramCatalog.get(i).getFilter();
                mChannelNameArray[i + 2] = mProgramCatalog.get(i).getName();
            }
        }
//        else {
//            Log.d(TAG, " mProgramCatalog size = " + mProgramCatalog.size());
//            mChannelNameArray = mTitleArray;
//        }
        mChannelIndex = new ArrayList<Integer>();
        Log.d(TAG, "----- init----- end ");
    }

    private int pageSize = 7;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	mInterceptKeyListener.handleKeyEvent();
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if(keyCode == 268){
            return true;
        }
        Log.d(TAG, " dispatchKeyEvent mPosition = " + mPosition);
        if ((mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action))) {
            return true;
        }
        
        if(keyCode == KeyEvent.KEYCODE_PAGE_DOWN && action == KeyEvent.ACTION_DOWN){
			int curPosition = mMenuListView.getFirstVisiblePosition();
			Log.d("songwenxuan","curPosition = " + curPosition);
			mMenuListView.setSelection(pageSize+curPosition);
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_PAGE_UP && action == KeyEvent.ACTION_DOWN){
			int curPosition = mMenuListView.getFirstVisiblePosition();
			Log.d("songwenxuan","curPosition = " + curPosition);
			mMenuListView.setSelection(curPosition-pageSize);
			return true;
		}
//        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && mPosition >= mChannelNameArray.length - 1) {
//            return true;
//        }
        //进入频道列表时再回来默认焦点在第一个Item
        // if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
        // mPosition = 0;
        // }
        return super.dispatchKeyEvent(event);
    }
    
    private boolean processMenuListKeyEvent(KeyEvent event, ListView list, BaseAdapter adapter) {
    	Log.d(TAG,"processMenuListKeyEvent() , keycode = " + event.getKeyCode() + "; action = " + event.getAction());
		final int keyCode = event.getKeyCode();
		final int action = event.getAction();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_PAGE_UP:
			if (action == KeyEvent.ACTION_DOWN ) {
				if (adapter.getCount() > 0 && list.getSelectedItemPosition() == 0) {
					list.setSelection(adapter.getCount() - 1);
					return true;
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_PAGE_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				Log.d(TAG, "list count = "  + adapter.getCount());
				if (adapter.getCount() > 0 && list.getSelectedItemPosition() == (adapter.getCount() - 1)) {
					list.setSelection(0);
					Log.d(TAG,"set list selection 0 ");
					return true;
				}
			}
			break;
		}
		return false;
	}
    private MyListAdapter mListAdapter;
    @Override
    protected void onFinishInflate() {
        /* // A11
        mMenuLiveGuideTextView = (TextView) findViewById(R.id.menu_pg_live_guide_textivew);
        mMenuWeekGuideTextView = (TextView) findViewById(R.id.menu_pg_week_guide_textivew);
        mFocusView = (ImageView) findViewById(R.id.ivFocus);
        mMenuLiveGuideTextView.setOnFocusChangeListener(onFocusChangeListener);
        mMenuWeekGuideTextView.setOnFocusChangeListener(onFocusChangeListener);
        mMenuLiveGuideTextView.setOnClickListener(this);
        mMenuWeekGuideTextView.setOnClickListener(this);
        mMenuLiveGuideTextView.requestFocus();
         */
        Log.d(TAG, "------onFinishInflate------");
        mMenuListView = (ListView) findViewById(R.id.progguide_menu_listview);
       
        try {
//            mMenuListView.setPageScrollSize(7);//TODO 使用了厂商的SDK API
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mMenuListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                showItemView(arg2 % mChannelNameArray.length);
            }
        });
        mMenuListView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d(TAG, "---onItemSelected--- arg2 = " + arg2);
                mPosition = arg2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Log.d(TAG, "---onNothingSelected--- ");
            }
        });
        mListAdapter = new MyListAdapter();
        mMenuListView.setAdapter(mListAdapter);
        mMenuListView.setOnKeyListener(new View.OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
        			if(mMenuListView.getFirstVisiblePosition() == mMenuListView.getSelectedItemPosition()){
        				mMenuListView.setSelection(mMenuListView.getFirstVisiblePosition() -1);
        				return true;
        			}
        		}
        		return false;
        	}
        });
//        mMenuListView.setAdapter(new ArrayAdapter<String>(getContext(),
//                R.layout.progguide_menu_listitem, mTitleArray));
        mMenuListView.requestFocus();
        super.onFinishInflate();
    }

    private InterceptKeyListener mInterceptKeyListener;

    public void setInterceptKeyListener(InterceptKeyListener interceptKeyListener) {
        mInterceptKeyListener = interceptKeyListener;
    }

    @Override
    public void getFocus() {
        Log.d(TAG, "-----getFocus---- mPosition = " + mPosition + " mChannelNameArray.length = "
                + mChannelNameArray.length);
        mMenuListView.requestFocus();
        if(mPosition==0){
        	int selection = 0;
        	if(mChannelNameArray.length > pageSize){
        		selection = mChannelNameArray.length * 1000000;
        	}
        	Log.d("songwenxuan", "selection = " + selection);
        	mMenuListView.setSelection(selection);
        	mPosition = selection;
        }else{
//            mMenuListView.setSelection(mPosition);
            mMenuListView.requestFocus(mPosition);
        }
        // if (mPosition >= mChannelNameArray.length - 1) {
        // mMenuListView.setSelection(mPosition);
        // }
    }

    @Override
    public void loseFocus() {
        Log.d(TAG, "-----loseFocus----");
        mMenuListView.clearFocus();
    }

    public void showItemView(int itemID) {
        Log.d(TAG, "----showItemView----- itemID = " + itemID);
        mNowMenuTitle = mChannelNameArray[itemID];
        switch (itemID) {
            case 0:
                mController.showProgramGuide();
                mInterceptKeyListener.exitMenu();
                break;
            case 1:
                mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_FAVORITE_CHANNEL);
                break;
            default:
                if (mChannelFilter.length <= 2) {
                    return;
                }
                mChannelIndex.clear();
                mChannelIndex = getChannelIndex(mChannelFilter[itemID]);
                mInterceptKeyListener.showSettingSub(MenuSetting.SHOW_CHANNEL_CATEGORY);
                break;
        }
    }
   /* @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_pg_live_guide_textivew:
                Log.d("songwenxuan", "menu_pg_live_guide_textivew onclick!!!");
                mController.showLiveGuide();
                mInterceptKeyListener.exitMenu();
                break;
            case R.id.menu_pg_week_guide_textivew:
                Log.d("songwenxuan", "menu_pg_week_guide_textivew onclick!!!");
                mController.showProgramGuide();
                mInterceptKeyListener.exitMenu();
                break;
            default:
                break;
        }
    }*/

    /*// A11
    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                int[] location = new int[2];
                v.getLocationInWindow(location);
                if (location[1] == 0)
                    return;
                MarginLayoutParams params = (MarginLayoutParams) mFocusView
                        .getLayoutParams();
                params.topMargin = location[1] - mOffset;
                Log.d("songwenxuan", "onFocusChange() , params.topMargin = " + params.topMargin);
                mFocusView.setLayoutParams(params);
                // Animation anim = new AlphaAnimation(0.0f, 1.0f);
                // anim.setDuration(300);
                // anim.setFillAfter(true);
                // anim.setFillEnabled(true);
                // mFocusView.startAnimation(anim);
            }
        }
    };
     */
    public void setController(ViewController controller) {
        this.mController = controller;
    }

    public ArrayList<Integer> getChannelIndex(int filter) {
        int tempIndex = -1;
        ArrayList<Integer> IndexList = new ArrayList<Integer>();
        while (true) {
            tempIndex = mChannelManager.nativeGetNextDVBService(tempIndex, filter);
            if (tempIndex < 0 || (IndexList.size() > 0 && tempIndex == IndexList.get(0))) {
                break;
            }
            IndexList.add(tempIndex);
        }
        return IndexList;
    }
    class MyListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
        	if(mChannelNameArray.length > pageSize){
        		return Integer.MAX_VALUE;
        	}else{
        		return mChannelNameArray.length;
        	}
        }

        @Override
        public Object getItem(int arg0) {
        	if(mChannelNameArray.length > pageSize){
        		return mChannelNameArray[arg0%mChannelNameArray.length];
        	}else{
        		return mChannelNameArray[arg0];
        	}
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            final ViewHolder viewHolder;
            if (arg1 == null || arg1.getTag() == null) {
                viewHolder = new ViewHolder();
                arg1 = inflate(getContext(), R.layout.progguide_menu_listitem, null);
                viewHolder.name = (TextView) arg1.findViewById(R.id.progguide_listview_item_tv);
                viewHolder.dividerLine = arg1.findViewById(R.id.divide_line);
                arg1.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) arg1.getTag();
            }
            viewHolder.name.setText(mChannelNameArray[arg0%mChannelNameArray.length]);
            if(mChannelNameArray.length > pageSize && arg0%mChannelNameArray.length == mChannelNameArray.length -1){
            	viewHolder.dividerLine.setVisibility(View.VISIBLE);
            }else{
            	viewHolder.dividerLine.setVisibility(View.INVISIBLE);
            }
            return arg1;
        }
    }

    private static class ViewHolder {
        TextView name;
        View dividerLine;
    }
}
