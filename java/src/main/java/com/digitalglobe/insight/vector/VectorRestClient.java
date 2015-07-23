package com.digitalglobe.insight.vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example class showing possible ways of interacting with the UVI REST API
 */
public class VectorRestClient
{
  protected static final Logger log = LoggerFactory.getLogger( VectorRestClient.class );

  public static class VectorPagingResponse
  {
    private int status;
    private String body;
    private String pageId;
    private int itemCount;

    public VectorPagingResponse( int status, String body, int itemCount, String pageId )
    {
      this.body = body;
      this.itemCount = itemCount;
      this.pageId = pageId;
      this.status = status;
    }

    public String getBody()
    {
      return body;
    }

    public int getItemCount()
    {
      return itemCount;
    }

    public String getPageId()
    {
      return pageId;
    }

    public int getStatus()
    {
      return status;
    }
  }

  private String authService;
  private String appService;
  private String ticketGrantingTicket;
  private HttpClient httpClient = new HttpClient();

  /**
   * Sets the base URL for the application service
   *
   * @param appService the app service URL
   */
  public void setAppService( String appService )
  {
    this.appService = appService;
  }

  /**
   * Sets the base URL for the CAS auth service
   *
   * @param authService the CAS service URL
   */
  public void setAuthService( String authService )
  {
    this.authService = authService;
  }

  /**
   * Gets a service ticket from CAS for use in authenticating with the application
   *
   * @param authServiceUrl the CAS URL for getting service tickets
   * @param ticketGrantingTicket the TGT to use for getting the service ticket
   * @param appUrl the app URL for which the service ticket will be generated
   *
   * @return a string holding the service ticket information
   */
  public String getServiceTicket( String authServiceUrl, String ticketGrantingTicket, String appUrl ) throws IOException
  {
    if ( ticketGrantingTicket == null ) { return null; }
    PostMethod post = new PostMethod( authServiceUrl + "/" + ticketGrantingTicket );
    post.setRequestBody( new NameValuePair[]{new NameValuePair( "service", appUrl )} );
    try
    {
      this.httpClient.executeMethod(post);
      String response = post.getResponseBodyAsString();
      switch (post.getStatusCode())
      {
        case 200:
          return response;
        default:
          String msg = "Invalid response code (" + post.getStatusCode()
                       + ") from CAS server!" + "\nResponse: " + response;
          throw new RuntimeException( msg );
      }
    }
    finally
    {
      post.releaseConnection();
    }
  }

  /**
   * Retrieves a ticket-granting ticket from the CAS server
   * @param serviceUrl  the URL of the CAS TGT service
   * @param username the username for which to grant a TGT
   * @param password the username's password
   * @return a string holding the TGT value
   */
  public String getTicketGrantingTicket( String serviceUrl, String username, String password ) throws IOException
  {
    PostMethod post = new PostMethod( serviceUrl );
    post.setRequestBody( new NameValuePair[]{
        new NameValuePair( "username", username ),
        new NameValuePair( "password", password )
    } );
    try
    {
      this.httpClient.executeMethod( post );
      String response = post.getResponseBodyAsString();
      switch ( post.getStatusCode() )
      {
        case 201:
          Matcher matcher = Pattern.compile( ".*action=\".*/(.*?)\".*" ).matcher(response);
          if (matcher.matches()) { return matcher.group(1); }
          throw new RuntimeException( "Successful ticket granting request, but no ticket found!" );
        default:
          String msg = "Invalid response code (" + post.getStatusCode()
                       + ") from CAS server!" + "\nResponse: " + response ;
          throw new RuntimeException( msg );
      }
    }
    finally
    {
      post.releaseConnection();
    }
  }

