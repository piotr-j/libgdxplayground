package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTextButton;

/*

https://gist.github.com/xoppa/2978633678fa1c19cc47

More info
ShapeRenderer can be used for easily rendering 2D (and 3D) primitive shapes on the fly, e.g. for debugging.
However its vertex layout is not the same as e.g. SpriteBatch. This is because SpriteBatch allows for 2D
(only x and y, no z) sprites containing texture coordinates and a color, while ShapeRender allows for 3D
(x, y and z) shapes with a color but no texture coordinates. Therefor they both also use a different shader.

MeshBuilder is often used for creating basic 3D models and is often used as "create once, render many".
However, it can also be used "on the fly" (create the mesh every render call) and it does allow for any
combination of vertex attributes (including 2D shapes). It can therefor be used to create a mesh which is
compatible with SpriteBatch.

SpriteBatch does allow you to specify a custom mesh, but only if it consist out of quads (a multiple of four
vertices with six indices). PolygonSpriteBatch on the other hand can be used for more complex shapes and is
interchangeable with SpriteBatch. So by using PolygonSpriteBatch instead of SpriteBatch it is possible to
render primitive shapes built with MeshBuilder as part of your normal rendering (e.g. as part of your stage).
 */

public class ShapeRendererTest extends BaseScreen {
	PolygonShapeDrawer drawer;
	PolygonSpriteBatch batch;
	Actor ellipse;
	TextureRegion white;

	public ShapeRendererTest (PlaygroundGame game) {
		super(game);
		drawer = new PolygonShapeDrawer();
		batch = new PolygonSpriteBatch();

		// replace with stage with custom batch
		stage = new Stage(guiViewport, batch);
		stage.addActor(root);

		white = new TextureRegion(new Texture(Gdx.files.internal("white.png")));
		VisTextButton button = new VisTextButton("Behind the shape!");
		button.setPosition(80, 120);
		root.addActor(button);

		ellipse = new Actor() {
			@Override
			public void draw (Batch batch, float parentAlpha) {
				drawer.setTextureRegion(white);
				float w = getWidth(), h = getHeight();
				drawer.setColor(getColor());
				drawer.ellipse(w, h, w * 0.5f, h * 0.5f, 20, getX() + w * 0.5f, getY() + h * 0.5f, 0, 0, 0, -1);
				drawer.draw((PolygonSpriteBatch)batch);
			}
		};
		ellipse.setSize(100, 100);
		ellipse.setPosition(100, 100);
		ellipse.setColor(Color.YELLOW);
		root.addActor(ellipse);

		button = new VisTextButton("In front of the shape!");
		button.setPosition(80, 160);
		root.addActor(button);

		// replace processor
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
	}

	public class PolygonShapeDrawer extends MeshBuilder {
		private Texture texture;

		public PolygonShapeDrawer () {
			super();
			super.begin(
				new VertexAttributes(new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), VertexAttribute
					.ColorPacked(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);
		}

		@Override
		public Mesh end () {
			throw new GdxRuntimeException("Not supported!");
		}

		@Override
		public Mesh end (Mesh mesh) {
			throw new GdxRuntimeException("Not supported!");
		}

		public void setTextureRegion (TextureRegion region) {
			if (getNumIndices() > 0)
				throw new GdxRuntimeException("Cannot change the TextureRegion in while creating a shape, call draw first.");
			texture = region.getTexture();
			setUVRange(region);
		}

		public void draw (PolygonSpriteBatch batch) {
			if (texture == null)
				throw new GdxRuntimeException("No texture specified, call setTextureRegion before creating the shape");
			batch.draw(texture, getVertices(), 0, getNumVertices() * getFloatsPerVertex(), getIndices(), 0, getNumIndices());
			clear();
		}
	}
}
