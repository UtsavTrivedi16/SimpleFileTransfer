package sftp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class FileSystem {

    private File _rootDir, _baseDir, _currentDir;
    private File _fileToSend;
    private String _fileToRename;
    private String _currSTOR;
    private final long newFileLimit;
    private final long fileSizeLimit;

    /**
     * @detail Contructor for Server fileSystem. Accepts a base directory for home
     * @param baseDir
     */
    FileSystem(String baseDir){
        this._rootDir = new File(System.getProperty("user.dir") + "/resources/");
        this._baseDir = new File(_rootDir.toString() + "/" + baseDir);
        this._currentDir = new File(_baseDir.toString());
        this.newFileLimit = 10000;
        this.fileSizeLimit = 50;
    }

    /**
     * @detail If verbose mode (V) is selected it displays:
     *          -> Time of Creation
     *          -> Last accessed
     *          -> Last modified
     * @param dir  Specific directory's contents to list
     * @param mode F for standard formatted directory listing
     *             and V for verbose directory listing
     * @return
     */
    String listDir(String dir, String mode){
       String response;
       Path path = Paths.get(_currentDir + "/" + dir);
       File[] contentList = new File(_currentDir + "/" + dir).listFiles();
       StringBuilder contents = new StringBuilder();

       if(contentList == null){
           contents.append("-Invalid Directory");
       }else{
           contents.append("\r\n+Contents");

           if(mode.equals("F")){

               for(File file : contentList){

                   if(file.isDirectory()){
                       contents.append("\r\n    " + file.getName() + '/');
                   }else if(file.isFile()){
                       contents.append("\r\n    " + file.getName());
                   }

               }

           }else if(mode.equals("V")){

               BasicFileAttributes dateInfo;
               String createdAt, lastAccessed, lastModified;

               DateFormat date = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

               for(File file : contentList){
                   try {
                       dateInfo = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);

                       if(file.isDirectory()){
                           contents.append("\r\n    " + file.getName() + "/ ");
                       }else if(file.isFile()){
                           contents.append("\r\n    " + file.getName() + "  ");
                       }

                       createdAt = " created time: " + date.format(dateInfo.creationTime().toMillis());
                       lastAccessed= " last accessed time: " + date.format(dateInfo.lastAccessTime().toMillis());
                       lastModified = " last modified time: " + date.format(dateInfo.lastModifiedTime().toMillis());

                       contents.append(createdAt + lastAccessed + lastModified);


                   } catch (IOException e) {
                       e.printStackTrace();
                   }

               }


           }


       }

       contents.append("\r\n");

       response = contents.toString();

       return response;

    }

    /**
     * @detail Changes current directory if valid
     * @param dir Directory to change to
     * @return
     */
    String changeCurrentDir(String dir){
        String response = "";
        File _newPath;


        if(dir.equals("..") || dir.equals("../")){
            _newPath = new File(_currentDir.getParent());
        }else if(dir.equals("/")){
            _newPath = new File(_baseDir.getPath());
        }else{
            _newPath = new File(_currentDir.getPath() + "/" + dir);
        }

        File[] contentList = _newPath.listFiles();

        if(contentList == null){
            response = "-Can't connect to directory because: (It does not exist)";

        }else if(contentList.length == 0) {
            _currentDir = new File(_newPath.getPath());
            response = "!Changed working dir to " + _currentDir.getAbsolutePath();

        }else{

            for(File file : contentList){

                if(file.getAbsolutePath().contains("sftp.client/") || file.getAbsolutePath().contains("src/")){
                    response = "-Can't connect to directory because: (Outside Server File System)";
                    break;
                }else {
                    _currentDir = new File(_newPath.getPath());
                    response = "!Changed working dir to " + _currentDir.getAbsolutePath();
                }
            }
        }


        return response;
    }

    /**
     * @detail Allows renaming of a file using TOBE command if it exists
     * @param fileName
     * @return
     */
    String checkFileName(String fileName){
        String response;

        if(checkFileExists(fileName, false)){
            response = "+File exists";
            _fileToRename = fileName;
        }else{
            response = "-Can't find " + fileName + "\n  NAME command is aborted, don't send TOBE";
            _fileToRename = null;
        }

        return response;

    }

    /**
     * @detail Checks if file exists in current directory or entire server file system
     * @param fileName Name of file to find
     * @param isWholeSystem Scans directories from base directory if true
     * @return
     */
    boolean checkFileExists(String fileName, boolean isWholeSystem){
        Path filePath = null;
        
        if(!isWholeSystem){
            filePath = Paths.get(_currentDir.getPath() + "/" + fileName);

        }else{

            if(fileName.contains(".txt")){
                filePath = Paths.get(_baseDir.getPath() + "/text/" + fileName);
            }else{
                filePath = Paths.get(_baseDir.getPath() + "/other/" + fileName);
            }

        }

        return Files.exists(filePath);

    }

    /**
     * @detail Changes filename if NAME command sends a positive response
     * @param newFileName
     * @return
     */
    String changeFileName(String newFileName){
        String response;

        if(_fileToRename != null){

            File oldFile = new File(_currentDir + "/" + _fileToRename);
            File renamedFile = new File(_currentDir + "/" + newFileName);

            if(oldFile.renameTo(renamedFile)){
                response = "+" + oldFile.getPath() + " renamed to " + renamedFile.getPath();
                _fileToRename = null;
            }else{
                response = "-File wasn't renamed as renaming failed";
            }

        }else{
            response = "-File wasn't renamed because file is not specified";
        }

        return response;
    }

    /**
     * @detail Deletes file specified by KILL command if it exists
     * @param fileName
     * @return
     */
    String deleteFile(String fileName){
        String response;
        Path filePath = Paths.get(_currentDir.getPath() + "/" + fileName);

        if(Files.exists(filePath)){

            File fileToDelete = new File(filePath.toString());

            if(fileToDelete.delete()){
                response = "+" + fileToDelete.getName() + " deleted";
            }else{
                response = "-Not deleted because deleting process failed";
            }


        }else{
            response = "-Not deleted because file does not exist";
        }

        return response;
    }

    /**
     * @detail Returns fileSize and keeps a record of the file to be sent
     * @param fileName
     * @return
     */
    String getRequestedFileSize(String fileName){
        String response;

        if(checkFileExists(fileName, false)){
            File file = new File(_currentDir + "/" + fileName);
            long fileSizeInBytes = file.length();
            response = String.valueOf(fileSizeInBytes);
            _fileToSend = file;

        }else{
            response = "-File doesn't exist";
            _fileToSend = null;
        }

        return response;
    }

    /**
     * @detail Discards File to be sent on STOP command
     * @return
     */
    String cancelSend(){

        if(_fileToSend != null){
            _fileToSend = null;
            return "+ok, RETR aborted";
        }else{
            return "-No File selected on remote server";
        }
    }

    /**
     * @detail Sets File operation depending on STOR command
     * @param fileName
     * @param mode OLD for overwriting, NEW for creating a duplicate
     *             and APP for appending to an existing text file.
     * @return
     */
    String setFileOperation(String fileName, String mode) {
       String response = "";
       boolean doesFileExist;

       doesFileExist = checkFileExists(fileName, true);

        switch (mode){
            case "NEW":
                if(doesFileExist){
                    response = "+File exists, will create new generation of file";

                }else{
                    response = "+File does not exist, will create new file";
                }

                break;

            case "OLD":
                if(doesFileExist){
                    response = "+Will write over old file";

                }else{
                    response = "+Will create new file";
                }

                break;

            case "APP":
                if(doesFileExist){
                    response = "+Will append to file";

                }else{
                    response = "+Will create file";
                }

                break;

            default:
                _currSTOR = "IDLE";
                break;
        }

        _currSTOR = mode;

        return response;

    }

    /**
     * @detail Checks if space is available for a file to be received
     * @param sizeInBytes
     * @return
     */
    String checkFreeSpaceForSTOR(long sizeInBytes){
        String response;

        if(_currSTOR.equals("IDLE")){
            response = "-STOR operation was not specified";
        }else{

            if(fileSizeLimit > sizeInBytes){
                response = "+ok, waiting for file";
            }else{
                response = "-Not enough room, don't send it";
                _currSTOR = "IDLE";
            }

        }

        return response;
    }

    /**
     * @detail Conducts an overwrite, new file creating or append
     * @param fileBytes File in bytes from a client
     * @param fileName Name of the file
     * @return
     */
    String processSTORSequence(byte[] fileBytes, String fileName){
        String response="";
        boolean doesFileExist;

        doesFileExist = checkFileExists(fileName, true);

        try {

            if(!doesFileExist){
                Path path;

                if(fileName.contains(".txt")){
                    path = Paths.get(_baseDir + "/text/" + fileName);
                }else{
                    path = Paths.get(_baseDir + "/other/" + fileName);
                }

                Files.write(path, fileBytes);
                response = "+Saved " + path;

            }else{

                    switch(_currSTOR){

                        case "IDLE":
                            response = "-Couldn't save because STOR operation was not specified";

                            break;

                        case "NEW":
                            String pathName;

                            if(fileName.contains(".txt")){
                                pathName = _baseDir + "/text/";
                            }else{
                                pathName = _baseDir + "/other/";
                            }

                            response = "-Couldn't save because file limit of duplicate files was reached";

                            for(int i = 0; i < newFileLimit; i++){

                                doesFileExist = checkFileExists("new_" + i + '_' + fileName, true);

                                if(!doesFileExist){
                                    Path path = Paths.get(pathName + "new_" + i + '_' + fileName);
                                    Files.write(path, fileBytes);
                                    response = "+Saved " + path.toString();
                                    break;
                                }

                            }

                            break;

                        case "OLD":
                            Path path;

                            if(fileName.contains(".txt")){
                                path = Paths.get(_baseDir + "/text/" + fileName);

                            }else{
                                path = Paths.get(_baseDir + "/other/" + fileName);
                            }

                            Files.write(path, fileBytes);
                            response = "+Saved " + path;

                            break;

                        case "APP":

                            if(fileName.contains(".txt")){
                                File fileToAppend = new File(_baseDir + "/text/" + fileName);
                                OutputStream os = new FileOutputStream(fileToAppend, true);
                                os.write(fileBytes, 0, fileBytes.length);
                                os.close();
                                response = "+Saved " + fileToAppend.getPath();

                            }else{
                                response = " -Couldn't save because file is not of text type";
                            }

                            break;

                }

            }

            _currSTOR = "IDLE";

        } catch (Exception e) {
            response = " -Couldn't save because " + e;
            e.printStackTrace();
        }


        return response;
    }

    /**
     * @detail Returns File to be sent from the RETR sequence
     * @return
     */
    public File getFileToSend() {
        File file = _fileToSend;
        _fileToSend = null;
        return file;
    }
}
