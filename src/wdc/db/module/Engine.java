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
 * Tank Module: Engine
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
@XmlRootElement(name = "engine")
public class Engine extends Module implements Comparable<Engine> {
    
    /** Engine power (HP) */
    @XmlElement
    public int power;
    
    /** Chance that the engine will catch fire when hit (%) */
    @XmlElement
    public double firechance;
    
    /** Type of gas needed by the engine */
    @XmlElement
    public Gas gas;
    
    /**
     * Compares two engines (implements java.lang.comparable) using their
     * horse power value
     * @param other the engine to compare with
     * @return comparison result: below 0 means this is smaller, above 0 means
     * this is bigger, 0 means both are equal
     */
    @Override
    public int compareTo(Engine other) {
        return this.power - other.power;
    }
    
    /**
     * Different gas types used in the game
     */
    public enum Gas {
        Gasoline,
        Diesel;
        
        /**
         * Gas string parser. Ignores case and leading/trailing whitespaces,
         * but is quite strict (and fast)
         * @param parse the string to parse
         * @return the recognized gas type
         * @throws IllegalAccessException if the string could not be associated
         * with a gas type
         */
        public static Gas parse(String parse) throws IllegalAccessException {
            String s = parse.trim().toLowerCase();
            if(s.startsWith("gasoline")) {
                return Gas.Gasoline;
            } else if(s.startsWith("diesel")) {
                return Gas.Diesel;
            } else {
                throw new IllegalAccessException("Gas type " + parse + " was not recognized.");
            }
        }
        
    }
    
}
