package io.piotrjastrzebski.playground.ecs.aijobs.tasks;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by PiotrJ on 19/08/15.
 */
@Wire
public class IdleTask extends BaseTask {
	private final static String TAG = IdleTask.class.getSimpleName();

	@Override
	public Status execute () {
//		Gdx.app.log(TAG, "idle " + getObject());
		return Status.SUCCEEDED;
	}
}
