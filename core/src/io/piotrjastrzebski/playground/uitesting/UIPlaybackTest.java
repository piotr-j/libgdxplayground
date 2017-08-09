package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sun.xml.internal.bind.v2.model.core.ID;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIPlaybackTest extends BaseScreen {
	protected final static String TAG = UIPlaybackTest.class.getSimpleName();

	TextureRegion region;
	public UIPlaybackTest (GameReset game) {
		super(game);

		clear.set(Color.DARK_GRAY);
		rebuild();
	}

	protected Vector2 v2 = new Vector2();
	protected Array<Step> steps = new Array<>();
	protected StepsRunner stepsRunner = new StepsRunner();
	protected void rebuild() {
		root.clear();

		steps.clear();
		steps.add(new StepC());
		steps.add(new StepA());
		steps.add(new StepB());
		steps.add(new StepC());
		steps.add(new StepC());
		steps.add(new StepB());
		steps.add(new StepA());
		steps.add(new StepC());

		stepsRunner.init(steps);
		stepsRunner.stepsListener = new StepsRunner.StepsListener() {
			@Override public void stateChanged (int stepId, int prevState, int nextState, float speed) {
				log(TAG, stepId + " :: "+StepsRunner.stateToString(prevState)+" => "+ StepsRunner.stateToString(nextState) + String.format(", s=%.2f", speed));
			}

			int lastPercent;
			float lastProgress = -1;
			@Override public void stateProgress (int stepId, int state, float progress, float speed) {
				if (lastProgress == -1) {
					lastProgress = progress;
				}
				float dir = progress - lastProgress;
				int percent = (int)(progress * 10) * 10;
				if (percent != lastPercent) {
					lastPercent = percent;
					String tag = String.format("%5s::%s", tick, TAG).replace(" ", "0");
					System.out.print(String.format("\r%s: %d :: %s %d%%, s=%.2f", tag, stepId, StepsRunner.stateToString(state), lastPercent, speed));

					if ((dir > 0 && percent == 100) || (dir < 0 && percent == 0)) {
						lastProgress = -1;
						System.out.println();
					}
				}
			}
		};
	}
	static int tick;
	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
		tick++;

		if (stepsRunner.update(delta)) {
			String dirStrng = stepsRunner.runDir == StepsRunner.RUN_FORWARD?"FORWARD":"BACKWARD";
			log(TAG, "Changing dir to "+dirStrng+"\n\n");
			stepsRunner.runDir(stepsRunner.runDir * -1);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			Gdx.app.log("CONTROL", "Run backwards");
			stepsRunner.runDir(StepsRunner.RUN_BACKWARD);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			Gdx.app.log("CONTROL", "Run forwards");
			stepsRunner.runDir(StepsRunner.RUN_FORWARD);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
			stepsRunner.runSlower();
			Gdx.app.log("CONTROL", "Run slower, " + stepsRunner.runSpeed());
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
			stepsRunner.runFaster();
			Gdx.app.log("CONTROL", "Run faster, " + stepsRunner.runSpeed());
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
			stepsRunner.runStop();
			Gdx.app.log("CONTROL", "Run stop");
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			stepsRunner.runSpeedDefault();
			Gdx.app.log(TAG, "Run normal");
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) {
			int count = stepsRunner.fastBackward();
			// breaks fancy percent stuff...
//			log(TAG, "Fast backward " + count);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
			int count = stepsRunner.fastForward();
			// breaks fancy percent stuff...
//			log(TAG, "Fast forward " + count);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			rebuild();
		}
	}

	private static void log (String tag, String message) {
		Gdx.app.log(String.format("%5s::%s", tick, tag).replace(" ", "0"), message);
	}

	static class StepsRunner {
		private final static String TAG = StepsRunner.class.getSimpleName();
		// steps related stuff
		static final int RUN_FORWARD = 1;
		static final int RUN_BACKWARD = -1;
		int runDir = RUN_FORWARD;
		float runSpeedDefault = 1f;
		float runSpeed = runSpeedDefault;
		int fastStepRuns;
		int fastDir = 0;
		// current step related stuff
		static final int IDLE = 0;
		static final int PRE_DELAY = 1;
		static final int RUNNING = 2;
		static final int POST_DELAY = 3;
		Step step;
		int stepId = -1;
		float preDelay;
		float postDelay;
		// pre or post delay, depending on the state
		float timer;
		int timerPercent;
		int state = IDLE;
		Array<Step> steps;
		boolean stepRun;

		StepsListener stepsListener;

		public StepsRunner () {reset();}

		boolean update(float delta) {
			boolean done = updateStep(delta * runDir * runSpeed);
			if (stepRun) {
				// if any step was run this frame
				if (fastStepRuns > 0) fastStepRuns--;
				if (fastStepRuns <= 0) {
					runSpeed = runSpeedDefault;
				}
			}
			return done;
		}

		/**
		 *
		 * @param delta dt, negative to reverse direction
		 * @return if steps are done
		 */
		boolean updateStep(float delta) {
			stepRun = false;
			do {
				if (state == IDLE) {
					if (delta > 0) {
						stepId++;
						if (stepId >= steps.size) {
							return true;
						}
						changeState(PRE_DELAY);
					} else {
						stepId--;
						if (stepId < 0) {
							return true;
						}
						changeState(POST_DELAY);
					}
					step = steps.get(stepId);
					preDelay = preDelay(step);
					postDelay = postDelay(step);
					timer = 0;
				}
				if (state == PRE_DELAY) {
					timer += delta;
					if (postDelay > 0) {
						if (stepsListener != null) {
							stepsListener.stateProgress(stepId, state, MathUtils.clamp(timer/preDelay, 0, 1), runSpeed);
						}
					}
					if (delta > 0 && timer >= postDelay) {
						changeState(RUNNING);
					} else if (delta < 0 && timer <= 0) {
						changeState(IDLE);
					}
				}
				if (state == RUNNING) {
					stepRun = true;
					if (delta > 0) {
						step.run();
						timer = 0;
						changeState(POST_DELAY);
					} else {
						step.undo();
						timer = preDelay;
						changeState(PRE_DELAY);
						timerPercent = 100;
					}
				}
				if (state == POST_DELAY) {
					timer += delta;
					if (postDelay > 0) {
						if (stepsListener != null) {
							stepsListener.stateProgress(stepId, state, MathUtils.clamp(timer/preDelay, 0, 1), runSpeed);
						}
					}
					if (delta > 0 && timer >= postDelay) {
						changeState(IDLE);
					} else if (delta < 0 && timer <= 0) {
						changeState(RUNNING);
					}

				}
			} while (state == IDLE && stepId < steps.size && stepId >= 0);
			return false;
		}

		private void changeState (int nextState) {
			int prevState = state;
			state = nextState;
			if (stepsListener!= null) stepsListener.stateChanged(stepId, prevState, state, runSpeed);
		}

		public void init (Array<Step> steps) {
			this.steps = steps;
			reset();
		}

		void reset() {
			step = null;
			stepId = -1;
			preDelay = 0;
			postDelay = 0;
			timer = 0;
			state = IDLE;
		}

		public void runDir (int runDir) {
			this.runDir = runDir;
		}

		public void runFaster () {
			runSpeed += .25f;
			if (runSpeed > 2) runSpeed = 2;
		}

		public void runSlower () {
			runSpeed -= .25f;
			if (runSpeed < 0) runSpeed = 0;
		}

		public void runSpeedDefault () {
			runSpeed = runSpeedDefault;
		}
		public void runStop () {
			runSpeed = 0;
		}

		public float runSpeed () {
			return runSpeed;
		}

		public int fastBackward () {
			runSpeed = -runSpeedDefault * 4;
			if (fastDir != RUN_BACKWARD) {
				fastDir = RUN_BACKWARD;
				fastStepRuns = 0;
			}
			return fastStepRuns++;
		}

		public int fastForward () {
			runSpeed = runSpeedDefault * 4;
			if (fastDir != RUN_FORWARD) {
				fastDir = RUN_FORWARD;
				fastStepRuns = 0;
			}
			return fastStepRuns++;
		}

		public static String stateToString (int state) {
			switch (state) {
			case IDLE: return "IDLE";
			case PRE_DELAY: return "PRE_DELAY";
			case RUNNING: return "RUNNING";
			case POST_DELAY: return "POST_DELAY";
			}
			return "???";
		}

		interface StepsListener {
			void stateChanged(int stepId, int prevState, int nextState, float speed);

			void stateProgress (int stepId, int state, float progress, float speed);
		}
	}

	private static float preDelay(Step step) {
		if (step instanceof StepA) {
			return 2.5f ;
		}
		if (step instanceof StepB) {
			return 3.5f;
		}
		// StepC, no delay
		return 0;
	}

	private static float postDelay(Step step) {
		if (step instanceof StepA) {
			return 2.5f ;
		}
		if (step instanceof StepB) {
			return 3.5f;
		}
		// StepC, no delay
		return 0;
	}

	private static abstract class Step {
		abstract void run();

		public abstract void undo ();
	}

	private static class StepA extends Step {
		static int _id;
		int id = _id++;
		@Override void run () {
			log("Steps", "Running StepA "+id +"!");
		}

		@Override public void undo () {
			log("Steps", "Undoing StepA "+id +"!");
		}
	}

	private static class StepB extends Step {
		static int _id;
		int id = _id++;
		@Override void run () {
			log("Steps", "Running StepB "+id +"!");
		}

		@Override public void undo () {
			log("Steps", "Undoing StepB "+id +"!");
		}
	}

	private static class StepC extends Step {
		static int _id;
		int id = _id++;
		@Override void run () {
			log("Steps", "Running StepC "+id +"!");
		}

		@Override public void undo () {
			log("Steps", "Undoing StepC "+id +"!");
		}
	}
	@Override public void dispose () {
		super.dispose();
		region.getTexture().dispose();
	}

	@Override public boolean keyDown (int keycode) {
		return super.keyDown(keycode);
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width *= .5f;
		config.height *= .5f;
		PlaygroundGame.start(args, config, UIPlaybackTest.class);
	}

}
