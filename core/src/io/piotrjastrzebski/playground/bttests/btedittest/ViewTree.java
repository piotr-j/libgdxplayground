package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTree<E> extends VisTree implements Pool.Poolable {
	private static final String TAG = ViewTree.class.getSimpleName();
	protected Pool<ViewTask<E>> pool;
	protected ModelTree<E> model;

	protected DragAndDrop dad;
	private Actor separator;

	public ViewTree () {
		// remove y spacing so we dont have gaps for DaD
		setYSpacing(0);
		separator = new VisImage(VisUI.getSkin().getDrawable("white"));
		separator.setColor(Color.GREEN);
		separator.setHeight(4 * VisUI.getSizes().scaleFactor);
		separator.setVisible(false);
		addActor(separator);

		dad = new DragAndDrop();
		pool = new Pool<ViewTask<E>>() {
			@Override protected ViewTask<E> newObject () {
				return new ViewTask<>(ViewTree.this);
			}
		};
		// single selection makes thing simpler for edit
		getSelection().setMultiple(false);
		addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				ViewTree.this.changed((ViewTask<E>)getSelection().getLastSelected());
			}
		});
	}

	public void addTrash (Actor trash) {
		dad.addTarget(new TrashTarget(trash));
	}

	public void update (float delta) {
		if (viewRoot == null) return;
		viewRoot.update(delta);
	}

	private void changed (ViewTask<E> selection) {
		if (selection != null) {
			for (ViewTaskSelectedListener<E> listener : listeners) {
				listener.selected(selection);
			}
		} else {
			for (ViewTaskSelectedListener<E> listener : listeners) {
				listener.deselected();
			}
		}
	}

	ViewTask<E> viewRoot;
	public void init (ModelTree<E> model) {
		if (this.model != null) reset();
		this.model = model;
		ModelTask<E> root = model.getRoot();
		add(viewRoot = pool.obtain().init(root));
		for (ModelTask<E> task : root) {
			addViewTask(viewRoot, task);
		}
		expandAll();
	}

	protected void addViewTask (ViewTask<E> parent, ModelTask<E> task) {
		ViewTask<E> node = pool.obtain().init(task);
		parent.add(node);
		for (ModelTask<E> child : task) {
			addViewTask(node, child);
		}
	}

	@Override public void reset () {
		listeners.clear();
	}

	protected Array<ViewTaskSelectedListener<E>> listeners = new Array<>();
	public void addListener(ViewTaskSelectedListener<E> listener) {
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}

	public void removeListener(ViewTaskSelectedListener<E> listener) {
		listeners.removeValue(listener, true);
	}

	/**
	 * register given actor as source with task that can be added to the tree
	 */
	public void addSource (Actor actor, final Class<? extends Task> task) {
		dad.addSource(new DragAndDrop.Source(actor) {
			@Override public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
				TaskPayload payload = payloadPool.obtain();
				payload.init(task);
				payload.addTarget(TaskPayload.TARGET_ADD);
				return payload;
			}

			@Override public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload,
				DragAndDrop.Target target) {
				payloadPool.free((TaskPayload)payload);
			}
		});
	}

	public Actor getSeparator () {
		return separator;
	}

	public boolean canAddTo (ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		switch (to) {
		case ABOVE:
			// insert vt above target into targets parent

			break;
		case MIDDLE:
			// insert vt into target

			break;
		case BELOW:
			// insert vt below target into targets parent

			break;
		}
		return false;
	}

	public void addTo(ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		switch (to) {
		case ABOVE:
			// insert vt above target into targets parent

			break;
		case MIDDLE:
			// insert vt into target

			break;
		case BELOW:
			// insert vt below target into targets parent

			break;
		}
	}

	public boolean canMoveTo (ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		switch (to) {
		case ABOVE:
			// insert vt above target into targets parent

			break;
		case MIDDLE:
			// insert vt into target

			break;
		case BELOW:
			// insert vt below target into targets parent

			break;
		}
		return false;
	}

	public void moveTo(ViewTask<E> vt, ViewTask<E> target, DropPoint to) {
		switch (to) {
		case ABOVE:
			// insert vt above target into targets parent

			break;
		case MIDDLE:
			// insert vt into target

			break;
		case BELOW:
			// insert vt below target into targets parent

			break;
		}
	}

	public void trash (ViewTask<E> vt) {
		Gdx.app.log(TAG, "Remove " + vt);
		model.remove(vt.getModelTask());
		pool.free(vt);
		remove(vt);
	}

	public interface ViewTaskSelectedListener<E> {
		void selected(ViewTask<E> task);
		void deselected();
	}

	protected class TrashTarget extends DragAndDrop.Target {
		public TrashTarget (Actor actor) {
			super(actor);
		}

		@Override public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			TaskPayload p = (TaskPayload)payload;

			// TODO check if payload contains proper target
			return p.hasTarget(TaskPayload.TARGET_TRASH);
		}

		@Override public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			trash((ViewTask<E>)payload.getObject());
		}
	}

	protected static class AddTarget extends DragAndDrop.Target {
		protected ViewTask owner;
		public AddTarget (ViewTask owner) {
			super(owner.getActor());
			this.owner = owner;
		}

		@Override public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			TaskPayload p = (TaskPayload)payload;

			// TODO check if payload contains proper target
			return p.hasTarget(TaskPayload.TARGET_ADD);
		}

		@Override public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			TaskPayload p = (TaskPayload)payload;
			// delete the node
			Gdx.app.log(TAG, "Add " + p.getTaskClass());

		}
	}

	private static Pool<TaskPayload> payloadPool = new Pool<TaskPayload>() {
		@Override protected TaskPayload newObject () {
			return new TaskPayload();
		}
	};

	protected static class TaskSource extends DragAndDrop.Source {
		protected ViewTask owner;
		public TaskSource (ViewTask owner) {
			super(owner.getActor());
			this.owner = owner;
		}

		@Override public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
			TaskPayload payload = payloadPool.obtain();
			owner.initPayload(payload);
			return payload;
		}

		@Override public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload,
			DragAndDrop.Target target) {
			payloadPool.free((TaskPayload)payload);
		}
	}

	protected static class TaskPayload extends DragAndDrop.Payload implements Pool.Poolable {
		public static final int TARGET_TRASH = 1;
		public static final int TARGET_ADD = 1 << 1;
		private VisLabel drag;
		private VisLabel valid;
		private VisLabel invalid;
		private ViewTask viewTask;
		private Class<? extends Task> taskClass;
		private int target = 0;

		public TaskPayload () {
			setDragActor(drag = new VisLabel());
			setValidDragActor(valid = new VisLabel());
			valid.setColor(Color.GREEN);
			setInvalidDragActor(invalid = new VisLabel());
			invalid.setColor(Color.RED);
		}

		public void init (String text, ViewTask viewTask) {
			setText(text);
			this.viewTask = viewTask;
			setObject(viewTask);
		}

		public void setText(String text) {
			drag.setText(text);
			valid.setText(text);
			invalid.setText(text);
		}

		public void init (Class<? extends Task> taskClass) {
			setText(taskClass.getSimpleName());
			this.taskClass = taskClass;
		}

		public void addTarget(int target) {
			this.target |= target;
		}

		public boolean hasTarget (int target) {
			return (this.target & target) != 0;
		}

		public ViewTask getViewTask () {
			return viewTask;
		}

		public Class<? extends Task> getTaskClass () {
			return taskClass;
		}

		@Override public void reset () {
			init("<?>", null);
			target = 0;
			setObject(null);
		}

		@Override public String toString () {
			return "TaskPayload{" +
				"viewTask=" + viewTask +
				'}';
		}
	}
}
