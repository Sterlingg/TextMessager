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

class Screens(object):

    def __init__(self):
        self.password = ""

        # curses.curs_set(0)
        # curses.cbreak()
        # curses.noecho()
        
    def main_menu(self, stdscreen):
        """
        """
        stdscreen.keypad(1)

        locale.setlocale(locale.LC_ALL,"")
        
        sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        sock.bind(('0.0.0.0',9001))
        sock.listen(5)
        data_lock = threading.Lock()

        main_menu = menu.Menu(stdscreen, sock, self.password)

        sockThread = sockthr.socketThread(main_menu, sock, data_lock)

        main_menu.start_thread(sockThread)

        main_menu.display()

        # Shutdown all further reads, which will also stop our socket from accepting anymore connections, and cause it to
        # throw an error.
        sock.shutdown(socket.SHUT_RDWR)
        sock.close()
        sockThread.join()
        
    def splash(self, screen):
        """
        The screen where the user enters their password.
        Arguments:
        - `self`:
        - `screen`:
        """
        max_y, max_x = screen.getmaxyx()

        pass_screen_y_off = 3
        pass_screen_x_off = 10
        
        pass_text_y_off = 0
        pass_text_x_off = 3
        
        pass_win = screen.subwin(5, 25,(max_y/2) - pass_screen_y_off, (max_x/2) - pass_screen_x_off)
        pass_win.box()
        pass_win.addstr(1, 2, "Please enter password")
        
        pass_box = screen.subwin(1, 9,(max_y/2) - pass_text_y_off, (max_x/2) - pass_text_x_off)
        
        #    pass_box.box()
        screen.refresh()
        tb = curses.textpad.Textbox(pass_box)
        
        pass_text = tb.edit()

        self.password[:len(pass_text) - 1]
        print "PASSWORD LENGTH ==== " + str(len(self.password))

if __name__ == '__main__':
    screens = Screens()
    
    curses.wrapper(screens.splash)
    print screens.password
    curses.wrapper(screens.main_menu)
