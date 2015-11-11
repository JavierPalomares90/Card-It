package cardit.palomares.javier.com.mycardit.cardit.palaomares.javier.com.mycardit.card.cardit.palomares.javier.com.mycardit.utils;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;

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
            return;;
        }
        parent.setAttribute(attrName,attrValue);
    }

    public Element getCurrentNode(){
        return current;
    }

    public void setCurrentNode(Element node){
        this.current = node;
    }

    
}
