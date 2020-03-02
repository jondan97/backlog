package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the roleNames and repository Ids of each role, instead of fetching
 * the role from the repository, this enum is used where the values are known beforehand
 */
public enum RoleEnum {
    MASTER_ADMIN(1, "MASTER_ADMIN"),
    ADMIN(2, "ADMIN"),
    PROJECT_MANAGER(3, "PROJECT_MANAGER"),
    USER(4, "USER");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         string roleName stored in the repository
     */
    RoleEnum(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    /**
     * @return returns the role name of the wanted enum
     */
    public String getName() {
        return name;
    }

    /**
     * @return returns the repository id of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }
}
