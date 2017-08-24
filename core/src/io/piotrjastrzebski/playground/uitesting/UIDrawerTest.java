package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class UIDrawerTest extends BaseScreen {
	private final static String TAG = UIDrawerTest.class.getSimpleName();
	Drawer drawer;
	public UIDrawerTest (GameReset game) {
		super(game);

		root.addActor(drawer = new Drawer());
		Table content = drawer.getContentTable();
		// some content to show, tree is quite fun
		Tree.TreeStyle treeStyle = VisUI.getSkin().get(VisTree.TreeStyle.class);
		treeStyle.background = VisUI.getSkin().getDrawable("window");
		VisTree visTree = new VisTree(treeStyle);
		for (int i = 0; i < 6; i++) {
			Tree.Node node = new Tree.Node(new VisLabel("NODE " + i));
			visTree.add(node);
			for (int j = 0; j < 3; j++) {
				node.add(new Tree.Node(new VisLabel("NODE " + i + "_" + j)));
			}
		}
		content.add(visTree).expand().fill();
	}

	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();
	}

	/**
	 * We assume that the drawer takes entire stage
	 * We want this to work like android drawer, more or less
	 */
	static class Drawer extends WidgetGroup {
		float animationDuration = .5f;
		boolean enabled;
		InputListener inputListener;
		Table content;
		boolean drawerVisible;
		float showProgress;

		public Drawer () {
			setFillParent(true);

			inputListener = new InputListener() {
				float xOffset;
				@Override public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					float width = getWidth();
					xOffset = x;
					if (drawerVisible) {
						if (content.hit(x, y, false) == null) {
							hideDrawer();
						}
						return false;
					}
					if (x <= width * .1) {
						addDrawer();
						return true;
					}
					return false;
				}

				@Override public void touchDragged (InputEvent event, float x, float y, int pointer) {
					x -= xOffset;
					float progress = MathUtils.clamp(x / content.getWidth(), .05f, 1);
					updateDrawer(progress);
				}

				@Override public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					float progress = MathUtils.clamp(x / content.getWidth(), 0, 1);
					if (progress > .5f) {
						showDrawer();
					} else {
						hideDrawer();
					}
				}
			};
			setEnabled(true);
			content = new Table();
			content.debug(Table.Debug.cell);
		}

		private void addDrawer () {
			content.setSize(getWidth() * .3f, getHeight());
			addActor(content);
			content.setX(-content.getWidth());
			updateDrawer(.05f);
		}

		private void updateDrawer (float progress) {
			showProgress = progress;
			content.setX(-content.getWidth() * (1-progress));
			drawerVisible = progress >= .5f;
		}

		private void showDrawer () {
			content.addAction(Actions.moveTo(
				0, 0, animationDuration * showProgress, Interpolation.fade
			));
			drawerVisible = true;
		}

		private void hideDrawer () {
			content.addAction(
				Actions.sequence(
					Actions.moveTo(
						-content.getWidth(), 0, animationDuration * showProgress, Interpolation.fade
					),
					Actions.run(new Runnable() {
						@Override public void run () {
							drawerVisible = false;
							showProgress = 0;
						}
					}),
					Actions.removeActor()
				));
		}

		public void setEnabled (boolean enabled) {
			this.enabled = enabled;
			if (enabled) {
				setTouchable(Touchable.enabled);
				addListener(inputListener);
			} else {
				setTouchable(Touchable.disabled);
				removeListener(inputListener);
			}
		}

		public Table getContentTable () {
			return content;
		}
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = PlaygroundGame.config();
		config.width *= .5f;
		config.height *= .5f;
		config.useHDPI = true;
		PlaygroundGame.start(args, config, UIDrawerTest.class);
	}
}
