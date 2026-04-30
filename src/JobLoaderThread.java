/**
 * JobLoaderThread is Thread 2 from the project description.
 *
 * Its job:
 *     Move processes from jobQueue to readyQueue IF enough memory is available.
 *
 * Simple example:
 *     total memory = 2048 MB
 *     used memory = 0 MB
 *     P1 requires 500 MB
 *
 * Since 0 + 500 <= 2048, P1 can be loaded into memory and moved to readyQueue.
 */
public class JobLoaderThread extends Thread {

    private SharedData data;

    /**
     * Constructor.
     *
     * Example usage:
     *     JobLoaderThread loader = new JobLoaderThread(data);
     *     loader.start();
     */
    public JobLoaderThread(SharedData data) {
        this.data = data;
    }

    /**
     * Runs automatically when Main calls:
     *     loader.start();
     *
     * This thread repeatedly checks the first process in jobQueue.
     */
    @Override
    public void run() {
        while (!data.isSimulationFinished()) {
            PCB nextJob = data.peekJobQueue();

            if (nextJob != null && data.hasEnoughMemoryFor(nextJob)) {
                PCB process = data.removeFromJobQueue();

                data.loadProcessToMemory(process);
                process.setState("READY");

                /*
                 * For the simplest version, all processes arrive at time 0.
                 * Later, when implementing accurate simulation time, the scheduler
                 * can update this value when the process actually enters readyQueue.
                 */
                process.setReadyQueueEnterTime(0);
                process.setLastAgingTime(0);

                data.addToReadyQueue(process);

                System.out.println("Loaded P" + process.getProcessId()
                        + " into memory. Available memory = "
                        + data.getAvailableMemoryMB() + " MB");
            } else {
                /*
                 * If the reader is finished and there are no jobs left, the loader
                 * has no more loading work to do. The scheduler will still execute
                 * the ready processes.
                 */
                if (data.isReaderFinished() && data.isJobQueueEmpty()) {
                    break;
                }

                /*
                 * Yield gives other threads a chance to run.
                 * This avoids a tight loop that wastes CPU.
                 *
                 * We are NOT using Thread.sleep(1) to simulate 1 ms.
                 * Simulation time should be handled by currentTime in Scheduler.
                 */
                Thread.yield();
            }
        }
    }
}
