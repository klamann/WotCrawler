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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.nx42.wotcrawler.db.TanksDB;
import de.nx42.wotcrawler.db.module.Engine;
import de.nx42.wotcrawler.db.module.Gun;
import de.nx42.wotcrawler.db.module.Module;
import de.nx42.wotcrawler.db.module.Radio;
import de.nx42.wotcrawler.db.module.Suspension;
import de.nx42.wotcrawler.db.module.Turret;
import de.nx42.wotcrawler.db.tank.Equipment;
import de.nx42.wotcrawler.db.tank.Tank;
import de.nx42.wotcrawler.db.tank.Tank.TankType;

/**
 * Evaluates a TankDB, detects common errors.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Evaluator {
    
    /** The database to evaluate */
    protected TanksDB db;
    /** The ModuleMap, mapping from each tank to a list of compatible modules */
    protected ModuleMap mm;
    
    /** all tanks in this set have no engine associated */
    protected Set<Tank> noEngine = new HashSet<Tank>();
    /** all tanks in this set have no gun associated */
    protected Set<Tank> noGun = new HashSet<Tank>();
    /** all tanks in this set have no radio associated */
    protected Set<Tank> noRadio = new HashSet<Tank>();
    /** all tanks in this set have no turret associated (except for td and spg) */
    protected Set<Tank> noTurret = new HashSet<Tank>();
    /** all tanks in this set have no suspension associated */
    protected Set<Tank> noSuspension = new HashSet<Tank>();
    
    /** List of other reports for each tank */
    Map<Tank,List<String>> tankReports = new HashMap<Tank,List<String>>();
    /** List of other reports for each module */
    Map<Module,List<String>> moduleReports = new HashMap<Module,List<String>>();
    
    /** has a report already been built? */
    boolean built = false;
    
    /**
     * Initializes the Evaluator
     * @param db the TankDB to evaluate
     * @param map the ModuleMap to work with
     */
    public Evaluator(TanksDB db, ModuleMap map) {
        this.db = db;
        this.mm = map;
    }
    
    /**
     * Initializes the Evaluator and generates a ModuleMap on the fly
     * @param db the TankDB to evaluate
     */
    public Evaluator(TanksDB db) {
        this.db = db;
        this.mm = ModuleMap.build(db);
    }
    
    
    // ------------ public accessors ------------
    
    
    /**
     * generates a report and stores it in the internal object structure
     */
    public void buildReport() {
        checkAllFields();
        this.built = true;
    }
    
    
    /**
     * Writes a previously generated report in plain text (with some basic
     * markdown) and returns it as String
     * @return the current report, as String
     */
    public String writeReport() {
        
        // build the report, if it was not created yet!
        if(!this.built) {
            buildReport();
        }
        
        // write the report
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n------------------\n");
        sb.append("TankDB Evaluation Report started. All Errors above this line \nare probably not covered in this report.\n");
        
        // Test 1
        sb.append("\n### Test 1\n\nMissing Tank -> Module Relations: ");
        int relationErrors = noEngine.size() + noGun.size() + noRadio.size() + noSuspension.size() + noTurret.size();
        if(relationErrors == 0) {
            sb.append("great, all tanks have at least one of each module type!\n");
        } else {
            sb.append(relationErrors);
            sb.append(" faulty relations\n\n");
            
            missingModules(sb, noEngine, "Engines");
            missingModules(sb, noGun, "Guns");
            missingModules(sb, noRadio, "Radios");
            missingModules(sb, noSuspension, "Suspensions");
            missingModules(sb, noTurret, "Turrets (excluding TDs and SPGs)");
        }
        
        // Test 2
        sb.append("\n### Test 2\n\nInvalid Tank Attributes: ");
        if(tankReports.isEmpty()) {
            sb.append("great, the attributes of every tank seem to be valid!\n");
        } else {
            sb.append("Each of these tanks has some broken fields:\n\n");
            for (Tank t : tankReports.keySet()) {
                sb.append(String.format("* %s (%s)\n", t.name, t.id));
                for (String report : tankReports.get(t)) {
                    sb.append(String.format("  - %s\n", report));
                }
            }
        }
        
        // Test 3
        sb.append("\n### Test 3\n\nInvalid Module Attributes: ");
        if(moduleReports.isEmpty()) {
            sb.append("great, the attributes of every module seem to be valid!\n");
        } else {
            sb.append("Each of these modules has some broken fields:\n\n");
            for (Module m : moduleReports.keySet()) {
                sb.append(String.format("* %s\n", m.name));
                for (String report : moduleReports.get(m)) {
                    sb.append(String.format("  - %s\n", report));
                }
            }
        }
        
        sb.append("\nReport finished\n------------------\n");
        
        return sb.toString();
    }
    
    // ------------ output ------------
    
    /**
     * Part of the text report system. writes down missing modules for each tank
     * @param sb the stringbuilder where the current report is stored in
     * @param tanks this set contains all tanks that have a missing module
     * @param module this is the module that the tanks in the set are missing
     */
    protected void missingModules(StringBuilder sb, Set<Tank> tanks, String module) {
        sb.append("* ");
        sb.append(module);
        
        if(tanks.isEmpty()) {
            sb.append(": great, every tank has at least one of these!\n");
        } else {
            sb.append("\n");
            for (Tank t : tanks) {
                sb.append(String.format("  - %s (%s)\n", t.name, t.id));
            }
        }
    }
    
    
    // ------------ evaluation ------------
    
    
    /**
     * Runs all tests, checks all fields for existence and validity
     */
    protected void checkAllFields() {
        for (Tank t : db.tanks) {
            // check tank fields
            checkTankFields(t);
            
            // check, if compatible modules exist
            if (ModuleMap.engine.get(t).isEmpty()) {
                noEngine.add(t);
            }
            if (ModuleMap.gun.get(t).isEmpty()) {
                noGun.add(t);
            }
            if (ModuleMap.radio.get(t).isEmpty()) {
                noRadio.add(t);
            }
            if (ModuleMap.suspension.get(t).isEmpty()) {
                noSuspension.add(t);
            }
            if (ModuleMap.turret.get(t).isEmpty()) {
                // these tank types usually have no turret, so no reference here...
                if(t.type != TankType.TankDestroyer && t.type != TankType.SelfPropelledGun) {
                    noTurret.add(t);
                }
            }
        }
        
        for (Engine e : db.modules.engines) {
            checkModEngineFields(e);
        }
        for (Gun g : db.modules.guns) {
            checkModGunFields(g);
        }
        for (Radio r : db.modules.radios) {
            checkModRadioFields(r);
        }
        for (Turret t : db.modules.turrets) {
            checkModTurretFields(t);
        }
        for (Suspension s : db.modules.suspensions) {
            checkModSuspFields(s);
        }
    }
    
    /**
     * Checks all fields of a single tank
     * @param t the tank to check
     */
    protected void checkTankFields(Tank t) {
        
        if(t.battleTierMax < 1 || t.battleTierMax > 12) {
            report(t, "battleTierMax: " + t.battleTierMax);
        }
        if(t.battleTierMin < 1 || t.battleTierMin > 12) {
            report(t, "battleTierMin: " + t.battleTierMin);
        }
        if(t.tier > 1 && t.cost < 1)
            report(t, "cost: " + t.cost);
        if(t.crewMembers < 1 || t.crewMembers > 10) {
            report(t, "crewMembers: " + t.crewMembers);
        }
        if(t.currency == null) {
            report(t, "currency is null");
        }
        if(t.gunArcRight < 1 || t.gunArcRight > 360) {
            report(t, "gunArcHigh: " + t.gunArcRight);
        }
        if(t.gunArcLeft < -360 || t.gunArcLeft > 0) {
            report(t, "gunArcLow: " + t.gunArcLeft);
        }
        if(t.hullFront < 5) {
            report(t, "hullFront: " + t.hullFront);
        }
        if(t.hullSide < 5) {
            report(t, "hullSide: " + t.hullSide);
        }
        if(t.hullRear < 5) {
            report(t, "hullRear: " + t.hullRear);
        }
        if(t.id.length() < 2) {
            report(t, "id: " + t.id);
        }
        if(t.name.length() < 2) {
            report(t, "name: " + t.name);
        }
        if(t.nation == null) {
            report(t, "nation is null");
        }
        if(t.tier < 1 || t.tier > 12) {
            report(t, "tier: " + t.tier);
        }
        if(t.speed < 5 || t.speed > 120) {
            report(t, "topSpeed: " + t.speed);
        }
        if(t.type == null) {
            report(t, "type is null");
        }
        
        checkTankEquipment(t, t.equipmentStock);
        checkTankEquipment(t, t.equipmentTop);
        
    }
    
    /**
     * Checks all equipment of a single tank
     * @param t the tank to check
     * @param eq the equipment to check
     */
    protected void checkTankEquipment(Tank t, Equipment eq) {
        
        if(eq.development == null) {
            report(t, "development is null");
        }
        if(eq.gunElevationHigh < 1 || eq.gunElevationHigh > 90) {
            report(t, "elevationHigh: " + eq.gunElevationHigh);
        }
        if(eq.gunElevationLow < -40 || eq.gunElevationLow > 50) {
            report(t, "elevationLow: " + eq.gunElevationLow);
        }
        if(eq.hitpoints < 10 || eq.hitpoints > 10000) {
            report(t, "hitpoints: " + eq.hitpoints);
        }
        if(eq.viewRange < 50 || eq.viewRange > 2000) {
            report(t, "viewRange: " + eq.viewRange);
        }
        if(eq.weight < 1) {
            report(t, "weight: " + eq.weight);
        }
        if(eq.weightLimit < 1) {
            report(t, "weightLimit: " + eq.weightLimit);
        }
        
    }
    
    /**
     * Checks all fields of a single engine
     * @param e the engine to check
     */
    protected void checkModEngineFields(Engine e) {
        String engine = String.format("Engine %s: ", e.name);
        
        if(e.compatibility.isEmpty()) {
            report(e, engine + "has no compatible tanks");
        }
        if(e.cost < 0) {    // stock modules are for free!
            report(e, engine + "cost: " + e.cost);
        }
        if(e.currency == null) {
            report(e, engine + "currency is null");
        }
        if(e.name.length() < 2) {
            report(e, engine + "name: " + e.name);
        }
        if(e.nation == null) {
            report(e, engine + "nation is null");
        }
        if(e.tier < 1 || e.tier > 12) {
            report(e, engine + "tier: " + e.tier);
        }
        if(e.weight < 1) {
            report(e, engine + "weight: " + e.weight);
        }
        
        if(e.firechance < 0.01 || e.firechance > 100) {
            report(e, engine + "firechance: " + e.firechance);
        }
        if(e.gas == null) {
            report(e, engine + "gas is null");
        }
        if(e.power < 5 || e.power > 10000) {
            report(e, engine + "power: " + e.power);
        }
        
    }
    
    /**
     * Checks all fields of a single gun
     * @param g the gun to check
     */
    protected void checkModGunFields(Gun g) {
        String gun = String.format("Gun %s: ", g.name);
        
        if(g.compatibility.isEmpty()) {
            report(g, gun + "has no compatible tanks");
        }
        if(g.cost < 0) {    // stock modules are for free!
            report(g, gun + "cost: " + g.cost);
        }
        if(g.currency == null) {
            report(g, gun + "currency is null");
        }
        if(g.name.length() < 2) {
            report(g, gun + "name: " + g.name);
        }
        if(g.nation == null) {
            report(g, gun + "nation is null");
        }
        if(g.tier < 1 || g.tier > 12) {
            report(g, gun + "tier: " + g.tier);
        }
        if(g.weight < 1) {
            report(g, gun + "weight: " + g.weight);
        }
        
        if(g.accuracyMax < 0.1) {
            report(g, gun + "accuracyMax: " + g.accuracyMax);
        }
        if(g.accuracyMin < 0.1) {
            report(g, gun + "accuracyMin: " + g.accuracyMin);
        }
        if(g.aimTimeMax < 0.5) {
            report(g, gun + "aimTimeMax: " + g.aimTimeMax);
        }
        if(g.aimTimeMin < 0.5) {
            report(g, gun + "aimTimeMin: " + g.aimTimeMin);
        }
        if(g.ammoCapacityMax < 1) {
            report(g, gun + "ammoCapacityMax: " + g.ammoCapacityMax);
        }
        if(g.ammoCapacityMin < 1) {
            report(g, gun + "ammoCapacityMin: " + g.ammoCapacityMin);
        }
        
        if(g.fireRateMax < 0.5) {
            report(g, gun + "fireRateMax: " + g.fireRateMax);
        }
        if(g.fireRateMin < 0.5) {
            report(g, gun + "fireRateMin: " + g.fireRateMin);
        }
        // skip damage and penetration (can be 0)
        
    }
    
    /**
     * Checks all fields of a single radio
     * @param g the radio to check
     */
    protected void checkModRadioFields(Radio r) {
        String radio = String.format("Radio %s: ", r.name);
        
        if(r.compatibility.isEmpty()) {
            report(r, radio + "has no compatible tanks");
        }
        if(r.cost < 0) {    // stock modules are for free!
            report(r, radio + "cost: " + r.cost);
        }
        if(r.currency == null) {
            report(r, radio + "currency is null");
        }
        if(r.name.length() < 2) {
            report(r, radio + "name: " + r.name);
        }
        if(r.nation == null) {
            report(r, radio + "nation is null");
        }
        if(r.tier < 1 || r.tier > 12) {
            report(r, radio + "tier: " + r.tier);
        }
        if(r.weight < 1) {
            report(r, radio + "weight: " + r.weight);
        }
        
        if(r.range < 50 || r.range > 10000) {
            report(r, radio + "range: " + r.range);
        }
        
    }
    
    /**
     * Checks all fields of a single suspension
     * @param s the suspension to check
     */
    protected void checkModSuspFields(Suspension s) {
        String susp = String.format("Engine %s: ", s.name);
        
        if(s.compatibility.isEmpty()) {
            report(s, susp + "has no compatible tanks");
        }
        if(s.cost < 0) {    // stock modules are for free!
            report(s, susp + "cost: " + s.cost);
        }
        if(s.currency == null) {
            report(s, susp + "currency is null");
        }
        if(s.name.length() < 2) {
            report(s, susp + "name: " + s.name);
        }
        if(s.nation == null) {
            report(s, susp + "nation is null");
        }
        if(s.tier < 1 || s.tier > 12) {
            report(s, susp + "tier: " + s.tier);
        }
        if(s.weight < 1) {
            report(s, susp + "weight: " + s.weight);
        }
        
        if(s.load < 1) {
            report(s, susp + "load: " + s.load);
        }
        if(s.traverse < 1) {
            report(s, susp + "traverse: " + s.traverse);
        }
        
    }
    
    /**
     * Checks all fields of a single turret
     * @param t the turret to check
     */
    protected void checkModTurretFields(Turret t) {
        String turret = String.format("Engine %s: ", t.name);
        
        if(t.compatibility.isEmpty()) {
            report(t, turret + "has no compatible tanks");
        }
        if(t.cost < 0) {    // stock modules are for free!
            report(t, turret + "cost: " + t.cost);
        }
        if(t.currency == null) {
            report(t, turret + "currency is null");
        }
        if(t.name.length() < 2) {
            report(t, turret + "name: " + t.name);
        }
        if(t.nation == null) {
            report(t, turret + "nation is null");
        }
        if(t.tier < 1 || t.tier > 12) {
            report(t, turret + "tier: " + t.tier);
        }
        if(t.weight < 1) {
            report(t, turret + "weight: " + t.weight);
        }
        
        if(t.armorFront < 1) {
            report(t, turret + "armorFront: " + t.armorFront);
        }
        if(t.armorSide < 1) {
            report(t, turret + "armorSide: " + t.armorSide);
        }
        if(t.armorRear < 1) {
            report(t, turret + "armorRear: " + t.armorRear);
        }
        if(t.traverse < 1) {
            report(t, turret + "traverse: " + t.traverse);
        }
        if(t.viewRange < 50 || t.viewRange > 10000) {
            report(t, turret + "viewrange: " + t.viewRange);
        }
        
    }
    
    // ------------ helpers ------------
    
    /**
     * Creates a report for a specific tank
     * @param t the tank to report about
     * @param report the contents of the report
     */
    protected void report(Tank t, String report) {
        if(tankReports.containsKey(t)) {
            tankReports.get(t).add(report);
        } else {
            List<String> reports = new LinkedList<String>();
            reports.add(report);
            tankReports.put(t, reports);
        }
    }
    
    /**
     * Creates a report for a specific module
     * @param m the module to report about
     * @param report the contents of the report
     */
    protected void report(Module m, String report) {
        if(moduleReports.containsKey(m)) {
            moduleReports.get(m).add(report);
        } else {
            List<String> reports = new LinkedList<String>();
            reports.add(report);
            moduleReports.put(m, reports);
        }
    }
    
    
    // ------------ static stuff ------------
    
    
    /**
     * Creates and immediately prints a report for the given TankDB
     * @param db the database to write a report about
     */
    public static void printReportOf(TanksDB db) {
        System.out.println(writeReportOf(db));
    }
    
    /**
     * Creates a report for the given TankDB
     * @param db the database to write a report about
     */
    public static String writeReportOf(TanksDB db) {
        ModuleMap mm = ModuleMap.build(db);
        Evaluator eva = new Evaluator(db, mm);
        eva.buildReport();
        return eva.writeReport();
    }
    
}
