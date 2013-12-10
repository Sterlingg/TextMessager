package transport;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import android.util.Log;

public class SocketHandler{
	
	private static SocketHandler instance = null;
//	private Socket sock = null;
	private InputStream is = null;
	private OutputStream os = null;  

	public SocketHandler(Socket sock){
	//	this.sock = sock;
		try {
			this.is = sock.getInputStream();
			this.os = sock.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SocketHandler getInstance() {
		if( instance == null ){
			throw new Error("Call init first.");
		}
		return instance;
	}
	
	public static void init(Socket sock){
		if( instance == null ){
				instance = new SocketHandler(sock);
		}
		else{
			// TODO: Proper error handling here.
			throw new Error();
		}
	}
	
	public byte[] receive(){
		DataInputStream dis = new DataInputStream(
				is);
		
		byte[] packetLenBuf= new byte[8];

		try {
			dis.readFully(packetLenBuf);
			int len = Integer.parseInt(new String(packetLenBuf));
			byte[] data = new byte[len];
											
			if (len > 0) {
				dis.readFully(data);
			}
			
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch bl
			e.printStackTrace();
			return null;
		}		
	}
	
	public void send(String s){
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
            //OutputStreamWriter out = new OutputStreamWriter(os);
            out.write(s);
            Log.i("SocketHandler", "Sending: " + s);
            out.flush();
           // out.close(
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void send(byte[] bytes){
		try{
		    DataOutputStream dos = new DataOutputStream(os);
		    dos.writeBytes(String.format("%08d", bytes.length));
		    if (bytes.length > 0) {
		        dos.write(bytes, 0, bytes.length);
		    }
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void sendWithLength(String s) {
	       try{
	    	   BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

	    	   out.write(
	    			   String.format("%08d", s.getBytes().length) + s);
	    	   out.flush();
	       }
	       catch(IOException e){
	    	   e.printStackTrace();
	       }
	}


}
