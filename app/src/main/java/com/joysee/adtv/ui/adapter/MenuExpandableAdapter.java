package com.joysee.adtv.ui.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.NETEventInfo;

public class MenuExpandableAdapter extends BaseExpandableListAdapter{
	DvbLog log = new DvbLog("MenuExpandableAdapter", DvbLog.DebugType.D);
    private LayoutInflater mInflater;
    private ArrayList<Integer> mParentList;
    private List<ArrayList<Integer>> mChildList;
    private Context mContext;
    private ViewController mViewController;
    private int mLastFavorite;
    private int mProgramNameLength;
    
    private static final ExecutorService mPool = Executors.newFixedThreadPool(2);
    
    @SuppressWarnings("rawtypes")
	private SparseArray<AsyncTask> mTasks = new SparseArray<AsyncTask>();
    
    
    public MenuExpandableAdapter(Context context,ArrayList<Integer> dadList, List<ArrayList<Integer>> child,ViewController viewController) {
        mInflater = LayoutInflater.from(context);
        mParentList = dadList;
//        mChildList=child;
        mContext = context;
        mViewController = viewController;
        mProgramNameLength = (int)mContext.getResources().getDimension(R.dimen.menu_clitem_program_info);
        log.D(" mLastFavorite "+mLastFavorite);
    }
    
    public void setLastFavorite(int lastFavorite){
    	mLastFavorite = lastFavorite;
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
    	return null;
//        return (mChildList==null || mChildList.get(groupPosition%mParentList.size())==null)?null:mChildList.get(groupPosition%mParentList.size()).get(childPosition-1);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	
    	ViewHolder viewHolder;
        if (convertView == null|| convertView.getTag()==null) {
            viewHolder = new ViewHolder();
            convertView= mInflater.inflate(R.layout.menu_channel_list_item, null);
            viewHolder.channelIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_channel_icon);
            viewHolder.channelNum = (TextView) convertView.findViewById(R.id.menu_clitem_channel_num);
            viewHolder.channelName = (TextView) convertView.findViewById(R.id.menu_clitem_channel_name);
            viewHolder.favoriteIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_favorite_icon);
            viewHolder.channelView = convertView.findViewById(R.id.menu_clitem_channel_info);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        
        if (mChildList!=null && mChildList.size()> 0 && mChildList.get(groupPosition%mParentList.size()) != null && mChildList.get(groupPosition%mParentList.size()).size() > childPosition) {
        	DvbService service = mViewController.getChannelByNativeIndex(mChildList.get(groupPosition%mParentList.size()).get(childPosition));
    		viewHolder.favoriteIcon.setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES?View.VISIBLE:View.INVISIBLE);
    		viewHolder.channelNum.setText(String.valueOf(service.getLogicChNumber()));
    		viewHolder.channelName.setText(service.getChannelName());
			viewHolder.channelIcon.setImageResource(R.drawable.bc_icon);
        }
        
        return null;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
