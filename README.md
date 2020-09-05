# OS and Tools
OS: Developed and Tested on Windows 10 64 bit  
IDE: IntelliJ IDE with JDK 11.08

# General Information
Details of SFTP (Simple File Transfer Protocol) and valid commands can be found in   
<https://tools.ietf.org/html/rfc913>

## Excerpt from the document
SFTP is a simple file transfer protocol.  It fills the need of people
wanting a protocol that is more useful than TFTP but easier to
implement (and less powerful) than FTP.  SFTP supports user access
control, file transfers, directory listing, directory changing, file
renaming and deleting.
                   
```
  <command> : = <cmd> [<SPACE> <args>] <NULL>
  <cmd> : =  USER ! ACCT ! PASS ! TYPE ! LIST ! CDIR
             KILL ! NAME ! DONE ! RETR ! STOR
  <response> : = <response-code> [<message>] <NULL>
  <response-code> : =  + | - |   | !
```

SFTP is used by opening a TCP connection to the remote hosts' SFTP
port (6789 decimal).  You then send SFTP commands and wait for
replies.  SFTP commands sent to the remote server are always 4 ASCII
letters (of any case) followed by a space, the argument(s), and a
`<NULL>`.  The argument can sometimes be null in which case the command
is just 4 characters followed by `<NULL>`.  Replies from the server are
always a response character followed immediately by an ASCII message
string terminated by a `<NULL>`.  A reply can also be just a response
character and a `<NULL>`.

# Port and IP Address
Port number is 6789 and localhost as destination IP address for client

# Folder Info

resources: Contains sftp.server which hosts the server files
	       and sftp.client which hosts the client's files
	       
src: Contains source code

# Compile Instructions

`cd src/`

For Windows CMD:  
`javac sftp\*.java sftp\server\*.java sftp\server\credentials\*.java sftp\client\*.java`

For Ubuntu 18.04 bash:  
`javac sftp/*.java sftp/server/*.java sftp/server/credentials/*.java sftp/client/*.java`


# Run Instructions

**Note:-** Run Server and Client on different consoles



For Windows CMD:  

```
cd ..\
java -cp src\ sftp.server.Server
java -cp src\ sftp.client.Client
```

For Ubuntu 18.04 bash

```
cd ../
java -cp src/ sftp.server.Server
java -cp src/ sftp.client.Client
```

# Test Cases for Commands
First command must be ```USER <username>``` 
Example:  
```
Input command: PASS C
Server response: -No User-id selected
```

Login is required for most commands after a user-id is specified
```
Input command: CDIR text
Server response: - No Login found
```
Invalid Command Response

```
Input command: WRONG
Server response: -Invalid command
```

### USER
The available users are **uoa, aut and admin**  
Credentials information can be found in src/sftp/server/credentials/users.csv  

```
Input command: USER admin 
Server response: !admin logged in
```

```
Input command: USER uoa
Server response: +uoa valid, send account and password
```

```
Input command: USER wrong 
Server response: -Invalid user-id, try again
```

### ACCT
Each User-id has a root account that does not need a password to login
```
Input command: ACCT root
Server response: ! Account valid, logged-in
```

```
Input command: ACCT utri092
Server response: +Account valid, send password
```

```
Input command: ACCT wrong
Server response: -Invalid account, try again
```


### PASS

The ACCT is utri092
```
Input command: PASS wrong
Server response: -Wrong password, try again
```

```
Input command: PASS study
Server response: ! Logged in
```

No account is selected. Switch User to aut for this case
```
Input command: USER aut
Server response: +aut valid, send account and password
Input command: PASS water
Server response: +Send account
```


### TYPE
```
Input command: TYPE A
Server response: +Using Ascii mode
```

```
Input command: TYPE B
Server response: +Using Binary mode
```

```
Input command: TYPE C
Server response: +Using Continuous mode
```

```
Input command: TYPE D
Server response: -Type not valid
```

### LIST

Default is current directory if none specified

##### F
```
Input command: LIST F
Server response: 
+Contents
    other/
    text/
```


