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
package wdc.xml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import wdc.db.BaseProperties;
import wdc.db.BaseProperties.Currency;
import wdc.db.BaseProperties.Nation;
import wdc.db.Modules;
import wdc.db.TanksDB;
import wdc.db.module.Engine;
import wdc.db.module.Engine.Gas;
import wdc.db.module.Gun;
import wdc.db.module.Module;
import wdc.db.module.Module.ModuleType;
import wdc.db.module.Radio;
import wdc.db.module.Suspension;
import wdc.db.module.Turret;
import wdc.db.tank.Equipment;
import wdc.db.tank.Tank;
import wdc.db.tank.Tank.TankType;
import wdc.db.tank.TankRef;
import wdc.util.Conversion;
import wdc.util.Tuple;

/**
 * This is the database crawler. It retrieves Information from the WoT Wiki
 * pages and stores them in the TanksDB.
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Crawler {
    
    /** base URL of the site to retrieve details from */
    public final static String baseURL = "http://wiki.worldoftanks.eu";
    /** US number format (to recognize stuff like "100,000.00" correctly) */
    protected final static NumberFormat format = NumberFormat.getInstance(Locale.US);
    
    /** Decides, if this crawler recieves files from local filesystem or directly from the web */
    protected Source src;
    /** If the local file system is used as source, this is the folder where all the pages are */
    protected String localFolder;
    
    /** the tanks database that is filled by this crawler */
    protected TanksDB db = new TanksDB();
    /** a mapping from tank name to actual tank objects (used to create parent-child relations) */
    protected Map<String,Tank> tankMapping = new HashMap<String, Tank>();
    
    
    /**
     * Crawl the wiki directly from the web
     */
    public Crawler() {
        this.src = Source.URL;
    }
    
    /**
     * Crawls a previously created local copy of the relevant wiki pages
     * (using the Download class)
     * @param localFolder 
     */
    public Crawler(String localFolder) {
        this.src = Source.FILE;
        this.localFolder = localFolder;
    }
    
    /**
     * Generates the tankdb, using the current crawler settings.
     * @return the complete Tank database
     */
    public TanksDB buildTankDB() {
        
        // tanks
        
        System.out.print("Retrieving tank URLs... ");
        List<URL> tankSource = getTankURLs();
        System.out.println("done.");
        
        System.out.println(String.format("\nCrawling detail pages for %s tanks...", tankSource.size()));
        List<Tank> tanks = crawlAllTankDetails(tankSource);
        
        System.out.print("\nCreating parent and child relations... ");
        linkTankRelations(tanks);
        System.out.println("done.");
        
        db.tanks = tanks;
        
        // modules
        
        System.out.println("\nCrawling Modules...");
        db.modules = crawlModules();
        
        return db;
    }
    
    
    // -------------------- URLs (file/http) --------------------
    
    
    /**
     * Generates the URLs for all tank overview lists (http or file)
     * @return 
     */
    public List<URL> getTankOverviewURLs() {
        List<URL> urls = new LinkedList<URL>();
        for (TankType tankType : TankType.values()) {
            try {
                urls.add(buildURL(tankType.getOverviewPage()));
            } catch (IOException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return urls;
    }
    
    /**
     * Generates the URLs for all module overview lists (http or file)
     * @return 
     */
    public List<URL> getModuleOverviewURLs() {
        List<URL> urls = new LinkedList<URL>();
        for (ModuleType m : ModuleType.values()) {
            try {
                urls.add(buildURL(m.getOverviewPage()));
            } catch (IOException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return urls;
    }
    
    /**
     * Generates the URLs for all tanks (http or file)
     * @return URLs to the overview-pages of all tanks
     */
    public List<URL> getTankURLs() {
        List<URL> urls = new LinkedList<URL>();
        
        for (URL overviewPage : getTankOverviewURLs()) {
            try {
                Document overview = Parser.parseHTML(overviewPage);
                Node context = firstXPathResult(overview, "//div[@class=\"mw-content-ltr\"]");
                urls.addAll(crawlTankURLs(context));
            } catch (IOException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return urls;
    }
    
    
    // -------------------- Crawlers --------------------
    
    
    /**
     * Creates a list of URLs for relevant tanks from a tank overview site
     * @param context the relevant context node that holds all lists (an not more,
     * if possible). Currently: div.mw-content-ltr
     * @return List of URLs found on this site
     */
    protected List<URL> crawlTankURLs(Node context) throws MalformedURLException {
        List<URL> urls = new LinkedList<URL>();
        
        List<Node> nodes = evaluateXPath(context, ".//li/a/@href");
        // System.out.println("Tanks in this category: " + nodes.size());
        for (Node node : nodes) {
            // build url (the substring is used to remove the first '/')
            urls.add(buildURL(node.getTextContent().substring(1)));
        }
        
        return urls;
    }
    
    /**
     * crawl all details & build tank mapping
     * @param urls the urls of all tanks
     * @return list of Tank objects
     */
    public List<Tank> crawlAllTankDetails(List<URL> urls) {
        List<Tank> tanks = new ArrayList<Tank>(urls.size());
        
        int counter = 0;
        int max = urls.size();
        
        for (URL tankURL : urls) {
            String name = tankURL.getPath();
            if(src == Source.FILE) {
                name = name.substring(name.lastIndexOf('/') +1, name.length() -5);
            }
            
            try {
                counter++;
                System.out.println(String.format("- Crawling details for tank %s/%s: %s", counter, max, name));
                
                Document tankHtml = Parser.parseHTML(tankURL);
                if(isValidTank(tankHtml)) {
                    Node context = firstXPathResult(tankHtml, "//div[@id=\"Panel\" and @class=\"Tank\"]");
                    Tank tank = crawlTankDetails(context);
                    
                    tanks.add(tank);
                    tankMapping.put(tank.name, tank);
                }
                
            } catch (ParseException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException ex) {
                System.err.println("Crawling of details failed for Tank " + name);
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return tanks;
    }
    
    /**
     * Decides, if the given document contains a valid tank detail page
     * @param wikiPage the detail page of a tank from the wot wiki
     * @return true, iff this page describes a valid tank
     */
    protected boolean isValidTank(Document wikiPage) {
        
        // wiki page nonexistent
        List<Node> context = evaluateXPath(wikiPage, "//div[@id=\"Panel\" and @class=\"Tank\"]");
        if(context.isEmpty()) {
            System.out.println("  -> the page about this tank is empty");
            return false;
        }
        
        // tank removed from game
        List<Node> candidates = evaluateXPath(wikiPage, "//div[@id=\"Panel\"]/p");
        if(isDeprecated(candidates)) {
            System.out.println("  -> this tank was removed from the game");
            return false;
        }
        
        return true;
    }
    
    /**
     * Decides, if one of the given context nodes contains a text pattern 
     * saying that this tank was removed from the game...
     * @param candidates the context nodes that might contain the text
     * @return true, iff one of the nodes contains the text "removed from the game"
     */
    private boolean isDeprecated(List<Node> candidates) {
        for (Node node : candidates) {
            if(node.getTextContent().contains("removed from the game")) {
                return true;
            }
        }
        return false;
    }
    
    // ----------- tank details -----------
    
    /**
     * Reads all details from the specified context node to create a tank object
     * @param context the div that contains all the tank data (class="Tank")
     * @return a complete tank object
     * @throws ParseException thrown if a number cannot be correctly parsed
     * (watch out for additional stuff like units)
     * @throws IllegalAccessException thrown if an enum was not recognized
     * correctly. Watch out for typing and additional characters around the word
     */
    protected Tank crawlTankDetails(Node context) throws ParseException, IllegalAccessException, NumberFormatException {
        
        Tank tank = new Tank();
        
        // basics
        
        tank.name = firstXPathTextResult(context, "./h3").replace("\u00a0","");     // remove nbsp
        tank.id = generateTankID(tank.name);
        tank.nation = BaseProperties.Nation.parse(firstXPathTextResult(context, "./table[1]//td[1]"));
        tank.type = TankType.parse(firstXPathTextResult(context, "./table[1]//td[2]"));
        tank.tier = (byte) Conversion.romanToDecimal(firstXPathTextResult(context, "./table[1]//td[3]").split(" ")[1]);
        
        // prepare for details
        Node context2 = evaluateXPath(context, "./table[2]/tbody").get(0);
        
        
        // battle tier
        tank.battleTierMin = Byte.parseByte(firstXPathTextResult(context2, ".//td[@style=\"background-color:#A29C84;\"]"));
        tank.battleTierMax = (byte) (evaluateXPath(context2, ".//td[@style=\"background-color:#A29C84;\"]").size() + tank.battleTierMin - 1);
        
        // money
        String cost = firstCellAfterHeader(context2, "Cost");
        String costTest = cost.toLowerCase();
        if (costTest.length() < 3 || costTest.contains("error") || costTest.contains("not available")) {
            // no currency value -> regular for free tanks or tanks that are not available for the masses
            tank.cost = 0;
            tank.currency = Currency.Credits;
        } else {
            // gift tank?
            if (cost.toLowerCase().contains("gift")) {
                tank.gift = true;
                tank.cost = format.parse(cost.substring(cost.indexOf('(') + 1, cost.indexOf(')'))).intValue();
            } else {
                tank.cost = format.parse(cost).intValue();
            }
            // currency
            tank.currency = BaseProperties.Currency.parse(evaluateXPath(context2, ".//th[text() = \"Cost\"]/following-sibling::td/img/@alt").get(0).getTextContent());
        }
        
        // crew
        tank.crewMembers = (byte) (evaluateXPath(context2, ".//th[text() = \"Crew\"]/../following-sibling::tr[1]//br").size() + 1);
        
        // speed
        tank.speed = Double.parseDouble(firstCellAfterHeader(context2, "Speed Limit").split(" ")[0]);
        
        // hull
        String[] hull = firstCellAfterHeader(context2, "Hull Armor").split("/");
        tank.hullFront = Double.parseDouble(hull[0]);
        tank.hullSide = Double.parseDouble(hull[1]);
        tank.hullRear = Double.parseDouble(hull[2].split(" ")[0]);
        
        // gunarc
        String[] gunarc = firstCellAfterHeader(context2, "Gun Arc").split("/");
        if(gunarc.length == 1) {
            // usually 360 here, so make it 0-360
            tank.gunArcLeft  = 0;
            tank.gunArcRight = Integer.parseInt(gunarc[0]);
        } else {
            tank.gunArcLeft  = Integer.parseInt(gunarc[0]);
            tank.gunArcRight = Integer.parseInt(gunarc[1]);
        }
        
        // depending equipment
        tank.equipmentStock = crawlTankEquipment(context2, BaseProperties.Development.Stock);
        tank.equipmentTop =   crawlTankEquipment(context2, BaseProperties.Development.Top);
        
        // read parent and children names, link them later
        String base = ".//tr/th[text() = \"%s\"]/following-sibling::td[1]//a/@title";
        
        List<Node> parents = evaluateXPath(context, String.format(base, "Parent"));
        for (Node node : parents) {
            tank.addParentName(node.getTextContent());
        }
        List<Node> children = evaluateXPath(context, String.format(base, "Child"));
        for (Node node : children) {
            tank.addChildName(node.getTextContent());
        }
        
        return tank;
    }
    
    /**
     * Reads the additional equipment, that can be either in the stock- or top-
     * development
     * @param context the context from where the details are read
     * @param dev the development (stock or top)
     * @return all the details of the given development
     */
    protected Equipment crawlTankEquipment(Node context, BaseProperties.Development dev) throws NumberFormatException, ParseException {
        Equipment equip = new Equipment();
        equip.development = dev;
        
        // hitpoints
        equip.hitpoints = format.parse(firstCellAfterHeader(context, "Hit Points", dev)).intValue();
        
        // weight and limit
        String weightLoad = firstCellAfterHeader(context, "Weight Limit", dev);
        equip.weight      = Double.parseDouble(weightLoad.split("/")[0]);
        equip.weightLimit = Double.parseDouble(weightLoad.split("/")[1]);
        
        // elevation
        String[] elevation = firstCellAfterHeader(context, "Elevation Arc", dev).split("/");
        equip.gunElevationLow = Double.parseDouble(elevation[0].replace("--", "-"));       // fix for bug in wiki...
        equip.gunElevationHigh = Double.parseDouble(elevation[1]);
        
        // view
        String view = firstXPathTextResult(context, String.format(".//tr/th[text() = \"View Range\"]/following-sibling::td/span[@class=\"%s\"]/div/text()", dev), "0");
        equip.viewRange = format.parse(view).doubleValue();
        // deprecated
        //equip.viewRange = format.parse(firstCellAfterHeader(context, "View Range", dev)).doubleValue();
        
        return equip;
    }
    
    /**
     * Adds the correct Parent- and Child-Relations to the list of tanks.
     * As the used list implementation is mutable and ephemeral, no return
     * is required, the changes are made in-place...
     * @param tanks 
     */
    protected void linkTankRelations(List<Tank> tanks) {
        
        for (Tank tank : tanks) {
            for (String parentName : tank.parentNames) {
                tank.addParent(tankMapping.get(parentName));
            }
            for (String childName : tank.childrenNames) {
                tank.addChild(tankMapping.get(childName));
            }
        }
        
    }
    
    // ----------- module details -----------
    
    /**
     * Crawls all available modules and writes them into the TanksDB
     * @return the Modules object, containing all modules in the game
     */
    public Modules crawlModules() {
        Modules mods = new Modules();
        
        mods.engines = crawlModuleType(ModuleType.Engine);
        mods.guns = crawlModuleType(ModuleType.Gun);
        mods.radios = crawlModuleType(ModuleType.Radio);
        mods.suspensions = crawlModuleType(ModuleType.Suspension);
        mods.turrets = crawlModuleType(ModuleType.Turret);
        
        return mods;
    }
    
    /**
     * Returns a list of all Modules of the given type. As only Modules of the
     * specified type are crawled, the resulting list could be as well assigned
     * to variables that are typed in the corresponding subclass.
     * Due to restrictions in java generics, this problem cannot be resolved
     * on compile-time...
     * @param type the type of Module to crawl
     * @return list of all modules of the specified type
     */
    protected List crawlModuleType(ModuleType type) {
        List<Module> modules = new ArrayList<Module>(100);
        
        try {
            System.out.println("- Crawling Modules: " + type.getOverviewPage());
            System.out.println("  * Retrieving...");
            
            Document moduleSite = Parser.parseHTML(buildURL(type.getOverviewPage()));
            Node context = firstXPathResult(moduleSite, "//div[@class=\"mw-content-ltr\"]");
            
            System.out.print("  * Parsing... ");

            List<Node> perNation = evaluateXPath(context, ".//div[@class = \"ModuleList\"]");
            for (Node node : perNation) {
                crawlModuleNation(type, modules, node);
            }
            
            System.out.println("done.");

        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return modules;
    }
    
    /**
     * Crawls all Modules from the given context node and associates them with the
     * nation that is found at the context node.
     * Adds all modules to the given (mutable) list, so no return type required...
     * @param type the type of module to crawl
     * @param modules mutable list of modules. results will be added to this
     * @param context from this context node the search is starting
     */
    protected void crawlModuleNation(ModuleType type, List<Module> modules, Node context) {
        String nationString = "";
        try {
            nationString = firstXPathResult(context, "./h3/span/@id").getTextContent();
        } catch (NullPointerException ex) {
            // ignore, for some entries there is no nation defined...
        }
        
        Nation nation = Nation.parseAdvanced(nationString);
        System.out.print(nation.toString() + ".. ");
        
        List<Node> rows = evaluateXPath(context, "./table/tbody/tr[not(@*)]");
        for (Node row : rows) {
            try {
                List<Node> cells = evaluateXPath(row, "./td");
                
                // switching on each row sucks ass, but doing it before makes
                // code massively redundant...
                switch(type) {
                    case Engine: modules.add(crawlSingleEngine(cells, nation)); break;
                    case Gun: modules.add(crawlSingleGun(cells, nation)); break;
                    case Radio: modules.add(crawlSingleRadio(cells, nation)); break;
                    case Suspension: modules.add(crawlSingleSuspension(cells, nation)); break;
                    case Turret: modules.add(crawlSingleTurret(cells, nation)); break;
                    default: System.err.println("Unrecognized Module Type: " + type.toString());
                }
            } catch (ParseException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    // single modules
    
    /**
     * Crawls the details for a single Engine
     * @param cells the cells of the row containing the details of this engine
     * @param nation the nation this engine belongs to
     * @return a completely filled engine object
     * @throws ParseException if a number cannot be parsed
     * @throws IllegalAccessException if an enum cannot be parsed
     */
    protected Engine crawlSingleEngine(List<Node> cells, Nation nation) throws ParseException, IllegalAccessException {
        Engine e = new Engine();
        
        e.nation = nation;
        e.tier = parseTier(cells, 1);
        e.name = parseStringBold(cells, 2);
        e.power = parseInt(cells, 3);
        e.firechance = parseInt(cells, 4);
        
        // gas
        String gas = parseString(cells, 5);
        e.gas = (gas.equals("--")) ? null : Gas.parse(gas);
        
        // cost
        parseCost(cells, 6, e);
        
        // weight
        String weight = parseString(cells, 7);
        e.weight = (weight.equals("--")) ? 0 : format.parse(weight).doubleValue();
        
        // compatibility
        e.compatibility = parseCompatibility(cells, 8);
        
        return e;
    }
    
    /**
     * Crawls the details for a single Gun
     * @param cells the cells of the row containing the details of this engine
     * @param nation the nation this engine belongs to
     * @return a completely filled gun object
     * @throws ParseException if a number cannot be parsed
     */
    protected Gun crawlSingleGun(List<Node> cells, Nation nation) throws ParseException {
        Gun g = new Gun();
        
        g.nation = nation;
        g.tier = parseTier(cells, 1);
        g.name = parseStringBold(cells, 2);
        
        // ammo
        Tuple<String,String> ammo = resolveStringTuple(parseStringBold(cells, 3), "-");
        g.ammoCapacityMin = ammo.fst().equals("?") ? -1 : Integer.parseInt(ammo.fst());
        g.ammoCapacityMax = ammo.snd().equals("?") ? -1 : Integer.parseInt(ammo.snd());
        
        // damage
        g.dmgAP = format.parse(firstXPathTextResult(cells.get(3), "./span[@class = \"ammoAP\"]", "0")).intValue();
        g.dmgAPCR = format.parse(firstXPathTextResult(cells.get(3), "./span[@class = \"ammoAPCR\"]", "0")).intValue();
        g.dmgHE = format.parse(firstXPathTextResult(cells.get(3), "./span[@class = \"ammoHE\"]", "0")).intValue();
        g.dmgHEAT = format.parse(firstXPathTextResult(cells.get(3), "./span[@class = \"ammoHEAT\"]", "0")).intValue();
        
        // penetration
        g.penAP = format.parse(firstXPathTextResult(cells.get(4), "./span[@class = \"ammoAP\"]", "0")).intValue();
        g.penAPCR = format.parse(firstXPathTextResult(cells.get(4), "./span[@class = \"ammoAPCR\"]", "0")).intValue();
        g.penHE = format.parse(firstXPathTextResult(cells.get(4), "./span[@class = \"ammoHE\"]", "0")).intValue();
        g.penHEAT = format.parse(firstXPathTextResult(cells.get(4), "./span[@class = \"ammoHEAT\"]", "0")).intValue();
        
        // fire rate
        Tuple<String,String> fireRate = resolveStringTuple(parseString(cells, 6), "-");
        g.fireRateMin = Double.parseDouble(fireRate.fst());
        g.fireRateMax = Double.parseDouble(fireRate.snd());
        
        // accuracy
        Tuple<String,String> accuracy = resolveStringTuple(parseString(cells, 7), "-");
        g.accuracyMin = Double.parseDouble(accuracy.fst());
        g.accuracyMax = Double.parseDouble(accuracy.snd());
        
        // aim time
        Tuple<String,String> aimTime = resolveStringTuple(parseString(cells, 8), "-");
        g.aimTimeMin = Double.parseDouble(aimTime.fst());
        g.aimTimeMax = Double.parseDouble(aimTime.snd());
        
        // cost
        parseCost(cells, 9, g);
        
        // weight
        String weight = parseString(cells, 10);
        g.weight = (weight.equals("--")) ? 0 : format.parse(weight).doubleValue();
        
        // compatibility
        g.compatibility = parseCompatibility(cells, 11);
        
        return g;
    }
    
    /**
     * Crawls the details for a single Radio
     * @param cells the cells of the row containing the details of this engine
     * @param nation the nation this engine belongs to
     * @return a completely filled radio object
     * @throws ParseException if a number cannot be parsed
     */
    protected Radio crawlSingleRadio(List<Node> cells, Nation nation) throws ParseException {
        Radio r = new Radio();
        
        r.nation = nation;
        r.tier = parseTier(cells, 1);
        r.name = parseStringBold(cells, 2);
        r.range = parseInt(cells, 3);
        parseCost(cells, 4, r);
        
        String weight = parseString(cells, 5);
        r.weight = (weight.equals("--")) ? 0 : format.parse(weight).doubleValue();
        
        r.compatibility = parseCompatibility(cells, 6);
        
        return r;
    }
    
    /**
     * Crawls the details for a single Suspension
     * @param cells the cells of the row containing the details of this engine
     * @param nation the nation this engine belongs to
     * @return a completely filled Suspension object
     * @throws ParseException if a number cannot be parsed
     */
    protected Suspension crawlSingleSuspension(List<Node> cells, Nation nation) throws ParseException {
        Suspension s = new Suspension();
        
        s.nation = nation;
        s.tier = parseTier(cells, 1);
        s.name = parseStringBold(cells, 2);
        s.load = parseDouble(cells, 3);
        s.traverse = parseInt(cells, 4);
        parseCost(cells, 5, s);
        
        String weight = parseString(cells, 6);
        s.weight = (weight.equals("--")) ? 0 : format.parse(weight).doubleValue();
        
        s.compatibility = parseCompatibility(cells, 7);
        
        return s;
    }
    
    /**
     * Crawls the details for a single Turret
     * @param cells the cells of the row containing the details of this engine
     * @param nation the nation this engine belongs to
     * @return a completely filled Turret object
     * @throws ParseException if a number cannot be parsed
     */
    protected Turret crawlSingleTurret(List<Node> cells, Nation nation) throws ParseException {
        Turret t = new Turret();
        
        t.nation = nation;
        t.tier = parseTier(cells, 1);
        t.name = parseStringBold(cells, 2);
        
        // armor
        String[] armor = parseString(cells, 3).split("/");
        if (armor.length == 3) {
            t.armorFront = Double.parseDouble(armor[0]);
            t.armorSide = Double.parseDouble(armor[1]);
            t.armorRear = Double.parseDouble(armor[2]);
        } else {
            System.err.println("Error parsing armor: not enough values");
        }
        
        t.traverse = parseDouble(cells, 4);
        t.viewRange = parseDouble(cells, 5);
        parseCost(cells, 6, t);
        
        String weight = parseString(cells, 7);
        t.weight = (weight.equals("--")) ? 0 : format.parse(weight).doubleValue();
        
        t.compatibility = parseCompatibility(cells, 8);
        
        return t;
    }
    
    // ----------- generic parsers -----------
    
    /**
     * Parses the Tier of an Object.
     * In fact, this method is just a generic roman number parses, that takes
     * a number of cells as input and an index that decides in which cell the
     * roman number can be found.
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the converted roman number (as byte)
     */
    protected byte parseTier(List<Node> cells, int cell) {
        return (byte) Conversion.romanToDecimal(allXPathTextResult(getRelevantContext(cells, cell), "./span/b/text()"));
    }
    
    /**
     * Parses a simple String from a given cell
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the string value of the selected cell
     */
    protected String parseString(List<Node> cells, int cell) {
        return allXPathTextResult(getRelevantContext(cells, cell), "./text()");
    }
    
    /**
     * Parses a simple String in within <b>-tags from a given cell
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the string value (without <b>-tags) of the selected cell
     */
    protected String parseStringBold(List<Node> cells, int cell) {
        return allXPathTextResult(getRelevantContext(cells, cell), "./b/text()");
    }
    
    /**
     * Parses an integer value from a given cell
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the integer value of the selected cell
     * @throws ParseException if the integer cannot be parsed
     */
    protected int parseInt(List<Node> cells, int cell) throws ParseException {
        return format.parse(allXPathTextResult(getRelevantContext(cells, cell), "./text()")).intValue();
    }
    
    /**
     * Parses a double value from a given cell
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the double value of the selected cell
     * @throws ParseException if the double cannot be parsed
     */
    protected double parseDouble(List<Node> cells, int cell) throws ParseException {
        return format.parse(allXPathTextResult(getRelevantContext(cells, cell), "./text()")).doubleValue();
    }
    
    /**
     * Parses the cost and currency of a module and writes them directly in the
     * given Module object (to avoid tuple return values...)
     * @param cells the cells of the row containing the required values
     * @param cell the position (starting by 1) of the required values in the list of cells
     * @throws ParseException if a number cannot be parsed
     */
    protected void parseCost(List<Node> cells, int cell, Module mod) throws ParseException {
        Node context = getRelevantContext(cells, cell);
        
        String currency = firstXPathTextResult(context, "./span/img/@alt").toLowerCase();
        if(currency.contains("credit")) {
            String cost = allXPathTextResult(context, "./text()");
            mod.cost = (cost.equals("--")) ? 0 : format.parse(cost).intValue();
            mod.currency = Currency.Credits;
        } else if(currency.contains("premium")) {
            mod.cost = 0;
            mod.currency = Currency.Premium;
        } else {
            System.err.println("error parsing cost, currency " + currency + " not recognized.");
        }
    }
    
    /**
     * Parses a list of tanks that are compatible to a module
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the list of tank references
     */
    protected List<TankRef> parseCompatibility(List<Node> cells, int cell) {
        List<Node> links = evaluateXPath(getRelevantContext(cells, cell), "./a/@title");
        List<TankRef> compat = new ArrayList<TankRef>(links.size());
        
        for (Node tankName : links) {
            compat.add(getTankRefByName(tankName.getTextContent()));
        }
        
        return compat;
    }
    
    // parser helper
    
    /**
     * Gets the context Node of a specific cell from a list of cells. If the
     * cell contains a <center> tag, it is removed and only it's actual contents
     * are shown.
     * @param cells the cells of the row containing the required value
     * @param cell the position (starting by 1) of the required value in the list of cells
     * @return the relevant context node at the specified position
     */
    private Node getRelevantContext(List<Node> cells, int cell) {
        return firstXPathResult(cells.get(cell-1), "./center", ".");
    }
    
    /**
     * Separates an input string using the specified delimiter and returns
     * a tuple of the first two results of this split. If there is no second
     * value, the first value will be copied to the second position instead.
     * @param input the input string
     * @param delimiter the delimiter to split the string
     * @return a tuple containing the first two values of the split
     */
    protected Tuple<String, String> resolveStringTuple(String input, String delimiter) {
        if (input.contains(delimiter)) {
            String[] ab = input.split(delimiter);
            return Tuple.of(ab[0], ab.length > 1 ? ab[1] : ab[0]);
        } else {
            return Tuple.of(input, input);
        }
    }
    
    /**
     * Returns a tank reference that points to the tank of the specified name
     * @param name the name of the tank to refer to
     * @return the corresponding tank reference
     */
    protected TankRef getTankRefByName(String name) {
        return new TankRef(tankMapping.get(name));
    }
    
    
    
    // -------------------- XPath --------------------
    
    
    
    /**
     * the XPath-Factory can create new XPath queries. No need to initialize
     * it more than once...
     */
    protected final static XPathFactory xpf = XPathFactory.newInstance();
    
    /**
     * Evaluates a given XPath query in the given context (must be an Element,
     * or a Node or something...) and returns a (possibly empty) list of results.
     * @param context the context where to start the query from. Will be ignored
     * of course, if the query starts with / or something. Use . to address the
     * context node.
     * @param expression the XPath expression as String
     * @return list of results (as Node-objects)
     */
    protected List<Node> evaluateXPath(Object context, String expression) {
        
        try {
            // evaluate xpath
            XPath xpath = xpf.newXPath();
            XPathExpression expr = xpath.compile(expression);
            NodeList result = (NodeList) expr.evaluate(context, XPathConstants.NODESET);
            
            // store result
            List<Node> nodes = new ArrayList<Node>(result.getLength());
            for (int i = 0; i < result.getLength(); i++) {
                nodes.add(result.item(i));
            }
            return nodes;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // return null, if xpath expression fails
        return null;
    }
    
    /**
     * Evaluates the given XPath expression and returns the first resulting node.
     * Warning: returns null if no results were found
     * @param context the context where to start the query from
     * @param expression the XPath expression as String
     * @return the first resulting Node
     */
    protected Node firstXPathResult(Object context, String expression) {
        try {
            return evaluateXPath(context, expression).get(0);
        } catch(java.lang.IndexOutOfBoundsException e) {
            //Logger.getLogger(Parser.class.getName()).log(Level.WARNING, null, e);
            // null results are expected...
            return null;
        }
    }
    
    /**
     * Evaluates the XPath expressions in their given order and returns the
     * first resulting node.
     * Warning: returns null if no results were found
     * @param context the context where to start the query from
     * @param expression the XPath expression as String
     * @return the first resulting Node
     */
    protected Node firstXPathResult(Object context, String... expressions) {
        for (String expr : expressions) {
            List<Node> results = evaluateXPath(context, expr);
            if(!results.isEmpty()) {
                return results.get(0);
            }
        }
        return null;
    }
    
    /**
     * Returns the full textual representation of the first resulting Node,
     * including the text contents of all subnodes.
     * @param context the context where to start the query from
     * @param expression the XPath expression as String
     * @return all text contents of the resulting node + subnodes. Trimmed.
     */
    protected String firstXPathTextResult(Object context, String expression) {
        return firstXPathResult(context, expression).getTextContent().trim();
    }
    
    /**
     * Returns the full textual representation of the first resulting Node,
     * including the text contents of all subnodes.
     * If there is no result, the value in returnOnError is returned
     * @param context the context where to start the query from
     * @param expression the XPath expression as String
     * @param returnOnError the value to return if no result is found
     * @return all text contents of the resulting node + subnodes. Trimmed.
     * (or the backup value, in case no result is found)
     */
    protected String firstXPathTextResult(Object context, String expression, String returnOnError) {
        try {
            return firstXPathTextResult(context, expression);
        } catch(NullPointerException ex) {
            return returnOnError;
        }
    }
    
    /**
     * Returns the combined textual representation of all resulting Nodes.
     * Good if you want to append several text() Nodes that are split by
     * other non-text nodes, like
     * &lt;p&gt;Hello, &lt;br/&gt;Mike!&lt;p&gt;
     * @param context the context where to start the query from
     * @param expression the XPath expression as String
     * @return all text contents of all resulting nodes, combined without
     * whitespaces. Result is trimmed.
     */
    protected String allXPathTextResult(Object context, String expression) {
        List<Node> nodes = evaluateXPath(context, expression);
        StringBuilder sb = new StringBuilder(nodes.size());
        for (Node node : nodes) {
            sb.append(node.getTextContent());
        }
        return sb.toString().trim();
    }
    
    /**
     * Special construct: Searches a &lt;th&gt; with text content == head. Picks
     * it's first following sibling &lt;td&gt; and returns it's text content
     * @param context the context where to start the query from
     * @param head the name of the table header to search
     * @return the information associated with this header
     */
    protected String firstCellAfterHeader(Object context, String head) {
        return allXPathTextResult(context, String.format(".//tr/th[text() = \"%s\"]/following-sibling::td[1]/text()", head));
    }
    
    /**
     * Special construct: Searches a &lt;th&gt; with text content == head. Picks
     * it's first following sibling &lt;td&gt; and returns the text content of
     * it's inner span with class == dev
     * @param context the context where to start the query from
     * @param head the name of the table header to search
     * @param dev the development to retrieve (stock or top)
     * @return the information associated with this header as stock or top
     */
    protected String firstCellAfterHeader(Object context, String head, BaseProperties.Development dev) {
        String xpath = String.format(".//tr/th[text() = \"%s\"]/following-sibling::td/span[@class=\"%s\"]/text()", head, dev);
        return allXPathTextResult(context, xpath);
    }
    
    
    
    // -------------------- Helpers --------------------
    
    
    /**
     * Builds the URL of the specified wiki page name, according to the settings
     * of this class (this means, it could return a http-URL pointing to the wot
     * wiki or a file-URL pointing to the local directory where the wiki pages
     * are stored)
     * @param siteName the name of the site in the wot wiki
     * @return the url of this site (file or http)
     * @throws MalformedURLException if there is something heavily wrong with the
     * site name (illegal characters and stuff...)
     */
    protected URL buildURL(String siteName) throws MalformedURLException {
        switch(src) {
            case FILE:
                return new File(localFolder, siteToFileName(siteName)).toURI().toURL();
            case URL:
                return buildWikiLink(siteName);
            default:
                System.err.println("Unknown enum value: " + src.toString());
                return null;
        }
    }
    
    /**
     * Builds the URL of the specified wiki page name, always HTTP
     * @param siteName the name of the site in the wot wiki
     * @return the url of this site (http)
     * @throws MalformedURLException if there is something heavily wrong with the
     * site name (illegal characters and stuff...)
     */
    public static URL buildWikiLink(String siteName) throws MalformedURLException {
        return new URL(String.format("%s/%s", Crawler.baseURL, siteName));
    }
    
    /**
     * Generates a tank ID that conforms to the rules of NCName, as defined by
     * the W3C: http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName
     * the NCName restriction is required for ID and IDREF values
     * @param name the name of the tank
     * @return the NCName conform ID generated from this name
     */
    protected static String generateTankID(String name) {
        return "_" + name.replace(" ", "").replace("(", "").replace(")", "").replace("/", "-").replace(".", "");
    }
    
    /**
     * Adds a ".html" prefix and removes some characters from the sitename,
     * that would make it illegal to store in a filesystem (like / or \)
     * @param siteName the name of the site
     * @return the filename generated from the site name
     */
    public static String siteToFileName(String siteName) {
        return siteName.replace('\\', '_').replace('/', '_') + ".html";
    }
    
    
    /**
     * Decides, if a crawler recieves files from the local filesystem or
     * directly from the web.
     */
    enum Source {
        FILE,
        URL;
    }
    
}
