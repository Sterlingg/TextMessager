package transport;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SocketUtility {

	
	// Used when first establishing the connection, before starting the reading thread.
	public static byte[] receive(InputStream is){

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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		

	}
	
	public static void send(OutputStream os, String s){
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
	
	public static void send(OutputStream sockOut, byte[] bytes){
		try{
		    DataOutputStream dos = new DataOutputStream(sockOut);
		    dos.writeBytes(String.format("%08d", bytes.length));
		    if (bytes.length > 0) {
		        dos.write(bytes, 0, bytes.length);
		    }
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
