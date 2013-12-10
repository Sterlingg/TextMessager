"""
sockthr.py
"""

import base64
import threading
import socket

PACKET_HEADER_LEN = 8
class SocketThread (threading.Thread):
    """
    The thread which the client receives on. Passing data to the Menu class as it becomes
    available.
    """
    def __init__(self, menu, sock):
        self.sock = sock
        threading.Thread.__init__(self)
        self.menu = menu
        self.crypt_keeper = None

    def run(self): 
        """
        The main socket loop. Receives packets of Unicode text starting with
        the size of the packet as a 4 byte integer. Followed by a JSON string
        with { (Mail box name): 
        [Array of mail messages with parameters (address, body, date)]}"""
        try:
            while 1:
                # Get the length of the packet.
                total_received = 0
                result = ""
                
                while total_received < PACKET_HEADER_LEN:
                    received = self.sock.recv(1024)
                    if not received: 
                        break

                    result += received
                    total_received += len(received)

                packet_length = int(result[0:PACKET_HEADER_LEN]) + PACKET_HEADER_LEN
                    
                # Receive the entire packet.
                while total_received < packet_length:
                    received = self.sock.recv(1024)
                    if not received: 
                        break
                    total_received += len(received)
                    result += received

                self.menu.direct_to_mail_box(
                    self.crypt_keeper.decrypt
                    (base64.b64decode(result[PACKET_HEADER_LEN:])))

        except socket.error:
            self.sock.close()
            return

    def set_crypt(self, crypt_keeper):
        """
        Sets the crypto handler for this thread.
        """
        self.crypt_keeper = crypt_keeper
