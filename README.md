# Scanner
An Android project for using Visible Light Communication

With this application it is possible to transfer data via light by exploiting the so called rolling shutter effect of CMOS cameras.

The sender is a LED which is connected to a Raspberry Pi. The Raspberry PI controls the LED with a Python application. This application turns the LED on and off at a very high frequency (for example 900 times per second) that the human eye cannot recognize. 
When a smartphone camera with 30fps scans this LED one frame contains bright and dark stripes are seen in the pictures because a frame is not recorded all at once but row by row with a CMOS camera. This bright and dark stripes are when the LED is turned on and off. With those stripes it is possible to detect some data in the pictures (you can imagine it like a barcode).

The demovideo and the presentation can give a more detailled explanation about the application.
