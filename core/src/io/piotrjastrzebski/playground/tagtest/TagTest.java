package io.piotrjastrzebski.playground.tagtest;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.managers.UuidEntityManager;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.tagtest.components.*;
import io.piotrjastrzebski.playground.tagtest.systems.BuildSystem;
import io.piotrjastrzebski.playground.tagtest.systems.JobSystem;
import io.piotrjastrzebski.playground.tagtest.systems.OrderSystem;
import io.piotrjastrzebski.playground.tagtest.systems.TagSystem;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class TagTest extends BaseScreen {
	World world;
	public TagTest (PlaygroundGame game) {
		super(game);
		WorldConfiguration cfg = new WorldConfiguration();

		TagSystem tagSystem;
		cfg.setSystem(tagSystem = new TagSystem(), true);
		cfg.setManager(new UuidEntityManager());
		cfg.setSystem(new BuildSystem());
		cfg.setSystem(new OrderSystem());
		JobSystem jobSystem;
		cfg.setSystem(jobSystem = new JobSystem());

		world = new World(cfg);

//		world.createEntity().edit().create(JobComponent.class);
//
//		Entity eb1 = world.createEntity();
//		eb1.edit().create(TagComponent.class).add("build");
//
//		Entity eb2 = world.createEntity();
//		eb2.edit().create(TagComponent.class).add("build").add("order");
//
//		Entity eb3 = world.createEntity();
//		eb3.edit().create(TagComponent.class).add("build").add("build");
//
//		Entity eb4 = world.createEntity();
//		eb4.edit().create(TagComponent.class).add("order");

//		Entity e2 = world.createEntity();
//		e2.edit().create(TagComponent.class).add("welp");
//		Entity e3 = world.createEntity();
//		e3.edit().create(TagComponent.class).add("order");
//		Entity e4 = world.createEntity();
//		e4.edit().create(TagComponent.class).add("order");
//		Entity e5 = world.createEntity();
//		e5.edit().create(TagComponent.class).add("order");
//		world.process();
//		Gdx.app.log("", "added " + world.getEntityManager().getTotalAdded());
//
//		Gdx.app.log("", "Init");
//		findTagged("build", tagSystem);
//		findTagged("order", tagSystem);
//		eb1.deleteFromWorld();
//		world.process();
//		Gdx.app.log("", "Removed 0");
//		findTagged("build", tagSystem);
//		findTagged("order", tagSystem);
//
//		tagSystem.remove("build", eb2);
//		world.process();
//		Gdx.app.log("", "Removed tag");
//		findTagged("build", tagSystem);
//		findTagged("order", tagSystem);
//		findTagged("welp", tagSystem);

		// NOTE tags work nicely
		// TODO figure out how do we handle subclasses of the components when we want all of base class type
		// ie we want all JobComponent entities
		// where we have stuff like BuildJobComponent and MineJobComponent that subclass job but dont have separate
		// JobComponent, maybe we enforce them all to have it, and point in the base component to subclassed ot something


		Entity worker1 = world.createEntity();
		worker1.edit().create(WorkerComponent.class).jobPref.addAll("mine", "build");

		Entity worker2 = world.createEntity();
		worker2.edit().create(WorkerComponent.class).jobPref.addAll("build");

		Entity worker3 = world.createEntity();
		worker3.edit().create(WorkerComponent.class).jobPref.addAll("mine");


		Entity buildJob1 = world.createEntity();
		BuildJobComponent buildJob1Component = buildJob1.edit().create(BuildJobComponent.class);
		JobDelegateComponent job1Component = buildJob1.edit().create(JobDelegateComponent.class);
		job1Component.setActualClass(BuildJobComponent.class);

		Entity buildJob2 = world.createEntity();
		BuildJobComponent buildJob2Component = buildJob2.edit().create(BuildJobComponent.class);
		JobDelegateComponent job2Component = buildJob2.edit().create(JobDelegateComponent.class);
		job2Component.setActualClass(BuildJobComponent.class);
//
		Entity mineJob1 = world.createEntity();
		MineJobComponent mineJob1Component = mineJob1.edit().create(MineJobComponent.class);
		JobDelegateComponent job3Component = mineJob1.edit().create(JobDelegateComponent.class);
		job3Component.setActualClass(MineJobComponent.class);
//
		Entity mineJob2 = world.createEntity();
		MineJobComponent mineJob2Component = mineJob2.edit().create(MineJobComponent.class);
		JobDelegateComponent job4Component = mineJob2.edit().create(JobDelegateComponent.class);
		job4Component.setActualClass(MineJobComponent.class);

		world.process();

		jobSystem.findJob(worker1);
		jobSystem.findJob(worker2);
		jobSystem.findJob(worker3);

		world.process();

		jobSystem.releaseJob(worker3);
		jobSystem.findJob(worker3);

		world.process();

		jobSystem.finishJob(worker2);

		world.process();

		jobSystem.finishJob(worker1);

		world.process();

		jobSystem.findJob(worker1);
		jobSystem.findJob(worker2);

		world.process();
	}

	private void findTagged(String tag, TagSystem tagSystem) {
		Array<Entity> fill = new Array<>();
		fill.clear();
		tagSystem.get(tag, fill);
		Gdx.app.log("", "Tagged by " + tag);
		for (Entity entity:fill) {
			Gdx.app.log("", entityToStr(entity));
		}
	}

	Bag<Component> cFill = new Bag<>();
	private String entityToStr(Entity e) {
		cFill.clear();
		e.getComponents(cFill);
		String str =  "Entity{id="+e.getId();
		for(Component c: cFill) {
			// can be null when it was removed
			if (c == null) continue;
			str+=", " + c.toString();
		}
		return str + "}";
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		world.process();
	}

	@Override
	public void dispose() {
		super.dispose();
		world.dispose();
	}
}
