package TripleGeo_extension;


import org.ini4j.Ini;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import utils.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;




public class Wrapper {

    public static void main(String[] args) throws IOException {
        System.out.println("\n\n");
        if (args.length >= 2) {
            Configuration currentConfig = new Configuration(args[0]) ;

            String dataset_path = "./datasets/";
            String geofabrik_areas_file = "./config/geofabrik_areas.ini";
            String produced_config_file = args[0].substring(0, args[0].lastIndexOf('/')) + "/produced.conf";


            // Checks if the necessary files exist or else it creates them
            if (!Files.exists(Paths.get(dataset_path))) {
                if (!new File(dataset_path).mkdirs())
                    System.out.println("Error: Cant create folder to store the downloaded datasets");

            }
            if (!Files.exists(Paths.get(geofabrik_areas_file))) {
                Ini_Constructor ini_constructor = new Ini_Constructor(geofabrik_areas_file);
                ini_constructor.Construct_File();
            }

            Ini geofabrik_areas_ini = new Ini(new File(geofabrik_areas_file));
            String config_filename = args[0];
            String[] requested_areas = Arrays.copyOfRange(args, 1, args.length);


            int today = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).getDayOfYear();
            String[] paths = new String[requested_areas.length];
            boolean resolved;

            //firstly checks if a recent file exist. if it doesn't, it searches in .ini file to find the url in order
            // to download it
            for (int i = 0; i < requested_areas.length; i++) {
                resolved = false;
                paths[i] = dataset_path + requested_areas[i] + ".osm.pbf";
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
                        System.out.println("Recent file for \"" + requested_areas[i] + "\" already exists");
                        resolved = true;
                    }
                }

                if (!resolved) {
                    for (String area : geofabrik_areas_ini.get("Areas").keySet()) {
                        if (clean_area.equals(area)) {
                            resolved = true;
                            String area_url = geofabrik_areas_ini.get("Areas").get(area);
                            System.out.println("Downloads from \"" + area_url + "\" for \"" + requested_areas[i] + "\"");
                            System.out.println("Location: \"" + paths[i] + "\"");

                            //Downloads and stores the file in the dataset folder
                            Connection.Response resultImageResponse = Jsoup.connect(area_url).ignoreContentType(true).execute();
                            FileOutputStream out = (new FileOutputStream(new java.io.File(paths[i])));
                            out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
                            out.close();
                            break;
                        }
                    }
                }
                if (!resolved){
                    System.out.println("Warning:\tWe couldn't resolve \"" + requested_areas[i] + "\"");
                }
                else {
                    // modifies the given configuration file -- changes its inputfile and its inputformat
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
                    FileWriter writer = new FileWriter(produced_config_file);
                    writer.write(new_text.toString());
                    writer.close();

                    try {
                        System.out.println("Executing TripleGeo for \"" + requested_areas[i] + "\"\n\n");
                        System.out.println("==================\tTripleGeo\t==================\n");

                        // Executes TripleGeo in a new process
                        Process tripleGeo_process = Runtime.getRuntime()
                                .exec("java -cp " + currentConfig.tripleGeo_jar + " eu.slipo.athenarc.triplegeo.Extractor " + produced_config_file);
                        tripleGeo_process.waitFor();


                        // Prints TripleGeo Errors
                        BufferedReader process_error = new BufferedReader(new InputStreamReader(tripleGeo_process.getErrorStream()));
                        String error_line;
                        while ((error_line = process_error.readLine()) != null) {
                            System.out.println(error_line);
                        }

                        //Prints TripleGeo output
                        BufferedReader process_output = new BufferedReader(new InputStreamReader(tripleGeo_process.getInputStream()));
                        String process_line;
                        while ((process_line = process_output.readLine()) != null) {
                            System.out.println(process_line);
                        }
                        process_output.close();



                        System.out.println("\n=============================================================\n");
                        if (tripleGeo_process.exitValue() != 0 )
                            System.out.println("Error:\tExecution Failed\n\n");
                        else
                            System.out.println("Execution completed Successfully\n\n");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //deletes the produced file
                    Files.deleteIfExists(Paths.get(produced_config_file));
                }
            }

        }
        else{
            System.out.println("Wrong Input");
        }
    }
}