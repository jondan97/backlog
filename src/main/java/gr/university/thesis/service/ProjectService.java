package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        sprintService.firstSprint(project);
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
}
