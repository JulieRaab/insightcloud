package com.digitalglobe.insight.vector;

import com.google.gson.Gson;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example class showing possible ways of interacting with the UVI REST API
 */
public class DGVectorTreeBuilder {

  /**
   * Authenticates with the UVI, requests pages of items, executes another
   * requests for non-paged items, then logs out from the application.
   *
   * @param args no args needed at this time
   */
  public static void main(String[] args) throws IOException {
    if ( args.length < 1 )
    {
      throw new RuntimeException( "Configuration file must be specified." );
    }

    ServiceProperties props = new ServiceProperties( args[0] );

    // authentication information
    String authService = props.getAuthService();
    String username = props.getUserName();
    String password = props.getPassword();

    // the base URL for accessing the vector service
    String appService = props.getAppService();
    String urlBase = props.getUrlBase();
    String appBase = appService + urlBase;

    // set up the client
    VectorRestClient client = new VectorRestClient();
    client.setAuthService( authService );
    client.setAppService( appService );

    System.out.println( "Authenticating with the application. . . ." );
    client.authenticate(username, password);

    Gson gson = new Gson();
    /*
     Find out what's in the DG UVI... query for a list of sources in your BBox and know how many features are in each source
     */
    String getSourcesURL = appBase + "/api/esri/sources?left=-180&right=180&upper=90&lower=-90";
    String jsonStringSources = client.executeGet(getSourcesURL);
    JSONObject sources = (JSONObject) JSONValue.parse(jsonStringSources);
    System.out.println(sources);
 
    DGVectorFacet sourceFacets = gson.fromJson(jsonStringSources, DGVectorFacet.class);
    //loop over the sources
    for (DGVectorFacetItem source : sourceFacets.getData()) {
      //now we'll grab the types for each source
      String getTypeUrl = appBase + "/api/vectors/" + source.getName() + "/types?left=-180&right=180&upper=90&lower=-90";
      String jsonStringTypesForSource = client.executeGet(getTypeUrl);
      //    System.out.println(jsonStringTypesForSource);
      DGVectorFacet dgTypesWithinSource = gson.fromJson(jsonStringTypesForSource, DGVectorFacet.class);
      for (DGVectorFacetItem type : dgTypesWithinSource.getData()) {
        System.out.println("\t" + type.getName() + " : " + type.getCount());

        //now lets get some data for this type, in this source
        String baseUrl = appBase + "/api/vectors/"
                         + URLEncoder.encode( source.getName(), "UTF-8" )
                         + "/" + URLEncoder.encode( type.getName() );
        String queryReq = baseUrl + "?left=-180&right=180&upper=90&lower=-90&count=10";
        String jsonStringVectorsOfTypesForSource = client.executeGet(queryReq);
        DGVectorItem[] vectorItems = gson.fromJson(jsonStringVectorsOfTypesForSource, DGVectorItem[].class);
        System.out.println("do something with these vector objects!");
        for(DGVectorItem item: vectorItems){
          System.out.println(item.toString());
          
        }
      }

    } 

    // logout from CAS
    client.logout();
  }
}
