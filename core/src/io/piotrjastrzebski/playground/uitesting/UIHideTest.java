package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIHideTest extends BaseScreen {
	private final static String TAG = UIHideTest.class.getSimpleName();

	public UIHideTest (GameReset game) {
		super(game);

		final VisTable table = new VisTable(true);
		table.debugAll();
		final VisTextButton hide = new VisTextButton("Hide", "toggle");
		hide.addListener(new ChangeListener() {
			Actor old;
			@Override public void changed (ChangeEvent event, Actor actor) {
				Cell cell = table.getCells().get(2);
				if (hide.isChecked()) {
					old = cell.getActor();
					cell.setActor(null);
				} else {
					cell.setActor(old);
					old = null;
				}
				table.invalidateHierarchy();
			}
		});
		table.add(hide).row();
		table.add(new VisLabel("Label1")).row();
		table.add(new VisLabel("Label2")).row();
		table.add(new VisLabel("Label3")).row();
		stage.addActor(table);
		table.setPosition(stage.getWidth()/2 - table.getWidth()/2, stage.getHeight()/2 - table.getHeight()/2);
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
		PlaygroundGame.start(args, UIHideTest.class);
	}
}
