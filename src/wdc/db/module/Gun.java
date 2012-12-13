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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Tank Module: Gun
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "gun")
public class Gun extends Module implements Comparable<Gun> {
    
    /** Ammo capacity (shells - minimum) */
    @XmlElement
    public int ammoCapacityMin;
    
    /** Ammo capacity (shells - maximum) */
    @XmlElement
    public int ammoCapacityMax;
    
    /** Gun Damage (AP = Armor Piercing) */
    @XmlElement
    public int dmgAP;
    
    /** Gun Damage (APCR = Armor Piercing Composite Rigid) */
    @XmlElement
    public int dmgAPCR;
    
    /** Gun Damage (HE = High Explosive)
        Note: For tanks that use more than one HE ammo type, only the first is selected */
    @XmlElement
    public int dmgHE;
    
    /** Gun Damage (HEAT = High Explosive Anti-Tank) */
    @XmlElement
    public int dmgHEAT;
    
    /** Shell Penetration in mm (AP = Armor Piercing) */
    @XmlElement
    public int penAP;
    
    /** Shell Penetration in mm (APCR = Armor Piercing Composite Rigid) */
    @XmlElement
    public int penAPCR;
    
    /** Shell Penetration in mm (HE = High Explosive)
        Note: For tanks that use more than one HE ammo type, only the first is selected */
    @XmlElement
    public int penHE;
    
    /** Shell Penetration in mm (HEAT = High Explosive Anti-Tank) */
    @XmlElement
    public int penHEAT;
    
    /** Fire Rate (Shots per Minute - minimum) */
    @XmlElement
    public double fireRateMin;
    
    /** Fire Rate (Shots per Minute - maximum) */
    @XmlElement
    public double fireRateMax;
    
    /** Accuracy (diameter in meters at 100m distance - minimum) */
    @XmlElement
    public double accuracyMin;
    
    /** Accuracy (diameter in meters at 100m distance - maximum) */
    @XmlElement
    public double accuracyMax;
    
    /** Aim Time (seconds - minimum) */
    @XmlElement
    public double aimTimeMin;
    
    /** Aim Time (seconds - maximum) */
    @XmlElement
    public double aimTimeMax;
    
    
    /**
     * Compares two guns (implements java.lang.comparable) using their tier.
     * This should maybe be replaced by a more sophisticated weapon rating (as
     * not always the higher tier weapon is better), but as this method may be
     * called quite often, this would probably significantly increase runtime.
     * @param other the gun to compare with
     * @return comparison result: below 0 means this is smaller, above 0 means
     * this is bigger, 0 means both are equal
     */
    @Override
    public int compareTo(Gun other) {
        
        // TODO more detailed weapon rating necessary
        // double dpmThis = this.dmgAP * this.fireRateMin / 60;
        
        return this.tier - other.tier;
    }
    
}
