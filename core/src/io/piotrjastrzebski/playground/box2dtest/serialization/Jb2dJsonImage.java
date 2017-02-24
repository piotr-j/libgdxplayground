package io.piotrjastrzebski.playground.box2dtest.serialization;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by EvilEntity on 30/11/2016.
 */
public class Jb2dJsonImage {
	String name;
	String path;
	String file;
	Body body;
	Vector2 center;
	float angle;
	float scale;
	float aspectScale;
	boolean flip;
	float opacity;
	int filter; // 0 = nearest, 1 = linear
	float renderOrder;
	int colorTint[];

	Vector2 corners[];

	int numPoints;
	float points[];
	float uvCoords[];
	int numIndices;
	short indices[];

	public Jb2dJsonImage () {
		colorTint = new int[4];
	}
}
