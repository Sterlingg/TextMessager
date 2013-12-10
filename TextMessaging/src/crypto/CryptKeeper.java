package crypto;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class CryptKeeper {

        private static CryptKeeper instance = null;
        
        private static SecureRandom random = new SecureRandom();
        private static final int PASSWORD_LENGTH = 8;        
        private String password = "";
        
		private Cipher aesCipher;
        private SecretKeySpec aesKeySpec;
        private IvParameterSpec aesIv;
        private String device_id = "";
        
        private static final String KDF_ALG = "PBKDF2WithHmacSHA1";
        private static final int KDF_ITERATIONS = 1024;
        private static final int KEY_SIZE = 128;
        private byte[] kdf_salt = null;
        
		private static final String ENCRYPTION_ALG = "AES";
		private static final String ENCRYPTION_MODE = "CBC";
		private static final String PADDING = "ZeroBytePadding";
		
        private CryptKeeper() {
         		this.password = randomPassword();
        		pbkdf2Gen(this.password.toCharArray());
        		
        		System.out.println("Password:" + password);
        		
        		System.out.println("Key: " + 
        		Arrays.toString(aesKeySpec.getEncoded()));
        
    			aesIv = new IvParameterSpec(gen16ByteSalt());
        		        
        		try {
					aesCipher = Cipher.getInstance(ENCRYPTION_ALG + "/" 
								+ ENCRYPTION_MODE + "/"
								+ PADDING);
	        		aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec,
	        				aesIv);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}          	                
        }
             
    	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    	public static String bytesToHex(byte[] bytes) {
    	    char[] hexChars = new char[bytes.length * 2];
    	    int v;
    	    for ( int j = 0; j < bytes.length; j++ ) {
    	        v = bytes[j] & 0xFF;
    	        hexChars[j * 2] = hexArray[v >>> 4];
    	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    	    }
    	    return new String(hexChars);
    	}
                
        public byte[] decrypt(byte[] b)
        		throws NoSuchPaddingException,
        		InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        	byte[] decrypted = null;

        	try {
        		aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec, aesIv);
        	} catch (InvalidAlgorithmParameterException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	decrypted = aesCipher.doFinal(b);

        	return decrypted;
        }

        public byte[] encrypt(byte[] bs){
			// initialize the cipher for encrypt mode
			byte[] encrypted = null;

				try {
					aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, aesIv);
					encrypted = aesCipher.doFinal(bs);
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block!
					Log.e("CryptKeeper", "Invalid Key" + "Encrypting.");
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					Log.e("CryptKeeper", "Invalid Key" + "Encrypting.");

					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					Log.e("CryptKeeper", "Invalid Key" + "Encrypting.");

					e.printStackTrace();
				} catch (BadPaddingException e) {
					Log.e("CryptKeeper", "Invalid Key" + "Encrypting.");

					e.printStackTrace();
				}
                        					
			return encrypted;
		}

		public byte[] encrypt(String s){
			return encrypt(s.getBytes());
		}

		public byte[] gen8ByteSalt(){
            //IV.
            byte[] iv = new byte[8];
            random.nextBytes(iv);
            
            return iv;
		}
		
		public byte[] gen16ByteSalt(){
                //IV.
                byte[] iv = new byte[16];
                random.nextBytes(iv);
                
                return iv;
        }
		
		public String getDeviceId(){
			return this.device_id;
		}

		public byte[] getIV(){			
			return aesIv.getIV();
		}

		public CharSequence getPassword() {
			// TODO Auto-generated method stub
			return this.password;
		}

		public byte[] getSalt(){			
			return kdf_salt;
		}
		
		public static CryptKeeper getInstance() {
			// TODO Auto-generated method stub
			if (instance  == null)
			{
				instance = new CryptKeeper();
			}
			return instance;
		}        
		
		public static byte[] hexStringToByteArray(String s) {
		    int len = s.length();
		    byte[] data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;
		}
		
    	public static String padString(String s){
			int BLOCK_SIZE = 16;
			char PAD_VALUE = ' ';            		
		    int padSize = BLOCK_SIZE - (s.length() % BLOCK_SIZE);		
		    
		    Log.i("CryptKeeper", "String encryption length: " + s.length());
		    String padString = "";
		    
		    for (int i = 0; i < padSize; i++){
		    	padString = padString + PAD_VALUE;
		    }
		    
		    Log.i("CryptKeeper", "Padded length: " + (s + padString).length());           	
			return s + padString;
		}

		private void pbkdf2Gen(char[] password){

    			kdf_salt = gen16ByteSalt();
    			
    			SecretKeyFactory f;    			
    			try {
					f = SecretKeyFactory.getInstance(KDF_ALG);
					KeySpec ks = new PBEKeySpec(password, kdf_salt, KDF_ITERATIONS, KEY_SIZE);    	

	    			System.out.println(bytesToHex(f.generateSecret(ks).getEncoded()));
	    			aesKeySpec = new SecretKeySpec(
	    					bytesToHex(f.generateSecret(ks).getEncoded()).getBytes()
	    					, ENCRYPTION_ALG);
	    			aesIv = new IvParameterSpec("C111510372A7A003".getBytes());    			
		
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    			    	
    	}
    	
        /**
		 * Returns a random string of the desired length specified in the length argument. As well as
		 * sets the password to use for encryption.
		 */
		public String randomPassword(){
		password = randomString(PASSWORD_LENGTH);	
		
		return password;
		}
		
        /**
		 * Returns a random string of the desired length specified in the length argument.
		 */
		public String randomString(int length){			
		return (new BigInteger(length*5, random)).toString(32);
		}
}