/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalglobe.insight.vector;

import java.util.List;

/**
 *
 * @author mgiaconia
 */
public class AggResultItem {

  private List<KeyDocCount> buckets;
  private Integer doc_count_error_upper_bound = 0;
  private Integer sum_other_doc_count = 0;

  public List<KeyDocCount> getBuckets() {
    return buckets;
  }

  public void setBuckets(List<KeyDocCount> buckets) {
    this.buckets = buckets;
  }

  public Integer getDoc_count_error_upper_bound() {
    return doc_count_error_upper_bound;
  }

  public void setDoc_count_error_upper_bound(Integer doc_count_error_upper_bound) {
    this.doc_count_error_upper_bound = doc_count_error_upper_bound;
  }

  public Integer getSum_other_doc_count() {
    return sum_other_doc_count;
  }

  public void setSum_other_doc_count(Integer sum_other_doc_count) {
    this.sum_other_doc_count = sum_other_doc_count;
  }
  
}
