package org.apache.ddlutils.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;

public class JSqlParserTest extends SqlBaseTest {

  /**
   * Since this is valid MySQL but invalid "Standard SQL" (as far as the parser is concerned)
   *
   * <blockquote><pre>
   *   CREATE VIEW customer_list
   * AS
   * SELECT cu.customer_id AS ID, CONCAT(cu.first_name, ' ', cu.last_name) AS name, a.address AS address, a.postal_code AS `zip code`,
   * 	a.phone AS phone, city.city AS city, country.country AS country, IF(cu.active, 'active','') AS notes, cu.store_id AS SID
   * FROM customer AS cu JOIN address AS a ON cu.address_id = a.address_id JOIN city ON a.city_id = city.city_id
   * 	JOIN country ON city.country_id = country.country_id;
   * </pre></blockquote>
   */
  @Test
  public void shouldThrowExceptionWhenParseSql() throws JSQLParserException {
    String sql = SqlCharsetSanitizer.sanitize(SqlBaseTest.sql);
    Statement stmt = CCJSqlParserUtil.parse(sql);
    System.out.println(stmt);
  }
}
