package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
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
     * @return: returns an optional that may contain the item the user has requested
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
        Iterable<Item> projectItems = itemRepository.findByProjectId(projectId);
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
                    //if it's not a story (task belongs straight to epic)
                    if (child.getType() != ItemType.STORY.getRepositoryId()) {
                        calculatedEffort += child.getEffort();
                    }
                    //task belongs to a story that belongs to an epic
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
     * @param parent:      the parent (epic/story) of this item
     */
    public void createItem(String title, String description, ItemType type, ItemPriority priority, int effort, Project project, User assignee, User owner, Item parent) {
        if (parent.getId() == 0) {
            parent = null;
        }
        //if its an epic or a story, we want to calculate its children's' effort
        if (type == ItemType.EPIC || type == ItemType.STORY) {
            effort = 0;
        }
        Item item = new Item(title, description, type.getRepositoryId(), priority.getRepositoryId(), effort, project, assignee, owner, parent);
        itemRepository.save(item);
    }

    /**
     * this method updates an existing item and saves it into the repository
     * @param itemId :      id of the item, needed to find it on the repository
     * @param title :       title of the item
     * @param description : description of the item
     * @param type :        ItemType of the item
     * @param priority :    ItemPriority of the item
     * @param effort :      effort required to finish this item
     * @param assignee :    the user that this item is assigned to
     * @param parent :      the parent (epic/story) of this item
     */
    public void updateItem(long itemId, String title, String description, String type, String priority, int effort, User assignee, Item parent) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        System.out.println(itemOptional.isPresent());
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setTitle(title);
            item.setDescription(description);
            item.setType(ItemType.valueOf(type).getRepositoryId());
            item.setPriority(ItemPriority.valueOf(priority).getRepositoryId());
            item.setEffort(effort);
            item.setAssignee(assignee);
            // if item was updated to no parent
            if (parent.getId() == 0) {
                item.setParent(null);
            }
            //all other cases
            else {
                Optional<Item> parentOptional = itemRepository.findById(parent.getId());
                if (parentOptional.isPresent()) {
                    setStatusToItemAndChildren(item, parentOptional.get().getStatus());
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
     * @return: returns an optional that may contain the item requested
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
     * this method updates the status of the item and all its children to a new one, for status information
     * please check the Item Class
     *
     * @param item:   the item (perhaps parent) that the user requested to update the status of
     * @param status: the new status that the user wants this item to have
     */
    public void setStatusToItemAndChildren(Item item, byte status) {
        //if it's a story or an epic, then set the same status for the children
        if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
            item.setStatus(status);
            for (Item child : item.getChildren()) {
                child.setStatus(status);
                //if the parent is an epic and the child a story, then set the same status for the children of the story
                for (Item childOfChild : child.getChildren()) {
                    childOfChild.setStatus(status);
                }
            }
        } else {
            item.setStatus(status);
        }
    }
}
