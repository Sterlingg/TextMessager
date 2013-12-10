/**
 * 
 */
package transport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.globex.textmessaging.SMS.SMSMessage;
import com.globex.textmessaging.SMS.SMSType;

import crypto.CryptKeeper;

/**
 * 
 * Types of packets:
 * 					'NewMessage': Represents a newly received text message. 
 * 					'Inbox'     : Represents the phone's initial inbox status.
 * 					'Outbox'    : Represents the phone's initial outbox status. 
 * @author sterling
 *
 */
public class Packetizer {
	
	/**
	 * @param s
	 * @return
	 */
	public static String packetize(String s){
		CryptKeeper ck = CryptKeeper.getInstance();
		byte[] dataToSend = ck.encrypt(s);
		String base64Data = Base64.encodeToString(dataToSend, Base64.DEFAULT); 
		
		s = String.format("%08d", base64Data.length()) + base64Data;
		return s;
	}
	
	public static String packetize(SMSMessage[] messages){
		
		JSONObject jsonWrapper = new JSONObject();
		JSONArray jsonInboxMessages = new JSONArray();
		JSONArray jsonOutboxMessages = new JSONArray();
		JSONArray jsonReceivedMessages = new JSONArray();
		
		SMSType type = null;
		for(int i = 0; i < messages.length; i++){
			type = messages[i].getType(); 
			
			switch(type)
	        	{
	        	case INBOX:
	        		jsonInboxMessages.put(messages[i].toJSON());
					break;
	        	case OUTBOX:
	        		jsonOutboxMessages.put(messages[i].toJSON());
	        		break;
				case RECEIVED:
					jsonReceivedMessages.put(messages[i].toJSON());
					break;
	        	}
		}

	        try {
	        	if(jsonInboxMessages.length() > 0)
	        		jsonWrapper.put("Inbox", jsonInboxMessages);
	        	
	        	if(jsonOutboxMessages.length() > 0)
	        		jsonWrapper.put("Outbox", jsonOutboxMessages);
	        	
	        	if(jsonReceivedMessages.length() > 0)
	        		jsonWrapper.put("Received", jsonReceivedMessages);
	        	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}	            
	        return packetize(jsonWrapper.toString());
	}
}
