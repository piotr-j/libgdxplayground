package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.ecs.jobs.components.Job;
import io.piotrjastrzebski.playground.ecs.jobs.components.Worker;
import io.piotrjastrzebski.playground.ecs.jobs.systems.JobMaker;
import io.piotrjastrzebski.playground.ecs.jobs.systems.Jobs;
import io.piotrjastrzebski.playground.ecs.jobs.systems.Workers;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class GUI extends IteratingSystem {
	private ComponentMapper<Godlike> mGodlike;
	protected ComponentMapper<Job> mJob;
	protected ComponentMapper<Worker> mWorker;
	@Wire Stage stage;
	@Wire(name = "game-cam") OrthographicCamera camera;

	JobMaker jobMaker;
	Jobs jobs;
	Workers workers;

	public GUI () {
		super(Aspect.all(Godlike.class));
	}

	VisLabel name;
	VisLabel entity;
	VisTextButton pause;
	@Override protected void initialize () {
		super.initialize();
		pause = new VisTextButton("Pause");
		pause.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				boolean enabled = jobMaker.isEnabled();
				if (enabled) {
					pause.setText("Resume");
					jobMaker.setEnabled(false);
					jobs.setEnabled(false);
					workers.setEnabled(false);
				} else {
					pause.setText("Pause");
					jobMaker.setEnabled(true);
					jobs.setEnabled(true);
					workers.setEnabled(true);
				}
			}
		});
		stage.addActor(pause);
		pause.setPosition(10, 10);
		VisWindow window = new VisWindow("Selected");
		window.setSize(300, 200);
		window.setResizeBorder(12);
		window.setResizable(true);
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

	@Override protected void inserted (int entityId) {
		Godlike godlike = mGodlike.get(entityId);
		stage.addActor(godlike.actor);
	}

	@Override protected void begin () {
		name.setText("Nothing");
		entity.setText("");
	}

	Vector3 temp = new Vector3();
	@Override protected void process (int e) {
		Godlike godlike = mGodlike.get(e);
		camera.project(temp.set(godlike.x + godlike.width / 2 , godlike.y + godlike.height / 2, 0));
		VisLabel label = godlike.actor;
		if (mJob.has(e)) {
			Job job = mJob.get(e);
			String text = job.name + "\np: "+(int)(job.progress*100) + "%";
			if (job.next >= 0) {
				Job next = mJob.get(job.next);
				text +="\nn:" + next.name;
			}
			if (job.required.size > 0) {
				text +="\nr:[";
				for (int i = 0; i < job.required.size; i++) {
					int id = job.required.get(i);
					if (id < 0) continue;
					Job req = mJob.get(id);
					if (req != null) {
						text += req.name + ",";
					} else {
						text += id + ",";
					}
				}
				text+="]";
			}
			if (job.workerID >= 0) {
				Worker worker = mWorker.get(job.workerID);
				text +="\nw:" + worker.name;
			}
			label.setText(text);
		} else if (mWorker.has(e)) {
			Worker worker = mWorker.get(e);
			String text = worker.name;

			if (worker.jobID >= 0) {
				Job next = mJob.get(worker.jobID);
				text +="\nj:" + next.name;
			} else {
				text +="\nj:none";
			}
			label.setText(text);
		}
		label.setPosition(temp.x - label.getWidth() / 2, temp.y - label.getHeight() / 2);
		if (godlike.selected) {
			name.setText(godlike.name);
			entity.setText(ECSJobsTest.entityToStr(world, e));
		}
	}

	@Override protected void end () {
		stage.act(world.delta);
		stage.draw();
	}

	@Override protected void removed (int entityId) {
		mGodlike.get(entityId).actor.remove();
	}
}
