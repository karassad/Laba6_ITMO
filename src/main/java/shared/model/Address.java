package shared.model;

import java.io.Serializable;

/**
 * Class representing address information.
 */
public class Address implements Serializable {
    private String zipCode;

    public Address(String zipCode) {
        if (zipCode != null && zipCode.length() > 28){
            throw new IllegalArgumentException("Zip code не может быть длиннее 28 символов.");
        }
        this.zipCode = zipCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public String toString() {
        return zipCode != null ? zipCode : "(нет данных)";
    }
}
