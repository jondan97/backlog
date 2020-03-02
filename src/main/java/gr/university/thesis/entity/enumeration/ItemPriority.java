package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the priorityNames and repository Ids of each item priority, instead of fetching
 * the priority from the repository, this enum is used where the values are known beforehand
 */
public enum ItemPriority {
    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    LOWEST(1, "Lowest"),
    LOW(2, "Low"),
    MEDIUM(3, "Medium"),
    HIGH(4, "High"),
    HIGHEST(5, "Highest");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         the priority of an item
     */
    ItemPriority(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    public static ItemPriority findItemTypeByRepositoryId(int id) {
        for (ItemPriority itemPriority : ItemPriority.values()) {
            if (itemPriority.repositoryId == id)
                return itemPriority;
        }
        return ItemPriority.NONE;
    }

    /**
     * @return returns the repository id of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }

    /**
     * @return returns the priority name of the wanted enum
     */
    public String getName() {
        return name;
    }

}