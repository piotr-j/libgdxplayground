package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.utils.CircularBuffer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class CameraShakeTest extends BaseScreen {
	private static final String TAG = CameraShakeTest.class.getSimpleName();
	private final int numPoints = 128;
	private CircularBuffer<Vector2> points = new CircularBuffer<>(numPoints, false);
	private CameraShaker shaker;
	private int shakerId;
	private Array<CameraShaker> shakers = new Array<>();
	private Array<String> shakerNames = new Array<>();

	public CameraShakeTest (GameReset game) {
		super(game);
		for (int i = 0; i < numPoints; i++) {
			points.store(new Vector2());
		}

		createShaker("DefaultShaker (random)", null);
		createShaker("NoiseShaker", new CameraShaker.ShakeStrategy() {
			private float timer = 0;
			@Override public Vector3 calculateOffset (float delta, float magnitude, Vector3 out) {
				timer += delta;
				out.x = SimplexNoise.noise(timer * 10) * magnitude;
				out.y = SimplexNoise.noise(timer * 5) * magnitude;
				return out;
			}
		});
		createShaker("RandomShaker", new CameraShaker.ShakeStrategy() {
			@Override public Vector3 calculateOffset (float delta, float magnitude, Vector3 out) {
				out.x = MathUtils.random(-magnitude, magnitude);
				out.y = MathUtils.random(-magnitude, magnitude);
				return out;
			}
		});
		createShaker("InterpolatedShaker", new CameraShaker.ShakeStrategy() {
			private float timer = 0;
			private float freq = 16;
			private Vector2 start = new Vector2();
			private Vector2 target = new Vector2();
			@Override public Vector3 calculateOffset (float delta, float magnitude, Vector3 out) {
				// note this isnt so great if magnitude changes
				if (timer <= 0) {
					start.set(target);
					target.set(MathUtils.random(-magnitude, magnitude), MathUtils.random(-magnitude, magnitude));
				}
				timer += delta;
				out.x = Interpolation.linear.apply(start.x, target.x, timer * freq);
				out.y = Interpolation.linear.apply(start.y, target.y, timer * freq);
				// pick target 4 times a second
				if (timer >= 1f/freq) {
					timer = 0;
				}
				return out;
			}
		});
		shaker = shakers.get(shakerId);

		Gdx.app.log(TAG, "Space - shake!");
		Gdx.app.log(TAG, "Q - prev shaker!");
		Gdx.app.log(TAG, "E - next shaker!");
	}

	private void createShaker (String name, CameraShaker.ShakeStrategy strategy) {
		shakerNames.add(name);
		shakers.add(new CameraShaker(strategy));
	}

	private float timer;

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		timer += delta;
		float newX = MathUtils.sin(timer*.75f) * 2;
		float newY = MathUtils.cos(timer*.75f) * 2;
		gameCamera.position.set(newX, newY, 0);
		gameCamera.update();

		shaker.begin(delta, gameCamera);

		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.rect(3f - VP_WIDTH / 2, 3f - VP_HEIGHT / 2, VP_WIDTH - 6f, VP_HEIGHT - 6f);
		renderer.end();

		renderer.setColor(Color.BLACK);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		int step = 1;
		float magnitude = 3f;
		float nOffset = 343312;
		float offsetY = 5f;
		for (int x = 0; x < 1280; x += step) {
			float y1 = SimplexNoise.noise(x * INV_SCALE);
			float y2 = SimplexNoise.noise((x + step) * INV_SCALE);
			renderer.line(x * INV_SCALE - VP_WIDTH / 2, y1 * magnitude + offsetY, (x + step) * INV_SCALE - VP_WIDTH / 2,
				y2 * magnitude + offsetY);
			y1 = SimplexNoise.noise(x * INV_SCALE + nOffset);
			y2 = SimplexNoise.noise((x + step) * INV_SCALE + nOffset);
			renderer.line(x * INV_SCALE - VP_WIDTH / 2, y1 * magnitude - +offsetY, (x + step) * INV_SCALE - VP_WIDTH / 2,
				y2 * magnitude - offsetY);
		}

		renderer.setColor(Color.WHITE);
		timer += delta;
		float xScale = 2;
		float amplitude = 5;
		magnitude = 2;
		float x = SimplexNoise.noise(timer * amplitude * xScale) * magnitude * gameViewport.getWorldWidth() / gameViewport
			.getWorldHeight();
		float y = SimplexNoise.noise(timer * amplitude) * magnitude;
		points.store(points.read().set(x, y));
		Vector2 first = points.read();
		points.store(first);
		for (int i = 0; i < numPoints - 1; i++) {
			float a = i / (float)numPoints;
			renderer.setColor(1, 1, 1, a);
			Vector2 next = points.read();
			renderer.line(first.x, first.y, next.x, next.y);
			first = next;
			points.store(next);
		}
		float w = magnitude * gameViewport.getWorldWidth() / gameViewport.getWorldHeight();
		float h = magnitude;
		renderer.rect(-w, -h, w * 2, h * 2);
		renderer.end();
		shaker.end();
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			shaker.shake(.5f, .25f);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			shakerId--;
			if (shakerId < 0) {
				shakerId = shakers.size -1;
			}
			shaker = shakers.get(shakerId);
			Gdx.app.log(TAG, "Shaker " + shakerNames.get(shakerId));
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			shakerId++;
			if (shakerId >= shakers.size) {
				shakerId = 0;
			}
			shaker = shakers.get(shakerId);
			Gdx.app.log(TAG, "Shaker " + shakerNames.get(shakerId));
		}
	}

	/**
	 * Shapes the wrapped camera
	 */
	public static class CameraShaker {
		public interface ShakeStrategy {
			Vector3 calculateOffset (float delta, float magnitude, Vector3 out);
		}
		private static ShakeStrategy simpleStrategy;
		private Vector3 camPos;
		private Vector3 out;
		private OrthographicCamera camera;
		private float duration;
		private float magnitude;
		private float maxMagnitude = 2;
		private float magnitudeTaperScale = 3f;
		private ShakeStrategy strategy;

		/**
		 * Create CameraShaker with simple ShakeStrategy
		 */
		public CameraShaker () {
			this(null);
		}

		/**
		 * Create CameraShaker with given strategy
		 * @param strategy ShakeStrategy to use, if null default one will be created
		 */
		public CameraShaker (ShakeStrategy strategy) {
			this.strategy = strategy;
			if (strategy == null) {
				if (simpleStrategy == null) {
					simpleStrategy = new ShakeStrategy() {
						@Override public Vector3 calculateOffset (float delta, float magnitude, Vector3 out) {
							out.x = MathUtils.random(-magnitude, magnitude);
							out.y = MathUtils.random(-magnitude, magnitude);
							return out;
						}
					};
				}
				this.strategy = simpleStrategy;
			}
			camPos = new Vector3();
			out = new Vector3();
		}

		/**
		 * Shake the camera
		 *
		 * Why minimum time? The noise function used spits out 0 for integer values,
		 * this allows for the shake to end very near the original camera position, albeit not at exact time
		 * How fast that happens is dependant on #getMagnitudeTaperScale()
		 *
		 * multiple shakes will be added up to each other
		 *
		 * @param minTime minimum time the shaking will take place
		 * @param magnitude magnitude of the shape, in game units
		 */
		public void shake(float minTime, float magnitude) {
			duration += minTime;
			this.magnitude = Math.min(this.magnitude + magnitude, maxMagnitude);
		}

		/**
		 * Must be called before rendering that uses wrapped camera begins
		 */
		public void begin(float delta, OrthographicCamera camera) {
			if (this.camera != null) throw new AssertionError("Call #end first!");
			this.camera = camera;
			camPos.set(camera.position);
			if (duration > 0) {
				duration -= delta;
				out.setZero();
				strategy.calculateOffset(delta, magnitude, out);
				camera.position.add(out);
				camera.update();
			} else if (magnitude > 0){
				// taper of magnitude quickly until we end up close enough to original camera position
				magnitude -= magnitudeTaperScale * delta;
				out.setZero();
				strategy.calculateOffset(delta, magnitude, out);
				if (!out.isZero(0.001f)) {
					camera.position.add(out);
					camera.update();
					duration -= delta;
				} else {
					magnitude = 0;
					Gdx.app.log("", "Overshoot = " + (-duration) + "ms");
					duration = 0;
				}
			}
		}

		/**
		 * Must be called after rendering that uses wrapped camera happened
		 */
		public void end() {
			if (camera == null) throw new AssertionError("Call #begin first!");
			camera.position.set(camPos);
			camera.update();
			camera = null;
		}

		/**
		 * Stop shaking like it would normally after the set duration has elapse
		 */
		public void stop () {
			duration = 0;
		}

		/**
		 * Immediately cancel shaking
		 */
		public void cancel () {
			duration = 0;
			magnitude = 0;
		}

		public float getDuration () {
			return duration;
		}

		public float getMagnitude () {
			return magnitude;
		}

		public boolean isShaking () {
			return magnitude > 0;
		}

		public float getMaxMagnitude () {
			return maxMagnitude;
		}

		public CameraShaker setMaxMagnitude (float maxMagnitude) {
			this.maxMagnitude = maxMagnitude;
			return this;
		}

		public float getMagnitudeTaperScale () {
			return magnitudeTaperScale;
		}

		public CameraShaker setMagnitudeTaperScale (float magnitudeTaperScale) {
			this.magnitudeTaperScale = magnitudeTaperScale;
			return this;
		}
	}

	/**
	 * Adapted from https://github.com/SRombauts/SimplexNoise/blob/master/src/SimplexNoise.cpp
	 */
	public static class SimplexNoise {
		private SimplexNoise(){}
		// how about an usigned byte eh?
		private static final int perm[] = {151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30,
			69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
			57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146,
			158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1,
			216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3,
			64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182,
			189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39,
			253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12,
			191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115,
			121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156,
			180};

		private static int hash (int i) {
			return perm[i % 256];
		}

		private static int fastFloor (float fp) {
			int i = (int)fp;
			return (fp < i) ? (i - 1) : (i);
		}

		private static float grad (int hash, float x) {
			int h = hash & 0x0F;        // Convert low 4 bits of hash code
			float grad = 1.0f + (h & 7);    // Gradient value 1.0, 2.0, ..., 8.0
			if ((h & 8) != 0)
				grad = -grad; // Set a random sign for the gradient
//  float grad = gradients1D[h];    // NOTE : Test of Gradient look-up table instead of the above
			return (grad * x);              // Multiply the gradient with the distance
		}

		static float noise (float x) {
			float n0, n1;   // Noise contributions from the two "corners"

			// No need to skew the input space in 1D

			// Corners coordinates (nearest integer values):
			int i0 = fastFloor(x);
			int i1 = i0 + 1;
			// Distances to corners (between 0 and 1):
			float x0 = x - i0;
			float x1 = x0 - 1.0f;

			// Calculate the contribution from the first corner
			float t0 = 1.0f - x0 * x0;
//  if(t0 < 0.0f) t0 = 0.0f; // not possible
			t0 *= t0;
			n0 = t0 * t0 * grad(hash(i0), x0);

			// Calculate the contribution from the second corner
			float t1 = 1.0f - x1 * x1;
//  if(t1 < 0.0f) t1 = 0.0f; // not possible
			t1 *= t1;
			n1 = t1 * t1 * grad(hash(i1), x1);

			// The maximum value of this noise is 8*(3/4)^4 = 2.53125
			// A factor of 0.395 scales to fit exactly within [-1,1]
			return 0.395f * (n0 + n1);
		}
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, CameraShakeTest.class);
	}
}
