package gr.university.thesis.service;

import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
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

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectRepository: repository that has access to all the projects
     */
    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
}
