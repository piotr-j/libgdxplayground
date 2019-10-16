package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIFBOTransitionTest extends BaseScreen {
	FrameBuffer fboA;
	FrameBuffer fboB;

	VisTextButton trans;
	VisTable dummyA;
	VisTable dummyB;

	VisTable current;
	public UIFBOTransitionTest (GameReset game) {
		super(game);

		trans = new VisTextButton("Transition");
		trans.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				initTransit = true;
			}
		});

		dummyA = new VisTable(true);
		fillDummy(dummyA);
		dummyB = new VisTable(true);
		fillDummy(dummyB);

		initTransit = true;
	}

	boolean initTransit;
	boolean inTransit;

	private void fillDummy(VisTable table) {
		for (int i = 0; i < 20 + MathUtils.random(10); i++) {
			if (MathUtils.randomBoolean()) {
				table.add(new VisLabel("Dummy text " + MathUtils.random(0, 200)));
			} else {
				table.add(new VisSlider(0, 5 + MathUtils.random(5.f), 1, false));
			}
			table.row();
		}
	}

	float transitTime;
	@Override public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (initTransit) {
			// draw to fbo so we have something to transit to
			initTransit = false;

			fboA.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			stage.act();
			stage.draw();
			fboA.end();

			// swap out views
			if (current == dummyA) {
				current = dummyB;
			} else {
				current = dummyA;
			}
			root.clearChildren();
			root.add(trans);
			root.row();
			root.add(current);

			fboB.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			stage.act();
			stage.draw();
			fboB.end();

			inTransit = true;
		}
		if (inTransit) {
			// draw the transition
			transitTime+=delta;
			// TODO some fancy transition
			batch.setProjectionMatrix(guiCamera.combined);
			batch.begin();
			batch.setColor(1, 1, 1, 1 - transitTime);
			batch.draw(fboA.getColorBufferTexture(), 0, 0, guiViewport.getScreenWidth(), guiViewport.getScreenHeight(), 0, 0,
				fboA.getColorBufferTexture().getWidth(), fboA.getColorBufferTexture().getHeight(), false, true);

			batch.setColor(1, 1, 1, transitTime);
			batch.draw(fboB.getColorBufferTexture(), 0, 0, guiViewport.getScreenWidth(), guiViewport.getScreenHeight(), 0, 0,
				fboB.getColorBufferTexture().getWidth(), fboB.getColorBufferTexture().getHeight(), false, true);
			batch.end();

			// we don't want to go to the end to avoid alpha problems if it happens to go over 1
			if (transitTime > 0.95f) {
				transitTime = 0;
				inTransit = false;
			}
		} else {
			// normal stuff
			stage.act(delta);
			stage.draw();
		}
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		if (fboA != null) fboA.dispose();
		fboA = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
		if (fboB != null) fboB.dispose();
		fboB = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
	}

	@Override public void dispose () {
		super.dispose();
		if (fboA != null) fboA.dispose();
		if (fboB != null) fboB.dispose();
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIFBOTransitionTest.class);
	}
}
