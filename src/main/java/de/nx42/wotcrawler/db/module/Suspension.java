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
package de.nx42.wotcrawler.db.module;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Tank Module: Suspension
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "suspension")
public class Suspension extends Module implements Comparable<Suspension> {
    
    /** Maximum load that can be carried (tons) */
    @XmlElement
    public double load;
    
    /** Rotation speed of the standing tank (deg/s) */
    @XmlElement
    public int traverse;
    
    /**
     * Compares two suspensions (implements java.lang.comparable) using their
     * tier as reference
     * @param other the suspension to compare with
     * @return comparison result: below 0 means this is smaller, above 0 means
     * this is bigger, 0 means both are equal
     */
    @Override
    public int compareTo(Suspension other) {
        return this.tier - other.tier;
    }
    
}
