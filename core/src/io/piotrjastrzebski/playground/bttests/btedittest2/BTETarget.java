package io.piotrjastrzebski.playground.bttests.btedittest2;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

/**
 * Created by PiotrJ on 12/10/15.
 */
public abstract class BTETarget extends DragAndDrop.Target {

	public BTETarget (Actor actor) {
		super(actor);
	}


	@Override final public boolean drag (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
		BTESource s = (BTESource)source;
		BTEPayload p = (BTEPayload)payload;
		return onDrag(s, p, x, y, pointer);
	}

	public abstract boolean onDrag (BTESource source, BTEPayload payload, float x, float y, int pointer);

	@Override final public void drop (DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
		BTESource s = (BTESource)source;
		BTEPayload p = (BTEPayload)payload;
		onDrop(s, p, x, y, pointer);
	}

	public abstract void onDrop (BTESource source, BTEPayload payload, float x, float y, int pointer);

	@Override final public void reset (DragAndDrop.Source source, DragAndDrop.Payload payload) {
		BTESource s = (BTESource)source;
		BTEPayload p = (BTEPayload)payload;
		onReset(s, p);
	}

	public void onReset (BTESource source, BTEPayload payload) {}
}
