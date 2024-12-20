package fstp.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class FileUtils {
    /**
     * Get all files from a directory recursively
     * 
     * @param dir Directory to get files from
     * @return List of files in directory
     */
    public static List<File> getFiles(File dir) {
        List<File> files = new ArrayList<File>();
        for (File file : dir.listFiles())
            if (file.isFile()) files.add(file);
            else files.addAll(getFiles(file));
        return files;
    }

    public static File getFile(File dir, String path) {
        return new File(dir.getPath() + "/" + path);
    }

    /**
     * Convert a file to a byte array
     * 
     * @param file File to convert
     * @return Byte array of file
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static byte[] fileToBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Get the last modified date of a file
     * 
     * @param file File to get date from
     * @return Last modified date of file
    */
    public static Date getFileData(File file) {
        return new Date(file.lastModified());
    }

    /**
     * Get the checksum of a file using Adler32
     * 
     * @param file File to get checksum from
     * @return Checksum of file
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static long checksumFile(File file) throws IOException {
        byte[] arr = fileToBytes(file);
        return checksumByteArr(arr);
    }

    /**
     * Get the checksum of a byte array using Adler32
     * 
     * @param arr Byte array to get checksum from
     * @return Checksum of byte array
     */
    public static long checksumByteArr(byte[] arr) {
        Checksum checksum = new Adler32();
        checksum.update(arr, 0, (int) arr.length);
        return checksum.getValue();
    }

    public static int getLastChunkSize(File file, int chunkSize) throws IOException {
        byte[] arr = fileToBytes(file);
        return getLastChunkSize(arr, chunkSize);
    }

    private static int getLastChunkSize(byte[] arr, int chunkSize) {
        return arr.length % chunkSize;
    }

    /**
     * Get the chunks ids of a file
     * 
     * @param file File to get chunks from
     * @param chunkSize Size of each chunk
     * @return List of chunks
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static List<Long> getChunks(File file, int chunkSize) throws IOException {
        List<Long> chunks = new ArrayList<>();
        byte[] arr = fileToBytes(file);
        int numChunks = (int) Math.ceil(arr.length / (double) chunkSize);
        for (int i = 0; i < numChunks; i++) {
            byte[] chunk = getChunk(arr, chunkSize, i);
            chunks.add(checksumByteArr(chunk));
        }
        return chunks;
    }

    /**
     * Get a chunk of a file
     * 
     * @param file File to get chunk from
     * @param chunkSize Size of each chunk
     * @param idx Index of chunk
     * @return Chunk of file
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static byte[] getChunk(File file, int chunkSize, int idx) throws IOException {
        return getChunk(fileToBytes(file), chunkSize, idx);
    }

    /**
     * Get a chunk of a byte array
     * 
     * @param arr Byte array to get chunk from
     * @param chunkSize Size of each chunk
     * @param idx Index of chunk
     * @return Chunk of byte array
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static byte[] getChunk(byte[] arr, int chunkSize, int idx) throws IOException {
        byte[] chunk = new byte[chunkSize];
        System.arraycopy(arr, idx * chunkSize, chunk, 0, Math.min(arr.length - idx * chunkSize, chunkSize));
        return chunk;
    }

    public static void emptyFile(File file) throws IOException {
        new File(file.getParent()).mkdirs();
        if (!file.exists())
            file.createNewFile();

        FileWriter fw = new FileWriter(file, false);
        fw.close();
    }
}
