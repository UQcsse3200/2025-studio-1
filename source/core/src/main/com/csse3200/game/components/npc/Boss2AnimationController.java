package com.csse3200.game.components.npc;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.events.listeners.EventListener0;

public class Boss2AnimationController extends Component {
    public static class Names {
        public String idle     = "idle";
        public String patrol   = "patrol";
        public String prep     = "prep";
        public String charge   = "charge";
        public String ret      = "return";   // back-to-line
        public String cooldown = "cooldown";
        public String death    = "death";
        public String hurt     = "hurt";
    }

    public static class Speeds {
        public float idle = 0.10f;
        public float patrol = 0.10f;
        public float prep = 0.10f;
        public float charge = 0.08f;
        public float ret = 0.10f;
        public float cooldown = 0.10f;
        public float death = 0.06f;    // usually faster/one-shot
        public float hurt = 0.06f;
    }

    private final Names names;
    private final Speeds speeds;
    private final Animation.PlayMode loopMode;
    private AnimationRenderComponent arc;

    /** Use default names (idle/patrol/prep/charge/return/cooldown/death/hurt) and default speeds. */
    public Boss2AnimationController() {
        this(new Names(), new Speeds(), Animation.PlayMode.LOOP);
    }

    /** Fully custom names & speeds. Set any name to null to disable that animation. */
    public Boss2AnimationController(Names names, Speeds speeds, Animation.PlayMode loopMode) {
        this.names = names != null ? names : new Names();
        this.speeds = speeds != null ? speeds : new Speeds();
        this.loopMode = loopMode != null ? loopMode : Animation.PlayMode.LOOP;
    }

    public Boss2AnimationController(String baseName, float frameDur, Animation.PlayMode mode) {
        Names n = new Names();
        n.idle = n.patrol = n.prep = n.charge = n.ret = n.cooldown = n.death = n.hurt = baseName;
        Speeds sp = new Speeds();
        sp.idle = sp.patrol = sp.prep = sp.charge = sp.ret = sp.cooldown = sp.death = sp.hurt = frameDur;
        this.names = n;
        this.speeds = sp;
        this.loopMode = (mode != null) ? mode : Animation.PlayMode.LOOP;
    }

    @Override
    public void create() {
        arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc == null) {
            throw new IllegalStateException("Boss2AnimationController requires AnimationRenderComponent.");
        }

        // Register animations declared in atlas by base name
        addIfNotNull(names.idle,     speeds.idle,     Animation.PlayMode.LOOP);
        addIfNotNull(names.patrol,   speeds.patrol,   Animation.PlayMode.LOOP);
        addIfNotNull(names.prep,     speeds.prep,     loopMode);
        addIfNotNull(names.charge,   speeds.charge,   loopMode);
        addIfNotNull(names.ret,      speeds.ret,      loopMode);
        addIfNotNull(names.cooldown, speeds.cooldown, loopMode);
        addIfNotNull(names.hurt,     speeds.hurt,     Animation.PlayMode.NORMAL);
        addIfNotNull(names.death,    speeds.death,    Animation.PlayMode.NORMAL);

        // Event wiring (two naming styles supported)
        on("boss2:idle", () -> play(names.idle));
        on("boss2:patrol", () -> play(names.patrol));
        on("boss2:prep", () -> play(names.prep));
        on("boss2:charge", () -> play(names.charge));
        on("boss2:return", () -> play(names.ret));
        on("boss2:cooldown", () -> play(names.cooldown));
        on("boss2:hurt", () -> play(names.hurt));
        on("boss2:death", () -> play(names.death));

        on("boss2Idle", () -> play(names.idle));
        on("boss2Patrol", () -> play(names.patrol));
        on("boss2Prep", () -> play(names.prep));
        on("boss2Charge", () -> play(names.charge));
        on("boss2Return", () -> play(names.ret));
        on("boss2Cooldown", () -> play(names.cooldown));
        on("boss2Hurt", () -> play(names.hurt));
        on("boss2Death", () -> play(names.death));

        // Start with idle if available
        play(names.idle);
    }

    private void addIfNotNull(String name, float frameDur, Animation.PlayMode mode) {
        if (name == null) return;
        // Safe to call multiple times; AnimationRenderComponent keeps a map by name
        arc.addAnimation(name, frameDur, mode);
    }

    private void on(String evt, EventListener0 l) {
        entity.getEvents().addListener(evt, l);
    }
    private void play(String name) {
        if (arc == null || name == null) return;
        // Only start if this animation was registered
        if (arc.hasAnimation(name)) {
            arc.startAnimation(name);
        }
    }
}
