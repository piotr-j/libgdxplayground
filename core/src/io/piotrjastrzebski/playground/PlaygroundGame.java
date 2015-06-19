package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import io.piotrjastrzebski.playground.tiledgentest.*;

public class PlaygroundGame extends Game {
	
	@Override
	public void create () {
//		setScreen(new TiledGenTest());
//		setScreen(new TemperatureTest());
//		setScreen(new BiomeTest());
//		setScreen(new RainTest());
//		setScreen(new BlurTest());
		setScreen(new CompositeGenTest());
	}
}
