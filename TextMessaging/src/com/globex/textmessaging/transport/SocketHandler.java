package com.globex.textmessaging.transport;

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
	private InputStream is = null;
	private OutputStream os = null;
	private Socket sock = null;
	private static boolean sockOpen = false;

	public SocketHandler(Socket sock){
		try {
			this.is = sock.getInputStream();
			this.os = sock.getOutputStream();		
			this.sock = sock;
			sockOpen = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SocketHandler getInstance() {
		if( instance == null ){
			throw new Error("Call initSocket first.");
		}
		return instance;
	}
	
	public static void initSocket(Socket sock) {
		if( instance == null ){
			instance = new SocketHandler(sock);
		}
		else{
			try {
				instance.is = sock.getInputStream();
				instance.os = sock.getOutputStream();
				instance.sock = sock;
				sockOpen = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
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

	public void closeSocket() {
		try {
			sock.close();
			sockOpen = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public static boolean isConnected() {
		if(sockOpen == true){
			return true;
		}
		else{
			return false;
		}
	}
}
