//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.20 at 11:20:44 AM CEST 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComDeviceTypes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ComDeviceTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="USERINTERACTIONDEVICE"/>
 *     &lt;enumeration value="GUI"/>
 *     &lt;enumeration value="MULTI_COMMODITY"/>
 *     &lt;enumeration value="ELECTRICITY"/>
 *     &lt;enumeration value="GAS"/>
 *     &lt;enumeration value="WATER"/>
 *     &lt;enumeration value="SEWAGE"/>
 *     &lt;enumeration value="GENERALLOGGER"/>
 *     &lt;enumeration value="FILELOGGER"/>
 *     &lt;enumeration value="DATABASELOGGER"/>
 *     &lt;enumeration value="OTHER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ComDeviceTypes")
@XmlEnum
public enum ComDeviceTypes {

    USERINTERACTIONDEVICE,
    GUI,
    MULTI_COMMODITY,
    ELECTRICITY,
    GAS,
    WATER,
    SEWAGE,
    GENERALLOGGER,
    FILELOGGER,
    DATABASELOGGER,
    OTHER;

    public String value() {
        return name();
    }

    public static ComDeviceTypes fromValue(String v) {
        return valueOf(v);
    }

}
