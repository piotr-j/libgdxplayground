package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Json;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class AnonJsonTest extends BaseScreen {
	private static final String TAG = AnonJsonTest.class.getSimpleName();

	public AnonJsonTest (GameReset game) {
		super(game);
		Json json = new Json();
		Job job1 = new Job();
		job1.name = "job1";
		job1.listener = new MyJobListener();
		job1.listener.print();

		String toJson = json.toJson(job1);
		Gdx.app.log("", json.prettyPrint(job1));
		Job job = json.fromJson(Job.class, toJson);
		job.listener.print();
	}

	public static class Job {
		public String name;
		public JobListener listener;
	}

	public interface JobListener {
		void print();
	}

	@Override public void render (float delta) {
		Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, AnonJsonTest.class);
	}

	private static class MyJobListener implements JobListener {
		@Override public void print () {
			Gdx.app.log("Listener", "welp " + this);
		}
	}
}
