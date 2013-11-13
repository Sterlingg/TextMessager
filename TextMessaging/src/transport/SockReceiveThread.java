package transport;

import java.io.DataInputStream;
import java.net.Socket;

import org.json.JSONObject;

import android.os.AsyncTask;

public class SockReceiveThread extends AsyncTask<Void, JSONObject, Void> {

	@Override
	protected Void doInBackground(Void... params) {
		try {
			Socket sock = new Socket("127.0.0.1", 9002);

			DataInputStream dis = new DataInputStream(
					sock.getInputStream());
			while(true){
				byte[] packetLenBuf= new byte[8];

				dis.readFully(packetLenBuf);

				int len = Integer.parseInt(new String(packetLenBuf));
				byte[] data = new byte[len];
												
				if (len > 0) {
					dis.readFully(data);
				}
				else
					break;

				publishProgress(new JSONObject(new String(data)));   
			}
			dis.close();
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


	@Override
	protected void onProgressUpdate(JSONObject ... values) {

	}

}
