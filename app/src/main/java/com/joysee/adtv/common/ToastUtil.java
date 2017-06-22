package com.joysee.adtv.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.R;

/**
 * 弹出toast提示工具类
 * @author songwenxuan
 */
public class ToastUtil {
/**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param sum
     */
    public static void showToast(Context context,int sum){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(tview);
        text.setText(sum);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    
    /**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param str
     */
    public static void showToast(Context context,String str){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(tview);
        text.setText(str);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 用于生成一个PopupWindow 供弹出提示用
     * @param context
     * @param str
     * @return
     */
    public static PopupWindow showPopToast(Context context,String str){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        text.setText(str);
        PopupWindow pop = new PopupWindow(tview);
        pop.setWidth(716);
        pop.setHeight(173);
        pop.setFocusable(false);
        return pop;
    }

	
	public static void showMessage(Context context, int resid) {
		showMessage(context, context.getResources().getString(resid));
	}
	
	public static void showMessage(Context context, String msg) {
		showMessage(context, msg, Toast.LENGTH_LONG);
	}
	
	public static void showMessage(Context context, int resid, int duration) {
		showMessage(context, context.getResources().getString(resid), duration);
	}
	
	public static void showMessage(Context context, String msg, int duration) {
		Toast toast = Toast.makeText(context, msg, duration);
		
		final LayoutInflater inflater = LayoutInflater.from(context);
		final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.launcher_notify_message_layout, null);
		TextView msgView = (TextView) layout.findViewById(R.id.launcher_nofity_message_layout_title);
		msgView.setText(msg);
		toast.setView(layout);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
