from Crypto.Cipher import AES
import base64
import socket
import pbkdf2


#decrypt: Decrypts a string with 128 bit AES-CBC.
def decrypt(to_decrypt):
    # TODO: Add initialization vector.
    hard_key = '768EE18AB6480D53CC8FFCD23D117D57'
    gen_key = pbkdf2.pbkdf2_hex('password', 'salt', 1024, 16)
    obj = AES.new(gen_key, AES.MODE_CBC,IV = 'C111510372A7A003')
    return obj.decrypt(to_decrypt)

#encrypt: Encrypts a string with 128 bit AES-CBC.
def encrypt(to_encrypt):
    # TODO: Add initialization vector.
    hard_key = '768EE18AB6480D53CC8FFCD23D117D57'
    gen_key = pbkdf2.pbkdf2_hex('password', 'salt', 1024, 16)

    print gen_key
    obj = AES.new(gen_key, AES.MODE_CBC,IV = 'C111510372A7A003')
    BLOCK_SIZE = 16
    PADDING = ' '
    pad = lambda s: s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * PADDING
    to_encrypt = pad(to_encrypt)

    cipher = obj.encrypt(to_encrypt)
    
    return cipher

def send(s):
    """
    """
    try:        
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect(('localhost', 8080))
        self.sock.sendall(str(len(json_num_body_str)).zfill(8) + json_num_body_str)

    except socket.error, (socde,message):
        print ":(" + message
