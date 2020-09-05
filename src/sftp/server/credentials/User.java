package sftp.server.credentials;

import java.util.ArrayList;

public class User {

    private ArrayList<Account> _accounts;
    private String _userName;
    private boolean _isAdmin;

    /**
     * @detail Constructor for User object
     * @param userName
     * @param isAdmin password is needed if false
     */
    User(String userName, boolean isAdmin) {
        this._accounts = new ArrayList<Account>();
        this._userName = userName;
        this._isAdmin = isAdmin;
    }

    /**
     * @detail Returns current UserName
     * @return
     */
    String getUserName() {
        return _userName;
    }

    /**
     * @detail Assigns associated accounts for the user
     * @param accounts
     */
    void setAccounts(ArrayList<Account> accounts) {
        this._accounts = accounts;
    }

    /**
     * @detail Returns accounts for User object
     * @return
     */
    ArrayList<Account> getAccountList(){
        return this._accounts;
    }

    /**
     * @detail Returns User's admin status
     * @return
     */
    boolean isAdmin() {
        return _isAdmin;
    }
}
