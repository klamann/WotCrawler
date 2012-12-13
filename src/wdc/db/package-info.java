/**
 * This package contains the full database definition that is used by this
 * application.
 * On the top layer (this package) there are only classes that cumulate the single
 * objects that are defined in the child packages.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */

@XmlSchema (
    xmlns = {},
    namespace = "http://nx42.de/projects/wot/schema11",
    elementFormDefault = XmlNsForm.QUALIFIED,
    attributeFormDefault = XmlNsForm.UNSET
)
package wdc.db;

import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
