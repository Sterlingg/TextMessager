package com.globex.textmessaging.SMS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * <p>
 * @author      Sterling Graham
 * @version     %I%, %G%
 * @since       1.0
 */
public class SMSReader {

	private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	
	//TODO: CLOSE CURSOR SOMEWHERE!
	private Cursor inboxCursor = null;
	private SMSMessage[] inboxMessages = null;
	
	private static int DB_ADDRESS;
	private static int DB_DATE;
	private static int DB_PERSON;
	private static int DB_BODY;
	
	 /** 
     * <p>
     *
     * @param context 
     * @since 1.0
     */
	public SMSReader(Context context){			
        this.inboxCursor = context.getContentResolver().query(
                SMS_INBOX_URI,
      new String[] { "_id", "thread_id", "address", "person", "date", "body" },
                null,
                null,
                null);
        
        DB_ADDRESS = inboxCursor.getColumnIndex("address");
    	DB_DATE = inboxCursor.getColumnIndex("date");
    	DB_PERSON = inboxCursor.getColumnIndex("person");
    	DB_BODY = inboxCursor.getColumnIndex("body");
	}
	
	
	/**
	 * @return
	 */
	public SMSMessage[] getInboxMessages(){
		if(inboxMessages == null)
		{	  
			int numOfMessages = inboxCursor.getCount();
			inboxMessages = new SMSMessage[numOfMessages];
						
			if(inboxCursor.moveToFirst())
			{						
				for(int i = 0; i < numOfMessages; i++)
				{	
					inboxMessages[i] = new SMSMessage(
							inboxCursor.getString(DB_ADDRESS),
							inboxCursor.getString(DB_DATE),
							inboxCursor.getString(DB_PERSON),
							inboxCursor.getString(DB_BODY),
							SMSType.INBOX
							);
					inboxCursor.moveToNext();
				}  	    	  
			}
		}
		
		return inboxMessages;
	}	
	
}
