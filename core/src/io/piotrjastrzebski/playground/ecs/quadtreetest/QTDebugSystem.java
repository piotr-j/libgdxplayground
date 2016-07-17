package io.piotrjastrzebski.playground.ecs.quadtreetest;

import com.artemis.*;
import com.artemis.annotations.Wire;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kotcrab.vis.ui.widget.*;

/**
 * Created by EvilEntity on 31/07/2015.
 */

@Wire
public class QTDebugSystem extends BaseSystem {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Size> mSize;
	private ComponentMapper<Selected> mSelected;
	private ComponentMapper<InQuad> mInQuad;
	EntitySubscription entitySub;
	@Wire ShapeRenderer renderer;
	@Wire ExtendViewport viewport;
	@Wire Table root;

	QTSystem qtSystem;
	VelocitySystem velocitySystem;
	DebugDrawSystem debugDrawSystem;

	public QTDebugSystem () {
		super();
	}

	int numEntities = 75000;
//	int numEntities = 0;
	float minSize = 0.05f;
//	float minSize = 2f;
	float maxSize = 0.2f;
//	float maxSize = 2.0f;
	float maxVelocity = 0.5f;
//	float maxVelocity = 0f;
	boolean exact = true;
	int maxInBucket = 16;
	int maxDepth = 6;
	float staticPer = 0.5f;
	boolean linearSearch;
	int iterations = 1;
	VisLabel fps;
	@Override protected void initialize () {
		super.initialize();
		AspectSubscriptionManager manager = world.getSystem(AspectSubscriptionManager.class);
		entitySub = manager.get(Aspect.all(Position.class, Size.class));
		VisTable container = new VisTable(true);
		VisWindow window = new VisWindow("Config, options that modify entities require restart");
		VisTextButton resetBtn = new VisTextButton("Restart");
		resetBtn.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				reset();
			}
		});
		container.add(resetBtn);
		fps = new VisLabel("????????????????????????????????");
		container.add(fps);
		container.row();
		final VisCheckBox exactCB = new VisCheckBox("Exact search", true);
		exactCB.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				exact = exactCB.isChecked();
			}
		});
		container.add(exactCB);
		VisTextButton pauseBtn = new VisTextButton("Pause", "toggle");
		pauseBtn.setChecked(qtSystem.isEnabled());
		pauseBtn.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				qtSystem.setEnabled(!qtSystem.isEnabled());
				velocitySystem.setEnabled(qtSystem.isEnabled());
			}
		});
		container.add(pauseBtn);
		container.row();
		final VisLabel numEntLabel = new VisLabel(numEntities + " entities");

		final VisSlider numESlider = new VisSlider(0, 1000, 25, false);
		numESlider.setValue(0);
		final VisSlider numEKSlider = new VisSlider(0, 500000, 1000, false);
		numEKSlider.setValue(25000);

		ChangeListener numEntListener = new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				numEntities = (int)(numESlider.getValue() + numEKSlider.getValue());
				numEntLabel.setText(numEntities + " entities");
			}
		};
		numESlider.addListener(numEntListener);
		container.add(numESlider);
		numEKSlider.addListener(numEntListener);
		container.add(numEKSlider);
		container.add(numEntLabel);
		container.row();

		final VisTextButton linearBtn = new VisTextButton("Linear Search (slow)", "toggle");
		linearBtn.setChecked(linearSearch);
		linearBtn.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				linearSearch = linearBtn.isChecked();
				qtSystem.setEnabled(!linearSearch);
			}
		});
		container.add(linearBtn);
		container.row();

		final VisLabel minSizeLabel = new VisLabel("");
		final VisSlider minSizeSlider = new VisSlider(0.025f, 2.0f, 0.025f, false);
		minSizeSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				minSize = minSizeSlider.getValue();
				minSizeLabel.setText(minSize + " min size");
			}
		});
		minSizeSlider.setValue(minSize);
		container.add(minSizeSlider);
		container.add(minSizeLabel);
		container.row();

		final VisLabel maxSizeLabel = new VisLabel("");
		final VisSlider maxSizeSlider = new VisSlider(0.025f, 2.0f, 0.025f, false);
		maxSizeSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				maxSize = maxSizeSlider.getValue();
				maxSizeLabel.setText(maxSize + " max size");
			}
		});
		maxSizeSlider.setValue(maxSize);
		container.add(maxSizeSlider);
		container.add(maxSizeLabel);
		container.row();

		final VisLabel maxVelLabel = new VisLabel("");
		final VisSlider maxVelSlider = new VisSlider(0.0f, 6.0f, 0.1f, false);
		maxVelSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				maxVelocity = maxVelSlider.getValue();
				maxVelLabel.setText(maxVelocity + " max velocity");
			}
		});
		maxVelSlider.setValue(maxVelocity);
		container.add(maxVelSlider);
		container.add(maxVelLabel);
		final VisLabel staticPerLabel = new VisLabel("");
		final VisSlider staticPerSlider = new VisSlider(0.0f, 1.0f, 0.05f, false);
		staticPerSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				staticPer = staticPerSlider.getValue();
				staticPerLabel.setText(100 * staticPer + " % static");
			}
		});
		staticPerSlider.setValue(staticPer);
		container.add(staticPerSlider);
		container.add(staticPerLabel);
		container.row();

		final VisCheckBox drawQuadTree = new VisCheckBox("QuadTree", true);
		drawQuadTree.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				debugDrawSystem.drawQuadTree = drawQuadTree.isChecked();
			}
		});
		container.add(drawQuadTree);

		final VisCheckBox drawQTTouched = new VisCheckBox("QT Touched", true);
		drawQTTouched.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				debugDrawSystem.drawQuadTreeTouched = drawQTTouched.isChecked();
			}
		});
		container.add(drawQTTouched);
		container.row();

		final VisCheckBox drawSelected = new VisCheckBox("Selected", true);
		drawSelected.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				debugDrawSystem.drawSelected = drawSelected.isChecked();
			}
		});
		container.add(drawSelected);

		final VisCheckBox drawInQuad = new VisCheckBox("InQuad", true);
		drawInQuad.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				debugDrawSystem.drawInQuad = drawInQuad.isChecked();
			}
		});
		container.add(drawInQuad);
		final VisCheckBox drawRest = new VisCheckBox("Rest", true);
		drawRest.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				debugDrawSystem.drawRest = drawRest.isChecked();
			}
		});
		container.add(drawRest);
		container.row();

		final VisLabel maxInBucketLabel = new VisLabel("");
		final VisSlider maxInBucketSlider = new VisSlider(1, 16, 1, false);
		maxInBucketSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				maxInBucket = (int)maxInBucketSlider.getValue();
				maxInBucketLabel.setText(maxInBucket + " max in bucket");
				QuadTree.MAX_IN_BUCKET = maxInBucket;
			}
		});
		maxInBucketSlider.setValue(maxInBucket);
		container.add(maxInBucketSlider);
		container.add(maxInBucketLabel);

		final VisLabel maxDepthLabel = new VisLabel("");
		final VisSlider maxDepthSlider = new VisSlider(1, 16, 1, false);
		maxDepthSlider.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				maxDepth = (int)maxDepthSlider.getValue();
				maxDepthLabel.setText(maxDepth + " max depth");
				QuadTree.MAX_DEPTH = maxDepth;
			}
		});
		maxDepthSlider.setValue(maxDepth);
		container.add(maxDepthSlider);
		container.add(maxDepthLabel);
		container.row();
		final VisCheckBox rebuild = new VisCheckBox("Rebuild");
		rebuild.setChecked(qtSystem.rebuild);
		rebuild.addListener(new ChangeListener() {
			@Override public void changed (ChangeEvent event, Actor actor) {
				qtSystem.rebuild = rebuild.isChecked();
			}
		});
		container.add(rebuild);
		container.row();

		window.add(container);
		window.addCloseButton();
		window.pack();
		root.getStage().addActor(window);
		reset();
	}

	private void reset() {

		IntBag actives = entitySub.getEntities();
		for (int i = 0; i < actives.size(); i++) {
			Entity e = world.getEntity(actives.get(i));
			e.deleteFromWorld();
		}

		QuadTree.MAX_DEPTH = maxDepth;
		QuadTree.MAX_IN_BUCKET = maxInBucket;

		for (int i = 0; i < numEntities; i++) {
			createEntity();
		}
	}

	private void createEntity(){
		float x = MathUtils.random(-19.f, 19.f);
		float y = MathUtils.random(-10.f, 10.f);
		createEntity(x, y);
	}

	private void createEntity(float x, float y){
		Entity entity = world.createEntity();
		EntityEdit edit = entity.edit();
		Position position = edit.create(Position.class);
		position.x = x;
		position.y = y;
		Size size = edit.create(Size.class);
		size.width = MathUtils.random(minSize, maxSize);
		size.height = MathUtils.random(minSize, maxSize);
		if (MathUtils.random() > staticPer) {
			Velocity velocity = edit.create(Velocity.class);
			velocity.x = MathUtils.random(-maxVelocity, maxVelocity);
			velocity.y = MathUtils.random(-maxVelocity, maxVelocity);
		}
	}

	long nanoDiff;
	long startTime = TimeUtils.nanoTime();
	IntBag fill = new IntBag();
	@Override protected void processSystem () {
		QuadTree quadTree = qtSystem.getQuadTree();
		fill.clear();
		if (linearSearch) {
			IntBag actives = entitySub.getEntities();
			long start = System.nanoTime();
			for (int i = 0; i < iterations; i++) {
				fill.clear();
				for (int id = 0; id < actives.size(); id++) {
					Entity e = world.getEntity(actives.get(id));
					Position position = mPosition.get(e);
					Size size = mSize.get(e);
					temp1.set(position.x, position.y, size.width, size.height);
					if (selected.overlaps(temp1)) {
						fill.add(e.getId());
					}
				}
			}
			nanoDiff = System.nanoTime() - start;
			for (int i = 0; i < fill.size(); i++) {
				Entity e = world.getEntity(fill.get(i));
				if (!mSelected.has(e)) e.edit().create(Selected.class);
			}
		} else {
			if (exact) {
				long start = System.nanoTime();
				for (int i = 0; i < iterations; i++) {
					fill.clear();
					quadTree.getExact(fill, selected.x, selected.y, selected.width, selected.height);
				}
				nanoDiff = System.nanoTime() - start;
			} else {
				long start = System.nanoTime();
				for (int i = 0; i < iterations; i++) {
					fill.clear();
					quadTree.get(fill, selected.x, selected.y, selected.width, selected.height);
				}
				nanoDiff = System.nanoTime() - start;
			}
			for (int i = 0; i < fill.size(); i++) {
				Entity e = world.getEntity(fill.get(i));
				if (e == null) continue;
				EntityEdit edit = e.edit();
				if (exact) {
					if (!mSelected.has(e)) edit.create(Selected.class);
				} else {
					Position position = mPosition.get(e);
					Size size = mSize.get(e);
					temp1.set(position.x, position.y, size.width, size.height);
					if (selected.overlaps(temp1)) {
						if (!mSelected.has(e)) edit.create(Selected.class);
					} else {
						if (!mInQuad.has(e)) edit.create(InQuad.class);
					}
				}
			}
		}
		renderer.setProjectionMatrix(viewport.getCamera().combined);
		renderer.setColor(Color.ORANGE);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		renderer.rect(selected.x, selected.y, selected.width, selected.height);
		renderer.end();
		if (TimeUtils.nanoTime() - startTime > 1000000000) /* 1,000,000,000ns == one second */{
			fps.setText(Integer.toString(Gdx.graphics.getFramesPerSecond()));
			startTime = TimeUtils.nanoTime();
		}
		sb.setLength(0);
		if (linearSearch) {
			sb.append(nanoDiff / 1000000);
		} else {
			sb.append(qtSystem.diff / 1000000);
			sb.append(" update(), ");
			sb.append(nanoDiff / 1000000);
		}
		sb.append(" millis (");
		sb.append(iterations);
		sb.append(")");
		fps.setText(sb);
	}
	StringBuilder sb = new StringBuilder();

	Vector2 start = new Vector2();
	Rectangle selected = new Rectangle(-2.5f, -2.5f, 5, 5);
	Rectangle temp1 = new Rectangle();
	Rectangle temp2 = new Rectangle();
	boolean dragging;
	/**
	 * Touch point in world coordinates
	 */
	public void touched (float x, float y, int button) {
		if (button == Input.Buttons.LEFT) {
			createEntity(x, y);
		} else if (button == Input.Buttons.MIDDLE) {
			start.set(x, y);
			selected.setPosition(x, y);
			dragging = true;
		} else {
			temp1.set(x - 0.1f, y - 0.1f, 0.2f, 0.2f);
			fill.clear();
			qtSystem.getQuadTree().get(fill, temp1.x, temp1.y, temp1.width, temp1.height);
//			Gdx.app.log("", "found: " + fill.size);
			for (int i = 0; i < fill.size(); i++) {
				Entity e = world.getEntity(fill.get(i));
				Position position = mPosition.get(e);
				Size size = mSize.get(e);
				temp2.set(position.x, position.y, size.width, size.height);
				if (temp1.overlaps(temp2)) {
					e.deleteFromWorld();
					// delete only first one we find
					return;
				}
			}
		}
	}

	public void drag(float x, float y) {
		if (!dragging) return;

		float width = start.x - x;
		float height = start.y - y;
		// flip width
		if (width < 0) {
			width = -width;
			x = x - width;
		}
		// flip width
		if (height < 0) {
			height = -height;
			y = y - height;
		}
		selected.set(x, y, width, height);
	}

	public void touchUp (float x, float y, int button) {
		if (button == Input.Buttons.MIDDLE) {
			dragging = false;
		}
	}
}
