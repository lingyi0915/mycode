package com.hjh.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 黄俊辉
 * @Create: 2018-07-19
 * @Description:
 */
public class Doc {
  private int id;
  private String title;
  private String author;
  private String time;
  private String content;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Map<String, Object> getHashMap(){
    Map<String, Object> hashMap = new HashMap<>();
    hashMap.put("id", this.getId());
    hashMap.put("title", this.getTitle());
    hashMap.put("time", this.getTime());
    hashMap.put("author", this.getAuthor());
    return hashMap;
  }

  @Override
  public String toString() {
    return "Doc{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", time='" + time + '\'' +
            ", content='" + content + '\'' +
            '}';
  }
}
