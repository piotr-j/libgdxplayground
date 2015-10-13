package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
	protected VisTable container;

	protected BTESource source;
	protected BTETarget target;

	protected Actor separator;
	protected Drawable containerBG;

	public ViewTask (final ViewTree<E> owner) {
		super(new VisTable());
		// object is used to find this node in tree
		setObject(this);
		separator = owner.getSeparator();
		pool = owner.vtPool;
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

		source = new BTESource(getActor()) {
			@Override public BTEPayload dragStart (InputEvent event, float x, float y, int pointer, BTEPayload out) {
				out.setAsMove(ViewTask.this);
				return out;
			}
		};

		target = new BTETarget(getActor()) {
			@Override public boolean onDrag (BTESource source, BTEPayload payload, float x, float y, int pointer) {
				Actor actor = getActor();
				DropPoint dropPoint = getDropPoint(actor, y);
				boolean isValid = owner.canAddTo(payload.getViewTask(), ViewTask.this, dropPoint);
				updateSeparator(dropPoint, isValid, separator, container);
				return isValid;
			}

			@Override public void onDrop (BTESource source, BTEPayload payload, float x, float y, int pointer) {
				// TODO execute proper action
				DropPoint dropPoint = getDropPoint(getActor(), y);
//				Gdx.app.log("ViewTask", "add " + payload.getViewTask() + " at " + dropPoint);
				owner.addTo(payload.getViewTask(), ViewTask.this, dropPoint);
			}

			@Override public void onReset (BTESource source, BTEPayload payload) {
				updateSeparator(null, true, separator, container);
			}
		};
	}

	public ViewTask<E> init (ModelTask<E> task) {
		this.task = task;
		dad.addSource(source);
		dad.addTarget(target);
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
		dad.removeTarget(target);
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

	@Override public void validChanged (boolean valid) {
		if (valid) {
			name.setColor(Color.WHITE);
		} else {
			// TODO some sort of a hint?
			name.setColor(Color.RED);
		}
	}

	protected int getIndexInParent() {
		Tree.Node parent = getParent();
		if (parent == null) return 0;
		return parent.getChildren().indexOf(this, true);
	}

	public ModelTask<E> getModelTask () {
		return task;
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

	private static void updateSeparator (DropPoint dropPoint, boolean isValid, Actor sep, Table cont) {
		sep.setVisible(false);
		cont.setBackground((Drawable)null);
		Color color = isValid? Color.GREEN : Color.RED;
		sep.setColor(color);
		cont.setColor(color);
		// if dp is null we just hide the separator
		if (dropPoint == null)
			return;

		sep.setWidth(cont.getWidth());
		switch (dropPoint) {
		case ABOVE:
			sep.setVisible(true);
			sep.setPosition(cont.getX(), cont.getY() + cont.getHeight() - sep.getHeight());
			break;
		case MIDDLE:
			cont.setBackground("white");
			break;
		case BELOW:
			sep.setVisible(true);
			sep.setPosition(cont.getX(), cont.getY());
			break;
		}
	}
}
