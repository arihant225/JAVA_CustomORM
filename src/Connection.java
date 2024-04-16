import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Sql_Connection {


    static String connectionUrl = null;
    static String userName = null;

    static {
        connectionUrl = "jdbc:mysql";
    }

    private String url = null;
    private String password = null;
    private String username = null;
    private Connection connection = null;

    Sql_Connection(String server, String database, String username, String password) {

        this.url = String.format("%s://%s/%s", connectionUrl, server, database);
        this.password = password;
        this.username = username;
        try {

            this.connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.printf("can't create sql connection \n reason-> %s", e.getMessage());

        }
    }

    ResultSet getView(String query) throws SQLException {
        if (this.connection == null) {
            return null;
        } else if (this.connection.isClosed()) {

            this.connection = DriverManager.getConnection(url, username, password);


        }


        Statement statement = this.connection.createStatement();


        return statement.executeQuery(query);


    }

    int ExecuteQuery(String query) throws Exception {
        if (this.connection == null) {
            return -1;
        } else if (this.connection.isClosed()) {

            this.connection = DriverManager.getConnection(url, username, password);


        }
        Statement statement = this.connection.createStatement();

        return statement.executeUpdate(query);
    }


    <T> Object createEntity(T obj) throws Exception {
        Field[] fields = obj.getClass().getFields();
        Field PK = null;
        List<String> columns = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();

            boolean isPrimary = false;
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getSimpleName().equals("PrimaryKey")) {
                    isPrimary = true;
                    break;
                }
            }
            if (!isPrimary) {
                if (field.get(obj) instanceof String)

                    values.add("'" + field.get(obj).toString() + "'");
                else {
                    values.add(field.get(obj).toString());
                }
                columns.add(field.getName());
            } else {
                PK = field;
            }

        }
        String[] columns_ = new String[columns.size()];
        String[] values_ = new String[columns.size()];

        String query = buildInsertionQuery(columns.toArray(columns_), values.toArray(values_), obj.getClass().getSimpleName());
        int noOfRows = this.ExecuteQuery(query);
        if (noOfRows > 0 && PK != null) {
            query = String.format("select max(%s) as 'PK' from %s", PK.getName(), obj.getClass().getSimpleName());

            ResultSet set = this.getView(query);
            if (set.next())
                PK.set(obj, set.getObject("PK"));


        }
        return obj;


    }

    <T> List<T> getParsedData(T Mock) throws Exception {

        Field[] fields = Mock.getClass().getFields();

        String[] columns = new String[fields.length];
        int pointer = 0;

        for (Field temp : fields) {
            columns[pointer++] = temp.getName();

        }

        String query = buildViewQuery(columns, Mock.getClass().getSimpleName());
        ResultSet sets = this.getView(query);

        List<T> collection = new ArrayList<T>();

        while (sets.next()) {


            @SuppressWarnings("unchecked")
            T obj = (T) Mock.getClass().getDeclaredConstructor().newInstance();


            for (Field temp : fields) {
                temp.setAccessible(true);
                temp.set(obj, sets.getObject(temp.getName()));

            }

            collection.add(obj);

        }

        return collection;


    }

    private String buildViewQuery(String[] columns, String table) {
        String ColumnsJoin = String.join(",", columns);
        return String.format("select %s from %s", ColumnsJoin, table);

    }

    private String buildInsertionQuery(String[] columns, String[] values, String table) {
        String Entity_values = String.join(",", values);
        String Entity_Attributes = String.join(",", columns);
        return String.format("insert into %s(%s) values(%s)", table, Entity_Attributes, Entity_values);
    }

    void closeConnection() throws SQLException {
        if (this.connection == null) return;
        if (!this.connection.isClosed())
            connection.close();
    }
}