package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTask<E> extends VisTree.Node implements Pool.Poolable, ModelTask.ModelTaskListener {
	protected Pool<ViewTask<E>> pool;
	protected VisLabel name;
	protected VisLabel status;
	protected ModelTask<E> task;
	protected DragAndDrop dad;
	protected ViewTree.TaskSource source;
	protected ViewTree.AddTarget target;
	protected VisTable container;

	DragAndDrop.Target target2;
	Actor separator;
	Drawable containerBG;
	public ViewTask (final ViewTree<E> owner) {
		super(new VisTable());
		// object is used to find this node in tree
		setObject(this);
		separator = owner.getSeparator();
		pool = owner.pool;
		dad = owner.dad;
		container = (VisTable)getActor();
		name = new VisLabel();
		status = new VisLabel();
		container.add(name).pad(2, 0, 2, 10);
		container.add(status);
		// dad prefers touchable things, we want entire node to be a valid target
		container.setTouchable(Touchable.enabled);
		containerBG = VisUI.getSkin().getDrawable("white");
		container.setColor(Color.GREEN);

		source = new ViewTree.TaskSource(this);
		target = new ViewTree.AddTarget(this);

		target2 = new DragAndDrop.Target(container) {
			@Override public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				// TODO check if we can add given payload to this target
				Actor actor = getActor();
				DropPoint dropPoint = getDropPoint(actor, y);
				boolean isValid = owner.canMoveTo(ViewTask.this, (ViewTask<E>)payload.getObject(), dropPoint);
				updateSeparator(dropPoint, isValid);
				return isValid;
			}

			@Override public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
				owner.moveTo(ViewTask.this, (ViewTask<E>)payload.getObject(), getDropPoint(getActor(), y));
			}

			@Override public void reset (DragAndDrop.Source source, DragAndDrop.Payload payload) {
				updateSeparator(null, true);
			}
		};
	}

	public ViewTask<E> init (ModelTask<E> task) {
		this.task = task;
		dad.addSource(source);
		dad.addTarget(target2);
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
		dad.removeSource(source);
		dad.removeTarget(target2);
		for (Tree.Node node : getChildren()) {
			pool.free((ViewTask<E>)node);
		}
	}

	public static final float DROP_MARGIN = 0.25f;
	private DropPoint getDropPoint (Actor actor, float y) {
		float a = y / actor.getHeight();
		if (a < DROP_MARGIN) {
			return DropPoint.BELOW;
		} else if (a > 1 - DROP_MARGIN) {
			return DropPoint.ABOVE;
		}
		return DropPoint.MIDDLE;
	}

	private void updateSeparator (DropPoint dropPoint, boolean isValid) {
		separator.setVisible(false);
		container.setBackground((Drawable)null);
		Color color = isValid?Color.GREEN:Color.RED;
		separator.setColor(color);
		container.setColor(color);
		if (dropPoint == null) return;
		separator.setWidth(container.getWidth());
		switch (dropPoint) {
		case ABOVE:
			separator.setVisible(true);
			separator.setPosition(container.getX(), container.getY() + container.getHeight() - separator.getHeight());
			break;
		case MIDDLE:
			container.setBackground(containerBG);
			break;
		case BELOW:
			separator.setVisible(true);
			separator.setPosition(container.getX(), container.getY());
			break;
		}
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

	@Override public void validChanged (boolean valid) {
		if (valid) {
			name.setColor(Color.WHITE);
		} else {
			// TODO some sort of a hint?
			name.setColor(Color.RED);
		}
	}

	public Actor getDaDActor () {
		return container;
	}

	public void initPayload (ViewTree.TaskPayload payload) {
		payload.setObject(this);
		payload.init(name.getText().toString(), this);
		payload.addTarget(ViewTree.TaskPayload.TARGET_TRASH);
	}

	public ModelTask<E> getModelTask () {
		return task;
	}
}
