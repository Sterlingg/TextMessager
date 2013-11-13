package com.globex.textmessaging.SMS;
import transport.SendTask;
import transport.NetInfo;
import transport.Packetizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * @author sterling
 *
 */
public class SMSReceiver extends BroadcastReceiver {

	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    //private static final String LOG_TAG = "SMSReceiver";
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals(SMS_RECEIVED)) {
			Bundle bundle = intent.getExtras(); 
        		if(bundle != null){
        			Object[] pdus = (Object[])bundle.get("pdus");
        			final SmsMessage[] messages = new SmsMessage[pdus.length];       
        			for (int i = 0; i < pdus.length; i++) {
        				messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
        			}
  
        			SMSMessage[] myMessages = convertToMySMS(messages); 
        			Log.i("SMSReceiver", "Messages length = " + myMessages.length);
        			Log.i("SMSReceiver", "Message value: " + myMessages[0]);
        			(new SendTask()).execute(Packetizer.packetize(myMessages),
        					NetInfo.getIp()
        					,String.valueOf(NetInfo.getPort()));
        		}      		         	
        }		
	}
	
	/**
	 * @param messages
	 * @return
	 */
	private SMSMessage[] convertToMySMS(SmsMessage[] messages){
		
		SMSMessage[] real_messages = new SMSMessage[messages.length];
		
		Log.i("SMSReceiver", "Messages length = " + messages.length);
		for (int i = 0; i < messages.length; i++ ){
			SmsMessage sms = messages[i];
			SMSMessage message = new SMSMessage(sms.getOriginatingAddress()
				, String.valueOf(sms.getTimestampMillis())
				, String.valueOf(sms.getIndexOnIcc())
				, sms.getMessageBody()
				, SMSType.RECEIVED
					);
			real_messages[i] = message;
		}
		return real_messages;
	}	

}
