# ================================================= 
# Structure of Table: sakila.actor 
# ================================================== 
CREATE TABLE actor
(
    actor_id SMALLINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (actor_id)
) ;

CREATE INDEX idx_actor_last_name ON actor (last_name);

# ================================================= 
# Structure of Table: sakila.address 
# ================================================== 
CREATE TABLE address
(
    address_id SMALLINT NOT NULL AUTO_INCREMENT,
    address VARCHAR(50) NOT NULL,
    address2 VARCHAR(50) NULL,
    district VARCHAR(20) NOT NULL,
    city_id SMALLINT NOT NULL,
    postal_code VARCHAR(10) NULL,
    phone VARCHAR(20) NOT NULL,
    location BINARY(65535) NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (address_id)
) ;

CREATE INDEX idx_fk_city_id ON address (city_id);

CREATE INDEX idx_location ON address (location);

# ================================================= 
# Structure of Table: sakila.category 
# ================================================== 
CREATE TABLE category
(
    category_id SMALLINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(25) NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (category_id)
) ;

# ================================================= 
# Structure of Table: sakila.city 
# ================================================== 
CREATE TABLE city
(
    city_id SMALLINT NOT NULL AUTO_INCREMENT,
    city VARCHAR(50) NOT NULL,
    country_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (city_id)
) ;

CREATE INDEX idx_fk_country_id ON city (country_id);

# ================================================= 
# Structure of Table: sakila.country 
# ================================================== 
CREATE TABLE country
(
    country_id SMALLINT NOT NULL AUTO_INCREMENT,
    country VARCHAR(50) NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (country_id)
) ;

# ================================================= 
# Structure of Table: sakila.customer 
# ================================================== 
CREATE TABLE customer
(
    customer_id SMALLINT NOT NULL AUTO_INCREMENT,
    store_id SMALLINT NOT NULL,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    email VARCHAR(50) NULL,
    address_id SMALLINT NOT NULL,
    active TINYINT(1) DEFAULT 1 NOT NULL,
    create_date DATETIME NOT NULL,
    last_update DATETIME,
    PRIMARY KEY (customer_id)
) ;

CREATE INDEX idx_fk_address_id ON customer (address_id);

CREATE INDEX idx_fk_store_id ON customer (store_id);

CREATE INDEX idx_last_name ON customer (last_name);

# ================================================= 
# Structure of Table: sakila.film 
# ================================================== 
CREATE TABLE film
(
    film_id SMALLINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description MEDIUMTEXT NULL,
    release_year DATE,
    language_id SMALLINT NOT NULL,
    original_language_id SMALLINT,
    rental_duration SMALLINT DEFAULT 3 NOT NULL,
    rental_rate DECIMAL(4,2) DEFAULT 4.99 NOT NULL,
    length SMALLINT,
    replacement_cost DECIMAL(5,2) DEFAULT 19.99 NOT NULL,
    rating CHAR(5) DEFAULT 'G' NULL,
    special_features CHAR(54) NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (film_id)
) ;

CREATE INDEX idx_fk_language_id ON film (language_id);

CREATE INDEX idx_fk_original_language_id ON film (original_language_id);

CREATE INDEX idx_title ON film (title);

# ================================================= 
# Structure of Table: sakila.film_actor 
# ================================================== 
CREATE TABLE film_actor
(
    actor_id SMALLINT NOT NULL,
    film_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL
) ;

# ================================================= 
# Structure of Table: sakila.film_category 
# ================================================== 
CREATE TABLE film_category
(
    film_id SMALLINT NOT NULL,
    category_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL
) ;

# ================================================= 
# Structure of Table: sakila.film_text 
# ================================================== 
CREATE TABLE film_text
(
    film_id SMALLINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description MEDIUMTEXT NULL
) ;

# ================================================= 
# Structure of Table: sakila.inventory 
# ================================================== 
CREATE TABLE inventory
(
    inventory_id INTEGER NOT NULL AUTO_INCREMENT,
    film_id SMALLINT NOT NULL,
    store_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (inventory_id)
) ;

CREATE INDEX idx_fk_film_id ON inventory (film_id);

CREATE INDEX idx_store_id_film_id ON inventory (store_id, film_id);

# ================================================= 
# Structure of Table: sakila.language 
# ================================================== 
CREATE TABLE language
(
    language_id SMALLINT NOT NULL AUTO_INCREMENT,
    name CHAR(20) NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (language_id)
) ;

# ================================================= 
# Structure of Table: sakila.payment 
# ================================================== 
CREATE TABLE payment
(
    payment_id SMALLINT NOT NULL AUTO_INCREMENT,
    customer_id SMALLINT NOT NULL,
    staff_id SMALLINT NOT NULL,
    rental_id INTEGER,
    amount DECIMAL(5,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    last_update DATETIME,
    PRIMARY KEY (payment_id)
) ;

CREATE INDEX idx_fk_customer_id ON payment (customer_id);

CREATE INDEX idx_fk_staff_id ON payment (staff_id);

# ================================================= 
# Structure of Table: sakila.rental 
# ================================================== 
CREATE TABLE rental
(
    rental_id INTEGER NOT NULL AUTO_INCREMENT,
    rental_date DATETIME NOT NULL,
    inventory_id INTEGER NOT NULL,
    customer_id SMALLINT NOT NULL,
    return_date DATETIME,
    staff_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (rental_id)
) ;

CREATE UNIQUE INDEX rental_date ON rental (rental_date, inventory_id, customer_id);

CREATE INDEX idx_fk_customer_id ON rental (customer_id);

CREATE INDEX idx_fk_inventory_id ON rental (inventory_id);

CREATE INDEX idx_fk_staff_id ON rental (staff_id);

# ================================================= 
# Structure of Table: sakila.staff 
# ================================================== 
CREATE TABLE staff
(
    staff_id SMALLINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    address_id SMALLINT NOT NULL,
    picture MEDIUMBLOB NULL,
    email VARCHAR(50) NULL,
    store_id SMALLINT NOT NULL,
    active TINYINT(1) DEFAULT 1 NOT NULL,
    username VARCHAR(16) NOT NULL,
    password VARCHAR(40) NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (staff_id)
) ;

CREATE INDEX idx_fk_address_id ON staff (address_id);

CREATE INDEX idx_fk_store_id ON staff (store_id);

# ================================================= 
# Structure of Table: sakila.store 
# ================================================== 
CREATE TABLE store
(
    store_id SMALLINT NOT NULL AUTO_INCREMENT,
    manager_staff_id SMALLINT NOT NULL,
    address_id SMALLINT NOT NULL,
    last_update DATETIME NOT NULL,
    PRIMARY KEY (store_id)
) ;

CREATE UNIQUE INDEX idx_unique_manager ON store (manager_staff_id);

CREATE INDEX idx_fk_address_id ON store (address_id);

