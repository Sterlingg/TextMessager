package com.globex.textmessaging.Activities;

import transport.SecurityTask;


import com.globex.textmessaging.R;

import android.os.Bundle;
import android.view.Menu;
import android.app.Activity;


public class SecurityInitActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_security_init);
		 (new SecurityTask(this)).execute(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.security_init, menu);
		return true;
	}

}
