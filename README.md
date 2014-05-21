#TextMessager

An interface for sending and receiving text messages from an Android phone on your computer. 

## Demo
   ![Lights, Camera, Action!](https://github.com/Sterlingg/TextMessager/raw/master/Demo/cropped_demo.gif)

## Description
This is a program for sending and receiving text messages from your computer. It is meant to provide a faster way of responding to text messages received on your phone.

##Installation
1. First import the TextMessager project into Eclipse.
2. Make sure you have the ADT plugin installed.
3. Install the program on your device by hitting the play button and selecting the device you plugged in.
4. The program is now ready to use on your device.
5. Now run main.py in the TextMessager/Server directory by typing `python main.py` in the console.
6. Enter the computer's IP* on the Android interface and enter the device IP on the computer's interface. 
7. Press next step on the Android interface and enter / C-g on the computer's interface.
8. Enter the password into the computer's interface and hit enter / C-g.
9. Press "Next Step" on the Android interface.
10. A "Connection Established" message should show up on the Android interface after this.
11. If it doesn't, please give me information on the steps you took, and I'll try my best to fix it or troubleshoot your problem.

\* Make sure the computer and device are on the same subnet, or else make sure that port 9002 is forwarded properly for both the Android device and the computer.

##Usage
Use 'w' and 's' to select menu items on the computer's interface, and enter to select an item. Use 'n' and 'p' to scroll a mail box up and down. When sending a message use C-g to move between the phone number and body fields, and a final C-g to finish sending a message. 
