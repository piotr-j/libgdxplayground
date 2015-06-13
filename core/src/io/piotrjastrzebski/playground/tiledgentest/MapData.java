package io.piotrjastrzebski.playground.tiledgentest;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class MapData {
	public Tile[][] tiles;
	public int width;
	public int height;
	public float water;

	public int largestFeature;
	public float persistence;
	public long seed;
	public boolean waterEnabled;
	public boolean biomeEnabled;

	public static class Tile {
		public Color color = new Color();
		public int x, y;
		public double value;

		public void setColor (float r, float g, float b) {
			color.set(r, g, b, 1);
		}

		@Override public String toString () {
			return String.format("Tile<%d, %d, v:%.3f>", x, y, value);
		}
	}
}
