package io.piotrjastrzebski.playground.bttests.btedittest2;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/**
 * Created by PiotrJ on 12/10/15.
 */
public abstract class BTESource extends DragAndDrop.Source {
	public BTESource (Actor actor) {
		super(actor);
	}

	@Override final public DragAndDrop.Payload dragStart (InputEvent event, float x, float y, int pointer) {
		BTEPayload payload = BTEPayload.obtain();
		return dragStart(event, x, y, pointer, payload);
	}

	public abstract BTEPayload dragStart (InputEvent event, float x, float y, int pointer, BTEPayload out);

	@Override final public void dragStop (InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload,
		DragAndDrop.Target target) {
		BTEPayload p = (BTEPayload)payload;
		BTETarget t = (BTETarget)target;
		onDragStop(event, x, y, pointer, p, t);
		BTEPayload.free(p);
	}

	public void onDragStop (InputEvent event, float x, float y, int pointer, BTEPayload payload, BTETarget target) {

	}
}
