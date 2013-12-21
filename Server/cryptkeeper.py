""" 
cryptkeeper.py
"""
from Crypto.Cipher import AES
import base64
import socket
import pbkdf2

class CryptKeeper(object):
    """
    Class for handling the crypto hand shake, and all further encrypting and 
    decrypting of transmitted and received data.
    """
    def __init__(self, password, sock, sockthr):
        """
        Arguments:
        - `password`: The password typed in from the device.
        - `salt`: The salt received from the device.
        - `iv`: The IV received from the device.
        """
        self.password = password

        self.sock = sock
        self.salt = base64.b64decode(self.receive())
        self.iv = base64.b64decode(self.receive())
        self.key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)

        self.send(self.encrypt("Doge!!"))

        sockthr.set_crypt(self)
        sockthr.start()

    def decrypt(self, to_decrypt):
        """
        Decrypts the given string using AES in CBC mode using zero padding.

        Arguments:
        - `to_decrypt`: The string being decrypted.
        """
        gen_key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)
        obj = AES.new(self.key, AES.MODE_CBC,IV = self.iv)

        return obj.decrypt(to_decrypt).rstrip(chr(0))

    def encrypt(self, to_encrypt):
        """
        Encrypts the given string using AES in CBC mode using zero padding.

        Arguments:
        - `to_enrypt`: The string being decrypted.
        """
        gen_key = pbkdf2.pbkdf2_hex(self.password, self.salt, 1024, 16)
        
        obj = AES.new(self.key, AES.MODE_CBC,IV = self.iv)
        BLOCK_SIZE = 16
        PADDING = ' '
        pad = lambda s: s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * PADDING
        to_encrypt = pad(to_encrypt)
            
        cipher = obj.encrypt(to_encrypt)
        
        return cipher

    def receive(self):
        """
        Given an open socket connection returns a whole packet that has a length
        equal to the first 8 bytes received of the packet interpreted as an integer.
        """
        PACKET_HEADER_LEN = 8
        total_received = 0
        result = ""
        try:
            while total_received < PACKET_HEADER_LEN:
                received = self.sock.recv(1024)
                if not received: 
                    break
                result += received
                total_received += len(received)
            
            packet_length = int(result[0:PACKET_HEADER_LEN])
            
            # Receive the entire packet.
            while (total_received - PACKET_HEADER_LEN) < packet_length:
                received = self.sock.recv(1024)
                if not received: break
                total_received += len(received)
                result += received

            return received[8:]

        except socket.error, (socde, message):
            print message

    def send(self, to_send):
        """
        Given an open socket connection returns a whole packet that has a length
        equal to the first 8 bytes received of the packet interpreted as an integer.

        Arguments:
        `to_send`: The data being sent.
        """
        self.sock.sendall(str(len(to_send)).zfill(8) + to_send)
