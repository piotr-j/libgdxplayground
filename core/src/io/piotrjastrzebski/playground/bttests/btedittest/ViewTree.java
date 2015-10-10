package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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

	public ViewTree () {
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
			return true;
		}

		@Override public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
			TaskPayload p = (TaskPayload)payload;
			// delete the node
			Gdx.app.log(TAG, "Remove " + p.getViewTask());
			model.remove(p.getViewTask().getModelTask());
			pool.free(p.getViewTask());
			remove(p.getViewTask());
		}
	}

	protected static class TaskSource extends DragAndDrop.Source {
		private static Pool<TaskPayload> payloadPool = new Pool<TaskPayload>() {
			@Override protected TaskPayload newObject () {
				return new TaskPayload();
			}
		};

		protected ViewTask owner;
		public TaskSource (ViewTask owner) {
			super(owner.getSourceActor());
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
		private VisLabel drag;
		private VisLabel valid;
		private VisLabel invalid;
		private ViewTask viewTask;

		public TaskPayload () {
			setDragActor(drag = new VisLabel());
			setValidDragActor(valid = new VisLabel());
			valid.setColor(Color.GREEN);
			setInvalidDragActor(invalid = new VisLabel());
			invalid.setColor(Color.RED);
		}

		public void init (String text, ViewTask viewTask) {
			drag.setText(text);
			valid.setText(text);
			invalid.setText(text);
			this.viewTask = viewTask;
		}

		public ViewTask getViewTask () {
			return viewTask;
		}

		@Override public void reset () {
			init("<?>", null);
		}

		@Override public String toString () {
			return "TaskPayload{" +
				"viewTask=" + viewTask +
				'}';
		}
	}
}
