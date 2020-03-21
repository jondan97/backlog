package gr.university.thesis.entity.enumeration;


/**
 * this enum class helps in identifying the taskBoard names and repository Ids of item status in the task board,
 * instead of fetching the status from the repository, this enum is used where the values are known beforehand
 */
public enum TaskBoardStatus {
    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    TO_DO(1, "To Do"),
    IN_PROGRESS(2, "In Progress"),
    FOR_REVIEW(3, "For Review"),
    DONE(4, "Done");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         the status of an item in the task board
     */
    TaskBoardStatus(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    /**
     * this method takes as input a repository id and returns the task board status that this id belongs to
     *
     * @param id: the repository id of the task board status the user requested
     * @return : returns the task board status with that certain id
     */
    public static TaskBoardStatus findTaskBoardStatusByRepositoryId(int id) {
        for (TaskBoardStatus taskBoardStatus : TaskBoardStatus.values()) {
            if (taskBoardStatus.repositoryId == id)
                return taskBoardStatus;
        }
        return TaskBoardStatus.NONE;
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
