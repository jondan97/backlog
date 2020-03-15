package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the status and repository Ids of each item, instead of fetching
 * the status from the repository, this enum is used where the values are known beforehand
 */
public enum ItemStatus {

    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    BACKLOG(1, "In Backlog"),
    READY(2, "In Ready Sprint"),
    ACTIVE(3, "In Active Sprint"),
    FINISHED(4, "Finished");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         the status of an item
     */
    ItemStatus(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    /**
     * this method takes as input a repository id and returns the item status that the id belongs to
     *
     * @param id: the repository id of the status the user requested
     * @return: returns the item status with that certain id
     */
    public static ItemStatus findItemTypeByRepositoryId(int id) {
        for (ItemStatus itemStatus : ItemStatus.values()) {
            if (itemStatus.repositoryId == id)
                return itemStatus;
        }
        return ItemStatus.NONE;
    }

    /**
     * @return returns the repository of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }

    /**
     * @return returns the status name of the wanted enum
     */
    public String getName() {
        return name;
    }
}
