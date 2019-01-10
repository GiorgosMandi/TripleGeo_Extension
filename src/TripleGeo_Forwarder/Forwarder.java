package TripleGeo_Forwarder;


import org.ini4j.Ini;
import utils.Configuration;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.apache.commons.io.FileUtils;



public class Forwarder {

    public static void main(String[] args) throws IOException {
        System.out.println("\n\n");
        if (args.length == 1) {
            Configuration currentConfig = new Configuration(args[0]) ;

            String dataset_path = currentConfig.dataset_location;
            String geofabrik_areas_file = "./config/geofabrik_areas.ini";
            String config_filename = args[0];
            String produced_config_file = config_filename.substring(0, args[0].lastIndexOf('/')) + "/produced.conf";
            Ini geofabrik_areas_ini = new Ini(new File(geofabrik_areas_file));
            String[] requested_areas = currentConfig.requested_areas.split(";");

            // Checks if the necessary files exist or else it creates them
            if (!Files.exists(Paths.get(dataset_path))) {
                if (!new File(dataset_path).mkdirs())
                    System.out.println("Error:\tCould not create the folder, in which it will store the downloaded datasets");
            }
            if (!Files.exists(Paths.get(geofabrik_areas_file))) {
                Ini_Constructor ini_constructor = new Ini_Constructor(geofabrik_areas_file);
                ini_constructor.Construct_File();
            }

            int today = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).getDayOfYear();
            String[] paths = new String[requested_areas.length];
            boolean resolved;

            //for all the requested areas does the following
            // firstly checks if a recent file exist. if it doesn't, it searches in .ini file to find the url in order
            // to download it. Then produces a copy of  the config file and forwards it to TripleGeo
            long start = System.currentTimeMillis();
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
                        System.out.println("A recent file for \"" + requested_areas[i] + "\" already exists");
                        resolved = true;
                    }
                }

                if (!resolved) {
                    //Downloads and stores the file
                    for (String area : geofabrik_areas_ini.get("Areas").keySet()) {
                        if (clean_area.equals(area)) {
                            resolved = true;
                            String area_url = geofabrik_areas_ini.get("Areas").get(area);
                            System.out.println("Downloads \"" + area_url + "\" for \"" + requested_areas[i] + "\"");
                            System.out.println("Storing file in \"" + paths[i] + "\"");

                            //Downloads and stores the file in the dataset folder
                            try {
                                URL url = new URL(area_url);
                                File dest = new File(paths[i]);
                                FileUtils.copyURLToFile(url,  dest);
                            } catch (Exception e) {
                                System.err.println(e);
                            }

                            break;
                        }
                    }
                }
                if (!resolved){
                    System.out.println("Warning:\tWe couldn't resolve \"" + requested_areas[i] + "\"");
                }
                else {
                    // creates the output directory -- Each requested area's results will be stored on its own directory
                    String outputDir =  (currentConfig.outputDir.charAt(currentConfig.outputDir.length()-1) != '/') ?
                            currentConfig.outputDir + "/" + requested_areas[i] : currentConfig.outputDir + requested_areas[i];
                    if (!Files.exists(Paths.get(outputDir))) {
                        if (!new File(outputDir).mkdirs())
                            System.out.println("Error:\tCould not create the folder, in which it will store the results");
                    }

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
                        else if (line.contains("outputDir"))
                            value = "outputDir = " + outputDir + "\r\n";
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
                        //tripleGeo_process.waitFor();

                        // Prints TripleGeo Errors
                        BufferedReader process_error = new BufferedReader(new InputStreamReader(tripleGeo_process.getErrorStream()));
                        BufferedReader process_output = new BufferedReader(new InputStreamReader(tripleGeo_process.getInputStream()));
                        while(tripleGeo_process.isAlive()) {

                            //Prints TripleGeo output
                            String process_line;
                            while ((process_line = process_output.readLine()) != null) {
                                System.out.println(process_line);
                            }
                        }

                        tripleGeo_process.waitFor();

                        String error_line;
                        while ((error_line = process_error.readLine()) != null) {
                            System.out.println(error_line);
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
            long elapsed = System.currentTimeMillis() - start;

            System.out.println(String.format("Execution time: %d ms.", elapsed));
        }
        else{
            System.out.println("Wrong Input");
        }
    }
}