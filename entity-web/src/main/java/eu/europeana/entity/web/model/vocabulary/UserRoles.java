package eu.europeana.entity.web.model.vocabulary;

import eu.europeana.api.commons.definitions.vocabulary.Role;

public enum UserRoles implements eu.europeana.api.commons.definitions.vocabulary.Role {

    /**
     * TODO: verify if ANYNYMOUS role is still needed 
     */
    ANONYMOUS(new String[] { Operations.RETRIEVE }), 
    USER(new String[] { Operations.RETRIEVE }),
    ADMIN(new String[] { Operations.RETRIEVE, Operations.ADMIN_ALL });

    String[] operations;

    UserRoles(String[] operations) {
        this.operations = operations;
    }

    public String[] getOperations() {
        return operations;
    }

    public void setOperations(String[] operations) {
        this.operations = operations;
    }

    /**
     * This method returns the api specific Role for the given role name
     * 
     * @param name the name of user role
     * @return the user role
     */
    public static Role getRoleByName(String name) {
        Role userRole = null;
        for (UserRoles role : UserRoles.values()) {
            if (role.name().toLowerCase().equals(name)) {
                userRole = role;
                break;
            }
        }
        return userRole;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String[] getPermissions() {
        return getOperations();
    }

}
