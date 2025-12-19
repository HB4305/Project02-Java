package client;

import common.Message;
import common.MessageType;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {
    private String rootPath;
    private ClientApp clientApp;
    private WatchService watchService;
    private final Map<WatchKey, Path> keys;
    private volatile boolean isRunning;

    public FileWatcher(String path, ClientApp clientApp) {
        this.rootPath = path;
        this.clientApp = clientApp;
        this.keys = new HashMap<>();
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path root = Paths.get(rootPath);
            if (!Files.exists(root)) {
                clientApp.sendError("Directory does not exist: " + rootPath);
                return;
            }

            registerAll(root);
            clientApp.log("Started recursive monitoring: " + rootPath);

            while (isRunning) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException x) {
                    return;
                }

                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path name = ev.context();
                    Path child = dir.resolve(name);

                    String msg = kind.name() + ": " + child;
                    clientApp.sendFileChange(msg);

                    if (kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readable
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            clientApp.sendError("Watcher error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    public void stop() {
        isRunning = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
