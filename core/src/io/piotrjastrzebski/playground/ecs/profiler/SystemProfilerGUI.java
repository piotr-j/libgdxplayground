package io.piotrjastrzebski.playground.ecs.profiler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;

import java.util.Comparator;

/**
 * Gui for SystemProfilers implemented in Scene2d
 * Graph must be renderer separately with shape renderer, after stage.draw()
 *
 * Certain static values can be changed before creation to modify behaviour
 *
 * Dynamic modification of profilers is not supported
 *
 * See example implementation of how this class can be used
 *
 * Created by PiotrJ on 07/08/15.
 */
public class SystemProfilerGUI extends Window {
	public static final Color GRAPH_V_LINE = new Color(0.6f, 0.6f, 0.6f, 1);
	public static final Color GRAPH_H_LINE = new Color(0.25f, 0.25f, 0.25f, 1);
	public static float FADE_TIME = 0.3f;
	public static float PRECISION = 0.01f;
	public static String FORMAT = "%.2f";
	public static String STYLE_SMALL = "default";
	public static float MIN_WIDTH = 75;
	/**
	 * how many graphs to draw at most
	 */
	public static int DRAW_MAX_COUNT = 15;
	/**
	 * How often should text update
	 */
	public static float REFRESH_RATE = 0.25f;

