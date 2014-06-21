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
    final int numRecordsToProcess = 10;
    final long millisecondsBetween = 1000L;

    LOGGER.info("****************** Submitting InterruptableRunnable ******************");
    submitRunnable(executor, new InterruptableRunnable(numRecordsToProcess, millisecondsBetween));

    Thread.sleep(10);

    LOGGER.info("****************** Submitting NonInterruptableRunnable ******************");
    submitRunnable(executor, new NonInterruptableRunnable(numRecordsToProcess, millisecondsBetween));

    LOGGER.info("Shutting down thread pool");
    executor.shutdown();
    LOGGER.info("Waiting for shutdown...");
    while (!executor.isTerminated()) {}
    LOGGER.info("Finished all threads");
  }

  /**
   * Submits a runnable to the given executor and then waits for 5 seconds after submitting the runnable to cancel it.
   *
   * @param executor the thread executor pool
   * @throws InterruptedException if the parent thread is interrupted while waiting to interrupt the runnable
   */
  private static void submitRunnable(final ExecutorService executor, final Runnable runnable)
      throws InterruptedException {

    Future<?> future = executor.submit(runnable);

    LOGGER.info("I am going to interrupt your sleep in 5 seconds...");
    Thread.sleep(5000L);
    LOGGER.info("Interrupting now");
    future.cancel(/* mayInterruptIfRunning */true);
  }

  /**
   * Simple runnable which attempts to process 10 records, with a 1 second wait in between each record. However, if it
   * is interrupted, then it stops sleeping and gracefully completes.
   */
  private static class InterruptableRunnable implements Runnable {

    private static Logger LOGGER = Logger.getLogger(InterruptableRunnable.class);

    private final int numToProcess;

    private final long sleepDuration;

    public InterruptableRunnable(final int numToProcess, final long sleepDuration) {
      this.numToProcess = numToProcess;
      this.sleepDuration = sleepDuration;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      LOGGER.info(String.format("I am running, my name is: %s", Thread.currentThread().getName()));
      for (int i = 0; i < numToProcess; i++) {
        LOGGER.info(String.format("Processing record at index %d", i));
        try {
          sleep(sleepDuration);
        }
        catch (InterruptedException e) {
          LOGGER.info("I have been interrupted from my sleep, so I need to stop processing records.");
          Thread.currentThread().interrupt(); // reset the interrupt flag so callers can use it
          return; // don't do anything else since I have been interrupted
        }
      }
      LOGGER.info("I completed my sleep without interruptions.");
    }

    private void sleep(final long millisecondsToSleep) throws InterruptedException {
      Thread.sleep(millisecondsToSleep);
    }

  }

  /**
   * Runnable which cannot be easily interrupted, since it does not handle the InterruptedException properly.
   */
  private static class NonInterruptableRunnable implements Runnable {

    private static Logger LOGGER = Logger.getLogger(NonInterruptableRunnable.class);

    private final int numToProcess;

    private final long sleepDuration;

    public NonInterruptableRunnable(final int numToProcess, final long sleepDuration) {
      this.numToProcess = numToProcess;
      this.sleepDuration = sleepDuration;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      LOGGER.info(String.format("I am running, my name is: %s", Thread.currentThread().getName()));
      for (int i = 0; i < numToProcess; i++) {
        LOGGER.info(String.format("Processing record at index %d", i));
        sleep(sleepDuration);
      }
      LOGGER.info("I completed my sleep without interruptions.");
    }

    private void sleep(final long millisecondsToSleep) {
      try {
        Thread.sleep(millisecondsToSleep);
      }
      catch (InterruptedException e) {
        LOGGER.info("Calling interrupt on the current thread.");
        Thread.currentThread().interrupt(); // this alone won't stop the thread, it will only set the interrupted flag
                                            // on the thread
      }
    }

  }

}
