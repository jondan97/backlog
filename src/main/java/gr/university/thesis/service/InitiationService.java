package gr.university.thesis.service;

import gr.university.thesis.entity.*;
import gr.university.thesis.entity.enumeration.*;
import gr.university.thesis.exceptions.*;
import gr.university.thesis.repository.RoleRepository;
import gr.university.thesis.repository.UserRepository;
import gr.university.thesis.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * service that is used to initialise some needed data when the application starts for the first time
 */
@Service
public class InitiationService {

    //avoiding magic numbers here
    static final String ADMIN_EMAIL = "example@gmail.com";
    static final String ADMIN_PASSWORD = "321";
    static final String ADMIN_FIRST_NAME = "Ioannis";
    static final String ADMIN_SECOND_NAME = "McDaniels";
    UserRepository userRepository;
    RoleRepository roleRepository;
    ProjectService projectService;
    ItemService itemService;
    ItemSprintHistoryService itemSprintHistoryService;
    SprintService sprintService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userRepository:          repository that has access to all the users of the system
     * @param roleRepository           : repository that has access to all the roles of the system
     * @param projectService           : the project service that will be needed in order to create a project
     * @param itemService              : service that will initiate the items needed for the test project
     * @param itemSprintHistoryService : service that will help manage the existing sprint
     * @param sprintService            : service that will manage the initial ready sprint
     */
    @Autowired
    public InitiationService(UserRepository userRepository, RoleRepository roleRepository,
                             ProjectService projectService, ItemService itemService,
                             ItemSprintHistoryService itemSprintHistoryService,
                             SprintService sprintService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectService = projectService;
        this.itemService = itemService;
        this.itemSprintHistoryService = itemSprintHistoryService;
        this.sprintService = sprintService;
    }

    /**
     * initializes some essential data that the system requires in order to work properly
     *
     * @param phase: step that will be taken of initialisation is needed
     * @return: returns true if phase 1 has successfully finished and phase two is needed
     */
    public boolean firstTime(int phase) throws ProjectHasEmptyTitleException, ProjectAlreadyExistsException, ItemAlreadyExistsException, ItemHasEmptyTitleException, SprintHasZeroEffortException {
        if (phase == 1) {
            boolean initiate = createRolesAndUsers();
            if (initiate) {
                createProject();
                createProjectBacklogItems();
                createSprintBacklogItems();
                return true;
            }
            return false;
        } else if (phase == 2) {
            sprintService.startSprint(8, "Make the Customer happy");
            moveItemsRandomlyOnActiveTaskboard();
            moveActiveSprint7DaysBack();
            setRandomDatesForTheItemsInTheActiveSprint();
            createPreviousSprints(5);
            return false;
        }
        return false;
    }

    /**
     * this method moves the active sprint's start date 7 days back
     */
    private void moveActiveSprint7DaysBack() {
        Optional<Sprint> sprintOptional = sprintService.findActiveSprintInProject(new Project(7));
        Sprint sprint = sprintOptional.get();
        sprint.setStart_date(Time.addOrSubtractDaysFromDate(sprint.getStart_date(), -7));
        sprint.setEnd_date(Time.addOrSubtractDaysFromDate(new Date(), 7));
        sprintService.save(sprint);
    }

    /**
     * this method creates a test project in the application for the user to see and interact with
     */
    private void createProject() throws ProjectHasEmptyTitleException, ProjectAlreadyExistsException {
        String title = "Test Project";
        String description = "This is just a test project. With this, " +
                "the user can get accustomed to the application and learn how a Scrum Project " +
                "would look like during its development." +
                "This project was automatically generated.";
        String developersWorkingStr = "7";
        String teamVelocityStr = "30";
        String sprintDurationStr = "2";
        User owner = new User(6);
        projectService.createProject(title, description, developersWorkingStr, teamVelocityStr, sprintDurationStr, owner);
    }

