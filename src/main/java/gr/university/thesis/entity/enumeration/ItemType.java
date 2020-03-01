package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the typeNames and repository Ids of each item type, instead of fetching
 * the type from the repository, this enum is used where the values are known beforehand
 */
public enum ItemType {
    EPIC(1, "Epic"),
    STORY(2, "Story"),
    TASK(3, "Task"),
    BUG(4, "Bug");

    private final String typeName;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param typeName:     the type of an item
     */
    ItemType(int repositoryId, String typeName) {
        this.typeName = typeName;
        this.repositoryId = repositoryId;
    }

    /**
     * @return returns the type name of the wanted enum
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return returns the repository of the wanted enum
     */
    public int getRepositoryId() {
        return repositoryId;
    }


}
