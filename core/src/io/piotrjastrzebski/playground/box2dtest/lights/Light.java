package io.piotrjastrzebski.playground.box2dtest.lights;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by EvilEntity on 06/11/2015.
 */
public interface Light {
	Light setPos(float x, float y);
	void fixedUpdate();
	void draw(ShapeRenderer renderer);
	void draw(PolygonSpriteBatch batch);
}
