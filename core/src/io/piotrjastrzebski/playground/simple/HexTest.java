package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.graphics.g2d.Batch.*;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;

/**
 * Simple hex map test
 *
 * Created by EvilEntity on 25/01/2016.
 */
public class HexTest extends BaseScreen {
	private static final String TAG = HexTest.class.getSimpleName();

	MyHexagonalTiledMapRenderer mapRenderer;
	TiledMap map;
	TiledMapTileLayer layer;
	public HexTest (final GameReset game) {
		super(game);
		map = new TmxMapLoader().load("hex/hex-map2.tmx");
		mapRenderer = new MyHexagonalTiledMapRenderer(map, INV_SCALE, batch);
		layer = (TiledMapTileLayer)map.getLayers().get(0);
		firstTile = new SelectedTile();
		firstTile.tile = layer.getCell(0, 0).getTile();
		firstTile.gx = 0;
		firstTile.gy = 0;
		secondTile = new SelectedTile();
		secondTile.tile = layer.getCell(2, 0).getTile();
		secondTile.gx = 2;
		secondTile.gy = 0;

		gameCamera.position.set((layer.getWidth() * layer.getTileWidth())/2 * INV_SCALE, (layer.getHeight() * layer.getTileHeight() * .75f)/2* INV_SCALE, 0);
		gameCamera.update();

		GestureDetector gd = new GestureDetector(new GestureDetector.GestureAdapter(){
			Vector3 sp = new Vector3();
			Vector3 ep = new Vector3();
			@Override public boolean pan (float x, float y, float deltaX, float deltaY) {
				gameCamera.unproject(sp.set(x, y, 0));
				gameCamera.unproject(ep.set(x + deltaX, y + deltaY, 0));
				gameCamera.position.add(-ep.x + sp.x, -ep.y + sp.y, 0);
				gameCamera.update();
				return true;
			}
		});
		multiplexer.addProcessor(0, gd);
	}

	SelectedTile overTile;
	SelectedTile firstTile;
	SelectedTile secondTile;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);

		renderer.setColor(0, 0, 1, .5f);
		int width = layer.getWidth();
		int height = layer.getHeight();
		float tw = layer.getTileWidth() * INV_SCALE;
		float th = layer.getTileHeight() * INV_SCALE;
		float s = th/2f;
		float hexH = th;
		// for 'real' hexagons
//		float hexW = (float)(Math.sqrt(3) /2  * hexH);
		float hexW = tw;
//		float vs = hexH * .75f;
		float vs = hexH * .722f;
//		float avs = (float)(Math.sqrt(3) /2  * hexW);

		for (int x = width -1; x >= 0; x--) {
			for (int y = height -1; y >= 0; y--) {
				float ox = y % 2 == 0?.5f:0;
				renderer.setColor(0, ox, .5f, .25f);
				hex(renderer, (x + ox) * hexW + hexW * .5f, y * vs + hexH * .5f, s);
			}
		}

		if (overTile != null) {
			renderer.setColor(1, 1, 0, .5f);
			hex(renderer, overTile.x, overTile.y, s);
		}
		if (firstTile != null) {
			renderer.setColor(1, 0, 0, .5f);
			hex(renderer, firstTile.x, firstTile.y, s);
		}
		if (secondTile != null) {
			renderer.setColor(0, 1, 0, .5f);
			hex(renderer, secondTile.x, secondTile.y, s);
		}
