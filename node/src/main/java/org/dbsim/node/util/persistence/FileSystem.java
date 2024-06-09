package org.dbsim.node.util.persistence;

import org.dbsim.node.model.db.Index;
import org.dbsim.node.util.btree.BTree;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FileSystem {
    boolean createDirectory(String directory);
    boolean renameDirectory(String oldDirectory, String newDirectory);
    boolean renameFile(String directory, String oldFileName, String newFileName);
    Optional<String> getFileContent(String directory, String fileName);
    boolean createFile(String directory, String fileName, String content);
    boolean deleteFile(String directory, String fileName);
    boolean deleteDirectory(String directory);
    List<File> getFiles(String directory);
    boolean doesDirectoryExist(String directory);
    boolean doesFileExist(String directory, String fileName);

    boolean createBinaryFile(String indexFilePath, String fileName, Serializable content);
    Optional<Serializable> getBinaryFileContent(String indexFilePath, String fileName);

}