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
package de.nx42.wotcrawler.db;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import de.nx42.wotcrawler.db.module.Engine;
import de.nx42.wotcrawler.db.module.Gun;
import de.nx42.wotcrawler.db.module.Radio;
import de.nx42.wotcrawler.db.module.Suspension;
import de.nx42.wotcrawler.db.module.Turret;

/**
 * Modules is part of the tanks database and contains all available modules,
 * each type in it's own list.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "modules")
public class Modules {
    
    /** A list of all engines that are available in the game */
    @XmlElementWrapper(name = "engines")
    @XmlElement(name = "engine")
    public List<Engine> engines;
    
    /** A list of all guns that are available in the game */
    @XmlElementWrapper(name = "guns")
    @XmlElement(name = "gun")
    public List<Gun> guns;
    
    /** A list of all radios that are available in the game */
    @XmlElementWrapper(name = "radios")
    @XmlElement(name = "radio")
    public List<Radio> radios;
    
    /** A list of all suspensions that are available in the game */
    @XmlElementWrapper(name = "suspensions")
    @XmlElement(name = "suspension")
    public List<Suspension> suspensions;
    
    /** A list of all turrets that are available in the game */
    @XmlElementWrapper(name = "turrets")
    @XmlElement(name = "turret")
    public List<Turret> turrets;
    
}
