package fstp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.text.SimpleDateFormat;
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
     * Convert a file to a string with the following format:
     * 
     * <p>
     * <code>path*checksum*last_modified</code>
     * </p>
     * 
     * @param file File to convert
     * @return String representation of file
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read
     */
    public static String fileToString(String path, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sb.append(file.getPath().replace(path, "")).append('*');
        sb.append(fileToChecksum(file)).append('*');
        sb.append(sdf.format(getFileData(file)));
        return sb.toString();
    }

    /**
     * Convert a list of files to a string with the following format:
     * 
     * <p>
     * <code>path*checksum*last_modified,path*checksum*last_modified,...</code>
     * </p>
     * 
     * @param files
     * @return
     * @throws IOException
     */
    public static String filesToString(String path, List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();	
        for (File file : files)
            sb.append(fileToString(path, file)).append(',');
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
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
    public static long fileToChecksum(File file) throws IOException {
        Checksum checksum = new Adler32();
        byte[] arr = fileToBytes(file);
        checksum.update(arr, 0, (int) arr.length);
        return checksum.getValue();
    }
}
