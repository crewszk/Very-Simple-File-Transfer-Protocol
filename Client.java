import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {

    private Socket _socket = null;
    private DataInputStream _input = null;
    private DataOutputStream _output = null;
    private BufferedReader stdin = null;
    private String serverInput = "";
    private String userInput = "";
    private String[] inputArgs;

    public static void main(String[] args) {
        Client _client = new Client();
        Scanner user = new Scanner(System.in);

        System.out.printf("%s CNT4504\t[ Zack's VSFTP Service Socket Client ]\n", _client.returnTime());
        System.out.printf("%s Client:\tIP: ", _client.returnTime());
        String ip = user.nextLine();
        System.out.printf("%s Client:\tPort: ", _client.returnTime());
        int port = user.nextInt();

        _client.startClient(ip, port);
        _client.endClient();
    }

    public void startClient(String ip, int port) {
        try {
            _socket = new Socket(ip, port);
            _input = new DataInputStream(_socket.getInputStream());
            _output = new DataOutputStream(_socket.getOutputStream());
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.printf("%s Client:\t[ Connected ]\n", returnTime());
        }
        catch(IOException e) {
            System.out.printf("%s Client:\t[ IOException Thrown during connection with IP: %s and Port: %d ]\n", returnTime(), ip, port);
        }

        runClient();        
    }

    public void runClient() {
        try {
            serverInput = translate(_input.readUTF());
            System.out.printf("%s Server:\t%s", returnTime(), serverInput);
            if(serverInput.contains("-")) throw new IOException();
        }
        catch(IOException e) {
            System.out.printf("%s Client:\t[ Service Disconnected ]\n", returnTime());
            return;
        }
        
        try {
            System.out.printf("%s Client:\t", returnTime());
            userInput = stdin.readLine();
            inputArgs = userInput.split(" ");
            
            while (userInput != null) {
                if (inputArgs[0].toLowerCase().contains("retr")) {
                    if(!retrieveFile(inputArgs[1])) return;
                }
                else _output.writeUTF(userInput);
                System.out.printf("%s Server:\t%s", returnTime(), translate(_input.readUTF()));
                if (userInput.toLowerCase().contains("done")) return;
                
                System.out.printf("%s Client:\t", returnTime());
                userInput = stdin.readLine();
                inputArgs = userInput.split(" ");
            }
        }
        catch(IOException e) {
            System.out.printf("%s Client:\t[ IOException Thrown during input ]\n", returnTime());
        }
    }
    
    public static class RetrTimeOut implements Callable<String> {
        
        private int fileSize;
        private String filename;
        private DataInputStream _input;
        private DataOutputStream _output;
        private BufferedOutputStream _fileOutput;
        private Socket _socket;
        private Client _client;
        
        RetrTimeOut(int fileSize, String filename,  Client client) {
            this.fileSize = fileSize;
            this._socket = client._socket;
            this.filename = filename;
            this._input = client._input;
            this._client = client;
        }
        
        @Override
        public String call() throws Exception {
            byte [] buffer = new byte[fileSize];
            _fileOutput = new BufferedOutputStream(new FileOutputStream("root/" + filename));
            
            System.out.printf("%s Server:\t%s", _client.returnTime(), _client.translate(_input.readUTF()));
            
            _input.read(buffer);
            _fileOutput.write(buffer);
            
            _fileOutput.close();
            return "success";
        }
    }
    
    public boolean retrieveFile(String arg) throws IOException {
        _output.writeUTF("RETR " + arg);
        serverInput = _input.readUTF();
        if(serverInput.contains("-")) return false;
        
        inputArgs = serverInput.split(" ");
        double fileSize = Integer.parseInt(inputArgs[1]);
        String size = "B";
        if(fileSize > 1000 && fileSize < 1000000) { fileSize /= 1000; size = "KB"; }
        else if(fileSize > 1000000) { fileSize /= 1000000; size = "MB"; }
        else if(fileSize > 1000000000) { fileSize /= 1000000000; size = "GB"; }
        
        serverInput = serverInput.replaceAll("convert", String.format("%.2f %s", fileSize, size));
        
        System.out.printf("%s Server:\t%s", returnTime(), translate(serverInput));
        System.out.printf("%s Client:\t", returnTime());
        userInput = stdin.readLine();
        _output.writeUTF(userInput);
        
        if(userInput.contains("send")) {        
            Future<String> timeout = Executors.newSingleThreadExecutor().submit(
                    new RetrTimeOut(Integer.parseInt(inputArgs[1]), arg, this));
            
            try {
                String result = timeout.get(1, TimeUnit.MINUTES);
                if(result.contains("success")) return true;
            }
            catch (TimeoutException e) {
                timeout.cancel(true);
                System.out.printf("%s Client:\t[ Server Timeout, 5 Minutes Elapsed, Disconnecting from Service ]\n", returnTime());
                return false;
            }
            catch (InterruptedException e) {
                timeout.cancel(true);
                System.out.printf("%s Client:\t[ Server Interrupted Transfer, Disconnecting from Service ]\n", returnTime());
                return false;
            }
            catch (ExecutionException e) {
                timeout.cancel(true);
                System.out.printf("%s Client:\t[ Could not Execute Transfer, Disconnecting from Service ]\n", returnTime());
                return false;
            }
        }
        else {
            System.out.printf("%s Server:\t%s", returnTime(), translate(_input.readUTF()));
        }
        
        return false;
    }
    
    public String translate(String input) {
        String output = "";
        
        output = input.replaceAll("<CRLF>", "\n\t\t\t\t");
        output = output.replaceAll("<NULL>", "\n");
        
        return output;
    }

    public void endClient() {
        try {
            _socket.close();
            _input.close();
            _output.close();
        }
        catch(IOException e) {
            System.out.printf("%s Client:\t[ IOException Thrown while closing client ]", returnTime());
        }
        finally {
            System.out.printf("%s Client:\t[ Disconnected ]", returnTime());
        }
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
