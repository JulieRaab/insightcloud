package com.digitalglobe.insight.vector;

import com.google.gson.Gson;
import java.util.List;

/**
 *
 * @author mgiaconia
 */
public class DGVectorItem {

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

  private String name;
  private String typeString;
  private RawGeom geometry;
  private DGVectorProperties properties;

  public DGVectorProperties getProperties() {
    return properties;
  }

  public void setProperties(DGVectorProperties properties) {
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTypeString() {
    return typeString;
  }

  public void setTypeString(String typeString) {
    this.typeString = typeString;
  }

  public RawGeom getGeometry() {
    return geometry;
  }

  public void setGeometry(RawGeom geometry) {
    this.geometry = geometry;
  }

  public static class RawGeom {

    private List coordinates;
    private String type;
    private DGVectorProperties properties;

    public List  getCoordinates() {
      return coordinates;
    }

    public void setCoordinates(List  coordinates) {
      this.coordinates = coordinates;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public DGVectorProperties getProperties() {
      return properties;
    }

    public void setProperties(DGVectorProperties properties) {
      this.properties = properties;
    }

  }

  //this class mimics this
  /*
   {
   "geometry": {
   "coordinates": [
   [
   106.7476029,
   -6.1974100000000005
   ],
   [
   106.7475843,
   -6.1992642
   ],
   [
   106.74756520000001,
   -6.200075600000001
   ],
   [
   106.74754510000001,
   -6.200928500000001
   ],
   [
   106.74752520000001,
   -6.2017711
   ],
   [
   106.74750460000001,
   -6.2026462
   ],
   [
   106.74748550000001,
   -6.2034564
   ]
   ],
   "type": "LineString"
   },
   "properties": {
   "ingestDate": "2015-04-17T18:27:18Z",
   "id": "OSM-way-30368389",
   "text": "residential | Jl. Taman Kebon Jeruk",
   "ingestAttributes": {
   "osm.host": "10.0.64.50",
   "osm.database": "osm"
   },
   "source": null,
   "originalCrs": "EPSG:4326",
   "itemType": "Road",
   "name": "Jl. Taman Kebon Jeruk",
   "ingestSource": "OSM",
   "attributes": {
   "_osm.version.int": 3,
   "_osm.user.id.dbl": 1054929,
   "highway": "residential",
   "_osm.user.name": "Melgo",
   "_osm.changeset.int": 13895744,
   "_osm.changeset.dbl": 13895744,
   "name": "Jl. Taman Kebon Jeruk",
   "_osm.version.dbl": 3,
   "_osm.user.id": "1054929",
   "_osm.changeset": "13895744",
   "_osm.version": "3",
   "_osm.user.id.int": 1054929
   },
   "format": "OSM",
   "access": {
   "users": [
   "_ALL_"
   ],
   "groups": [
   "_ALL_"
   ]
   },
   "itemDate": "2012-11-16T21:34:54Z"
   },
   "type": "Feature"
   }
   */
}