	Table profilerLabels;
	Graph graph;
	Table profilersTable;
	Array<ProfilerRow> rows = new Array<>();
	Skin skin;
	public SystemProfilerGUI (Skin skin, String style) {
		super("Profiler", skin, style);
		this.skin = skin;

		setResizable(true);
		setResizeBorder(16);
		TextButton closeButton = new TextButton("X", skin);
		getTitleTable().add(closeButton).padRight(3);
		closeButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				hide();
			}
		});

		Table root = new Table();

		Table graphTable = new Table();
		Table graphLabels = new Table();
		for (int i = 32; i >= 0; i/=2) {
			graphLabels.add(label(Integer.toString(i), skin)).expandY().center().row();
			if (i == 0) break;
		}
		graphTable.add(graphLabels).expandY().fillY();

		graphTable.add(graph = new Graph()).expand().fill();

		profilerLabels = new Table();
		profilerLabels.add().expandX().fillX();
		profilerLabels.add(label("max", skin, Align.right)).minWidth(MIN_WIDTH);
		profilerLabels.add(label("lmax", skin, Align.right)).minWidth(MIN_WIDTH);
		profilerLabels.add(label("avg", skin, Align.right)).minWidth(MIN_WIDTH);

		for (SystemProfiler profiler : SystemProfiler.get()) {
			rows.add(new ProfilerRow(profiler, skin));
		}
		profilersTable = new Table();
		// act once so we can get all profilers and can pack nicely
		act(0);

		ScrollPane pane = new ScrollPane(profilersTable);
		pane.setScrollingDisabled(true, false);
		root.add(graphTable).expand().fill();
		root.add(pane).fillX().pad(0, 10, 10, 10).top();

		add(root).expand().fill();
		pack();
	}

	private static Label label(String text, Skin skin) {
		return label(text, skin, Align.left);
	}

	private static Label label(String text, Skin skin, int align) {
		Label label = new Label(text, skin, STYLE_SMALL);
		label.setAlignment(align);
		return label;
	}


	float refreshTimer = REFRESH_RATE;
	Comparator<ProfilerRow> byAvg = new Comparator<ProfilerRow>() {
		@Override public int compare (ProfilerRow o1, ProfilerRow o2) {
			return (int)(o2.getAverage() - o1.getAverage());
		}
	};

	@Override public void act (float delta) {
		super.act(delta);

		refreshTimer += delta;
		if (refreshTimer < REFRESH_RATE) return;
		refreshTimer -= REFRESH_RATE;

		if (rows.size != SystemProfiler.sixe()) {
			rebuildRows();
		}

		Sort.instance().sort(rows, byAvg);
		// TODO would be better without clearing and re adding
		profilersTable.clear();
		profilersTable.add(profilerLabels).expandX().fillX().right();
		profilersTable.row();

		for (ProfilerRow row : rows) {
			row.update(0);
			profilersTable.add(row).expandX().fillX().left();
			profilersTable.row();
		}
	}

	private void rebuildRows() {
		int target = SystemProfiler.sixe();
		if (target > rows.size) {
			for (int i = rows.size; i < target; i++) {
				rows.add(new ProfilerRow(skin));
			}
		} else if (target < rows.size) {
			rows.removeRange(rows.size - target + 1, rows.size - 1);
		}
		for (int i = 0; i < target; i++) {
			SystemProfiler profiler = SystemProfiler.get(i);
			rows.get(i).init(profiler);
		}
	}

	private Vector2 temp = new Vector2();
	/**
	 * Render graph for profilers, must be called after {@link Stage#draw()}
	 * @param renderer {@link ShapeRenderer} to use, must be ready and set to Line type
	 */
	public void renderGraph (ShapeRenderer renderer) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		graph.localToStageCoordinates(temp.setZero());
		drawGraph(renderer, temp.x, temp.y, graph.getWidth(), graph.getHeight(), getColor().a);
	}

	/**
	 * Render graph for profilers in a given bounds
	 * @param renderer {@link ShapeRenderer} to use, must be ready and set to Line type
	 */
	public static void drawGraph (ShapeRenderer renderer, float x, float y, float width, float height) {
		drawGraph(renderer, x, y, width, height);
	}

	/**
	 * Render graph for profilers in a given bounds
	 * @param renderer {@link ShapeRenderer} to use, must be ready and set to Line type
	 */
	public static void drawGraph (ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
		// we do this so the logical 0 and top are in the middle of the labels
		drawGraphAxis(renderer, x, y, width, height, alpha);
		float sep = height / 7;
		y += sep /2;
		height -= sep;
		graphProfileTimes(renderer, x, y, width, height, alpha);
	}

	private static void drawGraphAxis (ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
		float sep = height / 7;
		y += sep / 2;
		renderer.setColor(GRAPH_V_LINE.r, GRAPH_V_LINE.g, GRAPH_V_LINE.b, alpha);
		renderer.line(x, y, x, y + height - sep);
		renderer.line(x + width, y, x + width, y + height - sep);

		renderer.setColor(GRAPH_H_LINE.r, GRAPH_H_LINE.g, GRAPH_H_LINE.b, alpha);
		for (int i = 0; i < 7; i++) {
			renderer.line(x, y + i * sep , x + width, y + i * sep);
		}
	}

	private static final float NANO_MULTI = 1 / 1000000f;

	static Comparator<SystemProfiler> byLocalMax = new Comparator<SystemProfiler>() {
		@Override public int compare (SystemProfiler o1, SystemProfiler o2) {
			return (int)(o2.getLocalMax() - o1.getLocalMax());
		}
	};

	private static void graphProfileTimes (ShapeRenderer renderer, float x, float y, float width, float height, float alpha) {
		Sort.instance().sort(SystemProfiler.get(), byLocalMax);
		int drawn = 0;
		for (SystemProfiler profiler : SystemProfiler.get()) {
			if (!profiler.getDrawGraph())
				continue;
			if (drawn++ > DRAW_MAX_COUNT)
				break;

			renderer.setColor(profiler.getColor());
			renderer.getColor().a = alpha;
			float sampleLen = width / SystemProfiler.SAMPLES;
			// TODO make this cleaner, maybe find a nice circular buffer to iterate?
			int current = profiler.index;
			for (int i = profiler.times.length - 1; i >= 1; i--) {
				int prev = current == 0 ? profiler.times.length - 1 : current - 1;
				float x1 = profiler.times[prev] * NANO_MULTI;
				float prevPoint = getPoint(x1);
				float x2 = profiler.times[current] * NANO_MULTI;
				float currentPoint = getPoint(x2);
				renderer.line(x + (i - 1) * sampleLen, y + prevPoint * height / 6, x + i * sampleLen, y + currentPoint * height / 6);
				current = prev;
			}
		}
	}

	private static float getPoint(float sampleValue) {
		return sampleValue < 1 ? sampleValue : (MathUtils.log2(sampleValue) + 1);
	}

	/**
	 * Single row for profiler list
	 */
	private static class ProfilerRow extends Table {
		SystemProfiler profiler;
		Label name, max, localMax, avg;
		CheckBox draw;
		float lastMax, lastLocalMax, lastAvg;

		public ProfilerRow (Skin skin) {
			this(null, skin);
		}

		public ProfilerRow(SystemProfiler profiler, Skin skin) {
			super();
			draw = new CheckBox("", skin);
			name = new Label("", skin, STYLE_SMALL);
			max = label("", skin, Align.right);
			localMax = label("", skin, Align.right);
			avg = label("", skin, Align.right);

			add(draw);
			add(name).expandX().fillX();
			add(max).minWidth(MIN_WIDTH);
			add(localMax).minWidth(MIN_WIDTH);
			add(avg).minWidth(MIN_WIDTH);

			if (profiler != null) init(profiler);
		}

		public void init (final SystemProfiler profiler) {
			this.profiler = profiler;
			draw.setChecked(profiler.getDrawGraph());
			draw.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					profiler.setDrawGraph(!profiler.getDrawGraph());
					if (profiler.getDrawGraph()) {
						setChildColor(profiler.getColor());
					} else {
						setChildColor(Color.LIGHT_GRAY);
					}
				}
			});
			name.setText(profiler.getName());
			setChildColor(profiler.getColor());
			lastMax = lastLocalMax = lastAvg = -1;
		}

		private void setChildColor(Color color) {
			name.setColor(color);
			max.setColor(color);
			localMax.setColor(color);
			avg.setColor(color);
		}

		public void update (float delta) {
			// we don't want to update if the change wont affect the representation
			if (!MathUtils.isEqual(lastMax, profiler.getMax(), PRECISION)) {
				lastMax = profiler.getMax();
				max.setText(String.format(FORMAT, lastMax));
			}
			if (!MathUtils.isEqual(lastLocalMax, profiler.getLocalMax(), PRECISION)) {
				lastLocalMax = profiler.getLocalMax();
				localMax.setText(String.format(FORMAT, lastLocalMax));
			}
			if (!MathUtils.isEqual(lastAvg, profiler.getMovingAvg(), PRECISION)) {
				lastAvg = profiler.getMovingAvg();
				avg.setText(String.format(FORMAT, lastAvg));
			}
		}

		public float getAverage () {
			return profiler.getAverage();
		}

		public float getLocalMax () {
			return profiler.getLocalMax();
		}

		public SystemProfiler getProfiler () {
			return profiler;
		}

		public float getMax () {
			return profiler.getMax();
		}
	}

	/**
	 * Simple placeholder for actual graph
	 */
	private class Graph extends Table {
		public Graph () {}

		@Override public float getMinWidth () {
			return 300;
		}

		@Override public float getMinHeight () {
			return 200;
		}
	}

	public void show () {
		setColor(1, 1, 1, 0);
		addAction(Actions.fadeIn(FADE_TIME, Interpolation.fade));
	}

	public void hide () {
		addAction(Actions.sequence(Actions.fadeOut(FADE_TIME, Interpolation.fade), Actions.removeActor()));
	}
}
