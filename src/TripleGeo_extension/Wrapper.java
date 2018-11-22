package TripleGeo_extension;

import org.ini4j.Ini;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;


public class Wrapper {


    private static Path dataset_path = Paths.get(System.getProperty("user.dir") + "/datasets/");
    private static Path tripleGEO_Path = Paths.get(System.getProperty("user.dir") + "../TripleGeo");
    private static String geofabrik_areas_file = dataset_path.toString() + "/geofabrik_areas.ini";


    public static void main(String[] args) throws IOException {
        
        if (args.length >= 2) {

            Ini geofabrik_areas_ini = new Ini(new File(geofabrik_areas_file));
            String config_filename = args[0];
            String[] requested_areas = Arrays.copyOfRange(args, 1, args.length);

            // Checks if the folder exist
            if (!Files.exists(dataset_path))
                new File(dataset_path.toString()).mkdirs();

            int today = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).getDayOfYear();
            String[] paths = new String[requested_areas.length];
            boolean resolved;

            //firstly checks if a recent file exist, if it doesn't, it searches in .ini file to find the url in order
            // to download it
            for (int i = 0; i < requested_areas.length; i++) {
                System.out.println( "\n\n\n" + requested_areas[i] );
                resolved = false;
                paths[i] = dataset_path.toString() + "/" + requested_areas[i] + ".osm.pbf";
                String clean_area = requested_areas[i].toLowerCase().replace(" ", "_");

                if (Files.exists(Paths.get(paths[i]))) {
                    // firstly checks its creation date
                    Instant creation_date = null;
                    try {
                        creation_date = Files.getLastModifiedTime(Paths.get(paths[i])).toInstant();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    int creation_day = LocalDateTime.ofInstant(creation_date, ZoneOffset.UTC).getDayOfYear();

                    if (creation_day == today) {
                        System.out.println("Recent file for \"" + requested_areas[i] + "\" exists");
                        resolved = true;
                    }
                }

                if (!resolved) {

                    for (String area : geofabrik_areas_ini.get("Areas").keySet()) {
                        if (clean_area.equals(area)) {
                            resolved = true;
                            String area_url = geofabrik_areas_ini.get("Areas").get(area);
                            System.out.println("Downloads from " + area_url);

                            // Downloads and stores the file in the dataset folder
                            Connection.Response resultImageResponse = Jsoup.connect(area_url).ignoreContentType(true).execute();
                            FileOutputStream out = (new FileOutputStream(new java.io.File(paths[i])));
                            out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
                            out.close();
                            break;
                        }
                    }
                }
                if (!resolved){
                    System.out.println("We couldn't resolve " + requested_areas[i]);
                }
                else {
                    // rewrite the given configuration file changing its inputfile and its inputformat
                    File config_file = new File(config_filename);
                    BufferedReader reader = new BufferedReader(new FileReader(config_file));
                    String line;
                    StringBuilder new_text = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        String value;
                        if (line.contains("inputFormat"))
                            value = "inputFormat = OSM_PBF" + "\r\n";
                        else if (line.contains("inputFiles"))
                            value = "inputFiles = " + paths[i] + "\r\n";
                        else
                            value = line + "\r\n";
                        new_text.append(value);
                    }
                    reader.close();
                    FileWriter writer = new FileWriter("./test/conf/produced.conf");
                    writer.write(new_text.toString());
                    writer.close();

                }
            }

        }
        else{
            System.out.println("Wrong Input");
        }
    }
}