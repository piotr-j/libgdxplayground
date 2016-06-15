package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

import java.util.Comparator;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class PuzzleTest extends BaseScreen {
	private static final String TAG = PuzzleTest.class.getSimpleName();
	private Texture image;
	private Jigsaw jigsaw;
	private PolygonSpriteBatch polyBatch;

	public PuzzleTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		polyBatch = new PolygonSpriteBatch();
		image = new Texture("badlogic.jpg");
		jigsaw = new Jigsaw(new TextureRegion(image), -8, -8, 16 ,16, 4, 4);
	}

	@Override public void render (float delta) {
		super.render(delta);
		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		polyBatch.setColor(Color.DARK_GRAY);
		polyBatch.draw(image, -8, -8, 16, 16);
		polyBatch.setColor(Color.WHITE);
		jigsaw.draw(polyBatch);
		polyBatch.end();

//		renderer.setProjectionMatrix(gameCamera.combined);
//		renderer.begin(ShapeRenderer.ShapeType.Line);
//		jigsaw.drawDebug(renderer);
//		renderer.end();
	}

	public static class Jigsaw {
		private TextureRegion region;
		private Rectangle bounds = new Rectangle();
		private Array<Piece> pieces = new Array<>();
		private IntArray ids = new IntArray();
		private int locked;

		public Jigsaw (TextureRegion region, float x, float y, float width, float height, int piecesX, int piecesY) {
			this.region = region;
			bounds.set(x, y, width, height);
			pieces.ensureCapacity(piecesX * piecesY);
			for (int i = 0; i < piecesX * piecesY; i++) {
				ids.add(-i);
			}
			float rWidth = region.getRegionWidth();
			float rHeight = region.getRegionHeight();
			float pieceWidth = rWidth/piecesX;
			float pieceHeight = rHeight/piecesY;
			for (int gx = 0; gx < piecesX; gx++) {
				for (int gy = 0; gy < piecesY; gy++) {
					Piece piece = new Piece();
					piece.z = ids.removeIndex(MathUtils.random(ids.size -1));
					piece.target.set(x, y);
					piece.size.x = MathUtils.random(x - width/3, x + width/3);
					piece.size.y = MathUtils.random(y - height/3, y + height/3);
					piece.size.width = width;
					piece.size.height = height;
					piece.bounds.x = piece.size.x + gx * width/piecesX;
					piece.bounds.y = piece.size.y + gy * height/piecesY;
					piece.bounds.width = width/piecesX;
					piece.bounds.height = height/piecesY;
					piece.offset.set(piece.bounds.x - piece.size.x, piece.bounds.y - piece.size.y);

					float ox = gx * pieceWidth;
					float oy = gy * pieceHeight;
					// TODO nice shapes
					float[] vertices = {
						ox, oy,
						ox, oy + pieceHeight,
						ox + pieceWidth, oy + pieceHeight,
						ox + pieceWidth, oy
					};
					piece.region = new PolygonRegion(region, vertices, new short[]{0,1,2,0,3,2});
					pieces.add(piece);
				}
//				break;
			}
			pieces.sort();
		}

		public void update(float delta) {

		}

		public void draw (PolygonSpriteBatch polyBatch) {
			for (Piece piece : pieces) {
				piece.draw(polyBatch);
			}
		}

		public void drawDebug (ShapeRenderer renderer) {
			for (Piece piece : pieces) {
				piece.drawDebug(renderer);
			}
		}

		public Piece getPieceAt (float x, float y) {
			for (Piece piece : pieces) {
				if (piece.bounds.contains(x, y) && !piece.locked) return piece;
			}
			return null;
		}

		protected Piece drag;
		protected Vector2 dragOffset = new Vector2();
		public void touchDown (float x, float y) {
			drag = getPieceAt(x, y);
			if (drag != null) {
				for (Piece piece : pieces) {
					piece.z--;
				}
				drag.z = 0;
				pieces.sort();
				dragOffset.set(drag.size.x, drag.size.y).sub(x, y);
			}
		}

		public void touchDrag (float x, float y) {
			if (drag != null) {
				drag.position(x + dragOffset.x, y + dragOffset.y);
			}
		}

		public void touchUp (float x, float y) {
			if (drag != null) {
				if (drag.target.epsilonEquals(drag.size.x, drag.size.y, .75f)) {
					drag.position(drag.target.x, drag.target.y);
					drag.locked = true;
					drag.tint.set(Color.LIGHT_GRAY);
					locked++;
					if (locked == pieces.size) {
						for (Piece piece : pieces) {
							piece.tint.set(Color.WHITE);
						}
					}
				}
			}
		}

		public static class Piece implements Comparable<Piece> {
			protected Color tint = new Color(Color.WHITE);
			protected boolean locked;
			protected Rectangle size = new Rectangle();
			protected Rectangle bounds = new Rectangle();
			protected PolygonRegion region;
			protected int z;
			protected Vector2 target = new Vector2();
			protected Vector2 offset = new Vector2();

			public void draw (PolygonSpriteBatch batch) {
				batch.setColor(tint);
				batch.draw(region, size.x, size.y, size.width, size.height);
			}

			public void drawDebug (ShapeRenderer renderer) {
				renderer.setColor(Color.YELLOW);
				renderer.rect(size.x, size.y, size.width, size.height);
				renderer.setColor(Color.CYAN);
				renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
			}

			@Override public int compareTo (Piece o) {
				return Integer.compare(z, o.z);
			}

			public void position (float x, float y) {
				size.x = x;
				size.y = y;
				bounds.x = x + offset.x;
				bounds.y = y + offset.y;
			}
		}
	}

	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		jigsaw.touchDown(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		jigsaw.touchDrag(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		jigsaw.touchUp(cs.x, cs.y);
		return true;
	}

	@Override public void dispose () {
		super.dispose();
		polyBatch.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, PuzzleTest.class);
	}
}
