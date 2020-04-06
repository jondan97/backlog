package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemStatus;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.exceptions.ItemAlreadyExistsException;
import gr.university.thesis.exceptions.ItemHasEmptyTitleException;
import gr.university.thesis.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * service that handles everything relating to the items stored in the repository
 */
@Service
public class ItemService {

    ItemRepository itemRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param itemRepository: repository that has access to all the items
     */
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;

    }

    /**
     * this method searches the repository in search of an item requested by the user
     *
     * @param item: the item that the user wants to find
     * @return : returns an optional that may contain the item the user has requested
     */
    public Optional<Item> findItemById(Item item) {
        return itemRepository.findById(item.getId());
    }

    /**
     * this method calls the repository in order to find all the items that belong to a certain project  by using the
     * project id
     *
     * @param projectId: id of the project
     * @return returns all the items that are associated with a project
     */
    public Iterable<Item> findAllItemsByProjectId(long projectId) {
        Iterable<Item> projectItems = itemRepository.findByProjectIdOrderByPriorityDesc(projectId);
        calculatedCombinedEffort(projectItems);
        return projectItems;
    }

    /**
     * this method calls the repository in order to find all  the items that have a certain status and belong to a
     * certain project
     *
     * @param projectId:  id of the project
     * @param itemStatus: the status of the item, could be in backlog, finished etc.
     * @return returns all the items that are associated with a project
     */
    public Iterable<Item> findAllItemsInBacklogByProjectId(long projectId, ItemStatus itemStatus) {
        Iterable<Item> projectItems = itemRepository.findByProjectIdAndStatusOrderByPriorityDesc(projectId, (byte) itemStatus.getRepositoryId());
        calculatedCombinedEffort(projectItems);
        return projectItems;
    }

    /**
     * this method calculates the  combined effort of all the items (tasks/bugs etc.) for each epic/story
     * epics and stories have no effort on their own, and the effort for each one is the sum of all the tasks/bugs etc.
     * they contain
     *
     * @param items: the set of items that Java needs to calculate the combined effort of
     */
    public void calculatedCombinedEffort(Iterable<Item> items) {
        for (Item item : items) {
            if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
                int calculatedEffort = 0;
                for (Item child : item.getChildren()) {
                    //if it's not a story then it means that the item in the iteration is a story and its children
                    //will be calculated now
                    if (child.getType() != ItemType.STORY.getRepositoryId()) {
                        calculatedEffort += child.getEffort();
                    }
                    //if its a story, then the item is an epic and its children will be calculated now
                    else if (child.getType() == ItemType.STORY.getRepositoryId()) {
                        for (Item childOfStory : child.getChildren()) {
                            calculatedEffort += childOfStory.getEffort();
                        }
                    }
                }
                item.setEffort(calculatedEffort);
            }
        }
    }

    /**
     * this method calculates the combined effort of all the items (tasks/bugs etc.) for each epic/story that are
     * and only takes into account the effort of all the unfinished children
     * epics and stories have no effort on their own, and the effort for each one is the sum of all the tasks/bugs etc.
     * they contain
     *
     * @param items: the set of items that Java needs to calculate the unfinished effort of
     */
    public void calculateEffortOfUnfinishedItems(Iterable<Item> items) {
        for (Item item : items) {
            if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
                int calculatedEffort = 0;
                for (Item child : item.getChildren()) {
                    //if it's not a story then it means that the item in the iteration is a story and its children
                    //will be calculated now
                    if (child.getType() != ItemType.STORY.getRepositoryId()) {
                        if (child.getStatus() != ItemStatus.FINISHED.getRepositoryId())
                            calculatedEffort += child.getEffort();
                    }
                    //if its a story, then the item is an epic and its children will be calculated now
                    else if (child.getType() == ItemType.STORY.getRepositoryId()) {
                        for (Item childOfStory : child.getChildren()) {
                            if (childOfStory.getStatus() != ItemStatus.FINISHED.getRepositoryId())
                                calculatedEffort += childOfStory.getEffort();
                        }
                    }
                }
                item.setEffort(calculatedEffort);
            }
        }
    }

    /**
     * this method creates a new item and saves it into the repository
     *
     * @param title              :       the title of the new item
     * @param description        : the description of the new item
     * @param acceptanceCriteria : the criteria requires to be fulfilled in order for the item to be complete
     * @param type               :        the type of the new item
     * @param priority           :    the priority of the new item
     * @param effortStr          :   the effort required for this item to complete
     * @param estimatedEffortStr :   the estimated effort required for this item to complete
     * @param project            :     the project that this item belongs to
     * @param assignee           :    the user that this item has been assigned to
     * @param owner              :       the user who created this item
     * @param parent             :      the parent (epic/story) of this item
     * @throws ItemAlreadyExistsException : user has tried to create an item with the same title
     * @throws ItemHasEmptyTitleException : user has tried to create an item with no title
     */
    public void createItem(String title, String description, String acceptanceCriteria, ItemType type,
                           ItemPriority priority, String effortStr, String estimatedEffortStr,
                           Project project, User assignee, User owner, Item parent)
            throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        if (title.isEmpty()) {
            throw new ItemHasEmptyTitleException("Item cannot be created without a title.");
        }
        title = title.trim();
        if (itemRepository.findFirstByTitleAndProject(title, project).isPresent()) {
            throw new ItemAlreadyExistsException("Item with title '" + title + "' already exists.");
        }
        int effort = 0;
        //if the item has no parent
        if (parent.getId() == 0) {
            parent = null;
        }
        //example of input handling, not in the scope of this project
        if (!effortStr.isEmpty()) {
            effort = Integer.parseInt(effortStr);
            if (effort > 10) {
                effort = 10;
            } else if (effort < 0) {
                effort = 0;
            }
        }
        int estimatedEffort = 0;
        if (!estimatedEffortStr.isEmpty()) {
            estimatedEffort = Integer.parseInt(estimatedEffortStr);
            if (estimatedEffort < 0) {
                estimatedEffort = 0;
            }
        }
        //if its an epic or a story, we want to calculate its children's' effort
        if (type == ItemType.EPIC || type == ItemType.STORY) {
            effort = 0;
        }
        if (description.isEmpty()) {
            description = "No description";
        } else {
            description = description.trim();
        }
        if (acceptanceCriteria.isEmpty()) {
            acceptanceCriteria = "No Acceptance Criteria";
        } else {
            acceptanceCriteria = acceptanceCriteria.trim();
        }
        Item item = new Item(title, description, acceptanceCriteria, type.getRepositoryId(), priority.getRepositoryId(), effort, estimatedEffort, project, assignee, owner, parent);
        itemRepository.save(item);
    }

    /**
     * this method updates an existing item and saves it into the repository
     *
     * @param itemId              :      id of the item, needed to find it on the repository
     * @param title               :       title of the item
     * @param description         : description of the item
     * @param acceptanceCriteria: under what conditions, is this item considered done
     * @param type                :        ItemType of the item
     * @param priority            :    ItemPriority of the item
     * @param effortStr           :      effort required to finish this item
     * @param estimatedEffortStr  estimated effort required to finish this item
     * @param assignee            :    the user that this item is assigned to
     * @param parent              :      the parent (epic/story) of this item
     * @throws ItemAlreadyExistsException : user has tried to set the item's title to one that already exists
     * @throws ItemHasEmptyTitleException : user has tried to set the item's title to blank
     */
    public void updateItem(long itemId, String title, String description, String acceptanceCriteria, String type,
                           String priority, String effortStr, String estimatedEffortStr, User assignee, Item parent)
            throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        if (title.isEmpty()) {
            throw new ItemHasEmptyTitleException("Item cannot be created without a title.");
        }
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        title = title.trim();
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            Optional<Item> itemWithThatTitleOptional = itemRepository.findFirstByTitleAndProject(title, item.getProject());
            if (itemWithThatTitleOptional.isPresent()) {
                Item itemWithThatTitle = itemWithThatTitleOptional.get();
                if (!itemWithThatTitle.getTitle().equals(item.getTitle())) {
                    throw new ItemAlreadyExistsException("Item with title '" + itemWithThatTitle.getTitle() + "' already exists.");
                }
            }
            int effort = 0;
            item.setTitle(title);
            if (description.isEmpty()) {
                description = "No Description";
            } else {
                description = description.trim();
            }
            item.setDescription(description);
            if (acceptanceCriteria.isEmpty()) {
                acceptanceCriteria = "No Acceptance Criteria";
            } else {
                acceptanceCriteria = acceptanceCriteria.trim();
            }
            item.setAcceptanceCriteria(acceptanceCriteria);
            item.setType(ItemType.valueOf(type).getRepositoryId());
            item.setPriority(ItemPriority.valueOf(priority).getRepositoryId());
            //example of input handling, not in the scope of this project
            if (!effortStr.isEmpty()) {
                //might throw exception
                effort = Integer.parseInt(effortStr);
                if (effort > 10) {
                    effort = 10;
                } else if (effort < 0) {
                    effort = 0;
                }
            }
            item.setEffort(effort);
            int estimatedEffort = 0;
            if (!estimatedEffortStr.isEmpty()) {
                //might throw exception
                estimatedEffort = Integer.parseInt(estimatedEffortStr);
                if (estimatedEffort < 0) {
                    estimatedEffort = 0;
                }
            }
            item.setEstimatedEffort(estimatedEffort);
            item.setAssignee(assignee);
            // if item was updated to no parent
            if (parent.getId() == 0) {
                item.setParent(null);
            }
            //all other cases
            else {
                Optional<Item> parentOptional = itemRepository.findById(parent.getId());
                //updating status to match that of the parent's
                if (parentOptional.isPresent()) {
                    setStatusToItemAndChildren(item, ItemStatus.findItemTypeByRepositoryId(parentOptional.get().getStatus()));
                    item.setParent(parentOptional.get());
                }
            }
            itemRepository.save(item);
        }
    }

    /**
     * this method deletes a item from the repository
     *
     * @param itemId: the item id that is needed in order for the item to be found in the repository
     *                and to be deleted
     */
    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    /**
     * this method finds an item in a certain project, most likely used to confirm that this item is included
     * in the project
     *
     * @param itemId:    the item that the user wants to find
     * @param projectId: the project that this item belongs to
     * @return : returns an optional that may contain the item requested
     */
    public Optional<Item> findItemInProject(long itemId, long projectId) {
        return itemRepository.findDistinctItemByProjectId(itemId, projectId);
    }

    /**
     * this method updates the assignee of an item to another assignee, and only admins/projects managers or current
     * assignees can change that
     *
     * @param itemId:   the id of the item that we want the new assignee to 'own'
     * @param assignee: the new assignee that the user wants to 'own' this item
     */
    public void updateAssignee(long itemId, User assignee) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setAssignee(assignee);
            itemRepository.save(item);
        }
    }


    /**
     * this method saves an item to the repository
     *
     * @param item: the item the user wants to save in the repository
     */
    public void saveToRepository(Item item) {
        itemRepository.save(item);
    }

    /**
     * this method changes the status of an item and then saves it to the repository
     *
     * @param item:       the item that the user requested to change the status of
     * @param itemStatus: the new item status the user requested to have
     * @return : returns the item that had its status changed and was saved in the repository
     */
    public Item changeItemStatus(Item item, ItemStatus itemStatus) {
        item.setStatus((byte) itemStatus.getRepositoryId());
        return itemRepository.save(item);
    }

    /**
     * this method updates the status of an item and all its children (but the finished ones) to a new one,
     * for status information please check the Item Class
     *
     * @param item:   the item (perhaps parent) that the user requested to update the status of
     * @param status: the new status that the user wants this item to have
     */
    public void setStatusToItemAndChildren(Item item, ItemStatus status) {
        if (item.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
            //if it's a story or an epic, then set the same status for the children
            if ((item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId())) {
                item.setStatus((byte) status.getRepositoryId());
                for (Item child : item.getChildren()) {
                    if (child.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                        child.setStatus((byte) status.getRepositoryId());
                    }
                    //if the parent is an epic and the child a story, then set the same status for the children of the story
                    for (Item childOfChild : child.getChildren()) {
                        if (childOfChild.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                            childOfChild.setStatus((byte) status.getRepositoryId());
                        }
                    }
                }
            } else {
                item.setStatus((byte) status.getRepositoryId());
            }
        }
    }

    /**
     * this method checks if the parent of an item is finished, this is done by checking all the children of the parent
     * and if all the children have a status of 'FINISHED', then the parent is also finished
     *
     * @param item: the item we want to check the parent of
     * @return : returns true if parent has all its children finished, false if any of its children is not finished
     */
    public boolean checkIfParentIsComplete(Item item) {
        boolean finished = false;
        boolean childHasUnfinishedItems = false;
        Item parent;
        //need to check if the item is an epic, as epics will never have a parent
        if (item.getType() != ItemType.EPIC.getRepositoryId()) {
            parent = item.getParent();
        } else {
            parent = item;
        }
        //counter inside the iteration
        int i = 0;
        for (Item child : parent.getChildren()) {
            //if its an epic and it has a story child
            if (child.getType() == ItemType.STORY.getRepositoryId()) {
                //if the story has children on its own (that are not finished)
                for (Item childOfChild : child.getChildren()) {
                    if (childOfChild.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                        childHasUnfinishedItems = true;
                        break;
                    }
                }
            } else {
                //if the child is a task/bug
                if (child.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                    break;
                }
            }
            //in each iteration, we need to check if any of the children, has a set of children that might be
            //unfinished, for example if an epic has a story as a child, and the story has its own set of
            //children, then we need to also check if any of the story children are unfinished
            if (childHasUnfinishedItems) {
                break;
            }
            i++;
        }
        //if the number of the children is the same as the counter, it means that we have checked all the children
        //of this parent
        if (parent.getChildren().size() == i) {
            finished = true;
        }
        return finished;
    }

    /**
     * this method checks if an item (epic or story), has any children and if it has, whether any of them are finished.
     * If any of them are finished, then the item cannot be deleted, as the finished items will also be deleted
     *
     * @param item: the item that the user requested to check if it has any finished children
     */
    public boolean checkIfItemContainsFinishedChildren(Item item) {
        boolean hasFinishedChildren = false;
        //only an epic or a story can have children
        if (item.getChildren() != null) {
            for (Item child : item.getChildren()) {
                if (child.getStatus() == ItemStatus.FINISHED.getRepositoryId()) {
                    hasFinishedChildren = true;
                    break;
                }
                if (child.getChildren() != null) {
                    for (Item childOfChild : child.getChildren())
                        if (childOfChild.getStatus() == ItemStatus.FINISHED.getRepositoryId()) {
                            hasFinishedChildren = true;
                            break;
                        }
                }
            }
        }
        return hasFinishedChildren;
    }
}
