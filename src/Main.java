//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import Models.Users;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {


        Sql_Connection _db = new Sql_Connection("localhost:3306", "practice", "root", "12345");

        for (int i = 0; i < 100; i++) {
            Users temp = new Users();
            temp.emailId = "tempUsers@gmail.com";
            temp.name = "temp User " + i;
            _db.createEntity(temp);
            System.out.println(temp.userId);
        }
        List<Users> users = _db.getParsedData(new Users());


        for (Users user : users) {
            System.out.printf("userId %d,name %s,emailId %s", user.userId, user.name, user.emailId);

        }


        _db.closeConnection();


    }


}