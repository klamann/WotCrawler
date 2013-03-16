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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import wdc.db.BaseProperties.Development;
import wdc.db.module.Engine;
import wdc.db.module.Gun;
import wdc.db.module.Radio;
import wdc.db.module.Suspension;
import wdc.db.module.Turret;
import wdc.db.tank.Equipment;
import wdc.db.tank.Tank;
import wdc.db.tank.Tank.TankType;

/**
 * The TankRating is an approach to show the strengths and weaknesses of every
 * tank by comparing it's stats to any other tank of the same class.
 * 
 * The existing tank stats are combined to create ratings in different categories,
 * which results in an overall tank rating, which is, of course, ignores some
 * important facts and is totally subjective anyway (though hopefully not too 
 * arbitrary).
 * 
 * For more details, see http://www.nx42.de/projects/wot/rating.html
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class TankRating {
    
    // base object: Tank
    protected Tank t;
    
    // derived tank details
    protected Equipment eq;
    protected Engine e;
    protected Gun g;
    protected Radio r;
    protected Suspension s;
    protected Turret tu;
    
    // abstraction layer 1: basic ratings
    protected double hitpoints;
    protected double weight;
    protected double firechance;
    protected double traverseTurret;
    protected double traverseSuspension;
    protected double gunAccuracy;
    protected double gunAimTime;
    protected double gunAmmo;
    protected double speed;
    protected double enginePower;
    protected double powerWeightRatio;
    protected double radioRange;
    protected double viewRange;
    // more advanced ratings, but focused on single basic stats
    protected double hullArmor;
    protected double turretArmor;
    protected double gunArc;
    protected double gunElevation;
    protected double damage;
    protected double penetration;
    
    // abstraction layer 2: cumulation of basic ratings in categories
    protected double ratingDefense;
    protected double ratingAttack;
    protected double ratingMobility;
    protected double ratingRecon;
    protected double ratingCostBenefit;
    
    // abstraction layer 3: final rating
    protected double ratingOverall;
    
    /**
     * Prepares a new TankRating for the specified tank with the given
     * development.
     * 
     * @param t the tank to create a rating for
     * @param dev the development of this tank
     */
    public TankRating(Tank t, Development dev) {
        this.t = t;
        
        this.e = ModuleMap.getModuleByDev(Engine.class, t, dev);
        this.g = ModuleMap.getModuleByDev(Gun.class, t, dev);
        this.r = ModuleMap.getModuleByDev(Radio.class, t, dev);
        this.s = ModuleMap.getModuleByDev(Suspension.class, t, dev);
        this.tu = ModuleMap.getModuleByDev(Turret.class, t, dev);
        
        switch(dev) {
            case Stock:
                this.eq = t.equipmentStock;
                break;
            case Top:
                this.eq = t.equipmentTop;
                break;
            default:
                System.err.println("Unsupported enum value: " + dev);
        }
        
    }
    
    /**
     * Calculates all available ratings.
     */
    public void calculateRatings() {
        calculateBaseRatings();
        calculateAdvancedRatings();
    }
    
    /**
     * Calculates the basic ratings (comparison of module parts and basic
     * properties)
     */
    protected void calculateBaseRatings() {
        
        // base
        this.hitpoints = percentage(Field.TE_Hitpoints, eq.hitpoints);
        this.weight = percentage(Field.TE_Weight , eq.weight);
        this.firechance = percentageInverse(Field.ME_Firechance , e.firechance);
        this.traverseTurret = percentage(Field.MT_Traverse , tu.traverse);
        this.traverseSuspension = percentage(Field.MS_Traverse , s.traverse);
        this.speed = percentage(Field.T_TopSpeed , t.speed);
        this.enginePower = percentage(Field.ME_Power , e.power);
        this.powerWeightRatio = percentage(Field.DP_HPperTon, t, eq.development);
        this.radioRange = percentage(Field.MR_Range , r.range);
        this.viewRange = percentage(Field.TE_ViewRange , eq.viewRange);
        
        // depending on development
        switch (eq.development) {
            case Stock:
                this.gunAccuracy = percentageInverse(Field.MG_Accuracy_Max, g.accuracyMax);
                this.gunAimTime = percentageInverse(Field.MG_AimTime_Max, g.aimTimeMax);
                break;
            case Top:
                this.gunAccuracy = percentageInverse(Field.MG_Accuracy_Min, g.accuracyMin);
                this.gunAimTime = percentageInverse(Field.MG_AimTime_Min, g.aimTimeMin);
                break;
        }
        
        // advanced
        this.gunElevation = percentage(Field.DP_Elevation, t, eq.development);
        this.gunArc = percentage(Field.DP_GunArc, t, eq.development);
        this.gunAmmo = percentage(Field.DP_Ammo_Normalized, t, eq.development);
        
        // prepare for calculation
        double hullF = percentage(Field.T_Hull_Front , t.hullFront);
        double hullS = percentage(Field.T_Hull_Side , t.hullRear);
        double hullR = percentage(Field.T_Hull_Rear , t.hullSide);
        double turretF = percentage(Field.MT_Armor_Front , tu.armorFront);
        double turretS = percentage(Field.MT_Armor_Side , tu.armorSide);
        double turretR = percentage(Field.MT_Armor_Rear , tu.armorRear);
        
        double dmgAP   = percentage(Field.DP_DmgPS_AP, t, eq.development);
        double dmgAPCR = percentage(Field.DP_DmgPS_APCR, t, eq.development);
        double dmgHE   = percentage(Field.DP_DmgPS_HE, t, eq.development);
        double dmgHEAT = percentage(Field.DP_DmgPS_HEAT, t, eq.development);
        double penAP   = percentage(Field.MG_Penetration_AP , g.penAP);
        double penAPCR = percentage(Field.MG_Penetration_APCR , g.penAPCR);
        double penHE   = percentage(Field.MG_Penetration_HE , g.penHE);
        double penHEAT = percentage(Field.MG_Penetration_HEAT , g.penHEAT);
        
        this.hullArmor = carlculateArmorRating(hullF, hullS, hullR);
        this.turretArmor = carlculateArmorRating(turretF, turretS, turretR);
        this.damage = carlculateDamageRating(new double[]{ dmgAP, dmgAPCR, dmgHE, dmgHEAT });
        this.penetration = carlculateDamageRating(new double[]{ penAP, penAPCR, penHE, penHEAT });
        
    }
    
    /**
     * Calculates the advanced ratings, depending on the tank type
     * (weighed cumulation of ratings in different categories - nonexclusive!)
     */
    protected void calculateAdvancedRatings() { 
        
        /*
         * values that might not be set (on purpose):
         * -> turret: hull, traverse
         * only for TD, SPG
         */
        
        switch(t.type) {
            case LightTank:
                ratingLightTank();
                break;
            case MediumTank:
                ratingMediumTank();
                break;
            case HeavyTank:
                ratingHeavyTank();
                break;
            case TankDestroyer:
                ratingTD();
                break;
            case SelfPropelledGun:
                ratingSPG();
                break;
            default:
                System.err.println("Unknown Tank type: "+t.type);
        }
        
    }
    
    /**
     * Calculates the ratings for light tanks
     */
    private void ratingLightTank() {
        
        this.ratingDefense =
                0.3 * hitpoints +
                0.2 * hullArmor +
                0.2 * turretArmor +
                0.1 * weight +     // resist ram
                0.03 * firechance +
                0.05 * gunElevation +
                0.06 * traverseSuspension +
                0.06 * traverseTurret;
        this.ratingAttack = 
                0.3 * penetration +
                0.4 * damage +
                0.08 * gunAmmo +
                0.08 * gunAccuracy +
                0.04 * gunAimTime +
                0.04 * gunElevation +
                0.06 * weight;     // ram
        this.ratingMobility = 
                0.45 * speed +
                0.30 * powerWeightRatio +
                0.15 * traverseSuspension +
                0.10 * traverseTurret;
        this.ratingRecon = 
                0.35 * radioRange +
                0.65 * viewRange;
        this.ratingCostBenefit = -1;     // ignore for now...

        this.ratingOverall = 
                0.2 * ratingDefense +
                0.4 * ratingAttack +
                0.3 * ratingMobility +
                0.1 * ratingRecon;
    }
    
    /**
     * Calculates the ratings for medium tanks
     */
    private void ratingMediumTank() {
        
        this.ratingDefense =
                0.25 * hitpoints +
                0.3 * hullArmor +
                0.25 * turretArmor +
                0.05 * weight +     // resist ram
                0.02 * firechance +
                0.03 * gunArc +
                0.03 * gunElevation +
                0.03 * traverseSuspension +
                0.04 * traverseTurret;
        this.ratingAttack = 
                0.4 * penetration +
                0.3 * damage +
                0.05 * gunAmmo +
                0.1 * gunAccuracy +
                0.05 * gunAimTime +
                0.03 * gunArc +
                0.03 * gunElevation +
                0.04 * weight;     // ram
        this.ratingMobility = 
                0.4 * speed +
                0.3 * powerWeightRatio +
                0.15 * traverseSuspension +
                0.15 * traverseTurret;
        this.ratingRecon = 
                0.35 * radioRange +
                0.65 * viewRange;
        this.ratingCostBenefit = -1;     // ignore for now...

        this.ratingOverall = 
                0.34 * ratingDefense +
                0.34 * ratingAttack +
                0.2 * ratingMobility +
                0.12 * ratingRecon;
    }
    
    /**
     * Calculates the ratings for heavy tanks
     */
    private void ratingHeavyTank() {
        
        this.ratingDefense =
                0.25 * hitpoints +
                0.3 * hullArmor +
                0.25 * turretArmor +
                0.05 * weight +     // resist ram
                0.02 * firechance +
                0.03 * gunArc +
                0.03 * gunElevation +
                0.03 * traverseSuspension +
                0.04 * traverseTurret;
        this.ratingAttack = 
                0.4 * penetration +
                0.3 * damage +
                0.05 * gunAmmo +
                0.08 * gunAccuracy +
                0.07 * gunAimTime +
                0.03 * gunArc +
                0.03 * gunElevation +
                0.04 * weight;     // ram
        this.ratingMobility = 
                0.24 * speed +
                0.16 * powerWeightRatio +
                0.2 * traverseSuspension +
                0.4 * traverseTurret;
        this.ratingRecon = 
                0.5 * radioRange +
                0.5 * viewRange;
        this.ratingCostBenefit = -1;     // ignore for now...
        
        this.ratingOverall = 
                0.38 * ratingDefense +
                0.37 * ratingAttack +
                0.15 * ratingMobility +
                0.1 * ratingRecon;
    }
    
    /**
     * Calculates the ratings for tank destroyers
     */
    private void ratingTD() {
        boolean noturret = (turretArmor == -1);
        
        if(noturret) {
            this.ratingDefense =
                0.3 * hitpoints +
                0.5 * hullArmor +
                0.04 * weight +     // resist ram
                0.02 * firechance +
                0.07 * gunArc +
                0.04 * gunElevation +
                0.03 * traverseSuspension;
            this.ratingMobility = 
                0.35 * speed +
                0.15 * powerWeightRatio +
                0.5 * traverseSuspension;
        } else {
            this.ratingDefense =
                0.3 * hitpoints +
                0.3 * hullArmor +
                0.2 * turretArmor +
                0.03 * weight +     // resist ram
                0.01 * firechance +
                0.06 * gunArc +
                0.04 * gunElevation +
                0.02 * traverseSuspension +
                0.04 * traverseTurret;
            this.ratingMobility = 
                0.35 * speed +
                0.15 * powerWeightRatio +
                0.15 * traverseSuspension +
                0.35 * traverseTurret;
        }
        
        this.ratingAttack = 
                0.30 * penetration +
                0.36 * damage +
                0.05 * gunAmmo +
                0.1 * gunAccuracy +
                0.07 * gunAimTime +
                0.05 * gunArc +
                0.05 * gunElevation +
                0.02 * weight;     // ram
        this.ratingRecon = 
                0.35 * radioRange +
                0.65 * viewRange;
        this.ratingCostBenefit = -1;     // ignore for now...
        
        this.ratingOverall = 
                0.25 * ratingDefense +
                0.5 * ratingAttack +
                0.15 * ratingMobility +
                0.1 * ratingRecon +
                0.0 * ratingCostBenefit;
    }
    
    /**
     * Calculates the ratings for SPGs
     */
    private void ratingSPG() {
        boolean noturret = (turretArmor == -1);
        
        if(noturret) {
            this.ratingDefense =
                0.25 * hitpoints +
                0.35 * hullArmor +
                0.1 * weight +     // resist ram
                0.02 * firechance +
                0.1 * gunArc +
                0.03 * gunElevation +
                0.15 * traverseSuspension;
            this.ratingMobility = 
                0.2 * speed +
                0.1 * powerWeightRatio +
                0.7 * traverseSuspension;
        } else {
            this.ratingDefense =
                0.25 * hitpoints +
                0.20 * hullArmor +
                0.15 * turretArmor +
                0.08 * weight +     // resist ram
                0.02 * firechance +
                0.07 * gunArc +
                0.03 * gunElevation +
                0.05 * traverseSuspension +
                0.15 * traverseTurret;
            this.ratingMobility = 
                0.15 * speed +
                0.08 * powerWeightRatio +
                0.2 * traverseSuspension +
                0.57 * traverseTurret;
        }
        
        this.ratingAttack = 
                0.2 * penetration +
                0.5 * damage +
                0.03 * gunAmmo +
                0.08 * gunAccuracy +
                0.08 * gunAimTime +
                0.08 * gunArc +
                0.03 * gunElevation;
        this.ratingRecon = 
                0.8 * radioRange +
                0.2 * viewRange;
        this.ratingCostBenefit = -1;     // ignore for now...

        this.ratingOverall = 
                0.1 * ratingDefense +
                0.7 * ratingAttack +
                0.1 * ratingMobility +
                0.1 * ratingRecon;
    }
    
    /**
     * Calculates the armor rating from the single values for front, side and
     * rear armor.
     * @param front the front armor rating
     * @param side the side armor rating
     * @param rear the rear armor rating
     * @return the overall armor rating
     */
    private double carlculateArmorRating(double front, double side, double rear) {
        return front * 0.5 + side * 0.3 + rear * 0.2;
    }
    
    /**
     * Calculates the damage rating from the single ratings of the four damage
     * values (ap, apcr, he, heat), which need to be specified in this order in
     * the array parameter. Nonexisting damage types need to be specified with -1.0.
     * The average of all existing ratings will be the overall damage rating
     * (nonexisting damage types are ignored)
     * @param damage the four damage ratings (ap, apcr, he, heat), in this, and
     * ONLY THIS order. nonexisting damage ratings need to be set to -1
     * @return the overall damage rating for this gun
     */
    private double carlculateDamageRating(double[] damage) {
        List<Double> valid = new LinkedList<Double>();
        for (double dmg : damage) {
            if(dmg >= 0 && dmg <= 1) {
                valid.add(dmg);
            } else if(dmg != -1.0) {
                // -1 is the magic number for nonexistent damage type. All others are errors...
                System.err.println(String.format("Tank %s has an illegal damage rating of " + dmg
                        + ". This should not be possible!", t.name));
            }
        }
        
        double sum = 0.0;
        for (Double dmg : valid) {
            sum += dmg;
        }
        
        return valid.size() > 0 ? (sum / valid.size()) : -1;
    }

    /**
     * Creates the rating for a single field with the specified value.
     * The rating is the fraction of the actual value from the best value
     * that has been determined before.
     * @param f the field to look up the best value from
     * @param value the actual value of this field (for the current tank)
     * @return the rating of this field for the specified value
     */
    private double percentage(Field f, double value) {
        
        if (value > 0.0) {
            if (value > f.best) {
                System.err.println("value not within reasonable bounds: " + value + " / " + f.best);
            } else {
                return value / f.best;
            }
        } else if (value < 0.0) {
            System.err.println(String.format("Field %s for Tank %s is associated with illegal value %s.", f.toString(), t.name, value));
        }
        
        // ignore 0.0 values, these are expected e.g. for incompatible damage types
        // return error value -1
        return -1;
    }
    
    /**
     * Creates the rating for a single field with the value from the specified
     * tank.
     * The rating is the fraction of the actual value from the best value
     * that has been determined before.
     * @param f the field to look up the best value from
     * @param t the tank to look up the actual value from
     * @param dev the development of this tank (in case the actual value depends
     * on it...)
     * @return the rating of this field for the specified tank
     */
    private double percentage(Field f, Tank t, Development dev) {
        return percentage(f, f.calc(t, dev));
    }
    
    /**
     * Creates the rating for a single field where the principle "lower is
     * better" applies.
     * As opposed to the percentage-Method, the rating is determined by the
     * inverse fraction of actual and best value: 1.0 / (actual / best)
     * @param f the field to look up the best (=lowest) value from
     * @param value the actual value of this field (for the current tank)
     * @return the rating of this field for the specified value
     */
    private double percentageInverse(Field f, double value) {
        
        if(value > 0.0) {
            if(value < f.best) {
                System.err.println("value (lower is better) not within reasonable bounds: " + value + " / " + f.best);
            } else {
                return 1.0 / (value / f.best);
            }
        } else if (value < 0.0) {
            System.err.println(String.format("Field %s for Tank %s is associated with illegal value %s.", f.toString(), t.name, value));
        }
        
        // ignore 0.0 values, these are expected e.g. for incompatible damage types
        // return error value -1
        return -1;
    }
    
    /**
     * Creates the rating for a single field where the principle "lower is
     * better" applies.
     * As opposed to the percentage-Method, the rating is determined by the
     * inverse fraction of actual and best value: 1.0 / (actual / best)
     * @param f the field to look up the best (=lowest) value from
     * @param t the tank to look up the actual value from
     * @param dev the development of this tank (in case the actual value depends
     * on it...)
     * @return the rating of this field for the specified tank
     */
    private double percentageInverse(Field f, Tank t, Development dev) {
        return percentageInverse(f, f.calc(t, dev));
    }
    
    
    
    // ---------- static stuff ----------
    
    /**
     * Creates a HTML snipped that contains the specified value inside a 
     * <code><td></code> tag and the specified comment inside a 
     * <code><abbr></code> tag which is wrapped around the value.
     * @param value the field value
     * @param comment the comment to add to this value
     * @return a html snipped containing the value and the comment inside a
     * table cell
     */
    public static String fieldToHTML(String value, String comment) {
        return String.format("<td><abbr title=\"%s\">%s</abbr></td>", comment, value);
    }
    
    /**
     * Creates a HTML snipped that contains the specified value inside a 
     * <code><td></code> tag.
     * @param value the field value
     * @return a html snipped containing the value inside a table cell
     */
    public static String fieldToHTML(String value) {
        return String.format("<td>%s</td>", value);
    }
    
    /**
     * Get the ratings of a specific tank type
     * @param ratings all available ratings
     * @param type the type of rating to filter
     * @return only ratings of the specified tank type
     */
    public static List<TankRating> getRatings(List<TankRating> ratings, TankType type) {
        List<TankRating> r = new ArrayList<TankRating>(ratings.size() / 5);
        for (TankRating tr : ratings) {
            if(tr.t.type == type) {
                r.add(tr);
            }
        }
        return r;
    }
    
}
