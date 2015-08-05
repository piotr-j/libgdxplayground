/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.piotrjastrzebski.playground.ecs.aitest.dog;

import com.artemis.Component;

/** @author implicit-invocation
 * @author davebaol */
public class Dog extends Component {

	public String name;
	public String brainLog;

	public Dog () {

	}

	public Dog (String name) {
		setName(name);
	}

	public void setName (String name) {
		this.name = name;
		this.brainLog = name + " brain";
	}

	private boolean urgent = false;

	public boolean isUrgent () {
		return urgent;
	}

	public void setUrgent (boolean urgent) {
		this.urgent = urgent;
	}

	@Override public String toString () {
		return "Dog{" +
			"name='" + name + '\'' +
			'}';
	}
}
