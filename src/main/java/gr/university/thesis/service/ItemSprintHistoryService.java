package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import gr.university.thesis.repository.ItemSprintHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * service that handles everything relating to the history between the items and the sprints stored in the repository
 */
@Service
public class ItemSprintHistoryService {

    ItemService itemService;
    SprintService sprintService;
    ItemSprintHistoryRepository itemSprintHistoryRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param itemService:                 service that manages items
     * @param sprintService:               service that manages sprints
     * @param itemSprintHistoryRepository: repository for the history of item/sprint
     */
    @Autowired
    public ItemSprintHistoryService(ItemService itemService, SprintService sprintService,
                                    ItemSprintHistoryRepository itemSprintHistoryRepository) {
        this.itemService = itemService;
        this.sprintService = sprintService;
        this.itemSprintHistoryRepository = itemSprintHistoryRepository;
    }

    /**
     * this method moves an item to a sprint, and creates the necessary associations in the item-sprint history table in
     * the repository
     *
     * @param item:   the item the user wants to move into a sprint
     * @param sprint: the sprint that the user wants to move an item into
     * @param parent: the parent of the item, needed to define whether new associations should be created or removed
     */
    public void moveItemToSprint(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        if (itemOptional.isPresent() && sprintOptional.isPresent()) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            itemService.setStatusToItemAndChildren(item, (byte) 2);
            //whatever the user clicks to move to the sprint, will have no parent
            if (parent == null) {
                item.setParent(null);
            }
            //if the user changes the parent to a parent that is in the ready sprint, then we need to set it this way:
            else {
                item.setParent(parent);
            }
            itemService.saveToRepository(item);
            List<ItemSprintHistory> assocations = createAssociationForItemAndChildren(item, sprint);
            itemSprintHistoryRepository.saveAll(assocations);
        }
    }

    /**
     * this method removes an item from a sprint, and deletes the existing associations in the item-sprint history
     * table in the repository
     *
     * @param item:   the item the user wants to remove from a sprint
     * @param sprint: the sprint that the user wants to remove an item from
     * @param parent: the parent of the item, needed to define whether new associations should be created or removed
     */
    public void removeItemToSprint(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        if (itemOptional.isPresent() && sprintOptional.isPresent()) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            Optional<ItemSprintHistory> itemSprintHistoryOptional = itemSprintHistoryRepository.findFirstByItemAndSprint(item,
                    sprint);
            if (itemSprintHistoryOptional.isPresent()) {
                itemSprintHistoryRepository.delete(itemSprintHistoryOptional.get());
                itemService.setStatusToItemAndChildren(item, (byte) 1);
                //whatever the user clicks to move to the sprint, will have no parent
                if (parent == null) {
                    item.setParent(null);
                }
                //if the user changes the parent to a parent that is in the ready sprint, then we need to set it this way:
                else {
                    item.setParent(parent);
                }
                itemService.saveToRepository(item);
                List<ItemSprintHistory> associations = createAssociationForItemAndChildren(item, sprint);
                itemSprintHistoryRepository.deleteAll(associations);
            }
        }
    }

    /**
     * this method creates associations between an item and its children with a sprint, and returns a list of
     * associations that will be stored in the item-sprint-history table in the repository
     *
     * @param item:   the item(possibly parent) that we want to create an association for
     * @param sprint: the sprint that belongs to the assocation
     * @return: returns a list with all the associations created in this method
     */
    private List<ItemSprintHistory> createAssociationForItemAndChildren(Item item, Sprint sprint) {
        List<ItemSprintHistory> assocations = new ArrayList<>();
        ItemSprintHistory parentItemSprintHistory = new ItemSprintHistory(item, sprint,
                TaskBoardStatus.TO_DO);
        parentItemSprintHistory.getSprintItemId().setItemId(item.getId());
        parentItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
        assocations.add(parentItemSprintHistory);
        //if it's a story or an epic, then create an association for the children
        if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
            for (Item child : item.getChildren()) {
                ItemSprintHistory childItemSprintHistory = new ItemSprintHistory(child, sprint,
                        TaskBoardStatus.TO_DO);
                childItemSprintHistory.getSprintItemId().setItemId(child.getId());
                childItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                assocations.add(childItemSprintHistory);
                //if the parent is an epic and the child a story, then also create an association for the
                // children of the story
                for (Item childOfChild : child.getChildren()) {
                    ItemSprintHistory childOfChildItemSprintHistory = new ItemSprintHistory(childOfChild, sprint,
                            TaskBoardStatus.TO_DO);
                    childOfChildItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                    childOfChildItemSprintHistory.getSprintItemId().setItemId(childOfChild.getId());
                    assocations.add(childOfChildItemSprintHistory);
                }
            }
        }
        return assocations;
    }

    /**
     * this method checks if an item needs to have its associations updated when the user requests to
     * assign it to a new parent, for example if the item is in the backlog and needs to be set
     * under a parent that is in the ready sprint, then new associations need to be created in the
     * item-sprint-history table for that specific item and its children
     *
     * @param item:   the item that we want to check if association updating is needed
     * @param sprint: the sprint that the item belongs to
     * @param parent: the new parent of the sprint
     */
    public void manageItemSprintAssociation(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        Optional<Item> parentOptional;
        if (parent.getId() != 0) {
            parentOptional = itemService.findItemById(parent);
        } else {
            return;
        }
        if (itemOptional.isPresent() && sprintOptional.isPresent() && (parentOptional != null && parentOptional.isPresent())) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            parent = parentOptional.get();
            if (item.getStatus() == 1 && parent.getStatus() == 1) {
                return;
            } else if (item.getStatus() == 1 && parent.getStatus() == 2) {
                moveItemToSprint(item, sprint, parent);
            } else if (item.getStatus() == 2 && parent.getStatus() == 1) {
                removeItemToSprint(item, sprint, parent);
            } else if (item.getStatus() == 1 && parent.getStatus() == 1) {
                return;
            }
        }
    }

    /**
     * this method takes as input two types (at max) of item (bug and task for example), and returns all the items
     * that belongs to a certain sprint and have a certain task board status (for example 'done'), the developer chose
     * two item types, because at the time of creating this method, they needed a way to fetch both tasks and bugs,
     * and instead of using 2 queries for it, they used only one
     *
     * @param sprint:          the sprint that the requested items belong to
     * @param taskBoardStatus: the task board status of the requested items (for example 'done')
     * @param itemType1:       item type 1 (for example bug)
     * @param itemType2:       item type 2 (for example task)
     * @return: returns an optional that may contain a list with all the items requested
     */
    public Optional<List<ItemSprintHistory>> findAllAssociationsByStatus(Sprint sprint, TaskBoardStatus taskBoardStatus, ItemType itemType1, ItemType itemType2) {
        return itemSprintHistoryRepository.findAllBySprintAndStatusAndItemTypes(sprint.getId(),
                taskBoardStatus, itemType1.getRepositoryId(), itemType2.getRepositoryId());
    }
}
