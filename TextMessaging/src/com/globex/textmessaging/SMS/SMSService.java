package com.globex.textmessaging.SMS;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author sterling
 *
 */
public class SMSService extends Service{
    // Binder given to clients
    private final IBinder mBinder = new SMSBinder();
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class SMSBinder extends Binder {
        SMSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SMSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
