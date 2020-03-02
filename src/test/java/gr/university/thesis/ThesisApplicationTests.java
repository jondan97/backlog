package gr.university.thesis;

import gr.university.thesis.entity.Comment;
import gr.university.thesis.entity.Item;
import gr.university.thesis.service.ItemService;
import gr.university.thesis.service.ProjectService;
import gr.university.thesis.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.Assert.assertTrue;


@SpringBootTest
class ThesisApplicationTests {

    private ProjectService projectService;
    private SessionService sessionService;
    private ItemService itemService;

    @Autowired
    public ThesisApplicationTests(ProjectService projectService, SessionService sessionService, ItemService itemService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.itemService = itemService;
    }

    @Test
    void contextLoads() {
        assertTrue(itemService.findItemInProject(9, 7).isPresent());
    }

    @Test
    void stackOverFlowWithEntitiesTest() {
        Optional<Item> itemOptional = itemService.findItemInProject(9, 7);
        for (Comment comment : itemOptional.get().getComments()) {
            System.out.println(comment.getBody());
        }
    }

}
