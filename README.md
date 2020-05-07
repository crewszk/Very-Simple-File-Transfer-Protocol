# VSFTP Project

In my computer networks class, we were tasked with creating a Very Simple File Transfer Protocol utilizing Java's (or C++ but I chose Java) Socket API. In each folder, both Client and Server have a README.txt file that corresponds with how to set up the Client and the Server as well as the various commands that you may use.

Recieved a 100% on the project though the project has some bugs
- During the RETR command, when asked to SEND the file or STOP the transmission using STOP crashes the Server and disconnects the client across all platforms
- During the RETR command, when asked to SEND the file the Server crashes and disconnects the Client on Linux platforms. This was found when the professor was testing on a Linux platform for all students while I developed on a Windows platform and specify in the README to use command prompt for testing.