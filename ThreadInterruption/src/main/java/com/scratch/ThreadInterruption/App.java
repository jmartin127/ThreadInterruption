package com.scratch.ThreadInterruption;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * This class represents a simple example of one way to handle thread interruption within an application.
 */
public class App {

  private static Logger LOGGER = Logger.getLogger(App.class);

  /**
   * Main method for kicking off the application.
   *
   * @param args the command-line argumetns
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {
    BasicConfigurator.configure();
    final ExecutorService executor = Executors.newFixedThreadPool(5);
    submitSleepingRunnable(executor, /* shouldInterrupt */true);
    Thread.sleep(10);
    submitSleepingRunnable(executor, /* shouldInterrupt */false);

    LOGGER.info("Shutting down thread pool");
    executor.shutdown();
    LOGGER.info("Waiting for shutdown...");
    while (!executor.isTerminated()) {}
    LOGGER.info("Finished all threads");
  }

  /**
   * Submits a runnable to the given executor. If souldInterrupt is true, then 5 seconds after submitting the runnable,
   * it will cancel it.
   *
   * @param executor the thread executor pool
   * @param shouldInterrupt whether or not the submitted job should be canceled after 5 seconds
   * @throws InterruptedException if the parent thread is interrupted while waiting to interrupt the runnable
   */
  private static void submitSleepingRunnable(final ExecutorService executor, boolean shouldInterrupt)
      throws InterruptedException {
    LOGGER.info("******************* Kicking off a new sleeping runnable ************************");
    LOGGER.info("Should interrupt? " + shouldInterrupt);
    LOGGER.info("Creating runnable");
    final Runnable runnable = new SleepingRunnable();
    LOGGER.info("Submitting runnable");
    Future<?> future = executor.submit(runnable);

    if (shouldInterrupt) {
      LOGGER.info("I am going to interrupt your sleep in 5 seconds...");
      Thread.sleep(5000L);
      LOGGER.info("Interrupting now");
      future.cancel(true);
    }
  }

  /**
   * Simple runnable which attempts to sleep for 30 seconds. However, if it is interrupted, then it stops sleeping and
   * gracefully completes.
   */
  private static class SleepingRunnable implements Runnable {

    private static Logger LOGGER = Logger.getLogger(SleepingRunnable.class);

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      LOGGER.info(String.format("I am running, my name is: %s", Thread.currentThread().getName()));
      try {
        sleep(30000L);
      }
      catch (InterruptedException e) {
        LOGGER.info("I have been interrupted from my sleep, so I need to stop sleeping.");
        Thread.currentThread().interrupt(); // reset the interrupt flag so callers can use it
        return; // don't do anything else since I have been interrupted
      }
      LOGGER.info("I completed my sleep without interruptions.");
    }

    private void sleep(final long milliSecondsToSleep) throws InterruptedException {
      LOGGER.info(String.format("I am going to try to sleep for %d milliseconds.", milliSecondsToSleep));
      Thread.sleep(milliSecondsToSleep);
      LOGGER.info("Done sleeping.");
    }

  }

}
