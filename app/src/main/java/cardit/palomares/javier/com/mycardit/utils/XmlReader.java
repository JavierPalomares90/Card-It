package cardit.palomares.javier.com.mycardit.utils;

/**
 * Created by javierpalomares on 11/7/15.
 */


import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlReader {

    private Document doc;
    private Element root;
    private Element current;

    public XmlReader(DefinitionSource src) throws IOException, SAXException,ParserConfigurationException
    {
        this(src.getInputSource());
    }

    public XmlReader(InputStream in)  throws IOException, SAXException,ParserConfigurationException
    {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(in);
            root = doc.getDocumentElement();
        }catch(DOMException e){
            throw new SAXException(e.getMessage(),e);
        }
    }

    public XmlReader(Reader in) throws IOException, SAXException, ParserConfigurationException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(in));
            root = doc.getDocumentElement();
        }catch(DOMException e){
            throw new SAXException(e.getMessage(),e);
        }
    }

    protected XmlReader(DocumentBuilder builder, DefinitionSource src) throws IOException, SAXException, ParserConfigurationException
    {
        try{
            doc = builder.parse(new InputSource(src.getInputSource()));
            root = doc.getDocumentElement();
        }catch (DOMException e)
        {
            throw new SAXException(e.getMessage(),e);
        }
    }

    public void close(){
        if (doc != null)
        {
            doc = null;
            root = null;
            current = null;
        }
    }

    public Element getRoot(){
        return root;
    }

    public int getIntAttribute(Element node, String name, int defVal){
        String val = getStringAttribute(node,name,Integer.toString(defVal));
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e)
        {
            //TODO: Log error to logcat
        }
        return defVal;
    }

    public long getLongAttribute(Element node, String name, long defVal)
    {
        String val = getStringAttribute(node,name,Long.toString(defVal));
        try{
            return Long.parseLong(val);
        }catch (NumberFormatException e)
        {
            //TODO: Log error to logcat
        }
        return defVal;
    }

    public boolean getBoolAttribute(Element node,String name, boolean defVal)
    {
        String val = getStringAttribute(node,name,Boolean.toString(defVal));
        if(val != null)
        {
            if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes"))
            {
                return true;
            }
            return false;
        }
        return defVal;
    }

    public String getStringAttribute(Element node, String name, String defVal)
    {
        String val = null;
        if (node.hasAttribute(name)){
            val = node.getAttribute(name);
        }

        if (val == null){
            return defVal;
        }
        return val;
    }

    public static String getNodeName(Element node)
    {
        String val = node.getNodeName();
        if (val != null)
        {
            int offset = val.indexOf(":");
            if(offset > 0)
            {
                return val.substring(offset+1);
            }
        }
        return val;
    }

    public Element nextNode(Element node)
    {
        Node ptr = node;
        do{
            ptr = node.getNextSibling();
            if(ptr != null && ptr.getNodeType() == Node.ELEMENT_NODE)
            {
                break;
            }
        }while(ptr != null && ptr.getNodeType() != Node.ELEMENT_NODE);
        return (Element) ptr;
    }


    public Element prevNode(Element node)
    {
        Node ptr = node;
        do{
            ptr = node.getPreviousSibling();
            if(ptr != null && ptr.getNodeType() == Node.ELEMENT_NODE)
            {
                break;
            }
        }while (ptr != null && ptr.getNodeType() != Node.ELEMENT_NODE);
        return (Element)ptr;
    }

    public long getNodeContentsLong(Element node, long defValue){
        if (node != null){
            String text = getNodeContents(node);
            if (text != null){
                try{
                    String hexCheck = text.substring(0,2);
                    if (hexCheck.equals("0x")){
                        return Long.parseLong(text.substring(2),16);
                    }
                    else{
                        return Long.parseLong(text);
                    }
                }catch (NumberFormatException ex){}
                catch (StringIndexOutOfBoundsException ex){
                    try{
                        return Long.parseLong(text);
                    }catch (NumberFormatException ex2){
                        return defValue;
                    }
                }
            }
        }
        return defValue;
    }

    public String getNodeContents(Element node){
        if (node != null){
            String text = node.getTextContent();
            if (text != null){
                return text.trim();
            }
        }
        return null;
    }

    public String getNodeContents(Element node, boolean needDecrypted){
        String contents = getNodeContents(node);
        needDecrypted = false;
        return contents;
    }

    public Element getChildNode(Element parent, String name)
    {
        if (parent.hasChildNodes()){
            NodeList list = parent.getChildNodes();
            if (list != null)
            {
                for(int i = 0; i < list.getLength(); i++)
                {
                    Node next = list.item(i);
                    if (next.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element el = (Element)next;
                        String nodeName = XmlReader.getNodeName(el);
                        if (nodeName.equalsIgnoreCase(name)){
                            return el;
                        }
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<Element> getChildNodes(Element parent, String name)
    {
        ArrayList<Element> retVal = new ArrayList<Element>();
        if (parent.hasChildNodes()){
            NodeList list = parent.getChildNodes();
            if(list != null){
                for (int i = 0; i < list.getLength(); i++){
                    Node next = list.item(i);
                    if (next.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element el = (Element)next;
                        String nodeName = XmlReader.getNodeName(el);
                        if(nodeName.equalsIgnoreCase(name))
                        {
                            retVal.add(el);
                        }
                    }
                }
            }
        }
        return retVal;
    }

    public ArrayList<Element> getChildNodes(Element parent)
    {
        ArrayList<Element> retVal = new ArrayList<Element>();

        if (parent.hasChildNodes())
        {
            NodeList list = parent.getChildNodes();

            if (list != null)
            {
                for (int i = 0; i < list.getLength(); i++)
                {
                    Node next = list.item(i);
                    if(next.getNodeType() == Node.ELEMENT_NODE)
                    {
                        retVal.add((Element)next);
                    }
                }
            }
        }
        return retVal;
    }
}
