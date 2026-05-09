import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
public class SharedData {


    public static final int TOTAL_MEMORY_MB = 2048;
    public static final int ROUND_ROBIN_QUANTUM = 5;
    public static final int AGING_INTERVAL = 4;

    private Queue<PCB> jobQueue;
    private ArrayList<PCB> readyQueue;
    private ArrayList<PCB> allProcesses;
    private ArrayList<GanttEntry> ganttChart;

    private int usedMemoryMB;

    private boolean readerFinished;
    private boolean simulationFinished;


    public SharedData() {
        jobQueue = new LinkedList<PCB>();
        readyQueue = new ArrayList<PCB>();
        allProcesses = new ArrayList<PCB>();
        ganttChart = new ArrayList<GanttEntry>();
        usedMemoryMB = 0;
        readerFinished = false;
        simulationFinished = false;
    }


    public synchronized void addToJobQueue(PCB process) {
        jobQueue.add(process);
    }
    public synchronized PCB peekJobQueue() {
        return jobQueue.peek();
    }
    public synchronized PCB removeFromJobQueue() {
        return jobQueue.poll();
    }
    public synchronized boolean isJobQueueEmpty() {
        return jobQueue.isEmpty();
    }


    public synchronized void addToReadyQueue(PCB process) {
        readyQueue.add(process);
    }
    public synchronized void removeFromReadyQueue(PCB process) {
        readyQueue.remove(process);
    }
    public synchronized PCB pollReadyQueue() {
        if (readyQueue.isEmpty()) {
            return null;
        }
        return readyQueue.remove(0);
    }
    public synchronized boolean isReadyQueueEmpty() {
        return readyQueue.isEmpty();
    }
    public synchronized int getReadyQueueSize() {
        return readyQueue.size();
    }
    public synchronized ArrayList<PCB> getReadyQueueSnapshot() {
        return new ArrayList<PCB>(readyQueue);
    }


    public synchronized void addToAllProcesses(PCB process) {
        allProcesses.add(process);
    }
    public synchronized ArrayList<PCB> getAllProcessesSnapshot() {
        return new ArrayList<PCB>(allProcesses);
    }


    public synchronized void addGanttEntry(GanttEntry entry) {
        ganttChart.add(entry);
    }
    public synchronized ArrayList<GanttEntry> getGanttChartSnapshot() {
        return new ArrayList<GanttEntry>(ganttChart);
    }


    public synchronized boolean hasEnoughMemoryFor(PCB process) {
        return usedMemoryMB + process.getMemoryRequired() <= TOTAL_MEMORY_MB;
    }
    public synchronized void loadProcessToMemory(PCB process) {
        usedMemoryMB += process.getMemoryRequired();
    }
    public synchronized void removeProcessFromMemory(PCB process) {
        usedMemoryMB -= process.getMemoryRequired();

        if (usedMemoryMB < 0) {
            usedMemoryMB = 0;
        }
    }
    public synchronized int getUsedMemoryMB() {
        return usedMemoryMB;
    }
    public synchronized int getAvailableMemoryMB() {
        return TOTAL_MEMORY_MB - usedMemoryMB;
    }

    public synchronized void setReaderFinished(boolean readerFinished) {
        this.readerFinished = readerFinished;
    }
    public synchronized boolean isReaderFinished() {
        return readerFinished;
    }


    public synchronized void setSimulationFinished(boolean simulationFinished) {
        this.simulationFinished = simulationFinished;
    }

    public synchronized boolean isSimulationFinished() {
        return simulationFinished;
    }

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
