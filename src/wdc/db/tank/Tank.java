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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import wdc.db.BaseProperties;
/**
 * Tank holds all attributes of a single tank.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "tank")
public class Tank extends BaseProperties {
    
    /** The ID of this tank (unique identifier, used to maintain references in
        the xml database) */
    @XmlID
    @XmlAttribute
    public String id;
    
    /** The type of the tank: Light/Medium/Heavy Tank, Tank Destroyer or Artillery (SPG) */
    @XmlElement
    public TankType type;
    
    /** The minimum battle tier the tank (usually) participates in */
    @XmlElement
    public byte battleTierMin;
    
    /** The maximum battle tier the tank (usually) participates in */
    @XmlElement
    public byte battleTierMax;
    
    /** This tank is only accessible as special gift */
    @XmlElement
    public boolean gift;
    
    /** Hull armor (mm) - Front */
    @XmlElement
    public double hullFront;
    
    /** Hull armor (mm) - Sides */
    @XmlElement
    public double hullSide;
    
    /** Hull armor (mm) - Rear */
    @XmlElement
    public double hullRear;
    
    /** Maximum speed (km/h) on even ground */
    @XmlElement
    public double speed;
    
    /** The number of crew members */
    @XmlElement
    public byte crewMembers;
    
    /** Max. rotation of gun / turret to the left (deg) */
    @XmlElement
    public double gunArcLeft;       // change to gunArcLeft on next revision
    
    /** Max. rotation of the gun to the right (deg).
        Note that 360° means the tower can rotate infinitely */
    @XmlElement
    public double gunArcRight;      // change to gunArcRight on next revision
    
    /** This object holds the stock equipment for this tank */
    @XmlElement(name = "stock")
    public Equipment equipmentStock;
    
    /** This object holds the top equipment for this tank */
    @XmlElement(name = "top")
    public Equipment equipmentTop;
    
    /** A list of tanks that lead to this tank in the tech tree (only empty for tier 1 tanks) */
    @XmlElementWrapper(name = "parents")
    @XmlElement(name = "tank")
    public List<TankRef> parents = new ArrayList<TankRef>();
    
    /** A list of tanks this tank leads to in the tech tree */
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "tank")
    public List<TankRef> children = new ArrayList<TankRef>();
    
    /**
     * Adds a parent tank to the list of parents
     * @param parent the parent tank
     */
    public void addParent(Tank parent) {
        parents.add(new TankRef(parent));
    }
    
    /**
     * Adds a child tank to the list of children
     * @param child the child tank
     */
    public void addChild(Tank child) {
        children.add(new TankRef(child));
    }
    
    
    /** a list of parent names (as string). this needs to be held until the
        database is built and object references are possible */
    @XmlTransient
    public List<String> parentNames = new ArrayList<String>();
    
    /** a list of children names (as string). this needs to be held until the
        database is built and object references are possible */
    @XmlTransient
    public List<String> childrenNames = new ArrayList<String>();
    
    /**
     * adds a parent reference (as string)
     * @param parent the parent name to add
     */
    public void addParentName(String parent) {
        parentNames.add(parent);
    }
    
    /**
     * adds a child reference (as string)
     * @param child the child name to add
     */
    public void addChildName(String child) {
        childrenNames.add(child);
    }
    
    /**
     * String representation of tank attributes.
     * Incomplete / for debugging purposes only...
     * @return incomplete string representation of tank attributes
     */
    @Override
    public String toString() {
        
        String parStr = "";
        for(String s : parentNames) {
            parStr += s + ", ";
        }
        String chilStr = "";
        for(String s : childrenNames) {
            chilStr += s + ", ";
        }
        
        return new StringBuffer()
                .append("Details for Tank: ").append(name)
                .append("\nBattle Tier Min: ").append(battleTierMin)
                .append("\nBattle Tier Max: ").append(battleTierMax)
                .append("\nCost: ").append(cost)
                .append("\nCrew: ").append(crewMembers)
                .append("\nCurrency: ").append(currency)
                .append("\nGun Arc: ").append(gunArcLeft).append(" / ").append(gunArcRight)
                .append("\nHull Armor: ").append(hullFront).append(" / ").append(hullSide).append(" / ").append(hullRear)
                .append("\nNation: ").append(nation)
                .append("\nTopspeed: ").append(speed)
                .append("\nTier: ").append(tier)
                .append("\nType: ").append(type)
                .append("\n").append(equipmentStock.toString())
                .append("\n").append(equipmentTop.toString())
                .append("\nParent(s): ").append(parStr)
                .append("\nChild(ren): ").append(chilStr)
                .toString();
    }
    
    
    /**
     * The Tank Type
     */
    public enum TankType {
        LightTank,
        MediumTank,
        HeavyTank,
        TankDestroyer,
        SelfPropelledGun;
        
        /**
         * TankType string parser. Very strict (and fast)
         * @param s the sting to parse
         * @return the corresponding TankType
         * @throws IllegalAccessException if the TankType was not recognized
         */
        public static TankType parse(String s) throws IllegalAccessException {
            if("Light Tank".equals(s)) {
                return TankType.LightTank;
            } else if("Medium Tank".equals(s)) {
                return TankType.MediumTank;
            }  else if("Heavy Tank".equals(s)) {
                return TankType.HeavyTank;
            }  else if("TD".equals(s) || "Tank Destroyer".equals(s) || "Turreted TD".equals(s)) {
                return TankType.TankDestroyer;
            }  else if("SPG".equals(s) || "Self Propelled Gun".equals(s)) {
                return TankType.SelfPropelledGun;
            } else {
                throw new IllegalAccessException("Tank Type " + s + " was not recognized.");
            }
        }
        
        /**
         * Returns the name of the Overviewpage for this tank type in the
         * WoT wiki
         * @return wiki page name
         */
        public String getOverviewPage() {
            switch (this) {
                case LightTank: return "Light_Tanks";
                case MediumTank: return "Medium_Tanks";
                case HeavyTank: return "Heavy_Tanks";
                case TankDestroyer: return "Tank_Destroyers";
                case SelfPropelledGun: return "Self_Propelled_Guns";
                default: return this.name();
            }
        }
        
        @Override
        public String toString() {
            switch (this) {
                case LightTank: return "Light";
                case MediumTank: return "Medium";
                case HeavyTank: return "Heavy";
                case TankDestroyer: return "TD";
                case SelfPropelledGun: return "SPG";
                default: return this.name();
            }
        }
    }
    
    
}

