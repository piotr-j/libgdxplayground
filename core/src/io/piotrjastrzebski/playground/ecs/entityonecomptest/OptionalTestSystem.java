package io.piotrjastrzebski.playground.ecs.entityonecomptest;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;

/**
 * Created by PiotrJ on 10/07/15.
 */
@Wire
public class OptionalTestSystem extends EntityProcessingSystem {
    ComponentMapper<MandatoryComponent> mandatoryCM;
    ComponentMapper<OptionalComponentA> optionalACM;
    ComponentMapper<OptionalComponentB> optionalBCM;


    public OptionalTestSystem() {
        super(Aspect.all(MandatoryComponent.class).one(OptionalComponentA.class, OptionalComponentB.class));
    }

    Bag<Component> fill = new Bag<>();
    @Override
    protected void inserted(Entity e) {
        fill.clear();
        e.getComponents(fill);
        Gdx.app.log("OptionalTestSystem", "Inserted " + e + " " + fill);
    }

    @Override
    protected void process(Entity e) {
        fill.clear();
        e.getComponents(fill);
        Gdx.app.log("OptionalTestSystem", "Process " + e + " " + fill);
    }

    @Override
    protected void removed(Entity e) {
        fill.clear();
        e.getComponents(fill);
        Gdx.app.log("OptionalTestSystem", "Removed " + e + " " + fill);
    }
}
