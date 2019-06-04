package com.hjh.leetcode;

import java.util.HashMap;
import java.util.Map;

public class p535_Encode_and_Decode_TinyURL {
  Map<Integer, String> map = new HashMap<Integer, String>();
  public String encode(String longUrl) {
    map.put(longUrl.hashCode(),longUrl);
    return "http://tinyurl.com/"+longUrl.hashCode();
  }
  public String decode(String shortUrl) {
    return map.get(Integer.parseInt(shortUrl.replace("http://tinyurl.com/", "")));
  }
}
