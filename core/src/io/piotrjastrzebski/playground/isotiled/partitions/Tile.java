package io.piotrjastrzebski.playground.isotiled.partitions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by EvilEntity on 06/01/2016.
 */
class Tile {
	public int id;
	public int x;
	public int y;
	public Color color = new Color();
	public Color tint = new Color();
	public int type;

	public Tile (int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
		setType(0);
	}

	public void setType (int type) {
		this.type = type;
		switch (type) {
		case 0: // grass
			color.set(0, 1, 0, 1);
			break;
		case 1: // wall
			color.set(Color.FIREBRICK);
			break;
		case 2: // door
			color.set(1f, .8f, .6f, 1);
			break;
		}
	}

	public void render (ShapeRenderer renderer, float delta) {
		renderer.setColor(color);
		renderer.rect(x, y, 1, 1);
	}

	public void setColor (Color color) {
		this.color.set(color);
	}

	public boolean contains(float x, float y) {
		return this.x <= x && this.x + 1f >= x && this.y <= y && this.y + 1f >= y;
	}

	@Override public String toString () {
		return "Tile{" + x + ", " + y + ", id=" + id + "}";
	}

	@Override public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Tile tile = (Tile)o;
		return id == tile.id;
	}

	@Override public int hashCode () {
		return id;
	}
}
