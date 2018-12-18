<h1>TripleGeo Forwarder</h1>

TripleGeo_Forwarder is a wrapper of TripleGeo which downloads the requested by the user datasets from geofabrik and then forwards them to TripleGeo. 

**Input - Configuration Settings**

As it is already mentioned, TripleGeo requires a configuration file as its input. For TripleGeo_Forwarder purposes, in this configuration file user can specify his requested regions in the “requested_areas” field separated by dashes(“-”). Furthermore he must initialize the path to his TripleGeo jar file in the “tripleGeo_jar” field, in order wrapper would be able to locate it. Those two fields are necessary to be filled for the execution of TripleGeo_Forwarder.


**Geofabrik’s Download Server**

In https://download.geofabrik.de/ is located Geofabrik’s free download server. This server has data extracts from the OpenStreetMap project which are normally updated every day. In this implementation we use this server in order to download the datasets for the requested regions, before we forward them to TripleGeo.


**Ini_Constructor**

In order to avoid repeatedly searching and using the geofabrik web page, we constructed the geofabrik_area.ini file. This file contains the labels of all the regions that exist in geofabrik and the links to their datasets. So instead of searching in the webpage to find the requested areas, we search in geofabrik_area.ini and we download the dataset from the corresponding link. 

This file is constructed by Ini_Constructor.java which uses web scraping techniques and stores the information of all the tables that exist in https://download.geofabrik.de/  to geofabrik_area.ini. In a few words, the function search_geofabrik_tables firstly locates the main html table that contains the regions, then for each entity in the html table stores its label and its download link and calls the search_geofabrik_tables with the url of the webpage that contains its subregions. Hence, recursively stores the information of all subregions of every region to the geofabrik_area.ini.

**Forwarder**

This routine firstly reads from the configuration file the path to TripleGeo’s jar file and the requested regions. For every region it checks whether an already downloaded dataset exist and if it is a recent one. In case it doesn’t exist or it is an old one, it searches in geofabrik_area.ini file and downloads the datasets from the corresponding links. When it complete downloading the dataset, it creates a duplicate of the configuration file and modifies the fields that are relevant with datasets’	 properties. Then it executes TripleGeo in a new process, giving it as an argument the new configuration file. This procedure is executed for every requested area and the results will be stored in the location that was defined in the configuration file.
