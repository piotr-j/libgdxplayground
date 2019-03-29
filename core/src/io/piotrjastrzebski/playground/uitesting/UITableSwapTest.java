package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UITableSwapTest extends BaseScreen {
	public UITableSwapTest (GameReset game) {
		super(game);

		VisTextButton button = new VisTextButton("Swap");
		final VisLabel labelA = new VisLabel("Label A");
		final VisLabel labelB = new VisLabel("Label B");
		final VisLabel labelC = new VisLabel("Label C");

		Table table = new Table();
		table.add(button).row();
		table.add(labelA).row();
		table.add(labelB).row();
		table.add(labelC).row();
		root.add(table);

		button.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				swap(labelA, labelC);
			}
		});

		root.debugAll();
	}

	private void swap (final Actor actorA, final Actor actorB) {
		if (actorA.getParent() != actorB.getParent()) throw new AssertionError("");
		final Table parent = (Table)actorA.getParent();
		float duration = .2f;
		actorA.addAction(Actions.moveTo(actorB.getX(), actorB.getY(), duration));
		actorB.addAction(Actions.moveTo(actorA.getX(), actorA.getY(), duration));
		parent.addAction(Actions.sequence(
			Actions.delay(duration), Actions.run(new Runnable() {
				@Override public void run () {
					Cell<Actor> cellA = parent.getCell(actorA);
					actorA.remove();
					Cell<Actor> cellB = parent.getCell(actorB);
					actorB.remove();
					cellA.setActor(actorB);
					cellB.setActor(actorA);
				}
			})
		));
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	@Override public void dispose () {
		super.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, UITableSwapTest.class);
	}
}
