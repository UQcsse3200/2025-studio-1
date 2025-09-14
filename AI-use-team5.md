Charmaine Chan:
I created a junit test for tutorial screen:
@ExtendWith(GameExtension.class)
public class TutorialScreenDisplayTest {
private GdxGame mockGame;
private TutorialScreenDisplay display;
private Stage stage;

    @BeforeEach
    void createConditions() {
        // Register ResourceService with test skins
        ServiceLocator.registerResourceService(new ResourceService());
        //String[] skinPaths = {"flat-earth/skin/flat-earth-ui.json"}; // replace with your actual skin file(s)
        ServiceLocator.getResourceService().loadAll();

        // Mock stage for rendering
        stage = mock(Stage.class);
        RenderService renderService = new RenderService();
        renderService.setStage(stage);
        ServiceLocator.registerRenderService(renderService);

        // Other required services
        ServiceLocator.registerInputService(new InputService());

        mockGame = mock(GdxGame.class);

        TutorialStep step1 = new TutorialStep("Step 1", "First step", null);
        TutorialStep step2 = new TutorialStep("Step 2", "Second step", null);

        display = new TutorialScreenDisplay(mockGame, List.of(step1, step2));
        display.create();  // now works without crashing
    }

    @Test
    void startsAtFirstStep() {
        Assertions.assertEquals(0, display.currentStep);
    }


    @Test
    void mainMenuButtonSwitchesScreen() {
        display.backMainMenu();
        verify(mockGame).setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

How do I check if the 'next' and 'previous' button is created correctly?

ChatGPT:
You want a s test function that checks for the existence of the previous and next buttons within the nested table. Here’s a complete example:
@Test
void testPrevNextButtonsRenderedCorrectly() {
// Capture the table added to the stage
ArgumentCaptor<Table> tableCaptor = ArgumentCaptor.forClass(Table.class);
verify(stage, atLeastOnce()).addActor(tableCaptor.capture());
Table rootTable = tableCaptor.getValue();
assertNotNull(rootTable, "Root table should be created");

    boolean hasPrevButton = false;
    boolean hasNextButton = false;

    for (Actor actor : rootTable.getChildren()) {
        if (actor instanceof Table) { // check nested table for next/prev buttons
            Table animTable = (Table) actor;
            for (Actor nestedActor : animTable.getChildren()) {
                if (nestedActor instanceof ImageButton) {
                    // In this test, we assume the first is prev and the last is next
                    int index = animTable.getChildren().indexOf(nestedActor, true);
                    if (index == 0) hasPrevButton = true;
                    if (index == animTable.getChildren().size - 1) hasNextButton = true;
                }
            }
        }
    }

    assertTrue(hasPrevButton, "Previous button should exist in the nested table");
    assertTrue(hasNextButton, "Next button should exist in the nested table");
}