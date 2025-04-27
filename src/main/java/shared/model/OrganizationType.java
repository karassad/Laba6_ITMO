package shared.model;

import java.io.Serializable;

/**
 * Enum representing types of organizations.
 */
public enum OrganizationType implements Serializable {
    COMMERCIAL,
    PUBLIC,
    GOVERNMENT,
    TRUST,
    PRIVATE_LIMITED_COMPANY;
}
