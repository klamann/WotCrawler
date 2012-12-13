/*
 * Copyright (C) 2012 Sebastian Straub <sebastian-straub@gmx.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wdc.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses HTML files
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Parser {
    
    // -------------------- html parsing --------------------
    
    /**
     * Parses a HTML document, transforms it into valid XML using the
     * htmlcleaner-library and returns it as org.w3c.dom.Document
     * @param file the html file to parse
     * @return org.w3c.dom.Document representation of the cleaned HTML file
     * @throws IOException cannot access the file
     * @throws ParserConfigurationException parser configuration invalid
     * @throws SAXException error while parsing (usually invalid xml)
     */
    public static Document parseHTML(File file) throws IOException, ParserConfigurationException, SAXException {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode tagNode = cleaner.clean(file);
        String cleanHTML = new SimpleXmlSerializer(cleaner.getProperties()).getAsString(tagNode);
        return buildDOM(cleanHTML);
    }
    
    /**
     * Parses a HTML document, transforms it into valid XML using the
     * htmlcleaner-library and returns it as org.w3c.dom.Document
     * @param url the url where the document can be retrieved
     * @return org.w3c.dom.Document representation of the cleaned HTML file
     * @throws IOException cannot access the file
     * @throws ParserConfigurationException parser configuration invalid
     * @throws SAXException error while parsing (usually invalid xml)
     */
    public static Document parseHTML(URL url) throws IOException, ParserConfigurationException, SAXException {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode tagNode = cleaner.clean(url);
        String cleanHTML = new SimpleXmlSerializer(cleaner.getProperties()).getAsString(tagNode);
        return buildDOM(cleanHTML);
    }
    
    // -------------------- DOM operations --------------------
    
    /**
     * Creates a org.w3c.dom.Document from a given XML String
     * @param cleanedHTML a valid xml document as string
     * @return org.w3c.dom.Document representation of the string
     * @throws ParserConfigurationException parser configuration invalid
     * @throws SAXException error while parsing (usually invalid xml)
     * @throws IOException this should not occur, necessary because the String is
     * read as InputSource
     */
    protected static Document buildDOM(String cleanedHTML) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(cleanedHTML)));
    }
    
    /**
     * Creates a simple String representation of a given DOM tree
     * @param node this node and all it's descendants will be printed
     * @return current DOM as XML String
     */
    public static String domToString(Node node) {
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(buffer));
            return buffer.toString();
        } catch (TransformerException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "xml transformation failed!";
    }
    
}
