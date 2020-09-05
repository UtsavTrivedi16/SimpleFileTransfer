package sftp.server;

import sftp.SFTP;
import sftp.server.credentials.CredentialsManager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static SFTP _sftp;
    private static Integer _port;
    private static ServerSocket _welcomeSocket;
    private static Boolean _isListening, _isConnected;
    private static Boolean _outToLunch;
    private static String _localHost;
    private static CredentialsManager _credentialsManager;
    private static Logger _LOGGER = Logger.getLogger(Server.class.getName());
    private static FileSystem _fileSystemHandle;
    private static boolean _isBypassLogin;
    private static byte[] _fileBytesToReceive;
    private static String _fileNameToReceive;


    /**
     * @detail Blocks till a sftp.client is accepted.
     *         If Out To Lunch is true
     *              -> Client receives (-) response and the connection is closed
     *         Else
     *              -> Client receives (+) response and can send further commands
     *              -> Creates a sftp.SFTP instance for the new Connection Socket
     *              -> Creates a sftp.server.credentials.CredentialsManager instance for managing login process
     *              -> Creates a sftp.server.FileSystem instance for file operations
     *
     *         If BypassLogin is true
     *              -> Login Process is skipped for all commands
     */
    private static void acceptClient() {
        String response;
        Socket connectionSocket;

        try {
            connectionSocket = _welcomeSocket.accept();
            _sftp = new SFTP(connectionSocket, false);
            _localHost = InetAddress.getLocalHost().toString();
            System.out.printf("Listening to Client at INetAddress: %s, connectionSocketPort: %s\n", connectionSocket.getInetAddress(), connectionSocket.getPort());

            if (_outToLunch) {
                response = '-' + _localHost + ' ' + "Out to Lunch";
                _sftp.writeToOutputStream(response);
                _sftp.terminateSession();
                _isConnected = false;
            } else {
                response = '+' + _localHost + ' ' + "SFTP Service";
                _credentialsManager = new CredentialsManager();

                if (_isBypassLogin) {
                    _credentialsManager.setIsBypass(_isBypassLogin);
                    _LOGGER.info("BYPASS Login Mode\n");
                }

                _credentialsManager.loadUsers();
                _sftp.writeToOutputStream(response);
                _isConnected = true;
            }

            _isListening = true;

        } catch (IOException e) {
            response = '-' + _localHost + ' ' + "Error with SFTP Service";
            e.printStackTrace();
        }

    }

    /**
     * @detail Checks if command and its respective arguments are in the correct format
     * @param request
     * @return If client request can be processed
     */
    private static boolean isValidRequestFormat(String request[]) {

        String cmd = null;
        boolean isValid = true;

        if (request.length >= 1) {
            cmd = request[0];

            switch (cmd) {

                case "USER":
                    if (request.length != 2) {
                        isValid = false;
                    }
                    break;

                case "ACCT":
                    if (request.length != 2) {
                        isValid = false;
                    }
                    break;

                case "CDIR":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "NAME":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "TOBE":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "TYPE":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "SEND":
                    if (request.length > 1) {
                        isValid = false;
                    }

                    break;

                case "STOP":
                    if (request.length > 1) {
                        isValid = false;
                    }

                    break;

                case "RETR":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "STOR":
                    if (request.length != 3) {
                        isValid = false;
                    } else if (!request[1].equals("NEW") && !request[1].equals("OLD") && !request[1].equals("APP")) {
                        isValid = false;
                    }

                    break;

                case "KILL":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "SIZE":
                    if (request.length != 2) {
                        isValid = false;
                    }

                    break;

                case "PASS":
                    if (request.length != 2) {
                        isValid = false;
                    }
                    break;

                case "LIST":
                    if (request.length != 2 && request.length != 3) {
                        isValid = false;
                    } else if (!request[1].equals("F") && !request[1].equals("V")) {
                        isValid = false;
                    }
                    break;

                case "DONE":
                    if (request.length > 1) {
                        isValid = false;
                    }
                    break;

                default:
                    isValid = false;
                    break;
            }

        } else {
            isValid = false;
        }

        return isValid;
    }

    /**
     * @detail Calls one of the handleX commands if the client request was in the correct format.
     *         User must be the first command else it will respond demanding a user-id.
     *         Commands after that might require logging in to be processed.
     */
    private static void processClientRequest() {

        String[] request;
        String message;

        try {
            message = _sftp.readInputStreamAsString();

            if(message.isEmpty()){
                request = null;

            }else{
                String cmd = null;
                ArrayList<String> args = new ArrayList<>();
                boolean isValid;

                request = message.split(" ");

                isValid = isValidRequestFormat(request);

                if (isValid) {
                    cmd = request[0];

                    if (cmd.equals("USER")) {
                        args.add(request[1]);
                        handleUSER(args.get(0));
                    } else {

                        if (_credentialsManager.isCurrentUserSelected()) {

                            switch (cmd) {

                                case "ACCT":
                                    args.add(request[1]);
                                    handleACCT(args.get(0));

                                    break;

                                case "CDIR":
                                    args.add(request[1]);
                                    handleCDIR(args.get(0));

                                    break;

                                case "PASS":
                                    args.add(request[1]);
                                    handlePASS(args.get(0));

                                    break;

                                case "NAME":
                                    args.add(request[1]);
                                    handleNAME(args.get(0));

                                    break;

                                case "TOBE":
                                    args.add(request[1]);
                                    handleTOBE(args.get(0));

                                    break;

                                case "TYPE":
                                    args.add(request[1]);
                                    handleTYPE(args.get(0));

                                    break;

                                case "KILL":
                                    args.add(request[1]);
                                    handleKILL(args.get(0));

                                    break;

                                case "RETR":
                                    args.add(request[1]);
                                    handleRETR(args.get(0));

                                    break;

                                case "SEND":
                                    handleSEND();

                                    break;

                                case "SIZE":
                                    args.add(request[1]);
                                    handleSIZE(args.get(0));

                                    break;

                                case "STOP":
                                    handleSTOP();

                                    break;

                                case "STOR":
                                    for (int i = 1; i < request.length; i++) {
                                        args.add(request[i]);
                                    }

                                    handleSTOR(args);

                                    break;

                                case "LIST":
                                    for (int i = 1; i < request.length; i++) {
                                        args.add(request[i]);
                                    }

                                    handleLIST(args);

                                    break;

                                case "DONE":
                                    handleDONE();

                                    break;
                            }

                        } else {
                            _sftp.writeToOutputStream("-No User-id selected");
                        }

                    }

                } else {
                    _sftp.writeToOutputStream("-Invalid command");
                }

            }

        } catch (IOException e) {
            request = null;
            _isConnected = false;
            e.printStackTrace();
        }

    }

    /**
     * @detail Sets Transfer Type in sftp instance
     * @param type
     */
    private static void handleTYPE(String type) {
        String response;


        if(_credentialsManager.isAUserLoggedIn()) {


           response = _sftp.setTransmissionType(type);

        }else{

            _fileNameToReceive = null;
            response = "- No Login found";

        }

        _sftp.writeToOutputStream(response);
    }

    /**
     * @detail Works only if a user is logged in.
     *         Checks if free space is available in the server for storing files from client.
     *         If free space exists:
     *              -> Attempts to retrieve file byte array from client via sftp instance
     *              -> Passes byte array to file system for continuing STOR sequence
     *              -> Returns response on whether it was successful.
     *         Else:
     *              -> Quits STOR sequence.
     * @param fileSize
     */
    private static void handleSIZE(String fileSize) {
        String response;


        if(_credentialsManager.isAUserLoggedIn()) {

            try{

                int clientFilesize = Integer.parseUnsignedInt(fileSize);
                response = _fileSystemHandle.checkFreeSpaceForSTOR(clientFilesize);
                _sftp.writeToOutputStream(response);

                if(response.contains("-")){
                    _fileNameToReceive = null;
                }

                if(_fileNameToReceive != null){
                    _fileBytesToReceive = _sftp.readInputStreamAsBytes(clientFilesize);
                    response = _fileSystemHandle.processSTORSequence(_fileBytesToReceive, _fileNameToReceive);
                    _sftp.writeToOutputStream(response);
                    _fileNameToReceive = null;
                    _fileBytesToReceive = null;

                }

            }catch (NumberFormatException e){
                response = "-Size is invalid";
                _sftp.writeToOutputStream(response);
                e.printStackTrace();
            }

        }else{

            _fileNameToReceive = null;
            response = "- No Login found";
            _sftp.writeToOutputStream(response);

        }

    }

    /**
     * @detail Works only if a user is logged in.
     *         Quits RETR sequence if Client determines that free space is not available for the selected file
     */
    private static void handleSTOP() {
        String response;

        if(_credentialsManager.isAUserLoggedIn()){

            response = _fileSystemHandle.cancelSend();

        }else{

            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);
    }

    /**
     * @detail Works only if a user is logged in.
     *         Initiates a file storing process from client.
     *         Depending on Mode:
     *              -> NEW: Creates a file if it does note exist else makes a duplicate
     *              -> OLD: Creates a file if it does not exist else will overwrite
     *              -> APP: Creates a file if it does not exist else appends to it
     * @param args
     */
    private static void handleSTOR(ArrayList<String> args) {
        String response;
        String fileName, mode;

        mode = args.get(0);
        fileName = args.get(1);

        if(_credentialsManager.isAUserLoggedIn()) {
            response = _fileSystemHandle.setFileOperation(fileName, mode);
            _fileNameToReceive = fileName;
        }else{
            _fileBytesToReceive = null;
            _fileNameToReceive = null;
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Works only if a user is logged in.
     *         Confirms file to be sent from server to client if space is available
     */
    private static void handleSEND() {
        String response;

        if(_credentialsManager.isAUserLoggedIn()){

            File serverFile = _fileSystemHandle.getFileToSend();

            if(serverFile != null){
                _sftp.writeToOutputStream(serverFile);
                response = "+File Saved on Client's side";
            }else{
                response = "-No File selected on remote server";
            }

        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Works only if a user is logged in.
     *         Initiates file transfer from Server to Client.
     * @param fileName
     */
    private static void handleRETR(String fileName) {
        String response;

        if(_credentialsManager.isAUserLoggedIn()){
            response = _fileSystemHandle.getRequestedFileSize(fileName);
        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Works only if a user is logged in.
     *         Deletes a file in the Server Filesystem if it exists in the current directory
     * @param fileName
     */
    private static void handleKILL(String fileName) {
        String response;

        if (_credentialsManager.isAUserLoggedIn()) {
            response = _fileSystemHandle.deleteFile(fileName);
        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Works only if a user is logged in.
     *         Requests Server for renaming a file if it exists
     * @param fileName
     */
    private static void handleNAME(String fileName) {
        String response;

        if (_credentialsManager.isAUserLoggedIn()) {
            response = _fileSystemHandle.checkFileName(fileName);
        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);
    }

    /**
     * @detail Works only if a user is logged in.
     *         Renames a file in the Server FileSystem if NAME command sends a positive response
     * @param fileName
     */
    private static void handleTOBE(String fileName){
        String response;

        if (_credentialsManager.isAUserLoggedIn()) {
            response = _fileSystemHandle.changeFileName(fileName);
        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);
    }

    /**
     * @detail Works only if a user is logged in.
     *         Requests FileSystem to change the current directory
     * @param dir
     */
    private static void handleCDIR(String dir) {
        String response;

        if (_credentialsManager.isAUserLoggedIn()) {

            response = _fileSystemHandle.changeCurrentDir(dir);

        }else{
            response = "- No Login found";
        }

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Works only if a user is logged in.
     *         Lists files in a specified or current directory
     * @param args F and V for File Listing Mode
     */
    private static void handleLIST(ArrayList<String> args) {
        String response = "";
        String dir, mode;

        mode = args.get(0);

        if (args.size() == 2) {
            dir = args.get(1);
        } else {
            dir = "";
        }

        if (_credentialsManager.isAUserLoggedIn()) {

            response = _fileSystemHandle.listDir(dir, mode);

            _sftp.writeToOutputStream(response);

        } else {
            _sftp.writeToOutputStream("- No Login found");
        }

    }

    /**
     * @detail Attempts Logging in a user for accessing the file system
     * @param password
     */
    private static void handlePASS(String password) {
        String response;

        response = _credentialsManager.attemptLogin(password);

        _sftp.writeToOutputStream(response);
    }

    /**
     * @detail Closes Connection on both client and server
     */
    private static void handleDONE() {

        _sftp.writeToOutputStream("+ Thanks for using " + _localHost + " SFTP Service. Goodbye!");
        _sftp.terminateSession();
        _isConnected = false;
    }

    /**
     * @detail Selects a current account based on the User-id selected
     * @param accountName
     */
    private static void handleACCT(String accountName) {

        String response;

        response = _credentialsManager.setCurrentAccount(accountName);

        _sftp.writeToOutputStream(response);

    }

    /**
     * @detail Selects a user-id to continue the login process
     * @param userName
     */
    private static void handleUSER(String userName){

        String response = _credentialsManager.setCurrentUser(userName);

        _sftp.writeToOutputStream(response);

    }

    public static void main(String argv[]) throws Exception
    {

        _port = 6789;
        _isListening = false;
        _isConnected = false;
        _outToLunch = false;
        _isBypassLogin = false;

        _LOGGER.setLevel(Level.INFO);
        _fileSystemHandle = new FileSystem("sftp.server");
        _welcomeSocket = new ServerSocket(_port);
        _LOGGER.info("Server Setup at port: " + _welcomeSocket.getLocalPort());

        while(true){
            try{

                if(!_isListening && !_isConnected || _isListening && !_isConnected) {
                    acceptClient();

                }else if(_isListening && _isConnected){
                    processClientRequest();

                }

            }catch (NullPointerException e) {
                _isConnected = false;
                System.out.printf("Check if Client is alive! Try connecting again!\n");
            }

        }

    }

}