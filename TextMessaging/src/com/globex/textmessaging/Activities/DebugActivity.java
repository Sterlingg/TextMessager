package com.globex.textmessaging.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.globex.textmessaging.R;
import com.globex.textmessaging.transport.ConnectTask;

/**
 * @author sterling
 *
 */
public class DebugActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
               
        (new ConnectTask()).execute(this);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}