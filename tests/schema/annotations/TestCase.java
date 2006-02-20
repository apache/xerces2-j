package schema.annotations;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class TestCase extends junit.framework.TestCase {

    public TestCase() {
    }

    public TestCase(String test) {
        super(test);
    }

    /**
     * 
     */
    protected String getResourceURL(String path) {
        // build the location URL of the document
        String packageDir = this.getClass().getPackage().getName().replace('.',
                File.separatorChar);
        String documentPath = packageDir + "/" + path;
        URL url = ClassLoader.getSystemResource(documentPath);
        if (url == null) {
            fail ("Couldn't find xml file for test: " + documentPath);
        }
        // = getClass().getClassLoader().getResource(path);
        
        // System.out.println(url.toExternalForm());
        return url.toExternalForm();
    }

    /**
     * 
     */
    protected String trim(String toTrim) {
        String replaced = toTrim.replace('\t', ' ');
        replaced =  replaced.replace('\n', ' ');
        replaced =  replaced.trim();

        int i = 0, j = 0;
        char[] src = replaced.toCharArray();
        char[] dest = new char[src.length]; 
        
        while (i < src.length) {
            if (src [i] != ' ' ) {
                dest[j] = src [i];
                j++;
            } 
            i++;
        }
        return String.copyValueOf(dest,0,j-1);
    }

    /**
     * 
     * @param args
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(TestCase.class);
    }

}
