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
package de.nx42.wotcrawler.db.tank;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class holds a reference to a tank object. As self-references are not
 * allowed (either in the xml IDREF spec or just in jaxb, I don't know), an
 * extra class is needed to do this. So basically, it's a workaround...
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "tankref")
public class TankRef {

    /** the tank this wrapper class points to */
    @XmlAttribute
    @XmlIDREF
    public Tank ref;
    
    public TankRef() {
        // default constructor needed for xml stuff...
    }
    
    public TankRef(Tank tank) {
        ref = tank;
    }
}