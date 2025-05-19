package server;

import shared.model.Organization;
import shared.model.Coordinates;
import shared.model.OrganizationType;
import shared.model.Address;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

/**
 * Class managing the collection of organizations, хранит и загружает её в CSV.
 */
public class CollectionManager {

    private LinkedHashSet<Organization> collection = new LinkedHashSet<>();
    private final String filename;
    private final ZonedDateTime initDate = ZonedDateTime.now();

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
        return collection;
    }

    public void add(Organization organization) {
        collection.add(organization);
    }

    public void clear() {
        collection.clear();
    }

    public boolean removeById(int id) {
        return collection.removeIf(o -> o.getId() == id);
    }

    public boolean updateById(int id, Organization newOrg) {
        return false;
    }
}
