package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Based on https://www.shadertoy.com/view/csX3RH
 * Video https://www.youtube.com/watch?v=6xf6aKaroR4
 */
public class PixelArtShaderV3Test extends BaseScreen {
	public final static String TAG = PixelArtShaderV3Test.class.getSimpleName();

	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/SCALE;
	public final static float VP_WIDTH = 1280 * INV_SCALE;
	public final static float VP_HEIGHT = 720 * INV_SCALE;
	private final static int TEX_1X = 0;
	private final static int TEX_2X = 1;
	private final static int TEX_4X = 2;
	private final static int TEX_8X = 3;
	private Array<Texture> textures = new Array<>();
	private ShaderProgram shaderDefault;
	protected ShaderProgram shader;
	private Texture current;
	protected Texture backgroundTextureNearest;
	protected Texture backgroundTextureLinear;
	protected Texture charactersTextureNearest;
	protected Texture charactersTextureLinear;

	protected float cx;
	protected float cy;
	private Array<AnimChar> characters;

	public PixelArtShaderV3Test(GameReset game) {
		// ignore this
		super(game);
		shaderDefault = batch.getShader();
		ScreenViewport viewport = (ScreenViewport) stage.getViewport();
		viewport.setUnitsPerPixel(.33f);

		charactersTextureLinear = new Texture("pixels/characters.png");
		charactersTextureLinear.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		TextureRegion[][] regions = new TextureRegion(charactersTextureLinear).split(32, 32);

		backgroundTextureLinear = new Texture("pixels/scene.png");
		backgroundTextureLinear.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		shader = new ShaderProgram(Gdx.files.internal("pixels/pixelartv3.vsh"), Gdx.files.internal("pixels/pixelartv3.fsh"));

		if (!shader.isCompiled())
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());

		float frameDuration = 1/8f;
		Array<Animation<TextureRegion>> anims = new Array<>();
		anims.add(new Animation<>(frameDuration, new Array<>(regions[0]), Animation.PlayMode.LOOP));
		anims.add(new Animation<>(frameDuration, new Array<>(regions[1]), Animation.PlayMode.LOOP));
		anims.add(new Animation<>(frameDuration, new Array<>(regions[2]), Animation.PlayMode.LOOP));
		{
			Array<TextureRegion> trimmedRegions = new Array<>(regions[3]);
			trimmedRegions.removeRange(4, trimmedRegions.size -1);
			anims.add(new Animation<>(frameDuration, trimmedRegions, Animation.PlayMode.LOOP));
		}

		cx = 1280 * .5f * viewport.getUnitsPerPixel();
		cy = 720 * .5f * viewport.getUnitsPerPixel();

		Image bg = new Image(backgroundTextureLinear);
		bg.setPosition(cx, cy, Align.center);
		stage.addActor(bg);

		characters = new Array<>();
		for (int i = 0; i < 20; i++) {
			characters.add(new AnimChar(anims.random()));
		}

		for (AnimChar character : characters) {
			stage.addActor(character);
		}
	}

	@Override
	protected Stage newStage() {
		batch = new SpriteBatch() {
			@Override
			protected void switchTexture(Texture texture) {
				super.switchTexture(texture);
				if (batch.getShader() == shader) {
					shader.setUniformf("u_texSize", texture.getWidth(), texture.getHeight());
				}
			}
		};
		return new Stage(guiViewport, batch);
	}

	protected class AnimChar extends Image {
		Animation<TextureRegion> animation;

		float state;

		public AnimChar(Animation<TextureRegion> animation) {
			this.animation = animation;
			setDrawable(new TextureRegionDrawable(animation.getKeyFrame(state)));
			setSize(getPrefWidth(), getPrefHeight());
			setPosition(cx + MathUtils.random(-160, 160), cy + MathUtils.random(-90, 90));
			setScale(MathUtils.random(.66f, 1.33f));
			setOrigin(Align.center);
			setRotation(MathUtils.random(0, 360));
			rngMove();
		}

		private void rngMove() {
			addAction(Actions.sequence(
					Actions.delay(MathUtils.random(1, 3)),
					Actions.parallel(
							Actions.moveToAligned(cx + MathUtils.random(-160, 160), cy + MathUtils.random(-90, 90), Align.center, 5f),
							Actions.rotateBy(MathUtils.random(-180, 180), 5f)
					),
					Actions.run(this::rngMove)
			));
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			state += delta;
			setDrawable(new TextureRegionDrawable(animation.getKeyFrame(state)));
		}
	}

	int moveX;
	int moveY;
	int shift = 1;
	float rotation;
	@Override public void render (float delta) {
		super.render(delta);
		float moveSpeed = delta * 5 * shift;
		if (moveX > 0) {
			guiCamera.position.x += moveSpeed;
		} else if (moveX < 0){
			guiCamera.position.x -= moveSpeed;
		}
		if (moveY > 0) {
			guiCamera.position.y += moveSpeed;
		} else if (moveY < 0) {
			guiCamera.position.y -= moveSpeed;
		}
		guiCamera.update();

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
			if (batch.getShader() == shader) {
				Gdx.app.log(TAG, "Use default shader");
				batch.setShader(shaderDefault);
			} else {
				Gdx.app.log(TAG, "Use custom shader");
				batch.setShader(shader);
			}

		}

		batch.setColor(Color.WHITE);
		stage.act(delta);
		stage.draw();
	}

	Vector3 tp = new Vector3();
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT) return false;
		gameCamera.unproject(tp.set(screenX, screenY, 0));
		return true;
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.Q:
			rotation+= 11.25f;
			break;
		case Input.Keys.E:
			rotation-= 11.25f;
			break;
		case Input.Keys.W:
		case Input.Keys.UP:
			moveY++;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			moveY--;
			break;
		case Input.Keys.A:
		case Input.Keys.LEFT:
			moveX--;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			moveX++;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 10;
			break;
		}
		return super.keyDown(keycode);
	}

	@Override public boolean keyUp (int keycode) {
		switch (keycode) {
		case Input.Keys.W:
		case Input.Keys.UP:
			moveY--;
			break;
		case Input.Keys.S:
		case Input.Keys.DOWN:
			moveY++;
			break;
		case Input.Keys.A:
		case Input.Keys.LEFT:
			moveX++;
			break;
		case Input.Keys.D:
		case Input.Keys.RIGHT:
			moveX--;
			break;
		case Input.Keys.SHIFT_LEFT:
			shift = 1;
			break;
		}
		return super.keyUp(keycode);
	}


	@Override public void dispose () {
		super.dispose();
		for (Texture texture : textures) {
			texture.dispose();
		}
		batch.setShader(null);
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = PlaygroundGame.config();
		config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 4);
		PlaygroundGame.start(args, PixelArtShaderV3Test.class);
	}
}
