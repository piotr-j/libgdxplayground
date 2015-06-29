package io.piotrjastrzebski.playground.tagtest.systems;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.UuidEntityManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.piotrjastrzebski.playground.tagtest.components.JobComponent;
import io.piotrjastrzebski.playground.tagtest.components.JobDelegateComponent;
import io.piotrjastrzebski.playground.tagtest.components.WorkerComponent;

import java.util.UUID;

/**
 * Created by PiotrJ on 25/06/15.
 */
@Wire public class JobSystem extends EntityProcessingSystem {
    ComponentMapper<JobDelegateComponent> delegateM;
    ComponentMapper<WorkerComponent> workerM;

    ObjectMap<String, Array<Job>> availableJobsByTag = new ObjectMap<>();
    ObjectMap<String, Array<Job>> claimedJobsByTag = new ObjectMap<>();
    // we need a fast way to find a job via entity id
    IntMap<Job> jobByID = new IntMap<>();
    ObjectMap<UUID, Job> jobByUUID = new ObjectMap<>();

    public JobSystem() {
        super(Aspect.getAspectForAll(JobDelegateComponent.class));
        setPassive(true);
    }

    ObjectMap<String, JobHandler> tagToHandler = new ObjectMap<>();

    public void add(String tag, JobHandler handler) {
        // TODO check for override, nulls
        tagToHandler.put(tag, handler);
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void inserted(Entity e) {
        JobDelegateComponent delegateComponent = delegateM.get(e);
        String aClass = delegateComponent.getActualClassStr();
        if (aClass == null) {
            return;
        }
        // find actual job component via its class name
        JobComponent actual = null;
        try {
            Class jobClass = ClassReflection.forName(aClass);
            if (JobComponent.class.isAssignableFrom(jobClass)) {
                actual = (JobComponent) e.getComponent(jobClass);
                delegateComponent.setActual(actual);
            }
        } catch (ReflectionException ex) {
            Gdx.app.error("", ex.getMessage());
        }
        // re assign stuff
        actual = delegateComponent.getActual();
        if (actual != null) {
            Array<Job> jobs = availableJobsByTag.get(actual.tag, null);
            if (jobs == null) {
                jobs = new Array<>();
                availableJobsByTag.put(actual.tag, jobs);
            }
            Job job;
            // note should be pooled
            jobs.add(job = new Job(e.getId(), e.getUuid(), actual));
            // quick access by entity id, we cant get it via mapper as we dont know the class
            jobByID.put(job.id, job);
            jobByUUID.put(job.uuid, job);

            // we are reloading the game, restore the job
            if (job.comp.claimedBy != null) {
                restoreJob(job);
            }
        }
    }

    @Override
    protected void process(Entity e) {
        // note passive system, do nothing
    }

    @Override
    protected void removed(Entity e) {
        JobDelegateComponent delegateComponent = delegateM.get(e);

        JobComponent actual = delegateComponent.getActual();
        if (actual != null) {
            Job job = jobByID.get(e.getId());
            Array<Job> jobs = availableJobsByTag.get(actual.tag, null);
            if (jobs != null) {
                // is null if we called finishJob before
                if (job != null) {
                    jobs.removeValue(job, true);
                }
                Gdx.app.log("", "Job removed " + job);
            }
        }

    }

    public void findJob(Entity worker) {
        WorkerComponent workerComponent = workerM.get(worker);
        /* NOTE
            we want to find best job we can in reasonable amount of time

            we probably need some tags for the jobs, classes are unwieldy
         */

        Job bestJob = null;
        findJob:
        // jobPref should be sorted in order of priority
        for (String jobTag : workerComponent.jobPref) {
            Array<Job> jobs = availableJobsByTag.get(jobTag, null);
            // no jobs for this priority
            if (jobs == null || jobs.size == 0) continue;
            // find best job
            for (Job job : jobs) {
                // TODO do we want this? or move to other queue... if we release the job we need it to go at its old position
                if (job.comp.claimed) continue;
                // NOTE just take first one for testing, in real app do something more sensible
                bestJob = job;
                break findJob;
            }
        }

        if (bestJob != null) {
            workerComponent.job = bestJob.comp;
            bestJob.comp.claim(worker);
            workerComponent.jobEntityID = bestJob.id;
            workerComponent.setJob(bestJob);
        } else {
            Gdx.app.log("", "No job for " + worker);
        }
    }

    public void finishJob(Entity worker) {
        Gdx.app.log("", worker+ " finished job");
        WorkerComponent workerComponent = workerM.get(worker);

        String tag = workerComponent.job.tag;

        Array<Job> jobs = availableJobsByTag.get(tag, null);
        if (jobs != null) {
            jobs.removeValue(jobByID.get(workerComponent.jobEntityID), true);
        }
        jobByID.remove(workerComponent.jobEntityID);


        JobHandler jobHandler = tagToHandler.get(tag, null);
        if (jobHandler != null) {
            jobHandler.finishJob(world.getEntity(workerComponent.jobEntityID));
        } else {
            Gdx.app.log(",", "No handler registered for tag " + tag);
        }
        workerComponent.clearJob();
    }

    public void releaseJob(Entity worker) {
        // move the job back in queue
        Gdx.app.log("", worker + " release job");
        WorkerComponent workerComponent = workerM.get(worker);
        // can be picked up again
        jobByID.get(workerComponent.jobEntityID).comp.claimed = false;
    }

    @Wire private UuidEntityManager uuidManager;
    private void restoreJob(Job job) {
        Entity worker = uuidManager.getEntity(job.comp.claimedBy);
        WorkerComponent workerComponent = workerM.get(worker);
        workerComponent.job = job.comp;
        job.comp.claim(worker);
        workerComponent.jobEntityID = job.id;
        workerComponent.setJob(job);
    }

    public class Job {
        // we can use this for access during single session, wont work after re/load
        public int id;
        public JobComponent comp;
        // permanent id of the entity that contains the component
        public UUID uuid;

        public Job(int id, UUID uuid, JobComponent jobComponent) {
            this.id = id;
            this.uuid = uuid;
            this.comp = jobComponent;
        }

        @Override
        public String toString() {
            return "Job{" +
                    "id=" + id +
                    ", comp=" + comp +
                    '}';
        }
    }
}
