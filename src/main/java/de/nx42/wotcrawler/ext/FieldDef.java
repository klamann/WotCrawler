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

/**
 * This class contains some more or less arbitrary collections of fields.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class FieldDef {
    
    /**
     * All basic fields (tank and modules)
     */
    public final static Field[] complete = {
        
        // tank (base)
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_Tier,
        Field.T_BattleTier_Min,
        Field.T_BattleTier_Max,
        Field.T_CrewMembers,
        Field.T_TopSpeed,
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.T_Cost,
        Field.T_Currency,
        Field.T_Gift,
        Field.T_GunArc_Left,
        Field.T_GunArc_Right,
        Field.T_Children,
        Field.T_Parents,
        Field.EMPTY,
        
        // tank (depending)
        Field.TE_Development,
        Field.TE_Elevation_Low,
        Field.TE_Elevation_High,
        Field.TE_Hitpoints,
        Field.TE_ViewRange,
        Field.TE_Weight,
        Field.TE_WeightLimit,
        Field.EMPTY,
        
        // module: engine
        Field.ME_Name,
        Field.ME_Tier,
        Field.ME_Nation,
        Field.ME_Cost,
        Field.ME_Currency,
        Field.ME_Weight,
        Field.ME_Compatibility,
        // specifics
        Field.ME_Firechance,
        Field.ME_Gas,
        Field.ME_Power,
        Field.EMPTY,
        
        // module: gun
        Field.MG_Name,
        Field.MG_Tier,
        Field.MG_Nation,
        Field.MG_Cost,
        Field.MG_Currency,
        Field.MG_Weight,
        Field.MG_Compatibility,
        // specifics
        Field.MG_Accuracy_Min,
        Field.MG_Accuracy_Max,
        Field.MG_AimTime_Min,
        Field.MG_AimTime_Max,
        Field.MG_AmmoCapacity_Min,
        Field.MG_AmmoCapacity_Max,
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Dmg_HEAT,
        Field.MG_FireRate_Min,
        Field.MG_FireRate_Max,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_Penetration_HEAT,
        Field.EMPTY,
        
        // module: radio
        Field.MR_Name,
        Field.MR_Tier,
        Field.MR_Nation,
        Field.MR_Cost,
        Field.MR_Currency,
        Field.MR_Weight,
        Field.MR_Compatibility,
        // specifics
        Field.MR_Range,
        Field.EMPTY,
        
        // module: suspension
        Field.MS_Name,
        Field.MS_Tier,
        Field.MS_Nation,
        Field.MS_Cost,
        Field.MS_Currency,
        Field.MS_Weight,
        Field.MS_Compatibility,
        // specifics
        Field.MS_Load,
        Field.MS_Traverse,
        Field.EMPTY,
        
        // module: turret
        Field.MT_Name,
        Field.MT_Tier,
        Field.MT_Nation,
        Field.MT_Cost,
        Field.MT_Currency,
        Field.MT_Weight,
        Field.MT_Compatibility,
        // specifics
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        Field.MT_Traverse,
        Field.MT_ViewRange
        
    };
    
    /**
     * All basic fields, reordered and with less redundancy
     */
    public final static Field[] complete_irredundant = {
        
        // tank (base)
        Field.TE_Development,
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_CrewMembers,
        Field.T_Tier,
        Field.T_BattleTier_Min,
        Field.T_BattleTier_Max,
        Field.TE_Hitpoints,
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.T_TopSpeed,
        Field.TE_ViewRange,
        Field.T_Cost,
        Field.T_Currency,
        Field.T_Gift,
        Field.TE_Weight,
        Field.TE_WeightLimit,
        Field.T_GunArc_Left,
        Field.T_GunArc_Right,
        Field.TE_Elevation_Low,
        Field.TE_Elevation_High,
        Field.T_Children,
        Field.T_Parents,
        Field.EMPTY,
        
        // module: engine
        Field.ME_Name,
        Field.ME_Tier,
        Field.ME_Cost,
        Field.ME_Currency,
        Field.ME_Weight,
        Field.ME_Power,
        Field.ME_Gas,
        Field.ME_Firechance,
        Field.EMPTY,
        
        // module: gun
        Field.MG_Name,
        Field.MG_Tier,
        Field.MG_Cost,
        Field.MG_Currency,
        Field.MG_Weight,
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Dmg_HEAT,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_FireRate_Min,
        Field.MG_FireRate_Max,
        Field.MG_AimTime_Min,
        Field.MG_AimTime_Max,
        Field.MG_Accuracy_Min,
        Field.MG_Accuracy_Max,
        Field.MG_AmmoCapacity_Min,
        Field.MG_AmmoCapacity_Max,
        Field.EMPTY,
        
        // module: radio
        Field.MR_Name,
        Field.MR_Tier,
        Field.MR_Cost,
        Field.MR_Currency,
        Field.MR_Weight,
        Field.MR_Range,
        Field.EMPTY,
        
        // module: suspension
        Field.MS_Name,
        Field.MS_Tier,
        Field.MS_Cost,
        Field.MS_Currency,
        Field.MS_Weight,
        Field.MS_Load,
        Field.MS_Traverse,
        Field.EMPTY,
        
        // module: turret
        Field.MT_Name,
        Field.MT_Tier,
        Field.MT_Cost,
        Field.MT_Currency,
        Field.MT_Weight,
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        Field.MT_Traverse,
        Field.MT_ViewRange
        
    };
    
    /**
     * Detailed subset of fields, with separate module values
     */
    public final static Field[] detailed_ModulesApart = {
        
        // tank (base)
        Field.TE_Development,
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_CrewMembers,
        
        Field.T_Tier,
        Field.T_BattleTier_Min,
        Field.T_BattleTier_Max,
        
        Field.TE_Hitpoints,
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.T_TopSpeed,
        Field.TE_ViewRange,
        
        Field.T_Cost,
        Field.T_Currency,
        Field.T_Gift,
        
        Field.TE_Weight,
        Field.TE_WeightLimit,
        
        Field.T_GunArc_Left,
        Field.T_GunArc_Right,
        Field.TE_Elevation_Low,
        Field.TE_Elevation_High,
        
        Field.T_Children,
        Field.T_Parents,
        Field.EMPTY,
        
        // module: engine
        Field.ME_Power,
        Field.ME_Firechance,
        Field.ME_Weight,
        Field.ME_Cost,
        Field.ME_Currency,
        Field.EMPTY,
        
        // module: gun
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Dmg_HEAT,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_Penetration_HEAT,
        Field.MG_FireRate_Min,
        Field.MG_FireRate_Max,
        Field.MG_AimTime_Min,
        Field.MG_AimTime_Max,
        Field.MG_Accuracy_Min,
        Field.MG_Accuracy_Max,
        Field.MG_AmmoCapacity_Min,
        Field.MG_AmmoCapacity_Max,
        Field.MG_Cost,
        Field.MG_Currency,
        Field.MG_Weight,
        Field.EMPTY,
        
        // module: radio
        Field.MR_Range,
        Field.MR_Weight,
        Field.MR_Cost,
        Field.MR_Currency,
        Field.EMPTY,
        
        // module: suspension
        Field.MS_Load,
        Field.MS_Traverse,
        Field.MS_Weight,
        Field.MS_Cost,
        Field.MS_Currency,
        Field.EMPTY,
        
        // module: turret
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        Field.MT_Traverse,
        Field.MT_ViewRange,
        Field.MT_Weight,
        Field.MT_Cost,
        Field.MT_Currency,
        
    };
    
    
    /**
     * Detailed subset of fields, fields mixed (grouped by topic)
     */
    public final static Field[] detailed_Combined = {
        
        // tank (base)
        Field.TE_Development,
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_CrewMembers,
        
        // tier
        Field.T_Tier,
        Field.T_BattleTier_Min,
        Field.T_BattleTier_Max,
        
        // cost
        Field.T_Cost,
        Field.T_Currency,
        Field.T_Gift,
        
        // hp + weight
        Field.TE_Hitpoints,
        Field.TE_Weight,
        Field.TE_WeightLimit,
        
        // mobility
        Field.ME_Power,
        Field.DP_HPperTon,
        Field.T_TopSpeed,
        Field.MS_Traverse,
        
        // armor
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        
        // armament
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Dmg_HEAT,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_Penetration_HEAT,
        Field.MG_FireRate_Min,
        Field.MG_FireRate_Max,
        Field.MG_AimTime_Min,
        Field.MG_AimTime_Max,
        Field.MG_Accuracy_Min,
        Field.MG_Accuracy_Max,
        Field.MG_AmmoCapacity_Min,
        Field.MG_AmmoCapacity_Max,
        Field.MT_Traverse,
        Field.T_GunArc_Left,
        Field.T_GunArc_Right,
        Field.TE_Elevation_Low,
        Field.TE_Elevation_High,
        
        // general
        Field.TE_ViewRange,
        Field.MR_Range,
        Field.ME_Firechance,
//        Field.T_Parents,
//        Field.T_Children,
        
    };
    
    /**
     * All Engine Fields
     */
    public final static Field[] mod_engine = {
        Field.ME_Nation,
        Field.ME_Tier,
        Field.ME_Name,
        Field.ME_Power,
        Field.ME_Firechance,
        Field.ME_Gas,
        Field.ME_Cost,
        Field.ME_Currency,
        Field.ME_Weight,
        Field.ME_Compatibility
    };
    
    /**
     * All Gun Fields
     */
    public final static Field[] mod_gun = {
        Field.MG_Nation,
        Field.MG_Tier,
        Field.MG_Name,
        Field.MG_AmmoCapacity_Min,
        Field.MG_AmmoCapacity_Max,
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Dmg_HEAT,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_Penetration_HEAT,
        Field.MG_FireRate_Min,
        Field.MG_FireRate_Max,
        Field.MG_Accuracy_Min,
        Field.MG_Accuracy_Max,
        Field.MG_AimTime_Min,
        Field.MG_AimTime_Max,
        Field.MG_Cost,
        Field.MG_Currency,
        Field.MG_Weight,
        Field.MG_Compatibility
    };
    
    /**
     * All Radio Fields
     */
    public final static Field[] mod_radio = {
        Field.MR_Nation,
        Field.MR_Tier,
        Field.MR_Name,
        Field.MR_Range,
        Field.MR_Cost,
        Field.MR_Currency,
        Field.MR_Weight,
        Field.MR_Compatibility
    };
    
    /**
     * All Suspension Fields
     */
    public final static Field[] mod_suspension = {
        Field.MS_Nation,
        Field.MS_Tier,
        Field.MS_Name,
        Field.MS_Load,
        Field.MS_Traverse,
        Field.MS_Cost,
        Field.MS_Currency,
        Field.MS_Weight,
        Field.MS_Compatibility
    };
    
    /**
     * All Turret Fields
     */
    public final static Field[] mod_turret = {
        Field.MT_Nation,
        Field.MT_Tier,
        Field.MT_Name,
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        Field.MT_Traverse,
        Field.MT_ViewRange,
        Field.MT_Cost,
        Field.MT_Currency,
        Field.MT_Weight,
        Field.MT_Compatibility
    };
    
    /**
     * tank specifics without modules
     */
    public final static Field[] tank_base = {
        
        // tank (base)
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_Tier,
        Field.T_BattleTier_Min,
        Field.T_BattleTier_Max,
        Field.T_CrewMembers,
        Field.T_TopSpeed,
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.T_Cost,
        Field.T_Currency,
        Field.T_Gift,
        Field.T_GunArc_Left,
        Field.T_GunArc_Right,
        Field.T_Children,
        Field.T_Parents,
        
        // tank (depending)
        Field.TE_Development,
        Field.TE_Elevation_Low,
        Field.TE_Elevation_High,
        Field.TE_Hitpoints,
        Field.TE_ViewRange,
        Field.TE_Weight,
        Field.TE_WeightLimit,
        
        // module lists
        Field.REL_Engines,
        Field.REL_Guns,
        Field.REL_Radios,
        Field.REL_Suspensions,
        Field.REL_Turrets
        
    };
    
    /**
     * Short and arbitrary list of fields...
     */
    public final static Field[] simple = {
        
        // tank (base)
        Field.TE_Development,
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_Tier,
        
        // hp + armor
        Field.TE_Hitpoints,
        Field.T_Hull_Front,
        Field.T_Hull_Side,
        Field.T_Hull_Rear,
        Field.MT_Armor_Front,
        Field.MT_Armor_Side,
        Field.MT_Armor_Rear,
        
        // armament
        Field.MG_Dmg_AP,
        Field.MG_Dmg_APCR,
        Field.MG_Dmg_HE,
        Field.MG_Penetration_AP,
        Field.MG_Penetration_APCR,
        Field.MG_Penetration_HE,
        Field.MG_FireRate_Max,
        Field.MG_AimTime_Max,
        Field.MG_Accuracy_Max,
        Field.MG_AmmoCapacity_Max,
        Field.MT_Traverse,
        
        // mobility
        Field.ME_Power,
        Field.T_TopSpeed,
        Field.MS_Traverse,
        
        // else
        Field.TE_ViewRange,
        Field.MR_Range,
        Field.TE_Weight,
        Field.T_Cost,
        Field.T_Currency,
        
    };
    
    /**
     * All Rating fields and some base tank properties
     */
    public final static Field[] rating = { 
        
        // base properties
        Field.TE_Development,
        Field.T_Name,
        Field.T_Type,
        Field.T_Nation,
        Field.T_Tier,
        Field.EMPTY,
        
        // overall rating
        Field.RT2_OverallRating,
        Field.EMPTY,
        
        // main categories
        Field.RT2_Attack,
        Field.RT2_Defense,
        Field.RT2_Mobility,
        Field.RT2_Recon,
        Field.RT2_CostBenefit,
        Field.EMPTY,
        
        // single ratings
        Field.RT_Damage,
        Field.RT_Penetration,
        Field.RT_Accuracy,
        Field.RT_AimTime,
        Field.RT_AmmoCapacity,
        
        Field.RT_Hitpoints,
        Field.RT_HullArmor,
        Field.RT_TurretArmor,
        Field.RT_GunArc,
        Field.RT_GunElevation,
        
        Field.RT_TopSpeed,
        Field.RT_PowerWeightRatio,
        Field.RT_TraverseSuspension,
        Field.RT_TraverseTurret,
        
        Field.RT_RadioRange,
        Field.RT_ViewRange,
        
        Field.RT_Weight,
        Field.RT_EnginePower,
        Field.RT_Firechance,
        
    };
    
}
