package io.piotrjastrzebski.playground.ecs.fancywalltest.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by PiotrJ on 30/09/15.
 */
public class Transformer extends PooledComponent {
	public Vector2 dst = new Vector2();
	public Vector2 src = new Vector2();
	public float dstAngle;
	public float srcAngle;
	public float duration;
	public float timer;
	public boolean reverse;
	@Override protected void reset () {
		dst.setZero();
		src.setZero();
		dstAngle = 0;
		srcAngle = 0;
		duration = 0;
		timer = 0;
		reverse = false;
	}

	public Transformer setSrs (Vector2 pos, float angle) {
		setSrcPos(pos);
		setSrcAngle(angle);
		return this;
	}

	public Transformer setDst (Vector2 pos, float angle) {
		setDstPos(pos);
		setDstAngle(angle);
		return this;
	}

	public Transformer setSrs (float x, float y, float angle) {
		src.set(x, y);
		srcAngle = angle;
		return this;
	}

	public Transformer setDst (float x, float y, float angle) {
		dst.set(x, y);
		dstAngle = angle;
		return this;
	}

	public Transformer setSrcPos (Vector2 pos) {
		src.set(pos);
		return this;
	}

	public Transformer setDstPos (Vector2 pos) {
		dst.set(pos);
		return this;
	}

	public Transformer setSrcAngle (float angle) {
		srcAngle = angle;
		return this;
	}

	public Transformer setDstAngle (float angle) {
		dstAngle = angle;
		return this;
	}

	public Transformer setDuration (float duration) {
		this.duration = duration;
		return this;
	}
}
