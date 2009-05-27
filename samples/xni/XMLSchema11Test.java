package xni;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLSchema11Test implements ErrorHandler {

    public static void main (String [] args) {

		XMLSchema11Test test = new XMLSchema11Test();
		test.run(args[0], args[1]);
    }

	private void run(String xmlfile, String schemapath) {
      try {
            System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/v1.1",
                 "org.apache.xerces.jaxp.validation.XMLSchema11Factory");
            SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema/v1.1");          
            Schema s = sf.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator(); 
			v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
        }
        catch (Exception e) {
           e.printStackTrace();
        }
	}

	public void error(SAXParseException exception)
           throws SAXException {
       System.out.println(exception.getSystemId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ":" + exception.getMessage());
    }

	public void fatalError(SAXParseException exception)
                throws SAXException {
      System.out.println(exception.getSystemId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ":" + exception.getMessage());
    }

	public void warning(SAXParseException exception)
             throws SAXException {
      System.out.println(exception.getSystemId() + ":" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ":" + exception.getMessage());
	}
}