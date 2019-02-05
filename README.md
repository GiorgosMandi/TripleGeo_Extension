<h1>TripleGeo Forwarder</h1>

TripleGeo_Forwarder’s primary purpose is to massively convert OSM data into RDF Triples. This is achieved by downloading the datasets of the requested areas and forwarding them to TripleGeo which will convert them into RDF triples. Therefore, It is an add-on of TripleGeo that simplifies the conversion of OSM data by automating the process of collecting and storing it, and by executing TripleGeo for a series of datasets.


**Input - Configuration Settings**

TripleGeo requires a configuration file as its input. For TripleGeo_Forwarder purposes, the user must initialise some fundamental variables in the configuration file. 
 
* The requested regions are declared in the “**requested_areas**” field separated by semicolons(“;”).
* The users must specify the location of TripleGeo jar file in the “**tripleGeo_jar**” field
* The location in which the downloaded datasets will be stored is specified in the "**dataset_location**" field.
* The "**inputFile**" field must be empty.

Those fields are required to be initialised inside the configuration file, for the execution of TripleGeo_Forwarder. Also the configuration file must contain all the necessary fields for the execution of TripleGeo


**Geofabrik’s Download Server**

In https://download.geofabrik.de/ is located Geofabrik’s free download server. This server contains data extracts from the OpenStreetMap project which are normally updated every day. This server is used in order to download the datasets of the requested regions.



**Execution**
java -cp \<path to JAR file\> TripleGeo_Forwarder.Forwarder \<path to the configuration file\>