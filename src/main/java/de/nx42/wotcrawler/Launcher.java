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
package de.nx42.wotcrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import de.nx42.wotcrawler.db.TanksDB;
import de.nx42.wotcrawler.ext.Evaluator;
import de.nx42.wotcrawler.ext.FieldDef;
import de.nx42.wotcrawler.util.Download;
import de.nx42.wotcrawler.xml.Crawler;
import de.nx42.wotcrawler.xml.Serializer;
import de.nx42.wotcrawler.xml.Transformer;

/**
 * Command Line Parser and Program Launcher
 *
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    /**
     * Launches the program
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Launcher l = new Launcher(args);
        l.launch();
    }

    @Parameter(names = { "-h", "--help" }, description = "Print this help text")
    protected boolean help;

    @Parameter(names = { "-s", "--schema" }, description = "Generate an xml schema file. "
            + "Please specify the full path (including filename) of the new schema.")
    protected String schema;

    protected String[] args;

    protected final Command[] commands = {
        new CommandDownload(),
        new CommandCrawl(),
        new CommandEvaluate(),
        new CommandExport(),
        new CommandRunall()
    };

    /**
     * Initializes the Launcher and Command Line Parser
     * @param args the command line args, as provided by the main method
     */
    public Launcher(String[] args) {
        this.args = args;
    }

    /**
     * Parses command line args and launches the requested program parts.
     */
    public void launch() throws ParameterException {

        if(args.length < 1) {
            System.out.println("No arguments specified. Type --help for more information.");
            return;
        }

        // setup
        JCommander jc = new JCommander(this);
        jc.setProgramName("java -jar wotcrawler.jar");

        for(Command c : commands) {
            jc.addCommand(c.cmd1, c, c.cmd2);
        }

        // parse
        try {
            jc.parse(args);

            if (this.help) {
                jc.usage();
            }
            if (this.schema != null) {
                Serializer.generateSchema(this.schema);
            }
            if (jc.getParsedCommand() != null) {
                String parsed = jc.getParsedCommand();

                for (Command c : commands) {
                    if(parsed.equals(c.cmd1)) {
                        c.launch();
                    }
                }
            }
        } catch (ParameterException e) {
            System.err.println("Could not correctly parse command line args: " + e.getMessage() +
                    "\nPlease type -h or --help to get usage information.");
        }
    }


    /**
     * Abstract command definition
     * (A command is part of the command line args and usually entered without
     * a preceding -, as in git commit [args]; commit is the command)
     */
    protected abstract class Command {

        /** command identifier */
        public final String cmd1;
        /** second command identifier */
        public final String cmd2;

        /**
         * Sets the command identifiers
         * @param cmd1 first identifiers
         * @param cmd2 second identifiers
         */
        public Command(String cmd1, String cmd2) {
            this.cmd1 = cmd1;
            this.cmd2 = cmd2;
        }

        /**
         * Launches the action behind this command
         */
        public abstract void launch();

    }

    /**
     * Download-Command. Downloads all relevant Wiki-Pages into a local folder
     */
    @Parameters(commandDescription = "Download source data for future crawling")
    protected class CommandDownload extends Command {

        public CommandDownload() {
            super("download", "dl");
        }

        /** The folder where the wiki pages will be downloaded in */
        @Parameter(names = { "-f", "--folder" }, required = true,
                description = "The folder where the wiki pages will be downloaded in.")
        protected String folderDownload;

        /**
         * Downloads all relevant Wiki-Pages into the specified local folder
         */
        @Override
        public void launch() {
            Download.downloadAll(folderDownload);
        }

    }

    /**
     * Crawl Command. Reads the data from the wiki pages and stores it in an xml
     * database.
     */
    @Parameters(commandDescription = "Crawl through Wiki-pages, generate Tank Database (xml)")
    protected class CommandCrawl extends Command {

        public CommandCrawl() {
            super("crawl", "cr");
        }

        /**
         * If specified, the files from this local folders are used.
         * Else they are downloaded on the fly.
         */
        @Parameter(names = { "-l", "--local" }, description = "Uses the html files "
                + "downloaded in the specified local folder to build the database. "
                + "Downloads stuff directly, if not specified.")
        protected String folderLocal;

        /** The file where the xml database will be stored */
        @Parameter(names = { "-db", "--database" }, required = true,
                description = "Write the database into this xml file. "
                + "Please specify the full path (including filename) of the new xml.")
        protected String dbFile;

        /**
         * Reads the data from the wiki pages and stores it in an xml database.
         */
        @Override
        public void launch() {
            Crawler cr = new Crawler(folderLocal);
            Serializer.serialize(TanksDB.class, cr.buildTankDB(), new File(dbFile));
        }

    }

    /**
     * Evaluate Command. Analyzes a given database, creates an error log.
     */
    @Parameters(commandDescription = "Evaluate an existing database, show detailed error messages")
    protected class CommandEvaluate extends Command {

        public CommandEvaluate() {
            super("evaluate", "ev");
        }

        /** The XML database to evaluate */
        @Parameter(names = { "-src", "--source" }, required = true,
                description = "The XML database to evaluate.")
        protected String dbFile;

        /** The corresponding xml schema file to check that the database is valid */
        @Parameter(names = { "-sch", "--schema" },
                description = "Optional: The corresponding xml schema file to "
                + "check that the database is valid.")
        protected String schema;

        /** if set, the report will be written in this file (else on the command line) */
        @Parameter(names = { "-r", "--report" },
                description = "Optional: Specify a file to write the report into.")
        protected String report;

        /**
         * Analyzes a given database, creates an error log
         */
        @Override
        public void launch() {
            TanksDB db;

            if (schema != null) {
                // use schema to validate
                db = Serializer.deserialize(TanksDB.class, new File(dbFile), new File(schema));
            } else {
                // ignore schema
                db = Serializer.deserialize(TanksDB.class, new File(dbFile));
            }

            if (report != null) {
                // write to file
                try {
                    PrintWriter out = new PrintWriter(report);
                    out.write(Evaluator.writeReportOf(db));
                    out.flush();
                    out.close();
                } catch (FileNotFoundException ex) {
                    log.error("Writing of report to file failed", ex);
                }
            } else {
                // print
                Evaluator.printReportOf(db);
            }

        }

    }

    /**
     * Export Command. Transforms and exports the data from the database in html tables.
     */
    @Parameters(commandDescription = "Generates HTML tables from the given database.")
    protected class CommandExport extends Command {

        public CommandExport() {
            super("export", "ex");
        }

        /** The XML database to use as source */
        @Parameter(names = { "-src", "--source" }, required = true,
                description = "The XML database to use as source.")
        protected String dbFile;

        /** The corresponding xml schema file to check that the database is valid */
        @Parameter(names = { "-sch", "--schema" },
                description = "Optional: The corresponding xml schema file to "
                + "check that the database is valid.")
        protected String schema;

        /** Path to store the detailed table in */
        @Parameter(names = { "-td", "--detailed" },
                description = "Generates one big HTML table containing all raw data")
        protected String detailed;

        /** Path to store the module-table in */
        @Parameter(names = { "-tm", "--modules" },
                description = "Generates one table with all tanks and one for each module "
                + "type, with links between tanks and modules.")
        protected String modules;

        /** Path to store the rating-table in */
        @Parameter(names = { "-tr", "--rating" },
                description = "Generates one table for each tank type, comparing "
                + "each tank to it's competitors.")
        protected String rating;

        /**
         * Transforms and exports the data from the database in html tables
         */
        @Override
        public void launch() {

            // build database
            TanksDB db;
            if (schema != null) {
                db = Serializer.deserialize(TanksDB.class, new File(dbFile), new File(schema));
            } else {
                db = Serializer.deserialize(TanksDB.class, new File(dbFile));
            }

            // export
            Transformer tr = new Transformer(db);
            if(detailed != null) {
                tr.writeTableTank(detailed, FieldDef.detailed_Combined);
            }
            if(modules != null) {
                tr.writeTablesLinked(modules);
            }
            if(rating != null) {
                tr.writeRatingTable(rating);
            }
        }

    }

    /**
     * Command Runall. Runs all at once: Download, Crawl, Export. Fire and forget...
     */
    @Parameters(commandDescription = "Runs all at once: Download, Crawl, Export. Fire and forget...")
    protected class CommandRunall extends Command {

        public CommandRunall() {
            super("runall", "ra");
        }

        /** The folder where all the results will be stored */
        @Parameter(names = { "-o", "--output" }, required = true,
                description = "The folder where all the results will be stored")
        protected String folderOutput;

        /**
         * Tries to warn the user, before the actual work starts...
         */
        @Override
        public void launch() {

            System.out.print("Using this function is not recommended for the following reason:\n"
                    + "In the next step, about 350 wiki pages will be downloaded and "
                    + "a database will be created from them. If anything "
                    + "goes wrong, the data may be useless, and you have to download everything "
                    + "again, which is not very nice towards the server administrator.\n"
                    + "The better approach is to use the commands 'download', 'crawl', "
                    + "'evaluate' and 'export' one at a time, so you always know where "
                    + "things go wrong, without losing any data.\n"
                    + "So, are you sure you wanna run this? (y/n) ");

            Scanner sc = new Scanner(System.in);
            String input;
            boolean abort = false;

            while (!abort) {
                input = sc.next();
                if (input.charAt(0) == 'y') {
                    abort = true;
                    System.out.println("All right, your choice. Here we go...");
                    sc.close();
                    run();
                } else if (input.charAt(0) == 'n') {
                    abort = true;
                    System.out.println("Wise choice :)\nCall --help, if you are not sure how to continue");
                    sc.close();
                } else {
                    System.out.print("Don't write a poem, just press 'y' or 'n': ");
                }
            }


        }

        /**
         * Run everything
         */
        protected void run() {

            // crawl
            Crawler cr = new Crawler();
            TanksDB db = cr.buildTankDB();

            // report
            //Evaluator.printReportOf(db);

            // serialize
            Serializer.generateSchema(folderOutput + "/tanks.xsd");
            Serializer.serialize(TanksDB.class, db, new File(folderOutput, "tanks.xml"));

            // export
            Transformer tr = new Transformer(db);
            tr.writeTableTank(folderOutput + "/table-detailed.html", FieldDef.detailed_Combined);
            tr.writeTablesLinked(folderOutput + "/table-linked.html");
            tr.writeRatingTable(folderOutput + "/table-rating.html");

        }

    }

}
