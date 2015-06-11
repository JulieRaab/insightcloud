package com.digitalglobe.insight.vector;

import java.util.List;

/**
 *
 * @author mgiaconia
 */
public class DGVectorFacet {
  private List<DGVectorFacetItem> data;
  private int shards=0;

  public List<DGVectorFacetItem> getData() {
    return data;
  }

  public void setData(List<DGVectorFacetItem> data) {
    this.data = data;
  }

  public int getShards() {
    return shards;
  }

  public void setShards(int shards) {
    this.shards = shards;
  }
  
}
