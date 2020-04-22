package gr.university.thesis.service;

import gr.university.thesis.dto.BurnDownChartData;
import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemStatus;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.entity.enumeration.SprintStatus;
import gr.university.thesis.exceptions.ProjectAlreadyExistsException;
import gr.university.thesis.exceptions.ProjectHasEmptyTitleException;
import gr.university.thesis.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * service that handles everything relating to the projects stored in the repository
 */
@Service
public class ProjectService {

    ProjectRepository projectRepository;
    ItemService itemService;
    SprintService sprintService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectRepository: repository that has access to all the projects
     * @param itemService:       service that manages items
     * @param sprintService:     services that manages sprints
     */
    @Autowired
    public ProjectService(ProjectRepository projectRepository, ItemService itemService, SprintService sprintService) {
        this.itemService = itemService;
        this.projectRepository = projectRepository;
        this.sprintService = sprintService;
    }

    /**
     * this method allows an user to fetch all projects from the repository
     *
     * @return : returns the list of all the projects
     */
    public List<Project> findAllProjects() {
        List<Project> allProjects = projectRepository.findAll();
        for (Project project : allProjects) {
            calculateTotalEffort(project);
            calculateRemainingEffort(project);
            calculateEstimatedTotalEffort(project);
            calculateSprintsNeeded(project);
            calculateNumberOfExecutedSprints(project);
        }
        return allProjects;
    }

    /**
     * this method takes a project and calculates the total effort from all its children items within, it doesn't
     * take into account the stories/epics but rather, the children items of those parents (tasks/bugs etc.)
     * this method uses the item service to calculate the effort of each parent, before it sets the total effort
     * of the project
     *
     * @param project: the project that the total effort needs to be calculated
     */
    public void calculateTotalEffort(Project project) {
        itemService.calculatedCombinedEffort(project.getItems());
        int totalEffort = 0;
        for (Item item : project.getItems()) {
            if (item.getType() == ItemType.EPIC.getRepositoryId()
                    || (item.getType() == ItemType.STORY.getRepositoryId()
                    && item.getParent() == null)
                    || (item.getParent() == null)) {
                totalEffort += item.getEffort();
            }
        }
        project.setTotal_effort(totalEffort);
    }

    /**
     * this method takes a project and calculates the remaining effort of all its children items within, it does not
     * take into account any items that have been finished, it only adds the items that are either in the backlog, or
     * in the sprint and are stray, for example a story without a parent or a task without a parent will also be
     * added into the calculations, unlike the calculateTotalEffort method,
     *
     * @param project: the project that the remaining effort needs to be calculated
     */
    public void calculateRemainingEffort(Project project) {
        itemService.calculateEffortOfUnfinishedItems(project.getItems());
        int remainingEffort = 0;
        for (Item item : project.getItems()) {
            if (item.getType() == ItemType.EPIC.getRepositoryId()
                    || (item.getType() == ItemType.STORY.getRepositoryId()
                    && item.getParent() == null)
                    || (item.getParent() == null && item.getStatus() != ItemStatus.FINISHED.getRepositoryId())) {
                remainingEffort += item.getEffort();
            }
        }
        project.setRemaining_effort(remainingEffort);
    }

    /**
     * this method takes a project and calculates the estimated effort of all its chilrden within, the items that are
     * taken under consideration include the epics and stories(that are stray), and the estimated effort of those
     * is summed to form the total estimated effort needed to finish the project
     *
     * @param project: the project that the estimated total effort needs to be calculated
     */
    public void calculateEstimatedTotalEffort(Project project) {
        int estimatedTotalEffort = 0;
        for (Item item : project.getItems()) {
            if (item.getType() == ItemType.EPIC.getRepositoryId()
                    || (item.getType() == ItemType.STORY.getRepositoryId()
                    && item.getParent() == null)) {
                estimatedTotalEffort += item.getEstimatedEffort();
            }
        }
        project.setEstimated_total_effort(estimatedTotalEffort);
    }

