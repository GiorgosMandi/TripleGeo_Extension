<h2>TripleGeo_Extension</h2>

**Wrapper** It is a wrapper of TripleGeo that downloads the datasets of the
 requested areas, from geofabrik, and forwards them to TripleGeo. 

**Ini_Constructor**  constructs the geofabrik_areas.ini file. It is called by Wrapper in case
 the geofabrik_areas.ini does not exist. 


**Execution** <br/>
java -cp ./target/TripleGeo_Extension-1.6-SNAPSHOT.jar TripleGeo_extension.Wrapper <path to .conf file>  <series of areas>
