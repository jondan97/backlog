package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.repository.ItemRepository;
import gr.university.thesis.repository.ProjectRepository;
import gr.university.thesis.repository.UserRepository;
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
    UserRepository userRepository;
    ItemRepository itemRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectRepository: repository that has access to all the projects
     * @param userRepository:    repository that has access to all the users
     * @param itemRepository:    repository that has access to all the items
     */
    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * this method allows an user to fetch all projects from the repository
     *
     * @return: returns the list of all the projects
     */
    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }


    /**
     * this method allows an user to fetch all projects from the repository
     *
     * @return: returns the list of all the projects
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
        projectRepository.save(project);
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
     * this method creates a new item and saves it into the repository
     *
     * @param title:       the title of the new item
     * @param description: the description of the new item
     * @param type:        the type of the new item
     * @param priority:    the priority of the new item
     * @param effort:      the effort required for this item to complete
     * @param project:     the project that this item belongs to
     * @param assignee:    the user that this item has been assigned to
     * @param owner:       the user who created this item
     */
    public void createItem(String title, String description, ItemType type, ItemPriority priority, int effort, Project project, User assignee, User owner) {
        Item item = new Item(title, description, type.getRepositoryId(), priority.getRepositoryId(), effort, project, assignee, owner);
        itemRepository.save(item);
    }
}
