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
package wdc.ext;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import wdc.db.BaseProperties.Development;
import wdc.db.TanksDB;
import wdc.db.module.Engine;
import wdc.db.module.Gun;
import wdc.db.module.Module;
import wdc.db.module.Radio;
import wdc.db.module.Suspension;
import wdc.db.module.Turret;
import wdc.db.tank.Equipment;
import wdc.db.tank.Tank;
import wdc.db.tank.Tank.TankType;
import wdc.db.tank.TankRef;
import wdc.xml.Crawler;
import wdc.xml.Transformer;

/**
 * This enum is real sugar: It contains all attributes that can be extracted
 * from the TanksDB. Furthermore, for every Field methods are provided to
 * access the value of this field for a specified tank object.
 * 
 * This means that you can create a list of Fields and access the values
 * of these fields for every tank, just by looping over it. For examples,
 * see the FieldDef and Transformer class.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public enum Field {
    
    //<editor-fold desc="all fields (enum)">
    
    // -- tank --
    
    //<editor-fold defaultstate="collapsed" desc="tank base details">
    /** The minimum battle tier the tank (usually) participates in */
    T_BattleTier_Min {
        @Override
        public String get(Tank t, Development dev) {
            return Byte.toString(t.battleTierMin);
        }
    },
    /** The maximum battle tier the tank (usually) participates in */
    T_BattleTier_Max {
        @Override
        public String get(Tank t, Development dev) {
            return Byte.toString(t.battleTierMax);
        }
    },
    /** A list of tanks this tank leads to in the tech tree */
    T_Children {
        @Override
        public String get(Tank t, Development dev) {
            return (t.children == null) ? na : buildTankRefList(t.children, t.name, LinkType.INTERNAL);
        }
    },
    /** The cost of this object (currency in separate field) */
    T_Cost {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString(t.cost);
        }
    },
    /** The number of crew members */
    T_CrewMembers {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString(t.crewMembers);
        }
    },
    /** The currency this tank is paid for with */
    T_Currency {
        @Override
        public String get(Tank t, Development dev) {
            return t.currency.toString();
        }
    },
    /** Indicates, if this tank is only accessible as special gift */
    T_Gift {
        @Override
        public String get(Tank t, Development dev) {
            return t.gift ? "Yes" : "No";
        }
    },
    /** Max. rotation of gun / turret to the left (deg) */
    T_GunArc_Left {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString(t.gunArcLeft);
        }
    },
    /** Max. rotation of the gun to the right (deg).
        Note that 360° means the tower can rotate infinitely */
    T_GunArc_Right {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString(t.gunArcRight);
        }
    },
    /** Hull armor (mm) - Front */
    T_Hull_Front {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(t.hullFront);
        }
    },
    /** Hull armor (mm) - Side */
    T_Hull_Side {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(t.hullSide);
        }
    },
    /** Hull armor (mm) - Rear */
    T_Hull_Rear {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(t.hullRear);
        }
    },
    /** The ID of this tank (unique identifier, used to maintain references in the xml database) */
    T_ID {
        @Override
        public String get(Tank t, Development dev) {
            return t.id;
        }
    },
    /** The name of this tank */
    T_Name {
        @Override
        public String get(Tank t, Development dev) {
            try {
                return String.format("<a href=\"%s\">%s</a>", Crawler.buildWikiLink(t.name).toString(), t.name);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
                return t.name;
            }
        }
    },
    /** The Nation this tank belongs to */
    T_Nation {
        @Override
        public String get(Tank t, Development dev) {
            return t.nation.toString();
        }
    },
    /** A list of tanks that lead to this tank in the tech tree (only empty for tier 1 tanks) */
    T_Parents {
        @Override
        public String get(Tank t, Development dev) {
            return (t.parents == null) ? na : buildTankRefList(t.parents, t.name, LinkType.INTERNAL);
        }
    },
    /** The tier / level of this tank */
    T_Tier {
        @Override
        public String get(Tank t, Development dev) {
            return Byte.toString(t.tier);
        }
    },
    /** Maximum speed (km/h) on even ground */
    T_TopSpeed {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(t.speed);
        }
    },
    /** The type of the tank: Light/Medium/Heavy Tank, Tank Destroyer or Artillery (SPG) */
    T_Type {
        @Override
        public String get(Tank t, Development dev) {
            return t.type.toString();
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="tank equipment (stock/top)">
    /** Indicates, if this tank's attributes are top or stock values */
    TE_Development {
        @Override
        public String get(Tank t, Development dev) {
            return dev.toShortString();
        }
    },
    /** Minimum gun elevation (deg) = Lowest angle the gun can be positioned */
    TE_Elevation_Low {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(getEquipment(t, dev).gunElevationLow);
        }
    },
    /** Maximum gun elevation (deg) = Highest angle the gun can be positioned */
    TE_Elevation_High {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(getEquipment(t, dev).gunElevationHigh);
        }
    },
    /** Hitpoints */
    TE_Hitpoints {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString(getEquipment(t, dev).hitpoints);
        }
    },
    /** The view range of the tank (m) */
    TE_ViewRange {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(getEquipment(t, dev).viewRange);
        }
    },
    /** Current weight (tons) */
    TE_Weight {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(getEquipment(t, dev).weight);
        }
    },
    /** Maximum load that can be carried (tons) */
    TE_WeightLimit {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(getEquipment(t, dev).weightLimit);
        }
    },
    //</editor-fold>
    
    // -- modules --
    
    //<editor-fold defaultstate="collapsed" desc="engine">
    /** all tanks that are compatible with this engine */
    ME_Compatibility {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null || e.compatibility == null) ? na : buildTankRefList(e.compatibility, t.name, LinkType.INTERNAL);
        }
        
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null || e.compatibility == null) ? na : buildTankRefList(e.compatibility, "engine", LinkType.INTERNAL);
        }
    },
    /** The cost of this engine (currency in separate field) */
    ME_Cost {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : Integer.toString(e.cost);
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : Integer.toString(e.cost);
        }
    },
    /** The currency this engine is paid for with */
    ME_Currency {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null || e.currency == null) ? na : e.currency.toString();
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null || e.currency == null) ? na : e.currency.toString();
        }
    },
    /** The name of this engine */
    ME_Name {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : e.name;
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : e.name;
        }
    },
    /** The Nation this engine belongs to */
    ME_Nation {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null || e.nation == null) ? na : e.nation.toString();
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null || e.nation == null) ? na : e.nation.toString();
        }
    },
    /** The tier / level of this engine */
    ME_Tier {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : Byte.toString(e.tier);
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : Byte.toString(e.tier);
        }
    },
    /** the weight of the module */
    ME_Weight {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : df.format(e.weight);
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : df.format(e.weight);
        }
    },
    /** Chance that the engine will catch fire when hit (%) */
    ME_Firechance {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : df.format(e.firechance);
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : df.format(e.firechance);
        }
    },
    /** Type of gas needed by the engine */
    ME_Gas {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null || e.gas == null) ? na : e.gas.toString();
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null || e.gas == null) ? na : e.gas.toString();
        }
    },
    /** Engine power (HP) */
    ME_Power {
        @Override
        public String get(Tank t, Development dev) {
            Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
            return (e == null) ? na : Integer.toString(e.power);
        }
        @Override
        public String get(Module m) {
            Engine e = (Engine) m;
            return (e == null) ? na : Integer.toString(e.power);
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="gun">
    /** all tanks that are compatible with this gun */
    MG_Compatibility {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null || g.compatibility == null) ? na : buildTankRefList(g.compatibility, t.name, LinkType.INTERNAL);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null || g.compatibility == null) ? na : buildTankRefList(g.compatibility, "gun", LinkType.INTERNAL);
        }
    },
    /** The cost of this gun (currency in separate field) */
    MG_Cost {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.cost);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.cost);
        }
    },
    /** The currency this gun is paid for with */
    MG_Currency {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null || g.currency == null) ? na : g.currency.toString();
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null || g.currency == null) ? na : g.currency.toString();
        }
    },
    /** The name of this gun */
    MG_Name {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : g.name;
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : g.name;
        }
    },
    /** The Nation this gun belongs to */
    MG_Nation {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null || g.nation == null) ? na : g.nation.toString();
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null || g.nation == null) ? na : g.nation.toString();
        }
    },
    /** The tier / level of this gun */
    MG_Tier {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Byte.toString(g.tier);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Byte.toString(g.tier);
        }
    },
    /** the weight of the module */
    MG_Weight {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : df.format(g.weight);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : df.format(g.weight);
        }
    },
    /** Accuracy (diameter in meters at 100m distance - minimum) */
    MG_Accuracy_Min {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.accuracyMin);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.accuracyMin);
        }
    },
    /** Accuracy (diameter in meters at 100m distance - maximum) */
    MG_Accuracy_Max {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.accuracyMax);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.accuracyMax);
        }
    },
    /** Aim Time (seconds - maximum) */
    MG_AimTime_Min {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.aimTimeMin);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.aimTimeMin);
        }
    },
    /** Aim Time (seconds - maximum) */
    MG_AimTime_Max {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.aimTimeMax);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.aimTimeMax);
        }
    },
    /** Ammo capacity (shells - minimum) */
    MG_AmmoCapacity_Min {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.ammoCapacityMin);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.ammoCapacityMin);
        }
    },
    /** Ammo capacity (shells - maximum) */
    MG_AmmoCapacity_Max {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.ammoCapacityMax);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.ammoCapacityMax);
        }
    },
    /** Gun Damage (AP = Armor Piercing) */
    MG_Dmg_AP {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : df.format(g.dmgAP);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : df.format(g.dmgAP);
        }
    },
    /** Gun Damage (APCR = Armor Piercing Composite Rigid) */
    MG_Dmg_APCR {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : df.format(g.dmgAPCR);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : df.format(g.dmgAPCR);
        }
    },
    /** Gun Damage (HE = High Explosive) */
    MG_Dmg_HE {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : df.format(g.dmgHE);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : df.format(g.dmgHE);
        }
    },
    /** Gun Damage (HEAT = High Explosive Anti-Tank) */
    MG_Dmg_HEAT {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : df.format(g.dmgHEAT);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : df.format(g.dmgHEAT);
        }
    },
    /** Fire Rate (Shots per Minute - minimum) */
    MG_FireRate_Min {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.fireRateMin);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.fireRateMin);
        }
    },
    /** Fire Rate (Shots per Minute - maximum) */
    MG_FireRate_Max {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : dfp.format(g.fireRateMax);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : dfp.format(g.fireRateMax);
        }
    },
    /** Shell Penetration in mm (AP = Armor Piercing) */
    MG_Penetration_AP {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.penAP);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.penAP);
        }
    },
    /** Shell Penetration in mm (APCR = Armor Piercing Composite Rigid) */
    MG_Penetration_APCR {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.penAPCR);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.penAPCR);
        }
    },
    /** Shell Penetration in mm (HE = High Explosive) */
    MG_Penetration_HE {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.penHE);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.penHE);
        }
    },
    /** Shell Penetration in mm (HEAT = High Explosive Anti-Tank) */
    MG_Penetration_HEAT {
        @Override
        public String get(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return (g == null) ? na : Integer.toString(g.penHEAT);
        }
        
        @Override
        public String get(Module m) {
            Gun g = (Gun) m;
            return (g == null) ? na : Integer.toString(g.penHEAT);
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="radio">
    /** all tanks that are compatible with this radio */
    MR_Compatibility {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null || r.compatibility == null) ? na : buildTankRefList(r.compatibility, t.name, LinkType.INTERNAL);
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null || r.compatibility == null) ? na : buildTankRefList(r.compatibility, "radio", LinkType.INTERNAL);
        }
    },
    /** The cost of this radio (currency in separate field) */
    MR_Cost {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null) ? na : Integer.toString(r.cost);
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null) ? na : Integer.toString(r.cost);
        }
    },
    /** The currency this radio is paid for with */
    MR_Currency {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null || r.currency == null) ? na : r.currency.toString();
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null || r.currency == null) ? na : r.currency.toString();
        }
    },
    /** The name of this radio */
    MR_Name {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null) ? na : r.name;
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null) ? na : r.name;
        }
    },
    /** The Nation this radio belongs to */
    MR_Nation {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null || r.nation == null) ? na : r.nation.toString();
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null || r.nation == null) ? na : r.nation.toString();
        }
    },
    /** The tier / level of this radio */
    MR_Tier {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null) ? na : Byte.toString(r.tier);
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null) ? na : Byte.toString(r.tier);
        }
    },
    /** the weight of the module */
    MR_Weight {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null) ? na : df.format(r.weight);
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null) ? na : df.format(r.weight);
        }
    },
    /** The radio transmission range (m) */
    MR_Range {
        @Override
        public String get(Tank t, Development dev) {
            Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
            return (r == null) ? na : Integer.toString(r.range);
        }
        
        @Override
        public String get(Module m) {
            Radio r = (Radio) m;
            return (r == null) ? na : Integer.toString(r.range);
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="suspension">
    /** all tanks that are compatible with this suspension */
    MS_Compatibility {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null || s.compatibility == null) ? na : buildTankRefList(s.compatibility, t.name, LinkType.INTERNAL);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null || s.compatibility == null) ? na : buildTankRefList(s.compatibility, "suspension", LinkType.INTERNAL);
        }
    },
    /** The cost of this suspension (currency in separate field) */
    MS_Cost {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : Integer.toString(s.cost);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : Integer.toString(s.cost);
        }
    },
    /** The currency this suspension is paid for with */
    MS_Currency {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null || s.currency == null) ? na : s.currency.toString();
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null || s.currency == null) ? na : s.currency.toString();
        }
    },
    /** The name of this suspension */
    MS_Name {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : s.name;
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : s.name;
        }
    },
    /** The Nation this suspension belongs to */
    MS_Nation {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null || s.nation == null) ? na : s.nation.toString();
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null || s.nation == null) ? na : s.nation.toString();
        }
    },
    /** The tier / level of this suspension */
    MS_Tier {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : Byte.toString(s.tier);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : Byte.toString(s.tier);
        }
    },
    /** the weight of the module */
    MS_Weight {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : df.format(s.weight);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : df.format(s.weight);
        }
    },
    /** Maximum load that can be carried (tons) */
    MS_Load {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : df.format(s.load);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : df.format(s.load);
        }
    },
    /** Rotation speed of the standing tank (deg/s) */
    MS_Traverse {
        @Override
        public String get(Tank t, Development dev) {
            Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
            return (s == null) ? na : Integer.toString(s.traverse);
        }
        
        @Override
        public String get(Module m) {
            Suspension s = (Suspension) m;
            return (s == null) ? na : Integer.toString(s.traverse);
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="turret">
    /** all tanks that are compatible with this turret */
    MT_Compatibility {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null || tu.compatibility == null) ? na : buildTankRefList(tu.compatibility, t.name, LinkType.INTERNAL);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null || tu.compatibility == null) ? na : buildTankRefList(tu.compatibility, "turret", LinkType.INTERNAL);
        }
    },
    /** The cost of this turret (currency in separate field) */
    MT_Cost {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : Integer.toString(tu.cost);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : Integer.toString(tu.cost);
        }
    },
    /** The currency this turret is paid for with */
    MT_Currency {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null || tu.currency == null) ? na : tu.currency.toString();
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null || tu.currency == null) ? na : tu.currency.toString();
        }
    },
    /** the name of this turret */
    MT_Name {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : tu.name;
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : tu.name;
        }
    },
    /** The Nation this turret belongs to */
    MT_Nation {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null || tu.nation == null) ? na : tu.nation.toString();
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null || tu.nation == null) ? na : tu.nation.toString();
        }
    },
    /** The tier / level of this turret */
    MT_Tier {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : Byte.toString(tu.tier);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : Byte.toString(tu.tier);
        }
    },
    /** the weight of the turret */
    MT_Weight {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.weight);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.weight);
        }
    },
    /** Turret armor (mm) - Front */
    MT_Armor_Front {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.armorFront);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.armorFront);
        }
    },
    /** Turret armor (mm) - Side */
    MT_Armor_Side {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.armorSide);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.armorSide);
        }
    },
    /** Turret armor (mm) - Rear */
    MT_Armor_Rear {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.armorRear);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.armorRear);
        }
    },
    /** Rotation speed of gun or turret (deg/s) */
    MT_Traverse {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.traverse);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.traverse);
        }
    },
    /** The view range of the tank (m) */
    MT_ViewRange {
        @Override
        public String get(Tank t, Development dev) {
            Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
            return (tu == null) ? na : df.format(tu.viewRange);
        }
        
        @Override
        public String get(Module m) {
            Turret tu = (Turret) m;
            return (tu == null) ? na : df.format(tu.viewRange);
        }
    },
    //</editor-fold>
    
    // -- module relations --
    
    //<editor-fold defaultstate="collapsed" desc="relations">
    /** List of engines compatible with this tank */
    REL_Engines {
        @Override
        public String get(Tank t, Development dev) {
            return buildModuleRefList(ModuleMap.getModules(t, Module.ModuleType.Engine), LinkType.INTERNAL);
        }
    },
    /** List of radios compatible with this tank */
    REL_Radios {
        @Override
        public String get(Tank t, Development dev) {
            return buildModuleRefList(ModuleMap.getModules(t, Module.ModuleType.Radio), LinkType.INTERNAL);
        }
    },
    /** List of guns compatible with this tank */
    REL_Guns {
        @Override
        public String get(Tank t, Development dev) {
            return buildModuleRefList(ModuleMap.getModules(t, Module.ModuleType.Gun), LinkType.INTERNAL);
        }
    },
    /** List of suspension compatible with this tank */
    REL_Suspensions {
        @Override
        public String get(Tank t, Development dev) {
            return buildModuleRefList(ModuleMap.getModules(t, Module.ModuleType.Suspension), LinkType.INTERNAL);
        }
    },
    /** List of turrets compatible with this tank */
    REL_Turrets {
        @Override
        public String get(Tank t, Development dev) {
            return buildModuleRefList(ModuleMap.getModules(t, Module.ModuleType.Turret), LinkType.INTERNAL);
        }
    },
    //</editor-fold>
    
    // -- specials --
    
    //<editor-fold defaultstate="collapsed" desc="depending fields">
    /** total gun arc (left and right) */
    DP_GunArc {
        @Override
        public String get(Tank t, Development dev) {
            return Integer.toString((int) calc(t, dev));
        }
        @Override
        public double calc(Tank t, Development dev) {
            return Math.abs(t.gunArcLeft) + t.gunArcRight;
        }
    },
    /** Total gun elevation (up and down) */
    DP_Elevation {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(calc(t, dev));
        }
        @Override
        public double calc(Tank t, Development dev) {
            Equipment eq = getEquipment(t, dev);
            return Math.abs(eq.gunElevationLow) + eq.gunElevationHigh;
        }
    },
    /** 
     * normalized ammo capacity, defined as the time you can shoot nonstop
     * (in seconds), before you run out of ammo
     */
    DP_Ammo_Normalized {
        @Override
        public String get(Tank t, Development dev) {
            return df.format(calc(t, dev));
        }
        @Override
        public double calc(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            double rpm = (dev == Development.Stock) ? g.fireRateMin : g.fireRateMax;
            double ammo = (dev == Development.Stock) ? g.ammoCapacityMin : g.ammoCapacityMax;
            return ammo / (rpm / 60.0);
        }
    },
    /** Damage per Second, using AP ammo */
    DP_DmgPS_AP {
        @Override
        public String get(Tank t, Development dev) {
            double dps = calc(t, dev);
            return (dps == -1) ? na : df.format(dps);
        }
        @Override
        public double calc(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return calculateDPS(g, dev, g.dmgAP);
        }
    },
    /** Damage per Second, using APCR ammo */
    DP_DmgPS_APCR {
        @Override
        public String get(Tank t, Development dev) {
            double dps = calc(t, dev);
            return (dps == -1) ? na : df.format(dps);
        }
        @Override
        public double calc(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return calculateDPS(g, dev, g.dmgAPCR);
        }
    },
    /** Damage per Second, using HE ammo */
    DP_DmgPS_HE {
        @Override
        public String get(Tank t, Development dev) {
            double dps = calc(t, dev);
            return (dps == -1) ? na : df.format(dps);
        }
        @Override
        public double calc(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return calculateDPS(g, dev, g.dmgHE);
        }
    },
    /** Damage per Second, using HEAT ammo */
    DP_DmgPS_HEAT {
        @Override
        public String get(Tank t, Development dev) {
            double dps = calc(t, dev);
            return (dps == -1) ? na : df.format(dps);
        }
        @Override
        public double calc(Tank t, Development dev) {
            Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
            return calculateDPS(g, dev, g.dmgHEAT);
        }
    },
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="ratings">
    /** the hitpoints rating */
    RT_Hitpoints {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.hitpoints, tr.eq.hitpoints, TE_Hitpoints.best);
        }
    },
    /** the weight rating */
    RT_Weight {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.weight, tr.eq.weight, TE_Weight.best);
        }
    },
    /** the engine firechance rating */
    RT_Firechance {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.firechance, tr.e.firechance, ME_Firechance.best);
        }
    },
    /** the turret rotation rating */
    RT_TraverseTurret {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.traverseTurret, tr.tu.traverse, MT_Traverse.best);
        }
    },
    /** the tank rotation rating */
    RT_TraverseSuspension {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.traverseSuspension, tr.s.traverse, MS_Traverse.best);
        }
    },
    /** the accuracy rating */
    RT_Accuracy {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLdp(tr.gunAccuracy, (tr.eq.development == Development.Stock) ? tr.g.accuracyMax : tr.g.accuracyMin, MG_Accuracy_Min.best);
        }
    },
    /** the aim time rating */
    RT_AimTime {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLdp(tr.gunAimTime, (tr.eq.development == Development.Stock) ? tr.g.aimTimeMax : tr.g.aimTimeMin, MG_AimTime_Min.best);
        }
    },
    /** the ammo capacity rating rating, using normalized ammo (continuous fire time) */
    RT_AmmoCapacity {
        @Override
        public String get(TankRating tr) {
            int ammo = (tr.eq.development == Development.Stock) ? tr.g.ammoCapacityMin : tr.g.ammoCapacityMax;
            double rate = (tr.eq.development == Development.Stock) ? tr.g.fireRateMin : tr.g.fireRateMax;
            long s = Math.round(ammo / (rate / 60.0));
            String duration = String.format("%02d:%02d", (s/60), (s%60));
            
            String tooltip = String.format("Ammo: %s rounds<br/>Rate: %s rpm<br/>=> Constant Fire: %s<br/>Best: %s s",
                    ammo, rate, duration, Math.round(DP_Ammo_Normalized.best));
            return ratingHTML(percent(tr.gunAmmo), tooltip);
        }
    },
    /** the top speed rating */
    RT_TopSpeed {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.speed, tr.t.speed, T_TopSpeed.best);
        }
    },
    /** the engine power rating */
    RT_EnginePower {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.enginePower, tr.e.power, ME_Power.best);
        }
    },
    /** the radio transmission rate rating */
    RT_RadioRange {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.radioRange, tr.r.range, MR_Range.best);
        }
    },
    /** the view range rating */
    RT_ViewRange {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.viewRange, tr.eq.viewRange, TE_ViewRange.best);
        }
    },
    /** The hull armor rating (cumulation of front, side and rear) */
    RT_HullArmor {
        @Override
        public String get(TankRating tr) {
            String tooltip = String.format("F: %s mm (best: %s)<br/>S: %s mm (best: %s)<br/>R: %s mm (best: %s)",
                    tr.t.hullFront, T_Hull_Front.best, tr.t.hullSide, T_Hull_Side.best,
                    tr.t.hullRear, T_Hull_Rear.best);
            return ratingHTML(percent(tr.hullArmor), tooltip);
        }
    },
    /** The turret armor rating (cumulation of front, side and rear) */
    RT_TurretArmor {
        @Override
        public String get(TankRating tr) {
            String tooltip = String.format("F: %s mm (best: %s)<br/>S: %s mm (best: %s)<br/>R: %s mm (best: %s)",
                    tr.tu.armorFront, MT_Armor_Front.best, tr.tu.armorSide, MT_Armor_Side.best,
                    tr.tu.armorRear, MT_Armor_Rear.best);
            return ratingHTML(percent(tr.turretArmor), tooltip);
        }
    },
    /** The gun arc (left/right) rating */
    RT_GunArc {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.gunArc, Field.DP_GunArc.calc(tr.t, tr.eq.development), DP_GunArc.best);
        }
    },
    /** The gun elevation (low/high) rating */
    RT_GunElevation {
        @Override
        public String get(TankRating tr) {
            return ratingHTMLsp(tr.gunElevation, Field.DP_Elevation.calc(tr.t, tr.eq.development), DP_Elevation.best);
        }
    },
    /** The damage rating (cumulation of all damage types) */
    RT_Damage {
        @Override
        public String get(TankRating tr) {
            String rate = (tr.eq.development == Development.Stock) ? df.format(tr.g.fireRateMin) : df.format(tr.g.fireRateMax);
            String row = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
            
            StringBuilder sb = new StringBuilder(200);
            sb.append("<table><tr><th>AmmoType</th><th>dmg/shot</th><th>shots/min</th><th>dmg/s</th><th>dmg/s (best)</th></tr>");
            sb.append(String.format(row, "AP", tr.g.dmgAP, rate, df.format(Field.DP_DmgPS_AP.calc(tr.t, tr.eq.development)),
                    df.format(Field.DP_DmgPS_AP.best)));
            sb.append(String.format(row, "APCR", tr.g.dmgAPCR, rate, df.format(Field.DP_DmgPS_APCR.calc(tr.t, tr.eq.development)),
                    df.format(Field.DP_DmgPS_APCR.best)));
            sb.append(String.format(row, "HE", tr.g.dmgHE,   rate, df.format(Field.DP_DmgPS_HE.calc(tr.t, tr.eq.development)),
                    df.format(Field.DP_DmgPS_HE.best)));
            sb.append(String.format(row, "HEAT", tr.g.dmgHEAT, rate, df.format(Field.DP_DmgPS_HEAT.calc(tr.t, tr.eq.development)),
                    df.format(Field.DP_DmgPS_HEAT.best)));
            sb.append("</table>");
            
            return ratingHTML(percent(tr.damage), sb.toString());
        }
    },
    /** The penetration rating (cumulation of all ammo types) */
    RT_Penetration {
        @Override
        public String get(TankRating tr) {
            String tooltip = String.format("AP: %s mm (best: %s)<br/>APCR: %s mm (best: %s)<br/>"
                    + "HE: %s mm (best: %s)<br/>HEAT: %s mm (best: %s)",
                    tr.g.penAP, MG_Penetration_AP.best, tr.g.penAPCR, MG_Penetration_APCR.best,
                    tr.g.penHE, MG_Penetration_HE.best, tr.g.penHEAT, MG_Penetration_HEAT.best);
            return ratingHTML(percent(tr.penetration), tooltip);
        }
    },
    
    /** The defensive rating for this tank */
    RT2_Defense {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingDefense);
        }
    },
    /** The offensive rating for this tank */
    RT2_Attack {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingAttack);
        }
    },
    /** The mobility rating for this tank */
    RT2_Mobility {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingMobility);
        }
    },
    /** The recon rating for this tank */
    RT2_Recon {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingRecon);
        }
    },
    /** The cost/benefit-rating for this tank */
    RT2_CostBenefit {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingCostBenefit);
        }
    },
    /** The overall rating for this tank */
    RT2_OverallRating {
        @Override
        public String get(TankRating tr) {
            return percent(tr.ratingOverall);
        }
    },
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="special fields">
    /** Just an empty field, no value associated */
    EMPTY;
    //</editor-fold>
    
    //</editor-fold>
    
    
    //<editor-fold desc="shared enum members">
    /** best value for each field (compared to the other instances) */
    public double best = -1;
    
    /**
     * Calculates the value of an advanced field (prefix DP_)
     * Returns -1 on all other fields
     * @param t the Tank to calculate the value for
     * @param dev the development (needed if field depends on it)
     * @return the value of this field (or -1, if not applicable)
     */
    public double calc(Tank t, Development dev) {
        return -1;
    }
    
    /**
     * Gets the value of this field as String
     * @param t the Tank to calculate the value for
     * @param dev the development (needed if field depends on it)
     * @return String representation of this field
     */
    public String get(Tank t, Development dev) {
        return "";
    }
    
    /**
     * Gets the value of this field as String.
     * Does only work for Modules, returns empty String for anything else!
     * @param m the module that is associated with this value
     * @return the value for the given module, or an empty string if wrong
     * module is given or the requested value is not part of a module
     */
    public String get(Module m) {
        return "";
    }
    
    /**
     * Gets the value of this rating (includes HTML)
     * @param tr the tank rating
     * @return String representation of this rating
     */
    public String get(TankRating tr) {
        return get(tr.t, tr.eq.development);
    }
    //</editor-fold>

    //<editor-fold desc="methods">
    
    /** Decimal formatter, one decimal digit */
    protected static final DecimalFormat df = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    /** More precise decimal formatter, two decimal digits */
    protected static final DecimalFormat dfp = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    
    /** this string is entered for invalid field values */
    public static final String na = "n/a";
    
    /**
     * Returns the equipment for this tank that represents stock or top.
     * @param t the tank
     * @param dev stock or top
     * @return equipment of this tank for stock or top
     */
    protected static Equipment getEquipment(Tank t, Development dev) {
        switch (dev) {
            case Stock:
                return t.equipmentStock;
            case Top:
                return t.equipmentTop;
            default:
                return null;
        }
    }
    
    // ---------- reference lists ----------
    
    /**
     * Creates a String of the tank reference list, using html &lt;a&gt; to link
     * stuff...
     * @param refs the references to link to
     * @param source the name of the source tank, only needed for error logging
     * @param link the type of link to create
     * @return HTML String representation of the given list
     */
    protected static String buildTankRefList(List<TankRef> refs, String source, LinkType link) {
        StringBuilder sb = new StringBuilder();
        for (TankRef tr : refs) {
            if (tr.ref != null) {
                switch (link) {
                    case INTERNAL:
                        sb.append(String.format("<a href=\"#%s\">%s</a>", tr.ref.id, tr.ref.name));
                        break;
                    case EXTERNAL:
                        try {
                            sb.append(String.format("<a href=\"%s\">%s</a>", Crawler.buildWikiLink(tr.ref.name).toString(), tr.ref.name));
                        } catch (MalformedURLException ex) {
                            Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
                            sb.append(tr.ref.name);
                        }
                        break;
                    case NONE:
                        sb.append(tr.ref.name);
                        break;
                    default:
                        sb.append(tr.ref.id);
                        break;
                }
                
                sb.append(", ");
            } else {
                Logger.getLogger(Transformer.class.getName()).log(Level.WARNING, String.format("Invalid child reference for Tank %s", source));
            }
        }
        
        // delete last comma and return
        if (sb.length() > 3) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
    
    /**
     * Creates a String of the module list, using html &lt;a&gt; to link
     * stuff...
     * @param modules the modules to link to
     * @param link the type of link to create
     * @return HTML String representation of the given list
     */
    protected static String buildModuleRefList(List modules, LinkType link) {
        StringBuilder sb = new StringBuilder();
        
        for (Object o : modules) {
            Module ref = (Module) o;
            switch (link) {
                case INTERNAL:
                    sb.append(String.format("<a href=\"#%s\">%s</a>", ref.name, ref.name));
                    break;
                case EXTERNAL:
                    // not supported, go on (no break)
                case NONE:
                    sb.append(ref.name);
                    break;
            }
            sb.append(", ");
        }
        
        // delete last comma and return
        if (sb.length() > 3) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
    
    // ---------- min max fields ----------
    
    /**
     * Calculates the minimum and maximum values for all relevant fields, using
     * the data from the specified TankDB
     * @param db the database to retrieve values from
     */
    public static void calculateMinMaxFields(TanksDB db) {
        for (Tank t : db.tanks) {
            for (Development dev : Development.values()) {
                calculateMinMaxFields(t, dev);
            }
        }
    }
    
    /**
     * Calculates the minimum and maximum values for all relevant fields, using
     * the data from the specified TankDB, but only for tanks of the specified
     * TankType
     * @param db the database to retrieve values from
     * @param type the TankType to calculate values for
     */
    public static void calculateMinMaxFields(TanksDB db, TankType type) {
        // reset old values
        resetBestValues();
        
        // calculate new ones for this type only
        for (Tank t : db.tanks) {
            // use only tanks of specified type for minmax rating!
            if(t.type == type) {
                for (Development dev : Development.values()) {
                    calculateMinMaxFields(t, dev);
                }
            }
        }
    }
    
    /**
     * Resets the min and max values, before new ones are generated.
     */
    protected static void resetBestValues() {
        // higher is better (default)
        for (Field f : Field.values()) {
            f.best = -1;
        }
        
        // lower is better
        Field.MG_AimTime_Min.best = Double.POSITIVE_INFINITY;
        Field.MG_AimTime_Max.best = Double.POSITIVE_INFINITY;
        Field.MG_Accuracy_Min.best = Double.POSITIVE_INFINITY;
        Field.MG_Accuracy_Max.best = Double.POSITIVE_INFINITY;
        Field.ME_Firechance.best = Double.POSITIVE_INFINITY;
    }
    
    /**
     * Calculates the minimum and maximum values for all relevant fields for
     * the specified tank. 
     * @param t the tank to calculate values for
     * @param dev the development of this tank (to retrieve correct modules)
     */
    protected static void calculateMinMaxFields(Tank t, Development dev) {
        
        Equipment eq = dev.getEquip(t, dev);
        
        Engine e = ModuleMap.getModuleByDev(Engine.class, t, dev);
        Gun g = ModuleMap.getModuleByDev(Gun.class, t, dev);
        Radio r = ModuleMap.getModuleByDev(Radio.class, t, dev);
        Suspension s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
        Turret tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
        
        
        // base properties
        updateMax(Field.TE_Hitpoints, eq.hitpoints);
        updateMax(Field.TE_Weight , eq.weight);
        updateMin(Field.ME_Firechance , e.firechance);
        updateMax(Field.MT_Traverse , tu.traverse);
        updateMax(Field.MS_Traverse , s.traverse);
        updateMax(Field.T_TopSpeed , t.speed);
        updateMax(Field.ME_Power , e.power);
        updateMax(Field.MR_Range , r.range);
        updateMax(Field.TE_ViewRange , eq.viewRange);
        
        updateMax(Field.T_Hull_Front , t.hullFront);
        updateMax(Field.T_Hull_Side , t.hullRear);
        updateMax(Field.T_Hull_Rear , t.hullSide);
        updateMax(Field.MT_Armor_Front , tu.armorFront);
        updateMax(Field.MT_Armor_Side , tu.armorSide);
        updateMax(Field.MT_Armor_Rear , tu.armorRear);
        updateMax(Field.MG_Penetration_AP , g.penAP);
        updateMax(Field.MG_Penetration_APCR , g.penAPCR);
        updateMax(Field.MG_Penetration_HE , g.penHE);
        updateMax(Field.MG_Penetration_HEAT , g.penHEAT);
        
        // special calculations
        updateMax(Field.DP_GunArc , t, dev);
        updateMax(Field.DP_Elevation, t, dev);
        updateMax(Field.DP_Ammo_Normalized, t, dev);
        updateMax(Field.DP_DmgPS_AP , t, dev);
        updateMax(Field.DP_DmgPS_APCR , t, dev);
        updateMax(Field.DP_DmgPS_HE , t, dev);
        updateMax(Field.DP_DmgPS_HEAT , t, dev);
        
        // depending on dev --> always use best values, will be applied anyways
        switch (eq.development) {
            case Stock:
                updateMin(Field.MG_Accuracy_Max, g.accuracyMin);
                updateMin(Field.MG_AimTime_Max, g.aimTimeMin);
                updateMax(Field.MG_AmmoCapacity_Min, g.ammoCapacityMax);
                break;
            case Top:
                updateMin(Field.MG_Accuracy_Min, g.accuracyMin);
                updateMin(Field.MG_AimTime_Min, g.aimTimeMin);
                updateMax(Field.MG_AmmoCapacity_Max, g.ammoCapacityMax);
                break;
        }
        
    }
    
    /**
     * Updates the maximum value of a given field with the specified value,
     * if it really is larger than the current max value
     * @param f the field to update
     * @param value the value to set as max, if larger
     */
    protected static void updateMax(Field f, double value) {
        if (value > 0.0) {
            if (value > f.best) {
                f.best = value;
            }
        } else if (value < 0.0) {
            System.err.println("Field " + f.toString() + " is associated with illegal value " + value);
        }
    }
    
    /**
     * Updates the maximum value of a given field with the value from the
     * specified tank, if it really is larger than the current max value
     * @param f the field to update
     * @param t the tank
     * @param dev stock or top equipment?
     */
    protected static void updateMax(Field f, Tank t, Development dev) {
        double value = f.calc(t, dev);
        updateMax(f, value);
    }
    
    /**
     * Updates the minimum value of a given field with the specified value,
     * if it really is smaller than the current min value
     * @param f the field to update
     * @param value the value to set as min, if smaller
     */
    protected static void updateMin(Field f, double value) {
        if (value > 0.0) {
            if (value < f.best) {
                f.best = value;
            }
        } else if (value < 0.0) {
            System.err.println("Field " + f.toString() + " is associated with illegal value " + value);
        }
    }
    
    /**
     * Updates the minimum value of a given field with the value from the
     * specified tank, if it really is smaller than the current max value
     * @param f the field to update
     * @param t the tank
     * @param dev stock or top equipment?
     */
    protected static void updateMin(Field f, Tank t, Development dev) {
        double value = f.calc(t, dev);
        updateMin(f, value);
    }
    
    // ---------- other helpers ----------
    
    /**
     * Converts a double into a String representing it's percent value
     * @param d the double to convert
     * @return percent (e.g. 0.52 -> 52.0 %)
     */
    public static String percent(double d) {
        return (d == -1) ? na : df.format(d * 100) + " %";
    }
    
    /**
     * Creates a html string containing the specified rating and a custom
     * comment inside an abbr-tag (so it is visible on mouseover)
     * @param rating the rating of a field
     * @param comment the comment for this field
     * @return rating and hidden comment as html string
     */
    protected static String ratingHTML(String rating, String comment) {
        return String.format("<abbr title=\"%s\">%s</abbr>", comment, rating);
    }
    
    /**
     * Special version of ratingHTML:
     * Writes the given rating (double) as percent string and surrounds it with
     * an abbr-tag that contains the absolute value as well as the given best
     * value.
     * sp: the hidden values will be printed in "single precision" (one decimal 
     * place)
     * @param rating the rating of this field
     * @param value the actual value of this field
     * @param best the best value of this field
     * @return rating and hidden comment as html string
     */
    protected static String ratingHTMLsp(double rating, double value, double best) {
        String tooltip = String.format("Value: %s<br/>Best: %s", df.format(value), df.format(best));
        return ratingHTML(percent(rating), tooltip);
    }
    
    /**
     * Special version of ratingHTML:
     * Writes the given rating (double) as percent string and surrounds it with
     * an abbr-tag that contains the absolute value as well as the given best
     * value.
     * dp: the hidden values will be printed in "double precision" (two decimal 
     * places)
     * @param rating the rating of this field
     * @param value the actual value of this field
     * @param best the best value of this field
     * @return rating and hidden comment as html string
     */
    protected static String ratingHTMLdp(double rating, double value, double best) {
        String tooltip = String.format("Value: %s<br/>Best: %s", dfp.format(value), dfp.format(best));
        return ratingHTML(percent(rating), tooltip);
    }
    
    /**
     * Calculates the Damage per Second for a specified gun. The development is
     * used to determine, if min or max fire rate is used and the damage value
     * is needed because there are different ammo types...
     * @param g the gun this value is calculated for
     * @param dev the development of the tank where the gun is mounted
     * @param damage the actual damage of the gun (depends on ammo type1)
     * @return dmg/s
     */
    protected static double calculateDPS(Gun g, Development dev, double damage) {
        if (g == null) {
            return -1;
        } else {
            double rate = (dev == Development.Stock) ? g.fireRateMin : g.fireRateMax;
            return damage * rate / 60.0;
        }
    }
    
    /**
     * Different types of (web)links
     */
    enum LinkType {
        /** internal html link (anchor) */
        INTERNAL,
        /** external html link (hyperref) */
        EXTERNAL,
        /** no link at all... */
        NONE;
    }
    //</editor-fold>
}