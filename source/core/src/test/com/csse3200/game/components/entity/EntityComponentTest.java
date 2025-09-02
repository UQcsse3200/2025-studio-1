package com.csse3200.game.components.entity;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class EntityComponentTest extends GameExtension {
    @Nested
    @DisplayName("Testing Constructors")
    public class ConstructorTest{
        @Test
        public void parameterisedConstrustorTest(){
            EntityComponent entity = new EntityComponent("Gun", 1, "Weapon");

            assertEquals("Gun", ((EntityComponent) entity).getName());
            assertEquals(1, entity.getId());
            assertEquals("Weapon", ((EntityComponent) entity).getType());
            assertFalse("Enemy/Player".equals(entity.getType()), "Gun is supposed to be a weapon");
        }

        @Test
        public void defaultConstructorTest(){
            EntityComponent entity = new EntityComponent();

            assertNull(((EntityComponent) entity).getName(), "Starting value is null");
            assertEquals(0, entity.getId(), "Default value is 0");
            assertNull(((EntityComponent) entity).getType(), "No type assigned at the beginning");
        }
    }

    @Nested
    @DisplayName("Testing getters and setters")

    public class GettersSettersTests{
        private static EntityComponent entity;

        @BeforeEach
        void setup(){
            entity =  new EntityComponent();
        }

        @Test
        public void testNameGetterSetter(){
            entity.setName("Main Player");
            assertEquals("Main Player", entity.getName());
        }

        @Test
        public void testIdGetterSetter(){
            entity.setId(10);
            assertEquals(10, entity.getId());
        }

        @Test
        public void testTypeGetterSetter(){
            entity.setType("Player");
            assertEquals("Player", entity.getType());
        }
    }

    @Nested
    @DisplayName("Testing Edge Cases")

    public class EdgeCasesTest{

        private EntityComponent entity;

        @BeforeEach
        void setup(){
            entity = new EntityComponent();
        }

        @Test
        public void testNullEdgeCase(){
            entity.setName(null);
            assertNull(entity.getName(), "Name is null");

            entity.setType(null);
            assertNull(entity.getType(), "No type assigned at the start");
        }

        @Test
        public void testNegativeEdgeCase(){
            entity.setId(-3);
            assertFalse(entity.getId() > 0, "ID should be a positive integer");
        }

        @Test
        public void testOutOfBoundEdgeCase(){
            entity.setId(Integer.MAX_VALUE);
            assertFalse(entity.getId() < Integer.MAX_VALUE, "ID out of bound. Should be a valid positive integer");
        }
    }

    @Nested
    @DisplayName("Testing : display()")

    public class DisplayTest{
        EntityComponent entity = new EntityComponent("Main Player", 10, "Player");

        @Test
        public void testDisplay(){

            assertEquals("Name : Main Player Id : 10 Type : Player" , ((EntityComponent)entity).display());
        }
    }
}
