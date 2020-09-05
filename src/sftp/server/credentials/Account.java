package sftp.server.credentials;

public class Account{

    private String _accountName;
    private String _password;
    private boolean _isLoggedIn;
    private boolean _isRoot;


    /**
     * @detail Constructor for accounts that require a password
     * @param accountName
     * @param password
     */
    Account( String accountName, String password) {
        this._password = password;
        this._accountName = accountName;
        this._isRoot = false;
    }

    /**
     * @detail Constructor for accounts that are have admin priviledges
     * @param rootName
     */
    Account( String rootName) {
        this._accountName = rootName;
        this._isRoot = true;
    }

    /**
     * @detail Returns password for selected account
     * @return
     */
    String getPassword() {
        if(!this._isRoot) {
            return _password;
        }else{
            return null;
        }
    }

    /**
     * @detail Returns accountName for selected account
     * @return
     */
    String getAccountName() {
        return _accountName;
    }

    /**
     * @detail Determines if a account has admin rights. Does not need password
     * @return
     */
    boolean isRoot() {
        return _isRoot;
    }

    /**
     * @detail Sets status to logged In if password is correct and logged out
     *          if accounts or user is switched
     * @param isLoggedIn
     */
    void setIsLoggedIn(boolean isLoggedIn) {
        this._isLoggedIn = isLoggedIn;
    }

    /**
     * @detail return LoggedIn status for account
     * @return
     */
    boolean isLoggedIn(){
        return _isLoggedIn;
    }
}
