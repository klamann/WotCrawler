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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import wdc.db.tank.Equipment;
import wdc.db.tank.Tank;

/**
 * Base Properties that are shared by all tanks and modules
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseProperties {
    
    /** The name of this object */
    @XmlElement
    public String name;
    
    /** The wiki page name under which the object can be found */
    @XmlElement
    public String wikiURL;
    
    /** The Nation this object belongs to */
    @XmlElement
    public Nation nation;
    
    /** The tier / level of this object */
    @XmlElement
    public byte tier;
    
    /** The cost of this object (currency in separate field) */
    @XmlElement
    public int cost;
    
    /** The currency this object is paid for with */
    @XmlElement
    public Currency currency;
    
    /**
     * The nations that are currently available ingame.
     */
    public enum Nation {
        China,
        France,
        Germany,
        UK,
        USA,
        USSR;
        
        /**
         * Nation string parser. Ignores case and leading/trailing whitespaces,
         * but is quite strict (and fast)
         * @param parse the string to parse
         * @return the recognized nation
         * @throws IllegalAccessException if the string could not be associated
         * with a nation
         */
        public static Nation parse(String parse) throws IllegalAccessException {
            String s = parse.trim().toLowerCase();
            if("china".equals(s)) {
                return Nation.China;
            } else if("france".equals(s)) {
                return Nation.France;
            }  else if("germany".equals(s)) {
                return Nation.Germany;
            }  else if("uk".equals(s)) {
                return Nation.UK;
            }  else if("usa".equals(s)) {
                return Nation.USA;
            }  else if("ussr".equals(s)) {
                return Nation.USSR;
            } else {
                throw new IllegalAccessException("Nation " + parse + " was not recognized.");
            }
        }
        
        /**
         * Advanced nation string parser. Slower, but recognizes nations as part
         * of strings. Beware of false positives...
         * Warning: Returns null values, if nation not recognized, instead of
         * exceptions!
         * @param parse the string to parse
         * @return the recognized nation
         */
        public static Nation parseAdvanced(String parse) {
            String s = parse.trim().toLowerCase();
            if(s.contains("chin")) {
                return Nation.China;
            } else if(s.contains("french") || s.contains("france")) {
                return Nation.France;
            }  else if(s.contains("german")) {
                return Nation.Germany;
            }  else if(s.contains("brit") || s.contains("uk")) {
                return Nation.UK;
            }  else if(s.contains("america") || s.contains("usa")) {
                return Nation.USA;
            }  else if(s.contains("soviet") || s.contains("ussr")) {
                return Nation.USSR;
            } else {
                return null;
            }
        }
        
    }
    
    /**
     * Some attributes of a tank vary, depending on whether the stock or top
     * equipment is mounted.
     */
    public enum Development {
        /** Stock Equipment: This is the initial equipment of a tank */
        Stock {
            public Equipment getEquip(Tank t) {
                return t.equipmentStock;
            }
        },
        /** Top Equipment: This is the best equipment that is available for a tank */
        Top {
            public Equipment getEquip(Tank t) {
                return t.equipmentTop;
            }
        };
        
        @Override
        public String toString() {
            switch(this) {
                case Stock: return "stock";
                case Top: return "top";
                default: return null;
            }
        }
        
        /**
         * Just a single character (T for Top and S for Stock)
         * @return one character representation of top and stock
         */
        public String toShortString() {
            switch(this) {
                case Stock: return "S";
                case Top: return "T";
                default: return null;
            }
        }
        
        /**
         * Returns the stock or top equipment for the given tank, depending
         * on the given Development-parameter
         * @param t the equipment of this tank is selected
         * @param dev based on this development
         * @return stock or top equipment
         */
        public Equipment getEquip(Tank t, Development dev) {
            switch(dev) {
                case Stock:
                    return t.equipmentStock;
                case Top:
                    return t.equipmentTop;
                default:
                    return null;
            }
        }
    }
    
    /**
     * 
     */
    public enum Currency {
        /** Credits: Regular ingame currency */
        Credits,
        /** Gold: Can only be bought for actual real world money */
        Gold,
        /** Premium: This is not a real currency, but the existence of modules
            that are only available for premium tanks (and therefore can't be
            bought) made this field necessary */
        Premium;
        
        /**
         * Currency string parser. Ignores case and leading/trailing whitespaces,
         * but is quite strict (and fast)
         * @param the string to parse
         * @return the recognized currency
         * @throws IllegalAccessException if the string was not recignized
         */
        public static Currency parse(String parse) throws IllegalAccessException {
            String s = parse.trim().toLowerCase();
            if ("credits".equals(s)) {
                return Currency.Credits;
            } else if ("gold".equals(s)) {
                return Currency.Gold;
            } else if ("premium".equals(s)) {
                return Currency.Premium;
            } else {
                throw new IllegalAccessException("Currency " + parse + " was not recognized.");
            }
        }
    }
    
}
