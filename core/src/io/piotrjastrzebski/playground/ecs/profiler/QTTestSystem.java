package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by PiotrJ on 05/08/15.
 */
@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class QTTestSystem extends BaseSystem {
	QTSystem qtSystem;
	@Wire(name = "game") OrthographicCamera camera;
	@Wire ShapeRenderer renderer;
	@Override protected void initialize () {
		super.initialize();

	}
	Rectangle testBounds = new Rectangle();
	float time = 0;
	@Override protected void processSystem () {
		time+=world.delta;
		if (time > 1) {
			time = 0;
			float width = MathUtils.random(0.5f, 3.0f);
			float height = MathUtils.random(0.5f, 3.0f);
			float x = MathUtils.random(-8, 8 - width);
			float y = MathUtils.random(-8, 8 - height);
			testBounds.set(x, y, width, height);
		}
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.YELLOW);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.rect(testBounds.x, testBounds.y, testBounds.width, testBounds.height);
		renderer.end();
	}
}
