
package com.soapboxrace.jaxb.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java de PersonaFriendsList complex type.
 * 
 * <p>O seguinte fragmento do esquema especifica o conteúdo esperado contido dentro desta classe.
 * 
 * <pre>
 * &lt;complexType name="PersonaFriendsList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="friendPersona" type="{}ArrayOfFriendPersona" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersonaFriendsList", propOrder = {
    "friendPersona"
})
public class PersonaFriendsList {

    protected ArrayOfFriendPersona friendPersona;

    /**
     * Obtém o valor da propriedade friendPersona.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFriendPersona }
     *     
     */
    public ArrayOfFriendPersona getFriendPersona() {
        return friendPersona;
    }

    /**
     * Define o valor da propriedade friendPersona.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFriendPersona }
     *     
     */
    public void setFriendPersona(ArrayOfFriendPersona value) {
        this.friendPersona = value;
    }

}