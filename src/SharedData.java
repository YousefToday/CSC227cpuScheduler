import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * SharedData is the shared box used by all parts of the program.
 *
 * It stores:
 *     1. jobQueue       -> processes read from job.txt but not loaded into memory yet
 *     2. readyQueue     -> processes loaded into memory and ready for CPU
 *     3. allProcesses   -> all processes, used for final table and averages
 *     4. ganttChart     -> all CPU execution segments
 *     5. memory counter -> total memory and used memory
 *
 * Why synchronized methods?
 *     Because JobReaderThread, JobLoaderThread, and Scheduler may access the
 *     same queues/memory at the same time.
 */
public class SharedData {

    // Project constants. Keeping them here makes them easy to find.
    public static final int TOTAL_MEMORY_MB = 2048;
    public static final int ROUND_ROBIN_QUANTUM = 5;
    public static final int AGING_INTERVAL = 4;

    private Queue<PCB> jobQueue;
    private ArrayList<PCB> readyQueue;
    private ArrayList<PCB> allProcesses;
    private ArrayList<GanttEntry> ganttChart;

    /*
     * We do NOT use int[] memory = new int[2048].
     *
     * The project only asks us to check whether enough MB are available.
     * So we use a simple counter.
     *
     * Example:
     *     usedMemoryMB = 500
     *     availableMemory = 2048 - 500 = 1548 MB
     */
    private int usedMemoryMB;

    private boolean readerFinished;
    private boolean simulationFinished;

    /**
     * Creates empty queues/lists and sets used memory to 0.
     *
     * Example usage:
     *     SharedData data = new SharedData();
     */
    public SharedData() {
        jobQueue = new LinkedList<PCB>();
        readyQueue = new ArrayList<PCB>();
        allProcesses = new ArrayList<PCB>();
        ganttChart = new ArrayList<GanttEntry>();
        usedMemoryMB = 0;
        readerFinished = false;
        simulationFinished = false;
    }

    // --------------------------------------------------
    // Job queue methods
    // --------------------------------------------------

    /**
     * Adds a process to the job queue.
     * Used by JobReaderThread after reading one line from job.txt.
     *
     * Example:
     *     data.addToJobQueue(process);
     */
    public synchronized void addToJobQueue(PCB process) {
        jobQueue.add(process);
    }

    /**
     * Looks at the first process in the job queue without removing it.
     * Used by JobLoaderThread to check whether the next job can fit in memory.
     *
     * Example:
     *     PCB next = data.peekJobQueue();
     */
    public synchronized PCB peekJobQueue() {
        return jobQueue.peek();
    }

    /**
     * Removes and returns the first process from the job queue.
     * Used when the loader decides that the process can enter memory.
     *
     * Example:
     *     PCB process = data.removeFromJobQueue();
     */
    public synchronized PCB removeFromJobQueue() {
        return jobQueue.poll();
    }

    /**
     * Returns true if the job queue is empty.
     * Useful for knowing whether there are still jobs waiting to be loaded.
     */
    public synchronized boolean isJobQueueEmpty() {
        return jobQueue.isEmpty();
    }

    // --------------------------------------------------
    // Ready queue methods
    // --------------------------------------------------

    /**
     * Adds a process to the ready queue.
     * Used by JobLoaderThread after memory is allocated.
     *
     * Example:
     *     process.setState("READY");
     *     data.addToReadyQueue(process);
     */
    public synchronized void addToReadyQueue(PCB process) {
        readyQueue.add(process);
    }

    /**
     * Removes a specific process from the ready queue.
     * Useful for SJF and Priority because they choose a process from anywhere
     * in the ready queue, not necessarily the first process.
     *
     * Example:
     *     PCB shortest = findShortestJob(data.getReadyQueueSnapshot());
     *     data.removeFromReadyQueue(shortest);
     */
    public synchronized void removeFromReadyQueue(PCB process) {
        readyQueue.remove(process);
    }

    /**
     * Removes and returns the first ready process.
     * Useful for Round Robin, which behaves like a queue.
     *
     * Example:
     *     PCB process = data.pollReadyQueue();
     */
    public synchronized PCB pollReadyQueue() {
        if (readyQueue.isEmpty()) {
            return null;
        }
        return readyQueue.remove(0);
    }

    /**
     * Returns true if the ready queue is empty.
     */
    public synchronized boolean isReadyQueueEmpty() {
        return readyQueue.isEmpty();
    }

