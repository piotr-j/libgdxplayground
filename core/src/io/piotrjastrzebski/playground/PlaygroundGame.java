package io.piotrjastrzebski.playground;

import com.badlogic.gdx.Game;
import io.piotrjastrzebski.playground.asyncscreentest.AsyncScreenTest;
import io.piotrjastrzebski.playground.clientserver.CSTest;
import io.piotrjastrzebski.playground.clientserverv2.CSTestV2;
import io.piotrjastrzebski.playground.tagtest.TagTest;
import io.piotrjastrzebski.playground.tiledgentest.*;
import io.piotrjastrzebski.playground.uitesting.UITest;

public class PlaygroundGame extends Game {
	
	@Override
	public void create () {
//		setScreen(new TiledGenTest());
//		setScreen(new TemperatureTest());
//		setScreen(new BiomeTest());
//		setScreen(new RainTest());
//		setScreen(new BlurTest());
//		setScreen(new CompositeGenTest());
//		setScreen(new UITest());
//		setScreen(new CSTest());
//		setScreen(new CSTestV2());
//		setScreen(new TagTest());
		setScreen(new AsyncScreenTest());
	}
}
