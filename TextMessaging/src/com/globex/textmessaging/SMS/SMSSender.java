package com.globex.textmessaging.SMS;

import org.json.JSONException;
import org.json.JSONObject;

import android.telephony.SmsManager;

public class SMSSender {
	SmsManager smsManager = null;
	
	public SMSSender(){
		smsManager = SmsManager.getDefault();						
	}
	
	public void sendMessage(JSONObject message){
		String phoneNumber;
		try {
			phoneNumber = message.getString("PhoneNumber");
			String messageBody = message.getString("Body");
			smsManager.sendTextMessage(phoneNumber, null, messageBody, null, null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
