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
package de.nx42.wotcrawler.util;

import java.io.Serializable;

/**
 * Creates a Tuple of two Objects
 * 
 * @author Sebastian Straub
 * @param <X> Type of the first Object
 * @param <Y> Type of the second Object
 */
public class Tuple<X, Y> implements Serializable {

    /** used to deserialize this class */
    private static final long serialVersionUID = 0xDEADC0DE;
    
    /** first Object */
    protected final X fst;
    /** second Object */
    protected final Y snd;

    /**
     * Creates a new Tuple
     * @param fst first Object
     * @param snd second Object
     */
    public Tuple(X fst, Y snd) {
        this.fst = fst;
        this.snd = snd;
    }

    /**
     * @return first Object of this Tuple
     */
    public X fst() {
        return fst;
    }
    
    /**
     * @return first Object of this Tuple
     */
    public X x() {
        return fst;
    }

    /**
     * @return second Object of this Tuple
     */
    public Y snd() {
        return snd;
    }
    
    /**
     * @return second Object of this Tuple
     */
    public Y y() {
        return snd;
    }
    
    /**
     * call Tuple.of(x, y) instead of new Tuple<X, Y>(x,y)
     * @param <X> Type of the first Object (will be infered)
     * @param <Y> Type of the second Object (will be infered)
     * @param fst the first Object of the Tuple
     * @param snd the second Object of the Tuple
     * @return a correctly typed Tuple containing the two Objects
     */
    public static <X, Y> Tuple<X, Y> of(X fst, Y snd) {
        return new Tuple<X, Y>(fst, snd);
    }

    /**
     * @return a String representation of this Tuple formatted as (x,y)
     */
    @Override
    public String toString() {
        return String.format("(%s, %s)", fst, snd);
    }
    
}
