package transport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import crypto.CryptKeeper;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author sterling
 *
 */
public class SendTask extends AsyncTask<String, Void, Void>{

	@Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

	@Override
	protected Void doInBackground(String... params) {
	    
        try {
			Socket sock = new Socket(params[1],Integer.parseInt(params[2]));
			
			OutputStream os = sock.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
            
            Log.i("ConnectionTask", "Sending!" + Packetizer.packetize(params[0]));
            out.write(Packetizer.packetize(params[0]));
            
            out.flush();
            
            sock.close();
            
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return null;
	}
}