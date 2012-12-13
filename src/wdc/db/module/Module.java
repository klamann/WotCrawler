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
package wdc.db.module;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import wdc.db.BaseProperties;
import wdc.db.tank.Tank;
import wdc.db.tank.TankRef;

/**
 * Abstract Tank Module description. Does currently have 5 implementations:
 * Engine, Gun, Radio, Suspension, Turret
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public abstract class Module extends BaseProperties {
    
    /** the weight of the module */
    @XmlElement
    public double weight;
    
    /** all tanks that are compatible with this module */
    @XmlElementWrapper(name = "compatibility")
    @XmlElement(name = "tank")
    public List<TankRef> compatibility = new ArrayList<TankRef>();
    
    /**
     * Adds a tank reference to the list of compatible tanks
     * @param compatible the compatible tank
     */
    public void addCompatibleTank(Tank compatible) {
        compatibility.add(new TankRef(compatible));
    }
    
    
    /**
     * The Module Type
     */
    public enum ModuleType {
        Engine,
        Gun,
        Radio,
        Suspension,
        Turret;
        
        /**
         * Returns the name of the Overviewpage for this module type in the
         * WoT wiki
         * @return wiki page name
         */
        public String getOverviewPage() {
            switch (this) {
                case Engine: return "Engine";
                case Gun: return "Gun";
                case Radio: return "Radio";
                case Suspension: return "Suspension";
                case Turret: return "Turret";
                default: return this.name();
            }
        }
    }
    
}
