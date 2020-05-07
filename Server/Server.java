import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;

public class Server {

    private ServerSocket _server = null;
    private Socket _socket = null;
    private DataOutputStream _output = null;
    private DataInputStream _input = null;

    public static void main(String[] args) {
        Server _currentServer = new Server();
        Scanner user = new Scanner(System.in);
        System.out.printf("%s CNT4504\t[ Zack's VSFTP Service Socket Server ]\n", _currentServer.returnTime());
        System.out.printf("%s Server:\tRequested Port: ", _currentServer.returnTime());
        int port = user.nextInt();
        
        _currentServer.startServer(port);
    }

    public void startServer(int port) {
        String disconnectionReason = "";
        
        System.out.printf("%s Server:\tConnecting with port %d\n", returnTime(), port);
        try {
            _server = new ServerSocket(port);
            System.out.printf("%s Server:\t[ Connected ]\n", returnTime());
            
            System.out.printf("%s Server:\tWaiting for client connection...\n", returnTime());
            _socket = _server.accept();
            System.out.printf("%s Server:\tClient Connection Successful\n", returnTime());
            
            _output = new DataOutputStream(_socket.getOutputStream());
            _input = new DataInputStream(_socket.getInputStream());
            
            runServer(port);
        } 
        catch(FileNotFoundException e) {
            System.out.printf("%s Server:\t[ users.txt file missing ]\n", returnTime());
        } 
        catch(InterruptedException e) {
            System.out.printf("%s Server:\t[ InterruptedException thrown ]\n", returnTime());
        }
        catch(IOException e) {
            System.out.printf("%s Server:\t[ Disconnected from Client ]\n", returnTime());
        } 
        finally {
            System.out.printf("%s Server:\tShutting down service...\n", returnTime());
            endServer();
        }
    }

