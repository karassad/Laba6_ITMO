package shared.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing an organization entity.
 */
public class Organization implements Serializable, Comparable<Organization> {

    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    private final int id;
    private final ZonedDateTime creationDate;

    private String name;
    private Coordinates coordinates;
    private Float annualTurnover;
    private  OrganizationType type;
    private Address officialAddress;

    private int userId; //для клиента

    public Organization(int id, ZonedDateTime creationDate, String name, Coordinates coordinates, Float annualTurnover, OrganizationType type, Address officialAddress) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя организации не может быть пустым или null.");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть null.");
        }
        if (annualTurnover == null || annualTurnover <= 0) {
            throw new IllegalArgumentException("Годовой оборот должен быть положительным числом.");
        }
        if (officialAddress == null) {
            throw new IllegalArgumentException("Официальный адрес не может быть null.");
        }
        this.id = id;
        this.creationDate = creationDate;
        this.name = name;
        this.coordinates = coordinates;
        this.annualTurnover = annualTurnover;
        this.type = type;
        this.officialAddress = officialAddress;
    }


    public Organization(String name, Coordinates coordinates, float turnover, OrganizationType type, Address address, int id, ZonedDateTime creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public Organization(String name,
                        Coordinates coordinates,
                        Float annualTurnover,
                        OrganizationType type,
                        Address officialAddress) {
        this.id = idGenerator.getAndIncrement();
        this.creationDate = ZonedDateTime.now();
        this.name = name;
        this.coordinates = coordinates;
        this.annualTurnover = annualTurnover;
        this.type = type;
        this.officialAddress = officialAddress;
    }

    @Override
    public int compareTo(Organization other) {
        return this.name.compareTo(other.name);
    }

    public int getId() {
        return id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Float getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(Float annualTurnover) {
        this.annualTurnover = annualTurnover;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public Address getOfficialAddress() {
        return officialAddress;
    }

    public void setOfficialAddress(Address officialAddress) {
        this.officialAddress = officialAddress;
    }

    @Override
    public String toString() {
        return String.format(
                "Организация #%d (%s)\nКоординаты: %s\nОборот: %.2f\nТип: %s\nАдрес: %s\nСоздано: %s\nID пользователя: %s",
                id, name, coordinates, annualTurnover, type != null ? type : "(нет типа)", officialAddress, creationDate, userId
        );
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setId(int userIdid) {
        this.userId = id;
    }

}
