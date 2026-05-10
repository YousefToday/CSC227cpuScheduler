import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        SharedData data = new SharedData();

        // Starts the threads responsible for reading and loading jobs.
        JobReaderThread readerThread = new JobReaderThread(data, "job.txt");
        JobLoaderThread loaderThread = new JobLoaderThread(data);

        readerThread.start();
        loaderThread.start();

        try {
            readerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Reader thread was interrupted.");
        }

        int choice = readUserChoice();

        Scheduler scheduler = new Scheduler(data);

        // Runs the scheduling algorithm selected by the user.
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