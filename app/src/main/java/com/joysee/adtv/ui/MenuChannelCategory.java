
package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

public class MenuChannelCategory extends LinearLayout implements MenuListener {

    private String TAG = "MenuChannelCategory";
    private ViewController mController;
    private ListView mMenuListView;
    private int mPosition = 0;
    private ArrayList<Integer> mChannleIndex;
    private View mTipsView;
    private MyListAdapter mListAdapter;
    private int mPageSize = 7;

    public MenuChannelCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, " 3 ");
        init();
    }

    public MenuChannelCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, " 2 ");
        init();
    }

    public MenuChannelCategory(Context context) {
        super(context);
        Log.d(TAG, " 1 ");
        init();
    }

    public void init() {
        mChannleIndex = MenuProgramGuide.mChannelIndex;
        Log.d(TAG, "-----init  mChannleIndex = " + mChannleIndex);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	mInterceptKeyListener.handleKeyEvent();
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        Log.d(TAG, " dispatchKeyEvent mPosition = " + mPosition);
//        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && action == KeyEvent.ACTION_DOWN){
//        	if (mListAdapter.getCount() > 0 && mMenuListView.getSelectedItemPosition() == (mListAdapter.getCount() - 1)) {
//        		mMenuListView.setSelection(0);
//				Log.d(TAG,"set list selection 0 ");
//				return true;
//			}
//        }
        if(keyCode == KeyEvent.KEYCODE_PAGE_DOWN && action == KeyEvent.ACTION_DOWN){
			int curPosition = mMenuListView.getFirstVisiblePosition();
			Log.d("songwenxuan","curPosition = " + curPosition);
			mMenuListView.setSelection(mPageSize+curPosition);
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_PAGE_UP && action == KeyEvent.ACTION_DOWN){
			int curPosition = mMenuListView.getFirstVisiblePosition();
			Log.d("songwenxuan","curPosition = " + curPosition);
			mMenuListView.setSelection(curPosition-mPageSize);
			return true;
		}
        if ((mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action))) {
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && mPosition >= mChannleIndex.size() - 1 && mChannleIndex.size() < mPageSize) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "------onFinishInflate------");
        mMenuListView = (ListView) findViewById(R.id.progcategory_listview);
