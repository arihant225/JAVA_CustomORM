package Models;

import Annotations.PrimaryKey;

public class Users {

    @PrimaryKey
    public int userId;
    public String name;
    public String emailId;
}
