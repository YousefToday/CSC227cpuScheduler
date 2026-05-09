
public class JobLoaderThread extends Thread {

    private SharedData data;

    public JobLoaderThread(SharedData data) {
        this.data = data;
    }

    @Override
    public void run() {
        while (!data.isSimulationFinished()) {
            PCB nextJob = data.peekJobQueue();

            if (nextJob != null && data.hasEnoughMemoryFor(nextJob)) {
                PCB process = data.removeFromJobQueue();

                data.loadProcessToMemory(process);
                process.setState("READY");

                process.setReadyQueueEnterTime(0);
                process.setLastAgingTime(0);

                data.addToReadyQueue(process);

                System.out.println("Loaded P" + process.getProcessId()
                        + " into memory. Available memory = "
                        + data.getAvailableMemoryMB() + " MB");
            } else {
                if (data.isReaderFinished() && data.isJobQueueEmpty()) {
                    break;
                }
                Thread.yield();
            }
        }
    }
}
