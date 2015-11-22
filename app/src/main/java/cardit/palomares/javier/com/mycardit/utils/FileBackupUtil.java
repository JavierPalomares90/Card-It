package cardit.palomares.javier.com.mycardit.utils;

import java.io.File;
import java.io.IOException;

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
                //TODO: Log error to logcat
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
                            //TODO: log error unable to create bak file
                        }
                    }else{
                        // TODO: log unable to open file
                    }
                }else{
                    try{

                        FileUtils.copyFile(tmpFile, bakFile);

                    }catch (IOException e){
                        //TODO: log unable to save bakFile
                    }
                }

                File configFile = new File(filename);
                boolean createResult = tmpFile.renameTo(configFile);

                if(!createResult){
                    // TODO: Log unable to save config file
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
            //TODO: LOG errors
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
                if (file.exists() && file.length() > 0 && backupFileHandler.readFile(file)){
                    success = true;
                }
            }catch(NullPointerException e){
                //TODO: log exception
            }

        }
        return success;
    }
}
