package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
