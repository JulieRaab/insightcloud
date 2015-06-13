package com.digitalglobe.insight.vector;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;

/**
 * An example class showing possible ways of interacting with the UVI REST API
 */
public class TwitterAggregationsSample {

  /**
   * Authenticates with the UVI, requests pages of items, executes another
   * requests for non-paged items, then logs out from the application.
   *
   * @param args no args needed at this time
   */
  public static void main(String[] args) throws IOException {
    args= new String[]{"example.properties"};
    
    if ( args.length < 1 )
    {
      throw new RuntimeException( "Configuration file must be specified." );
    }

    ServiceProperties props = new ServiceProperties( args[0] );

    // authentication information
    String authService = props.getAuthService();
    String username = "mark.giaconia";
    String password = "!nsightC!oud";

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

    
    String jsonStringOfTweetAggs = client.executeGet("https://iipbeta.digitalglobe.com/monocle-3/app/broker/sma/sma/twitter/tweets?"
            + "bbox=-78.086426,38.470804,-76.88916,39.314123&"
            + "datetimerange=2015-04-20T05:00:00.000Z,2015-04-20T17:00:00.000Z&"
            + "dimensions=geohash");
    String withAKeyWordFilter = "https://iipbeta.digitalglobe.com/monocle-3/app/broker/sma/sma/twitter/tweets?"
            + "bbox=-78.086426,38.470804,-76.88916,39.314123&datetimerange=2015-04-20T05:00:00.000Z,2015-04-20T17:00:00.000Z&"
            + "query=omg&"///the KEYWORD
            + "dimensions=geohash";
    
    JSONObject tweetAggs = (JSONObject) JSONValue.parse(jsonStringOfTweetAggs);
    client.logout();
    //go nuts with it... this is what you will get back. The lat and lon averages can be used to plot a center of mass of the tweets in each geohash grid box
    /*
    {
    "took": 420,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
    },
    "hits": {
        "total": 5024796,
        "max_score": 0,
        "hits": []
    },
    "aggregations": {
        "results": {
            "doc_count": 75,
            "geohash": {
                "buckets": [
                    {
                        "key": "dqbe1m",
                        "doc_count": 24,
                        "pos_sentiment_avg": {
                            "value": 0.5883796719814882
                        },
                        "geo_lat_avg": {
                            "value": 38.52516670833334
                        },
                        "geo_lon_avg": {
                            "value": -77.98618591666667
                        },
                        "neg_sentiment_avg": {
                            "value": 0.4116203280185117
                        }
                    },
                    {
                        "key": "dqbdcq",
                        "doc_count": 7,
                        "pos_sentiment_avg": {
                            "value": 0.6544502649134561
                        },
                        "geo_lat_avg": {
                            "value": 38.489284285714284
                        },
                        "geo_lon_avg": {
                            "value": -77.98814714285713
                        },
                        "neg_sentiment_avg": {
                            "value": 0.34554973508654385
                        }
                    },
                    {
                        "key": "dqbmz0",
                        "doc_count": 4,
                        "pos_sentiment_avg": {
                            "value": 0.6857335425585789
                        },
                        "geo_lat_avg": {
                            "value": 38.9796305
                        },
                        "geo_lon_avg": {
                            "value": -78.082052
                        },
                        "neg_sentiment_avg": {
                            "value": 0.3142664574414211
                        }
                    },
                    {
                        "key": "dqbdch",
                        "doc_count": 4,
                        "pos_sentiment_avg": {
                            "value": 0.5966530710156663
                        },
                        "geo_lat_avg": {
                            "value": 38.47574075
                        },
                        "geo_lon_avg": {
                            "value": -77.99604575000001
                        },
                        "neg_sentiment_avg": {
                            "value": 0.40334692898433366
                        }
                    },
                    {
                        "key": "dqbecg",
                        "doc_count": 3,
                        "pos_sentiment_avg": {
                            "value": 0.6395794299646361
                        },
                        "geo_lat_avg": {
                            "value": 38.647211666666664
                        },
                        "geo_lon_avg": {
                            "value": -77.96037766666666
                        },
                        "neg_sentiment_avg": {
                            "value": 0.36042057003536393
                        }
                    },
                    {
                        "key": "dqbdf5",
                        "doc_count": 3,
                        "pos_sentiment_avg": {
                            "value": 0.7241664869703329
                        },
                        "geo_lat_avg": {
                            "value": 38.472605333333334
                        },
                        "geo_lon_avg": {
                            "value": -77.951287
                        },
                        "neg_sentiment_avg": {
                            "value": 0.275833513029667
                        }
                    },
                    {
                        "key": "dqbdcn",
                        "doc_count": 3,
                        "pos_sentiment_avg": {
                            "value": 0.7796304496494155
                        },
                        "geo_lat_avg": {
                            "value": 38.489400999999994
                        },
                        "geo_lon_avg": {
                            "value": -77.99248866666666
                        },
                        "neg_sentiment_avg": {
                            "value": 0.22036955035058445
                        }
                    },
                    {
                        "key": "dqbdcm",
                        "doc_count": 3,
                        "pos_sentiment_avg": {
                            "value": 0.6430848202777028
                        },
                        "geo_lat_avg": {
                            "value": 38.480309999999996
                        },
                        "geo_lon_avg": {
                            "value": -77.987482
                        },
                        "neg_sentiment_avg": {
                            "value": 0.3569151797222972
                        }
                    },
                    {
                        "key": "dqbdfg",
                        "doc_count": 2,
                        "pos_sentiment_avg": {
                            "value": 0.6611532349436506
                        },
                        "geo_lat_avg": {
                            "value": 38.473151
                        },
                        "geo_lon_avg": {
                            "value": -77.9173435
                        },
                        "neg_sentiment_avg": {
                            "value": 0.33884676505634936
                        }
                    },
                    {
                        "key": "dqbdcr",
                        "doc_count": 2,
                        "pos_sentiment_avg": {
                            "value": 0.7344279382222835
                        },
                        "geo_lat_avg": {
                            "value": 38.492487
                        },
                        "geo_lon_avg": {
                            "value": -77.9855005
                        },
                        "neg_sentiment_avg": {
                            "value": 0.2655720617777166
                        }
                    },
                    {
                        "key": "dqbdc5",
                        "doc_count": 2,
                        "pos_sentiment_avg": {
                            "value": 0.6702302096703836
                        },
                        "geo_lat_avg": {
                            "value": 38.4724025
                        },
                        "geo_lon_avg": {
                            "value": -77.99849025
                        },
                        "neg_sentiment_avg": {
                            "value": 0.32976979032961634
                        }
                    },
                    {
                        "key": "dqbxd7",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.4555186121638458
                        },
                        "geo_lat_avg": {
                            "value": 39.307479
                        },
                        "geo_lon_avg": {
                            "value": -77.943579
                        },
                        "neg_sentiment_avg": {
                            "value": 0.5444813878361541
                        }
                    },
                    {
                        "key": "dqbwc8",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.2664870760163088
                        },
                        "geo_lat_avg": {
                            "value": 39.158556
                        },
                        "geo_lon_avg": {
                            "value": -77.978644
                        },
                        "neg_sentiment_avg": {
                            "value": 0.7335129239836913
                        }
                    },
                    {
                        "key": "dqbwc1",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9226007350017029
                        },
                        "geo_lat_avg": {
                            "value": 39.161125
                        },
                        "geo_lon_avg": {
                            "value": -77.993694
                        },
                        "neg_sentiment_avg": {
                            "value": 0.07739926499829702
                        }
                    },
                    {
                        "key": "dqbt5j",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.5
                        },
                        "geo_lat_avg": {
                            "value": 38.877115
                        },
                        "geo_lon_avg": {
                            "value": -77.905332
                        },
                        "neg_sentiment_avg": {
                            "value": 0.5
                        }
                    },
                    {
                        "key": "dqbt40",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.7756700122487102
                        },
                        "geo_lat_avg": {
                            "value": 38.849297
                        },
                        "geo_lon_avg": {
                            "value": -77.958236
                        },
                        "neg_sentiment_avg": {
                            "value": 0.22432998775128982
                        }
                    },
                    {
                        "key": "dqbs49",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.6719529845814749
                        },
                        "geo_lat_avg": {
                            "value": 38.681689
                        },
                        "geo_lon_avg": {
                            "value": -77.934922
                        },
                        "neg_sentiment_avg": {
                            "value": 0.3280470154185251
                        }
                    },
                    {
                        "key": "dqbrx1",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9431414424828423
                        },
                        "geo_lat_avg": {
                            "value": 39.295235
                        },
                        "geo_lon_avg": {
                            "value": -78.081543
                        },
                        "neg_sentiment_avg": {
                            "value": 0.05685855751715775
                        }
                    },
                    {
                        "key": "dqbqzz",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.8473574590224914
                        },
                        "geo_lat_avg": {
                            "value": 39.19522
                        },
                        "geo_lon_avg": {
                            "value": -78.054479
                        },
                        "neg_sentiment_avg": {
                            "value": 0.15264254097750868
                        }
                    },
                    {
                        "key": "dqbqze",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.520371418590702
                        },
                        "geo_lat_avg": {
                            "value": 39.176648
                        },
                        "geo_lon_avg": {
                            "value": -78.061287
                        },
                        "neg_sentiment_avg": {
                            "value": 0.47962858140929787
                        }
                    },
                    {
                        "key": "dqbe1t",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9820575206706454
                        },
                        "geo_lat_avg": {
                            "value": 38.526022
                        },
                        "geo_lon_avg": {
                            "value": -77.979221
                        },
                        "neg_sentiment_avg": {
                            "value": 0.017942479329354593
                        }
                    },
                    {
                        "key": "dqbe16",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.7488897903140767
                        },
                        "geo_lat_avg": {
                            "value": 38.509976
                        },
                        "geo_lon_avg": {
                            "value": -77.987931
                        },
                        "neg_sentiment_avg": {
                            "value": 0.2511102096859234
                        }
                    },
                    {
                        "key": "dqbdfk",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9594755409505842
                        },
                        "geo_lat_avg": {
                            "value": 38.476609
                        },
                        "geo_lon_avg": {
                            "value": -77.946148
                        },
                        "neg_sentiment_avg": {
                            "value": 0.04052445904941581
                        }
                    },
                    {
                        "key": "dqbdcw",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.5750649788829669
                        },
                        "geo_lat_avg": {
                            "value": 38.489066
                        },
                        "geo_lon_avg": {
                            "value": -77.980274
                        },
                        "neg_sentiment_avg": {
                            "value": 0.4249350211170331
                        }
                    },
                    {
                        "key": "dqbdcv",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.8988133585592464
                        },
                        "geo_lat_avg": {
                            "value": 38.48465
                        },
                        "geo_lon_avg": {
                            "value": -77.968646
                        },
                        "neg_sentiment_avg": {
                            "value": 0.10118664144075373
                        }
                    },
                    {
                        "key": "dqbdct",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9963875118482693
                        },
                        "geo_lat_avg": {
                            "value": 38.484877
                        },
                        "geo_lon_avg": {
                            "value": -77.976865
                        },
                        "neg_sentiment_avg": {
                            "value": 0.0036124881517306663
                        }
                    },
                    {
                        "key": "dqbdcp",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9987025960530218
                        },
                        "geo_lat_avg": {
                            "value": 38.490661
                        },
                        "geo_lon_avg": {
                            "value": -77.992519
                        },
                        "neg_sentiment_avg": {
                            "value": 0.0012974039469780734
                        }
                    },
                    {
                        "key": "dqbdcj",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.9496750081428026
                        },
                        "geo_lat_avg": {
                            "value": 38.480356
                        },
                        "geo_lon_avg": {
                            "value": -77.99982
                        },
                        "neg_sentiment_avg": {
                            "value": 0.050324991857197375
                        }
                    },
                    {
                        "key": "dqbdbu",
                        "doc_count": 1,
                        "pos_sentiment_avg": {
                            "value": 0.7105759793035298
                        },
                        "geo_lat_avg": {
                            "value": 38.477259
                        },
                        "geo_lon_avg": {
                            "value": -78.012127
                        },
                        "neg_sentiment_avg": {
                            "value": 0.2894240206964702
                        }
                    }
                ]
            }
        }
    }
}
    
    
    */
    

    

  
  }
}
