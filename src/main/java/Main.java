import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main  {
    public static void main(String[] args) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        System.out.println("Connecting to " + dbUrl);
        try {
            // Registers the driver
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
        }
        Connection conn= DriverManager.getConnection(dbUrl, "postgres", "guardspine");

        try {
            Statement s=conn.createStatement();
            String sqlStr = "INSERT INTO patients (familyname,givenname,phonenumber) values('Swire22', 'Catherine', '07755678899')";
            s.execute(sqlStr);

            s.close();
            conn.close();
        }
        catch (Exception e){
        }

    }
}