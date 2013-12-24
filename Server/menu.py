"""
menu.py
        TODO: Move mail box handling to a seperate object.
"""

import curses
import datetime
import json
import socket
import base64
import cryptkeeper

from curses import panel, textpad

class Menu(object):
    """
    Class representing the main menu.
    """
    def __init__(self, stdscreen, device_ip, password, sock):
        """
        Arguments:
        - `stdscreen`: Screen passed in by curses wrapper.
        - `device_ip`: The ip of the device being connected to.
        - `password`: The password read from the device.
        """
        self.max_yx = stdscreen.getmaxyx()

        self.stdscreen = stdscreen
        self.device_ip = device_ip
        self.password = password
        self.window = stdscreen.subwin(0, 0)
        self.panel = panel.new_panel(self.window)
        self.mail_win = self.window.subwin(6, 1)

        self.sock = sock
        self.sockthr = None
        self.crypt_keeper = None

        self.mail_boxes = {'Inbox': [],
                           'Received': []}

        self.start_lines = {'Inbox' : 0, 'Received' : 0}
        self.curr_mail_box = None
        self.num_mail_on_page = {'Inbox': 0, 'Received': 0}
        self.prev_num_mail_on_page = {'Inbox': 0, 'Received': 0}
        self.is_end_of_box = False

        self.col_y_x_sz = {'Date': [0, 0, 0], 
                           'Address': [0, 0, 0],
                           'Body': [0, 0, 0]}
        
        self.menu_pos = 0

        self.set_col_sizes(20, 20)

        self.items = [
            ('Inbox', lambda: self.update_mail_box(self.mail_boxes['Inbox'], 'Inbox')),
            ('Received', lambda: self.update_mail_box(
                self.mail_boxes['Received'], 'Received')),
            ('Send Message!', self.send_message)
            ]   

        self.items.append(('Exit', 'exit'))

    def direct_to_mail_box(self, messages):
        """
        Parses the JSON data into an array of SMS messages, and 
        passes it off to the appropriate mail box.
        Arguments:
        - `messages`: This is a JSON message sent from the device.
        """

        json_messages = json.loads(messages)

        try:
            self.mail_boxes['Inbox'] = json_messages['Inbox']
        except:
            pass
        try:
            self.mail_boxes['Outbox'] += json_messages['Outbox']
        except:
            pass
        try:
            self.mail_boxes['Received'] = json_messages['Received'] + self.mail_boxes['Received']
            self.mail_boxes['Inbox'] = json_messages['Received'] + self.mail_boxes['Inbox']
            self.window.addstr(len(self.items) + 1, 1, "New Message!", curses.A_NORMAL) 
        except:
            pass

    def display_items(self):
        """
        Writes the menu items to the screen.
        """
        for index, item in enumerate(self.items):
            if index == self.menu_pos:
                mode = curses.A_REVERSE
            else:
                mode = curses.A_NORMAL

            msg = '%d. %s' % (index, item[0])
            self.window.addstr(1+index, 1, msg, mode)
            curses.doupdate()

    def display(self):
        """
        Main loop to display the menu. Adds selections from the items variable
        to the list of options, and calls the second parameter of the tuple
        when an item is selected with the enter key.
        """
        self.panel.top()                                                     
        self.panel.show()                                                    
        self.window.clear()                                          
        while 1:
            self.display_items()

            key = self.window.getch()

            if key in [curses.KEY_ENTER, ord('\n')]:
                # Exit selected, so pop current panel off.
                if self.menu_pos == len(self.items)-1:
                    break
                # Perform the action pointed to by the cursor.
                else:
                    self.items[self.menu_pos][1]()
            elif key == ord('w'):
                self.navigate(-1)
            elif key == ord('s'):
                self.navigate(1)
            elif key == curses.KEY_RESIZE:
                pass
            elif key == ord('n'):
                self.scroll_down()
            elif key == ord('p'):
                self.scroll_up()
                
        self.window.clear()                                                  
        self.panel.hide()
        panel.update_panels()

    def format_timestamp(self, time_stamp):
        """
        Parses a time stamp formatted in milliseconds into a string of the 
        form %Y-%m-%d %H:%M:%S.

        Arguments:
        - `time_stamp`: Time stamp in milliseconds.
        """
        date_time = datetime.datetime.fromtimestamp(int(time_stamp) / 1000)
        return date_time.strftime('%Y-%m-%d %H:%M:%S')

    def navigate(self, delta):
        """
        Advance the cursor position by delta.
        """
        self.menu_pos += delta
        if self.menu_pos < 0:
            self.menu_pos = 0                                                
        elif self.menu_pos >= len(self.items):                               
            self.menu_pos = len(self.items)-1

    def scroll_down(self):
        """
        "Scrolls" the screen downwards by moving the starting line of the mail
        box down by the number of rows in the mail box.
        """
        if(self.mail_boxes[self.curr_mail_box] != None):
            incremented_mail_start_line = self.start_lines[self.curr_mail_box] + self.num_mail_on_page[self.curr_mail_box]
            if (incremented_mail_start_line < len(self.mail_boxes[self.curr_mail_box])):
                self.prev_num_mail_on_page[self.curr_mail_box] = self.num_mail_on_page[self.curr_mail_box]
                self.start_lines[self.curr_mail_box] += self.num_mail_on_page[self.curr_mail_box]
            else:
                self.is_end_of_box = True
            self.update_mail_box(self.mail_boxes[self.curr_mail_box]
                                 , self.curr_mail_box)

    def scroll_up(self):
        """
        "Scrolls" the screen upwards by moving the starting line of the mail
        box up by the number of rows in the mail box.
        """
        if(self.mail_boxes[self.curr_mail_box] != None):
            decremented_mail_start_line = self.start_lines[self.curr_mail_box] - self.num_mail_on_page[self.curr_mail_box]
            if (decremented_mail_start_line <= 0):
                self.start_lines[self.curr_mail_box] = 0
            else:
                self.start_lines[self.curr_mail_box] -= self.prev_num_mail_on_page[self.curr_mail_box]

            self.update_mail_box(self.mail_boxes[self.curr_mail_box]
                                 , self.curr_mail_box)
    
    def parse_mail_box(self, mail_box):
        """
        Displays the mail in the mail_box to the screen starting from
        start_line in the mail_box, and 
        Arguments:
        - `mail_box`: The mail_box being parsed.
        - `start_line`: The line to start parsing the mail box from.
        """
        self.mail_win.clear()
        self.set_up_col_names()
        line_num = 5
        mail_per_page = 0

        win_height = self.col_y_x_sz['Date'][2]
    
        if mail_box:
            for mail in mail_box[self.start_lines[self.curr_mail_box]:]:
                date_num_lines = self.str_to_win(self.mail_win, 
                                                 self.format_timestamp(
                                                     mail['date']),
                                                 line_num, 
                                                 self.col_y_x_sz['Date'])

                addr_num_lines = self.str_to_win(self.mail_win, 
                                                 mail['address'], 
                                                 line_num, 
                                                 self.col_y_x_sz['Address'])

                body_num_lines = self.str_to_win(self.mail_win,
                                                 mail['body'],
                                                 line_num,
                                                 self.col_y_x_sz['Body'])

                line_length = (max(date_num_lines, addr_num_lines,
                                   body_num_lines) + 1)
                line_num += line_length
                mail_per_page += 1

                if (self.start_lines[self.curr_mail_box] + mail_per_page) < len(mail_box):
                    self.is_end_of_box = False
                else:
                    self.is_end_of_box = True

                if line_num > win_height:
                    break
        else:
            self.str_to_win(self.mail_win, 'No messages.', line_num,
                            self.col_y_x_sz['Date'])

        self.num_mail_on_page[self.curr_mail_box] = mail_per_page
        if not self.is_end_of_box:
            self.prev_num_mail_on_page[self.curr_mail_box] = self.num_mail_on_page[self.curr_mail_box]
        self.mail_win.refresh()

    def send_message(self):
        """
        Sends a message from the Python client to the Android device, which
        then sends it to the address specified in the phone_num_text input
        with the body specified in body_text input.
        """
        num_window = curses.newwin(1, 16, 2, 35)
        text_window = curses.newwin(4, 40, 4, 35)

        num_tb = curses.textpad.Textbox(num_window, insert_mode = True)
        body_tb = curses.textpad.Textbox(text_window, insert_mode = True)

        self.window.addstr(1, 35, "Enter Phone Number",
                           curses.A_NORMAL)
        self.window.addstr(3, 35, "Enter Message",
                           curses.A_NORMAL)

        self.window.refresh()

        phone_num_text = num_tb.edit()
        body_text = body_tb.edit()

        for i in range(40, len(body_text), 40):
            body_text = body_text[0:i] + '' + body_text[i+1:]
        
        self.str_to_win(self.window, body_text, 2, [3, 40, 40])

        json_num_body_str = json.dumps({"PhoneNumber": phone_num_text,
                                        "Body": body_text})

        encrypted_str = base64.b64encode(
            self.crypt_keeper.encrypt(json_num_body_str))

        clear_length = 376
        self.str_to_win(self.window,
                        "".join([" " for x in range(clear_length)]),
                        0, [0, 35, 45])

        try:
            self.sock.sendall(str(len(encrypted_str)).zfill(8) + encrypted_str)

        except socket.error, (socde, message):
            print ":( from: " + socde + "because" + message

        return phone_num_text
        
    def set_col_sizes(self, date_size, addr_size):
        """
        Sets the date and address column sizes, and the body size which is determined by the 
        length of the date and address columns.
        """
        self.col_y_x_sz['Date'] = [6, 0, date_size]
        self.col_y_x_sz['Address'] = [6, date_size + 2, addr_size]
        self.col_y_x_sz['Body'] = [6,
                                   date_size + addr_size + 4,
                                   self.max_yx[1] - (date_size + addr_size) - 5]
        
    def set_up_col_names(self):
        """
        Adds the names of each of the mail box columns to the mail window.
        """
        y_start_pos = 4
        self.str_to_win(self.mail_win,
                        'Date', y_start_pos, self.col_y_x_sz['Date'])
        self.str_to_win(self.mail_win,
                        'From', y_start_pos, self.col_y_x_sz['Address'])
        self.str_to_win(self.mail_win,
                        'Body', y_start_pos, self.col_y_x_sz['Body'])

    def start_thread(self, sockthr):
        """
        Starts the thread that receives messages from the device, and begins
        the encryption hand shake.
        """
        self.sockthr = sockthr
        self.crypt_keeper = cryptkeeper.CryptKeeper(self.password, self.sock, self.sockthr)
        
    def str_to_win(self, win, s_to_add, start_line, y_x_sz):
        """
        Adds a string to the window line by line checking whether the line
        will fit on the screen.
        
        win - The window that the string is being written to.
        s_to_add - The string being added to this window.
        start_line - The first line that the string is being written
        to in this window.
        y_x_sz - A tuple containing the top left corner y position of this 
        window, the top left corner x position of this window, and the 
        maximum length of each line of text in this window.
        """
        i = start_line

        while len(s_to_add) > 0:
            if i < (self.max_yx[0] - y_x_sz[0]):
                if len(s_to_add) < y_x_sz[2]:
                    win.addstr(i, y_x_sz[1], s_to_add.encode("UTF-8"),
                               curses.A_NORMAL)
                    break
                else:
                    win.addstr(i, y_x_sz[1], 
                               s_to_add[0:y_x_sz[2] - 1].encode("UTF-8"),
                               curses.A_NORMAL)
                    s_to_add = s_to_add[y_x_sz[2] - 1:]
                    i += 1
            else:
                break
        return i - start_line

    def update_mail_box(self, mail_box, box_name):
        """
        Removes the 
        Arguments:
        - `mail_box`: List of text messages each of which contain the date
        the message was sent, the address the text message was sent from, and
        the body of the text message.
        """
        self.window.addstr(len(self.items) + 1, 1, "            ", curses.A_NORMAL) 
        self.curr_mail_box = box_name
        self.parse_mail_box(mail_box)
