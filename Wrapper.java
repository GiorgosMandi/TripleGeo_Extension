
package eu.slipo.athenarc.triplegeo;



import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.List;


public class Wrapper {


    private static Path dataset_path = Paths.get(System.getProperty("user.dir") + "/test/data/downloaded/");
    private static String requested_area;
    private static String geofabrik_url = "https://download.geofabrik.de/";

    private static boolean search_geofabrik_tables(String url, int level){

        try {
            Document doc = Jsoup.connect(url).get();
            Element region_table = doc.getElementById("subregions");
            Element special_region_table = null;

            if (level != 1) {
                Elements headers = doc.getElementsByTag("h3");
                for( Element header : headers) {
                    if (header.text().equals("Sub Regions")) {
                        region_table = header.nextElementSibling().nextElementSibling();
                        if (region_table == null || !region_table.tag().toString().equals("table"))
                            return false;

                    }
                    else if (header.text().equals("Special Sub Regions"))
                        special_region_table = header.nextElementSibling().nextElementSibling();
                }
            }

            for(int i=0; i<2; i++) {

                Element table;
                if(i == 0) table = region_table;
                else table = special_region_table;
                if (table == null) continue;


                Elements rows;
                List<Element> continents;
                try {
                    rows = table.select("tr");
                    continents = rows.subList(2, rows.size());
                }
                catch (java.lang.NullPointerException e){
                    continue;
                }

                for (Element continent : continents) {
                    Elements anchors_continent = continent.select("a");

                    System.out.println("Recursion " + level + "\n" + anchors_continent.get(0).text());

                    if (anchors_continent.get(0).text().toUpperCase().equals(requested_area.toUpperCase())) {

                        // found the url and downloads the file
                        String file_url = geofabrik_url + anchors_continent.get(1).attr("href");

                        System.out.println("Downloads from " + file_url);
                       /*Connection.Response resultImageResponse = Jsoup.connect(file_url)
                                .ignoreContentType(true).execute();

                        //stores the file in the dataset folder
                        FileOutputStream out = (new FileOutputStream(new java.io.File(file_path)));
                        out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
                        out.close();*/

                        return true;
                    } else {

                        String new_url = geofabrik_url + anchors_continent.get(0).attr("href");
                        System.out.println("\n");
                        if (search_geofabrik_tables(new_url, level + 1, writer))
                            return true;
                    }
                }
            }
        }
        catch (java.io.IOException ignored) { }

        return false;
    }


    public static void main(@NotNull String[] args)  {

        requested_area = args[args.length -1].toLowerCase().replace(" ", "_");

        String file_path = dataset_path.toString() + "/" + requested_area + ".osm.pbf";
        boolean recent_file_exist = false;

        //Checks whether the file exist AND if it is a recent one
        //firstly checks if there is a folder..then checks if the file exists
        if (!Files.exists(dataset_path))
            new File(dataset_path.toString()).mkdirs();
        else{
            if (Files.exists(Paths.get(file_path))){
                // first check its creation date

                Instant creation_date = null;
                try {
                    creation_date = Files.getLastModifiedTime(Paths.get(file_path)).toInstant();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                int creation_day = LocalDateTime.ofInstant(creation_date, ZoneOffset.UTC).getDayOfYear();
                int today = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).getDayOfYear();

                System.out.println(today + " -- " + creation_day);
                if (creation_day == today)
                    recent_file_exist = true;
            }
        }

        if (!recent_file_exist) {
            boolean found = search_geofabrik_tables(geofabrik_url, 1, writer);
            System.out.println("FOUND? " + found);
        }

        System.out.println("--------------------------------------\n\n");

        // Extractor extractor = new Extractor();
        // extractor.main(args);
    }
}
