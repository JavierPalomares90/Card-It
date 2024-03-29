package cardit.palomares.javier.com.mycardit.utils;

import java.io.File;

/**
 * Created by javierpalomares on 11/4/15.
 */
public interface BackupFileHandler {

    public void saveFile(File file);

    public boolean readFile(File file);

    public boolean restoreDefaultFileFromMemory();

    public String getFileName();

    public String getDefaultFilename();

    public int getMinConfigLength();
}
