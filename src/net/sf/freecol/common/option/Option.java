
package net.sf.freecol.common.option;

import net.sf.freecol.client.gui.i18n.Messages;

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public interface Option {

    public static final String NO_ID = "NO_ID";

    

    /**
    * Gives a short description of this <code>Option</code>.
    * Can for instance be used as a tooltip text.
    */
    public String getShortDescription();


    /**
    * Returns a textual representation of this object.
    * @return The name of this <code>Option</code>.
    * @see #getName
    */
    public String toString() ;


    /**
    * Returns the id of this <code>Option</code>.
    * @return The unique identifier as provided in the constructor.
    */
    public String getId();


    /**
    * Returns the name of this <code>Option</code>.
    * @return The name as provided in the constructor.
    */
    public String getName();


    /**
    * Makes an XML-representation of this object.
    *
    * @param document The document to use when creating new componenets.
    * @return The DOM-element ("Document Object Model") made to represent this "Option".
    */
    public Element toXMLElement(Document document);


    /**
    * Initializes this object from an XML-representation of this object.
    * @param element The DOM-element ("Document Object Model") made to represent this "Option".
    */
    public void readFromXMLElement(Element element);
}
