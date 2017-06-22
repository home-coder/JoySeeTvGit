package com.joysee.adtv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.joysee.adtv.aidl.email.IEmailService;
import com.joysee.adtv.logic.CaManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;

import java.util.ArrayList;
import java.util.List;

public class EmailService extends Service {
    public static final String TAG = "EmailService";
    private DVBPlayManager mDvbPlayManager;
    private CaManager mCaManager = CaManager.getCaManager();

    public void Log(String str) {
        Log.d(TAG, str);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log(" onBind " + intent.toString());
        if (mIBinder == null) {
            Log.e(TAG, " onBind mIBinder is null!!!!!!!!!!!");
        }else{
            mDvbPlayManager = DVBPlayManager.getInstance(getApplicationContext());
            mDvbPlayManager.init();
        }
        return mIBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        mDvbPlayManager.uninit();
        return super.onUnbind(intent);
    }
    
    private final IEmailService.Stub mIBinder = new IEmailService.Stub() {
        @Override
        public int getEmailUsedSpace() throws RemoteException {
            return mCaManager.nativeGetEmailUsedSpace();
        }

        @Override
        public int getEmailIdleSpace() throws RemoteException {
            return mCaManager.nativeGetEmailIdleSpace();
        }

        @Override
        public String getEmailContent(int id) throws RemoteException {
            EmailContent content = new EmailContent();
            int success = mCaManager.nativeGetEmailContent(id, content);
            Log(" getEmailContent back = " + success);
            return content.getEmailContent();
        }

        @Override
        public List<EmailHead> getEmailHeads() throws RemoteException {
            ArrayList<EmailHead> EmailHeadList = new ArrayList<EmailHead>();
            int getHeadBack = mCaManager
                    .nativeGetEmailHeads(EmailHeadList);
            Log("---- get Email header back = " + getHeadBack + " EmailHeadList size = "
                    + EmailHeadList.size());
            for (EmailHead emailHead : EmailHeadList) {
                Log.d(TAG, " emailHead " + emailHead.toString());
            }
            return EmailHeadList;
        }

        @Override
        public int DelEmailByID(int id) throws RemoteException {
            return mCaManager.nativeDelEmail(id);
        }
    };
}
