package io.piotrjastrzebski.playground.ecs.fancywalltest.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transformer;
import io.piotrjastrzebski.playground.ecs.fancywalltest.components.Transform;

/**
 * Created by PiotrJ on 30/09/15.
 */
@Wire
public class Interpolator extends EntityProcessingSystem {
	protected ComponentMapper<Transform> mTransform;
	protected ComponentMapper<Transformer> mTargetTransform;

	public Interpolator () {
		super(Aspect.all(Transform.class, Transformer.class));
	}

	Vector2 tmp = new Vector2();
	@Override protected void process (Entity e) {
		Transform t = mTransform.get(e);
		Transformer tf = mTargetTransform.get(e);
		if (tf.timer < tf.duration) {
			float a = tf.timer/tf.duration;
			if (tf.reverse) {
				tmp.set(tf.dst).interpolate(tf.src, a, Interpolation.linear);
				t.pos.set(tmp);
				t.angle = MathUtils.lerpAngleDeg(tf.dstAngle, tf.srcAngle, a);
			} else {
				tmp.set(tf.src).interpolate(tf.dst, a, Interpolation.linear);
				t.pos.set(tmp);
				t.angle = MathUtils.lerpAngleDeg(tf.srcAngle, tf.dstAngle, a);
			}
		}

		tf.timer += world.delta;

	}
}
