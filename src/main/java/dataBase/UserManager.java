package dataBase;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest; //для мд2
import java.security.NoSuchAlgorithmException;


//регистрация пользователей, авторизация
public class UserManager {

    //метод для регистрации нового пользователя
    public static boolean register(String username, String password) {

        if (userExists(username)) {
            return false; // логин занят
        }

        //хешируем пароль с помощью алгоритма MD2
        String hash = hashPasswordMD2(password);
        //SQL-запрос на добавление нового пользователя. ??-заглущки, значения передаем позже
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        //создаем подключение и формируем запрос
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username); //?? для заглушек
            stmt.setString(2, hash);
            stmt.executeUpdate(); //отправка
            return true;

        } catch (SQLException e) {
            System.out.println("Регистрация не удалась: " + e.getMessage());
            return false;
        }
    }

    //хэширование пароля
    private static String hashPasswordMD2(String password) {
        try {
            //объект для хэщирования
            MessageDigest md = MessageDigest.getInstance("MD2");
            //вовращает массив байт
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));

            //объект для построения строки, переводит массив байт в шестнадцатеричное (hex) представление
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                //Переводим в двузначный шестнадцатеричный виж для хранения в бд
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD2 не поддерживается", e);
        }
    }

    //авторизация
    public static boolean login(String username, String password) {
        //хэщируем пароль
        String hash = hashPasswordMD2(password);
        //запрос для поиска гле и логин, и хеш совпадают
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);

            return stmt.executeQuery().next(); // true, если пользователь найден

        } catch (SQLException e) {
            System.out.println("Ошибка авторизации: " + e.getMessage());
            return false;
        }
    }

    //получаем айди
    public static int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;

        } catch (SQLException e) {
            System.out.println("Ошибка при получении user_id: " + e.getMessage());
            return -1;
        }
    }


    /**
     * Проверяет, существует ли пользователь с заданным логином.
     */
    private static boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Ошибка при проверке существования пользователя: " + e.getMessage());
            return false;
        }
    }

}
