package monsterserver.server;

import monsterserver.exceptions.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection()
    {
        try {
            String url = "jdbc:postgresql://localhost:5432/MonsterGame";
            String user = "postgres";
            String password = "postgres";

            Connection conn = DriverManager.getConnection(url,user, password);
            return conn;

        } catch (SQLException e) {
            throw new DataAccessException("Datenbankverbindungsaufbau nicht erfolgreich", e);
        }
    }
}
