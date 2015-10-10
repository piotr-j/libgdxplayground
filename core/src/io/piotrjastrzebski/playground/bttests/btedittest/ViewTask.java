package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTask<E> extends VisTree.Node implements Pool.Poolable, ModelTask.StatusListener {
	protected Pool<ViewTask<E>> pool;
	protected VisLabel name;
	protected VisLabel status;
	protected ModelTask<E> task;

	public ViewTask (Pool<ViewTask<E>> pool) {
		super(new VisTable());
		this.pool = pool;
		VisTable container = (VisTable)getActor();
		name = new VisLabel();
		status = new VisLabel();
		container.add(name).padRight(10);
		container.add(status);
	}

	public ViewTask<E> init (ModelTask<E> task) {
		this.task = task;
		name.setText(task.getName());
		statusChanged(null, task.getStatus());
		task.addListener(this);
		return this;
	}

	public void update (float delta) {
		for (Tree.Node node : getChildren()) {
			((ViewTask)node).update(delta);
		}
	}

	@Override public void reset () {
		name.setText("");
		task.removeListener(this);
		task = null;
		for (Tree.Node node : getChildren()) {
			pool.free((ViewTask<E>)node);
		}
		remove();
	}

	@Override public String toString () {
		return "ViewTask{" +
			"label=" + name.getText() +
			'}';
	}

	protected float fadeTime = 1.5f;
	@Override public void statusChanged (Task.Status from, Task.Status to) {
		status.setText(to.toString());
		status.setColor(getColor(to));
		status.clearActions();
		status.addAction(Actions.color(Color.GRAY, fadeTime, Interpolation.pow3In));
	}

	private static Color getColor (Task.Status status) {
		if (status == null) return Color.GRAY;
		switch (status) {
		case SUCCEEDED:
			return Color.GREEN;
		case RUNNING:
			return Color.ORANGE;
		case FAILED:
			return Color.RED;
		case CANCELLED:
			return Color.PURPLE;
		case FRESH:
		default:
			return Color.GRAY;
		}
	}
}
