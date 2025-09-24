/**
 * package com.csse3200.game.entities;
 * <p>
 * import com.csse3200.game.components.CombatStatsComponent;
 * import com.csse3200.game.entities.configs.DaggerConfig;
 * import com.csse3200.game.entities.configs.LightsaberConfig;
 * import com.csse3200.game.entities.factories.WeaponsFactory;
 * import org.junit.Before;
 * import org.junit.Test;
 * <p>
 * public class WeaponsFactoryTest {
 * private LightsaberConfig lightsaberConfigs;
 * private DaggerConfig daggerConfigs;
 *
 * @Before public void readConfigs() {
 * this.lightsaberConfigs = new LightsaberConfig();
 * this.daggerConfigs = new DaggerConfig();
 * }
 * @Test public void createLightsaberTest() {
 * Entity lightSaber = WeaponsFactory.createLightsaber();
 * <p>
 * int baseAttack = lightSaber.
 * getComponent(CombatStatsComponent.class).
 * getBaseAttack();
 * int health = lightSaber.getComponent(CombatStatsComponent.class).getHealth();
 * <p>
 * assert(baseAttack == lightsaberConfigs.baseAttack);
 * assert(0 == health);
 * }
 * @Test public void createPistolTest() {
 * Entity pistol = WeaponsFactory.createPistol();
 * <p>
 * int baseAttack = pistol.
 * getComponent(CombatStatsComponent.class).
 * getBaseAttack();
 * int health = pistol.getComponent(CombatStatsComponent.class).getHealth();
 * <p>
 * // pistol is currently using lightsaber configs
 * assert(baseAttack == lightsaberConfigs.baseAttack);
 * assert(0 == health);
 * }
 * @Test public void createDaggerTest() {
 * Entity dagger = WeaponsFactory.createDagger();
 * <p>
 * int baseAttack = dagger.
 * getComponent(CombatStatsComponent.class).
 * getBaseAttack();
 * int health = dagger.getComponent(CombatStatsComponent.class).getHealth();
 * <p>
 * assert(baseAttack == daggerConfigs.baseAttack);
 * assert(0 == health);
 * }
 * }
 **/
