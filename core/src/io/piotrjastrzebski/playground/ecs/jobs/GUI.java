package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class GUI extends EntityProcessingSystem {
	private ComponentMapper<Godlike> mGodlike;
	@Wire Stage stage;

	public GUI () {
		super(Aspect.all(Godlike.class));
	}

	VisLabel name;
	VisLabel entity;
	@Override protected void initialize () {
		super.initialize();
		VisWindow window = new VisWindow("Stuff");
		window.setSize(300, 200);
		name = new VisLabel();
		window.add(name);
		window.row();
		entity = new VisLabel();
		entity.setWrap(false);
		window.add(entity);
		window.row();
		// ...
//		container.setPosition(0, 720);
		stage.addActor(window);
		window.centerWindow();
	}

	@Override protected void begin () {
		name.setText("Nothing");
		entity.setText("");
	}

	@Override protected void process (Entity e) {
		Godlike godlike = mGodlike.get(e);
		if (godlike.selected) {
			name.setText(godlike.name);
			entity.setText(ECSJobsTest.entityToStr(e));
		}
	}

	@Override protected void end () {
		stage.act(world.delta);
		stage.draw();
	}
}
