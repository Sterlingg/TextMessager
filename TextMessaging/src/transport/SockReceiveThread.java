package transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;

import com.globex.textmessaging.SMS.SMSSender;

import android.os.AsyncTask;
import android.util.Log;

public class SockReceiveThread implements Runnable {
	Socket sock;
	DataInputStream dis;
	ServerSocket ss; 

	@Override
	public void run() {
		SMSSender sender = new SMSSender();

		try {
			ss = new ServerSocket(9003);
			sock = ss.accept();
			Log.d("Received Data", "Accepted?");
			dis = new DataInputStream(
					sock.getInputStream());
			while(true){
				byte[] packetLenBuf= new byte[8];

				dis.readFully(packetLenBuf);

				int len = Integer.parseInt(new String(packetLenBuf));
				byte[] data = new byte[len];
												
				if (len > 0) {
					dis.readFully(data);
				}
				Log.d("Received Data", "Publishing progress");
				Log.d("Received Data", new String(data));
				sender.sendMessage(new JSONObject(new String(data)));				
			}
			//dis.close();
			//sock.close();
		} catch (Exception e) {
			Log.d("Received Data", "Catched you.");
			e.printStackTrace();
		}
		
	}

}
