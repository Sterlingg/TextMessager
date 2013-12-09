from Crypto.Cipher import AES
import base64
import socket
import pbkdf2

TEST_KEY = '768EE18AB6480D53CC8FFCD23D117D57'
TEST_IV = 'C111510372A7A003'

class CryptKeeper(object):
    """
    """
    def __init__(self, password = "ffffffff-9e04-e7aa-ffff-ffff99d603a9", salt = "salt", iv = TEST_IV):
        """
        """
        self.password = password
        self.salt = salt
        self.key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)
        print ord(self.key[0])
        self.iv = iv

        print "IV Length = " + str(len(self.iv))

        #decrypt: Decrypts a string with 128 bit AES-CBC.
    def decrypt(self, to_decrypt):
        """
        """     
        hard_key = '768EE18AB6480D53CC8FFCD23D117D57'
        gen_key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)
        obj = AES.new(self.key, AES.MODE_CBC,IV = self.iv)

        return obj.decrypt(to_decrypt).rstrip(chr(0))

        #encrypt: Encrypts a string with 128 bit AES-CBC.
    def encrypt(self, to_encrypt):
        # TODO: Add initialization vector.
        hard_key = '768EE18AB6480D53CC8FFCD23D117D57'
        gen_key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)
        
        obj = AES.new(self.key, AES.MODE_CBC,IV = self.iv)
        BLOCK_SIZE = 16
        PADDING = ' '
        pad = lambda s: s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * PADDING
        to_encrypt = pad(to_encrypt)
            
        cipher = obj.encrypt(to_encrypt)
        
        return cipher

def receive(conn):
    PACKET_HEADER_LEN = 8
    total_received = 0
    result = ""
    try:
        while total_received < PACKET_HEADER_LEN:
            received = conn[0].recv(1024)
            if not received: 
                break
            result += received
            total_received += len(received)
            
        packet_length = int(result[0:PACKET_HEADER_LEN])
            
        # Receive the entire packet.
        while (total_received - PACKET_HEADER_LEN) < packet_length:
            received = conn[0].recv(1024)
            if not received: break
            total_received += len(received)
            result += received

        return received[8:]
    except socket.error, (socde, message):
        print message

def send():
    try:
        sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sock.connect(('localhost',9001))

        to_send = "Doge!"

        sock.sendall(str(len(to_send)).zfill(8) + to_send)
        sock.close()
        
    except socket.error, (socde,message):
        print message

def send(conn, to_send):
    conn[0].sendall(str(len(to_send)).zfill(8) + to_send)

def security_setup(password):
    sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    sock.bind(('0.0.0.0',9006))
    sock.listen(5)

    conn = sock.accept()

    salt = base64.b64decode(receive(conn))
    iv = base64.b64decode(receive(conn))

    print "Received salt: " + salt + "\nReceived IV: " + iv
    print "Password: " + password
    # emulator_id = (length "ffffffff-9e04-e7aa-ffff-ffff99d603a9")
    ck = CryptKeeper(password, salt, iv)
    
    send(conn, ck.encrypt("Doge!!"))

    sock.close()
    return ck

if __name__ == '__main__':
    security_setup()
