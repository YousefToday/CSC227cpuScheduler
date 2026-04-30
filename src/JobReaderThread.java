import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * JobReaderThread is Thread 1 from the project description.
 *
 * Its job is simple:
 *     1. Open job.txt
 *     2. Read each process line
 *     3. Create a PCB object
 *     4. Add the process to jobQueue
 *     5. Add the process to allProcesses
 *     6. Mark readerFinished = true
 *
 * Example input line:
 *     1:25:4;500
 *
 * Means:
 *     processId = 1
 *     burstTime = 25
 *     priority = 4
 *     memoryRequired = 500
 */
public class JobReaderThread extends Thread {

    private SharedData data;
    private String fileName;

    /**
     * Constructor.
     *
     * Example usage:
     *     JobReaderThread reader = new JobReaderThread(data, "job.txt");
     *     reader.start();
     */
    public JobReaderThread(SharedData data, String fileName) {
        this.data = data;
        this.fileName = fileName;
    }

    /**
     * This method runs automatically when we call:
     *     reader.start();
     *
     * Do NOT call run() directly in Main.
     */
    @Override
    public void run() {
        int arrivalOrder = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Ignore empty lines in job.txt.
                if (line.isEmpty()) {
                    continue;
                }

                PCB process = parseLineToPCB(line, arrivalOrder);

                data.addToJobQueue(process);
                data.addToAllProcesses(process);

                arrivalOrder++;
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + fileName);
            System.out.println("Details: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: job.txt contains a number that could not be parsed.");
            System.out.println("Details: " + e.getMessage());
        } finally {
            data.setReaderFinished(true);
        }
    }

    /**
     * Converts one line from job.txt into a PCB object.
     *
     * Input format:
     *     ProcessID:BurstTime:Priority;MemoryRequired
     *
     * Example:
     *     line = "1:25:4;500"
     *
     * Step 1:
     *     split by semicolon:
     *         parts[0] = "1:25:4"
     *         parts[1] = "500"
     *
     * Step 2:
     *     split parts[0] by colon:
     *         processInfo[0] = "1"
     *         processInfo[1] = "25"
     *         processInfo[2] = "4"
     *
     * Step 3:
     *     convert strings to integers and create PCB.
     */
    private PCB parseLineToPCB(String line, int arrivalOrder) {
        String[] parts = line.split(";");
        String[] processInfo = parts[0].split(":");

        int processId = Integer.parseInt(processInfo[0].trim());
        int burstTime = Integer.parseInt(processInfo[1].trim());
        int priority = Integer.parseInt(processInfo[2].trim());
        int memoryRequired = Integer.parseInt(parts[1].trim());

        return new PCB(processId, burstTime, priority, memoryRequired, arrivalOrder);
    }
}
