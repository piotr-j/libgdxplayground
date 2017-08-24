package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/DragAndDropTest.java
 * Created by PiotrJ on 20/06/15.
 */
public class UIDaDInputReplayTest extends BaseScreen {
	static final String TAG = UIDaDInputReplayTest.class.getSimpleName();

	Label source;
	Label validTarget;
	Label invalidTarget;
	DragAndDrop dad;
	Texture texture;
	ReplayMultiplexer replayMultiplexer;

	public UIDaDInputReplayTest (GameReset game) {
		super(game);

		multiplexer.clear();
		replayMultiplexer = new ReplayMultiplexer();
		replayMultiplexer.addProcessor(this);
		replayMultiplexer.addProcessor(stage);
		multiplexer.addProcessor(replayMultiplexer);

		texture = new Texture("tiled/brick.png");
		root.add(validTarget = new Label("Valid Target", skin)).pad(100);
		root.add(source = new Label("Image Source", skin)).pad(100);
		root.add(invalidTarget = new Label("Invalid Target", skin)).pad(100);

		dad = new DragAndDrop();

		dad.addSource(new Source(source) {
			@Override public Payload dragStart (InputEvent event, float x, float y, int pointer) {
				Payload payload = new Payload();
				{
					Table table = new Table();
					Image image = new Image(texture);
					image.setColor(Color.ORANGE);
					table.add(image);
					table.add(new Label("Drag", skin));
					table.pack();
					payload.setDragActor(table);
				}
				{
					Table table = new Table();
					Image image = new Image(texture);
					image.setColor(Color.RED);
					table.add(image);
					table.add(new Label("Drag", skin));
					table.pack();
					payload.setInvalidDragActor(table);
				}
				{
					Table table = new Table();
					Image image = new Image(texture);
					image.setColor(Color.GREEN);
					table.add(image);
					table.add(new Label("Drag", skin));
					table.pack();
					payload.setValidDragActor(table);
				}
				return payload;
			}
		});

		dad.addTarget(new Target(validTarget) {
			@Override public boolean drag (Source source, Payload payload, float x, float y, int pointer) {
				return true;
			}

			@Override public void drop (Source source, Payload payload, float x, float y, int pointer) {
				Gdx.app.log(TAG, "dropped in valid target");
			}
		});
		dad.addTarget(new Target(invalidTarget) {
			@Override public boolean drag (Source source, Payload payload, float x, float y, int pointer) {
				return false;
			}

			@Override public void drop (Source source, Payload payload, float x, float y, int pointer) {
				Gdx.app.log(TAG, "dropped in invalid target");
			}
		});
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		replayMultiplexer.update();
		if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			replayMultiplexer.setReplayState(ReplayMultiplexer.ReplayState.RECORD);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			replayMultiplexer.setReplayState(ReplayMultiplexer.ReplayState.REPLAY);
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			replayMultiplexer.restart();
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
			replayMultiplexer.setReplayState(ReplayMultiplexer.ReplayState.PASS);
		}
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
		texture.dispose();
	}

	static class ReplayMultiplexer implements InputProcessor {
		private Array<InputProcessor> processors = new Array<>(4);
		public enum ReplayState {RECORD, REPLAY, PASS}
		private ReplayState state = ReplayState.PASS;
		// time current record started
		private long recordStartTime;
		private long replayStartTime;
		private Array<ReplayEvent> replayEvents = new Array<>();
		private int eventId = 0;

		public ReplayMultiplexer () {
		}

		public ReplayMultiplexer (InputProcessor... processors) {
			for (int i = 0; i < processors.length; i++)
				this.processors.add(processors[i]);
		}

		public void addProcessor (int index, InputProcessor processor) {
			if (processor == null)
				throw new NullPointerException("processor cannot be null");
			processors.insert(index, processor);
		}

		public void removeProcessor (int index) {
			processors.removeIndex(index);
		}

		public void addProcessor (InputProcessor processor) {
			if (processor == null)
				throw new NullPointerException("processor cannot be null");
			processors.add(processor);
		}

		public void removeProcessor (InputProcessor processor) {
			processors.removeValue(processor, true);
		}

		/**
		 * @return the number of processors in this multiplexer
		 */
		public int size () {
			return processors.size;
		}

		public void clear () {
			processors.clear();
		}

		public void setProcessors (Array<InputProcessor> processors) {
			this.processors = processors;
		}

		public Array<InputProcessor> getProcessors () {
			return processors;
		}

		public boolean keyDown (int keycode) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.KEY_DOWN, keycode));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyDown(keycode))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public boolean keyUp (int keycode) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.KEY_UP, keycode));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyUp(keycode))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public boolean keyTyped (char character) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.KEY_TYPED, character));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyTyped(character))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public boolean touchDown (int screenX, int screenY, int pointer, int button) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.TOUCH_DOWN, screenX, screenY, pointer, button));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchDown(screenX, screenY, pointer, button))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public boolean touchUp (int screenX, int screenY, int pointer, int button) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.TOUCH_UP, screenX, screenY, pointer, button));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchUp(screenX, screenY, pointer, button))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public boolean touchDragged (int screenX, int screenY, int pointer) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.TOUCH_DRAGGED, screenX, screenY, pointer));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchDragged(screenX, screenY, pointer))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		@Override public boolean mouseMoved (int screenX, int screenY) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.MOUSE_MOVED, screenX, screenY));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).mouseMoved(screenX, screenY))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		@Override public boolean scrolled (int amount) {
			switch (state) {
			case RECORD:
				replayEvents.add(new ReplayEvent(ReplayEvent.SCROLLED, amount));
			case PASS:
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).scrolled(amount))
						return true;
				break;
			case REPLAY:
				break;
			}
			return false;
		}

		public void setReplayState(ReplayState state) {
			if (this.state == state) return;
			Gdx.app.log(TAG, "Changing state from " + this.state + " to " + state);
			switch (state) {
			case RECORD:
				replayEvents.clear();
				recordStartTime = TimeUtils.millis();
				break;
			case REPLAY:
				restart();
				break;
			case PASS:
				break;
			}
			this.state = state;

		}

		public void restart () {
			replayStartTime = TimeUtils.millis();
			eventId = 0;
		}

		public void reset () {
			replayEvents.clear();
		}

		public void update() {
			switch (state) {
			case RECORD:
				break;
			case REPLAY:
				long replayDiff = TimeUtils.millis() - replayStartTime;
				for (; eventId < replayEvents.size; eventId++) {
					ReplayEvent event = replayEvents.get(eventId);
					long eventDiff = event.timestamp() - recordStartTime;
					if (replayDiff >= eventDiff) {
						process(event);
					} else {
						break;
					}
				}
				if (eventId >= replayEvents.size) restart();
				break;
			case PASS:
				break;
			}
		}

		private void process (ReplayEvent event) {
			switch (event.type) {
			case ReplayEvent.KEY_DOWN: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyDown(event.keycode()))
						return;
			} break;
			case ReplayEvent.KEY_UP: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyUp(event.keycode()))
						return;
			} break;
			case ReplayEvent.KEY_TYPED: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).keyTyped(event.character()))
						return;
			} break;
			case ReplayEvent.TOUCH_DOWN: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchDown(event.screenX(), event.screenY(), event.pointer(), event.button()))
						return;
			} break;
			case ReplayEvent.TOUCH_DRAGGED: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchDragged(event.screenX(), event.screenY(), event.pointer()))
						return;
			} break;
			case ReplayEvent.TOUCH_UP: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).touchUp(event.screenX(), event.screenY(), event.pointer(), event.button()))
						return;
			} break;
			case ReplayEvent.MOUSE_MOVED: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).mouseMoved(event.screenX(), event.screenY()))
						return;
			} break;
			case ReplayEvent.SCROLLED: {
				for (int i = 0, n = processors.size; i < n; i++)
					if (processors.get(i).scrolled(event.scrolled()))
						return;
			} break;
			}
		}

		static final class ReplayEvent {
			public final static int KEY_DOWN = 1;
			public final static int KEY_UP = 2;
			public final static int KEY_TYPED = 3;
			public final static int TOUCH_DOWN = 4;
			public final static int TOUCH_DRAGGED = 5;
			public final static int TOUCH_UP = 6;
			public final static int MOUSE_MOVED = 7;
			public final static int SCROLLED = 8;
			// TODO we could pack things maybe, dont need more then 4096x4096 for x,y
			public final int type;
			private final int screenX; // also scrolled, keycode, character
			private final int screenY, pointer, button;
			private final long timestamp;

			public ReplayEvent (int type, int value) {
				this(type, value, 0, 0, 0);
			}

			public ReplayEvent (int type, int screenX, int screenY) {
				this(type, screenX, screenY, 0, 0);
			}

			public ReplayEvent (int type, int screenX, int screenY, int pointer) {
				this(type, screenX, screenY, pointer, 0);
			}

			public ReplayEvent (int type, int screenX, int screenY, int pointer, int button) {
				this.type = type;
				this.screenX = screenX;
				this.screenY = screenY;
				this.pointer = pointer;
				this.button = button;
				timestamp = TimeUtils.millis();
			}

			public char character() {
				return (char)screenX;
			}

			public int keycode() {
				return screenX;
			}

			public int scrolled() {
				return screenX;
			}

			public int screenX() {
				return screenX;
			}

			public int screenY() {
				return screenY;
			}

			public int pointer() {
				return pointer;
			}

			public int button() {
				return button;
			}

			public long timestamp () {
				return timestamp;
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIDaDInputReplayTest.class);
	}
}
