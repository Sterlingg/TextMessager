package transport;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.globex.textmessaging.SMS.SMSSender;

import crypto.CryptKeeper;

import android.util.Base64;
import android.util.Log;

public class SockReceiveThread implements Runnable {
	private Socket sock;
	private DataInputStream dis;
	private ServerSocket ss;
	private CryptKeeper ck = null;

	public SockReceiveThread(){
		this.ck = CryptKeeper.getInstance();		
	}
	
	@Override
	public void run() {
		SMSSender sender = new SMSSender();

		try {
			Log.d("SockReceiveThread", "Not accepted.!");
			ss = new ServerSocket(9003);
			sock = ss.accept();
			Log.d("SockReceiveThread", "Accepted!!!?!!!@!1!!");
			dis = new DataInputStream(
					sock.getInputStream());
			while(true){
				byte[] packetLenBuf= new byte[8];

				dis.readFully(packetLenBuf);

				int len = Integer.parseInt(new String(packetLenBuf));
				byte[] b64data = new byte[len];
												
				if (len > 0) {
					dis.readFully(b64data);
				}
				
				byte[] data = Base64.decode(new String(b64data), Base64.DEFAULT);
				byte[] decryptedData = ck.decrypt(data);
				Log.d("Received Data", "Publishing progress!1!");
				Log.d("Received Data", new String(decryptedData));
				sender.sendMessage(new JSONObject(new String(decryptedData)));				
			}
			//dis.close();
			//sock.close();
		} catch (Exception e) {
			Log.d("Received Data", "Catched you..");
			e.printStackTrace();
		}
		
	}

}
