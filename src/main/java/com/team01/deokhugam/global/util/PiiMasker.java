package com.team01.deokhugam.global.util;

public class PiiMasker {

  private PiiMasker() {
  }

  public static String maskEmail(String email) {
    if (email == null) {
      return null;
    }
    int at = email.indexOf('@');
    if (at <= 0) {
      return "***";
    }
    String local = email.substring(0, at);
    String domain = email.substring(at);
    if (local.length() == 1) {
      return local.charAt(0) + "****" + domain;
    }
    return local.substring(0, 2) + "***" + domain;
  }
}
