package org.dbsim.node.util.persistence;

import org.dbsim.node.util.GlobalVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class FileSystemUtil implements FileSystem {
    private final String ROOT_DIRECTORY = GlobalVar.DB_ROOT_DIR;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemUtil.class);

    // Creates a directory under the root directory
    public boolean createDirectory(String directory) {
        Path path = Paths.get(ROOT_DIRECTORY, directory);
        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error creating directory: " + e.getMessage());
            return false;
        }
    }

    public boolean renameDirectory(String oldDirectory, String newDirectory) {
        Path oldPath = Paths.get(ROOT_DIRECTORY, oldDirectory);
        Path newPath = Paths.get(ROOT_DIRECTORY, newDirectory);
        try {
            Files.move(oldPath, newPath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error renaming directory: " + e.getMessage());
            return false;
        }
    }

    // Reads a file from the specified directory
    public Optional<String> getFileContent(String directory, String fileName) {
        Path path = Paths.get(ROOT_DIRECTORY, directory, fileName);
        try {
            return Optional.of(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            LOGGER.error("Error reading file: " + e.getMessage());
            return Optional.empty();
        }
    }


    // Writes a file to the specified directory
    public boolean createFile(String directory, String fileName, String content) {
        Path path = Paths.get(ROOT_DIRECTORY, directory, fileName);
        try {
            Files.write(path, content.getBytes());
            return true;
        } catch (IOException e) {
            LOGGER.error("Error writing file: " + e.getMessage());
            return false;
        }
    }

    // Deletes a file from the specified directory
    public boolean deleteFile(String directory, String fileName) {
        Path path = Paths.get(ROOT_DIRECTORY, directory, fileName);
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    public boolean renameFile(String directory, String oldFileName, String newFileName) {
        Path oldPath = Paths.get(ROOT_DIRECTORY, directory, oldFileName);
        Path newPath = Paths.get(ROOT_DIRECTORY, directory, newFileName);
        try {
            Files.move(oldPath, newPath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error renaming file: " + e.getMessage());
            return false;
        }
    }

    // Deletes a directory
    public boolean deleteDirectory(String directory) {
        Path path = Paths.get(ROOT_DIRECTORY, directory);
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error deleting directory: " + e.getMessage());
            return false;
        }
    }

    // Lists all files in the specified directory
    public List<File> getFiles(String directory) {
        Path path = Paths.get(ROOT_DIRECTORY, directory);
        File[] files = path.toFile().listFiles();
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    // Checks if a directory exists
    public boolean doesDirectoryExist(String directory) {
        Path path = Paths.get(ROOT_DIRECTORY, directory);
        return Files.exists(path);
    }

    // Checks if a file exists
    public boolean doesFileExist(String directory, String fileName) {
        Path path = Paths.get(ROOT_DIRECTORY, directory, fileName);
        return Files.exists(path);
    }

    public boolean createBinaryFile(String indexFilePath, String fileName, Serializable content) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(ROOT_DIRECTORY, indexFilePath + "/" + fileName));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(content);
            objectOutputStream.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("Error writing binary file: " + e.getMessage());
            return false;
        }
    }

    public Optional<Serializable> getBinaryFileContent(String indexFilePath, String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(ROOT_DIRECTORY, indexFilePath + "/" + fileName));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
            Serializable content = (Serializable) objectInputStream.readObject();
            objectInputStream.close();
            return Optional.of(content);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error reading binary file: " + e);
            return Optional.empty();
        }
    }
}