package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIUndoRedoTest extends BaseScreen {
	CommandManager manager;
	public UIUndoRedoTest (GameReset game) {
		super(game);
		// undo BaseScreen stuff
		stage.clear();
		stage.setDebugAll(true);

		manager = new CommandManager();

	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	public static interface Command {
		public void execute();
		public void undo();
	}

	public static class CommandGroup implements Command {
		static int IDS;
		int id = IDS++;
		Array<Command> commands = new Array<>();
		public void add(Command command) {
			commands.add(command);
		}

		@Override public void execute() {
			Gdx.app.log("GRP", "EXEC " + id);
			for (Command command : commands) {
				command.execute();
			}
		}

		@Override public void undo() {
			Gdx.app.log("GRP", "UNDO " + id);
			for (int i = commands.size-1; i >= 0 ; i--) {
				commands.get(i).undo();
			}
		}
	}
	static int CMD_IDS;
	public static class CommandManager {
		Array<Command> commands = new Array<>();
		int current = 0;

		public boolean canRedo () {
			return current < commands.size -1 && current >= -1;
		}

		public void redo() {
			if (!canRedo()) {
				Gdx.app.log("MNG", "Cannot redo");
				return;
			}
			Gdx.app.log("MNG", "REDO");
			current++;
			commands.get(current).execute();
		}

		public boolean canUndo () {
			return commands.size > 0 && current >= 0;
		}

		public void undo() {
			if (!canUndo()) {
				Gdx.app.log("MNG", "Cannot undo");
				return;
			}
			Gdx.app.log("MNG", "UNDO");
			commands.get(current).undo();
			current--;
		}

		public void execute(Command command) {
			if (current < commands.size -1 && commands.size > 0) {
				commands.removeRange(current + 1, commands.size - 1);
			}
			commands.add(command);
			command.execute();
			current = commands.size - 1;
		}
	}

	private Command rngCommand () {
		if (MathUtils.random() > 0.75f) {
			CommandGroup group = new CommandGroup();
			for (int i = 1; i < MathUtils.random(3); i++) {
				group.add(rngCommand());
			}
			return group;
		} else {
			return new Command() {
				int id = CMD_IDS++;
				@Override public void execute () {
					Gdx.app.log("CMD", "EXEC " + id);
				}

				@Override public void undo () {
					Gdx.app.log("CMD", "UNDO " + id);
				}
			};
		}
	}

	private boolean isCtrlPressed() {
		return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
	}

	@Override public boolean keyDown (int keycode) {
		switch (keycode) {
		case Input.Keys.Z:
			if (isCtrlPressed())
				manager.undo();
			break;
		case Input.Keys.R:
			if (isCtrlPressed())
				manager.redo();
			break;
		case Input.Keys.ENTER:
			manager.execute(rngCommand());
			break;
		}
		return super.keyDown(keycode);
	}
}
