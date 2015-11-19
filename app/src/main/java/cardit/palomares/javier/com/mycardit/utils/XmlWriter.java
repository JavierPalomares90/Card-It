package cardit.palomares.javier.com.mycardit.utils;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by javierpalomares on 11/4/15.
 */
public class XmlWriter {

    private Document doc;
    private Element root;
    private Element current;
    private DocumentBuilder builder;
    private String namespace;
    private String nodePrefix;


    private static Transformer prettyTransformer = null;
    private static Transformer regularTransformer = null;

    static {
        Thread load = new Thread(new LoaderThread());
        load.start();
    }

    public XmlWriter() throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
        nodePrefix="";
    }

    public XmlWriter(String namespaceURI, String nodeNamespacePrefix) throws ParserConfigurationException
    {
        this();
        namespace = namespaceURI;
        int x = nodeNamespacePrefix.indexOf(':');
        if (x>0){
            nodePrefix = nodeNamespacePrefix.substring(0,x+1);

        }else{
            nodePrefix = nodeNamespacePrefix;
        }
        DOMImplementation domImplementation = builder.getDOMImplementation();
        doc = domImplementation.createDocument(namespaceURI,nodeNamespacePrefix,null);
    }

    public Element setRootNode(String nodeName){
        if (root == null){
            if(namespace != null && namespace.length() > 0){
                root = doc.getDocumentElement();
            }
            else{
                root = doc.createElement(nodeName);
                doc.appendChild(root);

            }
        }
        return root;
    }

    public Element setRootNode(Element node){
        if (root == null){
            root = node;
            doc.appendChild(root);
        }
        return root;
    }

    public Element addNode(Element parent, String nodeName){
        Element node = doc.createElement(nodePrefix+nodeName);
        parent.appendChild(node);
        return node;
    }

    public Element addNode(Element parent, String nodeName, String nodeValue){
        if (nodeValue == null){
            return null;
        }

        Element node = doc.createElement(nodePrefix+nodeName);
        parent.appendChild(node);

        node.setTextContent(nodeValue.trim());

        return node;
    }

    public void addAttribute(Element parent, String attrName, String attrValue){
        if (attrValue == null){
            return;
        }
        parent.setAttribute(attrName, attrValue);
    }

    public Element getCurrentNode(){
        return current;
    }

    public void setCurrentNode(Element node){
        this.current = node;
    }

    public void saveDoc(File xmlFile) throws TransformerException, IOException{
        FileOutputStream out = null;
        try{
            boolean alreadyExists = xmlFile.exists();
            out = new FileOutputStream(xmlFile);
            try{
                saveDoc(out);
            }
            catch (RuntimeException e){
                if (alreadyExists == true){
                    throw(e);
                }else{
                    //TODO: Log error to logcat
                }
            }

        }finally{
            if (out != null){
                out.close();
            }
        }
    }

    public void saveDoc(OutputStream stream) throws TransformerException, IOException{
        synchronized (XmlWriter.class){
            Transformer xformer = getTransformer(true);
            if (xformer == null){
                xformer = getTransformer(false);

            }

            Source source = new DOMSource(doc);
            Result result = new StreamResult(stream);

            try{
                xformer.transform(source,result);
                stream.flush();
            }catch (RuntimeException e){
                throw new IOException(e.getMessage(),e);
            }
        }
    }

    public void saveDoc(Writer writer, boolean pretty) throws TransformerException, IOException{
        synchronized (XmlWriter.class){
            Transformer xformer = getTransformer(pretty);

            Source source = new DOMSource(doc);

            Result result = new StreamResult(writer);
            try{
                xformer.transform(source,result);
                writer.flush();
            }catch (RuntimeException e){
                throw  new IOException(e.getMessage(),e);
            }
        }
    }

    static Transformer getTransformer(boolean pretty) throws  IOException {
        if (pretty && prettyTransformer != null) {
            return prettyTransformer;
        } else if (!pretty && regularTransformer != null){
            return regularTransformer;
        }
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            if (pretty) {
                StreamSource style = new StreamSource(new StringReader(stylesheet));
                prettyTransformer = factory.newTransformer(style);
                if (prettyTransformer != null) {
                    prettyTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    prettyTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
                }
                return prettyTransformer;
            } else {
                regularTransformer = factory.newTransformer();
                return regularTransformer;
            }
        }catch (TransformerConfigurationException e){
            //TODO: Log exception to logcat
            return  null;
        }catch (RuntimeException e){
            throw new IOException(e);
        }
    }

    private static final String stylesheet = "<!DOCTYPE stylesheet [\r\n" +
                                             " <!ENTITY cr \" <xsl:text>\r\n" +
                                             "</xsl:text>\">\r\n" +
                                             "<xsl:stylesheet\r\n" +
                                             "   xmlns:xsl=\"http://www.w3.org/1999/XSL/Tranform\" \r\n" +
                                             "   xmlns:xalan=\"http://xml.apache.org/xslt\" \r\n" +
                                             "   version=\"1.0\">\r\n" +
                                             "   \r\n" +
                                             "   <xsl:output method=\"xml\" indent=\"yes\" xalan:indent-amount=\"4\"/> \r\n" +
                                             "    \r\n" +
                                             "   <!-- copy out the xml -->\r\n" +
                                             "   <xsl:template match=\"* | @*\">\r\n" +
                                             "       <xsl:copy><xsl:copy-of-select=\"@*\"/><xsl:apply-templates/></xsl:copy>\r\n" +
                                             "   </xsl:template>\r\n" +
                                             "\r\n" +
                                             "</xsl:stylesheet>";

    private static class LoaderThread implements  Runnable{
        @Override
        public void run(){
            synchronized (XmlWriter.class){
                int count = 0;
                boolean success = false;
                while(!success){
                    try{
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document localDoc = builder.newDocument();
                        Element root = localDoc.createElement("dummyRoot");
                        Element child = localDoc.createElement("dummyChild");
                        root.appendChild(child);
                        localDoc.appendChild(root);
                        StringWriter sw = new StringWriter();

                        Transformer tf = getTransformer(true);
                        Source source = new DOMSource(localDoc);
                        Result result = new StreamResult(sw);

                        try{
                            tf.transform(source,result);
                            sw.flush();
                        }catch (RuntimeException e){
                            throw new IOException(e.getMessage(),e);
                        }finally {
                            sw.close();
                        }
                        success = true;
                    }catch (Exception e){
                        try{
                            Thread.sleep(1000);

                        }catch (InterruptedException e1){}
                    }
                }
            }
        }
    }



}
