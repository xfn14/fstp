package fstp.utils;

import java.io.File;
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
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(arr, i * chunkSize, chunk, 0, Math.min(arr.length - i * chunkSize, chunkSize));
            chunks.add(checksumByteArr(chunk));
        }
        return chunks;
    }
}
