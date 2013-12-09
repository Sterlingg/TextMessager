package com.globex.textmessaging.Activities;

import transport.NetInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.globex.textmessaging.R;

public class Step1Activity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_step1);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.step1, menu);
		return true;
	}
	
	public void startStep2(View v){
		
		EditText ipTextBox = (EditText)findViewById(R.id.ip_box);
		Log.i("Step1Activity", ipTextBox.getText().toString());
		NetInfo.setIp(ipTextBox.getText().toString());
		
	    Intent intent = new Intent(this, Step2Activity.class);
	    startActivity(intent);
	}
}
