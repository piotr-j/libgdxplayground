package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class DominoTest extends BaseScreen {
	private static final String TAG = DominoTest.class.getSimpleName();

	public DominoTest (GameReset game) {
		super(game);
		clear.set(0, 0, 0, 1);

	}

	float timer = 0;
	Vector2 scale = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);
		enableBlending();

		float worldSize = VP_HEIGHT * .8f;


		timer += delta * .5f;
		scale.x = 1 + MathUtils.sin(timer * MathUtils.PI) * 2;
		scale.y = 1 + MathUtils.cos(timer * MathUtils.PI) * 2;

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.setColor(1, 0, 0, .25f);
		renderer.rect(-worldSize/2, -worldSize/2, worldSize, worldSize);
//		renderer.setColor(1, 1, 1, .1f);
		renderer.setColor(1, 1, 1, 1f);
		initiateDomino(-worldSize/2, -worldSize/2, worldSize);
		renderer.end();

	}

	public void initiateDomino (float x, float y, float edge) {
		if (edge < .2f) return;

		float dimension = edge / 3;

		renderer.rect(x + dimension * scale.x, y + dimension * scale.y, dimension, dimension);

		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				initiateDomino(x + dimension * i, y + dimension * j, edge / 3);
			}
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, DominoTest.class);
	}
}
