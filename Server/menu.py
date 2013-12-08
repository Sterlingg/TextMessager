import curses
import datetime
import json
import locale
import math
import random
import socket
import sys
import base64

import cryptkeeper

from curses import panel, textpad

# Menu: Base class for all menus in the UI.
class Menu(object):

    def __init__(self, stdscreen, password):
        """
        stdscreen is the default screen.
        """
        self.MAX_YX = stdscreen.getmaxyx()

        self.stdscreen = stdscreen
        self.password = password
        self.window = stdscreen.subwin(0, 0)

        curses.init_pair(1, curses.COLOR_GREEN , curses.COLOR_MAGENTA)
        self.update_pairs(curses.COLOR_MAGENTA)
        self.window.bkgdset(' ', curses.color_pair(1))

        self.mail_win = self.window.subwin(6, 1)

        self.panel = panel.new_panel(self.window)

        self.sock = None
        self.sockthr = None
        self.device_id = ""
        self.crypt_keeper = None

        self.mail_boxes = {'Inbox': {'Content': None, 'Size': 0},
                           'Received': {'Content': None, 'Size': 0}, 
                           'Outbox': {'Content': None, 'Size': 0}} 

        self.inbox_start_line = 0

        # The location of the given column, and how many characters long it is.
        self.col_yxsize = {'Date': [0,0,0], 'Address': [0,0,0], 'Body': [0,0,0]}
        self.date_loc_sz = [0,0,0]
        self.addr_loc_sz = [0,0,0]
        self.body_loc_sz = [0,0,0]
        
        self.mail_row_pos = 0

        self.position = 0

        self.set_col_sizes(20, 20)

        self.items = [
            ('Inbox', lambda: self.get_messages(self.mail_boxes['Inbox'])),
            ('Received', lambda: self.get_messages(self.mail_boxes['Received'])),
            ('Send Message', lambda: self.send_message()),
            ('Connect', lambda: self.connect_phone()),
            ('Randomize BG!', self.randomize_bg)
            ]   

        self.items.append(('exit', 'exit'))

    def add_str_to_win(self, win, s_to_add, start_line, y_x_sz):
        """
        Adds a string to the window line by line, and also colourizes each
        line in a random way. :)
        
        win - The window that the string is being written to.
        s_to_add - The string being added to this window.
        start_line - The first line that the string is being written
        to in this window.
        y_x_sz - A tuple containing the top left corner y position of this 
        window, the top left corner x position of this window, and the 
        maximum length of each line of text in this window.
        """
        i = start_line
        code = locale.getpreferredencoding()

        while len(s_to_add) > 0:
            # Check that we aren't outside of the viewable region.
            if i < (self.MAX_YX[0] - y_x_sz[0]):
                # String smaller than the current line, so add it to the current line and
                # then we're finished, so break from the loop.
                if len(s_to_add) < y_x_sz[2]:
                    win.addstr(i, y_x_sz[1], s_to_add,
                               curses.color_pair(random.randint(2, 7)))
                    break
                else:
                    win.addstr(i, y_x_sz[1], s_to_add[0:y_x_sz[2] - 1], curses.color_pair(random.randint(2, 7)))
                    s_to_add = s_to_add[y_x_sz[2] - 1:]
                    i += 1
            else:
                break
        return i - start_line

    def connect_phone(self):
        """
        Initiates a socket connection to the phone.
        """
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect(('localhost', 5051))

    def count_lines(self, s, y_x_sz):
        """
        Counts the number of rows a string will occupy.
        """
        i = 1
        while len(s) > 0:
            if i < (self.MAX_YX[0] - y_x_sz[0]):
                if len(s) < y_x_sz[2]:
                    break
                else:
                    s = s[y_x_sz[2] - 1:]
                    i += 1
            else:
                break
        return i

    # direct_to_mail_box: Parses the JSON data into an array of SMS messages, and passes
    #                     it off to the appropriate mail box.
    # TODO: YIKES !!
    def direct_to_mail_box(self,messages):
        jsonMessages = json.loads(messages)
        try:
            self.mail_boxes['Inbox']['Content'] = jsonMessages['Inbox']
        except:
            pass
        try:
            self.mail_boxes['Outbox']['Content'] = jsonMessages['Outbox']
        except:
            pass
        try:
            self.mail_boxes['Received']['Content'] = jsonMessages['Received']
        except:
            pass

    def format_timestamp(self, time_stamp):
        """
        """
        formatted = datetime.datetime.fromtimestamp(int(time_stamp) / 1000).strftime('%Y-%m-%d %H:%M:%S')
        return formatted

    def get_line_count(self, mail):
        """
        """  
        date_num_lines = self.add_str_to_win(self.mail_win, self.format_timestamp(mail['date']), line, self.col_yxsize['Date'])
        addr_num_lines = self.add_str_to_win(self.mail_win, mail['address'], 
                                             line, self.col_yxsize['Address'])
        body_num_lines = self.add_str_to_win(self.mail_win, mail['body'], 
                                             line, self.col_yxsize['Body'])
        line += (max(date_num_lines, addr_num_lines, body_num_lines) + 1)

    def get_messages(self, mail_box):
        self.set_mail_lines(mail_box)
        self.update_pad(mail_box)

    def navigate(self, n):
        """
        Advance the cursor position by n.
        """
        self.position += n
        if self.position < 0:
            self.position = 0                                                
        elif self.position >= len(self.items):                               
            self.position = len(self.items)-1                                

    def on_prev(self):
        """
        """
        win_size = self.MAX_YX[0] - self.col_yxsize['Date'][0]
        if self.inbox_start_line - win_size >= 0:
            self.inbox_start_line -= win_size
        else:
            self.inbox_start_line = 0
        self.update_pad(self.mail_boxes['Inbox'])

    def on_next(self):
        """
        """
        win_size = self.MAX_YX[0] - self.col_yxsize['Date'][0]
        if self.inbox_start_line + win_size < self.mail_boxes['Inbox']['Size']:
            self.inbox_start_line += win_size
        self.update_pad(self.mail_boxes['Inbox'])

    def parse_mail_box(self, mail_box, start):
        self.set_up_col_names()

        line_num = 5
        if mail_box['Content'] != None:
            for mail in mail_box['Content'][start:]:
                date_num_lines = self.add_str_to_win(self.mail_win, 
                                                     self.format_timestamp(mail['date']), 
                                                     line_num, self.col_yxsize['Date'])
                addr_num_lines = self.add_str_to_win(self.mail_win, 
                                                     mail['address'], line_num, 
                                                     self.col_yxsize['Address'])
                body_num_lines = self.add_str_to_win(self.mail_win, mail['body'], line_num,
                                                     self.col_yxsize['Body'])
                line_num += (max(date_num_lines, addr_num_lines, body_num_lines) + 1)
        else:
            self.add_str_to_win(self.mail_win, 'No messages.', line_num, self.col_yxsize['Date'])

        self.mail_win.refresh()

    def randomize_bg(self):
        fg = random.randint(1, 7)
        bg = random.randint(1, 7)

        while fg == bg:
            fg = random.randint(1, 7)
            bg = random.randint(1, 7)

        curses.init_pair(1, fg, bg)
        self.update_pairs(bg)

    def send_message(self):
        """
        """
        
        send_screen = curses.newwin(40, 40, 2, 35)

        num_window = curses.newwin(1, 16, 2, 35)
        text_window = curses.newwin(4, 40, 4, 35)

        num_tb = curses.textpad.Textbox(num_window, insert_mode = True)
        body_tb = curses.textpad.Textbox(text_window, insert_mode = True)

        self.window.addstr(1, 35, "Enter Phone Number",
                   curses.color_pair(random.randint(2, 7)))
        self.window.addstr(3, 35, "Enter Message",
                   curses.color_pair(random.randint(2, 7)))

        self.window.refresh()

        phone_num_text = num_tb.edit()
        body_text = body_tb.edit()

        # Remove all of the newlines.
        for i in range(40, len(body_text), 40):
            body_text = body_text[0:i] + '' + body_text[i+1:]
        
        self.add_str_to_win(self.window, body_text, 2, [3, 40, 40])

        json_num_body_str = json.dumps({"PhoneNumber": phone_num_text, "Body": body_text})

        encrypted_str = base64.b64encode(self.crypt_keeper.encrypt(json_num_body_str))


        self.add_str_to_win(self.window, "                                                                                                                                                                                                                                                                                                                                                                                              ", 0, [0, 35, 45])

        try:        
            self.sock.sendall(str(len(encrypted_str)).zfill(8) + encrypted_str)

        except socket.error, (socde,message):
            print ":(" + message

        return phone_num_text
        
    def set_mail_lines(self, mail_box):
        total_lines = 0

        if mail_box['Content'] != None:
            for mail in mail_box['Content']:
                date_num_lines = self.count_lines(self.format_timestamp(mail['date']), self.col_yxsize['Date'])
                addr_num_lines = self.count_lines(mail['address'], self.col_yxsize['Address'])
                body_num_lines = self.count_lines(mail['body'], self.col_yxsize['Body'])

                num_lines = max(date_num_lines, addr_num_lines, body_num_lines)

                mail['num_lines'] = num_lines
                total_lines += num_lines
                self.mail_boxes['Inbox']['Size'] = total_lines

    def start_thread(self, sockthr):
        """
        """
        self.sockthr = sockthr
        
        with open('test.txt', 'w') as f:
            f.write(self.password)

        print self.password

        self.crypt_keeper = cryptkeeper.security_setup(self.password)#self.password)
        self.sockthr.set_crypt(self.crypt_keeper)
        self.sockthr.start()
        
    def set_up_col_names(self):
        """
        """
        COL_NAME_START_Y = 4
        self.add_str_to_win(self.mail_win, 'DATE', COL_NAME_START_Y, self.col_yxsize['Date'])
        self.add_str_to_win(self.mail_win, 'FROM', COL_NAME_START_Y, self.col_yxsize['Address'])
        self.add_str_to_win(self.mail_win, 'THE BAWDDDYY LOOOoooK', COL_NAME_START_Y, self.col_yxsize['Body'])

    def set_col_sizes(self, date_size, addr_size):
        """
        Sets the date and address column sizes, and the body size which is determined by the 
        length of the date and address columns.
        """
        mail_locs = []

        self.col_yxsize['Date'] = [6, 0, date_size]
        self.col_yxsize['Address'] = [6, date_size + 2, addr_size]
        self.col_yxsize['Body'] = [6, date_size + addr_size + 4, self.MAX_YX[1] - (date_size + addr_size) - 5]
    
    def update_pad(self, mail_box):
        """
        """
        self.mail_win.clear()
        self.parse_mail_box(mail_box, self.inbox_start_line)

    def update_pairs(self, bg):
        """
        """
        j = 2
        for i in range(1, 8):
            if i != bg:
                curses.init_pair(j, i, bg)
                j += 1

    def display(self):
        """
        """
        self.panel.top()                                                     
        self.panel.show()                                                    
        self.window.clear()                                          
        while 1:
            self.window.refresh()
            curses.doupdate()
            for index, item in enumerate(self.items):
                if index == self.position:
                    mode = curses.A_REVERSE
                else:
                    mode = curses.A_NORMAL

                msg = '%d. %s' % (index, item[0])

                self.window.addstr(1+index, 1, msg, mode)

            key = self.window.getch()

            if key in [curses.KEY_ENTER, ord('\n')]:
                # Exit selected, so pop current panel off.
                if self.position == len(self.items)-1:
                    break
                # Perform the action pointed to by the cursor.
                else:
                    self.items[self.position][1]()
            elif key == ord('w'):#curses.KEY_UP:
               self.navigate(-1)
            elif key == ord('s'):#curses.KEY_DOWN:
                self.navigate(1)
            elif key == curses.KEY_RESIZE:
                pass#                self.on_resize()
            elif key == curses.KEY_NPAGE:
                self.on_next()
            elif key == curses.KEY_PPAGE:
                self.on_prev()
                
        self.window.clear()                                                  
        self.panel.hide()
        panel.update_panels()
        curses.doupdate()
