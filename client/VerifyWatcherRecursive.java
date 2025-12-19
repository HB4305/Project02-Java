package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VerifyWatcherRecursive extends ClientApp {

    public VerifyWatcherRecursive() {
        super(true);
    }

    // @Override
    public void start() {
        // no-op
    }

    @Override
    public synchronized void sendFileChange(String change) {
        System.out.println("[TEST SUCCESS] Change detected: " + change);
    }

    @Override
    public synchronized void sendError(String error) {
        System.out.println("[TEST ERROR] " + error);
    }

    // Override log to avoid null pointer if GUI not init
    @Override
    public void log(String message) {
        System.out.println(message);
    }

    public void test() throws InterruptedException, IOException {
        String testDir = "test_monitor_recursive";
        // Clean up
        deleteDirectory(new File(testDir));
        new File(testDir).mkdirs();

        FileWatcher watcher = new FileWatcher(testDir, this);
        new Thread(watcher).start();

        Thread.sleep(1000);

        System.out.println("Creating subdirectory 'sub1'...");
        File sub1 = new File(testDir + "/sub1");
        sub1.mkdir();

        Thread.sleep(1000);

        System.out.println("Creating file in 'sub1'...");
        File f1 = new File(testDir + "/sub1/test1.txt");
        f1.createNewFile();

        Thread.sleep(1000);

        System.out.println("Modifying file in 'sub1'...");
        Files.write(Paths.get(testDir + "/sub1/test1.txt"), "Recursive Hello".getBytes());

        Thread.sleep(1000);

        watcher.stop();
        System.out.println("Test finished.");

        // Clean up
        deleteDirectory(new File(testDir));
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    public static void main(String[] args) {
        try {
            new VerifyWatcherRecursive().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
