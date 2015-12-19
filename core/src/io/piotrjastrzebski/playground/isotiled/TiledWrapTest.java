package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class TiledWrapTest extends BaseScreen {
	public final static float MAP_WIDTH = 30;
	public final static float MAP_HEIGHT = 15;
	private final static Color v1 = new Color(Color.WHITE);
	private final static Color v2 = new Color(Color.BLUE);
	private final static Color v3 = new Color(Color.GREEN);
	private final static Color v4 = new Color(Color.RED);

	Array<MapWrapper> maps = new Array<>();
	Array<MapEntity> entities = new Array<>();
 	public TiledWrapTest (GameReset game) {
		super(game);
		// this is simple, max 4 visible maps, no bs translations and stuff
		// top maps
		maps.add(new MapWrapper(-MAP_WIDTH/2 - MAP_WIDTH, -MAP_HEIGHT/2 + MAP_HEIGHT));
		maps.add(new MapWrapper(-MAP_WIDTH/2, -MAP_HEIGHT/2 + MAP_HEIGHT));
		maps.add(new MapWrapper(-MAP_WIDTH/2 + MAP_WIDTH, -MAP_HEIGHT/2 + MAP_HEIGHT));
		// centre maps
		maps.add(new MapWrapper(-MAP_WIDTH/2 - MAP_WIDTH, -MAP_HEIGHT/2));
		maps.add(new MapWrapper(-MAP_WIDTH/2, -MAP_HEIGHT/2));
		maps.add(new MapWrapper(-MAP_WIDTH/2 + MAP_WIDTH, -MAP_HEIGHT/2));
		// bottom maps
		maps.add(new MapWrapper(-MAP_WIDTH/2 - MAP_WIDTH, -MAP_HEIGHT/2 - MAP_HEIGHT));
		maps.add(new MapWrapper(-MAP_WIDTH/2, -MAP_HEIGHT/2 - MAP_HEIGHT));
		maps.add(new MapWrapper(-MAP_WIDTH/2 + MAP_WIDTH, -MAP_HEIGHT/2 - MAP_HEIGHT));

		for (int i = 0; i < 100; i++) {
			// position relative to maps lower left corner
			entities.add(new MapEntity(
				MathUtils.random(0, MAP_WIDTH),
				MathUtils.random(0, MAP_HEIGHT),
				vb
			));
		}
	}

	Rectangle vb = new Rectangle();

	@Override public void render (float delta) {
		updateInput(delta);
		updateVB();
		Gdx.gl.glClearColor(.6f, .6f, .6f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (MapWrapper map : maps) {
			map.update(vb);
			map.render(renderer);
		}
		for (MapWrapper map : maps) {
			map.renderEntities(renderer, entities);
		}
		renderer.setColor(1, 1, 1, .25f);
		renderer.rect(vb.x, vb.y, vb.width, vb.height);
		renderer.setColor(0, 1, 1, .5f);
//		renderer.circle(cs.x-.25f, cs.y, 0.5f, 16);
//		renderer.circle(cs.x+.25f, cs.y, 0.5f, 16);

		renderer.end();
	}

	public static class MapWrapper {
		private final Rectangle mb = new Rectangle();
		private boolean visible;

		public MapWrapper (float x, float y) {
			mb.set(x, y, MAP_WIDTH, MAP_HEIGHT);
		}

		public void update(Rectangle vb) {
			visible = vb.overlaps(mb);
		}

		public void render (ShapeRenderer renderer) {
			if (!visible) return;
			// we use gradient so the boundaries are clearly visible
			renderer.rect(mb.x, mb.y, mb.width, mb.height, v1, v2, v3, v4);
		}

		public void renderEntities (ShapeRenderer renderer, Array<MapEntity> entities) {
			if (!visible) return;
			for (MapEntity entity : entities) {
				entity.render(renderer, this);
			}
		}
	}

	public static class MapEntity {
		private float x, y;
		private final Rectangle eb = new Rectangle();
		private Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), .75f);
		private Rectangle vb;

		public MapEntity (float x, float y, Rectangle vb) {
			this.x = x;
			this.y = y;
			this.vb = vb;
			eb.set(x, y, 1, 1);
		}

		public void render(ShapeRenderer renderer, MapWrapper wrapper) {
			eb.setPosition(wrapper.mb.x + x, wrapper.mb.y + y);
			if (!vb.overlaps(eb)) return;
			renderer.setColor(color);
			// we use gradient so the boundaries are clearly visible
			renderer.circle(eb.x + 0.5f, eb.y + 0.5f, .5f, 16);
		}
	}

	private void updateVB () {
		// we want the vb to be smaller than the map
		final float marginX = 6f;
		final float marginY = 5f;
		vb.set(
			gameCamera.position.x - VP_WIDTH / 2 + marginX,
			gameCamera.position.y - VP_HEIGHT / 2 + marginY,
			VP_WIDTH - marginX * 2,
			VP_HEIGHT - marginY * 2
		);
	}

	private void updateInput (float delta) {
		float scale = (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) ? 2 : 1;
		scale *= delta * 10f;
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			gameCamera.position.x -= scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			gameCamera.position.x += scale;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			gameCamera.position.y += scale;
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			gameCamera.position.y -= scale;
		}
		// if we move outside of desire range, we correct
		// this way we are always near origin andwe dont have to move the maps
		if (gameCamera.position.x < -MAP_WIDTH) gameCamera.position.x += MAP_WIDTH;
		if (gameCamera.position.x >  MAP_WIDTH) gameCamera.position.x -= MAP_WIDTH;
		if (gameCamera.position.y < -MAP_HEIGHT) gameCamera.position.y += MAP_HEIGHT;
		if (gameCamera.position.y >  MAP_HEIGHT) gameCamera.position.y -= MAP_HEIGHT;
		gameCamera.update();
		updateMousePosition(Gdx.input.getX(), Gdx.input.getY());
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, TiledWrapTest.class);
	}
}
