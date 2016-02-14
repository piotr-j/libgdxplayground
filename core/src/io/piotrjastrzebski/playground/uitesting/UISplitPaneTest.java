package io.piotrjastrzebski.playground.uitesting;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UISplitPaneTest extends BaseScreen {
	public UISplitPaneTest (GameReset game) {
		super(game);
		stage.setDebugAll(true);
		VisTable tableA = new VisTable();
		tableA.add(new VisLabel("LabelA1")).row();
		tableA.add(new VisLabel("LabelA2")).row();
		tableA.add(new VisLabel("LabelA3")).row();
		tableA.add(new VisLabel("LabelA3")).row();
		VisTable tableB = new VisTable();
		tableB.add(new VisLabel("LabelB1")).row();
		tableB.add(new VisLabel("LabelB2")).row();
		tableB.add(new VisLabel("LabelB3")).row();
		tableB.add(new VisLabel("LabelB3")).row();
		VisTable tableC = new VisTable();
		tableC.add(new VisLabel("LabelC1")).row();
		tableC.add(new VisLabel("LabelC2")).row();
		tableC.add(new VisLabel("LabelC3")).row();
		tableC.add(new VisLabel("LabelC3")).row();
		VisSplitPane splitPaneAB = new VisSplitPane(tableA, tableB, false);
		VisSplitPane splitPaneABC = new VisSplitPane(splitPaneAB, tableC, false);
		splitPaneABC.setSplitAmount(0.66f);
		root.add(splitPaneABC);
	}


	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UISplitPaneTest.class);
	}
}
