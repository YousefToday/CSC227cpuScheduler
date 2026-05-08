import java.util.Scanner;

/**
 * Main is the starting point of the program.
 *
 * Its job is NOT to implement the algorithms.
 * Its job is to connect everything together:
 *     1. Create SharedData
 *     2. Start JobReaderThread
 *     3. Start JobLoaderThread
 *     4. Ask the user to choose an algorithm
 *     5. Call Scheduler
 *
 * Example run:
 *     Choose Scheduling Algorithm:
 *     1. Shortest Job First
 *     2. Round Robin
 *     3. Priority Scheduling
 */
public class Main {

    /**
     * Program entry point.
     */
    public static void main(String[] args) {
        SharedData data = new SharedData();

        /*
         * Thread 1:
         * Reads job.txt and fills jobQueue.
         */
        JobReaderThread readerThread = new JobReaderThread(data, "job.txt");

        /*
         * Thread 2:
         * Moves jobs from jobQueue to readyQueue if enough memory exists.
         */
        JobLoaderThread loaderThread = new JobLoaderThread(data);

        readerThread.start();
        loaderThread.start();

        /*
         * Wait for the reader to finish reading the file.
         * This makes sure all processes are known before the user runs scheduling.
         */
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Reader thread was interrupted.");
        }

        /*
         * Ask user which scheduling algorithm to run.
         */
        int choice = readUserChoice();

        Scheduler scheduler = new Scheduler(data);

        if (choice == 1) {
            scheduler.runSJF();
        } else if (choice == 2) {
            scheduler.runRoundRobin();
        } else if (choice == 3) {
            scheduler.runPriority();
        } else {
            System.out.println("Invalid choice.");
            data.setSimulationFinished(true);
        }


        try {
            loaderThread.join();
        } catch (InterruptedException e) {
            System.out.println("Loader thread was interrupted.");
        }
    }

    /**
     * Reads algorithm choice from the user.
     *
     * Example:
     *     user enters 2
     *     method returns 2
     */
    private static int readUserChoice() {
        Scanner input = new Scanner(System.in);

        System.out.println("\nChoose Scheduling Algorithm:");
        System.out.println("1. Shortest Job First");
        System.out.println("2. Round Robin");
        System.out.println("3. Priority Scheduling");
        System.out.print("Enter choice: ");

        int choice = input.nextInt();
        return choice;
    }
}
