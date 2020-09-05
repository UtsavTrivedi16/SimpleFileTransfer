package sftp;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * @description Responsible for setting up/processing/terminatining data transfer sessions between a client and server
 */
public class SFTP {
    private Socket _connectionSocket;
    private BufferedReader _inputBufferedReader;
    private InputStreamReader _inputStreamReader;
    private DataOutputStream _outputMessage;
    private Logger LOGGER = Logger.getLogger(SFTP.class.getName());
    private String _transmissionType;
    private boolean _isClient;


    /**
     * @detail Constructor for SFTP instance
     * @param connectionSocket for getting input/output streams
     * @param isClient To determine if timeouts are required for file transfers
     */
    public SFTP(Socket connectionSocket, boolean isClient) {

        try{
            this._connectionSocket = connectionSocket;
            this._inputStreamReader = new InputStreamReader(this._connectionSocket.getInputStream());
            this._inputBufferedReader = new BufferedReader(_inputStreamReader);
            this._outputMessage = new DataOutputStream(connectionSocket.getOutputStream());
            this._transmissionType = "B";
            this._isClient = isClient;


        }catch (Exception e){
            System.out.printf("Exception: %s, error while constructing sftp.SFTP attributes\n", e);
        }

    }

    /**
     * @detail Returns bytes as a string
     * @return String message to the Server
     */
    public String readInputStreamAsString() throws IOException {
        String message = "";

        try {

            message = this._inputBufferedReader.readLine();

            // For multiline reading
            while (this._inputBufferedReader.ready()){
                message += ('\n' + this._inputBufferedReader.readLine());
            }

       }catch (SocketException e){
           message = null;
           System.out.printf("Exception %s, check if server was reset!\n", e);
       } catch (IOException e) {
           message = null;
           System.out.printf("Exception %s, error reading message from server!\n", e);
       }

       return message;
    }

    /**
     * @detail Returns Files as a byte array
     * @param messageSize
     * @return
     */
    public byte[] readInputStreamAsBytes(int messageSize){

        byte[] bytesToSend = new byte[messageSize];

        try {


            while(!_inputStreamReader.ready()){
                ;
            }

            if(_isClient){
                System.out.println("\nTimeout of 10 seconds set\n");
                _connectionSocket.setSoTimeout(10*1000);
            }

            for(int i = 0; i < messageSize; i++){

                if(_inputStreamReader.ready()){
                    bytesToSend[i] = (byte) _inputStreamReader.read();
                }else{
                    break;
                }
            }

            if(_isClient){
                _connectionSocket.setSoTimeout(0);
            }



        }catch (SocketException e){
            System.out.printf("Exception %s, check if server was reset!\n", e);
        } catch (IOException e) {
            System.out.printf("Exception %s, error reading message from server!\n", e);
        }catch (Exception e){
            e.printStackTrace();
        }

        return bytesToSend;
    }

    /**
     * @detail Creates a byteStream from a string and sends to destination
     */
    public void writeToOutputStream(String response) {

        try{

            _outputMessage.writeBytes(response + "\r\n");
            _outputMessage.flush();

        }catch (IOException e){
            System.out.printf("Exception %s, check if connection is alive!\n", e);
        }

    }

    /**
     * @detail Creates a byteStream from a File and sends to destination
     * @param fileToSend
     */
    public void writeToOutputStream(File fileToSend){

        try{

            byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());

            _outputMessage.write(fileBytes);

            _outputMessage.flush();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * @detail Sets Transmission type for file transfers
     * @param type
     * @return
     */
    public String setTransmissionType(String type){
        String response;

        switch(type){
            case "A":
                response = "+Using Ascii mode";

                break;

            case "B":
                response = "+Using Binary mode";

                break;

            case "C":
                response = "+Using Continuous mode";

                break;

            default:
                response = "-Type not valid";
                break;

        }

        return response;
    }

    /**
     * @detail Closes session for DONE command
     */
    public void terminateSession(){

        try{
            _connectionSocket.close();
        }catch (IOException e){
            System.out.printf("Exception %s, error closing session!\n", e);
        }
    }


}