package transport;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author sterling
 *
 */
public class SendTask extends AsyncTask<String, Void, Void>{

	private SocketHandler sockHandler;

	@Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

	@Override
	protected Void doInBackground(String... params) {
	    
			this.sockHandler = SocketHandler.getInstance();
			
            Log.i("ConnectionTask", "Sending!" + params[0]);
            this.sockHandler.send(params[0]);
	
		return null;
	}
}