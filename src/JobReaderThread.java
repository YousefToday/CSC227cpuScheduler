import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JobReaderThread extends Thread {

    private SharedData data;
    private String fileName;

    public JobReaderThread(SharedData data, String fileName) {
        this.data = data;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        int arrivalOrder = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

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
