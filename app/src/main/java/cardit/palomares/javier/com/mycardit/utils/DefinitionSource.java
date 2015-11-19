package cardit.palomares.javier.com.mycardit.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by javierpalomares on 11/10/15.
 */
public interface DefinitionSource {
    public InputStream getInputSource() throws IOException;

    public void closeSource() throws IOException;
}
