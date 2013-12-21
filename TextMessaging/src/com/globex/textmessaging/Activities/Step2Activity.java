package com.globex.textmessaging.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.globex.textmessaging.R;
import com.globex.textmessaging.crypto.CryptKeeper;


public class Step2Activity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_step2);
		TextView passwordText = (TextView)this.findViewById(R.id.s2_password);	
		CryptKeeper ck = CryptKeeper.getInstance();
		ck.init();
		passwordText.setText(ck.getPassword());		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu;11 this adds items to th1e action bar if it is present.
		getMenuInflater().inflate(R.menu.step2, menu);
		return true;
	}

	public void startStep3(View v){ 		
		Intent intent = new Intent(this, 
				DebugActivity.class);
		
		this.startActivity(intent);
	    this.finish();
	}
}
