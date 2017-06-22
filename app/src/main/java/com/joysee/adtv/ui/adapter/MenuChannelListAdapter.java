package com.joysee.adtv.ui.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 替换MenuExpandableAdapter
 * @author benz
 *
 */
public class MenuChannelListAdapter extends BaseAdapter{
	
	
	DvbLog log = new DvbLog("MenuExpandableAdapter", DvbLog.DebugType.D);
	
	private LayoutInflater mInflater;
	private ArrayList<Integer> list;
	private Context mContext;
	private ViewController mViewController;
	private int mLastFavorite;
	private int mProgramNameLength;
	private int mLastTvIndex;
//	private ArrayList<Integer> mChannelList;
	
	public MenuChannelListAdapter(Context context,ArrayList<Integer> dataList,ViewController viewController) {//ArrayList<Integer> channelList
		list = dataList;
		mContext = context;
		mViewController = viewController;
		mInflater = LayoutInflater.from(context);
//		this.mChannelList = channelList;
		log.D(" mLastFavorite "+mLastFavorite);
	}
	
	private static class ViewHolder {
		ImageView channelIconImageView;
		TextView channelNum;
		TextView channelName;
		ImageView favoriteIcon;
//		View channelView;
		View deviceLine;
//		TextView channelIconTextView;
		
		int tag;
	}
	
	public void setLastFavorite(int lastFavorite){
    	mLastFavorite = lastFavorite;
    }
	
	public void setLastTvIndex(int index){
		mLastTvIndex = index;
	}

	public int getCount() {
		return Integer.MAX_VALUE - 1000000;
	}

	public Object getItem(int position) {
		position = position % list.size();
		return list==null?null:list.get(position);
	}

	public long getItemId(int position) {
		return position%list.size();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null|| convertView.getTag()==null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.menu_channel_list_item,null);
//			viewHolder.channelView = convertView.findViewById(R.id.menu_clitem_channel_info);
			viewHolder.channelNum = (TextView) convertView.findViewById(R.id.menu_clitem_channel_num);
			viewHolder.channelName = (TextView) convertView.findViewById(R.id.menu_clitem_channel_name);
			viewHolder.channelIconImageView = (ImageView) convertView.findViewById(R.id.menu_clitem_channel_icon);
			viewHolder.favoriteIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_favorite_icon);
			viewHolder.deviceLine = convertView.findViewById(R.id.devide_line);
//			viewHolder.channelIconTextView = (TextView) convertView.findViewById(R.id.menu_clitem_channel_icon_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		int index = position%list.size();
		long start = SystemClock.uptimeMillis();
 		DvbService service = mViewController.getChannelByNativeIndex(list.get(index));
 		int serviceId = service.getServiceId();
 		log.D("耗时 getView getChannelByNativeIndex  "+(SystemClock.uptimeMillis()-start));
 		viewHolder.favoriteIcon.setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES && index <= mLastFavorite?View.VISIBLE:View.INVISIBLE);	
 		viewHolder.channelNum.setText(String.valueOf(service.getLogicChNumber()));
 		viewHolder.channelName.setText(service.getChannelName());
// 		viewHolder.channelIconTextView.setText(service.getLogicChNumber()+"");
 		if(index == list.size()-1 || index == mLastFavorite || index == mLastTvIndex){
 			viewHolder.deviceLine.setVisibility(View.VISIBLE);
 		}else{
 			viewHolder.deviceLine.setVisibility(View.INVISIBLE);
 		}
 		log.D(">>> position="+index +"   "+service.getChannelName());
 		Bitmap bitmap = getBitmap(serviceId);
		if (bitmap != null) {
			log.D("bitmap != null ");
			viewHolder.channelIconImageView.setImageBitmap(bitmap);
			viewHolder.channelIconImageView.setVisibility(View.VISIBLE);
			viewHolder.channelNum.setVisibility(View.VISIBLE);
//			viewHolder.channelIconTextView.setVisibility(View.GONE);
		}else{
			viewHolder.channelIconImageView.setImageResource(R.drawable.default_icon);
			log.D("bitmap == null , use default icon .");
//			if(index<mChannelList.size()){
//				Log.d("songwenxuan","index<mTvConut  index = " + index + "mTvCount = " + mChannelList.size());
//				viewHolder.channelIcon.setImageResource(R.drawable.default_icon);
//			}else{
//				Log.d("songwenxuan","index >= mTvConut");
//				viewHolder.channelIcon.setImageResource(R.drawable.default_bc_icon);
//			}
//		    viewHolder.channelNum.setVisibility(View.GONE);
//		    viewHolder.channelIconImageView.setVisibility(View.GONE);
//		    viewHolder.channelIconTextView.setBackgroundResource(R.drawable.default_icon);
//		    viewHolder.channelIconTextView.setVisibility(View.VISIBLE);
			if((service.getServiceType() & 0x01) == ServiceType.TV){
			}else{
//				viewHolder.channel_icon.setBackgroundResource(R.drawable.default_bc_icon);
//				viewHolder.channel_icon.setText("");
			}
		}
		if(parent.getChildAt(0)!=null){
			ViewHolder h = (ViewHolder) parent.getChildAt(0).getTag();
			String name = h.channelName.getText().toString();
			Log.d("xubin", "childAt0 = "+name);
		}
		
		return convertView;
	}
	
	
	/**=====================================*/
	private final static SparseArray<SoftReference<Bitmap>> mIconCache = new SparseArray<SoftReference<Bitmap>>();
	private Bitmap getBitmap(int serviceId) {
		long start = SystemClock.uptimeMillis();
		SoftReference<Bitmap> value = mIconCache.get(serviceId);
		Bitmap bitmap = null;
		if (value != null){
			bitmap = value.get();
		}
		if (bitmap != null) {
			return bitmap;
		}
		String iconPath = mViewController.getChannelIconPath(serviceId);
		log.D("耗时 native iconPath " + (SystemClock.uptimeMillis() - start)+ " " + iconPath);
//		iconPath = "/data/data/com.joysee.adtv/test.png";
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
