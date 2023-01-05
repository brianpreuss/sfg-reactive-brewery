/*
 * by convention Spring Boot will execute this SQL on startup
 * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.data-initialization.using-basic-sql-scripts
 */

create table if not exists beer (
    id uuid default random_uuid() primary key,
    beer_name varchar(255),
    beer_style varchar(255),
    upc varchar(25),
    version integer,
    quantity_on_hand integer,
    price decimal,
    created_date timestamp,
    last_modified_date timestamp
);
