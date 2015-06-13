package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import io.piotrjastrzebski.playground.tiledgentest.TiledGenTest;

public class PlaygroundGame extends Game {
	
	@Override
	public void create () {
		setScreen(new TiledGenTest());
	}
}
