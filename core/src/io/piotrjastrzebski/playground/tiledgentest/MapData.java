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
		// raw normalized values
		public float elevation;
		public float rainfall;
		public float temp;
		public float mountains;

		// is water tile
		public boolean water;
		public float blur;

		public void setColor (float r, float g, float b) {
			color.set(r, g, b, 1);
		}

		public void setColor (Color color) {
			this.color.set(color);
		}

		@Override public String toString () {
			return String.format("Tile<%d, %d, e:%.3f, r: %.2f, t:%.2f, m:%.2f>", x, y, elevation, rainfall, temp, mountains);
		}

		public void mulColor (float r, float g, float b) {
			color.mul(r, g, b, 1);
		}

		public void mulColor (Color c) {
			color.mul(c);
		}

		public void addColor (float r, float g, float b) {
			color.add(r, g, b, 1);
		}
	}
}
