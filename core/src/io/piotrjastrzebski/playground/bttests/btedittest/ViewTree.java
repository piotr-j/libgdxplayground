package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTree;

/**
 * Created by EvilEntity on 10/10/2015.
 */
public class ViewTree<E> extends VisTree implements Pool.Poolable {
	private static final String TAG = ViewTree.class.getSimpleName();
	protected Pool<ViewTask<E>> vtPool;
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
		vtPool = new Pool<ViewTask<E>>() {
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
		add(viewRoot = vtPool.obtain().init(root));
		for (ModelTask<E> task : root) {
			addViewTask(viewRoot, task);
		}
		expandAll();
	}

	protected void addViewTask (ViewTask<E> parent, ModelTask<E> task) {
		ViewTask<E> node = vtPool.obtain().init(task);
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
	public void addSource (Actor source, final Class<? extends Task> task) {
		dad.addSource(new BTESource(source) {
			@Override public BTEPayload dragStart (InputEvent event, float x, float y, int pointer, BTEPayload out) {
				// TODO should this create a node for this already?
//				ViewTask<E> obtain = vtPool.obtain();
				out.setAsAdd(task);
				return out;
			}

			@Override public void onDragStop (InputEvent event, float x, float y, int pointer, BTEPayload payload,
				BTETarget target) {
				Gdx.app.log("Add", "OnStop");

			}
		});
	}

	public void addTrash (Actor trash) {
		dad.addTarget(new BTETarget(trash) {
			@Override public boolean onDrag (BTESource source, BTEPayload payload, float x, float y, int pointer) {
				return payload.hasTarget(BTEPayload.TARGET_TRASH);
			}

			@Override public void onDrop (BTESource source, BTEPayload payload, float x, float y, int pointer) {
				// do we put the task in source on in payload?
				// TODO confirm?
				Gdx.app.log(TAG, "trash " + payload.getMoveTask());
//				trash(payload.getMoveTask());
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
		vtPool.free(vt);
		remove(vt);
	}

	public interface ViewTaskSelectedListener<E> {
		void selected(ViewTask<E> task);
		void deselected();
	}
}
