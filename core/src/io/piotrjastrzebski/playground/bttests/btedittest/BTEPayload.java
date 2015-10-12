package io.piotrjastrzebski.playground.bttests.btedittest;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Created by PiotrJ on 12/10/15.
 */
public class BTEPayload extends DragAndDrop.Payload implements Pool.Poolable {
	private static final Pool<BTEPayload> PAYLOAD_POOL = Pools.get(BTEPayload.class);

	protected static BTEPayload obtain () {
		return PAYLOAD_POOL.obtain();
	}

	protected static void free (BTEPayload payload) {
		PAYLOAD_POOL.free(payload);
	}

	protected static void clear () {
		PAYLOAD_POOL.clear();
	}

	public static final int TARGET_TRASH = 1;
	public static final int TARGET_ADD = 1 << 1;
	public static final int TARGET_MOVE = 1 << 2;

	protected VisLabel drag;
	protected VisLabel valid;
	protected VisLabel invalid;
	protected int targetMask;

	public BTEPayload () {
		drag = new VisLabel();
		setDragActor(drag);
		valid = new VisLabel();
		valid.setColor(Color.GREEN);
		setValidDragActor(valid);
		invalid = new VisLabel();
		invalid.setColor(Color.RED);
		setInvalidDragActor(invalid);
	}

	public void setDragText (String text) {
		drag.setText(text);
		valid.setText(text);
		invalid.setText(text);
	}

	public void addTarget(int target) {
		targetMask |= target;
	}

	public boolean hasTarget (int target) {
		return (targetMask & target) != 0;
	}

	@Override public void reset () {
		targetMask = 0;
		setDragText("<?>");
	}

	protected Class<? extends Task> addTaskClass;
	public void setAsAdd (Class<? extends Task> task) {
		addTaskClass = task;
		setDragText(task.getSimpleName());
		addTarget(TARGET_ADD);
	}

	protected ViewTask moveTask;
	public void setAsMove (ViewTask task) {
		moveTask = task;
		setDragText(task.getClass().getSimpleName());
		addTarget(TARGET_MOVE);
		addTarget(TARGET_TRASH);
	}

	public Class<? extends Task> getAddTaskClass () {
		return addTaskClass;
	}

	public ViewTask getMoveTask () {
		return moveTask;
	}
}
