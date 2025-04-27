package server;


import shared.model.Organization;

import java.io.*;
import java.util.LinkedHashSet;

/**
 * Class managing the collection of organizations.
 */
public class CollectionManager {

    private LinkedHashSet<Organization> collection = new LinkedHashSet<>();
    private final String filename;

    public CollectionManager(String filename) {
        this.filename = filename;
    }

    public void load(){
        try(FileReader reader = new FileReader(filename)){
            //Здесь будет загрузка из XML
            System.out.println("Файл успешно открыт для чтения (но парсинг ещё не реализован).");
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден. Коллекция будет пустой.");
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }

    public void save() throws FileNotFoundException {
        try(BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename))){
            //Здесь будет сериализация в XML
            System.out.println("Файл успешно открыт для записи (но запись ещё не реализована).");
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл: " + e.getMessage());
        }
    }

    public LinkedHashSet<Organization> getCollection() {
        return collection;
    }

    public void add(Organization organization){
        collection.add(organization);
    }

    public void clear() {
        collection.clear();
    }

    // Здесь потом будут другие методы управления коллекцией
}
