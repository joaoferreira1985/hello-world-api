package com.revolut.helloworld.model;

import io.vertx.core.json.JsonObject;

public class Person {
  private String username;
  private String dateOfBirth;

  public Person(String name, String dateOfBirth) {
    this.username = name;
    this.dateOfBirth = dateOfBirth;

  }
  public Person(JsonObject json) {
    this.username = json.getString("_id");
    this.dateOfBirth = json.getString("dateOfBirth");
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject()
        .put("dateOfBirth", dateOfBirth);
    return json;
  }

  public JsonObject jsonHttpResponse(String username, int days) {
    String missingDays=String.valueOf(days);
    if (days==0){
      return new JsonObject()
              .put("message", "Hello,"+username+"! Happy birthday!");
    }else{
      return new JsonObject()
              .put("message", "Hello,"+username+"! Your birthday is in "+missingDays+" day(s)");
    }
  }

  public String getUsername() {
    return username;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

}