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
package wdc.db.tank;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import wdc.db.BaseProperties.Development;

/**
 * This class holds all tank equipment, that is not necessarily part of a
 * specific module, but still changes depending on whether the tank is equipped 
 * with stock or top modules.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "details")
public class Equipment {
    
    /** Indicates, whether this object contains stock or top equipment */
    @XmlAttribute
    public Development development;
    
    /** Hitpoints */
    @XmlElement
    public int hitpoints;
    
    /** Current weight (tons) */
    @XmlElement
    public double weight;
    
    /** Maximum load that can be carried (tons) */
    @XmlElement
    public double weightLimit;
    
    /** Minimum gun elevation (deg) = Lowest angle the gun can be positioned */
    @XmlElement
    public double gunElevationLow;
    
    /** Maximum gun elevation (deg) = Highest angle the gun can be positioned */
    @XmlElement
    public double gunElevationHigh;
    
    /** The view range of the tank (m) */
    @XmlElement
    public double viewRange;
    
    
    public Equipment() {
        // default constructor needed for xml stuff...
    }
    
    public Equipment(Development dev) {
        this.development = dev;
    }
    
    
    @Override
    public String toString() {
        return new StringBuffer()
                .append("Details (").append(development).append("):")
                .append("\n  Hitpoints: ").append(hitpoints)
                .append("\n  Elevation: ").append(gunElevationLow).append(" / ").append(gunElevationHigh)
                .append("\n  ViewRange: ").append(viewRange)
                .append("\n  Weight:    ").append(weight).append(" / ").append(weightLimit)
                .toString();
    }
}