//		renderer.rect(0, 0, 1, 1);

		renderer.end();
	}

	private void hex (ShapeRenderer renderer, float x, float y, float size) {
		float angle = 60 * 6 + 30;
		float px = x + size * MathUtils.cosDeg(angle);
		float py = y + size * MathUtils.sinDeg(angle);
		for (int i = 0; i <= 6; i++) {
			angle = 60 * i  + 30;
			float ex = x + size * MathUtils.cosDeg(angle);
			float	ey = y + size * MathUtils.sinDeg(angle);
			renderer.triangle(x, y, px, py, ex, ey);
			px = ex;
			py = ey;
		}
		renderer.setColor(0, 0, 0, 1f);
		renderer.circle(x, y, .05f, 6);
		renderer.setColor(1, 0, 0, .5f);
		renderer.rect(x - size, y - .2f, size * 2, .4f);
		renderer.setColor(0, 1, 0, .5f);
		renderer.rect(x - .2f, y - size, .4f, size * 2);
	}

	protected static class SelectedTile {
		TiledMapTile tile;
		float x, y;
		int gx, gy;
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
	}

	@Override public void dispose () {
		super.dispose();
		map.dispose();
		mapRenderer.dispose();
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		super.mouseMoved(screenX, screenY);
		overTile = worldToHex(cs.x, cs.y);
		return false;
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		switch (button) {
		case Input.Buttons.LEFT: {
			firstTile = worldToHex(cs.x, cs.y);
		} return true;
		case Input.Buttons.RIGHT: {
			secondTile = worldToHex(cs.x, cs.y);
		} return true;
		}
		return false;
	}

	private SelectedTile worldToHex (float x, float y) {
		SelectedTile tile = new SelectedTile();
		int width = layer.getWidth();
		int height = layer.getHeight();
		// this should be centre of the tile
		tile.x = x;
		tile.y = y;
		return tile;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		return false;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		switch (button) {
		case Input.Buttons.LEFT: {

		} return true;
		case Input.Buttons.RIGHT: {

		} return true;
		}
		return false;
	}

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + 0.1f * amount, .1f, 2.5f);
		gameCamera.update();
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.SPACE: {
			gameCamera.zoom = 1;
			gameCamera.position.set((layer.getWidth() * layer.getTileWidth())/2 * INV_SCALE, (layer.getHeight() * layer.getTileHeight() * .75f)/2 * INV_SCALE, 0);
			gameCamera.update();
		} return true;
		}
		return super.keyDown(keycode);
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, HexTest.class);
	}

	static class MyHexagonalTiledMapRenderer extends BatchTiledMapRenderer {

		/**
		 * true for X-Axis, false for Y-Axis
		 */
		private boolean staggerAxisX = true;
		/**
		 * true for even StaggerIndex, false for odd
		 */
		private boolean staggerIndexEven = false;
		/**
		 * the parameter defining the shape of the hexagon from tiled. more specifically it represents the length of the sides that
		 * are parallel to the stagger axis. e.g. with respect to the stagger axis a value of 0 results in a rhombus shape, while a
		 * value equal to the tile length/height represents a square shape and a value of 0.5 represents a regular hexagon if tile
		 * length equals tile height
		 */
		private float hexSideLength = 0f;

		public MyHexagonalTiledMapRenderer (TiledMap map) {
			super(map);
			init(map);
		}

		public MyHexagonalTiledMapRenderer (TiledMap map, float unitScale) {
			super(map, unitScale);
			init(map);
		}

		public MyHexagonalTiledMapRenderer (TiledMap map, Batch batch) {
			super(map, batch);
			init(map);
		}

		public MyHexagonalTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
			super(map, unitScale, batch);
			init(map);
		}

		private void init (TiledMap map) {
			String axis = map.getProperties().get("staggeraxis", String.class);
			if (axis != null) {
				if (axis.equals("x")) {
					staggerAxisX = true;
				} else {
					staggerAxisX = false;
				}
			}

			String index = map.getProperties().get("staggerindex", String.class);
			if (index != null) {
				if (index.equals("even")) {
					staggerIndexEven = true;
				} else {
					staggerIndexEven = false;
				}
			}

			Integer length = map.getProperties().get("hexsidelength", Integer.class);
			if (length != null) {
				hexSideLength = length.intValue();
			} else {
				if (staggerAxisX) {
					length = map.getProperties().get("tilewidth", Integer.class);
					if (length != null) {
						hexSideLength = 0.5f * length.intValue();
					} else {
						TiledMapTileLayer tmtl = (TiledMapTileLayer)map.getLayers().get(0);
						hexSideLength = 0.5f * tmtl.getTileWidth();
					}
				} else {
					length = map.getProperties().get("tileheight", Integer.class);
					if (length != null) {
						hexSideLength = 0.5f * length.intValue();
					} else {
						TiledMapTileLayer tmtl = (TiledMapTileLayer)map.getLayers().get(0);
						hexSideLength = 0.5f * tmtl.getTileHeight();
					}
				}
			}
		}

		@Override public void renderTileLayer (TiledMapTileLayer layer) {
			final Color batchColor = batch.getColor();
			final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

			final int layerWidth = layer.getWidth();
			final int layerHeight = layer.getHeight();

			final float layerTileWidth = layer.getTileWidth() * unitScale;
			final float layerTileHeight = layer.getTileHeight() * unitScale;

			final float layerHexLength = hexSideLength * unitScale;

			if (staggerAxisX) {
				final float tileWidthLowerCorner = (layerTileWidth - layerHexLength) / 2;
				final float tileWidthUpperCorner = (layerTileWidth + layerHexLength) / 2;
				final float layerTileHeight50 = layerTileHeight * 0.50f;

				final int row1 = Math.max(0, (int)((viewBounds.y - layerTileHeight50) / layerTileHeight));
				final int row2 = Math.min(layerHeight, (int)((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

				final int col1 = Math.max(0, (int)(((viewBounds.x - tileWidthLowerCorner) / tileWidthUpperCorner)));
				final int col2 = Math.min(layerWidth, (int)((viewBounds.x + viewBounds.width + tileWidthUpperCorner) / tileWidthUpperCorner));

				// depending on the stagger index either draw all even before the odd or vice versa
				final int colA = (staggerIndexEven == (col1 % 2 == 0)) ? col1 + 1 : col1;
				final int colB = (staggerIndexEven == (col1 % 2 == 0)) ? col1 : col1 + 1;

				for (int row = row2 - 1; row >= row1; row--) {
					for (int col = colA; col < col2; col += 2) {
						renderCell(layer.getCell(col, row), tileWidthUpperCorner * col, layerTileHeight50 + (layerTileHeight * row),
							color);
					}
					for (int col = colB; col < col2; col += 2) {
						renderCell(layer.getCell(col, row), tileWidthUpperCorner * col, layerTileHeight * row, color);
					}
				}
			} else {
				final float tileHeightLowerCorner = (layerTileHeight - layerHexLength) / 2;
				final float tileHeightUpperCorner = (layerTileHeight + layerHexLength) / 2;
				final float layerTileWidth50 = layerTileWidth * 0.50f;

				final int row1 = Math.max(0, (int)(((viewBounds.y - tileHeightLowerCorner) / tileHeightUpperCorner)));
				final int row2 = Math.min(layerHeight, (int)((viewBounds.y + viewBounds.height + tileHeightUpperCorner) / tileHeightUpperCorner));

				final int col1 = Math.max(0, (int)(((viewBounds.x - layerTileWidth50) / layerTileWidth)));
				final int col2 = Math.min(layerWidth, (int)((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

				float shiftX = 0;
				for (int row = row2 - 1; row >= row1; row--) {
					// depending on the stagger index either shift for even or uneven indexes
					if ((row % 2 == 0) == staggerIndexEven)
						shiftX = 0;
					else
						shiftX = layerTileWidth50;
					for (int col = col1; col < col2; col++) {
						renderCell(layer.getCell(col, row), layerTileWidth * col + shiftX, tileHeightUpperCorner * row, color);
					}
				}
			}
		}

		/**
		 * render a single cell
		 */
		private void renderCell (final TiledMapTileLayer.Cell cell, final float x, final float y, final float color) {
			if (cell != null) {
				final TiledMapTile tile = cell.getTile();
				if (tile != null) {
					if (tile instanceof AnimatedTiledMapTile)
						return;

					final boolean flipX = cell.getFlipHorizontally();
					final boolean flipY = cell.getFlipVertically();
					final int rotations = cell.getRotation();

					TextureRegion region = tile.getTextureRegion();

					float x1 = x + tile.getOffsetX() * unitScale;
					float y1 = y + tile.getOffsetY() * unitScale;
					float x2 = x1 + region.getRegionWidth() * unitScale;
					float y2 = y1 + region.getRegionHeight() * unitScale;

					float u1 = region.getU();
					float v1 = region.getV2();
					float u2 = region.getU2();
					float v2 = region.getV();

					vertices[X1] = x1;
					vertices[Y1] = y1;
					vertices[C1] = color;
					vertices[U1] = u1;
					vertices[V1] = v1;

					vertices[X2] = x1;
					vertices[Y2] = y2;
					vertices[C2] = color;
					vertices[U2] = u1;
					vertices[V2] = v2;

					vertices[X3] = x2;
					vertices[Y3] = y2;
					vertices[C3] = color;
					vertices[U3] = u2;
					vertices[V3] = v2;

					vertices[X4] = x2;
					vertices[Y4] = y1;
					vertices[C4] = color;
					vertices[U4] = u2;
					vertices[V4] = v1;

					if (flipX) {
						float temp = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = temp;
						temp = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = temp;
					}
					if (flipY) {
						float temp = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = temp;
						temp = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = temp;
					}
					if (rotations == 2) {
						float tempU = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = tempU;
						tempU = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = tempU;
						float tempV = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = tempV;
						tempV = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = tempV;
					}
					batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
				}
			}
		}
	}
}
