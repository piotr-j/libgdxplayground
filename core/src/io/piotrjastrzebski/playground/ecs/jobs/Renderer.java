package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by EvilEntity on 17/08/2015.
 */
@Wire
public class Renderer extends EntityProcessingSystem {
	private ComponentMapper<Godlike> mGodlike;
	@Wire(name = "game-cam") OrthographicCamera camera;
	@Wire ShapeRenderer renderer;
	public Renderer () {
		super(Aspect.all(Godlike.class));
	}

	@Override protected void begin () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
	}

	@Override protected void process (Entity e) {
		Godlike godlike = mGodlike.get(e);
		renderer.setColor(godlike.color);
		renderer.rect(godlike.x, godlike.y, godlike.width, godlike.height);
		if (godlike.selected) {
			renderer.getColor().a = 0.5f;
			renderer.rect(godlike.x - 0.2f, godlike.y - 0.2f, godlike.width + 0.4f, godlike.height + 0.4f);
		}
	}

	@Override protected void end () {
		renderer.end();
	}
}
