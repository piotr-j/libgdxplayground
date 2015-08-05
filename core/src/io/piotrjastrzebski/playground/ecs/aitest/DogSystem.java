package io.piotrjastrzebski.playground.ecs.aitest;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.ecs.aitest.dog.Dog;

/**
 * Created by PiotrJ on 04/08/15.
 */
public class DogSystem extends BaseSystem {

	@Override protected void processSystem () {

	}


	public void bark (Dog dog) {
		log(dog, "Bow wow!!!");
	}

	public void startWalking (Dog dog) {
		log(dog, "Dog starts walking");
	}

	public void randomlyWalk (Dog dog) {
		log(dog, "Dog walks randomly around!");
	}

	public void stopWalking (Dog dog) {
		log(dog, "Dog stops walking");
	}

	public boolean standBesideATree (Dog dog) {
		if (Math.random() < 0.5) {
			log(dog, "No tree found :(");
			return false;
		}
		return true;
	}

	public void markATree (Dog dog) {
		log(dog, "Dog lifts a leg and pee!");
	}


	public void log (Dog dog, String msg) {
		Gdx.app.log(dog.name, msg);
	}

	public void brainLog (Dog dog, String msg) {
		Gdx.app.log(dog.brainLog, msg);
	}
}
