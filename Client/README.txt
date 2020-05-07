Zackery Crews
CNT4504 Computer Networks Project 1

Very Simple File Transfer Protocol

VSFTP Server:
    Program used to host the VSFTP server. Once running it will ask for the port to be used for the VSFTP services 

VSFTP Client:
    This is the client used to access the VSFTP server. 
    Once running it will ask for the IP of the server and port to be used in order to access the server


How to setup and run the VSFTP Server:

    1.	Place the Server.java file into its own folder, like a folder named Server. 
        Inside the Server folder, place the users.txt file as well as a root folder that contains files of your choosing in them.

    2.	Compile the VSFTP Server by opening the Server folder in the command prompt. Use the command:
                javac Server.java

    3.	From the Server folder, execute your VSFTP Server with the command:
                java Server 

    4.	The VSFTP server is now live and will request you for a port to use. 


How to setup and run the VSFTP Client:

    1.  Place the Client.java file into its own folder, like a folder named Client.
        Inside the Client folder, place a folder named root. This is the target location for files being transfered
        over from the Server and must exist

    2.  Compile the VSFTP Client by opening the Client folder in the command prompt. Use the command:
                javac Client.java

    3.  From the Client folder, execute your VSFTP Client with the command:
                java Client

    4.  The VSFTP Client is now live and will request you for an IP address and port to connect to.


Prefix Symbol Guide:
    !, +, # and - lines are server responses.
        ! means you have successfully logged in
        + is a positive response from the server, usually a successful command
        - is a negative response from the server, usually an error
        # is a numbered response from the server

Command Line Interface:
    On both the Server and Client, while running, all command line posts will be prefixed with the local time of when the
    line was sent. For a Server request/reply, it will show 'Server:'. For a Client request/reply, it will show 'Client:'.

    Client
        On the client interface when the Client is waiting for a command to be issued, it will show 'Client: ' with space
        to enter text after. Once a command is sent, it will send it over the Socket to the Server for processing. When
        the Server sends a reply, it will shoe 'Server: ' followed by a server prefix shown above.

    Server
        One the server interface when the Server is waiting for the Client to issue a command, it will show varying forms
        of 'Server: Waiting for client request...'. Any status events like successful commands or such will be encased in
        brackets '[ ]'. Note: The commandline on the Server is only for debugging purposes. The Client should be the main
        commandline viewed during this process after connecting to a port.

Commands:
    USER <username>
        Used to login to the Server from the client. Other commands will be inaccessable until the Client
        is logged in

        Responses:
            + <Username> valid, send password
            
            - Invalid username, try again
    
    PASS <password>
        Used to login to the Server from the client. Only used in conjunction with the USER command

        Responses:
            ! Logged in

            - Wrong password, try again

    LIST
        Responds with a list of files inside the folder named Root on the Server

        Responses:
            N/A

    KILL <file-name>
        Used to remove a file on the Server's root directory.

        Responses:
            + <file-name> deleted

            - Not deleted because (reason)

    RETR <file-name>
        Used to request a file to be transfered over the socket from the Server's root directory
        onto the Clients root directory.

        Responses:
            - File doesn't exist

            # <bytes to be sent> B (conversion to KB, MB, or GB)
                Send?

                This response requires the Client to use the command SEND to retrieve the file or
                STOP to not retrieve the file.

        Client responses:
            SEND
                Sends the file

                Responses:
                    + File transfer Started
                            Processing...
                    + File transfer Complete
            
            STOP
                Aborts the RETR command

                Responses:
                    + File Transfer Stopped
    
    DONE
        Used to end the connection between the Server and the Client, closing both applications

        Responses:
            + Goodbye

