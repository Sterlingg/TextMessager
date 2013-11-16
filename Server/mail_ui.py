#!/usr/bin/env python2                                                       
import socket
import curses
import locale
import threading
import time
import sys
import menu
import sockthr

#http://stackoverflow.com/questions/14200721/how-to-create-a-menu-and-submenus-in-python-curses
# mail_ui: Curses interface for sending/receiving messages from a phone running the SMSClient. Runs the display on one thread and the socket on the other thread.

class MyApp(object):    

    def __init__(self, stdscreen):
        self.screen = stdscreen
        curses.curs_set(0)
        curses.cbreak()
        curses.noecho()
        self.screen.keypad(1)

        # Set the locale to the default locale, so that curses will accept unicode characters.
        locale.setlocale(locale.LC_ALL,"")
        
        sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sock.bind(('0.0.0.0',9002))
        sock.listen(5)
        data_lock = threading.Lock()

        main_menu = menu.Menu(self.screen)

        sockThread = sockthr.socketThread(main_menu, sock, data_lock)
        sockThread.start()

        main_menu.display()

        # Shutdown all further reads, which will also stop our socket from accepting anymore connections, and cause it to
        # throw an error.
        sock.shutdown(socket.SHUT_RDWR)
        sock.close()
        sockThread.join()

if __name__ == '__main__':
    curses.wrapper(MyApp)
