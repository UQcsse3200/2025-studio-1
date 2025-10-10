package com.csse3200.game.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class BaseScreenTest {

    static Stream<Arguments> ctorParams() {
        return Stream.of(
                // backgrounds = null → treated as empty
                arguments(null, false, true, false),
                // empty list
                arguments(List.of(), false, true, false),
                // single bg: asset present, stage already exists
                arguments(List.of("bg.png"), true, true, false),
                // single bg: asset missing, stage already exists
                arguments(List.of("bg.png"), false, true, false),
                // multiple bgs: only first used; force ensureStage() creation
                arguments(List.of("a.png", "b.png"), true, false, false),
                // single bg, ensureStage() creation + setStage throws (fallback path)
                arguments(List.of("bg.png"), true, false, true)
        );
    }

    @ParameterizedTest(name = "[{index}] bgs={0} assetPresent={1} hasStage={2} setStageThrows={3}")
    @MethodSource("ctorParams")
    void ctor_parameterised_background_and_stage_paths(List<String> bgs,
                                                       boolean assetPresent,
                                                       boolean renderHasStage,
                                                       boolean setStageThrows) {
        // renderer & camera chain (for camera position verify)
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        Entity camEntity = mock(Entity.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(camEntity);

        // services
        RenderService renderService = mock(RenderService.class);
        ResourceService resourceService = mock(ResourceService.class);
        EntityService entityService = mock(EntityService.class);

        // stage setup
        Stage existingStage = mock(Stage.class);
        if (renderHasStage) {
            when(renderService.getStage()).thenReturn(existingStage);
        } else {
            when(renderService.getStage()).thenReturn(null);
            if (setStageThrows) {
                doThrow(new RuntimeException("setStage boom")).when(renderService).setStage(any(Stage.class));
            }
        }

        // backgrounds & assets
        final boolean hasBg = bgs != null && !bgs.isEmpty();
        final String[] bgArray = (bgs == null) ? null : bgs.toArray(new String[0]);
        final String firstBg = hasBg ? bgs.get(0) : null;
        if (hasBg) {
            when(resourceService.getAsset(eq(firstBg), eq(Texture.class)))
                    .thenReturn(assetPresent ? mock(Texture.class) : null);
        }

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class);
             // intercept any new Stage(...) in ensureStage()
             MockedConstruction<Stage> stageCtor = Mockito.mockConstruction(Stage.class)) {

            // static stubs
            rf.when(RenderFactory::createRenderer).thenReturn(renderer);
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            // swallow registrations/clear
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // act
            BaseScreen screen = new TestScreen(mock(GdxGame.class), bgArray);

            // camera positioned
            verify(camEntity).setPosition(5f, 5f);

            // stage binding / getStage() coverage
            if (renderHasStage) {
                assertSame(existingStage, TestScreen.STAGE.get());
                assertSame(existingStage, screen.getStage());
                verify(renderService, never()).setStage(any());
                assertEquals(0, stageCtor.constructed().size());
            } else {
                assertEquals(1, stageCtor.constructed().size(), "should construct one Stage");
                Stage constructed = stageCtor.constructed().get(0);
                verify(renderService).setStage(constructed); // attempted (may throw)
                assertSame(constructed, TestScreen.STAGE.get());
                assertSame(constructed, screen.getStage());
            }

            // background load + actor addition / omission
            if (hasBg) {
                verify(resourceService).loadTextures(argThat(arr -> arr.length == bgs.size()));
                verify(resourceService).loadAll();
                if (assetPresent) {
                    if (renderHasStage) {
                        verify(existingStage).addActor(any());
                    } else {
                        Stage constructed = stageCtor.constructed().get(0);
                        verify(constructed).addActor(any());
                    }
                } else {
                    // no actor if missing texture
                    verify(existingStage, never()).addActor(any());
                    if (!renderHasStage && !stageCtor.constructed().isEmpty()) {
                        verify(stageCtor.constructed().get(0), never()).addActor(any());
                    }
                }
            } else {
                verify(resourceService, never()).loadTextures(any(String[].class));
                verify(resourceService, never()).loadAll();
            }

            // also exercise render/resize/dispose for coverage coherence
            screen.render(0.016f);
            verify(entityService, times(1)).update();
            verify(renderer, times(1)).render();

            screen.resize(800, 600);
            verify(renderer).resize(800, 600);

            screen.dispose();
            verify(renderer).dispose();
            verify(renderService).dispose();
            if (hasBg) {
                verify(resourceService).unloadAssets(argThat(arr -> arr.length == bgs.size()));
            } else {
                verify(resourceService, never()).unloadAssets(any(String[].class));
            }
        }
    }

    @Test
    void ctor_throws_on_null_game() {
        assertThrows(NullPointerException.class, () -> new TestScreen(null));
    }

    @Test
    void ctor_noBackground_usesExistingStage_callsCreateUI_and_positionsCamera_and_covers_render_resize_dispose() {
        Graph g = new Graph();

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(g.renderer);

            // Wire ServiceLocator getters to our fakes
            svc.when(ServiceLocator::getRenderService).thenReturn(g.renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(g.resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(g.entityService);

            // Let all registrations and clear be no-ops
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            BaseScreen screen = new TestScreen(mock(GdxGame.class)); // no background

            // ctor paths
            assertTrue(TestScreen.CALLED.get());
            assertSame(g.stage, TestScreen.STAGE.get());
            assertNotNull(TestScreen.UI.get());
            verify(g.cameraEntity).setPosition(5f, 5f);

            // render path
            screen.render(0.016f);
            verify(g.entityService, times(1)).update();
            verify(g.renderer, times(1)).render();

            // resize path
            screen.resize(123, 456);
            verify(g.renderer).resize(123, 456);

            // dispose path (no exceptions)
            screen.dispose();
            verify(g.renderer).dispose();
            verify(g.renderService).dispose();
            svc.verify(ServiceLocator::clearExceptPlayer);
        }
    }

    @Test
    void ctor_withBackground_and_texturePresent_addsImageActor_and_loadsAssets() {
        Graph g = new Graph();
        Texture tex = mock(Texture.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(g.renderer);

            svc.when(ServiceLocator::getRenderService).thenReturn(g.renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(g.resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(g.entityService);

            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // loadAssets() then createUI(): getAsset must return a texture
            when(g.resourceService.getAsset("bg.png", Texture.class)).thenReturn(tex);

            BaseScreen screen = new TestScreen(mock(GdxGame.class), "bg.png");

            // loadAssets() happened
            verify(g.resourceService).loadTextures(argThat(arr -> arr.length == 1 && "bg.png".equals(arr[0])));
            verify(g.resourceService).loadAll();

            // createUI() added an Image actor
            ArgumentCaptor<Actor> actorCap = ArgumentCaptor.forClass(Actor.class);
            verify(g.stage).addActor(actorCap.capture());
            assertInstanceOf(Image.class, actorCap.getValue());

            // dispose should unload assets
            screen.dispose();
            verify(g.resourceService).unloadAssets(argThat(arr -> arr.length == 1 && "bg.png".equals(arr[0])));
        }
    }

    @Test
    void ctor_withBackground_and_missingTexture_doesNotAddActor_and_stillLoadsAssets() {
        Graph g = new Graph();

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(g.renderer);

            svc.when(ServiceLocator::getRenderService).thenReturn(g.renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(g.resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(g.entityService);

            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Missing asset -> getAsset returns null to hit the warn branch
            when(g.resourceService.getAsset("bg.png", Texture.class)).thenReturn(null);

            BaseScreen screen = new TestScreen(mock(GdxGame.class), "bg.png");

            verify(g.resourceService).loadTextures(any(String[].class));
            verify(g.resourceService).loadAll();
            verify(g.stage, never()).addActor(any());

            // Still should unload assets on dispose
            screen.dispose();
            verify(g.resourceService).unloadAssets(any(String[].class));
        }
    }

    @Test
    void ensureStage_createsAndBindsWhenRenderServiceStageIsNull_and_handles_setStage() {
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());

        RenderService renderService = mock(RenderService.class);
        ResourceService res = mock(ResourceService.class);
        EntityService ent = mock(EntityService.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class);
             // 🔑 Intercept ALL `new Stage(...)` calls and return a mock instead of running the ctor
             MockedConstruction<Stage> stageCtor = Mockito.mockConstruction(Stage.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            // RenderService has no stage -> triggers ensureStage() creation branch
            when(renderService.getStage()).thenReturn(null);

            // Route ServiceLocator calls
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(res);
            svc.when(ServiceLocator::getEntityService).thenReturn(ent);
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Act: constructing BaseScreen calls ensureStage() -> new Stage(...) (mocked) -> setStage(...)
            BaseScreen screen = new TestScreen(mock(GdxGame.class));

            // Assert: a Stage was "constructed" (mocked), bound back to RenderService, and passed to UI
            assertEquals(1, stageCtor.constructed().size(), "Stage should be constructed once");
            Stage constructedStage = stageCtor.constructed().getFirst();
            verify(renderService).setStage(constructedStage);
            assertSame(constructedStage, TestScreen.STAGE.get());

            screen.dispose();
        }
    }


    @Test
    void dispose_catchesExceptions_from_renderer_renderService_entityService() {
        Graph g = new Graph();

        // Throw from each dispose path to exercise catch blocks
        doThrow(new RuntimeException("renderer fail")).when(g.renderer).dispose();
        doThrow(new RuntimeException("renderService fail")).when(g.renderService).dispose();
        doThrow(new RuntimeException("entityService fail")).when(g.entityService).disposeExceptPlayer();

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(g.renderer);

            svc.when(ServiceLocator::getRenderService).thenReturn(g.renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(g.resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(g.entityService);

            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            BaseScreen screen = new TestScreen(mock(GdxGame.class), "bg.png");
            // dispose should swallow exceptions and still clear
            assertDoesNotThrow(screen::dispose);
            svc.verify(ServiceLocator::clearExceptPlayer);
            // unloadAssets was still attempted
            verify(g.resourceService).unloadAssets(any(String[].class));
        }
    }

    @Test
    void ensureStage_handles_setStage_exception_and_uses_local_fallback_stage() {
        // --- Renderer & camera so ctor can run its camera-position side effect
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());

        // --- Services: no stage initially, setStage will throw to hit the catch block
        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(null);
        doThrow(new RuntimeException("setStage boom")).when(renderService).setStage(any(Stage.class));

        ResourceService resourceService = mock(ResourceService.class);
        EntityService entityService = mock(EntityService.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class);
             // Intercept real Stage creation to avoid SpriteBatch/GL
             MockedConstruction<Stage> stageCtor = Mockito.mockConstruction(Stage.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            // Static wiring
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Act: ctor -> ensureStage() -> new Stage(...) (mocked) -> setStage(throws) -> fallback
            BaseScreen screen = new TestScreen(mock(GdxGame.class));

            // Assert: one Stage constructed; setStage was attempted (and threw); fallback stage is used
            assertEquals(1, stageCtor.constructed().size(), "Stage should be constructed once");
            Stage constructed = stageCtor.constructed().get(0);
            verify(renderService).setStage(constructed);          // attempted bind
            assertSame(constructed, TestScreen.STAGE.get(), "UI should receive the fallback Stage");
            assertSame(constructed, screen.getStage(), "getStage() should expose the fallback Stage");

            screen.dispose();
        }
    }

    @Test
    void ensureStage_usesExistingStage_and_getStage_returns_same_instance() {
        // --- Mocks ---
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());

        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class); // existing stage provided by RenderService
        when(renderService.getStage()).thenReturn(stage);

        ResourceService resourceService = mock(ResourceService.class);
        EntityService entityService = mock(EntityService.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            // ServiceLocator wiring
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            // swallow void static registrations/clear
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Act: ctor triggers ensureStage(); since getStage()!=null, it should reuse it
            BaseScreen screen = new TestScreen(mock(GdxGame.class));

            // Assert: ensureStage reused the RenderService stage; getStage() exposes same instance
            assertSame(stage, TestScreen.STAGE.get(), "Stage passed to createUIScreen must be RenderService stage");
            assertSame(stage, screen.getStage(), "getStage() must return the same instance");
            verify(renderService, never()).setStage(any()); // not called when stage already present

            screen.dispose();
        }
    }

    @Test
    void ensureStage_creates_new_stage_when_absent_binds_it_back_and_getStage_exposes_it() {
        // --- Mocks ---
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());

        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(null); // force creation branch

        ResourceService resourceService = mock(ResourceService.class);
        EntityService entityService = mock(EntityService.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class);
             // Intercept Stage construction so no real SpriteBatch/GL is created
             MockedConstruction<Stage> stageCtor = Mockito.mockConstruction(Stage.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            // ServiceLocator wiring
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);
            // swallow void static registrations/clear
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Act: ctor → ensureStage() → new Stage(...) (mocked) → renderService.setStage(newStage)
            BaseScreen screen = new TestScreen(mock(GdxGame.class));

            // Assert: constructed exactly one Stage, bound and visible via getStage()
            assertEquals(1, stageCtor.constructed().size(), "Stage should be constructed once");
            Stage constructed = stageCtor.constructed().get(0);
            verify(renderService).setStage(constructed);
            assertSame(constructed, TestScreen.STAGE.get(), "Stage passed to createUIScreen must be the constructed stage");
            assertSame(constructed, screen.getStage(), "getStage() must return the constructed stage");

            screen.dispose();
        }
    }

    @Test
    void resize_forwards_to_renderer() {
        // Arrange a safe graph
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());

        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(mock(Stage.class)); // non-null Stage
        ResourceService resourceService = mock(ResourceService.class);
        EntityService entityService = mock(EntityService.class);

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);

            // swallow registrations/clears
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            BaseScreen screen = new TestScreen(mock(GdxGame.class)); // no background

            // Act
            screen.resize(123, 456);

            // Assert
            verify(renderer).resize(123, 456);

            // Cleanup to avoid leaks
            screen.dispose();
        }
    }

    @Test
    void dispose_forwards_and_catches_exceptions_and_clears_globals() {
        // Arrange a graph that will throw inside dispose() to hit the catch blocks
        Renderer renderer = mock(Renderer.class);
        CameraComponent camera = mock(CameraComponent.class);
        when(renderer.getCamera()).thenReturn(camera);
        when(camera.getEntity()).thenReturn(new Entity());
        doThrow(new RuntimeException("renderer dispose boom")).when(renderer).dispose();

        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(mock(Stage.class));
        doThrow(new RuntimeException("renderService dispose boom")).when(renderService).dispose();

        ResourceService resourceService = mock(ResourceService.class);

        EntityService entityService = mock(EntityService.class);
        doThrow(new RuntimeException("entityService dispose boom"))
                .when(entityService).disposeExceptPlayer();

        try (MockedStatic<RenderFactory> rf = Mockito.mockStatic(RenderFactory.class);
             MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {

            rf.when(RenderFactory::createRenderer).thenReturn(renderer);

            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);
            svc.when(ServiceLocator::getResourceService).thenReturn(resourceService);
            svc.when(ServiceLocator::getEntityService).thenReturn(entityService);

            // swallow registrations/clears
            svc.when(() -> ServiceLocator.registerInputService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerResourceService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerEntityService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerRenderService(any())).then(inv -> null);
            svc.when(() -> ServiceLocator.registerTimeSource(any())).then(inv -> null);
            svc.when(ServiceLocator::clearExceptPlayer).then(inv -> null);

            // Use a background so unloadAssets() runs in dispose()
            BaseScreen screen = new TestScreen(mock(GdxGame.class), "bg.png");

            // Act + Assert: dispose must not throw even though internals fail
            assertDoesNotThrow(screen::dispose);

            // Verify each path was attempted
            verify(renderer).dispose();
            verify(renderService).dispose();
            verify(entityService).disposeExceptPlayer();
            svc.verify(ServiceLocator::clearExceptPlayer);

            // Because we had a background, unloadAssets() should be invoked
            verify(resourceService).unloadAssets(argThat(arr -> arr.length == 1 && "bg.png".equals(arr[0])));
        }
    }

    /**
     * Minimal concrete screen that records the Stage and returned UI.
     */
    static class TestScreen extends BaseScreen {
        static final AtomicBoolean CALLED = new AtomicBoolean(false);
        static final AtomicReference<Stage> STAGE = new AtomicReference<>();
        static final AtomicReference<Entity> UI = new AtomicReference<>();

        TestScreen(GdxGame game, String... bg) {
            super(game, bg);
        }

        @Override
        protected Entity createUIScreen(Stage stage) {
            CALLED.set(true);
            STAGE.set(stage);
            Entity ui = new Entity();
            UI.set(ui);
            return ui;
        }
    }

    /**
     * Helpers to build a common stub graph for most tests.
     */
    private static class Graph {
        final Renderer renderer = mock(Renderer.class);
        final CameraComponent camera = mock(CameraComponent.class);
        final Entity cameraEntity = mock(Entity.class);
        final RenderService renderService = mock(RenderService.class);
        final ResourceService resourceService = mock(ResourceService.class);
        final EntityService entityService = mock(EntityService.class);
        final Stage stage = mock(Stage.class);

        Graph() {
            when(renderer.getCamera()).thenReturn(camera);
            when(camera.getEntity()).thenReturn(cameraEntity);
            when(renderService.getStage()).thenReturn(stage);
        }
    }
}

