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
package wdc.db;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import wdc.db.tank.Tank;

/**
 * TanksDB is the root object of the tank database. It holds a list of all tanks
 * and the Modules-object, which holds lists of all modules.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "wot-db")
public class TanksDB {
    
    /** A list of all tanks that are available in the game */
    @XmlElementWrapper(name = "tanks")
    @XmlElement(name = "tank")
    public List<Tank> tanks;
    
    /** All available modules, sorted by module type */
    @XmlElement
    public Modules modules;

}
