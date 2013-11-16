package crypto;
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

public class CryptKeeper {

        private Cipher aesCipher;
        private SecretKeySpec aesKeySpec;
        private IvParameterSpec aesIv;
        private String shared_secret = "password";
        
        private static final String KDF_ALG = "PBKDF2WithHmacSHA1";
        private static final int KDF_ITERATIONS = 1024;
        private static final int KEY_SIZE = 128;
        private byte[] kdf_salt = null;
        
		private static final String ENCRYPTION_ALG = "AES";
		private static final String ENCRYPTION_MODE = "CBC";
		private static final String PADDING = "NoPadding";

        public CryptKeeper() {

         	
        		pbkdf2Gen(shared_secret.toCharArray());
        		
        		System.out.println("Key: " + 
        		Arrays.toString(aesKeySpec.getEncoded()));
        		//"C111510372A7A003".getBytes()
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
             
        public CryptKeeper(byte[] key, byte[] iv){ 

            	aesKeySpec = new SecretKeySpec(key, ENCRYPTION_ALG);
            	aesIv = new IvParameterSpec(iv);  
            	
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

        public byte[] encrypt(String s){
			return encrypt(padString(s).getBytes());
		}

		public byte[] encrypt(byte[] bs){
			// initialize the cipher for encrypt mode
			byte[] encrypted = null;

				try {
					aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, aesIv);
					encrypted = aesCipher.doFinal(bs);  
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                        					
			return encrypted;
		}

		public byte[] gen8ByteSalt(){
            //IV.
            byte[] iv = new byte[8];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            return iv;
		}
		
		public byte[] gen16ByteSalt(){
                //IV.
                byte[] iv = new byte[16];
                SecureRandom random = new SecureRandom();
                random.nextBytes(iv);
                
                return iv;
        }
		
		public byte[] get_iv(){			
			return aesIv.getIV();
		}
		
		public byte[] get_salt(){			
			return kdf_salt;
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
    	
        public static String padString(String s){
        	int BLOCK_SIZE = 16;
        	char PAD_VALUE = ' ';            		
            int padSize = BLOCK_SIZE - (s.length() % BLOCK_SIZE);		
            
            String padString = "";
            
            for (int i = 0; i < padSize; i++){
            	padString = padString + PAD_VALUE;
            }
                                   	
        	return s + padString;
        }        
}