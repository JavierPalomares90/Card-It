package cardit.palomares.javier.com.mycardit.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by javierpalomares on 11/18/15.
 */
public class FileBackupUtil {
    public static final String BACKUP_SUFFIX = ".bak";
    public static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String TAG = "FileBackupUtil";
    public static final boolean safeLoadFile(BackupFileHandler backupFileHandler){
        synchronized (backupFileHandler){
            boolean success = true;
            boolean restoreFile = false;

            String filename = backupFileHandler.getFileName();
            if(!loadFile(backupFileHandler,filename)){
                restoreFile = true;
                if(!loadFile(backupFileHandler,filename + BACKUP_SUFFIX)){
                    if(!loadFile(backupFileHandler,backupFileHandler.getDefaultFilename())){
                        if (!backupFileHandler.restoreDefaultFileFromMemory()){
                            success = false;
                        }
                    }
                }
            }
            if (success){
                if(restoreFile){
                    safeWriteFile(backupFileHandler);

                }
                createBackupFileIfNeeded(filename);
            }
            return success;
        }
    }

    private static void createBackupFileIfNeeded(String filename){
        File configFile = new File(filename);
        File bakFile = new File(filename + BACKUP_SUFFIX);
        boolean newBackupNeeded = true;

        try{
            String configMD5 = FileUtils.convertMD5Checksum(FileUtils.createChecksum(configFile));
            String backupMD5 = FileUtils.convertMD5Checksum(FileUtils.createChecksum(bakFile));
            if (configMD5 != null && backupMD5 !=null){
                newBackupNeeded = !configMD5.equals(backupMD5);
            }
        }catch (IOException e1){
            newBackupNeeded = true;
        }

        if (newBackupNeeded){
            try{
                FileUtils.copyFile(configFile, bakFile);
            }catch(IOException e){
                Log.d(TAG,"error in copying file when creating");
            }
        }
    }

    public static void safeWriteFile(BackupFileHandler backupFileHandler){
        synchronized (backupFileHandler){
            String filename = backupFileHandler.getFileName();

            if (!filename.isEmpty()){
                File file = new File(filename);
                File bakFile = new File(filename + BACKUP_SUFFIX);

                File tmpFile;
                try{
                    tmpFile = File.createTempFile(file.getName(),TEMP_FILE_SUFFIX,file.getParentFile());

                }catch(IOException e){
                    tmpFile = new File(file.getAbsolutePath() + TEMP_FILE_SUFFIX);
                }

                backupFileHandler.saveFile(tmpFile.getAbsoluteFile());

                if (file.exists() && file.length() > backupFileHandler.getMinConfigLength()){
                    if(verifyValidFormat(file)){
                        boolean bakCreateResult = file.renameTo(bakFile);
                        if (!bakCreateResult){
                            Log.d(TAG,"Unable to create backup file");
                        }
                    }else{
                        Log.d(TAG,"Error in wrirting file");
                    }
                }else{
                    try{
                        FileUtils.copyFile(tmpFile, bakFile);
                    }catch (IOException e){
                        Log.d(TAG,"Unable to write backup file");
                    }
                }

                File configFile = new File(filename);
                boolean createResult = tmpFile.renameTo(configFile);

                if(!createResult){
                    Log.d(TAG,"Unable to save config file");
                }
            }
        }
    }

    private  static boolean verifyValidFormat(File file){
        boolean success = false;

        FileDefinitionSource src = null;
        XmlReader reader = null;
        try{
            src = new FileDefinitionSource(file);
            reader = new XmlReader(src);
            success = true;
        }catch(Exception e){
            Log.d(TAG,"Error in verifying format");
        }finally{
            FileUtils.closeAndIgnoreFail(src);
            FileUtils.closeAndIgnoreFail(reader);
        }
        return success;
    }

    private static boolean loadFile(BackupFileHandler backupFileHandler,String filename){
        boolean success = false;
        if(!filename.isEmpty()){
            File file = new File(filename);
            try{
                boolean fileExists = file.exists();
                long len = file.length();
                if (fileExists &&  len > 0 ){
                    boolean readFile = backupFileHandler.readFile(file);
                    success = readFile;
                }
            }catch(NullPointerException e){
                Log.d(TAG,"Unable to load file");
            }

        }
        return success;
    }
}
