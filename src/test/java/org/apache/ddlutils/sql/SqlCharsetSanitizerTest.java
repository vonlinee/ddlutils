package org.apache.ddlutils.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlCharsetSanitizerTest {

  @Test
  public void testRemoveStandardUtf8mb4() {
    String input = "SELECT _utf8mb4 'Hello'";
    String expected = "SELECT 'Hello'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testRemoveComplexCharsetName() {
    // As you requested: weird charset names like utf8mbc_general_ci
    String input = "SELECT _utf8mbc_general_ci 'Hello'";
    String expected = "SELECT 'Hello'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testKeepCharsetInsideString() {
    // Should NOT remove _utf8mb4 if it's part of the data
    String input = "INSERT INTO t VALUES ('This string contains _utf8mb4 literal')";
    String expected = "INSERT INTO t VALUES ('This string contains _utf8mb4 literal')";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testKeepNormalColumnsStartingWithUnderscore() {
    // Should NOT remove column names like _my_col
    String input = "SELECT _my_col FROM table WHERE _id = 1";
    String expected = "SELECT _my_col FROM table WHERE _id = 1";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testMultipleOccurrences() {
    String input = "SELECT _latin1 'A', _utf8mb4 'B', _binary 'C'";
    String expected = "SELECT 'A', 'B', 'C'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testIgnoreInsideComments() {
    // _utf8mb4 inside a comment should be preserved
    String input = "SELECT /* _utf8mb4 'comment' */ 1";
    String expected = "SELECT /* _utf8mb4 'comment' */ 1";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testIgnoreInsideLineComments() {
    String input = "SELECT 1 -- _utf8mb4 'comment'";
    String expected = "SELECT 1 -- _utf8mb4 'comment'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testHandleEscapedQuotesInString() {
    // If the string contains escaped quotes, the sanitizer must not get confused
    String input = "SELECT _utf8mb4 'It\\'s a test'";
    String expected = "SELECT 'It\\'s a test'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testHandleStandardSqlEscapedQuotes() {
    // SQL often uses double single-quotes '' to escape
    String input = "SELECT _utf8mb4 'It''s a test'";
    String expected = "SELECT 'It''s a test'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testTightSpacing() {
    // No space between charset and quote
    String input = "SELECT _utf8mb4'NoSpace'";
    String expected = "SELECT 'NoSpace'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }

  @Test
  public void testWideSpacing() {
    // Lots of space/newlines
    String input = "SELECT _utf8mb4   \n   'Wide Space'";
    String expected = "SELECT 'Wide Space'";
    assertEquals(expected, SqlCharsetSanitizer.sanitize(input));
  }
}
