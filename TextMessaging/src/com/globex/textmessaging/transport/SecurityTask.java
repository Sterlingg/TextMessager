package com.globex.textmessaging.transport;

import com.globex.textmessaging.SMS.SMSMessage;
import com.globex.textmessaging.SMS.SMSReader;
import com.globex.textmessaging.crypto.CryptKeeper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class SecurityTask extends AsyncTask<Void, Void, Void> {

	private Context mainContext = null;
	private SocketHandler sockHandler = null;
	
	public SecurityTask(Context mainContext){
		super();
		this.mainContext = mainContext; 
	}
	@Override
	protected Void doInBackground(Void ... params) {
		try {
			this.sockHandler = SocketHandler.getInstance();
			CryptKeeper ck = CryptKeeper.getInstance();			
			
			String salt = Base64.encodeToString(ck.getSalt(), Base64.DEFAULT);
			this.sockHandler.sendWithLength(salt);
			
			Log.d("SecurityTask", "Sent salt! "
					+ new String(salt));
			
			String iv = Base64.encodeToString(ck.getIV(), Base64.DEFAULT);
			this.sockHandler.sendWithLength(iv);
			Log.d("SecurityTask", "Sent IV! "
					+ new String(iv));
			
			byte[] data = this.sockHandler.receive();

			Log.d("SecurityTask", "Received"
					+ new String(ck.decrypt(data)));
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

		Thread thr = new Thread(new SockReceiveThread(mainContext));
		thr.start();
		
        SMSMessage messages[] = (new SMSReader(mainContext)).getInboxMessages();
        Log.i("DebugActivity", "Sending Inbox" + Packetizer.packetize(messages));
        (new SendTask()).execute(Packetizer.packetize(messages)
        		,NetInfo.getIp()
        		,String.valueOf(NetInfo.getPort()));    
	}
}
