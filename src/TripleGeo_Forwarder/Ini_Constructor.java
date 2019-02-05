package TripleGeo_Forwarder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.List;


class Ini_Constructor {

    private String geofabrik_url = "http://download.geofabrik.de/";
    private String config_location;


    Ini_Constructor(String location){
        config_location = location;
    }


    private void search_geofabrik_tables(String url, PrintWriter writer, int level) {
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
                            return;
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

                    String area_label = anchors_continent.get(0).text().replace(" ", "_").toLowerCase();
                    String area_url = url.substring(0, url.lastIndexOf('/')) + "/" + anchors_continent.get(1).attr("href");

                    writer.println(area_label + " = " + area_url);
                    System.out.println(area_label + " = " + area_url + "\n\n");
                    String next_area_url = url.substring(0, url.lastIndexOf("/")+1) + anchors_continent.get(0).attr("href");
                    search_geofabrik_tables(next_area_url, writer, level+1);
                }
            }
        }
        catch (java.io.IOException ignored) { }
    }


    void Construct_File() throws IOException {

        PrintWriter writer = new PrintWriter(config_location);
        writer.println("[Areas]");
        search_geofabrik_tables(geofabrik_url, writer, 1);
        writer.close();

    }
}
