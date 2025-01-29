package org.apache.ddlutils.util;

import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Utils {

  public static String decodeToString(byte[] base64Data) {
    return new String(decodeBase64(base64Data));
  }

  public static String decodeBase64(String str) {
    return decodeBase64(str, null, null);
  }

  public static String decodeBase64(String str, Charset decodeEncoding, Charset encodeEncoding) {
    if (str == null) {
      return null;
    }
    byte[] bytes;
    if (decodeEncoding == null) {
      bytes = str.getBytes();
    } else {
      bytes = str.getBytes(decodeEncoding);
    }
    byte[] decodedBytes = Base64.getDecoder().decode(bytes);
    if (encodeEncoding == null) {
      return new String(decodedBytes);
    }
    return new String(decodedBytes, encodeEncoding);
  }

  public static byte[] decodeBase64(byte[] base64Data) {
    return Base64.getDecoder().decode(base64Data);
  }

  public static String encode(String str, Charset fromEncoding, Charset toEncoding) {
    byte[] bytes = fromEncoding == null ? str.getBytes() : str.getBytes(fromEncoding);
    byte[] encodedBytes = Base64.getEncoder().encode(bytes);
    return new String(encodedBytes, toEncoding);
  }

  public static byte[] encodeBase64(byte[] base64Data) {
    return Base64.getEncoder().encode(base64Data);
  }
}
