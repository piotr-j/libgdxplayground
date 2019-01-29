package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIShaderTest extends BaseScreen {
	private final static String TAG = UIShaderTest.class.getSimpleName();
	private Texture texture;
	private ActorShaderRenderer shaderRenderer;
	private ShaderProgram saturationShader;
	private ShaderProgram outlineShader;

	public UIShaderTest (GameReset game) {
		super(game);
		saturationShader = new ShaderProgram(
			Gdx.files.internal("shaders/saturation.vert"), Gdx.files.internal("shaders/saturation.frag"));
		if (!saturationShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + saturationShader.getLog());
		}

		outlineShader = new ShaderProgram(Gdx.files.internal("shaders/outline.vert"), Gdx.files.internal("shaders/outline.frag"));
		if (!outlineShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + outlineShader.getLog());
		}

		outlineShader.begin();
		outlineShader.setUniformf("u_viewportInverse", 1f / Gdx.graphics.getWidth(), 1f / Gdx.graphics.getHeight());
		outlineShader.setUniformf("u_thickness", 8);
		outlineShader.setUniformf("u_step", Math.min(1f, Gdx.graphics.getWidth() / 70f));
		outlineShader.setUniformf("u_color", Color.CYAN.r, Color.CYAN.g, Color.CYAN.b);
		outlineShader.end();

		shaderRenderer = new ActorShaderRenderer();
		shaderRenderer.debug = true;

		Table container = new Table();
		root.add(container).expand(1, 1);
		root.add().expand(2, 1);
//		texture = new Texture("badlogic.jpg");
//		texture = new Texture("shaders/rect-128.png");
		texture = new Texture("puncher/punch.png");
		float size = 128;
		{
			Image image = new SimpleShaderImage(new TextureRegionDrawable(new TextureRegion(texture)), outlineShader);
			addActions(image, size);
			container.add(image).size(size).row();
		}
		container.add().height(Value.percentHeight(.05f, root)).row();
		{
			Image image = new ShaderImage(new TextureRegionDrawable(new TextureRegion(texture)), outlineShader, shaderRenderer);
			addActions(image, size);
			container.add(image).size(size).row();
		}
		container.add().height(Value.percentHeight(.05f, root)).row();
		{
			Image image = new Image(new TextureRegion(texture));
			addActions(image, size);
			container.add(image).size(size).row();
		}

