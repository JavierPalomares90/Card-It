package cardit.palomares.javier.com.mycardit.utils;

import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by javierpalomares on 11/18/15.
 */
public class FileUtils {

    private static final int BUFFER_SIZE = (16*1024);

    private static final int EXEC_MODE = 0;
    private static final int READ_MODE = 1;
    private static final int WRITE_MODE = 2;
    private static final int READ_WRITE_EXEC_MODE = 3;

    private static boolean jniLoaded = false;

    public static String getBaseName(String fileName){
        int index = fileName.lastIndexOf(".");
        if (index != -1){
            fileName = fileName.substring(0,index);
        }
        return fileName;
    }

    public static String getExtension(String fileName){
        int index = fileName.lastIndexOf(".");
        if (index != -1 && index < fileName.length()){
            fileName = fileName.substring(index+1);
        }
        return fileName;
    }

    public static String readContents(File file) throws IOException{
        byte[] raw = readRawContents(file);
        if (raw != null){
            return new String(raw,0,raw.length);
        }
        return "";
    }

    public static byte[] readRawContents(File file) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (file.isFile() == true && file.canRead() == true){
            FileUtils.copyandClose(new FileInputStream(file),output);

        }
        return output.toByteArray();
    }

    public static void writeContents(File file, String contents) throws IOException{
        StringReader reader = new StringReader(contents);
        FileWriter writer = null;
        makeFile(file);

        try{
            writer = new FileWriter(file);
            char[] buf = new char[BUFFER_SIZE];
            int num = 0;
            while ((num = reader.read(buf)) != -1){
                writer.write(buf,0,num);
            }
            reader.close();
            reader = null;
            writer.flush();
            writer.close();
            writer = null;
        }finally {
            if (reader != null){
                reader.close();
            }
            if (writer != null){
                writer.close();
            }
        }
    }

    public static void writeContents(File file, byte[] data) throws IOException{
        if (file.isDirectory()){
            throw new IOException("Unable to write to a directory");
        }
        makeFile(file);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
        }finally {
            if (out!=null){
                out.close();
            }
        }
    }

    public static void writeContents(File file, InputStream stream) throws IOException{
        makeFile(file);
        FileUtils.copyAndClose(stream, new FileOutputStream(file));
    }

    public static void writeContents(File file, OutputStream out) throws IOException{
        FileUtils.copyAndClose(new FileInputStream(file),out);
    }

    public static boolean makeFile(File file) throws IOException{
        if (file.isDirectory()){
            throw new IOException("Unable to write to a directory");
        }
        if (file.exists() == true){
            return true;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()){
            parent.mkdirs();
        }
        return file.createNewFile();
    }

    public static void copyAndClose(InputStream src, OutputStream dst) throws IOException{
        byte[] buf = new byte[BUFFER_SIZE];
        int num = 0;

        try{
            while((num = src.read(buf)) != -1){
                dst.write(buf,0,num);
            }
            src.close();
            src = null;
            dst.flush();
            dst.close();
            dst = null;
        }finally {
            if (src != null){
                src.close();
            }
            if (dst != null){
                dst.close();
            }
        }
    }

    public static boolean copyAndClose(InputStream src, OutputStream dst, HttpGet getreq, int sizeLimit) throws IOException{
        byte[] buf = new byte[BUFFER_SIZE];
        int num = 0;
        int totalDownloaded = 0;
        boolean status = true;

        try{
            while ((num = src.read(buf)) != -1){
                dst.write(buf,0,num);
                totalDownloaded += num;

                if (totalDownloaded > sizeLimit){
                    status = false;
                    getreq.abort();
                    break;
                }
            }
            src.close();
            src = null;
            dst.flush();
            dst.close();
            dst = null;
        }
        finally {
            if (src != null){
                src.close();
            }
            if (dst != null){
                dst.close();
            }
        }
        return status;
    }

    public static void copyFile(File src, File dst) throws IOException{
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dst);
        copyandClose(fis,fos);
    }

    public static byte[] createChecksum(InputStream stream) throws IOException{
        try{
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[BUFFER_SIZE];
            int num;
            while((num = stream.read(buffer)) > 0){
                digest.update(buffer,0,num);
            }
            stream.close();
            stream = null;
            return digest.digest();
        }catch (NoSuchAlgorithmException e){
            throw new IOException(e.getMessage(),e);
        }
        finally {
            if (stream != null){
                stream.close();
            }
        }
    }

    public static String convertMD5Checksum(byte[] md5){
        if (md5 == null){
            return null;
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i< md5.length;i++ ){
            String digit = Integer.toString((md5[i] & 0xff) + 0x100,16).substring(1);
            result.append(digit);
        }
        return result.toString();
    }

    public static void closeAndIgnoreFail(Closeable file){
        try{
            if(file !=null){
                file.close();
            }
        }catch (IOException e ){

        }
    }


    public static void closeAndIgnoreFail(DefinitionSource src){
        try{
            if(src !=null){
                src.closeSource();
            }
        }catch (IOException e ){

        }
    }

    public static void closeAndIgnoreFail(XmlReader reader){
        if (reader !=null){
            reader.close();
        }
    }

    public static File createTempDirectory(String prefix, String suffix) throws IOException{
        File tmpDir = null;
        tmpDir = File.createTempFile(prefix,suffix);
        tmpDir.delete();
        tmpDir.mkdir();
        return tmpDir;
    }

    private FileUtils(){

    }
}
