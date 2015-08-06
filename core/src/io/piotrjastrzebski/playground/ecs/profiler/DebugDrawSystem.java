package io.piotrjastrzebski.playground.ecs.profiler;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.piotrjastrzebski.playground.ecs.quadtreetest.InQuad;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Position;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Selected;
import io.piotrjastrzebski.playground.ecs.quadtreetest.Size;

@Wire
@com.artemis.annotations.Profile(using = SystemProfiler.class, enabled = SystemProfiler.ENABLED)
public class DebugDrawSystem extends EntityProcessingSystem implements ProfilerConfig {
	private ComponentMapper<Position> mPosition;
	private ComponentMapper<Size> mSize;
	private ComponentMapper<Selected> mSelected;
	private ComponentMapper<InQuad> mInQuad;
	@Wire(name = "game") OrthographicCamera camera;
	@Wire ShapeRenderer renderer;
	QTSystem qtSystem;
	public DebugDrawSystem () {
		super(Aspect.all(Position.class, Size.class));
	}

	@Override protected void begin () {
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.GREEN);
		renderer.begin(ShapeRenderer.ShapeType.Line);
		drawQT(renderer, qtSystem.getQuadTree(), false);
	}

	private void drawQT(ShapeRenderer renderer, QTSystem.QuadTree quadTree, boolean touched) {
		QTSystem.QuadTree[] nodes = quadTree.getNodes();
		if (nodes != null && nodes[0] != null) {
			for (QTSystem.QuadTree tree : nodes) {
				drawQT(renderer, tree, touched);
			}
		}
		QTSystem.Container bounds = quadTree.getBounds();
		renderer.setColor(Color.BLUE);
		renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public boolean drawSelected = true;
	public boolean drawInQuad = true;
	public boolean drawRest = true;
	@Override protected void process (Entity e) {
		Position position = mPosition.get(e);
		Size size = mSize.get(e);
		if (mSelected.has(e) && drawSelected) {
			renderer.setColor(Color.GREEN);
			e.edit().remove(Selected.class);
			renderer.rect(position.x, position.y, size.width, size.height);
		} else if (mInQuad.has(e) && drawInQuad) {
			renderer.setColor(Color.OLIVE);
			e.edit().remove(InQuad.class);
			renderer.rect(position.x, position.y, size.width, size.height);
		} else if (drawRest) {
			if (position.dirty) {
				renderer.setColor(Color.RED);
			} else {
				renderer.setColor(Color.MAROON);
			}
			renderer.rect(position.x, position.y, size.width, size.height);
		}
	}

	@Override protected void end () {
		renderer.end();

	}

	@Override public Type getType () {
		return Type.RENDER;
	}

	@Override public float getRefreshRate () {
		return 0;
	}

	@Override public void setColor (Color color) {

	}

	@Override public String getName () {
		return null;
	}
}
