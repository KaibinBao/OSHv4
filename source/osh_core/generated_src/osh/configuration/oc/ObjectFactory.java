//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.20 at 11:25:55 AM CEST 
//


package osh.configuration.oc;

import javax.xml.bind.annotation.XmlRegistry;

import osh.configuration.system.ConfigurationParameter;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the osh.configuration.oc package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: osh.configuration.oc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OCConfiguration }
     * 
     */
    public OCConfiguration createOCConfiguration() {
        return new OCConfiguration();
    }

    /**
     * Create an instance of {@link ConfigurationParameter }
     * 
     */
    public ConfigurationParameter createConfigurationParameter() {
        return new ConfigurationParameter();
    }

    /**
     * Create an instance of {@link GAConfiguration }
     * 
     */
    public GAConfiguration createGAConfiguration() {
        return new GAConfiguration();
    }

    /**
     * Create an instance of {@link StoppingRule }
     * 
     */
    public StoppingRule createStoppingRule() {
        return new StoppingRule();
    }

}
