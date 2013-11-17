package transport;

import crypto.CryptKeeper;
import transport.SocketUtility;

import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;


import com.globex.textmessaging.Activities.DebugActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class SecurityTask extends AsyncTask<Void, Void, Void> {

	private Context mainContext = null;
	private Activity mainActivity = null;

	public SecurityTask(Context mainContext){
		this.mainContext = mainContext;	
		if(mainContext instanceof Activity){
			this.mainActivity = (Activity)mainContext;
		}

	}
		
	private String getDeviceId(){
		
        final TelephonyManager tm = (TelephonyManager) 
        		mainActivity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(
        		mainActivity.getContentResolver(), 
        		android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        
        return deviceId;
	}
		
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			CryptKeeper ck = CryptKeeper.getInstance(getDeviceId());
			
			Socket sock = new Socket("10.0.2.2", 9006);

			OutputStream os = sock.getOutputStream();
			
			String salt = Base64.encodeToString(ck.get_salt(), Base64.DEFAULT);
			SocketUtility.send(os, salt);
			
			Log.d("SecurityTask", "Sent salt! "
					+ new String(salt));
			
			String iv = Base64.encodeToString(ck.get_iv(), Base64.DEFAULT);
			SocketUtility.send(os, iv);
			Log.d("SecurityTask", "Sent IV! "
					+ new String(iv));
			
			byte[] data = 
					SocketUtility.receive(sock.getInputStream());

			Log.d("SecurityTask", "Received" 
					+ new String(ck.decrypt(data)));
			sock.close();
		} catch (Exception e) {
			
			e.printStackTrace();
		}		
		
		return null;
	}		
	
	@Override
	protected void onPostExecute(Void result) {
		
		Log.d("SecurityTask", "Finished?????");
		Toast.makeText(mainContext,"Connection established."
				,Toast.LENGTH_LONG).show();		
		        
		Intent intent = new Intent(mainContext, 
				DebugActivity.class);
		
		mainContext.startActivity(intent);   
    }
}