```
Input command: LIST F text/
Server response: 
+Contents
    example.txt
    s.txt

```

##### V
```
Input command: LIST V
Server response: 
+Contents
    other/  created time: 10:29:03 29/08/2020 last accessed time: 10:29:03 29/08/2020 last modified time: 10:29:03 29/08/2020
    text/  created time: 11:38:29 26/08/2020 last accessed time: 16:43:18 01/09/2020 last modified time: 16:43:17 01/09/2020
```

```
Input command: LIST V text/
Server response: 
+Contents
    example.txt   created time: 16:42:43 01/09/2020 last accessed time: 16:43:17 01/09/2020 last modified time: 16:43:17 01/09/2020
    s.txt   created time: 21:59:07 31/08/2020 last accessed time: 16:42:50 01/09/2020 last modified time: 16:42:50 01/09/2020

```
### CDIR
```
Input command: CDIR text
Server response: !Changed working dir to D:\IdeaProjects\CS725\resources\sftp.server\text
```

```
Input command: CDIR wrong
Server response: -Can't connect to directory because: (It does not exist)
```

### KILL
Files are in text/ directory

```
Input command: KILL c.txt
Server response: +c.txt deleted
```

```
Input command: KILL wrong
Server response: -Not deleted because file does not exist
```

### NAME
Files are in text/ directory

```
Input command: NAME example2.txt
Server response: +File exists
Input command: TOBE exampleNew.txt
Server response: +D:\IdeaProjects\CS725\resources\sftp.server\text\example2.txt renamed to D:\IdeaProjects\CS725\resources\sftp.server\text\exampleNew.txt
```

```
Input command: NAME wrong.txt
Server response: -Can't find wrong.txt
  NAME command is aborted, don't send TOBE

Input command: TOBE exampleNew.txt
Server response: -File wasn't renamed because file is not specified
```

### DONE

User does not need to be logged in  
Closes connection 
```
Input command: DONE
Server response: + Thanks for using DESKTOP-QATPI3Q/192.168.56.1 SFTP Service. Goodbye!
```

### RETR

Files are in text/ directory
```
Input command: RETR s.txt
Server response: 11
Input command: STOP
Server response: +ok, RETR aborted
```

```
Input command: RETR s.txt
Server response: 11
Input command: SEND

Timeout of 10 seconds set

Server response: +File Saved on Client's side
```

```
Input command: RETR wrong.txt
Server response: -File doesn't exist
```

### STOR

##### OLD

```
Input command: STOR OLD c.txt
Server response: +Will create new file
Input command: SIZE
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\c.txt
```

```
Input command: STOR OLD c.txt
Server response: +Will write over old file
Input command: SIZE
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\c.txt
```

```
Input command: STOR OLD h.mp3
Server response: +Will create new file
Input command: SIZE
Server response: -Not enough room, don't send it
```

```
Input command: STOR OLD wrong.txt
Error: File does not exist on Client System. Try again
```
##### APP
```
Input command: STOR APP example.txt
Server response: +Will append to file
Input command: SIZE
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\example.txt
```

```
Input command: KILL c.txt
Server response: +c.txt deleted
Input command: STOR APP c.txt
Server response: +Will create file
Input command: SIZE
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\c.txt
```

```
Input command: STOR APP h.mp3
Server response: +Will create file
Input command: SIZE
Server response: -Not enough room, don't send it
```

```
Input command: STOR APP wrong.txt
Error: File does not exist on Client System. Try again
```
##### NEW


```
Input command: STOR NEW c.txt
Server response: +File does not exist, will create new file
Input command: SIZE
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\c.txt
```

```
Input command: STOR NEW c.txt
Server response: +File exists, will create new generation of file
Input command: SIZE      
Server response: +ok, waiting for file
Server response: +Saved D:\IdeaProjects\CS725\resources\sftp.server\text\new_0_c.txt
```

```
Input command: STOR NEW h.mp3
Server response: +File does not exist, will create new file
Input command: SIZE
Server response: -Not enough room, don't send it
```

```
Input command: STOR NEW wrong.txt
Error: File does not exist on Client System. Try again
```
