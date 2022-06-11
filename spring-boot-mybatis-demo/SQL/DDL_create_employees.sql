-- https://www.javaguides.net/2019/08/spring-boot-mybatis-mysql-example.html
create table employees
(
   id integer not null,
   first_name varchar(255) not null,
   last_name varchar(255) not null,
   email_address varchar(255) not null,
   primary key(id)
);