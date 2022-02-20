# Scanner
An Android project for using Visible Light Communication

With this application it is possible to transfer data via light by exploiting the so called rolling shutter effect of CMOS cameras.

The sender is a LED which is connected to a Raspberry Pi. The Raspberry PI controls the LED with a Python application. This application turns the LED on and off at a very high frequency (for example 900 times per second) that the human eye cannot recognize. 
When a smartphone camera scans this LED one frame contains bright and dark stripes, as seen in the below picture.
<img width="357" alt="image" src="https://user-images.githubusercontent.com/37902981/154864226-c40928c2-ef6e-45c5-a5ba-d075fb708527.png">
This is because with a CMOS camera a frame is not recorded all at once but row by row.
The light and dark stripes show when the LED was on and off. Thus, several data can be extracted from one frame (you can imagine it like a barcode).

The demovideo and the presentation can give a more detailled explanation about the application.

