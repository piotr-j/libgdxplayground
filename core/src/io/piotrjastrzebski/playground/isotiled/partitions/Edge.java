package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by EvilEntity on 13/01/2016.
 */
public class Edge implements Pool.Poolable {
	private static Pool<Edge> pool = new Pool<Edge>() {
		@Override protected Edge newObject () {
			return new Edge();
		}
	};

	public static Edge obtain () {
		return pool.obtain();
	}

	public static void free (Edge edge) {
		pool.free(edge);
	}

	public int id;
	public int x;
	public int y;
	public int length;
	public boolean horizontal;
	public MapRegion.SubRegion subA;
	public MapRegion.SubRegion subB;
	// color for debug, buuuu
	public final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), .5f);

	protected Edge () {
	}

	public Edge init (int id, int x, int y, int length, boolean horizontal) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.length = length;
		this.horizontal = horizontal;
		return this;
	}

	public void add (MapRegion.SubRegion region) {
		if (subA == null) {
			subA = region;
		} else if (subB == null) {
			subB = region;
		} else {
			throw new AssertionError("There can only be 2 sub regions per edge!");
		}
	}

	@Override public void reset () {
		subA = null;
		subB = null;
		id = -1;
		x = -1;
		y = -1;
		length = -1;
		horizontal = false;
	}

	@Override public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Edge edge = (Edge)o;
		return id == edge.id;

	}

	@Override public int hashCode () {
		// if must be unique
		return id;
	}

	@Override public String toString () {
		return "Edge{" +
			"id=" + id +
			", x=" + x +
			", y=" + y +
			", length=" + length +
			(horizontal ? ", horizontal" : ", vertical") +
			'}';
	}
}
