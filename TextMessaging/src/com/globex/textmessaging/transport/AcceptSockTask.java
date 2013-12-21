package com.globex.textmessaging.transport;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

public class AcceptSockTask extends AsyncTask<Context, Void, Void> {

	private Context context = null;	
	
	@Override
	protected Void doInBackground(Context... params) {
		Socket sock = null;
		try {
			sock = new Socket(NetInfo.getIp(), NetInfo.getPort());

			SocketHandler.initSocket(sock);
			context = params[0];
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected void onPostExecute(Void v){	
		 assert(context instanceof Activity);
		 (new SecurityTask(context)).execute();
	}
}
