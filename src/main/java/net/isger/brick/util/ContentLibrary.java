package net.isger.brick.util;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.isger.brick.util.anno.Digest;
import net.isger.util.Asserts;
import net.isger.util.Callable;
import net.isger.util.Files;
import net.isger.util.Strings;

/**
 * 内容库
 * 
 * @author issing
 */
public class ContentLibrary {

    private transient ExecutorService service;

    private transient File root;

    private transient File carryDir;

    private transient File sourceDir;

    private int threads;

    private String workspace;

    @Digest
    private void initial() {
        service = Executors.newFixedThreadPool(threads = Math.max(Math.min(10, threads), 10));
        if (Strings.isEmpty(workspace)) {
            workspace = "./content-libary";
        }
        root = new File(workspace);
        if (root.exists() && !root.isDirectory()) {
            throw Asserts.argument("Invalid workspace path [%s]", workspace);
        }
        carryDir = new File(root, "carry");
        sourceDir = new File(root, "source");
        carryDir.mkdirs();
        sourceDir.mkdirs();
    }

    public Future<?> carry(String namespace, String name, File file) {
        return carry(namespace, name, file, null);
    }

    public Future<?> carry(final String namespace, final String name, File file, final Callable<String> callable) {
        if (file == null || !(file.exists() && file.isFile())) {
            return null;
        }
        final File carryFile = new File(carryDir, file.getName());
        Files.rename(file, carryFile);
        return service.submit(new Runnable() {
            public void run() {
                File sourcePath = new File(sourceDir, namespace + File.separatorChar + name);
                mkdir: {
                    if (sourcePath.exists()) {
                        if (sourcePath.isDirectory()) {
                            break mkdir;
                        }
                        Files.delete(sourcePath);
                    }
                    Files.mkdirs(sourcePath);
                }
                String file;
                if (callable == null) {
                    file = carryFile.getName();
                } else {
                    file = callable.call(namespace, name, carryFile, sourcePath);
                    if (Strings.isEmpty(file)) {
                        return;
                    }
                    sourcePath = new File(sourcePath, file);
                }
                remove(namespace, name, file);
                Files.rename(carryFile, sourcePath);
            }
        });
    }

    public void remove(String namespace, String name) {
        Files.delete(new File(sourceDir, namespace + File.separatorChar + name));
    }

    public void remove(String namespace, String name, String file) {
        Files.delete(new File(sourceDir, namespace + File.separatorChar + name + File.separatorChar + file));
    }

    public File source(String namespace, String name, String file) {
        return get(sourceDir, namespace + File.separatorChar + name + File.separatorChar + file);
    }

    private File get(File dir, String path) {
        File file = new File(dir, path);
        if (!file.exists()) {
            file = null;
        }
        return file;
    }

}
