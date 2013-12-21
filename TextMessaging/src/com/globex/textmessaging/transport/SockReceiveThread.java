package com.globex.textmessaging.transport;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.globex.textmessaging.Activities.Step1Activity;
import com.globex.textmessaging.SMS.SMSSender;
import com.globex.textmessaging.crypto.CryptKeeper;


public class SockReceiveThread implements Runnable {
	private CryptKeeper ck = null;
	private Context context = null;
	private SocketHandler sockHandler;

	public SockReceiveThread(Context context){
		this.ck = CryptKeeper.getInstance();		
		this.context = context;
	}
	
	@Override
	public void run() {
		SMSSender sender = new SMSSender();

		try {
			this.sockHandler = SocketHandler.getInstance();

				byte[] data;
				byte[] decryptedData;
			while(true){
				byte[] b64data = this.sockHandler.receive();
				
				// Go back to the beginning if the socket gets closed.
				if(b64data == null){		
					Intent intent = new Intent(context, 
							Step1Activity.class);
					
					context.startActivity(intent);
					((Activity)context).finish();
					sockHandler.closeSocket();
					break;
				}
				data = Base64.decode(new String(b64data), Base64.DEFAULT);
				decryptedData = ck.decrypt(data);
				Log.d("SockReceiveThread", "Received: " + new String(decryptedData));
				sender.sendMessage(new JSONObject(new String(decryptedData)));				
			}
		} catch (Exception e) {
			Log.d("Received Data", "Catched you..");
			e.printStackTrace();
		}
		
	}

}
