package sftp.client;

import sftp.SFTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static Integer _serverPort;
    private static Boolean _isConnected;
    private static InetAddress _localHost;
    private static BufferedReader _inputFromUser;
    private static SFTP _sftp;
    private static int _fileReceiveSize;
    private static Socket _clientSocket;
    private static boolean _isExit, _isResponseFileSize, _isResponseBytes, _isRequestSendingFile;
    private static Path _clientDir;
    private static String _fileReceiveName;
    private static File _fileToSend;
    private static Logger LOGGER = Logger.getLogger(Client.class.getName());

    /**
     * @detail Attempts a connection to a specified sftp.server port and ip address
     */
    private static void connectToServer() {

        try{
            _localHost = InetAddress.getLocalHost();
            _clientSocket = new Socket(_localHost, _serverPort);
            _isConnected = true;
            LOGGER.info("Connected to TCP Server INetAddress:" + ' ' + _clientSocket.getInetAddress());
        }catch (IOException e){
            System.out.printf("Exception %s, error getting localhostname!\n", e);
        }

    }

    /**
     * @detail Instantiates a sftp object and input Reader from the User.
     *         Server confirms if data transfers can be conducted
     */
    private static void setupIO(){

        _inputFromUser = new BufferedReader(new InputStreamReader(System.in));
        _sftp = new SFTP(_clientSocket, true);
        getServerResponse();
    }

    /**
     * @detail Receives File and/or String response from the Server
     */
    private static void getServerResponse() {

        try {
            
            if (_isResponseBytes) {
                byte[] fileBytes = _sftp.readInputStreamAsBytes(_fileReceiveSize);
                Files.write(Path.of(_clientDir + "/" + _fileReceiveName), fileBytes);
                _isResponseBytes = false;
                _fileReceiveName = null;
            }

            String response = _sftp.readInputStreamAsString();
            System.out.println("Server response: " + response);
            processStringResponse(response);

        } catch (IOException e) {
            System.out.printf("Exception %s, error reading message to Server\n", e);
        }

    }

    /**
     * @detail Sends a client Request to Server
     */
    private static void requestServer(){

        try{

            if(!_isRequestSendingFile) {
                String isValidRequest;

                System.out.printf("Input command: ");
                String message = _inputFromUser.readLine();

                isValidRequest = processUserCommand(message);

                if(!isValidRequest.equals("-")){
                    _sftp.writeToOutputStream(isValidRequest);
                    getServerResponse();
                }

            }else{
                
                _sftp.writeToOutputStream(_fileToSend);
                _fileToSend = null;
                _isRequestSendingFile = false;
                getServerResponse();
            }


        }catch (IOException e){
            System.out.printf("Exception %s, error sending message to Server\n", e);
        }


    }

    /**
     * @detail Checks if a user command is valid as a request to the server
     *         and anticipates certain responses by setting flags
     * @param message
     * @return
     */
    private static String processUserCommand(String message) throws IOException {
        String isValidRequest = message.strip();

        if (message.contains("STOR")) {
            String[] args = message.split(" ");

            if(args.length == 3){
                String fileName = args[2];

                if (!Files.exists(Paths.get(_clientDir + "/" + fileName))) {
                    _fileToSend = null;
                    isValidRequest = "-";
                    System.out.println("Error: File does not exist on Client System. Try again");
                }else{
                    _fileToSend = new File(_clientDir + "/" + fileName);
                }
            }

        } else if (message.contains("RETR")) {
            String[] args = message.split(" ");

            if(args.length == 2){
                _isResponseFileSize = true;
                _fileReceiveName = args[1];
            }


        } else if (message.contains("SEND")) {
            String[] args = message.split(" ");

            if (args.length == 1 && _fileReceiveName != null) {
                _isResponseBytes = true;
            }

        } else if (message.contains("SIZE")) {
            String[] args = message.split(" ");

            if (args.length == 1 && _fileToSend != null) {
                isValidRequest = message + ' ' + String.valueOf(_fileToSend.length());
            }
        }

        return isValidRequest;
    }

    /**
     * @detail Conducts actions like exiting or setting flags for receiving files
     *         based on the Server Response
     * @param response
     */
    private static void processStringResponse(String response) {

        if(response != null){

            if(response.charAt(0) == '-'){

                if(response.contains("Lunch")) {
                    _isExit = true;

                }else if(_isResponseFileSize){
                    _isResponseFileSize = false;
                    _fileReceiveName = null;
                }

            }else {

                if(response.contains("Goodbye")){
                    _isExit = true;

                }else if(_isResponseFileSize){
                    _fileReceiveSize = Integer.parseInt(response);
                    _isResponseFileSize = false;

                }else if(response.contains("+ok, waiting for file")){
                    _isRequestSendingFile = true;

                }else if (response.contains("+ok, RETR aborted") && _fileReceiveName != null){
                        _isResponseBytes = false;
                        _fileReceiveName = null;
                }
            }
        }
    }

    public static void main(String argv[]) throws Exception {

        _serverPort = 6789;
        _isConnected = false;
        _isExit = false;
        _isRequestSendingFile = false;
        _isResponseBytes = false;
        _isResponseFileSize = false;
        LOGGER.setLevel(Level.INFO);
        _fileReceiveSize = 0;
        _clientDir = Paths.get(System.getProperty("user.dir") + "/resources/sftp.client/");

        while (true) {

            try {

                if(_isExit){
                    _isConnected = false;
                    _clientSocket.close();
                    break;
                }else{

                    if (!_isConnected) {
                        connectToServer();
                        setupIO();
                    }else{
                        requestServer();
                    }

                }

            }catch (ConnectException e) {
                _clientSocket.close();
                System.out.printf("Exception %s, attempting to reconnect! Check if Server is alive!\n", e);

            }catch (SocketException e) {
                _clientSocket.close();
                System.out.printf("Exception %s, attempting to reconnect! Check if socket is alive!\n", e);

            }catch (Exception e){
                _clientSocket.close();
                _isConnected = false;
                System.out.printf("Exception %s, attempting to reconnect!\n",e);
            }

        }
    }

}
