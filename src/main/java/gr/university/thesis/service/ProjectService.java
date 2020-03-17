package gr.university.thesis.service;

import gr.university.thesis.dto.BurnDownChartData;
import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * @return: returns the list of all the projects
     */
    public List<Project> findAllProjects() {
        List<Project> allProjects = projectRepository.findAll();
        for (Project project : allProjects) {
            calculateTotalEffort(project);
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
            if (item.getType() == ItemType.EPIC.getRepositoryId() || (item.getType() == ItemType.STORY.getRepositoryId() && item.getParent() == null)
                    || (item.getParent() == null)) {
                totalEffort += item.getEffort();
            }
        }
        project.setTotal_effort(totalEffort);
    }

    /**
     * this method allows an user to fetch a certain project from the repository by using the project id
     *
     * @return: returns the project with a certain ID that the user requested
     */
    public Optional<Project> findProjectById(long projectId) {
        return projectRepository.findFirstById(projectId);
    }

    /**
     * this method allows the creation of a project, and is stored in the repository
     *
     * @param title:       title of the new project
     * @param description: description of the new project
     * @param owner:       the owner of this project
     */
    public void createProject(String title, String description, User owner) {
        Project project = new Project(title, description, owner);
        //saving it to get the id from the DB
        project = projectRepository.save(project);
        sprintService.createSprint(project);
    }

    /**
     * this method updates a project in the repository (cannot update projectID or ownerID)
     *
     * @param projectId:   the project id that is needed in order for the project to be found in the repository
     * @param title:       the title that the admin has possibly updated
     * @param description: the description that the admin has possibly updated
     */
    public void updateProject(long projectId, String title, String description) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            project.setTitle(title);
            project.setDescription(description);
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
     * @return: returns an optional that may contain the sprint requested
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
     * @return: returns a DTO that contains all the data needed in the burn down chart
     */
    public BurnDownChartData calculateBurnDownChartData(long projectId, List<Sprint> finishedSprints) {
        //setting up a new array list because we want the list to be shown to the user NOT reversed
        List<Sprint> reversedSprints = new ArrayList<>(finishedSprints);
        //reversing it because the real order is needed to ease the calculations
        Collections.reverse(reversedSprints);
        //supposedly, the project is never null
        Project project = null;
        Optional<Project> projectOptional = findProjectById(projectId);
        if (projectOptional.isPresent()) {
            project = projectOptional.get();
        }
        calculateTotalEffort(project);

        //here, the sprint names (in order) are written in an array called categories
        String[] categories = new String[reversedSprints.size() + 1];
        categories[0] = "Start";
        for (int i = 1; i < categories.length; i++) {
            categories[i] = "Sprint " + (i);
        }

        //here, the ideal burn for each sprint is calculated
        int[] ideal_burn = new int[reversedSprints.size() + 1];
        ideal_burn[0] = (int) project.getTotal_effort();
        long nextEffort = (int) project.getTotal_effort();
        //double needed because the division might be a decimal, and an accurate representation is needed
        double ideal_effort_burn = (double) project.getTotal_effort() / project.getEstimated_sprints_needed();
        for (int i = 1; i < ideal_burn.length; i++) {
            ideal_burn[i] = (int) (nextEffort - ideal_effort_burn);
            if (ideal_burn[i] < 0) {
                ideal_burn[i] = 0;
            }
            nextEffort = ideal_burn[i];
        }

        //here, the actual burn for each sprint is calculated
        int[] actual_burn = new int[reversedSprints.size() + 1];
        actual_burn[0] = (int) project.getTotal_effort();
        nextEffort = (int) project.getTotal_effort();
        for (int i = 1; i < actual_burn.length; i++) {
            actual_burn[i] = (int) (nextEffort - reversedSprints.get(i - 1).getVelocity());
            nextEffort = actual_burn[i];
        }

        return new BurnDownChartData(categories, ideal_burn, actual_burn);
    }
}
