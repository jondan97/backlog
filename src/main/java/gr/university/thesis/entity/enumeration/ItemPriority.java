package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the priorityNames and repository Ids of each item priority, instead of fetching
 * the priority from the repository, this enum is used where the values are known beforehand
 */
public enum ItemPriority {
    LOWEST(1, "Lowest"),
    LOW(2, "Low"),
    MEDIUM(3, "Medium"),
    HIGH(4, "High"),
    HIGHEST(5, "Highest");

    private final String priorityName;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param priorityName: the priority of an item
     */
    ItemPriority(int repositoryId, String priorityName) {
        this.priorityName = priorityName;
        this.repositoryId = repositoryId;
    }

    /**
     * @return returns the priority name of the wanted enum
     */
    public String getPriorityName() {
        return priorityName;
    }

    /**
     * @return returns the repository id of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }

}