    /**
     * this method takes a project and calculates the estimated sprints needed for that project to finish,
     * the formula used is this: total effort / team velocity
     * if the total effort is 0, then the estimated total effort from all the epics and stories is used instead
     * if both are 0, then the estimation also returns 0
     *
     * @param project: the project that the estimated total effort needs to be calculated
     */
    public void calculateSprintsNeeded(Project project) {
        double sprintsNeeded = 0.0;
        //can't divide by 0
        if (project.getTeam_velocity() != 0) {
            ///if the project has no tasks in it, then calculate it based on the estimated effort of each epic/story
            if (project.getTotal_effort() == 0) {
                sprintsNeeded = project.getEstimated_total_effort() / (double) project.getTeam_velocity();
            }
            //if it has tasks, then calculate the sprints needed based on the effort of the tasks
            else {
                sprintsNeeded = project.getTotal_effort() / (double) project.getTeam_velocity();
            }
        }
        //always round the double up, if the total effort is 25 and the velocity 20, then 2 sprints will be needed
        //and not 1
        sprintsNeeded = Math.ceil(sprintsNeeded);
        project.setEstimated_sprints_needed((long) sprintsNeeded);
    }

    /**
     * @param project: the project that the number of already executed sprints needs to be calculated
     */
    public void calculateNumberOfExecutedSprints(Project project) {
        Optional<List<Sprint>> finishedSprintsOptional =
                sprintService.findSprintsByProjectAndStatus(project, SprintStatus.FINISHED);
        if (finishedSprintsOptional.isPresent()) {
            List<Sprint> sprints = finishedSprintsOptional.get();
            project.setExecuted_sprints(sprints.size());
        }
    }

    /**
     * this method allows an user to fetch a certain project from the repository by using the project id
     *
     * @param projectId : the project that the user requested to fetch
     * @return : returns the project with a certain ID that the user requested
     */
    public Optional<Project> findProjectById(long projectId) {
        return projectRepository.findFirstById(projectId);
    }

