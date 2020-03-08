package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * service that is associated with the management of the sprints
 */
@Service
public class SprintService {

    ItemService itemService;
    SprintRepository sprintRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param itemService:      service that manages items
     * @param sprintRepository: repository that has access to all sprints
     */
    @Autowired
    public SprintService(ItemService itemService, SprintRepository sprintRepository) {
        this.itemService = itemService;
        this.sprintRepository = sprintRepository;
    }

    /**
     * this method initialises the first sprint of every project that is created
     * and sets the sprint status to 2 (ready to be filled with items)
     *
     * @param project: the project that this sprint belongs to
     */
    public void firstSprint(Project project) {
        Sprint sprint = new Sprint(project, (byte) 2);
        sprintRepository.save(sprint);
    }

    /**
     * this method finds a sprint in the repository and returns it to the user
     *
     * @param sprint: the sprint that the user requested to find
     * @return: returns an optional that may contain a sprint
     */
    public Optional<Sprint> findSprintById(Sprint sprint) {
        return sprintRepository.findById(sprint.getId());
    }

    /**
     * this method searches the repository for the active sprint of the project that the user requested
     *
     * @param project: the project that the user requested to find the active sprint in
     * @return: returns an optional that may contain the requested sprint
     */
    public Optional<Sprint> findActiveSprintInProject(Project project) {
        Optional<Sprint> readySprintOptional = sprintRepository.findFirstByProjectAndStatus(project, (byte) 2);
        //if there is a sprint ready to be filled with items
        if (readySprintOptional.isPresent()) {
            return readySprintOptional;
        } else {
            Optional<Sprint> activeSprintOptional = sprintRepository.findFirstByProjectAndStatus(project, (byte) 1);
            //if there is an active running sprint
            if (activeSprintOptional.isPresent()) {
                return activeSprintOptional;
            }
        }
        //if no active or ready sprint was found
        return readySprintOptional;
    }

    /**
     * this methods starts a sprint and moves it to an active state
     *
     * @param sprintId: the sprint that the user wants to start
     */
    public void startSprint(long sprintId) {
        Optional<Sprint> sprintOptional = findSprintById(new Sprint(sprintId));
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            sprint.setStatus((byte) 1);
            sprint.setStart_date(new Date());
            for (Item item : getAssociatedItemsList(sprint.getAssociatedItems())) {
                itemService.setStatusToItemAndChildren(item, (byte) 3);
            }
            sprintRepository.save(sprint);
        }
    }

    /**
     * this methods finishes a sprint and moves it to a finish state
     *
     * @param sprintId: the sprint that the user wants to finish
     * @param project:  the project that this sprint belongs to
     */
    public void finishSprint(long sprintId, Project project) {
        Optional<Sprint> sprintOptional = findSprintById(new Sprint(sprintId));
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            sprint.setProject(project);
            sprint.setStatus((byte) 2);
            sprint.setEnd_date(new Date());
            for (Item item : getAssociatedItemsList(sprint.getAssociatedItems())) {
                itemService.setStatusToItemAndChildren(item, (byte) 2);
            }
            sprintRepository.save(sprint);
        }
    }

    /**
     * this method takes as input associations between items and sprint, and returns in a list the set of items
     * from all the associations
     *
     * @param associations: the set of item-sprint history records that each one contains an item
     * @return: returns a list of items
     */
    public List<Item> getAssociatedItemsList(Set<ItemSprintHistory> associations) {
        List<Item> associatedItems = new ArrayList<>();
        for (ItemSprintHistory ish : associations) {
            associatedItems.add(ish.getItem());
        }
        return associatedItems;
    }


    /**
     * this method finds a sprint in a certain project, most likely used to confirm that this sprint is included
     * in the project
     *
     * @param sprintId:  the sprint that the user wants to find
     * @param projectId: the project that this sprint belongs to
     * @return: returns an optional that may contain the sprint requested
     */
    public Optional<Sprint> findSprintInProject(long sprintId, long projectId) {
        return sprintRepository.findDistinctSprintByProjectId(sprintId, projectId);

    }
}
