package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the roleNames and repository Ids of each role, instead of fetching
 * the role from the repository, this enum is used where the values are known beforehand
 */
public enum RoleEnum {
    PROJECT_MANAGER(1, "PROJECT_MANAGER"),
    ADMIN(2, "ADMIN"),
    USER(3, "USER");

    private final String roleName;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param roleName:     string roleName stored in the repository
     */
    RoleEnum(int repositoryId, String roleName) {
        this.roleName = roleName;
        this.repositoryId = repositoryId;
    }

    /**
     * @return returns the role name of the wanted enum
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @return returns the repository id of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }
}