    /**
     * this method allows the creation of a project, and is stored in the repository
     *
     * @param title                :       title of the new project
     * @param description          : description of the new project
     * @param developersWorkingStr : the number of developers working on this project, helps in estimating
     * @param teamVelocityStr:     the velocity of the team during the execution of the sprints
     * @param sprintDurationStr    : the duration of each sprint executed in the project
     * @param owner                :       the owner of this project
     * @throws ProjectAlreadyExistsException       : if the user tries to update a project with a title of another project that already
     *                                             exists
     * @throws ProjectHasEmptyTitleException       : the user cannot create a project with no title
     * @throws MethodArgumentTypeMismatchException : in case the user inputs strings as numbers for the developers
     *                                             working and estimated sprints needed
     */
    public void createProject(String title, String description, String developersWorkingStr, String teamVelocityStr, String sprintDurationStr, User owner)
            throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        if (title.isEmpty()) {
            throw new ProjectHasEmptyTitleException("Project cannot be created without a title.");
        }
        title = title.trim();
        Optional<Project> projectOptional = projectRepository.findFirstByTitle(title);
        int developersWorking = 0;
        int teamVelocity = 0;
        int sprint_duration = 0;
        if (projectOptional.isPresent()) {
            throw new ProjectAlreadyExistsException("Project with name '" + title + "' already exists.");
        }
        if (description.isEmpty()) {
            description = "No Description";
        } else {
            description = description.trim();
        }
        //example of input handling, not in the scope of this project
        if (!developersWorkingStr.isEmpty()) {
            //might throw exception
            developersWorking = Integer.parseInt(developersWorkingStr);
            if (developersWorking < 0) {
                developersWorking = 0;
            }
        }
        //example of input handling, not in the scope of this project
        if (!teamVelocityStr.isEmpty()) {
            //might throw exception
            teamVelocity = Integer.parseInt(teamVelocityStr);
            if (teamVelocity < 0) {
                teamVelocity = 0;
            }
        }
        if (!sprintDurationStr.isEmpty()) {
            //might throw exception
            sprint_duration = Integer.parseInt(sprintDurationStr);
            if (sprint_duration < 0) {
                sprint_duration = 0;
            }
        }
        Project project = new Project(title, description, developersWorking, teamVelocity, sprint_duration, owner);
        //saving it to get the id from the DB
        project = projectRepository.save(project);
        sprintService.createSprint(project);
    }

    /**
     * this method updates a project in the repository (cannot update projectID or ownerID)
     *
     * @param projectId                  :   the project id that is needed in order for the project to be found in the repository
     * @param title                      :       the title that the admin has possibly updated
     * @param description                : the description that the admin has possibly updated
     * @param developersWorkingStr       : the number of developers working on this project, helps in estimating
     * @param sprintDurationStr          : update to the duration of each sprint in the project
     * @throws ProjectAlreadyExistsException : if the user tries to update a project with a title of another project that already
     *                                       exists
     * @throws ProjectHasEmptyTitleException : if the user tries to update a project and sets the title to blank
     */
    public void updateProject(long projectId, String title, String description, String developersWorkingStr, String sprintDurationStr)
            throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        if (title.isEmpty()) {
            throw new ProjectHasEmptyTitleException("Project cannot be created without a title.");
        }
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        title = title.trim();
        Optional<Project> projectWithThatTitleOptional = projectRepository.findFirstByTitle(title);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            if (projectWithThatTitleOptional.isPresent()) {
                Project projectWithThatTitle = projectWithThatTitleOptional.get();
                if (!projectWithThatTitle.getTitle().equals(project.getTitle())) {
                    throw new ProjectAlreadyExistsException("Project with title '" + projectWithThatTitle.getTitle() + "' already exists.");
                }
            }
            int developersWorking = 0;
            int sprintDuration = 1;
            if (description.isEmpty()) {
                description = "No Description";
            } else {
                description = description.trim();
            }
            //example of input handling, not in the scope of this project
            if (!developersWorkingStr.isEmpty()) {
                //might throw exception
                developersWorking = Integer.parseInt(developersWorkingStr);
                if (developersWorking < 0) {
                    developersWorking = 0;
                }
            }
            //example of input handling, not in the scope of this project
            if (!sprintDurationStr.isEmpty()) {
                //might throw exception
                sprintDuration = Integer.parseInt(sprintDurationStr);
                if (sprintDuration < 1) {
                    sprintDuration = 1;
                }
            }
            project.setTitle(title);
            project.setDescription(description);
            project.setDevelopers_working(developersWorking);
            project.setSprint_duration(sprintDuration);
            projectRepository.save(project);
        }
    }

    /**
     * this method deletes a project from the repository
     *
     * @param projectId: the project id that is needed in order for the project to be found in the repository
     *                   and to be deleted
     */
    public void deleteProject(long projectId) {
        projectRepository.deleteById(projectId);
    }

    /**
     * this method finds a sprint in a certain project, most likely used to confirm that this sprint is included
     * in the project
     *
     * @param projectId: the project that this sprint belongs to
     * @param sprintId:  the sprint that the user wants to find
     * @return : returns an optional that may contain the sprint requested
     */
    public Optional<Sprint> findSprintInProject(long projectId, long sprintId) {
        return sprintService.findSprintByProjectId(projectId, sprintId);
    }

    /**
     * this method calculates all the necessary data for the burn down chart in the history page
     * it calculates the number of sprints and puts them in a ordered array named categories
     * it calculates the ideal burn of effort by dividing the total project effort with the estimated sprints
     * needed to finish the project, and puts every ideal burn of effort per sprint in an array
     * it calculates the actual burn of effort by calculating the effort finished per sprint and puts it in an array
     *
     * @param projectId:       the project that the user requested to see the data of
     * @param finishedSprints: the set of the finished sprints in the project
     * @return : returns a DTO that contains all the data needed in the burn down chart
     */
    public BurnDownChartData calculateBurnDownChartData(long projectId, List<Sprint> finishedSprints) {
        List<Sprint> reversedSprints = null;
        //active sprint should also be counted, and if an active sprint exists, then it should also change the size of
        //the dto arrays
        Optional<Sprint> activeSprintOptional = sprintService.findActiveSprintInProject(new Project(projectId));
        int arraySize;
        //if there are no finished sprints in this project
        if (finishedSprints == null) {
            //then, if there is an active sprint, make the array size 2 (start cell and current sprint)
            if (activeSprintOptional.isPresent()
                    && activeSprintOptional.get().getStatus() == SprintStatus.ACTIVE.getRepositoryId()) {
                arraySize = 2;
            }
            //else only add the start cell
            else {
                arraySize = 1;
            }
        }
        //if there are finished sprints in the project
        else {
            //setting up a new array list because we want the list and its data to be shown to the user NOT reversed
            reversedSprints = new ArrayList<>(finishedSprints);
            //reversing it because the real order is needed to ease the calculations
            Collections.reverse(reversedSprints);
            //if there is active sprint, then make the array size 2 plus the finished sprints
            if (activeSprintOptional.isPresent()
                    && activeSprintOptional.get().getStatus() == SprintStatus.ACTIVE.getRepositoryId()) {
                arraySize = reversedSprints.size() + 2;
                //if no active sprint, just add the start cell and the finished sprints
            } else {
                arraySize = reversedSprints.size() + 1;
            }

        }

        //supposedly, the project is never null
        Project project = null;
        Optional<Project> projectOptional = findProjectById(projectId);
        if (projectOptional.isPresent()) {
            project = projectOptional.get();
        }
        calculateTotalEffort(project);
        calculateEstimatedTotalEffort(project);
        calculateSprintsNeeded(project);

        //here, the sprint names (in order) are written in an array called categories
        //counting the Start cell and the current Sprint
        String[] categories = new String[arraySize];
        categories[0] = "Start";
        for (int i = 1; i < categories.length; i++) {
            categories[i] = "Sprint " + (i);
        }

        //if the estimated sprints needed is 0, then the ideal burn cannot be caulculated
        double[] ideal_burn = new double[0];
        if (project.getEstimated_sprints_needed() != 0) {
            //here, the ideal burn for each sprint is calculated
            ideal_burn = new double[arraySize];
            ideal_burn[0] = (int) project.getTotal_effort();
            double nextEffort = project.getTotal_effort();
            //double needed because the division might be a decimal, and an accurate representation is needed
            double ideal_effort_burn = (double) project.getTotal_effort() / project.getEstimated_sprints_needed();
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_EVEN);
            for (int i = 1; i < ideal_burn.length; i++) {
                ideal_burn[i] = Double.parseDouble(df.format(nextEffort - ideal_effort_burn));
                if (ideal_burn[i] < 0) {
                    ideal_burn[i] = 0;
                    ideal_burn = Arrays.copyOfRange(ideal_burn, 0, i);
                    break;
                }
                nextEffort = ideal_burn[i];
            }
        }

        //here, the actual burn for each sprint is calculated
        int[] actual_burn = new int[arraySize];
        actual_burn[0] = (int) project.getTotal_effort();
        double nextEffort = (int) project.getTotal_effort();
        if (reversedSprints != null) {
            for (int i = 1; i < actual_burn.length; i++) {
                try {
                    actual_burn[i] = (int) (nextEffort - reversedSprints.get(i - 1).getVelocity());
                } catch (IndexOutOfBoundsException exception) {
                    //when there is an active sprint, the reversedSprints array will throw
                    //this exception, instead of having two different loops with a slight variation, we use this
                    //'dirty trick'
                }
                nextEffort = actual_burn[i];
                //the graph line needs to be 'cut' when 0 is reached, we don't want it to keep showing for
                //next sprints etc.
                if (nextEffort == 0) {
                    actual_burn = Arrays.copyOfRange(actual_burn, 0, i + 1);
                    break;
                }
            }
        }
        //setting the final cells of each array in the end
        if (activeSprintOptional.isPresent()) {
            if (activeSprintOptional.get().getStatus() == SprintStatus.ACTIVE.getRepositoryId()) {
                categories[arraySize - 1] = "Current Sprint";
                actual_burn[arraySize - 1] = actual_burn[arraySize - 2] - activeSprintOptional.get().getVelocity();

            }
        }
        //this boolean is not needed for this case
        boolean earlierFinish = false;

        boolean possibleDelay = false;
        //if the latest ideal burn is 0, then there is no point in warning the user
        //if the latest actual remaining is more than the latest ideal remaining, then there is something wrong
        //and the project may be delayed
        if (ideal_burn.length != 0 && ideal_burn[ideal_burn.length - 1] != 0 && actual_burn[actual_burn.length - 1] > ideal_burn[ideal_burn.length - 1]) {
            possibleDelay = true;
        }
        return new BurnDownChartData(categories, ideal_burn, actual_burn, possibleDelay, earlierFinish);
    }

    /**
     * this method takes as input a project, and saves it into the repository, afterwards it returns the saved project
     * in case the user wants to see the id the repository generated for the project
     *
     * @param project: the project that the user requested to save
     * @return: returns the project with the id generated by the repository
     */
    public Project save(Project project) {
        return projectRepository.save(project);
    }
}
