package io.piotrjastrzebski.playground.ecs.aijobs.components;

import com.artemis.Component;
import com.badlogic.gdx.ai.btree.BehaviorTree;

/**
 * Created by EvilEntity on 17/08/2015.
 */
public class Worker extends Component {
	// id if entity
	public int eid = -1;
	public int jobID = -1;
	public String name;
	public BehaviorTree<Worker> ai;

	public void claim (Job job) {
		jobID = job.eid;
		job.workerID = eid;
	}

	@Override public String toString () {
		return "Worker " + name;
	}
}