    /**
     * this method creates random epics that contain random stories and places them in the project backlog
     */
    private void createProjectBacklogItems() throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        Project project = new Project(7);
        User owner = new User(6);
        User assignee = new User(0);
        Random random = new Random();
        Item emptyParent = new Item(0);
        String epicDescription = "This Epic is about...";
        String epicAcceptanceCriteria = "This Epic is considered done when...";
        //int randomNum = random.nextInt((max - min) + 1) + min;
        for (int i = 1; i <= 5; i++) {
            String title = "Epic " + i;
            int estimatedEffort = random.nextInt(10 + 1);
            int priority = random.nextInt((4 - 1) + 1) + 1;
            itemService.createItem(title, epicDescription, epicAcceptanceCriteria, ItemType.EPIC,
                    ItemPriority.findItemPriorityByRepositoryId(priority), "0",
                    String.valueOf(estimatedEffort), project, assignee, owner, emptyParent);
        }
        String storyDescription = "This Story is about...";
        String storyAcceptanceCriteria = "This Story is considered done when...";
        for (int i = 6; i <= 18; i++) {
            String title = "Story " + i;
            int randomParentId = random.nextInt((13 - 9) + 1) + 9;
            int estimatedEffort = random.nextInt(10 + 1);
            int priority = random.nextInt((4 - 1) + 1) + 1;
            itemService.createItem(title, storyDescription, storyAcceptanceCriteria, ItemType.STORY,
                    ItemPriority.findItemPriorityByRepositoryId(priority), "0",
                    String.valueOf(estimatedEffort), project, assignee, owner, new Item(randomParentId));
        }
    }

    /**
     * this method creates a set of stories that contains a random number of tasks/bugs and places them in the
     * sprint backlog
     */
    private void createSprintBacklogItems() throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        Project project = new Project(7);
        User owner = new User(6);
        User assignee = new User(0);
        Random random = new Random();
        Item emptyParent = new Item(0);
        String storyDescription = "This Story is about...";
        String storyAcceptanceCriteria = "This Story is considered done when...";
        for (int i = 19; i <= 22; i++) {
            String title = "Story " + i;
            int estimatedEffort = random.nextInt(10 + 1);
            Item story = itemService.createItem(title, storyDescription, storyAcceptanceCriteria, ItemType.STORY,
                    ItemPriority.HIGH, "0",
                    String.valueOf(estimatedEffort), project, assignee, owner, emptyParent);
            itemSprintHistoryService.moveItemToSprint(story, new Sprint(8), null);
        }
        for (int j = 23; j <= 32; j++) {
            int randomType = random.nextInt((4 - 3) + 1) + 3;
            String childTitle = "";
            String childDescription = "";
            String childAcceptanceCriteria = "";
            //we don't want tasks/bugs with 'won't have' priority
            int priority = random.nextInt((4 - 2) + 1) + 2;
            int effort = random.nextInt((10 - 1) + 1) + 1;
            int randomParentId = random.nextInt((30 - 27) + 1) + 27;
            if (randomType == ItemType.TASK.getRepositoryId()) {
                childTitle = "Task " + j;
                childDescription = "This Task is about...";
                ;
                childAcceptanceCriteria = "This Task is considered done when...";

            } else if (randomType == ItemType.BUG.getRepositoryId()) {
                childTitle = "Bug " + j;
                childDescription = "This Bug is about...";
                ;
                childAcceptanceCriteria = "This Bug is considered done when...";
            }
            //not sure why use the 'onTheGo' method but it works
            Item child = itemService.createItemOnTheGo(new Sprint(8), childTitle,
                    childDescription, childAcceptanceCriteria,
                    ItemType.findItemTypeByRepositoryId(randomType),
                    ItemPriority.findItemPriorityByRepositoryId(priority), String.valueOf(effort), project,
                    assignee, owner, new Item(randomParentId));
            itemSprintHistoryService.createAssociationAndSaveToRepository(child, new Sprint(8));
        }
    }

    /**
     * this method takes all the tasks and bugs of the active sprint, and sets a random date from today to 7 days back
     * to each one of them and saves them in the repository
     */
    private void setRandomDatesForTheItemsInTheActiveSprint() {
        Random random = new Random();
        Optional<List<ItemSprintHistory>> allAssociationsBySprintAndTypesOptional = itemSprintHistoryService.findAllAssociationsBySprintAndTypes(new Sprint(8), ItemType.TASK, ItemType.BUG);
        List<ItemSprintHistory> allAssociationsBySprintAndTypes = allAssociationsBySprintAndTypesOptional.get();
        for (ItemSprintHistory ish : allAssociationsBySprintAndTypes) {
            //max is 0 days from today, min is 7 days earlier than today
            int randomDay = random.nextInt(((0) - (-7)) + 1) + (-7);
            ish.setLast_moved(Time.addOrSubtractDaysFromDate(ish.getLast_moved(), randomDay));
        }
        itemSprintHistoryService.saveAllAssociations(allAssociationsBySprintAndTypes);
    }

    /**
     * this method changes the status of the sprint from ready to active and randomly moves the tasks/bugs in the
     * taskboard
     */
    private void moveItemsRandomlyOnActiveTaskboard() throws SprintHasZeroEffortException {
        Random random = new Random();
        for (int j = 31; j <= 40; j++) {
            Item item = new Item(j);
            int numberOfTimesToMove = random.nextInt(3 + 1);
            for (int repeat = 0; repeat <= numberOfTimesToMove; repeat++) {
                itemSprintHistoryService.changeStatusOfAssociationByOne(new Sprint(8), item, 1);
            }
        }
    }

    /**
     * this method creates a set of random previous sprints, each one containing finished tasks and bugs,
     * the number of sprints generated, is determined by the user
     *
     * @param amountOfSprints: the number of previous sprints the user requested to have
     */
    private void createPreviousSprints(int amountOfSprints) {
        //begins from -7 (instead of 0) because active sprint is already active for 1 week
        int startDate = amountOfSprints * (-14);
        startDate -= 7;
        int endDate = startDate + 14;
        Random random = new Random();
        for (int i = 1; i <= amountOfSprints; i++) {
            Sprint sprint = new Sprint();
            sprint.setStart_date(Time.addOrSubtractDaysFromDate(new Date(), startDate));
            sprint.setEnd_date(Time.addOrSubtractDaysFromDate(new Date(), endDate));
            sprint.setStatus((byte) SprintStatus.FINISHED.getRepositoryId());
            sprint.setProject(new Project(7));
            sprint.setDuration(2);
            sprint = sprintService.save(sprint);
            //range: 5-15 tasks
            int numberOfTasks = random.nextInt((15 - 5) + 1) + 5;
            //so that title does not collide with any existing item titles
            int min = (int) (sprint.getId() * 2);
            int max = min + numberOfTasks;
            int teamVelocity = 0;
            for (int j = min; j <= max; j++) {
                int randomType = random.nextInt((4 - 3) + 1) + 3;
                String childTitle = "";
                String childDescription = "";
                String childAcceptanceCriteria = "";
                //we don't want tasks/bugs with 'won't have' priority
                int priority = random.nextInt((4 - 2) + 1) + 2;
                int effort = random.nextInt((10 - 1) + 1) + 1;
                teamVelocity += effort;
                if (randomType == ItemType.TASK.getRepositoryId()) {
                    childTitle = "Task " + j;
                    childDescription = "This Task is about...";
                    ;
                    childAcceptanceCriteria = "This Task is considered done when...";

                } else if (randomType == ItemType.BUG.getRepositoryId()) {
                    childTitle = "Bug " + j;
                    childDescription = "This Bug is about...";
                    ;
                    childAcceptanceCriteria = "This Bug is considered done when...";
                }
                Item sprintItem = new Item(childTitle, childDescription,
                        childAcceptanceCriteria, randomType, priority, effort,
                        0, new Project(7), null, new User(6),
                        null, (byte) ItemStatus.FINISHED.getRepositoryId());
                sprintItem = itemService.saveToRepository(sprintItem);
                ItemSprintHistory itemSprintHistory = new ItemSprintHistory();
                itemSprintHistory.setItem(sprintItem);
                itemSprintHistory.setSprint(sprint);
                int randomDay = random.nextInt((endDate - startDate) + 1) + startDate;
                itemSprintHistory.setLast_moved(Time.addOrSubtractDaysFromDate(new Date(), randomDay));
                //let's suppose everything was done to avoid adding unfinished items back to the backlog
                itemSprintHistory.setStatus(TaskBoardStatus.DONE);
                SprintItemId sid = new SprintItemId();
                sid.setItemId(sprintItem.getId());
                sid.setSprintId(sprint.getId());
                itemSprintHistory.setSprintItemId(sid);
                itemSprintHistoryService.save(itemSprintHistory);
            }
            //if it's the final sprint, then set the team velocity of the project to that
            if (i == amountOfSprints) {
                Optional<Project> projectOptional = projectService.findProjectById(7);
                Project project = projectOptional.get();
                project.setTeam_velocity(teamVelocity);
                projectService.save(project);
            }
            startDate = endDate;
            endDate += 14;
        }
    }

    /**
     * this method creates the roles and the users needed for the application to work, it creates 5 roles:master_admin,
     * admin, scrum_master, product_owner and developer and it also creates an account with the master_admin role so that
     * the user can navigate through the application
     * before doing so, it checks if the account and roles have been created and depending if they exist or not in the
     * repository, it returns a boolean
     *
     * @return: returns true if users have been created in the repository, false if not
     */
    private boolean createRolesAndUsers() {
        if (!userRepository.findFirstByEmail(ADMIN_EMAIL).isPresent()) {
            User user = new User();
            user.setEmail(ADMIN_EMAIL);
            user.setFirstName(ADMIN_FIRST_NAME);
            user.setLastName(ADMIN_SECOND_NAME);
            //encrypt password
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
            String encodedPassword = bCryptPasswordEncoder.encode(ADMIN_PASSWORD);
            user.setPassword(encodedPassword);
            //create role for user
            List<Role> allRoles = new ArrayList<>();
            Role masterRole = new Role();
            masterRole.setId((long) 1);
            masterRole.setRole("MASTER_ADMIN");
            allRoles.add(masterRole);
            Role adminRole = new Role();
            adminRole.setId((long) 2);
            adminRole.setRole("ADMIN");
            allRoles.add(adminRole);
            Role productOwnerRole = new Role();
            productOwnerRole.setId((long) 3);
            productOwnerRole.setRole("PRODUCT_OWNER");
            allRoles.add(productOwnerRole);
            Role userRole = new Role();
            userRole.setId((long) 4);
            userRole.setRole("DEVELOPER");
            allRoles.add(userRole);
            Role scrumMasterRole = new Role();
            scrumMasterRole.setId((long) 5);
            scrumMasterRole.setRole("SCRUM_MASTER");
            allRoles.add(scrumMasterRole);
            roleRepository.saveAll(allRoles);
            user.setRoles(allRoles);
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }
}
