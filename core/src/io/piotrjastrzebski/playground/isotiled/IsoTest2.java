package io.piotrjastrzebski.playground.isotiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricStaggeredTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.*;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import static com.badlogic.gdx.graphics.g2d.Batch.*;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;

/**
 * Created by EvilEntity on 07/06/2015.
 */
public class IsoTest2 extends BaseScreen {
	MyIsometricStaggeredTiledMapRenderer mapRenderer;
	Matrix4 isoTransform;
	Matrix4 invIsotransform;
	Polygon tile;
	TiledMapTileLayer mapLayer;
	public IsoTest2 (GameReset game) {
		super(game);
		TiledMap map = new AtlasTmxMapLoader().load("tiled/test_render.tmx");

		mapRenderer = new MyIsometricStaggeredTiledMapRenderer(map, INV_SCALE, batch);
		mapLayer = (TiledMapTileLayer)map.getLayers().get(0);


		tile = new Polygon();
		tile.setVertices(new float[]{0, 0.5f, 1f, 0f, 2f, 0.5f, 1f, 1f});

		// create the isometric transform
		isoTransform = new Matrix4();
		isoTransform.idt();

		// isoTransform.translate(0, 32, 0);
		float sqrt = (float)(Math.sqrt(2.0)/2.0);
		isoTransform.scale(sqrt, sqrt / 2.0f, 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

		// ... and the inverse matrix
		invIsotransform = new Matrix4(isoTransform);
		invIsotransform.inv();

		gameCamera.position.x += 10;
		gameCamera.update();
	}

	@Override public void render (float delta) {
		super.render(delta);
		mapRenderer.setView(gameCamera);
		mapRenderer.render();

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.circle(cs.x, cs.y, 0.1f, 16);
		renderer.setColor(Color.CYAN);

		// busted :d
		int x = MathUtils.floor((isoPos.y)/2f - .5f);
		int y = MathUtils.floor((isoPos.x)/2f + .5f);
		TiledMapTileLayer.Cell cell = mapLayer.getCell(x, y);
		if (cell != null) {
			Gdx.app.log("x", x + ", " + y);
			float x1 = y + x;
			float y2 = x * .5f - y * .5f;

			tile.setPosition(x1, y2);
			renderer.setColor(Color.RED);
			renderer.polygon(tile.getTransformedVertices());
		}

		renderer.end();
	}

	private Vector3 isoPos = new Vector3();
	private Vector3 translateWorldToIso (Vector2 vec) {
		isoPos.set(vec.x, vec.y, 0);
		isoPos.mul(invIsotransform);
		return isoPos;
	}

	@Override public void dispose () {
		super.dispose();
		mapRenderer.getMap().dispose();
	}

	@Override public boolean mouseMoved (int screenX, int screenY) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		translateWorldToIso(cs);
		return super.mouseMoved(screenX, screenY);
	}

	Vector2 cs = new Vector2();
	Vector3 temp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		cs.set(temp.x, temp.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		// dumb, dont do this!
		gameCamera.unproject(temp.set(screenX, screenY, 0));
		temp.sub(gameCamera.position).scl(0.1f);
		gameCamera.position.add(temp.x, temp.y, 0);
		gameCamera.update();
		return true;
	}

	@Override public boolean scrolled (int amount) {
		gameCamera.zoom = MathUtils.clamp(gameCamera.zoom + gameCamera.zoom * amount * 0.1f, 0.1f, 3f);
		gameCamera.update();
		return true;
	}

	private static class MyIsometricStaggeredTiledMapRenderer extends BatchTiledMapRenderer {

		public MyIsometricStaggeredTiledMapRenderer (TiledMap map) {
			super(map);
		}

		public MyIsometricStaggeredTiledMapRenderer (TiledMap map, Batch batch) {
			super(map, batch);
		}

		public MyIsometricStaggeredTiledMapRenderer (TiledMap map, float unitScale) {
			super(map, unitScale);
		}

		public MyIsometricStaggeredTiledMapRenderer (TiledMap map, float unitScale, Batch batch) {
			super(map, unitScale, batch);
		}

		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {
			final Color batchColor = batch.getColor();
			final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

			final int layerWidth = layer.getWidth();
			final int layerHeight = layer.getHeight();

			final float layerTileWidth = layer.getTileWidth() * unitScale;
			final float layerTileHeight = layer.getTileHeight() * unitScale;

			final float layerTileWidth50 = layerTileWidth * 0.50f;
			final float layerTileHeight50 = layerTileHeight * 0.50f;

			final int minX = Math.max(0, (int)(((viewBounds.x - layerTileWidth50) / layerTileWidth)));
			final int maxX = Math.min(layerWidth,
				(int)((viewBounds.x + viewBounds.width + layerTileWidth + layerTileWidth50) / layerTileWidth));

			final int minY = Math.max(0, (int)(((viewBounds.y - layerTileHeight) / layerTileHeight)));
			final int maxY = Math.min(layerHeight, (int)((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight50));

			for (int y = maxY - 1; y >= minY; y--) {
				float offsetX = (y % 2 == 1) ? layerTileWidth50 : 0;
				for (int x = maxX - 1; x >= minX; x--) {
					final TiledMapTileLayer.Cell cell = layer.getCell(x, y);
					if (cell == null) continue;
					final TiledMapTile tile = cell.getTile();

					if (tile != null) {
						final boolean flipX = cell.getFlipHorizontally();
						final boolean flipY = cell.getFlipVertically();
						final int rotations = cell.getRotation();
						TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion)tile.getTextureRegion();

						float x1 = x * layerTileWidth - offsetX + tile.getOffsetX() * unitScale + region.offsetX * unitScale;
						float y1 = y * layerTileHeight50 + tile.getOffsetY() * unitScale + region.offsetY * unitScale;
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

						if (rotations != 0) {
							switch (rotations) {
							case TiledMapTileLayer.Cell.ROTATE_90: {
								float tempV = vertices[V1];
								vertices[V1] = vertices[V2];
								vertices[V2] = vertices[V3];
								vertices[V3] = vertices[V4];
								vertices[V4] = tempV;

								float tempU = vertices[U1];
								vertices[U1] = vertices[U2];
								vertices[U2] = vertices[U3];
								vertices[U3] = vertices[U4];
								vertices[U4] = tempU;
								break;
							}
							case TiledMapTileLayer.Cell.ROTATE_180: {
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
								break;
							}
							case TiledMapTileLayer.Cell.ROTATE_270: {
								float tempV = vertices[V1];
								vertices[V1] = vertices[V4];
								vertices[V4] = vertices[V3];
								vertices[V3] = vertices[V2];
								vertices[V2] = tempV;

								float tempU = vertices[U1];
								vertices[U1] = vertices[U4];
								vertices[U4] = vertices[U3];
								vertices[U3] = vertices[U2];
								vertices[U2] = tempU;
								break;
							}
							}
						}
						batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
					}
				}
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, IsoTest2.class);
	}
}
