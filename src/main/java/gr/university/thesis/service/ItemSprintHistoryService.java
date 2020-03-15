package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.enumeration.ItemStatus;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import gr.university.thesis.repository.ItemSprintHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
            itemService.setStatusToItemAndChildren(item, ItemStatus.READY);
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
                itemService.setStatusToItemAndChildren(item, ItemStatus.BACKLOG);
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
        List<ItemSprintHistory> associations = new ArrayList<>();
        ItemSprintHistory parentItemSprintHistory = new ItemSprintHistory(item, sprint,
                TaskBoardStatus.TO_DO);
        parentItemSprintHistory.getSprintItemId().setItemId(item.getId());
        parentItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
        associations.add(parentItemSprintHistory);
        //if it's a story or an epic, then create an association for the children
        if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
            for (Item child : item.getChildren()) {
                if (child.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                    ItemSprintHistory childItemSprintHistory = new ItemSprintHistory(child, sprint,
                            TaskBoardStatus.TO_DO);
                    childItemSprintHistory.getSprintItemId().setItemId(child.getId());
                    childItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                    associations.add(childItemSprintHistory);
                    //if the parent is an epic and the child a story, then also create an association for the
                    // children of the story
                    for (Item childOfChild : child.getChildren()) {
                        if (childOfChild.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                            ItemSprintHistory childOfChildItemSprintHistory = new ItemSprintHistory(childOfChild, sprint,
                                    TaskBoardStatus.TO_DO);
                            childOfChildItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                            childOfChildItemSprintHistory.getSprintItemId().setItemId(childOfChild.getId());
                            associations.add(childOfChildItemSprintHistory);
                        }
                    }
                }
            }
        }
        return associations;
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

    /**
     * this method changes the status of a given association, either to the previous or to the next and the direction
     * is indicated by the index
     *
     * @param sprint: the sprint that belongs to this association
     * @param item:   the item that belongs to this association
     * @param index:  the indicator towards which direction the status should go to, if index > 0 then it means that
     *                the status should be increased by 1, whereas if index < 0 then it means that the status should be
     *                decreased by one (based on the repository id)
     */
    public void changeStatusOfAssociationByOne(Sprint sprint, Item item, int index) {
        Optional<ItemSprintHistory> itemSprintHistoryOptional = itemSprintHistoryRepository.findFirstByItemAndSprint(item, sprint);
        if (itemSprintHistoryOptional.isPresent()) {
            ItemSprintHistory itemSprintHistory = itemSprintHistoryOptional.get();
            //checking for edge cases
            if ((itemSprintHistory.getStatus().getRepositoryId() != 1 && index < 0) ||
                    (itemSprintHistory.getStatus().getRepositoryId() != 4 && index > 0)) {
                TaskBoardStatus currentStatus = itemSprintHistory.getStatus();
                itemSprintHistory.setStatus(TaskBoardStatus.findTaskBoardStatusByRepositoryId(currentStatus.getRepositoryId() + index));
                itemSprintHistoryRepository.save(itemSprintHistory);
            }
        }
    }

    /**
     * this method transfers all the unfinished items from the old sprint to the new sprint, this means that all
     * associations with task board status that 'DONE', will not be transferred whereas all the other associations,
     * will be transferred with the status that they were left, when the sprint ended
     *
     * @param oldSprint: the old sprint that we want to transfer the associations from
     */
    public void transferUnfinishedItemsFromOldSprint(Sprint oldSprint) {
        Sprint newSprint = sprintService.createSprint(oldSprint.getProject());
        Set<ItemSprintHistory> associationsForNextSprint = new HashSet<>();
        for (ItemSprintHistory association : oldSprint.getAssociatedItems()) {
            //if the item is a task or a bug and it was not finished in the old sprint, then move it to the new one
            if ((association.getItem().getType() == ItemType.TASK.getRepositoryId() ||
                    association.getItem().getType() == ItemType.BUG.getRepositoryId()) &&
                    association.getStatus() != TaskBoardStatus.DONE &&
                    association.getItem().getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                associationsForNextSprint.addAll(createAssociationForItemAndParents(association.getItem(),
                        newSprint, association.getStatus()));
            }
            //done status can only be tasks/bugs, so no need to check if it's an epic/story etc.
            else if (association.getStatus() == TaskBoardStatus.DONE) {
                itemService.changeItemStatus(association.getItem(), ItemStatus.FINISHED);
                boolean isParentComplete = false;
                if (association.getItem().getParent() != null) {
                    isParentComplete = itemService.checkIfParentIsComplete(association.getItem());
                }
                //if the parent of the child is complete, then change the parent's state to 'finished' as well
                if (isParentComplete) {
                    itemService.changeItemStatus(association.getItem().getParent(), ItemStatus.FINISHED);
                    if (association.getItem().getParent().getParent() != null) {
                        boolean isParentOfParentComplete = itemService.checkIfParentIsComplete(association.getItem().getParent().getParent());
                        //if the parent of the parent is complete, then also change his state to 'finished'
                        if (isParentOfParentComplete) {
                            itemService.changeItemStatus(association.getItem().getParent().getParent(), ItemStatus.FINISHED);
                        }
                    }
                }
            }
            // in case the item has no children, this case is only added to avoid breaking the system with
            //stories or epics that are added onto the ready sprint without children
            //however this edge case is not perfect as an epic/story can still be added
            //to the ready sprint and have children that have been finished and cannot be seen
            //by the user, but as of now, entirely solving the edge case is not of primary concern
            else if ((association.getItem().getType() == ItemType.EPIC.getRepositoryId() ||
                    association.getItem().getType() == ItemType.STORY.getRepositoryId()) &&
                    association.getItem().getChildren().isEmpty()) {
                associationsForNextSprint.addAll(createAssociationForItemAndParents(association.getItem(),
                        newSprint, association.getStatus()));
            }
            System.out.println(association.getItem().getChildren());
        }
        itemSprintHistoryRepository.saveAll(associationsForNextSprint);
    }

    /**
     * this method creates associations for an item and its parents, for example if a task needs to be transferred
     * to a new sprint, creating associations for the story that it might belong to, is also needed. What is more, if
     * the story has its own parent, then an association for that parent (epic) needs to also be created
     *
     * @param item:      the item that the user requested to create an association for it and its parents
     * @param newSprint: the new sprint that these items are going to be associated with
     * @param oldStatus: the old status, from when the previous sprint ended, for the item to inherit
     * @return: returns a set with the unique associations that were created
     */
    public Set<ItemSprintHistory> createAssociationForItemAndParents(Item item, Sprint newSprint, TaskBoardStatus oldStatus) {
        //the developer chose Set as their data structure here, because they want to avoid having duplicates
        //inserted into the repository, two items may have the same parent, and two associations with the same
        //parent ID and sprint ID will be created, which is unwanted
        Set<ItemSprintHistory> associations = new HashSet<>();
        item = itemService.changeItemStatus(item, ItemStatus.READY);
        ItemSprintHistory newAssociation = new ItemSprintHistory(item, newSprint, oldStatus);
        associations.add(newAssociation);
        if (item.getParent() != null) {
            Item parent = item.getParent();
            parent = itemService.changeItemStatus(parent, ItemStatus.READY);
            if (parent.getType() == ItemType.EPIC.getRepositoryId()) {
                ItemSprintHistory newParentAssociation = new ItemSprintHistory(parent, newSprint, TaskBoardStatus.NONE);
                associations.add(newParentAssociation);
            }
            //different case is needed for stories, as they might have a parent on their own
            else if (parent.getType() == ItemType.STORY.getRepositoryId()) {
                ItemSprintHistory newParentAssociation = new ItemSprintHistory(parent, newSprint, TaskBoardStatus.NONE);
                associations.add(newParentAssociation);
                //if the story has an epic as its parent
                if (parent.getParent() != null) {
                    Item parentOfParent = parent.getParent();
                    parentOfParent = itemService.changeItemStatus(parentOfParent, ItemStatus.READY);
                    ItemSprintHistory newParentOfParentAssociation = new ItemSprintHistory(parentOfParent, newSprint, TaskBoardStatus.NONE);
                    associations.add(newParentOfParentAssociation);
                }
            }
        }
        return associations;
    }
}
