package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the roleNames and repository Ids of each role, instead of fetching
 * the role from the repository, this enum is used where the values are known beforehand
 */
public enum RoleEnum {
    ADMIN(1, "ADMIN"),
    USER(2, "USER"),
    PROJECT_OWNER(3, "PROJECT_OWNER");

    private final String roleName;
    private final long repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param roleName:     string roleName stored in the repository
     */
    RoleEnum(long repositoryId, String roleName) {
        this.roleName = roleName;
        this.repositoryId = repositoryId;
    }

    public String getRoleName() {
        return roleName;
    }

    public long getRepositoryId() {
        return repositoryId;
    }
}
