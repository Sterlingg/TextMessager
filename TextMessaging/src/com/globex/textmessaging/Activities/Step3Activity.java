package com.globex.textmessaging.Activities;

import transport.SecurityTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.globex.textmessaging.R;

public class Step3Activity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_step3);
		(new SecurityTask(this)).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.step3, menu);
		return true;
	}

	public void startCrypto(View button){

		Intent intent = new Intent(this, 
				DebugActivity.class);
		
		this.startActivity(intent);
	}
}
