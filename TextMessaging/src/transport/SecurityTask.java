package transport;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class SecurityTask extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	protected void onPostExecute(String result) {		
		Toast.makeText(getBaseContext(),"Registration Succesful",Toast.LENGTH_LONG).show();
		
		
		Toast.makeText(getBaseContext(),jObj.get("error").toString(),Toast.LENGTH_LONG).show(); 
		Intent intent = new Intent(Class1.this,Error.class);
		Class1.this.startActivity(intent);   

    }
}
