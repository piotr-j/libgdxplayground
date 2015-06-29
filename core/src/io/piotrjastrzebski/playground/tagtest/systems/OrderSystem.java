package io.piotrjastrzebski.playground.tagtest.systems;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.tagtest.components.BuildJobComponent;
import io.piotrjastrzebski.playground.tagtest.components.MineJobComponent;
import io.piotrjastrzebski.playground.tagtest.components.OrderTagComponent;

/**
 * Created by PiotrJ on 25/06/15.
 */
@Wire public class OrderSystem extends EntityProcessingSystem implements JobHandler {
    @Wire
    TagSystem tags;
    @Wire JobSystem jobs;

    ComponentMapper<MineJobComponent> mineM;

    public OrderSystem() {
        super(Aspect.getAspectForAll(MineJobComponent.class));
    }

    @Override
    protected void initialize() {
        // entities with "tag" will get this component added
        tags.add("order", OrderTagComponent.class);

        jobs.add("mine", this);
    }

    @Override
    protected void inserted(Entity e) {
        Bag<Component> components = e.getComponents(new Bag<Component>());
        Gdx.app.log("", "Added OrderTag" + e + " " + components.toString());

    }

    @Override
    protected void process(Entity e) {

    }

    @Override
    protected void removed(Entity e) {
        Bag<Component> components = e.getComponents(new Bag<Component>());
        Gdx.app.log("", "Removed OrderTag" + e + " " + components.toString());
    }


    @Override
    public void finishJob(Entity jobEntity) {
        // NOTE entity that contains the job component
        // NOTE do whatever is needed when job is finished
        MineJobComponent mineJobComponent = mineM.get(jobEntity);
        Gdx.app.log("","Finish job on " + jobEntity + " " +mineJobComponent);
        jobEntity.deleteFromWorld();
    }
}
