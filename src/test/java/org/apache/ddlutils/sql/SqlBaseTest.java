package org.apache.ddlutils.sql;

public class SqlBaseTest {

  // this SQL can be executed successfully in DBeaver (mysql).
  static final String sql = "CREATE VIEW customer_list\n" +
                            "AS\n" +
                            "SELECT cu.customer_id AS ID, CONCAT(cu.first_name, _utf8mb4' ', cu.last_name) AS name, a.address AS address, a.postal_code AS `zip code`,\n" +
                            "\ta.phone AS phone, city.city AS city, country.country AS country, IF(cu.active, _utf8mb4'active',_utf8mb4'') AS notes, cu.store_id AS SID\n" +
                            "FROM customer AS cu JOIN address AS a ON cu.address_id = a.address_id JOIN city ON a.city_id = city.city_id\n" +
                            "\tJOIN country ON city.country_id = country.country_id";

  // this SQL can be executed successfully in DBeaver (mysql).
  static final String sql1 = "CREATE DEFINER=CURRENT_USER SQL SECURITY INVOKER VIEW actor_info\n" +
                             "AS\n" +
                             "SELECT\n" +
                             "a.actor_id,\n" +
                             "a.first_name,\n" +
                             "a.last_name,\n" +
                             "GROUP_CONCAT(DISTINCT CONCAT(c.name, ': ',\n" +
                             "\t\t(SELECT GROUP_CONCAT(f.title ORDER BY f.title SEPARATOR ', ')\n" +
                             "                    FROM sakila.film f\n" +
                             "                    INNER JOIN sakila.film_category fc\n" +
                             "                      ON f.film_id = fc.film_id\n" +
                             "                    INNER JOIN sakila.film_actor fa\n" +
                             "                      ON f.film_id = fa.film_id\n" +
                             "                    WHERE fc.category_id = c.category_id\n" +
                             "                    AND fa.actor_id = a.actor_id\n" +
                             "                 )\n" +
                             "             )\n" +
                             "             ORDER BY c.name SEPARATOR '; ')\n" +
                             "AS film_info\n" +
                             "FROM sakila.actor a\n" +
                             "LEFT JOIN sakila.film_actor fa\n" +
                             "  ON a.actor_id = fa.actor_id\n" +
                             "LEFT JOIN sakila.film_category fc\n" +
                             "  ON fa.film_id = fc.film_id\n" +
                             "LEFT JOIN sakila.category c\n" +
                             "  ON fc.category_id = c.category_id\n" +
                             "GROUP BY a.actor_id, a.first_name, a.last_name";
}
