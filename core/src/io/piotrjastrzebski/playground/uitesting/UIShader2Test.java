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
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIShader2Test extends BaseScreen {
	private final static String TAG = UIShader2Test.class.getSimpleName();
	private Texture texture;
	private ActorShaderRenderer shaderRenderer;
	private ShaderProgram saturationShader;
	private ShaderProgram outlineShader;
	private ShaderProgram blurShader;

	public UIShader2Test (GameReset game) {
		super(game);
		saturationShader = new ShaderProgram(
			Gdx.files.internal("shaders/saturation.vert"), Gdx.files.internal("shaders/saturation.frag"));
		if (!saturationShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + saturationShader.getLog());
		}

		outlineShader = new ShaderProgram(Gdx.files.internal("shaders/outline2.vert"), Gdx.files.internal("shaders/outline2.frag"));
		if (!outlineShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + outlineShader.getLog());
		}

		blurShader = new ShaderProgram(Gdx.files.internal("shaders/gblur2.vert"), Gdx.files.internal("shaders/gblur2.frag"));
		if (!blurShader.isCompiled()) {
			throw new AssertionError(TAG + " : Shader not compiled!\n" + blurShader.getLog());
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
		texture = new Texture("shaders/badlogic.png");
		float size = 128;
		container.add().height(Value.percentHeight(.05f, root)).row();
		{
			Image image = new ShaderImage(new TextureRegionDrawable(new TextureRegion(texture)), blurShader, shaderRenderer);
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

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		shaderRenderer.resize(width, height);
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
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

        @Override public void shaderDraw (Batch batch, float parentAlpha, int pass) {
            super.draw(batch, parentAlpha);
        }

		@Override public ShaderProgram shader () {
			return shader;
		}

		@Override public void init (ShaderProgram shader, int width, int height, int pass) {
			if (true) {
				if (pass >= 0) {
					shader.setUniformf("u_resolution", width);
					shader.setUniformf("u_radius", 1f);
					if (pass == 0) {
						shader.setUniformf("u_dir", 0, 1);
					} else {
						shader.setUniformf("u_dir", 1, 0);
					}
				}
			}
			if (false) {
				if (pass == 0) {
					shader.setUniformf("u_viewportInverse", 1f / width, 1f / height);
					shader.setUniformf("u_thickness", 1 * getScaleX());
					shader.setUniformf("u_step", Math.min(.1f, width / 140f));
					shader.setUniformf("u_color", Color.CYAN.r, Color.CYAN.g, Color.CYAN.b);
				} else {
					shader.setUniformf("u_viewportInverse", 1f / width, 1f / height);
					shader.setUniformf("u_thickness", 2 * getScaleX());
					shader.setUniformf("u_step", Math.min(.1f, width / 140f));
					shader.setUniformf("u_color", Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b);
				}

			}
//			shader.setUniformf("u_saturation", 1.5f + MathUtils.sin(saturation) * 1.5f);
		}

		@Override public int passes () {
			return 2;
		}

		@Override public void margins (ActorShaderRenderer.Margins out) {
			int margin = (int)(getWidth() * .1f);
			out.set(margin);
        }
    }

	private static class ActorShaderRenderer {
		FrameBuffer fbo1;
		FrameBuffer fbo2;
		FrameBuffer fbo;
		TextureRegion region;
//		ScreenViewport viewport;
//		OrthographicCamera camera;

		boolean debug = false;
		ShapeRenderer shapes;

		public ActorShaderRenderer () {
//			viewport = new ScreenViewport(camera = new OrthographicCamera());
			resize(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
			shapes = new ShapeRenderer();
		}

		void resize (final int width, final int height) {
			if (fbo1 != null) fbo1.dispose();
			if (fbo2 != null) fbo2.dispose();
			fbo1 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
			fbo1.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			fbo2 = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
			fbo2.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			fbo = fbo1;
			region = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
			region.flip(false, true);
		}

		private boolean inFbo;
		private Margins out = new Margins();
		private Matrix4 prevProjection = new Matrix4();
		private Matrix4 prevTransform = new Matrix4();

//		private IntRect fboBounds = new IntRect();
		private Bounds actorBounds = new Bounds();

		public void begin () {
//			fboBounds.x = 0;
//			fboBounds.y = 0;
//			fboBounds.width = 100;
//			fboBounds.height = 100;
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
//			fboBounds.x = 0;
//			fboBounds.y = 0;
//			fboBounds.width = fbo.getWidth();
//			fboBounds.height = fbo.getHeight();

			sa.margins(out);

			actorBounds.set(actor, out);

			Bounds ab = this.actorBounds;

			// need to change viewport in stage as well for this to work...
//			fboBounds.x = actorBounds.aabb.x;
//			fboBounds.y = actorBounds.aabb.y;
//			fboBounds.width = actorBounds.aabb.width;
//			fboBounds.height = actorBounds.aabb.height;



			batch.end();
			Array<FrameBuffer> fbos = new Array<>();

			// 1. draw the actor to fbo with default shader
			float a = actor.getColor().a;
			actor.getColor().a = 1;
//			batch.flush();
			int passes = sa.passes();

			FrameBuffers.begin(fbo);
			if (debug) {
				Gdx.gl.glClearColor(1, 0, 1, .1f);
			} else {
				Gdx.gl.glClearColor(0, 0, 0, 0);
			}
//			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//			batch.setProjectionMatrix(batch.getProjectionMatrix().translate(-ab.aabb.x, -ab.aabb.y, 0));
			batch.setColor(Color.WHITE);
			// probably got to do this to avoid blending all the time
			// separate blend func probably a better idea...
			// tho not quite working

//			ShaderProgram prevShader = batch.getShader();
//			ShaderProgram shader = sa.shader();
//			batch.setShader(shader);
			batch.begin();

//			sa.init(shader, fbo.getWidth(), fbo.getHeight(), 0);
			sa.shaderDraw(batch, parentAlpha, -1);


//			batch.flush();
			batch.end();
//			batch.setShader(prevShader);

			if (debug) {
				shapes.setProjectionMatrix(batch.getProjectionMatrix());
				shapes.begin(ShapeRenderer.ShapeType.Line);


				shapes.setColor(Color.ORANGE);
				shapes.rect(ab.aabb.x, ab.aabb.y, ab.aabb.width, ab.aabb.height);

				shapes.setColor(Color.CYAN);
				shapes.line(ab.bl.x, ab.bl.y, ab.br.x, ab.br.y);
				shapes.line(ab.tl.x, ab.tl.y, ab.tr.x, ab.tr.y);
				shapes.line(ab.bl.x, ab.bl.y, ab.tl.x, ab.tl.y);
				shapes.line(ab.br.x, ab.br.y, ab.tr.x, ab.tr.y);

				shapes.end();
			}

			FrameBuffers.end();

			if (true) {
				batch.setColor(Color.WHITE);
//				batch.setProjectionMatrix(batch.getProjectionMatrix().translate(ab.aabb.x, ab.aabb.y, 0));
				batch.begin();
				region.setTexture(fbo.getColorBufferTexture());
				region.setRegion(0, fbo.getHeight(), fbo.getWidth(), -fbo.getHeight());
//				batch.draw(region, 450, 400, size, size);
//				batch.draw(region, 750, 0, fbo.getWidth()/2, fbo.getHeight()/2);
				batch.draw(region, 600, 0, fbo1.getWidth()/2, fbo1.getHeight()/2);
				batch.end();
//				batch.setProjectionMatrix(batch.getProjectionMatrix().translate(-ab.aabb.x, -ab.aabb.y, 0));
			}


			// 2. draw the result desired amount of times to fbos
			// super annoying if we need base stuff, we need shaders to not override source

			ShaderProgram prevShader = batch.getShader();

			ShaderProgram shader = sa.shader();
			batch.setShader(shader);
			batch.begin();
			while (--passes >= 0) {
				region.setTexture(fbo.getColorBufferTexture());
				region.setRegion(ab.aabb.x, ab.aabb.y, ab.aabb.width, ab.aabb.height);
//				region.setRegion(0, 0, ab.aabb.width, ab.aabb.height);
				swap();
				fbos.add(fbo);
				FrameBuffers.begin(fbo);
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//				batch.begin();
				sa.init(shader, fbo.getWidth(), fbo.getHeight(), passes);
				batch.draw(region, ab.aabb.x, ab.aabb.y + region.getRegionHeight(), region.getRegionWidth(), -region.getRegionHeight());
				batch.flush();
				FrameBuffers.end();

			}
			batch.setShader(prevShader);

			actor.getColor().a = a;
			batch.end();
			batch.begin();

//			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

//			batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
//			batch.disableBlending();
//			batch.setProjectionMatrix(batch.getProjectionMatrix().translate(ab.aabb.x, ab.aabb.y, 0));
			region.setTexture(fbo.getColorBufferTexture());
			region.setRegion(ab.aabb.x, ab.aabb.y, ab.aabb.width, ab.aabb.height);
//			region.setRegion(0, 0, ab.aabb.width, ab.aabb.height);
			batch.setColor(1, 1, 1, a);
			batch.draw(region, ab.aabb.x, ab.aabb.y + region.getRegionHeight(), region.getRegionWidth(), -region.getRegionHeight());
//			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//			batch.enableBlending();

			if (true) {
				int id = 1;
				batch.setColor(Color.WHITE);
				for (FrameBuffer fbo : fbos) {
					region.setTexture(fbo.getColorBufferTexture());
					region.setRegion(0, fbo.getHeight(), fbo.getWidth(), -fbo.getHeight());
//				batch.draw(region, 450, 400, size, size);
//				batch.draw(region, 750, 0, fbo.getWidth()/2, fbo.getHeight()/2);
					batch.draw(region, 600, fbo.getHeight()/3f * id, fbo.getWidth()/2f, fbo.getHeight()/2f);
					id++;
				}
//
//
//				region.setTexture(fbo2.getColorBufferTexture());
//				region.setRegion(0, fbo2.getHeight(), fbo2.getWidth(), -fbo2.getHeight());
//				batch.setColor(Color.WHITE);
////				batch.draw(region, 450, 400, size, size);
////				batch.draw(region, 750, 0, fbo.getWidth()/2, fbo.getHeight()/2);
//				batch.draw(region, 750, 2 * fbo2.getHeight()/3, fbo2.getWidth()/3, fbo2.getHeight()/3);
			}
		}

		private void swap () {
			if (fbo == fbo1) {
				fbo = fbo2;
			} else {
				fbo = fbo1;
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

		public static class Bounds {
			public Vector2 tl = new Vector2();
			public Vector2 tr = new Vector2();
			public Vector2 bl = new Vector2();
			public Vector2 br = new Vector2();
			private Rectangle faabb = new Rectangle();
			public IntRect aabb = new IntRect();

			public Bounds set(Actor actor, Margins margins) {
				float aw = actor.getWidth();
				float ah = actor.getHeight();
				tl.set(ah + margins.top, -margins.left);
				tr.set(ah + margins.top, aw + margins.right);
				bl.set(-margins.bottom, -margins.left);
				br.set(-margins.bottom, aw + margins.right);

				// do we do screen or stage?
				actor.localToStageCoordinates(tl);
				actor.localToStageCoordinates(tr);
				actor.localToStageCoordinates(bl);
				actor.localToStageCoordinates(br);

				faabb.set(tl.x, tl.y, 0, 0);
				faabb.merge(tr.x, tr.y);
				faabb.merge(bl);
				faabb.merge(br);
				aabb.x = MathUtils.floor(faabb.x);
				aabb.y = MathUtils.floor(faabb.y);
				aabb.width = MathUtils.ceil(faabb.width);
				aabb.height = MathUtils.ceil(faabb.height);
				return this;
			}
		}

		public interface ShaderActor {
			void shaderDraw (Batch batch, float parentAlpha, int pass);

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
		config.width /= 2;
		config.height /= 2;
		PlaygroundGame.start(args, config, UIShader2Test.class);
	}
}