//        try {
//			mMenuListView.setPageScrollSize(7);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
        mTipsView = findViewById(R.id.progcategory_tips);
        mMenuListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mController.switchChannelFromIndex(ServiceType.TV, mChannleIndex.get(arg2%mChannleIndex.size()));
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
        mMenuListView.requestFocus();
        if (mChannleIndex.size() <= 0) {
            mTipsView.setVisibility(View.VISIBLE);
        } else {
            mTipsView.setVisibility(View.GONE);
        }
        int selection = 0;
        if(mChannleIndex.size() > mPageSize){
        	selection = mChannleIndex.size() * 1000000;
        }
        Log.d("songwenxuan", "selection = " + selection);
        mMenuListView.setSelection(selection);
        super.onFinishInflate();
    }

    private InterceptKeyListener mInterceptKeyListener;

    public void setInterceptKeyListener(InterceptKeyListener interceptKeyListener) {
        mInterceptKeyListener = interceptKeyListener;
    }

    @Override
    public void getFocus() {
        if (mChannleIndex.size() > 0) {
            if (mMenuListView.getVisibility() == View.GONE) {
                mMenuListView.setVisibility(View.VISIBLE);
            }
            mMenuListView.requestFocus();
            int selection = 0;
            if(mChannleIndex.size() > mPageSize){
            	selection = mChannleIndex.size() * 1000000;
            }
            Log.d("songwenxuan", "selection = " + selection);
            mMenuListView.setSelection(selection);
            mPosition = selection;
        } else {
            mMenuListView.setVisibility(View.GONE);
            mTipsView.requestFocus();
        }
    }

    @Override
    public void loseFocus() {
        mMenuListView.clearFocus();
        mTipsView.clearFocus();
    }

    public void setController(ViewController controller) {
        this.mController = controller;
    }
    
    public void notifyDataChange() {
        init();
        if (mChannleIndex.size() <= 0) {
            mTipsView.setVisibility(View.VISIBLE);
        } else {
            mTipsView.setVisibility(View.GONE);
        }
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        int selection = 0;
        if(mChannleIndex.size() > mPageSize){
        	selection = mChannleIndex.size() * 1000000;
        }
        mMenuListView.setSelection(selection);
        Log.d("songwenxuan", "selection = " + selection);
    }

    class MyListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
        	if(mChannleIndex.size() > mPageSize){
        		return Integer.MAX_VALUE- 1000000;
        	}else{
        		return mChannleIndex.size();
        	}
        }

        @Override
        public Object getItem(int position) {
            return mChannleIndex.get(position % mChannleIndex.size());
        }

        @Override
        public long getItemId(int arg0) {
            return arg0%mChannleIndex.size();
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
        	Log.d("songwenxuan","getView() , position = " + arg0);
            final ViewHolder viewHolder;
            if (arg1 == null || arg1.getTag() == null) {
                viewHolder = new ViewHolder();
                arg1 = inflate(getContext(), R.layout.menu_channel_category_item, null);
                viewHolder.name = (TextView) arg1.findViewById(R.id.menu_channel_category_name);
                viewHolder.number = (TextView) arg1.findViewById(R.id.menu_channel_category_num);
                viewHolder.icon = (ImageView) arg1.findViewById(R.id.menu_channel_category_icon);
                viewHolder.favorite_icon = (ImageView) arg1
                        .findViewById(R.id.menu_channel_category_favorite_icon);
                viewHolder.divideLine = arg1.findViewById(R.id.devide_line);
//                viewHolder.channelIconTextView = (TextView) arg1.findViewById(R.id.menu_channel_category_icon_text);
                arg1.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) arg1.getTag();
            }
            if(mChannleIndex.size() > mPageSize && arg0%mChannleIndex.size() == mChannleIndex.size() - 1){
            	viewHolder.divideLine.setVisibility(View.VISIBLE);
            }else{
            	viewHolder.divideLine.setVisibility(View.INVISIBLE);
            }

            DvbService service = mController.getChannelByNativeIndex(mChannleIndex.get(arg0 % mChannleIndex.size()));
            int serviceId = service.getServiceId();
            viewHolder.favorite_icon
                    .setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES ? View.VISIBLE
                            : View.INVISIBLE);
            viewHolder.number.setText(String.valueOf(service.getLogicChNumber()));
            viewHolder.name.setText(service.getChannelName());
//            viewHolder.channelIconTextView.setText(""+service.getLogicChNumber());
            Bitmap bitmap = getBitmap(serviceId);
            if (bitmap != null) {
            	Log.d("songwenxuan","bitmap != null ");
                viewHolder.icon.setVisibility(View.VISIBLE);
                viewHolder.icon.setImageBitmap(bitmap);
                viewHolder.number.setVisibility(View.VISIBLE);
//                viewHolder.channelIconTextView.setVisibility(View.GONE);
            }else{
            	Log.d("songwenxuan","bitmap == null ");
            	viewHolder.icon.setImageResource(R.drawable.default_icon);
//                viewHolder.number.setVisibility(View.GONE);
//                viewHolder.icon.setVisibility(View.GONE);
//                viewHolder.channelIconTextView.setBackgroundResource(R.drawable.default_icon);
            }
            return arg1;
        }
    }

    private static class ViewHolder {
        TextView number;
        TextView name;
//        TextView channelIconTextView;
        ImageView icon;
        ImageView favorite_icon;
        View divideLine;
    }

    private final static SparseArray<SoftReference<Bitmap>> mIconCache = new SparseArray<SoftReference<Bitmap>>();

    private Bitmap getBitmap(int serviceId) {
        SoftReference<Bitmap> value = mIconCache.get(serviceId);
        Bitmap bitmap = null;
        if (value != null) {
            bitmap = value.get();
        }
        if (bitmap != null) {
            return bitmap;
        }
        String iconPath = mController.getChannelIconPath(serviceId);
//        iconPath = "/data/data/com.joysee.adtv/test.png";
        if (iconPath != null && !iconPath.equals("")) {
            bitmap = BitmapFactory.decodeFile(iconPath);
            if (bitmap != null) {
                mIconCache.put(serviceId, new SoftReference<Bitmap>(bitmap));
                return bitmap;
            }
        }
        return null;
    }
}
