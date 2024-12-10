import java.sql.SQLException;

public class Main  {
    public static void main(String[] args) throws SQLException {
        System.out.println("Hello world!");
        String dbUrl = "jdbc:postgresql://"+System.getenv("PGHOST")+":"+System.getenv("PGPORT")+"/"+System.getenv("PGDATABASE");
        System.out.println(dbUrl);
        DatabaseHandler dbh = new DatabaseHandler();
    }
}