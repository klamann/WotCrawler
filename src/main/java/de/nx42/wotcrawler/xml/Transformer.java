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
package de.nx42.wotcrawler.xml;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nx42.wotcrawler.db.BaseProperties.Development;
import de.nx42.wotcrawler.db.TanksDB;
import de.nx42.wotcrawler.db.module.Module;
import de.nx42.wotcrawler.db.tank.Tank;
import de.nx42.wotcrawler.db.tank.Tank.TankType;
import de.nx42.wotcrawler.ext.Field;
import de.nx42.wotcrawler.ext.FieldDef;
import de.nx42.wotcrawler.ext.ModuleMap;
import de.nx42.wotcrawler.ext.TankRating;

/**
 * This class is used to transform the database contents into other formats.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Transformer {
    
    private static final Logger log = LoggerFactory.getLogger(Transformer.class);
    
    /** The database to transform */
    protected TanksDB db;
    /** The ModuleMap, mapping from each tank to a list of compatible modules */
    protected ModuleMap mm;
    
    /**
     * Initializes the transformer with the specified TanksDB
     * @param db the database to work with
     */
    public Transformer(TanksDB db) {
        this.db = db;
        this.mm = ModuleMap.build(db);
    }
    
    // -------------------- generic html table creation --------------------
    
    /**
     * Writes a html table containing all tanks with stock and top equipment
     * and the specified fields. All fields are allowed except for the rating
     * fields (prefix RT_ and RT2_)
     * @param dest the file where the table shall be stored in
     * @param fields the fields that shall be stored in the table
     */
    public void writeTableTank(String dest, Field[] fields) {
        try {
            PrintWriter out = new PrintWriter(dest);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<html><body>\n\n");
            out.write(buildTankTable(fields));
            out.write("\n\n</body></html>");
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            log.error("Could not write to local file: Not found!", ex);
        }
    }
    
    // -------------------- specific html tables --------------------
    
    
    /**
     * Writes a table at the specified location, containing many details about
     * each tank
     * @param dest the file where the table shall be stored in
     */
    public void writeTableTankDetailed(String dest) {
        writeTableTank(dest, FieldDef.detailed_Combined);
    }
    
    /**
     * Writes a table containing all tanks, their parents and children and
     * compatible modules, as well as one table for each module type.
     * There are html anchors between all tanks and modules, but prepare for
     * big tables...
     * @param dest the file where the tables shall be stored in
     */
    public void writeTablesLinked(String dest) {
        try {
            PrintWriter out = new PrintWriter(dest);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<html><body><table>\n\n");
            out.write(buildTankTable(FieldDef.tank_base));
            out.write("</table>\n\n");
            out.write(buildModuleTables());
            out.write("\n\n\n</body></html>");
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            log.error("Could not write to local file: Not found!", ex);
        }
    }
    
    /**
     * Writes a table for each tank type, containing the ratings for each tank,
     * as defined by the TankRating class.
     * @param dest the file where the tables shall be stored in
     */
    public void writeRatingTable(String dest) {
        try {
            PrintWriter out = new PrintWriter(dest);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<html><body>\n\n");
            
            out.write("\n\n\n<!-- light -->\n\n");
            out.write(buildSingleRatingTable(TankType.LightTank, FieldDef.rating));
            out.write("\n\n\n<!-- medium -->\n\n");
            out.write(buildSingleRatingTable(TankType.MediumTank, FieldDef.rating));
            out.write("\n\n\n<!-- heavy -->\n\n");
            out.write(buildSingleRatingTable(TankType.HeavyTank, FieldDef.rating));
            out.write("\n\n\n<!-- td -->\n\n");
            out.write(buildSingleRatingTable(TankType.TankDestroyer, FieldDef.rating));
            out.write("\n\n\n<!-- spg -->\n\n");
            out.write(buildSingleRatingTable(TankType.SelfPropelledGun, FieldDef.rating));
            
            out.write("\n\n\n</body></html>");
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            log.error("Could not write to local file: Not found!", ex);
        }
    }
    
    // -------------------- tables only --------------------
    
    /**
     * Builds a single html table, containing all the specified fields.
     * Note: All fields are allowed except for the rating fields (prefix RT_ and RT2_)
     * @param fields the fields that shall be stored in the table
     * @return the HTML table as string
     */
    protected String buildTankTable(Field[] fields) {
        StringBuilder sb = new StringBuilder(fields.length * 2 * 20);
        sb.append("<table>\n");

        // head
        sb.append("<thead>\n<tr>\n");
        for (Field f : fields) {
            sb.append("\t<th>");
            sb.append(f.toString());
            sb.append("</th>\n");
        }
        sb.append("</tr>\n</thead>\n");

        // body
        sb.append("<tbody>\n");
        for (Tank t : db.tanks) {
            for (Development dev : Development.values()) {
                sb.append(String.format("<tr id=\"%s\">\n", t.id));
                for (Field field : fields) {
                    sb.append("\t<td>");
                    sb.append(convertField(field, t, dev));
                    sb.append("</td>\n");
                }
                sb.append("</tr>\n");
            }
        }
        sb.append("</tbody>\n");
        
        sb.append("</table>\n");
        return sb.toString();
    }
    
    /**
     * Builds 5 tables, one for each module type, with the specified fields
     * for each module type
     * @param engine the fields that shall be stored in the engine table
     * @param gun the fields that shall be stored in the gun table
     * @param radio the fields that shall be stored in the radio table
     * @param suspension the fields that shall be stored in the suspension table
     * @param turret the fields that shall be stored in the turret table
     * @return all the html tables in one long string...
     */
    protected String buildModuleTables(Field[] engine, Field[] gun, Field[] radio, Field[] suspension, Field[] turret) {
        StringBuilder sb = new StringBuilder(100000);
        
        sb.append("\n\n\n<!-- engines -->\n\n");
        sb.append(buildSingleModuleTable(db.modules.engines, engine));
        
        sb.append("\n\n\n<!-- guns -->\n\n");
        sb.append(buildSingleModuleTable(db.modules.guns, gun));
        
        sb.append("\n\n\n<!-- radios -->\n\n");
        sb.append(buildSingleModuleTable(db.modules.radios, radio));
        
        sb.append("\n\n\n<!-- suspensions -->\n\n");
        sb.append(buildSingleModuleTable(db.modules.suspensions, suspension));
        
        sb.append("\n\n\n<!-- turrets -->\n\n");
        sb.append(buildSingleModuleTable(db.modules.turrets, turret));
        
        return sb.toString();
    }
    
    /**
     * Builds 5 tables, one for each module type, using all available fields
     * for each module type
     * @return all the html tables in one long string...
     */
    protected String buildModuleTables() {
        return buildModuleTables(FieldDef.mod_engine, FieldDef.mod_gun,
                FieldDef.mod_radio, FieldDef.mod_suspension, FieldDef.mod_turret);
    }
    
    /**
     * Creates a table from a list of modules and an array of fields the table
     * shall contain. Also creates a header with ugly names...
     * Restrictions: Use only one type of module, use only fields that are
     * covered by this module!
     * For different module types, you got to create a new table for each module
     * type.
     * 
     * @param modules this should be <code>List<Module></code>, but as java
     * eliminates types at compilation, this is not possible, but don't expect
     * that this will work for anything but Modules or their subclasses!
     * @param fields the fields that shall be contained in the table. choose
     * only fields that are compatible with this module (watch out for prefixes...)
     * @return HTML table containing the modules in the list and the fields from
     * the array.
     */
    protected String buildSingleModuleTable(List<? extends Module> modules, Field[] fields) {
        StringBuilder sb = new StringBuilder(20000);
        sb.append("<table>\n");

        // head
        sb.append("<thead>\n<tr>\n");
        for (Field f : fields) {
            sb.append("\t<th>");
            sb.append(f.toString());
            sb.append("</th>\n");
        }
        sb.append("</tr>\n</thead>\n");

        // body
        sb.append("<tbody>\n");
        for (Object o : modules) {
            Module m = (Module) o;
            sb.append(String.format("<tr id=\"%s\">\n", m.name));
            for (Field f : fields) {
                sb.append("\t<td>");
                sb.append(f.get(m));
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</tbody>\n");

        sb.append("</table>\n");
        return sb.toString();
    }
    
    /**
     * Generates a single table containing all tanks of the specified type.
     * The rating Fields are supported here, as well as all other fields.
     * Note that for the rating fields, tooltips will be created that show the
     * actual values behind the ratings.
     * 
     * @param type the tanks that shall be stored in this table (ratings are
     * always bound to one type - apples and oranges)
     * @param fields the fields that shall be contained in the table
     * @return the rating table as string
     */
    protected String buildSingleRatingTable(TankType type, Field[] fields) {
        
        /*
         * calculate minmax for this tanktype
         * calculate rating for each tank and write in sb
         * 
         * -> if minmax is calculated staticly but for this type only,
         *    multithreaded access to enum Field is not allowed!
         */
        
        Field.calculateMinMaxFields(db, type);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");

        // head
        sb.append("<thead>\n<tr>\n");
        for (Field f : fields) {
            sb.append("\t<th>");
            sb.append(f.toString());
            sb.append("</th>\n");
        }
        sb.append("</tr>\n</thead>\n");

        // body
        sb.append("<tbody>\n");
        for (Tank t : db.tanks) {
            if (t.type == type) {
                for (Development dev : Development.values()) {
                    
                    // calculate rating
                    TankRating tr = new TankRating(t, dev);
                    tr.calculateRatings();
                    
                    // write table entry
                    sb.append(String.format("<tr id=\"%s\">\n", t.id));
                    for (Field field : fields) {
                        addCell(tr, sb, field);
                    }
                    sb.append("</tr>\n");
                }
            }
        }
        sb.append("</tbody>\n");

        sb.append("</table>\n");
        return sb.toString();
    }
    
    // -------------------- helpers --------------------
    
    /**
     * Adds a cell to the stringbuilder. If the cell contains the special field
     * EMPTY, an empty td with class "empty" is added, else the contents of this
     * cell's get() method are stored within td tags
     * @param tr the tankrating (as source for the cell valie)
     * @param sb the stringbuilder, where the result is added
     * @param f the field to retrieve the value from
     */
    protected static void addCell(TankRating tr, StringBuilder sb, Field f) {
        if (f == Field.EMPTY) {
            sb.append("\t<td class=\"empty\"></td>\n");
        } else {
            sb.append("\t<td>");
            sb.append(f.get(tr));
            sb.append("</td>\n");
        }
    }
    
    /**
     * Retrieves the value of a field and converts its contents with integer or
     * decimal value 0 to n/a (except for fields where 0 is a valid value,
     * such as Cost or GunArc)
     * @param field the field to retrieve the value from
     * @param t the Tank to calculate the value for
     * @param dev the development (needed if field depends on it)
     * @return the converted field value
     */
    protected String convertField(Field field, Tank t, Development dev) {
        String f = field.get(t, dev);
        if(f.startsWith("0") && field != Field.T_GunArc_Left && field != Field.T_Cost) {
            if("0".equals(f)) {
                return Field.na;
            } else if(f.startsWith("0.0")) {
                return Field.na;
            }
        }
        return f;
    }
    
}
