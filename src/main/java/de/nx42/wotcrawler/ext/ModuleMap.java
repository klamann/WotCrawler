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
package de.nx42.wotcrawler.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nx42.wotcrawler.db.BaseProperties.Development;
import de.nx42.wotcrawler.db.TanksDB;
import de.nx42.wotcrawler.db.module.Engine;
import de.nx42.wotcrawler.db.module.Gun;
import de.nx42.wotcrawler.db.module.Module;
import de.nx42.wotcrawler.db.module.Module.ModuleType;
import de.nx42.wotcrawler.db.module.Radio;
import de.nx42.wotcrawler.db.module.Suspension;
import de.nx42.wotcrawler.db.module.Turret;
import de.nx42.wotcrawler.db.tank.Tank;
import de.nx42.wotcrawler.db.tank.TankRef;

/**
 * The ModuleMap holds a Map for each module type that maps from each tank
 * to a list of compatible modules. The lists are static so they can be
 * accessed by anonymous inner classes like the enums of the Field class.
 * 
 * The purpose of this class is to give fast access for any tank to it's modules.
 * The list of modules is ordered from worst to best, so the stock and top modules
 * can be accessed easily.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class ModuleMap {
    
    private static final Logger log = LoggerFactory.getLogger(ModuleMap.class);
    
    /** Maps from a Tank to its compatible engines, ordered from worst to best */
    public static Map<Tank, List<Engine>> engine = new HashMap<Tank, List<Engine>>();
    /** Maps from a Tank to its compatible guns, ordered from worst to best */
    public static Map<Tank, List<Gun>> gun = new HashMap<Tank, List<Gun>>();
    /** Maps from a Tank to its compatible radios, ordered from worst to best */
    public static Map<Tank, List<Radio>> radio = new HashMap<Tank, List<Radio>>();
    /** Maps from a Tank to its compatible suspensions, ordered from worst to best */
    public static Map<Tank, List<Suspension>> suspension = new HashMap<Tank, List<Suspension>>();
    /** Maps from a Tank to its compatible turrets, ordered from worst to best */
    public static Map<Tank, List<Turret>> turret = new HashMap<Tank, List<Turret>>();
    
    /**
     * Builds the module map for each tank and module from the given TanksDB
     * @param db the database to build the maps from
     * @return the filled ModuleMap
     */
    public static ModuleMap build(TanksDB db) {
        ModuleMap mm = new ModuleMap();
        mm.buildModuleMapping(db);
        return mm;
    }
    
    /**
     * Builds the Module mapping for all tanks.
     * Fills the static Maps that map from each tank to it's list of Modules.
     * The maps are sorted in ascending order, so the worst module is at the
     * first position, the best at last.
     * The maps are heavily used by the enum specific functions, so they need
     * to be static...
     */
    public final void buildModuleMapping(TanksDB db) {
        
        // tanks init
        for (Tank t : db.tanks) {
            engine.put(t, new ArrayList<Engine>(2));
            gun.put(t, new ArrayList<Gun>(2));
            radio.put(t, new ArrayList<Radio>(2));
            suspension.put(t, new ArrayList<Suspension>(2));
            turret.put(t, new ArrayList<Turret>(2));
        }
        
        
        // engines
        for (Engine e : db.modules.engines) {
            for(TankRef t : e.compatibility) {
                if(t.ref != null) {
                    engine.get(t.ref).add(e);
                }
            }
        }
        
        // guns
        for (Gun g : db.modules.guns) {
            for(TankRef t : g.compatibility) {
                if(t.ref != null) {
                    gun.get(t.ref).add(g);
                }
            }
        }
        
        // radios
        for (Radio r : db.modules.radios) {
            for(TankRef t : r.compatibility) {
                if(t.ref != null) {
                    radio.get(t.ref).add(r);
                }
            }
        }
        
        // suspensions
        for (Suspension s : db.modules.suspensions) {
            for(TankRef t : s.compatibility) {
                if(t.ref != null) {
                    suspension.get(t.ref).add(s);
                }
            }
        }
        
        // turrets
        for (Turret tr : db.modules.turrets) {
            for(TankRef t : tr.compatibility) {
                if(t.ref != null) {
                    turret.get(t.ref).add(tr);
                }
            }
        }
        
        
        // sort
        for (List<Engine> eMaps : engine.values()) {
            Collections.sort(eMaps);
        }
        for (List<Gun> gMaps : gun.values()) {
            Collections.sort(gMaps);
        }
        for (List<Radio> rMaps : radio.values()) {
            Collections.sort(rMaps);
        }
        for (List<Suspension> sMaps : suspension.values()) {
            Collections.sort(sMaps);
        }
        for (List<Turret> tMaps : turret.values()) {
            Collections.sort(tMaps);
        }
        
    }
    
    /**
     * Returns the list of modules for a given Tank and the specified
     * module type
     * @param t the compatible modules for this tank will be searched
     * @param type only the modules of this type will be returned
     * @return the list of specified modules for this tank
     */
    public static List<? extends Module> getModules(Tank t, ModuleType type) {
        switch(type) {
            case Engine:
                return engine.get(t);
            case Radio:
                return radio.get(t);
            case Gun:
                return gun.get(t);
            case Suspension:
                return suspension.get(t);
            case Turret:
                return turret.get(t);
            default:
                return null;
        }
    }
    
    /**
     * Returns the list of modules for a given Tank and the specified
     * module type.
     * This is a desperate approach to teach java the purpose of generics. I
     * guess it is obvious that this is not an optimal solution...
     * @param <M> The module type
     * @param type the class that defines the module type
     * @param t the compatible modules for this tank will be searched
     * @return the list of specified modules for this tank
     */
    public static <M> List getModules(Class<M> type, Tank t) {
        if (type.getSuperclass().isAssignableFrom(Module.class)) {
            // is module
            
            if(type.isAssignableFrom(Engine.class)) {
                return engine.get(t);
            } else if(type.isAssignableFrom(Gun.class)) {
                return gun.get(t);
            } else if(type.isAssignableFrom(Radio.class)) {
                return radio.get(t);
            } else if(type.isAssignableFrom(Suspension.class)) {
                return suspension.get(t);
            } else if(type.isAssignableFrom(Turret.class)) {
                return turret.get(t);
            } else {
                throw new IllegalArgumentException("Unknown Module Type " + type.getName());
            }
            
        } else {
            // is not a module
            throw new IllegalArgumentException("This method accepts only subclasses "
                    + "of 'Module' as parameters. Wrong parameter was " + type.getName());
        }
    }
    
    /**
     * Retrieves the Module that is considered as "Top" or "Stock" for the specified
     * Tank. The type of module that is returned is defined by the given class.
     * @param <M> The module type
     * @param type the class that defines the module type
     * @param t the modules from this tank are retrieved
     * @param dev stock or top?
     * @return correct module (stock or top)
     */
    public static <M> M getModuleByDev(Class<M> type, Tank t, Development dev) {
        List<M> modules = getModules(type, t);
        
        if (modules.isEmpty()) {
            try {
                /*
                 * just return a new instance to prevent nullpointers.
                 * for a detailed error report, use Evaluator.class
                 * querying this here results in error flood for even the slightest anomality...
                 */
                return type.newInstance();
            } catch (Exception e) {
                log.error("Error while instantiating a new module (for the "
                        + "list of valid modules of this type was empty)", e);
                return null;
            }
        } else {
            switch (dev) {
                case Stock:
                    return modules.get(0);
                case Top:
                    return modules.get(modules.size() - 1);
                default:
                    return null;
            }
        }
    }
    
}
