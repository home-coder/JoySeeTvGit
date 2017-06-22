package com.joysee.adtv.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.joysee.adtv.R;

import android.app.Dialog;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DvbUtil {
	
	private static final String TAG = "DvbUtil";
	public static final String axis = "/sys/class/video/axis";
	public static final String mCurrentResolution = "/sys/class/display/mode";
	public static String getCurrentOutputResolution() {
        String currentMode = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(mCurrentResolution);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(fileReader);
        try {
            currentMode = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentMode;
    }
		
	private final static String sel_480ioutput_x = "ubootenv.var.480ioutputx";
    private final static String sel_480ioutput_y = "ubootenv.var.480ioutputy";
    private final static String sel_480ioutput_width = "ubootenv.var.480ioutputwidth";
    private final static String sel_480ioutput_height = "ubootenv.var.480ioutputheight";
    private final static String sel_480poutput_x = "ubootenv.var.480poutputx";
    private final static String sel_480poutput_y = "ubootenv.var.480poutputy";
    private final static String sel_480poutput_width = "ubootenv.var.480poutputwidth";
    private final static String sel_480poutput_height = "ubootenv.var.480poutputheight";
    private final static String sel_576ioutput_x = "ubootenv.var.576ioutputx";
    private final static String sel_576ioutput_y = "ubootenv.var.576ioutputy";
    private final static String sel_576ioutput_width = "ubootenv.var.576ioutputwidth";
    private final static String sel_576ioutput_height = "ubootenv.var.576ioutputheight";
    private final static String sel_576poutput_x = "ubootenv.var.576poutputx";
    private final static String sel_576poutput_y = "ubootenv.var.576poutputy";
    private final static String sel_576poutput_width = "ubootenv.var.576poutputwidth";
    private final static String sel_576poutput_height = "ubootenv.var.576poutputheight";
    private final static String sel_720poutput_x = "ubootenv.var.720poutputx";
    private final static String sel_720poutput_y = "ubootenv.var.720poutputy";
    private final static String sel_720poutput_width = "ubootenv.var.720poutputwidth";
    private final static String sel_720poutput_height = "ubootenv.var.720poutputheight";
    private final static String sel_1080ioutput_x = "ubootenv.var.1080ioutputx";
    private final static String sel_1080ioutput_y = "ubootenv.var.1080ioutputy";
    private final static String sel_1080ioutput_width = "ubootenv.var.1080ioutputwidth";
    private final static String sel_1080ioutput_height = "ubootenv.var.1080ioutputheight";
    private final static String sel_1080poutput_x = "ubootenv.var.1080poutputx";
    private final static String sel_1080poutput_y = "ubootenv.var.1080poutputy";
    private final static String sel_1080poutput_width = "ubootenv.var.1080poutputwidth";
    private final static String sel_1080poutput_height = "ubootenv.var.1080poutputheight";
    private final static String[] mOutputModeList = {
            "480i",  
            "480p",
            "576i",  
            "576p",         
            "720p",
            "1080i", 
            "1080p",
            "720p50hz",
            "1080i50hz",
            "1080p50hz"
    };
    
    public static int[] getPosition(String mode) {
        int[] curPosition = {
                0, 0, 1280, 720
        };
        int index = 4; // 720p
        for (int i = 0; i < mOutputModeList.length; i++) {
            if (mode.equalsIgnoreCase(mOutputModeList[i]))
                index = i;
        }
        switch (index) {
            case 0: // 480i
                curPosition[0] = SystemProperties.getInt(sel_480ioutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_480ioutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_480ioutput_width, 720);
                curPosition[3] = SystemProperties.getInt(sel_480ioutput_height, 480);
                break;
            case 1: // 480p
                curPosition[0] = SystemProperties.getInt(sel_480poutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_480poutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_480poutput_width, 720);
                curPosition[3] = SystemProperties.getInt(sel_480poutput_height, 480);
                break;
            case 2: // 576i
                curPosition[0] = SystemProperties.getInt(sel_576ioutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_576ioutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_576ioutput_width, 720);
                curPosition[3] = SystemProperties.getInt(sel_576ioutput_height, 576);
                break;
            case 3: // 576p
                curPosition[0] = SystemProperties.getInt(sel_576poutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_576poutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_576poutput_width, 720);
                curPosition[3] = SystemProperties.getInt(sel_576poutput_height, 576);
                break;
            case 4: // 720p
            case 7:
                curPosition[0] = SystemProperties.getInt(sel_720poutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_720poutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_720poutput_width, 1280);
                curPosition[3] = SystemProperties.getInt(sel_720poutput_height, 720);
                break;
            case 5: // 1080i
            case 8:
                curPosition[0] = SystemProperties.getInt(sel_1080ioutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_1080ioutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_1080ioutput_width, 1920);
                curPosition[3] = SystemProperties.getInt(sel_1080ioutput_height, 1080);
                break;
            case 6: // 1080p
            case 9:
                curPosition[0] = SystemProperties.getInt(sel_1080poutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_1080poutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_1080poutput_width, 1920);
                curPosition[3] = SystemProperties.getInt(sel_1080poutput_height, 1080);
                break;
            default: // 720p
                curPosition[0] = SystemProperties.getInt(sel_720poutput_x, 0);
                curPosition[1] = SystemProperties.getInt(sel_720poutput_y, 0);
                curPosition[2] = SystemProperties.getInt(sel_720poutput_width, 1280);
                curPosition[3] = SystemProperties.getInt(sel_720poutput_height, 720);
                break;
        }
        return curPosition;
    }
	
    public static void writeFile(String file, String value) {
        File OutputFile = new File(file);
        if (!OutputFile.exists()) {
            File tfile = new File(file);
            try {
                tfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            FileUtils.setPermissions(file, 0777, -1, -1);
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(OutputFile), 32);
            try {
                Log.d(TAG, "set" + file + ": " + value);
                out.write(value);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException when write " + OutputFile,e);
        }
    }
    
    /**
	 * @param true 开deinterlace 隔行
	 */
    private static final String Filemap= "/sys/class/vfm/map";
    private static final String File_amvdec_mpeg12 = "/sys/module/amvdec_mpeg12/parameters/dec_control";
    private static final String File_amvdec_h264 = "/sys/module/amvdec_h264/parameters/dec_control";
	public static void switchDeinterlace(boolean enable) {
//	    boolean deinterlaceEnable = false;
//	      String mapValue = readSysfs(Filemap);
//	      Log.d(TAG,"switchDeinterlace mapValue = " + mapValue);
//	      if (mapValue.contains("deinterlace")) {
//	         deinterlaceEnable = true;
//	      }
//	      if (deinterlaceEnable == enable) {
//	    	  Log.d(TAG,"deinterlaceEnable == enable, return;");
//	         return;
//	      }
//	    if(enable){
//	        writeFile(Filemap, "rm default decoder ppmgr amvideo");
//            writeFile(Filemap, "rm default_osd osd amvideo");
//            writeFile(Filemap, "rm default_ext vdin amvideo2");
//            writeFile(Filemap, "add default decoder deinterlace amvideo");
//            writeFile(File_amvdec_h264, "3");
//            writeFile(File_amvdec_mpeg12, "14");
//	    }else{
//	        writeFile(Filemap, "rm default decoder deinterlace amvideo");
//            writeFile(Filemap, "add default decoder ppmgr amvideo");
//            writeFile(Filemap, "add default_osd osd amvideo");
//            writeFile(Filemap, "add default_ext vdin amvideo2");
//            writeFile(File_amvdec_h264, "0");
//            writeFile(File_amvdec_mpeg12, "0"); 
//	    }
	}
	
	private static String readSysfs(String path) {
        if (!new File(path).exists()) {
            Log.e(TAG, "File not found: " + path);
            return "";
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path), 62);
            try {
                return reader.readLine();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception when write: " + path, e);
        }
        return "";
    }
	
	private static final String PpscalerFile= "/sys/class/ppmgr/ppscaler";
    private static final String PpscalerRectFile= "/sys/class/ppmgr/ppscaler_rect";
    private static final String FreescaleFb0File = "/sys/class/graphics/fb0/free_scale";
    private static final String FreescaleFb1File = "/sys/class/graphics/fb1/free_scale";
    private static final String OutputModeFile= "/sys/class/display/mode";
    private static final String request2XScaleFile = "/sys/class/graphics/fb0/request2XScale";
    private static final String scaleAxisOsd1File = "/sys/class/graphics/fb1/scale_axis";
    private static final String scaleOsd1File = "/sys/class/graphics/fb1/scale";
    private static final String blankFb0File = "/sys/class/graphics/fb0/blank";
//    private final String axis = "/sys/class/video/axis";
    public static synchronized void setRealVideoOnOff(boolean on){
        if(on){
            Log.d(TAG,"setRealVideoOnOff on = " + on);
            boolean realVideoOn = false;
            String mapValue = readSysfs(FreescaleFb0File);
            Log.d(TAG,"setRealVideoOnOff mapValue = " + mapValue);
            if (mapValue.contains("0x0")) {
               realVideoOn = true;
            }
            if (realVideoOn == on) {
            	Log.d(TAG,"setRealVideoOnOff == on, return;");
               return;
            }
            writeFile(blankFb0File, "1");        //surfaceflinger will set back to 0
            // String cur_mode = SystemProperties.get(STR_OUTPUT_VAR);
            String cur_mode = getCurrentOutputResolution();
            writeFile(PpscalerFile, "0");
            writeFile(FreescaleFb0File, "0");
            writeFile(FreescaleFb1File, "0");
            if((cur_mode.equals(mOutputModeList[0])) || 
              (cur_mode.equals(mOutputModeList[1]))){
                writeFile(request2XScaleFile, "16 720 480");
                writeFile(scaleAxisOsd1File, "1280 720 720 480");
                writeFile(scaleOsd1File, "0x10001");
            }
            else if((cur_mode.equals(mOutputModeList[2])) || 
                    (cur_mode.equals(mOutputModeList[3]))){
                writeFile(request2XScaleFile, "16 720 576");
                writeFile(scaleAxisOsd1File, "1280 720 720 576");
                writeFile(scaleOsd1File, "0x10001");
            }
            else if((cur_mode.equals(mOutputModeList[5])) || 
                    (cur_mode.equals(mOutputModeList[6])) || 
                    (cur_mode.equals(mOutputModeList[8])) || 
                    (cur_mode.equals(mOutputModeList[9]))){
                writeFile(request2XScaleFile, "8");
                writeFile(scaleAxisOsd1File, "1280 720 1920 1080");
                writeFile(scaleOsd1File, "0x10001");
            }
            else{
                writeFile(request2XScaleFile, "16 1280 720");    //for setting blank to 0
            }
        }else{
            int[] curPosition = {0, 0, 1280, 720};
            writeFile(blankFb0File, "1");        //surfaceflinger will set back to 0
            // String cur_mode = SystemProperties.get(STR_OUTPUT_VAR);
            String cur_mode = getCurrentOutputResolution();
            curPosition = getPosition(cur_mode);
//          writeFile(VideoAxisFile, "0 0 1280 720");
            writeFile(PpscalerFile, "1");
            writeFile(PpscalerRectFile,
                    curPosition[0] + " " +
                    curPosition[1] + " " +
                    (curPosition[2] + curPosition[0] - 1) + " " +
                    (curPosition[3] + curPosition[1] - 1) + " " + 0);
            writeFile(FreescaleFb0File, "1");
            writeFile(FreescaleFb1File, "1");
            writeFile(request2XScaleFile, "2");
            writeFile(scaleOsd1File, "0");
            writeFile(PpscalerRectFile,
                    curPosition[0] + " " +
                    curPosition[1] + " " +
                    (curPosition[2] + curPosition[0] - 1) + " " +
                    (curPosition[3] + curPosition[1] - 1) + " " + 0);
        }
    }
    
    public static void resetWindowSize() {
        try {
			String cur_mode = getCurrentOutputResolution();
			int[] curPosition = {
			        0, 0, 1280, 720
			};
			curPosition = getPosition(cur_mode);
			Log.d("songwenxuan",
			        "curPosition[0] = " + curPosition[0] +
			        " curPosition[1] = " + curPosition[1] +
			        " curPosition[2] = " + curPosition[2] +
			        " curPosition[3] = " + curPosition[3]);
			writeFile(axis,
			        curPosition[0] + " " +
			        curPosition[1] + " " +
			        (curPosition[2] + curPosition[0] - 1) + " " +
			        (curPosition[3] + curPosition[1] - 1) + " " + 0);
			String readSysfs = readSysfs(axis);
			Log.d("songwenxuan","read axis = " + readSysfs);
		} catch (Exception e) {
			Log.d(TAG,"resetWindowSize() throws an exception -- " + e.toString());
		}
    }
    private static View view;
    private static WindowManager wm;
    public static void showTransitionDialog(Context context){
//    	if(dialog == null){
//    		dialog = new Dialog(context.getApplicationContext(),R.style.alertDialogTheme);
//    	}
    	LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
    	if(view ==null)
    		view = inflater.inflate(R.layout.alert_dialog_no_button_layout, null);
		TextView textView = (TextView) view.findViewById(R.id.alert_text);
		textView.setText(R.string.timeshift_to_tv);
        wm = (WindowManager)context.getApplicationContext().getSystemService("window");  
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();  
        wmParams.type=2002;  //type是关键，这里的2002表示系统级窗口，你也可以试试2003。  
        wmParams.format=1;
        /** 
         *这里的flags也很关键 
         *代码实际是wmParams.flags |= FLAG_NOT_FOCUSABLE; 
         *40的由来是wmParams的默认属性（32）+ FLAG_NOT_FOCUSABLE（8） 
         */  
        wmParams.flags=40;
//		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
//		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
//		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        int width=(int) context.getApplicationContext().getResources().getDimension(R.dimen.alert_dialog_no_button_width);
        int height=(int) context.getApplicationContext().getResources().getDimension(R.dimen.alert_dialog_no_button_height);
//        dialog.setContentView(view,new LinearLayout.LayoutParams(width,height));
//        dialog.show();
        wmParams.width = width;
        wmParams.height = height;
        wm.addView(view, wmParams);//创建View
	}
    
    public static void dismissTransitionDialog(){
    	try {
    		if(view !=null)
    			wm.removeView(view);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
}
