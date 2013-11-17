package com.globex.textmessaging.SMS;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author sterling
 *
 */
public class SMSMessage {
	
	private String address;
	private String date;
	private String person;
	private String body;	
	private SMSType type;
	
	/**
	 * @return the type
	 */
	public SMSType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(SMSType type) {
		this.type = type;
	}

	/**
	 * @param address
	 * @param date
	 * @param person
	 * @param body
	 */
	public SMSMessage(String address, String date, String person, String body, SMSType type) {
		this.address = address;
		this.date = date;
		this.person = person;
		this.body  = body;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SMSMessage [address=" + address + ", date=" + date
				+ ", person=" + person + ", body=" + body + "]";
	}
	
	/**
	 * @return
	 */
	public JSONObject toJSON(){
		
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("address", address);
			jsonObj.put("date", date);
			jsonObj.put("person", person);
			jsonObj.put("body", body);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return jsonObj;
	}	
}
