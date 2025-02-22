//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.20 at 11:20:44 AM CEST 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BusDeviceTypes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BusDeviceTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CHILLIICONTROLLER"/>
 *     &lt;enumeration value="EEBUSGATEWAY"/>
 *     &lt;enumeration value="HABITEQGATEWAY"/>
 *     &lt;enumeration value="HAGERGATEWAY"/>
 *     &lt;enumeration value="MIELEGATEWAY"/>
 *     &lt;enumeration value="PLUGWISEGATEWAY"/>
 *     &lt;enumeration value="WAGO750-860"/>
 *     &lt;enumeration value="FILELOGGER"/>
 *     &lt;enumeration value="OTHER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BusDeviceTypes")
@XmlEnum
public enum BusDeviceTypes {

    CHILLIICONTROLLER("CHILLIICONTROLLER"),
    EEBUSGATEWAY("EEBUSGATEWAY"),
    HABITEQGATEWAY("HABITEQGATEWAY"),
    HAGERGATEWAY("HAGERGATEWAY"),
    MIELEGATEWAY("MIELEGATEWAY"),
    PLUGWISEGATEWAY("PLUGWISEGATEWAY"),
    @XmlEnumValue("WAGO750-860")
    WAGO_750_860("WAGO750-860"),
    FILELOGGER("FILELOGGER"),
    OTHER("OTHER");
    private final String value;

    BusDeviceTypes(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BusDeviceTypes fromValue(String v) {
        for (BusDeviceTypes c: BusDeviceTypes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
