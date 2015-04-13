package org.jenkinsci.plugins.mesos;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;

import java.util.logging.Logger;

@Extension
public final class MesosCleanupThread extends AsyncPeriodicWork {

  private static final Logger LOGGER = Logger.getLogger(MesosCleanupThread.class.getName());

  public static final int RECURRANCE_MINUTES = 5;

  public MesosCleanupThread() {
    super("Mesos cleanup thread");
  }

  @Override
  public long getRecurrencePeriod() {
    return MIN * RECURRANCE_MINUTES;
  }

  public static void invoke() {
    getInstance().run();
  }

  private static MesosCleanupThread getInstance() {
    return Jenkins.getInstance().getExtensionList(AsyncPeriodicWork.class)
        .get(MesosCleanupThread.class);
  }

  @Override
  protected void execute(TaskListener listener) {
    Jenkins jenkins = Jenkins.getInstance();
    for (Cloud cloud : jenkins.clouds) {
      if (cloud instanceof MesosCloud) {
        if (((MesosCloud) cloud).isOnDemandRegistration()) {
          // Supervise if Mesos scheduler is running but if there are no builds in queue.
          if (Mesos.getInstance().isSchedulerRunning() && jenkins.getQueue().isEmpty()) {
            LOGGER.info("Mesos Cleanup thread calling supervise...");           
            JenkinsScheduler.supervise();
            LOGGER.info("Mesos Cleanup thread supervise completed.");
          }
        }
      }
    }
  }
}
