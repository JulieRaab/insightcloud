package com.digitalglobe.insight.vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.io.IOException;

public class UploadVectorItems
{
  public static void main( String[] args ) throws IOException
  {
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

    System.out.println( "Auth service: " + authService );
    System.out.println( "App service: " + appService );
    System.out.println( "App base: " + appBase );

    System.out.println( "Authenticating with the application. . . ." );
    client.authenticate( username, password );

    // send a single item to upload
    System.out.println( "Sending single-item upload. . . .");
    String singleItemUploadRequest = appBase + "/api/vector";
    String singleItemJson = "{" +
        "    \"type\": \"Feature\"," +
        "        \"geometry\": {" +
        "            \"type\": \"Point\"," +
        "            \"coordinates\": [-42,42]" +
        "        }," +
        "        \"properties\": {" +
        "            \"name\" : \"Jabberwocky\"," +
        "            \"ingestSource\" : \"Test\"," +
        "            \"itemType\" : \"Test item\"," +
        "            \"text\" : \"'Twas brillig and the slithy toves. . . .\"," +
        "            \"source\" : \"Lewis Carroll\"," +
        "            \"attributes\" : {" +
        "               \"my.arbitrary.string\":\"foo\"," +
        "               \"my.arbitrary.number\": 42," +
        "               \"my.arbitrary.date\": \"2015-07-23T13:37:42.000Z\"" +
        "            }\n" +
        "        }\n" +
        "}";

    String singleItemResponse = client.executePost( singleItemUploadRequest, singleItemJson );
    System.out.println( singleItemResponse );

    handleItemPaths( singleItemResponse, appBase, client );

    // Now send an array of items to upload
    System.out.println( "Sending multi-item upload. . . .");
    String multiItemUploadRequest = appBase + "/api/vectors";
    String multiItemJson = "[" +
      "{" +
      "    \"type\": \"Feature\"," +
      "        \"geometry\": {" +
      "            \"type\": \"Point\"," +
      "            \"coordinates\": [-42,42]" +
      "        }," +
      "        \"properties\": {" +
      "            \"name\" : \"Jabberwocky, line 1\"," +
      "            \"ingestSource\" : \"Test\"," +
      "            \"itemType\" : \"Test item\"," +
      "            \"text\" : \"'Twas brillig and the slithy toves\"," +
      "            \"source\" : \"Lewis Carroll\"," +
      "            \"attributes\" : {" +
      "               \"my.arbitrary.string\":\"foo\"," +
      "               \"my.arbitrary.number\": 42," +
      "               \"my.arbitrary.date\": \"2015-07-23T13:37:42.000Z\"" +
      "            }\n" +
      "        }\n" +
      "}," +
      "{" +
      "    \"type\": \"Feature\"," +
      "        \"geometry\": {" +
      "            \"type\": \"Point\"," +
      "            \"coordinates\": [-43,43]" +
      "        }," +
      "        \"properties\": {" +
      "            \"name\" : \"Jabberwocky, line 2\"," +
      "            \"ingestSource\" : \"Test\"," +
      "            \"itemType\" : \"Test item\"," +
      "            \"text\" : \"did gyre and gimbel in the wabe\"," +
      "            \"source\" : \"Lewis Carroll\"," +
      "            \"attributes\" : {" +
      "               \"my.arbitrary.string\":\"foo\"," +
      "               \"my.arbitrary.number\": 43," +
      "               \"my.arbitrary.date\": \"2015-07-23T13:37:42.000Z\"" +
      "            }\n" +
      "        }\n" +
      "}," +
    "]";

    String multiItemResponse = client.executePost( multiItemUploadRequest, multiItemJson );
    System.out.println( multiItemResponse );

    handleItemPaths( multiItemResponse, appBase, client );

    client.logout();
  }

  public static void handleItemPaths( String response, String appBase, VectorRestClient client ) throws IOException
  {
    // the POST response gives us an array of paths to retrieve the created items
    JSONArray itemPaths = (JSONArray) JSONValue.parse( response );
    for ( Object itemPath : itemPaths )
    {
      String path = (String) itemPath;
      System.out.println( "Getting object for path: " + path );
      // b/c we're brokered through monocle-3 for now, the paths are messed up
      String url = path.replaceFirst( "/insight-vector", appBase );
      String getResponse = client.executeGet( url );
      System.out.println( getResponse );

      // DELETE the items we just uploaded to clean up test items.
      // Note, only the user specified in the item's ingestAttributes{ _rest.user }
      // property can update/delete an item.
      System.out.println( "Deleting item at path: " + path );
      String deleteResponse = client.executeDelete( url );
      // should be no content
      if ( deleteResponse != null )
      {
        throw new RuntimeException( "Got got content when we shouldn't have: "
                                        + deleteResponse );
      }
      else
      {
        System.out.println( "No content from DELETE response . . . ok.");
      }
    }
  }
}
