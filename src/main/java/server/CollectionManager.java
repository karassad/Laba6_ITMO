package server;

import dataBase.DatabaseManager;
import shared.model.Organization;
import shared.model.Coordinates;
import shared.model.OrganizationType;
import shared.model.Address;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class managing the collection of organizations, хранит и загружает её в CSV.
 */
public class CollectionManager {

    private LinkedHashSet<Organization> collection = new LinkedHashSet<>();
    private final String filename;
    private final ZonedDateTime initDate = ZonedDateTime.now();

    //Для синхронизации доступа к коллекции
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    //позволяет нескольким потокам читать, но записывать только 1
    //делаю, чтобы во время работы неск клиентов изменять объекты мог только 1, остальные только читать


    public CollectionManager(String filename) {
        this.filename = filename;
    }

    /**
     * Возвращает время инициализации менеджера.
     */
    public ZonedDateTime getInitDate() {
        return initDate;
    }

    /**
     * Загружает коллекцию из CSV-файла или создаёт пустую, если файл не найден.
     * Формат CSV: id,name,x,y,turnover,type,zip,creationDate
     */
    public void load() {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("CSV-файл не найден, стартуем с пустой коллекции.");
            return;
        }

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                BufferedReader reader = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8))
        ) {
            String header = reader.readLine(); // пропускаем заголовок
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Разбираем строку
                String[] parts = line.split(",", -1);
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                float x = Float.parseFloat(parts[2]);
                int   y = Integer.parseInt(parts[3]);
                float turnover = Float.parseFloat(parts[4]);
                OrganizationType type = parts[5].isEmpty() ? null : OrganizationType.valueOf(parts[5]);
                Address addr = parts[6].isEmpty() ? null : new Address(parts[6]);
                ZonedDateTime cd = ZonedDateTime.parse(parts[7]);

                Organization org = new Organization(
                        id, cd, name,
                        new Coordinates(x, y),
                        turnover, type, addr
                );
                collection.add(org);
            }
            System.out.println("Загружено " + collection.size() + " элементов из CSV.");
        } catch (IOException | RuntimeException e) {
            System.err.println("Ошибка чтения CSV: " + e.getMessage());
            collection.clear();
        }
    }

    /**
     * Сохраняет текущую коллекцию в CSV-файл (перезапись).
     */
    public void save() {
        File file = new File(filename);
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))
        ) {
            // Заголовок
            writer.write("id,name,x,y,turnover,type,zip,creationDate");
            writer.newLine();

            // Каждая организация в строку
            for (Organization org : collection) {
                StringBuilder sb = new StringBuilder();
                sb.append(org.getId()).append(',')
                        .append(org.getName()).append(',')
                        .append(org.getCoordinates().getX()).append(',')
                        .append(org.getCoordinates().getY()).append(',')
                        .append(org.getAnnualTurnover()).append(',')
                        .append(org.getType() != null ? org.getType() : "").append(',')
                        .append(org.getOfficialAddress() != null ? org.getOfficialAddress().getZipCode() : "").append(',')
                        .append(org.getCreationDate());
                writer.write(sb.toString());
                writer.newLine();
            }
            System.out.println("Коллекция сохранена в CSV: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка записи CSV: " + e.getMessage());
        }
    }


    public LinkedHashSet<Organization> getCollection() {
        lock.readLock().lock();
        try {
            return new LinkedHashSet<>(collection); // копия для безопасности
        } finally {
            lock.readLock().unlock();
        }
    }


    public void add(Organization organization) {
        lock.writeLock().lock();
        try {
            collection.add(organization);
        } finally {
            lock.writeLock().unlock();
        }
    }


    public void clear() {
        lock.writeLock().lock();
        try {
            collection.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean removeById(int id) {
        lock.writeLock().lock();
        try {
            return collection.removeIf(o -> o.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean updateById(int id, Organization newOrg) {
        return false;
    }

    //добавление органиазции в бд. тру если все ок
    public boolean addToDatabase(Organization org) {
        String sql = "INSERT INTO organizations " +
                "(name, coordinates_x, coordinates_y, creation_date, annual_turnover, type, zip_code, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        //returning id возвращает id, который бд присвоила новой записи

        //открываем поделючение, готовим запрос
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, org.getName());
            stmt.setFloat(2, org.getCoordinates().getX());
            stmt.setInt(3, org.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.from(org.getCreationDate().toInstant()));
            stmt.setFloat(5, org.getAnnualTurnover());
            stmt.setString(6, org.getType() != null ? org.getType().toString() : null);
            stmt.setString(7, org.getOfficialAddress() != null ? org.getOfficialAddress().getZipCode() : null);
            stmt.setInt(8, org.getUserId());

            //выполнение нашего запроса. Получаем таблицу результата. В ней только id
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { //перемещает указатель на 1 строку, по умолчанию стоит до нее и возвращает
                //тру фолз если эта строка есть/нет
                int generatedId = rs.getInt(1); //resultset нумерется с 1 не с 0
                org.setId(generatedId);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении в БД: " + e.getMessage());
        }

        return false;
    }

    public boolean updateInDatabase(int id, Organization newOrg, int userId) {
        String sql = "UPDATE organizations SET name = ?, coordinates_x = ?, coordinates_y = ?, annual_turnover = ?, type = ?, zip_code = ? " +
                "WHERE id = ? AND user_id = ?"; //проверка на пользователя

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newOrg.getName());
            stmt.setFloat(2, newOrg.getCoordinates().getX());
            stmt.setInt(3, newOrg.getCoordinates().getY());
            stmt.setFloat(4, newOrg.getAnnualTurnover());
            stmt.setString(5, newOrg.getType() != null ? newOrg.getType().toString() : null);
            stmt.setString(6, newOrg.getOfficialAddress().getZipCode());
            stmt.setInt(7, id); //7 и 8 сравниваем
            stmt.setInt(8, userId);

            int affectedRows = stmt.executeUpdate(); //возвращает количество строк, которые были изменены
            if (affectedRows > 0) {
                //обновляем в памяти
                updateInMemory(id, newOrg);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении в БД: " + e.getMessage());
        }
        return false;
    }

    //Обновляем в коллекции
    public void updateInMemory(int id, Organization newOrg) {
        lock.writeLock().lock();
        try {
            collection.removeIf(org -> org.getId() == id);
            collection.add(newOrg);
        } finally {
            lock.writeLock().unlock();
        }
    }


    public boolean removeByIdFromDatabase(int id, int userId) {
        String sql = "DELETE FROM organizations WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                lock.writeLock().lock();
                try {
                    collection.removeIf(org -> org.getId() == id); //удаляем из памяти
                } finally {
                    lock.writeLock().unlock();
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при удалении из БД: " + e.getMessage());
        }

        return false;
    }


    public boolean clearByUser(int userId) {
        String sql = "DELETE FROM organizations WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate(); //сколько строк удалено

            lock.writeLock().lock();
            try {
                collection.removeIf(org -> org.getUserId() == userId);
            } finally {
                lock.writeLock().unlock();
            }

            return affected > 0;

        } catch (SQLException e) {
            System.out.println("Ошибка при очистке БД: " + e.getMessage());
            return false;
        }
    }


    //выгружаем бд на локалку
    public void loadFromDatabase() {
        lock.writeLock().lock();
        try{
            collection.clear(); //очищаем старую коллекцию

            String sql = "SELECT * FROM organizations";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    float x = rs.getFloat("coordinates_x");
                    int y = rs.getInt("coordinates_y");
                    Timestamp ts = rs.getTimestamp("creation_date");
                    ZonedDateTime creationDate = ts.toInstant().atZone(ZonedDateTime.now().getZone());
                    float turnover = rs.getFloat("annual_turnover");
                    String typeStr = rs.getString("type");
                    OrganizationType type = typeStr != null ? OrganizationType.valueOf(typeStr) : null;
                    String zip = rs.getString("zip_code");
                    int userId = rs.getInt("user_id");

                    Organization org = new Organization(id, creationDate, name,
                            new Coordinates(x, y), turnover, type,
                            zip != null ? new Address(zip) : null);
                    org.setUserId(userId);
                    collection.add(org);
                }
            } finally {
                lock.writeLock().unlock();
            }

        } catch (SQLException e) {
            System.out.println("Ошибка при загрузке из БД: " + e.getMessage());
        }
    }

    public int removeGreater(Organization ref, int userId) {
        lock.writeLock().lock();
        try {
            int before = collection.size();

            // Удаляем из памяти
            collection.removeIf(o ->
                    o.getUserId() == userId && o.compareTo(ref) > 0
            );

            // Удаляем из базы
            String sql = "DELETE FROM organizations WHERE user_id = ? AND name > ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setString(2, ref.getName()); // основано на compareTo()
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Ошибка при удалении из БД: " + e.getMessage());
            }

            return before - collection.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int removeLower(Organization ref, int userId) {
        lock.writeLock().lock();
        try {
            int before = collection.size();

            // Удаление из памяти
            collection.removeIf(o ->
                    o.getUserId() == userId && o.compareTo(ref) < 0
            );

            // Удаление из БД
            String sql = "DELETE FROM organizations WHERE user_id = ? AND name < ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.setString(2, ref.getName());
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Ошибка при удалении из БД: " + e.getMessage());
            }

            return before - collection.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

}
