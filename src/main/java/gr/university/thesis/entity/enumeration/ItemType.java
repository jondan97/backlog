package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the typeNames and repository Ids of each item type, instead of fetching
 * the type from the repository, this enum is used where the values are known beforehand
 */
public enum ItemType {
    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    EPIC(1, "Epic"),
    STORY(2, "Story"),
    TASK(3, "Task"),
    BUG(4, "Bug");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         the type of an item
     */
    ItemType(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    /**
     * this method takes as input a repository id and returns the type that the id belongs to
     *
     * @param id: the repository id of the type the user requested
     * @return : returns the type with that certain id
     */
    public static ItemType findItemTypeByRepositoryId(int id) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.repositoryId == id)
                return itemType;
        }
        return ItemType.NONE;
    }

    /**
     * @return returns the repository of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }

    /**
     * @return returns the type name of the wanted enum
     */
    public String getName() {
        return name;
    }
}