//    	log.D(" mParentList.size() "+mParentList.size());
//    	log.D(" mChildList.size() "+mChildList.size());
//        return (mChildList==null || mChildList.get(groupPosition%mParentList.size())==null)?0:(mChildList.get(groupPosition%mParentList.size())).size();
    	return 0;
    }
    
    @Override
    public Object getGroup(int groupPosition) {
    	groupPosition = groupPosition % mParentList.size();
        return mParentList==null?null:mParentList.get(groupPosition);
    }
    
    @Override
    public int getGroupCount() {
    	return Integer.MAX_VALUE - 1000000;
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition%mParentList.size();
    }
    
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	long startTime = SystemClock.uptimeMillis();
        final ViewHolder viewHolder;
        if (convertView == null|| convertView.getTag()==null) {
            viewHolder = new ViewHolder();
            convertView= mInflater.inflate(R.layout.menu_channel_list_item, null);
            viewHolder.channelView = convertView.findViewById(R.id.menu_clitem_channel_info);
            viewHolder.channelNum = (TextView) convertView.findViewById(R.id.menu_clitem_channel_num);
            viewHolder.channelName = (TextView) convertView.findViewById(R.id.menu_clitem_channel_name);
            viewHolder.channelIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_channel_icon);
            viewHolder.favoriteIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_favorite_icon);
            viewHolder.deviceLine = (ImageView) convertView.findViewById(R.id.devide_line);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
            if(viewHolder.tag !=0 ){
            	removeTask(viewHolder.tag);
            }
        }
        int index = groupPosition%mParentList.size();
    	if(index == mParentList.size()-1){
    		if(viewHolder.channelView.getVisibility() == View.VISIBLE){
    			viewHolder.channelView.setVisibility(View.GONE);
    		}
    	}else{
    		if(viewHolder.channelView.getVisibility() == View.GONE){
    			viewHolder.channelView.setVisibility(View.VISIBLE);
    		}
    		long start = SystemClock.uptimeMillis();
    		DvbService service = mViewController.getChannelByNativeIndex(mParentList.get(index));
    		int serviceId = service.getServiceId();
    		log.D("耗时 getView getChannelByNativeIndex  "+(SystemClock.uptimeMillis()-start));
    		viewHolder.favoriteIcon.setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES && index <= mLastFavorite?View.VISIBLE:View.INVISIBLE);	
    		viewHolder.channelNum.setText(String.valueOf(service.getLogicChNumber()));
    		viewHolder.channelName.setText(service.getChannelName());
    		
			Bitmap bitmap = getBitmap(serviceId);
			if(bitmap != null)
				viewHolder.channelIcon.setImageBitmap(bitmap);
			getNetEpgInfo(serviceId,viewHolder,index+1);
				
    	}
    	log.D(" getView 耗时  "+(SystemClock.uptimeMillis()-startTime));
        return convertView;
    }
    
    private final static SparseArray<SoftReference<Bitmap>> mIconCache = new SparseArray<SoftReference<Bitmap>>();
    
    private Bitmap getBitmap(int serviceId) {
    	long start = SystemClock.uptimeMillis();
    	SoftReference<Bitmap> value = mIconCache.get(serviceId);
    	Bitmap bitmap = null;
    	if(value != null)
    		bitmap = value.get();
    	if(bitmap != null){
    		log.D("-----------------------------   "+Thread.currentThread().getName()+"  "+serviceId);
			return bitmap;
    	}
		String iconPath = mViewController.getChannelIconPath(serviceId);
		log.D("耗时 native iconPath "+(SystemClock.uptimeMillis()-start) +" "+iconPath);
		if(iconPath != null){
			start = SystemClock.uptimeMillis();
			bitmap = BitmapFactory.decodeFile(iconPath);
			log.D("耗时 .png "+(SystemClock.uptimeMillis()-start) +" "+iconPath);
			if(bitmap != null){
				log.D("*******************************   "+Thread.currentThread().getName()+"  "+serviceId);
				mIconCache.put(serviceId, new SoftReference<Bitmap>(bitmap));
				return bitmap;
			}
		}
		return null;
	}
    private void removeTask(int tag){
    	log.D(" convertView !=  null mTasks.size() "+mTasks.size());
    	@SuppressWarnings("rawtypes")
		AsyncTask task = mTasks.get(tag);
    	if( task != null ){
    		Log.d("wgh", " remove "+tag);
        	task.cancel(true);
        	mTasks.remove(tag);
    	}
    }
    @SuppressWarnings("rawtypes")
	private void getNetEpgInfo(final int serviceId, final ViewHolder viewHolder,final int tag) {
    	viewHolder.tag = tag;
    	
    	AsyncTask task = new AsyncTask<Integer, Void, NETEventInfo>() {
			protected NETEventInfo doInBackground(Integer... params)  {
				long startTime = System.currentTimeMillis();
				log.D(" startTime ");
				NETEventInfo eventInfo = mViewController.getCurrentProgramInfo(serviceId);
				log.D("eventInfo 耗时"+(System.currentTimeMillis()-startTime)+"   "+Thread.currentThread().getName()+"  "+serviceId);
				return eventInfo;
			}

			@Override
			protected void onPostExecute(NETEventInfo eventInfo) {
				if(viewHolder.tag != tag)
					return;
				if(eventInfo !=null ){
					String eName = eventInfo.getEname();
					eventInfo.getBegintime();
					eventInfo.getDuration();
					log.D(" ");
				}else{
//					viewHolder.programName.setText(R.string.no_program_info);
				}
				viewHolder.tag = 0;
				mTasks.remove(tag);
				super.onPostExecute(eventInfo);
			}
			
		}.executeOnExecutor(mPool);
		
		Log.d("wgh", " put "+tag +"  serviceId " +serviceId);
		mTasks.put(tag, task);
	}
    
	@Override
    public boolean hasStableIds() {
        return false;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    } 
    
    private static class ViewHolder{
        ImageView channelIcon;
        TextView channelNum;
        TextView channelName;
        ImageView favoriteIcon;
        View channelView ;
        ImageView deviceLine;
		int tag;
    }

	public void clearTaskMap() {
		Log.d("wgh"," clearTaskMap ");
		int size = mTasks.size();
		for(int i=0;i<size;i++){
			AsyncTask task = mTasks.valueAt(i);
			if(task !=null ){
				task.cancel(true);
			}
		}
		mTasks.clear();
	}
    
}