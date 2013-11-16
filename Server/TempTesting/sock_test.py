import socket
import json

if __name__ == '__main__':

    try:
        sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sock.connect(('10.0.2.15',9003))

        to_send = "Hellol world!"

        sock.sendall(str(len(to_send)).zfill(8) + to_send)
        
    except socket.error, (socde,message):
       print message
