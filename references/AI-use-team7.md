# Team 7 AI Usage

ChatGPT was used to create the labelling enhancement using the prompt "Can you make the label render better when scaled and look better"
This can be found in `StationComponent.java`
```
@Override
    public void create() {
        setPlayer(ServiceLocator.getPlayer());
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
        ServiceLocator.getPlayer().getEvents().addListener("interact", this::upgrade);

        // Font setup with mipmapping for smoother scaling
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ithaca.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 28; // base font size
        params.minFilter = Texture.TextureFilter.Linear;  // smooth scaling
        params.magFilter = Texture.TextureFilter.Linear;
        params.borderColor = Color.BLACK;
        params.borderWidth = 2; // gives outline for readability
        params.shadowColor = new Color(0, 0, 0, 0.6f);
        params.shadowOffsetX = 2;
        params.shadowOffsetY = 2;

        BitmapFont font = generator.generateFont(params);
        generator.dispose();

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        // Add a background (9-patch image or simple tinted drawable)
        Texture bgTexture = new Texture(Gdx.files.internal("images/textbg.png"));
        NinePatch ninePatch = new NinePatch(bgTexture, 10, 10, 10, 10);
        labelStyle.background = new NinePatchDrawable(ninePatch);

        buyPrompt = new Label(config.promptText, labelStyle);
        buyPrompt.setVisible(false);
        buyPrompt.setAlignment(Align.center); // center text inside background
        buyPrompt.setSize(500f, 100f);   // set box size
        buyPrompt.setWrap(true); // wrap text if needed
        buyPrompt.setOrigin(Align.center);
        buyPrompt.setPosition(
                ServiceLocator.getRenderService().getStage().getWidth() / 2f,
                ServiceLocator.getRenderService().getStage().getHeight() / 2f, // bottom quarter of screen
                Align.center
        );
        buyPrompt.setTouchable(Touchable.disabled); // don't block clicks
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);
    }
```
