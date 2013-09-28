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
package de.nx42.wotcrawler.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.nx42.wotcrawler.db.TanksDB;

/**
 * Serializes java objects and deserializes XML documents using the JAXB library
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Serializer {
    
    private static final Logger log = LoggerFactory.getLogger(Serializer.class);
    
    // -------------------- Serialize java objects --------------------
    
    /**
     * Serialize an object to XML using JAXB
     * @param c the class of the object to serialize (wtf is wrong with reflection?!)
     * @param instance the actual object instance to serialize
     * @param schemaLocation schemalocation tag that is added to the xml
     * @param output the file to write the xml into
     */
    public static void serialize(Class c, Object instance, String schemaLocation, File output) {
        
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
            m.marshal(instance, output);
        } catch (JAXBException ex) {
            log.error("Error serializing object to XML", ex);
        }
        
    }
    
    /**
     * Serialize an object to XML using JAXB
     * @param c the class of the object to serialize (wtf is wrong with reflection?!)
     * @param instance the actual object instance to serialize
     * @param output the file to write the xml into
     */
    public static void serialize(Class c, Object instance, File output) {
        
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(instance, output);
        } catch (JAXBException ex) {
            log.error("Error serializing object to XML", ex);
        }
        
    }
    
    // -------------------- deserialize XML documents --------------------
    
    /**
     * Deserializes an XML document to a java object using JAXB
     * @param <S> the type of the java object to create
     * @param c the class of the object to create
     * @param xml the xml file to deserialize
     * @param schema the corresponding schema file, to check validity
     * @return the deserialized java object
     */
    public static <S> S deserialize(Class c, File xml, File schema) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema s = sf.newSchema(schema);
            return deserialize(c, xml, s);
        } catch (SAXException ex) {
            log.error("Error deserializing object from XML", ex);
            return null;
        }
        
    }
    
    /**
     * Deserializes an XML document to a java object using JAXB
     * @param <S> the type of the java object to create
     * @param c the class of the object to create
     * @param xml the xml file to deserialize
     * @param schema the corresponding schema file, to check validity
     * @return the deserialized java object
     */
    public static <S> S deserialize(Class c, File xml, URL schema) {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema s = sf.newSchema(schema);
            return deserialize(c, xml, s);
        } catch (SAXException ex) {
            log.error("Error deserializing object from XML", ex);
            return null;
        }
    }
    
    /**
     * Deserializes an XML document to a java object using JAXB
     * @param <S> the type of the java object to create
     * @param c the class of the object to create
     * @param xml the xml file to deserialize
     * @param schema the corresponding schema file as Schema object
     * @return the deserialized java object
     */
    public static <S> S deserialize(Class c, File xml, Schema schema) {
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema);
            S deserialized = (S) unmarshaller.unmarshal(xml);
            return deserialized;
        } catch (JAXBException ex) {
            log.error("Error deserializing object from XML", ex);
            return null;
        }
    }
    
    /**
     * Deserializes an XML document to a java object using JAXB.
     * Warning: Using this method without specifying a schema is insecure!
     * Invalid xml documents can generate corrupt java objects.
     * @param <S> the type of the java object to create
     * @param c the class of the object to create
     * @param xml the xml file to deserialize
     * @return the deserialized java object
     */
    public static <S> S deserialize(Class c, File xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            S deserialized = (S) unmarshaller.unmarshal(xml);
            return deserialized;
        } catch (JAXBException ex) {
            log.error("Error deserializing object from XML", ex);
            return null;
        }
    }
    
    // -------------------- XML schema --------------------
    
    /**
     * Generates an xml schema from the current TanksDB class
     * @param dest the file where the schema shall be written in
     */
    public static void generateSchema(String dest) {
        try {
            JAXBContext context = JAXBContext.newInstance(TanksDB.class);
            context.generateSchema(new MySchemaOutputResolver(dest));
        } catch (JAXBException ex) {
            log.error("Error generating schema", ex);
        } catch (IOException ex) {
            log.error("Error writing schema", ex);
        }
        
    }
    
    /**
     * The schema is written to a file using this SchemaOutputResolver
     */
    private static class MySchemaOutputResolver extends SchemaOutputResolver {
        
        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return new StreamResult(new File(dest));
        }
        
        private String dest;
        
        public MySchemaOutputResolver(String dest) {
            this.dest = dest;
        }
    }
    
}