//		root.debugAll();

		clear.set(.5f, .5f, .5f, 1);
	}

	Array<Actor> debugActors = new Array<>();
	private void addActions (Image image, float size) {
		debugActors.add(image);
		image.setSize(size, size);
		image.setScaling(Scaling.fit);
		image.setOrigin(Align.center);
		image.setAlign(Align.center);
		image.addAction(Actions.forever(Actions.parallel(
			Actions.sequence(
				Actions.scaleTo(.5f, .5f, 2),
				Actions.scaleTo(1.5f, 1.5f, 2)
			),
			Actions.rotateBy(360, 4),
			Actions.sequence(
				Actions.moveBy(size/2, 0, 1),
				Actions.moveBy(-size, 0, 2),
				Actions.moveBy(size/2, 0, 1)
			),
			Actions.sequence(
				Actions.color(new Color(1, 1, 1, .5f), 2),
				Actions.color(Color.WHITE, 2)
			)
		)));
		image.debug();
	}

	private boolean paused = false;
	private boolean forward = false;
	private boolean backward = false;
	private Vector2 v2 = new Vector2();
	@Override public void render (float delta) {
		super.render(delta);
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			paused = !paused;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			backward = true;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			forward = true;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			shaderRenderer.debug = !shaderRenderer.debug;
		}
		if (!paused || forward || backward) {
			if (backward) { // close enough
				stage.act(-delta);
			} else {
				stage.act(delta);
			}
			forward = false;
			backward = false;
		}
		shaderRenderer.begin();
		stage.draw();
		shaderRenderer.end();
		renderer.setProjectionMatrix(guiCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.setColor(Color.MAGENTA);
		for (Actor actor : debugActors) {
			actor.localToStageCoordinates(v2.set(actor.getOriginX(), actor.getOriginY()));
			renderer.line(v2.x - 10, v2.y, v2.x + 10, v2.y);
			renderer.line(v2.x, v2.y - 10, v2.x, v2.y + 10);
		}
		renderer.end();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	private static class SimpleShaderImage extends Image {

		private final ShaderProgram shader;

		public SimpleShaderImage (Drawable drawable, ShaderProgram shader) {
			super(drawable);
			this.shader = shader;
		}

		float saturation = 0;
		@Override public void act (float delta) {
			super.act(delta);
			saturation += delta;

		}

		@Override public void draw (Batch batch, float parentAlpha) {
			ShaderProgram old = batch.getShader();
			// this will flush the batch even if its the same shader
			// but we probably want that anyway, since we probably have per object uniforms
			batch.setShader(shader);

			if (true) {
				shader.setUniformf("u_viewportInverse", 1f / Gdx.graphics.getWidth(), 1f / Gdx.graphics.getHeight());
				shader.setUniformf("u_thickness", 8);
				shader.setUniformf("u_step", Math.min(1f, Gdx.graphics.getWidth() / 70f));
				shader.setUniformf("u_color", Color.CYAN.r, Color.CYAN.g, Color.CYAN.b);
			}

//			shader.setUniformf("u_saturation", 1.5f + MathUtils.sin(saturation) * 1.5f);
			super.draw(batch, parentAlpha);
			batch.setShader(old);
			if (true) { // if the shader replaces source...
				super.draw(batch, parentAlpha);
			}
		}
	}

	private static class ShaderImage extends Image implements ActorShaderRenderer.ShaderActor {

		private final ShaderProgram shader;
        private final ActorShaderRenderer shaderRenderer;

        public ShaderImage (Drawable drawable, ShaderProgram shader, ActorShaderRenderer shaderRenderer) {
			super(drawable);
			this.shader = shader;
            this.shaderRenderer = shaderRenderer;
        }

		float saturation = 0;
		@Override public void act (float delta) {
			super.act(delta);
			saturation += delta;

		}

		@Override public void draw (Batch batch, float parentAlpha) {
		    shaderRenderer.draw(this, batch, parentAlpha);
		}

        @Override public void shaderDraw (Batch batch, float parentAlpha, boolean useTransform) {
            super.draw(batch, parentAlpha);
        }

		@Override public ShaderProgram shader () {
			return shader;
		}

		@Override public void init (ShaderProgram shader, int width, int height, int pass) {
			if (true) {
				shader.setUniformf("u_viewportInverse", 1f / width, 1f / height);
				shader.setUniformf("u_thickness", .2f);
				shader.setUniformf("u_step", Math.min(.1f, width / 70f));
				shader.setUniformf("u_color", Color.CYAN.r, Color.CYAN.g, Color.CYAN.b);
			}
//			shader.setUniformf("u_saturation", 1.5f + MathUtils.sin(saturation) * 1.5f);
		}

		@Override public int passes () {
			return 1;
		}

		@Override public void margins (ActorShaderRenderer.Margins out) {
			int margin = (int)(getWidth() * .1f);
			out.set(margin);
        }
    }

	private static class ActorShaderRenderer {
		FrameBuffer fbo;
		TextureRegion region;
		ScreenViewport viewport;
		OrthographicCamera camera;

		final int size = 2048;
		boolean debug = false;
		ShapeRenderer shapes;

		public ActorShaderRenderer () {
			viewport = new ScreenViewport(camera = new OrthographicCamera());
			resize(size, size);
			shapes = new ShapeRenderer();
		}

		void resize (final int width, final int height) {
			if (fbo != null) fbo.dispose();
			fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false) {
				@Override protected void setFrameBufferViewport () {
//					super.setFrameBufferViewport();
//					int vpWidth = MathUtils.nextPowerOfTwo(fboWidth);
//					int vpHeight = MathUtils.nextPowerOfTwo(fboHeight);
//					Gdx.gl20.glViewport(0, 0, vpWidth, vpHeight);
					Gdx.gl20.glViewport(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			};
//			fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
			fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			region = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
			region.flip(false, true);
		}

		private boolean inFbo;
		private Margins out = new Margins();
		private Matrix4 prevProjection = new Matrix4();
		private Matrix4 prevTransform = new Matrix4();

		private IntRect bounds = new IntRect();

		public void begin () {
			bounds.x = 0;
			bounds.y = 0;
			bounds.width = 100;
			bounds.height = 100;
		}

		public void end () {

		}
		/**
		 * Draw the actor
		 *
		 * If alpha != 1, offscreen buffer will be used that handles pma
		 */
		public void draw (Actor actor, Batch batch, float parentAlpha) {
			if (!(actor instanceof ShaderActor)) {
				return;
			}
			ShaderActor sa = (ShaderActor)actor;

			Color color = actor.getColor();
			// we need to use custom path only when semi transparent
			if (!inFbo) {
				// we cant nest, easily
				inFbo = true;
				batch.flush();

				// cant peek, explodes if there isnt one
				Rectangle svp = ScissorStack.getViewport();
				boolean scissors = svp.x != 0 || svp.y != 0 || svp.width != Gdx.graphics.getWidth() || svp.height != Gdx.graphics.getHeight();
				if (scissors) Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

				final Margins margins = out;
				margins.reset();
				sa.margins(margins);


				bounds.x = 0;
				bounds.y = 0;

				final int width = MathUtils.round(actor.getWidth()) + margins.left + margins.right;
				final int height = MathUtils.round(actor.getHeight()) + margins.top + margins.bottom;
				bounds.width = width;
				bounds.height = height;

				// multi pass shaders are tricky...
				// we need 2 ping pong buffers, draw result (last ine) to target (screen?)
				// we can get screen bounds with actor.localToScreenCoordinates()
				// expand them to for any extras i guess?
				// move camera, draw stuff
				FrameBuffers.begin(fbo);

				camera.position.x = bounds.x + width/2f - margins.left;
				camera.position.y = bounds.y + height/2f - margins.bottom;
				viewport.update(width, height, false);

				if (debug) {
					Gdx.gl.glClearColor(1, 0, 1, .5f);
				} else {
					Gdx.gl.glClearColor(0, 0, 0, 0);
				}
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

				prevProjection.set(batch.getProjectionMatrix());
				batch.setProjectionMatrix(camera.combined);

				float a = color.a;
				color.a = 1;
				// need to update viewport so clip works
				Viewport prevViewport = actor.getStage().getViewport();
				actor.getStage().setViewport(viewport);
				// this feels overly complicated, but seems to work
				// to screen coos?
				float x = actor.getX();
				float y = actor.getY();
				float sx = actor.getScaleX();
				float sy = actor.getScaleY();
				float rotation = actor.getRotation();

				prevTransform.set(batch.getTransformMatrix());
				batch.setTransformMatrix(batch.getTransformMatrix().idt());

				actor.setPosition(bounds.x, bounds.y);
				actor.setScale(1f, 1f);
				actor.setRotation(0);

				sa.shaderDraw(batch, 1, false);

				actor.setScale(sx, sy);
				actor.setPosition(x, y);
				actor.setRotation(rotation);
				actor.getStage().setViewport(prevViewport);
				color.a = a;
				// reset the color just in case
				batch.setColor(Color.WHITE);

//				batch.end();

				if (false) {
					shapes.setProjectionMatrix(camera.combined);
					shapes.begin(ShapeRenderer.ShapeType.Filled);
					shapes.setColor(0, 1, 1, .5f);
					rect(shapes, bounds.x, bounds.y, bounds.width, bounds.height, 4);
//				shapes.rect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
//				shapes.rect(bounds.x, bounds.y, 3, bounds.height);
//				shapes.rect(bounds.x, bounds.y, bounds.width, 3);
//				shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
//				shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
					shapes.end();

				}
				FrameBuffers.end();

				// restore if it was enabled
				if (scissors) Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

				// flipped on y
				region.setRegion(bounds.x, bounds.y + height, width, -height);

				batch.setProjectionMatrix(prevProjection);
				batch.setTransformMatrix(prevTransform);

				float alpha = a * parentAlpha;
				batch.setColor(color.r, color.g, color.b, alpha);
                ShaderProgram oldShader = batch.getShader();
				int passes = sa.passes();
				// if > 1 we need to draw to another buffer first
				ShaderProgram shader = sa.shader();
				batch.setShader(shader);
				batch.flush();
				for (int i = 0; i < passes; i++) {

//                sa.init(shader, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					sa.init(shader, width, height, i);
//                sa.init(shader, 4096, 4096);

					// draw the region from fbo
					batch.draw(region,
						x - margins.left,
						y - margins.bottom,
						margins.left + actor.getOriginX(),
						margins.bottom + actor.getOriginY(),
						width, height,
						sx, sy,
						actor.getRotation()
					);
					batch.flush();
				}

				batch.setColor(Color.WHITE);
                batch.setShader(oldShader);
				inFbo = false;
			} else {
				if (true) throw new AssertionError("welp");
                ShaderProgram oldShader = batch.getShader();
				ShaderProgram shader = sa.shader();
				batch.setShader(shader);
				sa.init(shader, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
				sa.shaderDraw(batch, parentAlpha, true);
                batch.setShader(oldShader);
			}

			if (debug) {
				region.setRegion(0, size, size, -size);
				batch.setColor(Color.WHITE);
//				batch.draw(region, 450, 400, size, size);
				batch.draw(region, 450, 0, size, size);
			}
		}

		private void rect (ShapeRenderer shapes, int x, int y, int width, int height, int thickness) {
			shapes.rect(x, y, width, thickness);
			shapes.rect(x, y + height - thickness * 4, width, thickness);

//			shapes.rect(x, y + thickness, thickness, height - thickness * 2);
//			shapes.rect(x + width - thickness, y + thickness, thickness, height - thickness * 2);
			shapes.setColor(0, 1, 0, .5f);
			shapes.rect(x, y, width/2, height/2);

			shapes.setColor(0, 0, 1, .5f);
			shapes.rect(x-width/2, y-width/2, width/2, height/2);
		}

		static class IntRect {
			public int x, y, width, height;
		}

        public static class Margins {
			public int top;
			public int left;
			public int bottom;
			public int right;

			Margins () {
				reset();
			}

			public Margins set(int margin) {
				this.top = margin;
				this.left = margin;
				this.bottom = margin;
				this.right = margin;
				return this;
			}

			public Margins set(int top, int left, int bottom, int right) {
				this.top = top;
				this.left = left;
				this.bottom = bottom;
				this.right = right;
				return this;
			}

			public void reset () {
				top = 4;
				left = 4;
				right = 4;
				bottom = 4;
			}
		}

		public interface ShaderActor {
			void shaderDraw (Batch batch, float parentAlpha, boolean useTransform);

			void margins (Margins out);

			ShaderProgram shader ();

			void init (ShaderProgram shader, int width, int height, int pass);

			int passes ();
		}
	}



	private static class FrameBuffers {
		private static Array<FrameBuffer> fbos = new Array<>();

		public static void begin (FrameBuffer fbo) {
			if (fbos.size > 0) {
				fbos.get(fbos.size - 1).end();
			}
			fbos.add(fbo);
			fbo.begin();
		}

		public static void end () {
			fbos.pop().end();
			if (fbos.size > 0) {
				fbos.get(fbos.size - 1).begin();
			}
		}
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
//		config.width /= 2;
//		config.height /= 2;
		PlaygroundGame.start(args, config, UIShaderTest.class);
	}
}
