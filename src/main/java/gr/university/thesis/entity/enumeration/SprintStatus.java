package gr.university.thesis.entity.enumeration;

/**
 * this enum class helps in identifying the status and repository Ids of each sprint, instead of fetching
 * the status from the repository, this enum is used where the values are known beforehand
 */
public enum SprintStatus {

    //'NONE' just in case something goes wrong, and we don't want the system to crash
    NONE(0, "N/A"),
    READY(1, "Ready"),
    ACTIVE(2, "Active"),
    FINISHED(3, "Finished");

    private final String name;
    private final int repositoryId; //database id, known beforehand

    /**
     * @param repositoryId: id stored in the repository
     * @param name:         the status of a sprint
     */
    SprintStatus(int repositoryId, String name) {
        this.name = name;
        this.repositoryId = repositoryId;
    }

    /**
     * this method takes as input a repository id and returns the sprint status that the id belongs to
     *
     * @param id: the repository id of the status the user requested
     * @return : returns the sprint status with that certain id
     */
    public static SprintStatus findSprintStatusByRepositoryId(int id) {
        for (SprintStatus sprintStatus : SprintStatus.values()) {
            if (sprintStatus.repositoryId == id)
                return sprintStatus;
        }
        return SprintStatus.NONE;
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