    /**
     * Returns the number of processes currently waiting in ready queue.
     * Used for starvation threshold:
     *     threshold = readyQueueSize * 5
     */
    public synchronized int getReadyQueueSize() {
        return readyQueue.size();
    }

    /**
     * Returns a copy of the ready queue.
     *
     * Why a copy?
     *     To avoid directly exposing the original list while threads may use it.
     *
     * Example:
     *     ArrayList<PCB> ready = data.getReadyQueueSnapshot();
     */
    public synchronized ArrayList<PCB> getReadyQueueSnapshot() {
        return new ArrayList<PCB>(readyQueue);
    }

    // --------------------------------------------------
    // All processes methods
    // --------------------------------------------------

    /**
     * Adds a process to the allProcesses list.
     * This list is used later to print the final process table.
     */
    public synchronized void addToAllProcesses(PCB process) {
        allProcesses.add(process);
    }

    /**
     * Returns a copy of all processes.
     * Used by Scheduler when printing tables and averages.
     */
    public synchronized ArrayList<PCB> getAllProcessesSnapshot() {
        return new ArrayList<PCB>(allProcesses);
    }

    // --------------------------------------------------
    // Gantt chart methods
    // --------------------------------------------------

    /**
     * Adds one execution segment to the Gantt chart.
     *
     * Example:
     *     data.addGanttEntry(new GanttEntry(1, 0, 5, 25, 20));
     */
    public synchronized void addGanttEntry(GanttEntry entry) {
        ganttChart.add(entry);
    }

    /**
     * Returns a copy of the Gantt chart entries.
     */
    public synchronized ArrayList<GanttEntry> getGanttChartSnapshot() {
        return new ArrayList<GanttEntry>(ganttChart);
    }

    // --------------------------------------------------
    // Memory methods
    // --------------------------------------------------

    /**
     * Checks if the process can fit in memory.
     *
     * Example:
     *     usedMemoryMB = 1200
     *     process.memoryRequired = 700
     *
     *     1200 + 700 <= 2048
     *     true, so it can be loaded.
     */
    public synchronized boolean hasEnoughMemoryFor(PCB process) {
        return usedMemoryMB + process.getMemoryRequired() <= TOTAL_MEMORY_MB;
    }

    /**
     * Loads the process into memory by increasing usedMemoryMB.
     *
     * Example:
     *     usedMemoryMB = 500
     *     process memory = 700
     *     after loading: usedMemoryMB = 1200
     */
    public synchronized void loadProcessToMemory(PCB process) {
        usedMemoryMB += process.getMemoryRequired();
    }

    /**
     * Removes a finished process from memory.
     *
     * Example:
     *     usedMemoryMB = 1200
     *     finished process memory = 500
     *     after removing: usedMemoryMB = 700
     */
    public synchronized void removeProcessFromMemory(PCB process) {
        usedMemoryMB -= process.getMemoryRequired();

        if (usedMemoryMB < 0) {
            usedMemoryMB = 0;
        }
    }

    /**
     * Returns currently used memory.
     */
    public synchronized int getUsedMemoryMB() {
        return usedMemoryMB;
    }

    /**
     * Returns available memory.
     * Formula:
     *     available = 2048 - usedMemoryMB
     */
    public synchronized int getAvailableMemoryMB() {
        return TOTAL_MEMORY_MB - usedMemoryMB;
    }

    // --------------------------------------------------
    // Control flags
    // --------------------------------------------------

    /**
     * JobReaderThread calls this when it finishes reading job.txt.
     */
    public synchronized void setReaderFinished(boolean readerFinished) {
        this.readerFinished = readerFinished;
    }

    /**
     * Used by JobLoaderThread to know whether file reading is done.
     */
    public synchronized boolean isReaderFinished() {
        return readerFinished;
    }

    /**
     * Scheduler or Main can call this when the simulation is done.
     */
    public synchronized void setSimulationFinished(boolean simulationFinished) {
        this.simulationFinished = simulationFinished;
    }

    /**
     * Used by JobLoaderThread to stop its loop.
     */
    public synchronized boolean isSimulationFinished() {
        return simulationFinished;
    }

    /**
     * Returns true if all processes have state TERMINATED.
     *
     * Example usage in Scheduler loop:
     *     while (!data.allProcessesCompleted()) {
     *         // keep scheduling
     *     }
     */
    public synchronized boolean allProcessesCompleted() {
        if (allProcesses.isEmpty()) {
            return false;
        }

        for (PCB process : allProcesses) {
            if (!process.getState().equals("TERMINATED")) {
                return false;
            }
        }

        return true;
    }
}
