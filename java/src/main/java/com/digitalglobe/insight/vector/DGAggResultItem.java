/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalglobe.insight.vector;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author mgiaconia
 */
public class DGAggResultItem {

  private double north;
  private double south;
  private double east;
  private double west;
  private List<DGVectorFacetItem> data;

  public double getNorth() {
    return north;
  }

  public void setNorth(double north) {
    this.north = north;
  }

  public double getSouth() {
    return south;
  }

  public void setSouth(double south) {
    this.south = south;
  }

  public double getEast() {
    return east;
  }

  public void setEast(double east) {
    this.east = east;
  }

  public double getWest() {
    return west;
  }

  public void setWest(double west) {
    this.west = west;
  }

  public List<DGVectorFacetItem> getData() {
    return data;
  }

  public void setData(List<DGVectorFacetItem> data) {
    this.data = data;
  }


  
  
}