  /**
   * Authenticates the HttpClient using a CAS service ticket (different from TGT)
   * @param serviceUrl the URL with which to authenticate
   * @param serviceTicket the service ticket to use to authenticate
   */
  public void authenticateClient( String serviceUrl, String serviceTicket) throws IOException
  {
    System.out.println( "Authenticating client with URL: " + serviceUrl );
    GetMethod method = new GetMethod( serviceUrl );
    method.setQueryString( new NameValuePair[]{new NameValuePair( "ticket", serviceTicket )} );
    try
    {
      this.httpClient.executeMethod( method );
      switch ( method.getStatusCode() )
      {
        case 200:
          log.info( "Authenticated with application. . . ." );
          break;
        default:
          String msg = "Invalid response code (" + method.getStatusCode()
              + ") from application server!"
              + "\nResponse: " + method.getResponseBodyAsString();
          throw new RuntimeException( msg );
      }
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Execute a GET request against the provided URL.  This method assumes an
   * already authenticated HttpClient which has stored the session cookie.
   * @param url the URL to execute
   * @return a String holding the response body
   */
  public String executeGet( String url ) throws IOException
  {
    System.out.println( "Getting from URL: " + url );
    GetMethod method = new GetMethod( url );
    method.setRequestHeader( new Header( "Accept", "application/json" ) );
    try
    {
      this.httpClient.executeMethod( method );
      String response = method.getResponseBodyAsString();
      if ( method.getStatusCode() != 200 )
      {
        String msg = "Invalid response code (" + method.getStatusCode()
                      + ") from application!" + "\nResponse: " + response;
        throw new RuntimeException( msg );
      }
      return response;
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Execute a POST request against the provided URL.  This method assumes an
   * already authenticated HttpClient which has stored the session cookie.
   * @param url the URL to execute
   * @param body the body of the request to send
   * @return a String holding the response body
   */
  public String executePost( String url, String body ) throws IOException
  {
    System.out.println( "Posting to URL: " + url );
    PostMethod method = new PostMethod( url );
    method.setRequestHeader( new Header( "Content-Type", "application/json" ) );
    method.setRequestHeader( new Header( "Accept", "application/json" ) );

    RequestEntity entity = new ByteArrayRequestEntity( body.getBytes( "UTF-8") );
    method.setRequestEntity( entity );

    try
    {
      this.httpClient.executeMethod( method );
      String response = method.getResponseBodyAsString();
      if ( method.getStatusCode() != 201 )
      {
        String msg = "Invalid response code (" + method.getStatusCode()
            + ") from application!" + "\nResponse: " + response;
        throw new RuntimeException( msg );
      }
      return response;
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Execute a DELETE request against the provided URL.  This method assumes an
   * already authenticated HttpClient which has stored the session cookie.
   * @param url the URL to execute
   * @return a String holding the response body
   */
  public String executeDelete( String url ) throws IOException
  {
    System.out.println( "Deleting from URL: " + url );
    DeleteMethod method = new DeleteMethod( url );
    try
    {
      this.httpClient.executeMethod( method );
      String response = method.getResponseBodyAsString();
      if ( method.getStatusCode() != 204 )
      {
        String msg = "Invalid response code (" + method.getStatusCode()
            + ") from application!" + "\nResponse: " + response;
        throw new RuntimeException( msg );
      }
      return response;
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Retrieves a page of items from an already initiated vector paging session.
   * This method assumes an already authenticated HttpClient which has stored
   * the session cookie.
   *
   * @param url the URL from which to retrieve a page of items
   * @param params the parameters to provide along with the request
   * @return information about the response
   */
  public VectorPagingResponse executePagingRequest( String url, Map<String, String> params ) throws IOException
  {
    System.out.println( "Posting to URL: " + url );
    PostMethod method = new PostMethod( url );

    if ( params != null && ( params.size() > 0 ) )
    {
      for ( String key : params.keySet() )
      {
        method.addParameter( key, params.get( key ) );
      }
    }
    method.setRequestHeader( new Header( "Accept", "application/json" ) );
    try
    {
      this.httpClient.executeMethod( method );
      String response = method.getResponseBodyAsString();
      switch ( method.getStatusCode() )
      {
        case 200:
          log.info( "Successful page request." );
          break;
        default:
          log.warn( "Invalid response code (" + method.getStatusCode() + ") from application!" );
          log.info( "Response: $response" );
          break;
      }
      int itemCount = Integer.parseInt( method.getResponseHeader( "Vector-Item-Count" ).getValue() );
      String pagingId = method.getResponseHeader( "Vector-Paging-Id" ).getValue();
      return new VectorPagingResponse( method.getStatusCode(), response, itemCount, pagingId );
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Logs out the client when work is complete.
   */
  public void logout()
  {
    DeleteMethod method = new DeleteMethod( this.authService + "/" + this.ticketGrantingTicket );
    try
    {
      this.httpClient.executeMethod( method );
      switch (method.getStatusCode())
      {
        case 200:
          log.info( "Logged out" );
          break;
        default:
          log.warn( "Invalid response code (" + method.getStatusCode() + ") from CAS server!" );
          log.info( "Response: " + method.getResponseBodyAsString() );
          break;
      }
    }
    catch (final IOException e)
    {
      log.warn( e.getMessage() );
    }
    finally
    {
      method.releaseConnection();
    }
  }

  /**
   * Authenticates this client with CAS and the application.
   *
   * @param username the username with which to authenticate
   * @param password the username's password
   */
  public void authenticate( String username, String password ) throws IOException
  {
    // get a ticket-granting ticket from CAS
    this.ticketGrantingTicket = this.getTicketGrantingTicket( authService, username, password );
    System.out.println( "TicketGrantingTicket is " + this.ticketGrantingTicket );

    // get a service ticket to access the app
    String appServiceAuthEndpoint = this.appService + "/j_spring_cas_security_check";
    System.out.println( "Authenticating to URL: " + appServiceAuthEndpoint );

    String serviceTicket = this.getServiceTicket( this.authService, this.ticketGrantingTicket, appServiceAuthEndpoint );
    System.out.println( "ServiceTicket is " + serviceTicket );

    // authenticate with the ticket. . . .
    // CAS puts session cookie in the client's store, so make
    // sure to use the same client for all requests
    this.authenticateClient( appServiceAuthEndpoint, serviceTicket );
  }

  /**
   * Authenticates with the UVI, requests pages of items, executes another
   * requests for non-paged items, then logs out from the application.
   *
   * @param args no args needed at this time
   */
  public static void main(String[] args) throws Exception
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

    // Set up a bounding box to search
    String bboxParams = "left=-145.936399490872&upper=51.6307842812699"
                        + "&right=-33.6258400156824&lower=19.2701146019781";

    // assume we already know from previous requests that we want items from the
    // "Gazeteer" (sic) source, with an "item_type" of "Airport", and which are
    // a Point geometry

    System.out.println( "Retrieving 'Point' items of type 'Airport' "
                            + "from the 'Gazeteer' (sic) source." );

    // initiate a paging request
    String pagingRequest = appBase
        + "/api/esri/Gazeteer/Point/Airport/paging"
        + "?" + bboxParams
        + "&ttl=1m&count=100";

    // the paging request response should be JSON with a single field "pagingId"
    // that pagingId is used in a follow-on request to retrieve the first page of items
    String jsonString = client.executeGet( pagingRequest );
    JSONObject json = (JSONObject) JSONValue.parse( jsonString );
    String pageId = (String) json.get( "pagingId" );

    // iterate through the pages, returning items in ESRI JSON format
    String singlePageRequest = appBase + "/api/esri/paging";
    boolean doRequest = true;
    int itemTotal = 0;
    while ( doRequest )
    {
      Map<String,String> params = new HashMap<>();
      params.put( "ttl", "5m" );
      params.put( "fields", "attributes" );
      params.put( "pagingId", pageId );

      VectorPagingResponse response = client.executePagingRequest( singlePageRequest, params );
      System.out.println( "Page item count: " + response.getItemCount() );
      itemTotal += response.getItemCount();
      System.out.println( "Total so far: " + itemTotal );
      pageId = response.getPageId();

      // if we needed to, we could parse the JSON and do something with the items
      // JSONObject itemJson = (JSONObject) JSONValue.parse( response.getBody() );

      // when a paging request returns 0 items, there are no more items to return
      doRequest = ( response.getItemCount() > 0 );
    }

    // ========================================================================
    // NOTE: you can make multiple calls using the same client instance. . . .

    System.out.println( "Querying for type 'School' matching the keyword "
                        + "'technical' in the same bounding box. . . .");

    // Query for items with text matching "technical" in the same bounding box
    // returning 500 items in GeoJSON format with only the name and geometry
    // returned for each item.
    String queryRequest = appBase
        + "/app/broker/vector/api/vectors/query/items"
        + "?" + bboxParams + "&q=technical&count=500";

    // when we get a collection of items it's a JSON array
    String itemsJson = client.executeGet( queryRequest );
    JSONArray items = (JSONArray) JSONValue.parse( itemsJson );
    System.out.println( "Returned " + items.size() + " items." );

    for ( Object item : items )
    {
      JSONObject i = (JSONObject) item;
      System.out.println( "----------------------------------------------");
      // Get the item ID and print it
      String id = ( (JSONObject) i.get( "properties" ) ).get( "id" ).toString();
      System.out.println( "ID: " + id );

      // Get the item geometry and print it
      JSONObject geom = (JSONObject) i.get( "geometry" );
      System.out.println( "Geometry: " + geom );

      // Get the item attributes and print them
      System.out.println( "Attributes:" );
      JSONObject itemAttributes = (JSONObject) ( (JSONObject) i.get( "properties" ) ).get( "attributes" );
      for ( Object key : itemAttributes.keySet() )
      {
        System.out.println( "\t" + key + ": " + itemAttributes.get( key ) );
      }
    }

    // logout from CAS
    client.logout();
  }
}
