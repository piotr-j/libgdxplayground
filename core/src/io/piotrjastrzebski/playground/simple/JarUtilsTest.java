package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.JarUtils;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class JarUtilsTest extends BaseScreen {
	private final static String TAG = JarUtilsTest.class.getSimpleName();

	public JarUtilsTest (GameReset game) {
		super(game);
		JarFile jar = JarUtils.getJarFor(this.getClass());

		if (jar != null) {
			Gdx.app.log(TAG, "jar: " + jar.getName());
			try {
				File particles = Gdx.files.external("particles").file();
				particles.mkdirs();
				Gdx.app.log(TAG, "extracting particles to " + particles.getAbsolutePath());
				JarUtils.extractResources(jar, "particles", particles.getAbsolutePath());
			} catch (IOException e) {
				Gdx.app.error(TAG, "Failed to unpack things! ", e);
			}
		} else {
			Gdx.app.log(TAG, "jar not found, not running from a jar!");
		}
	}
}
