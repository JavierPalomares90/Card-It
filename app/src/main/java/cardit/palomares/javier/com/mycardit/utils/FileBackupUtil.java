package cardit.palomares.javier.com.mycardit.utils;

import java.io.File;

/**
 * Created by javierpalomares on 11/18/15.
 */
public class FileBackupUtil {
    public static final String BACKUP_SUFFIX = ".bak";
    public static final String TEMP_FILE_SUFFIX = ".tmp";
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
            String 
        }
    }
}
