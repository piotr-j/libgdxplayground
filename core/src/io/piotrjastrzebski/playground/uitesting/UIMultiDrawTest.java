package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIMultiDrawTest extends BaseScreen {
	private final static String TAG = UIMultiDrawTest.class.getSimpleName();

	public UIMultiDrawTest (GameReset game) {
		super(game);

		VisLabel label = new VisLabel("Some long label");
		label.setWrap(true);
		label.setEllipsis(true);

		Wrapper wrapper = new Wrapper(label);
		wrapper.setTransform(false);

		root.add(wrapper).width(60).fillX().left().pad(24).row();
		root.add(label).width(120).fillX().pad(24).row();

		root.debugActor();

		clear.set(Color.GRAY);
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	/**
	 * Kinda works for label, but will be enough for something more complex?
	 * Since the intended use case is selectbox, we perhaps could build the list each time we need to show it.
	 * Stick actual actor in the dropdown, use simple screenshot of it when we open list.
	 * Would not work for animation if we had one in that actor.
	 * Doing layout for this twice per frame is not optimal, but it wont be visible for long time.
	 */
	private static class Wrapper extends Table {
		Actor wrapped;
		public Wrapper (Actor wrapped) {
			this.wrapped = wrapped;
			add().prefSize(wrapped.getWidth(), wrapped.getHeight());
		}

		@Override protected void drawChildren (Batch batch, float parentAlpha) {
			super.drawChildren(batch, parentAlpha);
			float x = getX();
			float y = getY();
			float cx = wrapped.getX();
			float cy = wrapped.getY();
			float cw = wrapped.getWidth();
			float ch = wrapped.getHeight();
			wrapped.setPosition(x, y);
			wrapped.setSize(getWidth(), getHeight());
			wrapped.draw(batch, parentAlpha);
			wrapped.setPosition(cx, cy);
			wrapped.setSize(cw, ch);
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIMultiDrawTest.class);
	}
}
