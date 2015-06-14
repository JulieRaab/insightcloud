/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalglobe.insight.vector;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * ********************INCOMPLETE CLASS************************ This class
 * takes a BBOX, uses the DigitalGlobe Unified Vector Index aggregation services
 * to box the area and get a data source histogram. It then queries for subtypes
 * of each source and creates what amounts to an N DImensional vector space on a
 * spatial grid (once complete)
 *
 * @author mgiaconia
 */
public class DGSpatialStatsWorkflow {

  public static void run(String[] args) {
    try {
      args = new String[]{"example.properties"};

      if (args.length < 1) {
        throw new RuntimeException("Configuration file must be specified.");
      }

      ServiceProperties props = new ServiceProperties(args[0]);

      // authentication information
      String authService = props.getAuthService();
      String username = "un";
      String password = "pw";

      // the base URL for accessing the vector service
      String appService = props.getAppService();
      String urlBase = props.getUrlBase();
      String appBase = appService + urlBase;

      // set up the client
      VectorRestClient client = new VectorRestClient();
      client.setAuthService(authService);
      client.setAppService(appService);

      System.out.println("Authenticating with the application. . . .");
      client.authenticate(username, password);

      Gson gson = new Gson();
      SortedSet<String> cols = new TreeSet<>();
      List<String> boxes = getBboxes();
      Map<String, SortedMap<String, Double>> output = new TreeMap<>();
      int counter = 0;
      for (String bbox : boxes) {
        try {
          counter++;
          if (counter > 10) {
            break;
          }
          String[] parts = bbox.split(",");
          double n = Double.valueOf(parts[0]);
          double s = Double.valueOf(parts[1]);
          double e = Double.valueOf(parts[2]);
          double w = Double.valueOf(parts[3]);

          ///go get all the vector sources in my aoi
          String aggCallUrl = "https://iipbeta.digitalglobe.com/monocle-3/app/broker/vector/api/aggs/sources/binned?north=" + n + "&east=" + e + "&south=" + s + "&west=" + w + "&binCountX=2&binCountY=2";
          System.out.println(aggCallUrl);
          String aggJson = client.executeGet(aggCallUrl);
          DGAggResult aggs = gson.fromJson(aggJson, DGAggResult.class);
          Set<String> sources = new HashSet<>();
          for (int i = 0; i < aggs.getData().size(); i++) {
            DGAggResultItem agg = aggs.getData().get(i);

            for (int x = 0; x < aggs.getData().get(i).getData().size(); x++) {
              DGVectorFacetItem typeItem = aggs.getData().get(i).getData().get(x);
              sources.add(typeItem.getName());
              //    String getTypeUrl = appBase + "/api/vectors/" + typeItem.getName() + "/types?left=" + agg.getWest() + "&right=" + agg.getEast() + "&upper=" + agg.getNorth() + "&lower=" + agg.getSouth() + "";
              //   String jsonStringTypesForSource = client.executeGet(getTypeUrl);
              //    System.out.println(jsonStringTypesForSource);
              //  DGVectorFacet dgTypesWithinSource = gson.fromJson(jsonStringTypesForSource, DGVectorFacet.class);
              // agg.getData().addAll(dgTypesWithinSource.getData());

            }
          }

          for (int i = 0; i < aggs.getData().size(); i++) {
            DGAggResultItem agg = aggs.getData().get(i);

            for (String source : sources) {
              // DGVectorFacetItem typeItem = aggs.getData().get(i).getData().get(x);

              String getTypeUrl = appBase + "/api/vectors/" + source + "/types?left=" + agg.getWest() + "&right=" + agg.getEast() + "&upper=" + agg.getNorth() + "&lower=" + agg.getSouth() + "";
              System.out.println(getTypeUrl);
              String jsonStringTypesForSource = client.executeGet(getTypeUrl);
              //    System.out.println(jsonStringTypesForSource);
              DGVectorFacet dgTypesWithinSource = gson.fromJson(jsonStringTypesForSource, DGVectorFacet.class);
              agg.getData().addAll(dgTypesWithinSource.getData());

            }
          }

          System.out.println(aggs);
          ///make all the objects the same Structure.... a pivot table...
          for (DGAggResultItem item : aggs.getData()) {
            Set<String> keys = new HashSet<>();
            for (DGVectorFacetItem fi : item.getData()) {
              keys.add(fi.getName());
            }
            for (String col : cols) {
              if (!keys.contains(col)) {
                DGVectorFacetItem fitem = new DGVectorFacetItem();
                fitem.setCount(0);
                fitem.setName(col);
                item.getData().add(fitem);
              }
            }
          }

          ///at this point we basically have a pivot table.....
          //write it to a file, with WKT as a polygon to represnt the cells of data
          GeometryFactory fac = new GeometryFactory();
          for (DGAggResultItem item : aggs.getData()) {
            double lat, lon;
            lat = item.getNorth();
            lon = item.getWest();

            String wkt = fac.createPoint(new Coordinate(lon, lat)).toText();
            String newKey = wkt;
            output.put(newKey, new TreeMap<String, Double>());

            for (DGVectorFacetItem fi : item.getData()) {

              if (fi.getName() == null || fi.getCount() == null) {
                continue;
              }
              output.get(newKey).put(fi.getName(), Math.log(fi.getCount() + 1));
            }

          }

        } catch (Exception exc) {
          exc.printStackTrace();
          continue;
        }
        // System.out.println(output);
        // System.out.println("\tPRINTING DATA\n");
      }
      Set<String> badKeys = new HashSet<>();
      Pattern p = Pattern.compile("[^a-zA-Z0-9]");

      for (String key : output.keySet()) {
        Set<String> keys = output.get(key).keySet();
        if (keys.isEmpty()) {
          continue;
        }
        String colNames = "wkt;";
        for (String name : keys) {
          String asdf = p.matcher(name).replaceAll("");
          if (asdf.trim().isEmpty()) {
            badKeys.add(name);
            continue;
          }
          colNames += name + ";";
        }
        System.out.println(colNames.replaceAll("(\\(|\\)|,| |-)", ""));
        break;

      }

      for (String key : output.keySet()) {
        String vals = "";
        for (String innerKey : output.get(key).keySet()) {
          if (badKeys.contains(innerKey)) {
            continue;
          }
          double val = output.get(key).get(innerKey);
          vals += val + ";";
        }
        if (vals.isEmpty()) {
          continue;
        }
        System.out.println(key + ";" + vals.substring(0, vals.length() - 1));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    try {
      run(args);
      if (args == null) {
        return;
      }
      args = new String[]{"example.properties"};

      if (args.length < 1) {
        throw new RuntimeException("Configuration file must be specified.");
      }

      ServiceProperties props = new ServiceProperties(args[0]);

      // authentication information
      String authService = props.getAuthService();
      String username = "un";
      String password = "pw";

      // the base URL for accessing the vector service
      String appService = props.getAppService();
      String urlBase = props.getUrlBase();
      String appBase = appService + urlBase;

      // set up the client
      VectorRestClient client = new VectorRestClient();
      client.setAuthService(authService);
      client.setAppService(appService);

      System.out.println("Authenticating with the application. . . .");
      client.authenticate(username, password);

      Gson gson = new Gson();
      SortedSet<String> cols = new TreeSet<>();
      ///go get all the vector sources in my aoi
      String aggCallUrl = "https://iipbeta.digitalglobe.com/monocle-3/app/broker/vector/api/aggs/sources/binned?north=20.243169&east=21.268045&south=-9.376361&west=-11.701767&binCountX=5&binCountY=4";
      String aggJson = client.executeGet(aggCallUrl);
      DGAggResult aggs = gson.fromJson(aggJson, DGAggResult.class);
      Set<String> sources = new HashSet<>();
      for (int i = 0; i < aggs.getData().size(); i++) {
        DGAggResultItem agg = aggs.getData().get(i);

        for (int x = 0; x < aggs.getData().get(i).getData().size(); x++) {
          DGVectorFacetItem typeItem = aggs.getData().get(i).getData().get(x);
          sources.add(typeItem.getName());
          //    String getTypeUrl = appBase + "/api/vectors/" + typeItem.getName() + "/types?left=" + agg.getWest() + "&right=" + agg.getEast() + "&upper=" + agg.getNorth() + "&lower=" + agg.getSouth() + "";
          //   String jsonStringTypesForSource = client.executeGet(getTypeUrl);
          //    System.out.println(jsonStringTypesForSource);
          //  DGVectorFacet dgTypesWithinSource = gson.fromJson(jsonStringTypesForSource, DGVectorFacet.class);
          // agg.getData().addAll(dgTypesWithinSource.getData());

        }
      }

      for (int i = 0; i < aggs.getData().size(); i++) {
        DGAggResultItem agg = aggs.getData().get(i);

        for (String source : sources) {
          // DGVectorFacetItem typeItem = aggs.getData().get(i).getData().get(x);

          String getTypeUrl = appBase + "/api/vectors/" + source + "/types?left=" + agg.getWest() + "&right=" + agg.getEast() + "&upper=" + agg.getNorth() + "&lower=" + agg.getSouth() + "";
          System.out.println(getTypeUrl);
          String jsonStringTypesForSource = client.executeGet(getTypeUrl);
          //    System.out.println(jsonStringTypesForSource);
          DGVectorFacet dgTypesWithinSource = gson.fromJson(jsonStringTypesForSource, DGVectorFacet.class);
          agg.getData().addAll(dgTypesWithinSource.getData());

        }
      }

//        String tweetStatsUrl = "https://iipbeta.digitalglobe.com/monocle-3/app/broker/sma/sma/twitter/tweets?"
//                + "bbox=" + agg.getWest() + "," + agg.getSouth() + "," + agg.getEast() + "," + agg.getNorth()
//                + "&datetimerange=2015-02-20T05:00:00.000Z,2015-07-20T17:00:00.000Z&"
//                + "dimensions=hashtag";
//        String tweetStats = client.executeGet(tweetStatsUrl);
//
//        JSONObject jo = (JSONObject) JSONValue.parse(tweetStats);
//        JSONObject aggJo = (JSONObject) jo.get("aggregations");
//        JSONObject resultsJo = (JSONObject) aggJo.get("results");
//        HashMap<String, Object> mapRes = resultsJo;
//        Object o = mapRes.get("term");
//        String s = o.toString();
//        AggResultItem res = gson.fromJson(s, AggResultItem.class);
//        //now I have twitter term stats from this box... add them to the histogram
//        System.out.println("numTerms: " + res.getBuckets().size());
//        DGVectorFacetItem newItem = new DGVectorFacetItem();
//        newItem.setName("tweets");
//        newItem.setCount(res.getSum_other_doc_count());
//        agg.getData().add(newItem);
//        cols.add("tweets");
//        for (KeyDocCount kd : res.getBuckets()) {
//          DGVectorFacetItem anotherItem = new DGVectorFacetItem();
//          anotherItem.setName("ht." + kd.getKey());
//          anotherItem.setCount(kd.getDoc_count());
//          agg.getData().add(anotherItem);
//          cols.add("ht." + kd.getKey());
      //  }
      //  }
      System.out.println(aggs);
      ///make all the objects the same Structure.... a pivot table...
      for (DGAggResultItem item : aggs.getData()) {
        Set<String> keys = new HashSet<>();
        for (DGVectorFacetItem fi : item.getData()) {
          keys.add(fi.getName());
        }
        for (String col : cols) {
          if (!keys.contains(col)) {
            DGVectorFacetItem fitem = new DGVectorFacetItem();
            fitem.setCount(0);
            fitem.setName(col);
            item.getData().add(fitem);
          }
        }

      }
      ///at this point we basically have a pivot table.....
      //write it to a file, with WKT as a polygon to represnt the cells of data
      Map<String, SortedMap<String, Double>> output = new TreeMap<>();
      GeometryFactory fac = new GeometryFactory();
      for (DGAggResultItem item : aggs.getData()) {
        double lat, lon;
        lat = ((item.getNorth() + item.getSouth()) / 2);
        lon = ((item.getEast() + item.getWest()) / 2);
        String wkt = fac.createPoint(new Coordinate(lon, lat)).toText();
        String newKey = wkt;
        output.put(newKey, new TreeMap<String, Double>());

        for (DGVectorFacetItem fi : item.getData()) {
          //  fi.getName().replaceAll("[^a-zA-Z0-9]", "");
          System.out.println(fi.getName());
          System.out.println(fi.getCount());
          if (fi.getName() == null || fi.getCount() == null) {
            continue;
          }
          output.get(newKey).put(fi.getName(), Math.log(fi.getCount() + 1));
        }

      }

      System.out.println(output);
      System.out.println("\tPRINTING DATA\n");
      Set<String> badKeys = new HashSet<>();
      Pattern p = Pattern.compile("[^a-zA-Z0-9]");

      for (String key : output.keySet()) {
        Set<String> keys = output.get(key).keySet();
        if (keys.isEmpty()) {
          continue;
        }
        String colNames = "wkt;";
        for (String name : keys) {
          String asdf = p.matcher(name).replaceAll("");
          if (asdf.trim().isEmpty()) {
            badKeys.add(name);
            continue;
          }
          colNames += name + ";";
        }
        System.out.println(colNames.replaceAll("(\\(|\\)|,| |-)", ""));
        break;

      }

      for (String key : output.keySet()) {
        String vals = "";
        for (String innerKey : output.get(key).keySet()) {
          if (badKeys.contains(innerKey)) {
            continue;
          }
          double val = output.get(key).get(innerKey);
          vals += val + ";";
        }
        if (vals.isEmpty()) {
          continue;
        }
        System.out.println(key + ";" + vals.substring(0, vals.length() - 1));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static List<String> getBboxes() {
    List<String> boxes = new ArrayList<>();
    boxes.add("12.69589388,11.3771014,-5.673919749999999,-4.563187374999998");
    boxes.add("14.014686359999999,12.69589388,-5.673919749999999,-4.563187374999998");
    boxes.add("15.333478839999998,14.014686359999999,-5.673919749999999,-4.563187374999998");
    boxes.add("16.652271319999997,15.333478839999998,-5.673919749999999,-4.563187374999998");
    boxes.add("17.9710638,16.65227132,-5.673919749999999,-4.563187374999998");
    boxes.add("11.3771014,10.058308920000002,-4.563187374999998,-3.4524549999999983");
    boxes.add("12.69589388,11.3771014,-4.563187374999998,-3.4524549999999983");
    boxes.add("14.014686359999999,12.69589388,-4.563187374999998,-3.4524549999999983");
    boxes.add("15.333478839999998,14.014686359999999,-4.563187374999998,-3.4524549999999983");
    boxes.add("16.652271319999997,15.333478839999998,-4.563187374999998,-3.4524549999999983");
    boxes.add("17.9710638,16.65227132,-4.563187374999998,-3.4524549999999983");
    boxes.add("10.058308919999998,8.73951644,-3.4524549999999987,-2.3417226249999987");
    boxes.add("11.3771014,10.058308920000002,-3.4524549999999987,-2.3417226249999987");
    boxes.add("12.69589388,11.3771014,-3.4524549999999987,-2.3417226249999987");
    boxes.add("14.014686359999999,12.69589388,-3.4524549999999987,-2.3417226249999987");
    boxes.add("15.333478839999998,14.014686359999999,-3.4524549999999987,-2.3417226249999987");
    boxes.add("16.652271319999997,15.333478839999998,-3.4524549999999987,-2.3417226249999987");
    boxes.add("17.9710638,16.65227132,-3.4524549999999987,-2.3417226249999987");
    boxes.add("10.058308919999998,8.73951644,-2.3417226249999987,-1.2309902499999987");
    boxes.add("11.3771014,10.058308920000002,-2.3417226249999987,-1.2309902499999987");
    boxes.add("12.69589388,11.3771014,-2.3417226249999987,-1.2309902499999987");
    boxes.add("14.014686359999999,12.69589388,-2.3417226249999987,-1.2309902499999987");
    boxes.add("15.333478839999998,14.014686359999999,-2.3417226249999987,-1.2309902499999987");
    boxes.add("16.652271319999997,15.333478839999998,-2.3417226249999987,-1.2309902499999987");
    boxes.add("17.9710638,16.65227132,-2.3417226249999987,-1.2309902499999987");
    boxes.add("10.058308919999998,8.73951644,-1.2309902499999987,-0.12025787499999874");
    boxes.add("11.3771014,10.058308920000002,-1.2309902499999987,-0.12025787499999874");
    boxes.add("12.69589388,11.3771014,-1.2309902499999987,-0.12025787499999874");
    boxes.add("14.014686359999999,12.69589388,-1.2309902499999987,-0.12025787499999874");
    boxes.add("15.333478839999998,14.014686359999999,-1.2309902499999987,-0.12025787499999874");
    boxes.add("16.652271319999997,15.333478839999998,-1.2309902499999987,-0.12025787499999874");
    boxes.add("17.9710638,16.65227132,-1.2309902499999987,-0.12025787499999874");
    boxes.add("10.058308919999998,8.73951644,-0.12025787499999918,0.9904745000000008");
    boxes.add("11.3771014,10.058308920000002,-0.12025787499999918,0.9904745000000008");
    boxes.add("12.69589388,11.3771014,-0.12025787499999918,0.9904745000000008");
    boxes.add("14.014686359999999,12.69589388,-0.12025787499999918,0.9904745000000008");
    boxes.add("15.333478839999998,14.014686359999999,-0.12025787499999918,0.9904745000000008");
    boxes.add("16.652271319999997,15.333478839999998,-0.12025787499999918,0.9904745000000008");
    boxes.add("17.9710638,16.65227132,-0.12025787499999918,0.9904745000000008");
    boxes.add("10.058308919999998,8.73951644,0.9904745000000013,2.1012068750000013");
    boxes.add("11.3771014,10.058308920000002,0.9904745000000013,2.1012068750000013");
    boxes.add("12.69589388,11.3771014,0.9904745000000013,2.1012068750000013");
    boxes.add("14.014686359999999,12.69589388,0.9904745000000013,2.1012068750000013");
    boxes.add("15.333478839999998,14.014686359999999,0.9904745000000013,2.1012068750000013");
    boxes.add("16.652271319999997,15.333478839999998,0.9904745000000013,2.1012068750000013");
    boxes.add("17.9710638,16.65227132,0.9904745000000013,2.1012068750000013");
    boxes.add("10.058308919999998,8.73951644,2.1012068750000017,3.2119392500000017");
    boxes.add("11.3771014,10.058308920000002,2.1012068750000017,3.2119392500000017");
    boxes.add("12.69589388,11.3771014,2.1012068750000017,3.2119392500000017");
    boxes.add("14.014686359999999,12.69589388,2.1012068750000017,3.2119392500000017");
    boxes.add("15.333478839999998,14.014686359999999,2.1012068750000017,3.2119392500000017");
    boxes.add("16.652271319999997,15.333478839999998,2.1012068750000017,3.2119392500000017");
    boxes.add("17.9710638,16.65227132,2.1012068750000017,3.2119392500000017");
    boxes.add("10.058308919999998,8.73951644,3.2119392500000012,4.322671625000002");
    boxes.add("11.3771014,10.058308920000002,3.2119392500000012,4.322671625000002");
    boxes.add("12.69589388,11.3771014,3.2119392500000012,4.322671625000002");
    boxes.add("14.014686359999999,12.69589388,3.2119392500000012,4.322671625000002");
    boxes.add("15.333478839999998,14.014686359999999,3.2119392500000012,4.322671625000002");
    boxes.add("16.652271319999997,15.333478839999998,3.2119392500000012,4.322671625000002");
    boxes.add("17.9710638,16.65227132,3.2119392500000012,4.322671625000002");
    boxes.add("-7.085993319999999,-8.404785799999999,4.322671625000001,5.433404000000001");
    boxes.add("-5.767200839999999,-7.085993319999999,4.322671625000001,5.433404000000001");
    boxes.add("-4.448408359999999,-5.767200839999999,4.322671625000001,5.433404000000001");
    boxes.add("-3.1296158799999994,-4.448408359999999,4.322671625000001,5.433404000000001");
    boxes.add("-1.8108233999999994,-3.1296158799999994,4.322671625000001,5.433404000000001");
    boxes.add("-0.4920309199999995,-1.8108233999999994,4.322671625000001,5.433404000000001");
    boxes.add("0.8267615600000005,-0.4920309199999995,4.322671625000001,5.433404000000001");
    boxes.add("6.10193148,4.783139,4.322671625000001,5.433404000000001");
    boxes.add("7.420723960000001,6.101931480000001,4.322671625000001,5.433404000000001");
    boxes.add("8.73951644,7.42072396,4.322671625000001,5.433404000000001");
    boxes.add("10.058308919999998,8.73951644,4.322671625000001,5.433404000000001");
    boxes.add("11.3771014,10.058308920000002,4.322671625000001,5.433404000000001");
    boxes.add("12.69589388,11.3771014,4.322671625000001,5.433404000000001");
    boxes.add("14.014686359999999,12.69589388,4.322671625000001,5.433404000000001");
    boxes.add("15.333478839999998,14.014686359999999,4.322671625000001,5.433404000000001");
    boxes.add("16.652271319999997,15.333478839999998,4.322671625000001,5.433404000000001");
    boxes.add("17.9710638,16.65227132,4.322671625000001,5.433404000000001");
    boxes.add("-7.085993319999999,-8.404785799999999,5.433404,6.544136375000001");
    boxes.add("-5.767200839999999,-7.085993319999999,5.433404,6.544136375000001");
    boxes.add("-4.448408359999999,-5.767200839999999,5.433404,6.544136375000001");
    boxes.add("-3.1296158799999994,-4.448408359999999,5.433404,6.544136375000001");
    boxes.add("-1.8108233999999994,-3.1296158799999994,5.433404,6.544136375000001");
    boxes.add("-0.4920309199999995,-1.8108233999999994,5.433404,6.544136375000001");
    boxes.add("0.8267615600000005,-0.4920309199999995,5.433404,6.544136375000001");
    boxes.add("2.1455540400000013,0.8267615600000013,5.433404,6.544136375000001");
    boxes.add("3.4643465200000003,2.1455540400000004,5.433404,6.544136375000001");
    boxes.add("4.783138999999999,3.4643465199999994,5.433404,6.544136375000001");
    boxes.add("6.10193148,4.783139,5.433404,6.544136375000001");
    boxes.add("7.420723960000001,6.101931480000001,5.433404,6.544136375000001");
    boxes.add("8.73951644,7.42072396,5.433404,6.544136375000001");
    boxes.add("10.058308919999998,8.73951644,5.433404,6.544136375000001");
    boxes.add("11.3771014,10.058308920000002,5.433404,6.544136375000001");
    boxes.add("12.69589388,11.3771014,5.433404,6.544136375000001");
    boxes.add("14.014686359999999,12.69589388,5.433404,6.544136375000001");
    boxes.add("15.333478839999998,14.014686359999999,5.433404,6.544136375000001");
    boxes.add("16.652271319999997,15.333478839999998,5.433404,6.544136375000001");
    boxes.add("17.9710638,16.65227132,5.433404,6.544136375000001");
    boxes.add("-7.085993319999999,-8.404785799999999,6.544136375000002,7.654868750000002");
    boxes.add("-5.767200839999999,-7.085993319999999,6.544136375000002,7.654868750000002");
    boxes.add("-4.448408359999999,-5.767200839999999,6.544136375000002,7.654868750000002");
    boxes.add("-3.1296158799999994,-4.448408359999999,6.544136375000002,7.654868750000002");
    boxes.add("-1.8108233999999994,-3.1296158799999994,6.544136375000002,7.654868750000002");
    boxes.add("-0.4920309199999995,-1.8108233999999994,6.544136375000002,7.654868750000002");
    boxes.add("0.8267615600000005,-0.4920309199999995,6.544136375000002,7.654868750000002");
    boxes.add("2.1455540400000013,0.8267615600000013,6.544136375000002,7.654868750000002");
    boxes.add("3.4643465200000003,2.1455540400000004,6.544136375000002,7.654868750000002");
    boxes.add("4.783138999999999,3.4643465199999994,6.544136375000002,7.654868750000002");
    boxes.add("6.10193148,4.783139,6.544136375000002,7.654868750000002");
    boxes.add("7.420723960000001,6.101931480000001,6.544136375000002,7.654868750000002");
    boxes.add("8.73951644,7.42072396,6.544136375000002,7.654868750000002");
    boxes.add("10.058308919999998,8.73951644,6.544136375000002,7.654868750000002");
    boxes.add("11.3771014,10.058308920000002,6.544136375000002,7.654868750000002");
    boxes.add("12.69589388,11.3771014,6.544136375000002,7.654868750000002");
    boxes.add("14.014686359999999,12.69589388,6.544136375000002,7.654868750000002");
    boxes.add("15.333478839999998,14.014686359999999,6.544136375000002,7.654868750000002");
    boxes.add("16.652271319999997,15.333478839999998,6.544136375000002,7.654868750000002");
    boxes.add("17.9710638,16.65227132,6.544136375000002,7.654868750000002");
    boxes.add("-7.085993319999999,-8.404785799999999,7.654868750000001,8.765601125000002");
    boxes.add("-5.767200839999999,-7.085993319999999,7.654868750000001,8.765601125000002");
    boxes.add("-4.448408359999999,-5.767200839999999,7.654868750000001,8.765601125000002");
    boxes.add("-3.1296158799999994,-4.448408359999999,7.654868750000001,8.765601125000002");
    boxes.add("-1.8108233999999994,-3.1296158799999994,7.654868750000001,8.765601125000002");
    boxes.add("-0.4920309199999995,-1.8108233999999994,7.654868750000001,8.765601125000002");
    boxes.add("0.8267615600000005,-0.4920309199999995,7.654868750000001,8.765601125000002");
    boxes.add("2.1455540400000013,0.8267615600000013,7.654868750000001,8.765601125000002");
    boxes.add("3.4643465200000003,2.1455540400000004,7.654868750000001,8.765601125000002");
    boxes.add("4.783138999999999,3.4643465199999994,7.654868750000001,8.765601125000002");
    boxes.add("6.10193148,4.783139,7.654868750000001,8.765601125000002");
    boxes.add("7.420723960000001,6.101931480000001,7.654868750000001,8.765601125000002");
    boxes.add("8.73951644,7.42072396,7.654868750000001,8.765601125000002");
    boxes.add("10.058308919999998,8.73951644,7.654868750000001,8.765601125000002");
    boxes.add("11.3771014,10.058308920000002,7.654868750000001,8.765601125000002");
    boxes.add("12.69589388,11.3771014,7.654868750000001,8.765601125000002");
    boxes.add("14.014686359999999,12.69589388,7.654868750000001,8.765601125000002");
    boxes.add("15.333478839999998,14.014686359999999,7.654868750000001,8.765601125000002");
    boxes.add("16.652271319999997,15.333478839999998,7.654868750000001,8.765601125000002");
    boxes.add("17.9710638,16.65227132,7.654868750000001,8.765601125000002");
    boxes.add("-7.085993319999999,-8.404785799999999,8.765601125,9.8763335");
    boxes.add("-5.767200839999999,-7.085993319999999,8.765601125,9.8763335");
    boxes.add("-4.448408359999999,-5.767200839999999,8.765601125,9.8763335");
    boxes.add("-3.1296158799999994,-4.448408359999999,8.765601125,9.8763335");
    boxes.add("-1.8108233999999994,-3.1296158799999994,8.765601125,9.8763335");
    boxes.add("-0.4920309199999995,-1.8108233999999994,8.765601125,9.8763335");
    boxes.add("0.8267615600000005,-0.4920309199999995,8.765601125,9.8763335");
    boxes.add("2.1455540400000013,0.8267615600000013,8.765601125,9.8763335");
    boxes.add("3.4643465200000003,2.1455540400000004,8.765601125,9.8763335");
    boxes.add("4.783138999999999,3.4643465199999994,8.765601125,9.8763335");
    boxes.add("6.10193148,4.783139,8.765601125,9.8763335");
    boxes.add("7.420723960000001,6.101931480000001,8.765601125,9.8763335");
    boxes.add("8.73951644,7.42072396,8.765601125,9.8763335");
    boxes.add("10.058308919999998,8.73951644,8.765601125,9.8763335");
    boxes.add("11.3771014,10.058308920000002,8.765601125,9.8763335");
    boxes.add("12.69589388,11.3771014,8.765601125,9.8763335");
    boxes.add("14.014686359999999,12.69589388,8.765601125,9.8763335");
    boxes.add("15.333478839999998,14.014686359999999,8.765601125,9.8763335");
    boxes.add("16.652271319999997,15.333478839999998,8.765601125,9.8763335");
    boxes.add("17.9710638,16.65227132,8.765601125,9.8763335");
    boxes.add("-7.085993319999999,-8.404785799999999,9.876333500000001,10.987065875");
    boxes.add("-5.767200839999999,-7.085993319999999,9.876333500000001,10.987065875");
    boxes.add("-4.448408359999999,-5.767200839999999,9.876333500000001,10.987065875");
    boxes.add("-3.1296158799999994,-4.448408359999999,9.876333500000001,10.987065875");
    boxes.add("-1.8108233999999994,-3.1296158799999994,9.876333500000001,10.987065875");
    boxes.add("-0.4920309199999995,-1.8108233999999994,9.876333500000001,10.987065875");
    boxes.add("0.8267615600000005,-0.4920309199999995,9.876333500000001,10.987065875");
    boxes.add("2.1455540400000013,0.8267615600000013,9.876333500000001,10.987065875");
    boxes.add("3.4643465200000003,2.1455540400000004,9.876333500000001,10.987065875");
    boxes.add("4.783138999999999,3.4643465199999994,9.876333500000001,10.987065875");
    boxes.add("6.10193148,4.783139,9.876333500000001,10.987065875");
    boxes.add("7.420723960000001,6.101931480000001,9.876333500000001,10.987065875");
    boxes.add("8.73951644,7.42072396,9.876333500000001,10.987065875");
    boxes.add("10.058308919999998,8.73951644,9.876333500000001,10.987065875");
    boxes.add("11.3771014,10.058308920000002,9.876333500000001,10.987065875");
    boxes.add("12.69589388,11.3771014,9.876333500000001,10.987065875");
    boxes.add("14.014686359999999,12.69589388,9.876333500000001,10.987065875");
    boxes.add("15.333478839999998,14.014686359999999,9.876333500000001,10.987065875");
    boxes.add("16.652271319999997,15.333478839999998,9.876333500000001,10.987065875");
    boxes.add("17.9710638,16.65227132,9.876333500000001,10.987065875");
    boxes.add("-7.085993319999999,-8.404785799999999,10.987065874999999,12.097798249999999");
    boxes.add("-5.767200839999999,-7.085993319999999,10.987065874999999,12.097798249999999");
    boxes.add("-4.448408359999999,-5.767200839999999,10.987065874999999,12.097798249999999");
    boxes.add("-3.1296158799999994,-4.448408359999999,10.987065874999999,12.097798249999999");
    boxes.add("-1.8108233999999994,-3.1296158799999994,10.987065874999999,12.097798249999999");
    boxes.add("-0.4920309199999995,-1.8108233999999994,10.987065874999999,12.097798249999999");
    boxes.add("0.8267615600000005,-0.4920309199999995,10.987065874999999,12.097798249999999");
    boxes.add("2.1455540400000013,0.8267615600000013,10.987065874999999,12.097798249999999");
    boxes.add("3.4643465200000003,2.1455540400000004,10.987065874999999,12.097798249999999");
    boxes.add("4.783138999999999,3.4643465199999994,10.987065874999999,12.097798249999999");
    boxes.add("6.10193148,4.783139,10.987065874999999,12.097798249999999");
    boxes.add("7.420723960000001,6.101931480000001,10.987065874999999,12.097798249999999");
    boxes.add("8.73951644,7.42072396,10.987065874999999,12.097798249999999");
    boxes.add("10.058308919999998,8.73951644,10.987065874999999,12.097798249999999");
    boxes.add("11.3771014,10.058308920000002,10.987065874999999,12.097798249999999");
    boxes.add("12.69589388,11.3771014,10.987065874999999,12.097798249999999");
    boxes.add("14.014686359999999,12.69589388,10.987065874999999,12.097798249999999");
    boxes.add("15.333478839999998,14.014686359999999,10.987065874999999,12.097798249999999");
    boxes.add("16.652271319999997,15.333478839999998,10.987065874999999,12.097798249999999");
    boxes.add("17.9710638,16.65227132,10.987065874999999,12.097798249999999");
    boxes.add("-7.085993319999999,-8.404785799999999,12.09779825,13.208530625");
    boxes.add("-5.767200839999999,-7.085993319999999,12.09779825,13.208530625");
    boxes.add("-4.448408359999999,-5.767200839999999,12.09779825,13.208530625");
    boxes.add("-3.1296158799999994,-4.448408359999999,12.09779825,13.208530625");
    boxes.add("-1.8108233999999994,-3.1296158799999994,12.09779825,13.208530625");
    boxes.add("-0.4920309199999995,-1.8108233999999994,12.09779825,13.208530625");
    boxes.add("0.8267615600000005,-0.4920309199999995,12.09779825,13.208530625");
    boxes.add("2.1455540400000013,0.8267615600000013,12.09779825,13.208530625");
    boxes.add("3.4643465200000003,2.1455540400000004,12.09779825,13.208530625");
    boxes.add("4.783138999999999,3.4643465199999994,12.09779825,13.208530625");
    boxes.add("6.10193148,4.783139,12.09779825,13.208530625");
    boxes.add("7.420723960000001,6.101931480000001,12.09779825,13.208530625");
    boxes.add("8.73951644,7.42072396,12.09779825,13.208530625");
    boxes.add("10.058308919999998,8.73951644,12.09779825,13.208530625");
    boxes.add("11.3771014,10.058308920000002,12.09779825,13.208530625");
    boxes.add("12.69589388,11.3771014,12.09779825,13.208530625");
    boxes.add("14.014686359999999,12.69589388,12.09779825,13.208530625");
    boxes.add("15.333478839999998,14.014686359999999,12.09779825,13.208530625");
    boxes.add("16.652271319999997,15.333478839999998,12.09779825,13.208530625");
    boxes.add("17.9710638,16.65227132,12.09779825,13.208530625");
    boxes.add("-7.085993319999999,-8.404785799999999,13.208530625000002,14.319263000000001");
    boxes.add("-5.767200839999999,-7.085993319999999,13.208530625000002,14.319263000000001");
    boxes.add("-4.448408359999999,-5.767200839999999,13.208530625000002,14.319263000000001");
    boxes.add("-3.1296158799999994,-4.448408359999999,13.208530625000002,14.319263000000001");
    boxes.add("-1.8108233999999994,-3.1296158799999994,13.208530625000002,14.319263000000001");
    boxes.add("-0.4920309199999995,-1.8108233999999994,13.208530625000002,14.319263000000001");
    boxes.add("0.8267615600000005,-0.4920309199999995,13.208530625000002,14.319263000000001");
    boxes.add("2.1455540400000013,0.8267615600000013,13.208530625000002,14.319263000000001");
    boxes.add("3.4643465200000003,2.1455540400000004,13.208530625000002,14.319263000000001");
    boxes.add("4.783138999999999,3.4643465199999994,13.208530625000002,14.319263000000001");
    boxes.add("6.10193148,4.783139,13.208530625000002,14.319263000000001");
    boxes.add("7.420723960000001,6.101931480000001,13.208530625000002,14.319263000000001");
    boxes.add("8.73951644,7.42072396,13.208530625000002,14.319263000000001");
    boxes.add("10.058308919999998,8.73951644,13.208530625000002,14.319263000000001");
    boxes.add("11.3771014,10.058308920000002,13.208530625000002,14.319263000000001");
    boxes.add("12.69589388,11.3771014,13.208530625000002,14.319263000000001");
    boxes.add("14.014686359999999,12.69589388,13.208530625000002,14.319263000000001");
    boxes.add("15.333478839999998,14.014686359999999,13.208530625000002,14.319263000000001");
    boxes.add("16.652271319999997,15.333478839999998,13.208530625000002,14.319263000000001");
    boxes.add("17.9710638,16.65227132,13.208530625000002,14.319263000000001");
    boxes.add("-7.085993319999999,-8.404785799999999,14.319263,15.429995374999999");
    boxes.add("-5.767200839999999,-7.085993319999999,14.319263,15.429995374999999");
    boxes.add("-4.448408359999999,-5.767200839999999,14.319263,15.429995374999999");
    boxes.add("-3.1296158799999994,-4.448408359999999,14.319263,15.429995374999999");
    boxes.add("-1.8108233999999994,-3.1296158799999994,14.319263,15.429995374999999");
    boxes.add("-0.4920309199999995,-1.8108233999999994,14.319263,15.429995374999999");
    boxes.add("0.8267615600000005,-0.4920309199999995,14.319263,15.429995374999999");
    boxes.add("2.1455540400000013,0.8267615600000013,14.319263,15.429995374999999");
    boxes.add("3.4643465200000003,2.1455540400000004,14.319263,15.429995374999999");
    boxes.add("4.783138999999999,3.4643465199999994,14.319263,15.429995374999999");
    boxes.add("6.10193148,4.783139,14.319263,15.429995374999999");
    boxes.add("7.420723960000001,6.101931480000001,14.319263,15.429995374999999");
    boxes.add("8.73951644,7.42072396,14.319263,15.429995374999999");
    boxes.add("10.058308919999998,8.73951644,14.319263,15.429995374999999");
    boxes.add("11.3771014,10.058308920000002,14.319263,15.429995374999999");
    boxes.add("12.69589388,11.3771014,14.319263,15.429995374999999");
    boxes.add("14.014686359999999,12.69589388,14.319263,15.429995374999999");
    boxes.add("15.333478839999998,14.014686359999999,14.319263,15.429995374999999");
    boxes.add("16.652271319999997,15.333478839999998,14.319263,15.429995374999999");
    boxes.add("17.9710638,16.65227132,14.319263,15.429995374999999");
    boxes.add("-7.085993319999999,-8.404785799999999,15.429995375,16.540727750000002");
    boxes.add("-5.767200839999999,-7.085993319999999,15.429995375,16.540727750000002");
    boxes.add("-4.448408359999999,-5.767200839999999,15.429995375,16.540727750000002");
    boxes.add("-3.1296158799999994,-4.448408359999999,15.429995375,16.540727750000002");
    boxes.add("-1.8108233999999994,-3.1296158799999994,15.429995375,16.540727750000002");
    boxes.add("-0.4920309199999995,-1.8108233999999994,15.429995375,16.540727750000002");
    boxes.add("0.8267615600000005,-0.4920309199999995,15.429995375,16.540727750000002");
    boxes.add("2.1455540400000013,0.8267615600000013,15.429995375,16.540727750000002");
    boxes.add("3.4643465200000003,2.1455540400000004,15.429995375,16.540727750000002");
    boxes.add("4.783138999999999,3.4643465199999994,15.429995375,16.540727750000002");
    boxes.add("6.10193148,4.783139,15.429995375,16.540727750000002");
    boxes.add("7.420723960000001,6.101931480000001,15.429995375,16.540727750000002");
    boxes.add("8.73951644,7.42072396,15.429995375,16.540727750000002");
    boxes.add("10.058308919999998,8.73951644,15.429995375,16.540727750000002");
    boxes.add("11.3771014,10.058308920000002,15.429995375,16.540727750000002");
    boxes.add("12.69589388,11.3771014,15.429995375,16.540727750000002");
    boxes.add("14.014686359999999,12.69589388,15.429995375,16.540727750000002");
    boxes.add("15.333478839999998,14.014686359999999,15.429995375,16.540727750000002");
    boxes.add("16.652271319999997,15.333478839999998,15.429995375,16.540727750000002");
    boxes.add("17.9710638,16.65227132,15.429995375,16.540727750000002");

    return boxes;
  }
}
