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
     * this method calls the repository in order to find all the items that belong to a certain project  by using the
     * project id
     *
     * @param projectId: id of the project
     * @return returns all the items that are associated with a project
     */
    public Iterable<Item> findAllItemsByProjectId(long projectId) {
        return itemRepository.findByProjectId(projectId);
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

    /**
     * this method updates an existing item and saves it into the repository
     *
     * @param itemId:      id of the item, needed to find it on the repository
     * @param title:       title of the item
     * @param description: description of the item
     * @param type:        ItemType of the item
     * @param priority:    ItemPriority of the item
     * @param effort:      effort required to finish this item
     * @param assigneeId:  the user that thisitem is assigned to
     */
    public void updateItem(long itemId, String title, String description, String type, String priority, int effort, long assigneeId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        System.out.println(itemOptional.isPresent());
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setTitle(title);
            item.setDescription(description);
            item.setType(ItemType.valueOf(type).getRepositoryId());
            item.setPriority(ItemPriority.valueOf(priority).getRepositoryId());
            item.setEffort(effort);
            item.setAssignee(new User(assigneeId));
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

    public Optional<Item> findItemInProject(long itemId, long projectId) {
        return itemRepository.findDistinctItemByProjectId(itemId, projectId);
    }

    public void updateAssignee(long itemId, User assignee) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setAssignee(assignee);
            itemRepository.save(item);
        }
    }
}
