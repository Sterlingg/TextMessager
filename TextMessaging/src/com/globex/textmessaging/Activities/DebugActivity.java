package com.globex.textmessaging.Activities;

import transport.SendTask;
import transport.SockReceiveThread;
import transport.NetInfo;
import transport.Packetizer;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.globex.textmessaging.R;
import com.globex.textmessaging.SMS.SMSMessage;
import com.globex.textmessaging.SMS.SMSReader;


/**
 * @author sterling
 *
 */
public class DebugActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SMSMessage messages[] = (new SMSReader(this)).getInboxMessages();

        String toSend = Packetizer.packetize(messages);
 
        ((EditText)findViewById(R.id.ip_field1)).setText("192.168.1.144");
        
        NetInfo.setIp(((EditText)findViewById(R.id.ip_field1)).getText().toString());
        
        (new SendTask()).execute(toSend
        		,NetInfo.getIp()
        		,String.valueOf(NetInfo.getPort())); 
        Thread thr = new Thread(new SockReceiveThread());
        thr.start();
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}