package com.digitalglobe.insight.vector;

import java.util.HashMap;

/**
 *
 * @author mgiaconia
 */
public class DGVectorProperties {

  private String ingestDate;
  private String id;
  private String text;
  private HashMap<String, Object> ingestAttributes;
  private String source;
  private String originalCrs;
  private String itemType;
  private String name;
  private String ingestSource;
  private HashMap<String, Object> attributes;
  private String format;
  private HashMap<String, String[]> access;

  private String itemDate;

  public String getIngestDate() {
    return ingestDate;
  }

  public void setIngestDate(String ingestDate) {
    this.ingestDate = ingestDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public HashMap<String, Object> getIngestAttributes() {
    return ingestAttributes;
  }

  public void setIngestAttributes(HashMap<String, Object> ingestAttributes) {
    this.ingestAttributes = ingestAttributes;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getOriginalCrs() {
    return originalCrs;
  }

  public void setOriginalCrs(String originalCrs) {
    this.originalCrs = originalCrs;
  }

  public String getItemType() {
    return itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIngestSource() {
    return ingestSource;
  }

  public void setIngestSource(String ingestSource) {
    this.ingestSource = ingestSource;
  }

  public HashMap<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(HashMap<String, Object> attributes) {
    this.attributes = attributes;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public HashMap<String, String[]> getAccess() {
    return access;
  }

  public void setAccess(HashMap<String, String[]> access) {
    this.access = access;
  }

  public String getItemDate() {
    return itemDate;
  }

  public void setItemDate(String itemDate) {
    this.itemDate = itemDate;
  }

}
