package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the priorityNames and repository Ids of each item priority, instead of fetching
 * the priority from the repository, this enum is used where the values are known beforehand
 */
public enum ItemPriority {
    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    HIGH(4, "Must Have"),
    MEDIUM(3, "Should Have"),
    LOW(2, "Could Have"),
    LOWEST(1, "Won't Have");


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

    /**
     * this method takes as input a repository id and returns the item priority that the id belongs to
     *
     * @param id: the repository id of the item priority the user requested
     * @return : returns the item priority with that certain id
     */
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