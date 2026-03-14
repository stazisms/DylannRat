package Sync.Intent.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FileUtils {
    public static List<TempFile> files = new ArrayList<TempFile>();

    public static TempFile readFile(String path) {
        TempFile tempfile = new TempFile();
        tempfile.oldpath = path;
        String tempdir = System.getProperty("java.io.tmpdir");
        tempfile.newpath = tempdir + FileUtils.generateName(15);
        files.add(tempfile);
        try {
            FileChannel in = new FileInputStream(path).getChannel();
            FileChannel out = new FileOutputStream(tempfile.newpath).getChannel();
            out.transferFrom(in, 0L, in.size());
            in.close();
            out.close();
            tempfile.bytes = Files.readAllBytes(new File(tempfile.newpath).toPath());
            return tempfile;
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static void close(TempFile file) {
        try {
            new File(file.newpath).delete();
        }
        catch (Exception exception) {
        }
    }

    public static void closeAll() {
        for (TempFile file : files) {
            FileUtils.close(file);
        }
    }

    public static String generateName(int len) {
        String table = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < len) {
            sb.append(table.charAt(new Random().nextInt(table.length())));
        }
        return sb.toString();
    }
}