    public void runServer(int port) throws IOException, InterruptedException{
        String userInput;
        String[] args;
        System.out.printf("%s Server:\t[ Ready ]\n", returnTime());
        boolean login = false;
        
        _output.writeUTF("+ Welcome to the VSFTP Service <NULL>");
        do {
            System.out.printf("%s Server:\tWaiting for client request...\n", returnTime());
            userInput = _input.readUTF();
            System.out.printf("%s Client:\t%s\n", returnTime(), userInput);
            args = userInput.split(" ");

            switch (args[0].toLowerCase()) {
                case "user": 
                    if(!login) {
                        try {
                            login = serverLogin(args[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            _output.writeUTF("- Invalid user-id, try again <NULL>");
                            System.out.printf("%s Server:\t[ Argument not provided ]\n", returnTime());
                        }
                    }
                    else {
                        _output.writeUTF("- Poor Syntax, you're already logged in <NULL>");
                        System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                    }
                    break;
                case "pass": 
                    _output.writeUTF("- Poor syntax, command only used with 'USER' command <NULL>");
                    System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                    break;
                case "list": 
                    if(!login) {
                        _output.writeUTF("- Not logged in, login with 'USER <username>' <NULL>");
                        System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                        break; 
                    } 
                    else {
                        listCommand();
                    }
                    break;
                case "retr":
                    if(!login) {
                        _output.writeUTF("- Not logged in, login with 'USER <username>' <NULL>");
                        System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                        break;
                    }
                    else {
                        try {
                            retrCommand(args[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            _output.writeUTF("- File doesn't exist <NULL>");
                            System.out.printf("%s Server:\t[ No filename provided ]\n", returnTime());
                        }
                    }
                    break;
                case "kill":
                    if(!login) {
                        _output.writeUTF("- Not logged in, login with 'USER <username>' <NULL>");
                        System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                        break;
                    }
                    else {
                        try {
                            killCommand(args[1]);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            _output.writeUTF("- Not deleted because no filename was provided <NULL>");
                            System.out.printf("%s Server:\t[ No filename provided ]\n", returnTime());
                        }
                    }
                    break;
                case "done":
                    _output.writeUTF("+ Goodbye <NULL>");
                    return;
                default:
                    _output.writeUTF("- Command not recognized, try again <NULL>");
                    System.out.printf("%s Server:\t[ Bad command syntax used ]\n", returnTime());
                    break;
            }
        } while (true);
    }

    public boolean serverLogin(String arg) throws IOException {
        String input = "";
        String[] inputArgs;
        File users = new File("users.txt");
        final Scanner scanFile = new Scanner(users);

        while (scanFile.hasNextLine()) {
            String user = scanFile.next();
            if(user.contains(arg)) {
                _output.writeUTF(String.format("+ %s valid, send password <NULL>", arg));
                System.out.printf("%s Server:\tWaiting for password request...\n", returnTime());
                
                input = _input.readUTF();
                user = scanFile.next();
                inputArgs = input.split(" ");
                
                if(inputArgs[0].toLowerCase().contains("pass")) {
                    if(inputArgs[1].contains(user)) {
                        System.out.printf("%s Server:\t[ Client login successful ]\n", returnTime());
                        _output.writeUTF("! Logged in <NULL>");
                        scanFile.close();
                        return true;
                    }
                    else {
                        System.out.printf("%s Server:\t[ Client login failed ]\n", returnTime());
                        _output.writeUTF("- Wrong password, try again <NULL>");
                        scanFile.close();
                        return false;
                    }
                }
                else {
                    System.out.printf("%s Server:\t[ Client login failed ]\n", returnTime());
                    _output.writeUTF("- Poor Syntax, command syntax is 'PASS <password>', try again <NULL>");
                    scanFile.close();
                    return false;
                }
            }
            scanFile.nextLine();
        }
        
        System.out.printf("%s Server:\t[ Client login failed ]\n", returnTime());
        _output.writeUTF("- Invalid username, try again <NULL>");
        scanFile.close();
        return false;
    }
    
    public void listCommand() throws IOException {
        File root = new File("root");
        File[] rootFiles = root.listFiles();
        String serverOutput = "+ Root Folder";
        
        for(int i = 0; i < rootFiles.length; i++) {
            serverOutput += "<CRLF>";
            serverOutput += String.format("%s", rootFiles[i].getName());
        }
        
        serverOutput += "<NULL>";
        _output.writeUTF(serverOutput);
        System.out.printf("%s Server:\t[ LIST reply Sent ]\n", returnTime());
    }
    
    public void retrCommand(String arg) throws IOException {
        String input = "";
        File file = new File(String.format("root/%s", arg));
        
        
        BufferedInputStream _fileInput;
        
        if (!file.exists()) {
            _output.writeUTF("- File doesn't exist <NULL>");
            System.out.printf("%s Server:\t[ File Retrieval Failed - Filename %s Not Found ]\n", returnTime(), arg);
            return;
        }
        
        System.out.printf("%s Server:\t[ File transfer started ]\n", returnTime());
        
        byte[] buffer = new byte[(int)file.length()];
        
        _output.writeUTF(String.format("# %d B (convert) <CRLF>Send?<NULL>", file.length()));
        input = _input.readUTF().toLowerCase();
        System.out.printf("%s Client:\t%s\n", returnTime(), input);
        if(!input.contains("send")) {
            if(input.contains("stop")) {
                System.out.printf("%s Server:\t[ File Retrieval stopped by Client ]\n", returnTime());
                _output.writeUTF("+ File Transfer Stopped <NULL>");
            }
            else {
                System.out.printf("%s Server:\t[ File Retrieval stopped, unknown command ]\n", returnTime());
                _output.writeUTF("- File transfer stopped, unknown command used <NULL>");
            }
            return;
        }
        
        _fileInput = new BufferedInputStream(new FileInputStream(file));
        
        _output.writeUTF("+ File transfer Started<CRLF>Processing...<NULL>");
        
        _fileInput.read(buffer);
        _output.write(buffer);
        
        _fileInput.close();
        _output.writeUTF("+ File Transfer Complete<NULL>");
    }
    
    public void killCommand(String arg) throws IOException {
        File delete = new File("root/" + arg);
        
        if(!delete.exists()) {
            _output.writeUTF("- Not deleted because file does not exist <NULL>");
            System.out.printf("%s Server:\t[ File: %s doesn't exist ]\n", returnTime(), arg);
        }
        else {
            if(delete.delete()) {
                _output.writeUTF(String.format("+ %s deleted <NULL>", arg));
                System.out.printf("%s Server:\t[ File: %s deleted from Root ]\n", returnTime(), arg);
            }
            else {
                _output.writeUTF("- Not deleted because command failed to execute for unknown reason <NULL>");
                System.out.printf("%s Server:\t[ File: %s failed to delete for unknown reason ]\n", returnTime());
            }
        }
    }

    public void endServer() {
        try {
            _server.close();
            _socket.close();
            _input.close();
            _output.close();
        }
        catch(NullPointerException e) {
            System.out.printf("%s Server:\t[ Server Shutdown, a service didn't fully connect ]\n", returnTime());
            return;
        }
        catch(IOException e) {
            System.out.printf("%s Server:\t[ Unknown IOException Error, FORCE EXIT ]\n", returnTime());
            return;
        }

        System.out.printf("%s Server:\t[ Disconnected ]\n", returnTime());
    }
    
    public String returnTime() {
        int hour = LocalTime.now().getHour();
        int minute = LocalTime.now().getMinute();
        int second = LocalTime.now().getSecond();
        String thisHour = "";
        String thisMinute = "";
        String thisSecond = "";
        
        if(hour < 10) thisHour = "0" + hour;
        else thisHour += hour;
        
        if(minute < 10) thisMinute = "0" + minute;
        else thisMinute += minute;
        
        if(second < 10) thisSecond = "0" + second;
        else thisSecond += second;
        
        return thisHour + ":" + thisMinute + ":" + thisSecond;
    }
}
