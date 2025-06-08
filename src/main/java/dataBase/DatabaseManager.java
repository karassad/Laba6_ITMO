package dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//логика подключения
public class DatabaseManager {
//    private static final String URL = "jdbc:postgresql://pg:5432/studs";
    private static final String URL = "jdbc:postgresql://pg:5432/studs";
    private static final String USER = "s465785";
    private static final String PASSWORD = "123";


//    //блок выполняется один раз при первом обращении
    static {
        try {
            Class.forName("org.postgresql.Driver"); //загружаем драйвер в драйвер менеджер
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver не найден", e);
        }
    }

    //метод для подключения к базе
    public static Connection getConnection() throws SQLException {
        //создает моннекшн по драйверу
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
