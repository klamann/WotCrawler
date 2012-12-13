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
package wdc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wdc.xml.Crawler;

/**
 * This class is used to download all relevant pages from the wot wiki and
 * make them available offline, so parsing errors do not force you to download
 * stuff again.
 * This method is highly recommended, as it may save a lot of bandwidth...
 * 
 * @author Sebastian Straub <sebastian-straub@gmx.net>
 */
public class Download {
    
    /** the crawler, used to retrieve and generate the download urls */
    protected static final Crawler cr = new Crawler();
    
    /**
     * Downloads all relevant wiki pages (tank overview, modules and tanks)
     * to the specified folder
     * @param downloadFolder the folder where the html pages are stored
     */
    public static void downloadAll(String downloadFolder) {
        
        System.out.println("Downloading tank overview pages... ");
        downloadTankLists(downloadFolder);
        
        System.out.println("\nDownloading module overview pages... ");
        downloadModules(downloadFolder);
        
        System.out.println("\nDownloading single tank detail pages...");
        downloadTanks(downloadFolder);
        
    }
    
    /**
     * Downloads the tank overview pages to the specified folder
     * @param downloadFolder the folder where the html pages are stored
     */
    public static void downloadTankLists(String downloadFolder) {
        downloadPages(downloadFolder, cr.getTankOverviewURLs());
    }
    
    /**
     * Downloads the module overview pages to the specified folder
     * @param downloadFolder the folder where the html pages are stored
     */
    public static void downloadModules(String downloadFolder) {
        downloadPages(downloadFolder, cr.getModuleOverviewURLs());
    }
    
    /**
     * Downloads the single tank detail pages to the specified folder
     * @param downloadFolder the folder where the html pages are stored
     */
    public static void downloadTanks(String downloadFolder) {
        downloadPages(downloadFolder, cr.getTankURLs());
    }
    
    /**
     * Downloads the files / pages from the specified URLs to the specified folder
     * Converts the name so it does not contain any illegal characters such as / or \
     * @param downloadFolder the folder where the files / pages are stored
     * @param pages the files / pages, as a list of URLs
     */
    public static void downloadPages(String downloadFolder, List<URL> pages) {
        for (URL page : pages) {
            String fsName = Crawler.siteToFileName(page.getPath().substring(1));
            try {
                System.out.println(String.format("Downloading page '%s' to file '%s'", page.getPath(), fsName));
                downloadFile(page, downloadFolder, fsName);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Download.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Downloads a single file to the specified folder with the specified filename
     * @param url the URL from where the file is downloaded
     * @param folder the folder where the file is stored in
     * @param filename the name the file shall have
     * @throws FileNotFoundException thrown if the root folder does not exist
     * @throws IOException thrown if the destination file is locked
     */
    protected static void downloadFile(URL url, String folder, String filename) throws FileNotFoundException, IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(new File(folder, filename));
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.flush();
        fos.close();
        rbc.close();
    }
    
}
