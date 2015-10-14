package io.piotrjastrzebski.playground.ecs.tagtest.systems;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.ecs.tagtest.components.BuildJobComponent;
import io.piotrjastrzebski.playground.ecs.tagtest.components.BuildTagComponent;

/**
 * Created by PiotrJ on 25/06/15.
 */
@Wire public class BuildSystem extends IteratingSystem implements JobHandler {
    @Wire TagSystem tags;
    @Wire JobSystem jobs;
    ComponentMapper<BuildJobComponent> buildM;

    public BuildSystem() {
        super(Aspect.all(BuildJobComponent.class));
    }

    @Override
    protected void initialize() {
        // entities with "tag" will get this component added
        tags.add("build", BuildTagComponent.class);
        // when job with this tag is finished, jobFinished will be called
        jobs.add("build", this);
    }

    @Override
    protected void inserted(int e) {
        Bag<Component> components = world.getEntity(e).getComponents(new Bag<Component>());
        Gdx.app.log("", "Added BuildTag" + e + " " + components.toString());

    }

    @Override
    protected void process(int e) {

    }


    @Override
    protected void removed(int e) {
        Bag<Component> components = world.getEntity(e).getComponents(new Bag<Component>());
        Gdx.app.log("", "Removed BuildTag" + e + " " + components.toString());
    }

    @Override
    public void finishJob(Entity jobEntity) {
        // NOTE entity that contains the job component
        // NOTE do whatever is needed when job is finished
        BuildJobComponent buildJobComponent = buildM.get(jobEntity);
        Gdx.app.log("", "Finish job on " + jobEntity + " " + buildJobComponent);
        jobEntity.deleteFromWorld();
//        jobEntity.edit().remove(BuildJobComponent.class);
    }
}
