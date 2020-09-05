package sftp.server.credentials;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * @detail Manages Users, Accounts and authentication process
 */
public class CredentialsManager {

    private User _currentUser;
    private Account _currentAccount;
    private ArrayList<User> _users;
    private boolean _isBypass;

    /**
     * @detail Accepts a Username after USER <username> is established
     * @param userName
     * @return response if user-id was valid and selected
     */
    public String setCurrentUser(String userName) {
        String response = "-Invalid user-id, try again";

        if(_isBypass){
            response = "+ Bypass Login";
        }else if(!_users.isEmpty()){
            for(User user : _users){

                if(user.getUserName().equals(userName)){
                    this._currentUser = user;

                    if(user.isAdmin()){
                        this._currentAccount = null;
                        response = "!" + userName + " logged in";
                    }else{

                        if(this._currentAccount != null){
                            this._currentAccount.setIsLoggedIn(false);
                            this._currentAccount = null;
                        }

                        response = "+" + userName +  " valid, send account and password";
                    }

                    break;
                }

            }
        }

        return response;
    }

    /**
     * @detail Setter for Bypassing the Login process.
     *         Its a developer setting for skipping the login process
     *         to test other commands
     * @param isBypass
     */
    public void setIsBypass(boolean isBypass){
        _isBypass = isBypass;
    }

    /**
     * @detail Accepts a Account after ACCT <account> is established
     * @param accountName
     * @return response if account is valid and was set
     */
    public String setCurrentAccount(String accountName) {
        String response = "-Invalid account, try again";

        if(_isBypass) {

            response = "+ Bypass Login";

        }else if(!(_currentUser.getUserName().equals("admin"))) {

            for (Account acct: _currentUser.getAccountList()){

                String acctName = acct.getAccountName();

                if(acctName.equals(accountName)){

                    if(acct.isRoot()){
                        this._currentAccount = acct;
                        this._currentAccount.setIsLoggedIn(true);
                        response = "! Account valid, logged-in";
                    }else{
                        this._currentAccount = acct;
                        this._currentAccount.setIsLoggedIn(false);
                        response = "+Account valid, send password";
                    }

                    break;
                }
            }
        }

        return response;
    }

    /**
     * @detail Logs in an Account with a correct password
     * @param password
     * @return response if login was successful
     */
    public String attemptLogin(String password){
        String response = "-Wrong password, try again";

        if(_isBypass) {

            response = "+ Bypass Login";

        }else if(_currentAccount == null || _currentAccount.isLoggedIn()) {

            response = "+Send account";

        }else if(_currentAccount.getPassword().equals(password)) {
            _currentAccount.setIsLoggedIn(true);
            response = "! Logged in";
        }

        return response;
    }

    /**
     * @detail Reads a users.csv and generates users and accounts
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void loadUsers(){

        _users = new ArrayList<User>();
        String root = System.getProperty("user.dir");
        String pathToCsv = root+"/src/sftp/server/credentials/users.csv";
        BufferedReader csvReader = null;
        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));

            String row;

            while ((row = csvReader.readLine()) != null) {
                User user;
                String[] data = row.split(",");

                if(data[0].equals("admin")){

                    user = new User(data[0], true);

                }else{
                    user = new User(data[0], false);

                    ArrayList<Account> accountList= new ArrayList<Account>();

                    for(int i = 1; i < data.length; i++){
                        String[] credentials = data[i].split("\\.");
                        Account account;

                        if(credentials[0].equals("root")){
                            account = new Account(credentials[0]);
                        }else{
                            account = new Account(credentials[0], credentials[1]);
                        }

                        accountList.add(account);
                    }

                    user.setAccounts(accountList);
                }

                _users.add(user);

            }

            csvReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @detail Returns if a valid User was accepted
     * @return
     */
    public boolean isCurrentUserSelected(){

        if(_isBypass) {
            return true;
        }else if(_currentUser == null){
            return false;
        }else{
            return true;
        }
    }

    /**
     * @detail Returns if a valid User with or without an account was logged in
     * @return
     */
    public boolean isAUserLoggedIn() {
        boolean isLoggedIn;

        if(_isBypass) {
            isLoggedIn = true;
        }else if(_currentUser.isAdmin()){
            isLoggedIn = true;
        }else if(_currentAccount != null){
            isLoggedIn =  _currentAccount.isLoggedIn();
        }else{
            isLoggedIn = false;
        }

        return isLoggedIn;

    }

}
