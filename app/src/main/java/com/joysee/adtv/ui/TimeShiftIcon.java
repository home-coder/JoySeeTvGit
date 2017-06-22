
package com.joysee.adtv.ui;

import com.joysee.adtv.activity.DvbMainActivity;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;

public class TimeShiftIcon implements IDvbBaseView {
    private DvbLog log = new DvbLog("TimeShiftIcon", DvbLog.DebugType.D);
    private DvbMainActivity mActivity;

    public TimeShiftIcon(DvbMainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void processMessage(Object sender, DvbMessage msg) {
        log.D(" processMessage " + msg.toString());
        switch (msg.what) {
            case ViewMessage.SWITCH_CHANNEL:
            case ViewMessage.START_PLAY_TV:
                if (mActivity != null) {
                    mActivity.setTimeShiftImg();
                }
                break;
        }
    }
}
