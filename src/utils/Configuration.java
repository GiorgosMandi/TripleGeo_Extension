package utils;

/*
 *
 * This file is partly copied and modified by TripleGeo Configuration.java
 *
 *
 * @(#) Configuration.java 	 version 1.6   2/3/2018
 *
 * Copyright (C) 2013-2018 Information Systems Management Institute, Athena R.C., Greece.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Parser of user-specified configuration files to be used during transformation of geospatial features into RDF triples.
 *
 * @author Kostas Patroumpas
 * @version 1.6
 */

/* DEVELOPMENT HISTORY
 * Initially implemented for geometry2rdf utility (source: https://github.com/boricles/geometry2rdf/tree/master/Geometry2RDF)
 * Modified by: Kostas Patroumpas, 8/2/2013; adjusted to TripleGeo functionality
 * Last modified: 2/3/2018
 */
public final class Configuration {

    /**
     * Format of input data. Supported formats: SHAPEFILE, DBMS, CSV, GPX, GEOJSON, XML, OSM.
     */
    public String inputFormat;

    /**
     * Path to a properties file containing all parameters used in the transformation.
     */
    private String path;

    /**
     * Path to file(s) containing input dataset(s).
     * Multiple input files (of exactly the same format and attributes) can be specified, separating them by ';' in order to activate multiple concurrent threads for their transformation.
     */
    public String inputFiles;

    /**
     * Path to TripleGeo jar file.
     */
    public String tripleGeo_jar;

    /**
     * the areas that their datasets will be forwarded to TripleGeo
     */
    public String  requested_areas;

    /**
     * the path to the folder that datasets will be stored
     */
    public String  dataset_location;
    /**
     * Constructor of a Configuration object.
     * @param path   Path to a properties file containing all parameters to be used in the transformation.
     */
    public Configuration(String path){
        this.path = path;
        buildConfiguration();
    }

    /**
     * Loads the configuration from a properties file.
     */
    private void buildConfiguration() {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException io) {
            System.out.println(Level.WARNING + " Problems loading configuration file: " + io);
        }
        initializeParameters(properties);
    }


    /**
     * Examines whether a given string is null or empty (blank).
     * @param text  The input string
     * @return  True if the parameter is null or empty; otherwise, False.
     */
    private boolean isNullOrEmpty(String text) {

        return text == null || text.equals("");
    }

    /**
     * Initializes all the parameters for the transformation from the configuration file.
     *
     * @param properties    All properties as specified in the configuration file.
     */
    private void initializeParameters(Properties properties) {

        //Format of input data: SHAPEFILE, DBMS, CSV, GPX, GEOJSON, XML
        if (!isNullOrEmpty(properties.getProperty("inputFormat"))) {
            inputFormat = properties.getProperty("inputFormat").trim();
        }

        //File specification properties
        if (!isNullOrEmpty(properties.getProperty("inputFiles"))) {
            inputFiles = properties.getProperty("inputFiles").trim();
        }

        //Path to tripleGeo jar file
        if (!isNullOrEmpty(properties.getProperty("tripleGeo_jar"))) {
            tripleGeo_jar = properties.getProperty("tripleGeo_jar").trim();
        }

        // the areas that their datasets will be forwarded to TripleGeo
        if (!isNullOrEmpty(properties.getProperty("requested_areas"))) {
            requested_areas = properties.getProperty("requested_areas").trim();
        }
        // the areas that their datasets will be forwarded to TripleGeo
        if (!isNullOrEmpty(properties.getProperty("dataset_location"))) {
            dataset_location = properties.getProperty("dataset_location").trim();
        }
    }

}

