"""
main.py
"""

#!/usr/bin/env python2                                                       
import socket
import curses
import locale
import threading
import menu
import sockthr

class Screens(object):
    """
    http://stackoverflow.com/questions/14200721/
    how-to-create-a-menu-and-submenus-in-python-curses
    Curses interface for sending/receiving text messages from a phone.
    Represents all of the screens that the UI uses.
    """
    def __init__(self):
        self.password = ""
        self.device_ip = ""

    def curses_prefs(self):
        """
        Sets up the preferences of the curses window.
        """
        curses.curs_set(0)
        curses.cbreak()
        curses.noecho()

    def main_menu(self, stdscreen):
        """
        Starts the main menu of the UI.
        """
        stdscreen.keypad(1)
        self.curses_prefs()
        
        locale.setlocale(locale.LC_ALL,"")
        
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.bind(('0.0.0.0', 9002))
        sock.listen(5)
        conn = sock.accept()

        main_menu = menu.Menu(stdscreen, self.device_ip, self.password, conn[0])

        sock_thread = sockthr.SocketThread(main_menu, conn[0])

        main_menu.start_thread(sock_thread)
        main_menu.display()

        # Shutdown all further reads, which will also stop our socket from 
        # accepting anymore connections, and cause it to throw an error.
        conn[0].shutdown(socket.SHUT_RDWR)
        conn[0].close()
        sock_thread.join()

    def ip_screen(self, stdscreen):
        """
        The screen where the user enters their password.
        Arguments:
        - `stdscreen`: The standard screen passed in by the curses wrapper.
        """
        max_y, max_x = stdscreen.getmaxyx()
        self.curses_prefs()

        ip_screen_y_off = 3
        ip_screen_x_off = 10
        
        ip_text_y_off = 0
        ip_text_x_off = 3
        
        ip_win = stdscreen.subwin(5,
                                  25,
                                  (max_y/2) - ip_screen_y_off,
                                  (max_x/2) - ip_screen_x_off)
        ip_win.box()
        ip_win.addstr(1, 2, "Please enter ip.")
        
        ip_win = stdscreen.subwin(1,
                                  16,
                                  (max_y/2) - ip_text_y_off,
                                  (max_x/2) - ip_text_x_off)
        
        stdscreen.refresh()
        ip_box = curses.textpad.Textbox(ip_win)
        
        ip_text = ip_box.edit()

        self.device_ip = ip_text[:len(ip_text) - 1]
        stdscreen.clear()
        
    def pw_screen(self, stdscreen):
        """
        The screen where the user enters their password.
        Arguments:
        - `self`:
        - `stdscreen`:
        """
        max_y, max_x = stdscreen.getmaxyx()
        self.curses_prefs()

        pass_screen_y_off = 3
        pass_screen_x_off = 10
        
        pass_text_y_off = 0
        pass_text_x_off = 3
        
        pass_win = stdscreen.subwin(5,
                                    25,
                                    (max_y/2) - pass_screen_y_off,
                                    (max_x/2) - pass_screen_x_off)
        pass_win.box()
        pass_win.addstr(1, 2, "Please enter password")
        
        pass_win = stdscreen.subwin(1,
                                    9,
                                    (max_y/2) - pass_text_y_off,
                                    (max_x/2) - pass_text_x_off)
        stdscreen.refresh()
        pass_win.refresh()
        pass_box = curses.textpad.Textbox(pass_win)
        
        pass_text = pass_box.edit()

        self.password = pass_text[:len(pass_text) - 1]

if __name__ == '__main__':
    screens = Screens()
    curses.wrapper(screens.ip_screen)
    curses.wrapper(screens.pw_screen)
    curses.wrapper(screens.main_menu)
