package cardit.palomares.javier.com.mycardit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by javierpalomares on 11/10/15.
 */
public class FileDefinitionSource implements DefinitionSource {

    private File sourceFile;
    private FileInputStream stream;

    public FileDefinitionSource(File sourceFile){
        super();
        this.sourceFile = sourceFile;
    }

    public InputStream getInputSource() throws IOException{
        if (this.stream != null){
            closeSource();
        }
        this.stream = new FileInputStream(this.sourceFile);
        return this.stream;
    }

    public void closeSource() throws IOException{
        if (this.stream != null){
            this.stream.close();
            this.stream = null;
        }
    }
}
