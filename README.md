<h1>TripleGeo Forwarder</h1>

TripleGeo_Forwarder’s primary purpose is to massively convert OSM data into RDF Triples. This is achieved by downloading the datasets of the requested areas and forwarding them to TripleGeo which will convert them into RDF triples. Therefore, It is an add-on of TripleGeo that simplifies the conversion of OSM data by automating the process of collecting and storing it, and by executing TripleGeo for a series of datasets.


**Input - Configuration Settings**

TripleGeo requires a configuration file as its input. For TripleGeo_Forwarder purposes, in this configuration file user can specify his requested regions in the “requested_areas” field separated by semicolons(“;”). Furthermore he must declare the path to his TripleGeo jar file in the “tripleGeo_jar” field, in order Forwarder to be able to locate it. Those two fields are required for the execution of TripleGeo_Forwarder.


**Geofabrik’s Download Server**

In https://download.geofabrik.de/ is located Geofabrik’s free download server. This server contains data extracts from the OpenStreetMap project which are normally updated every day. This server is used in order to download the datasets of the requested regions.



**Execution**
java -cp \<path to JAR file\> TripleGeo_Forwarder.Forwarder \<path to the configuration file\>