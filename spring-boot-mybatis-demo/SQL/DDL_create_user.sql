-- https://www.sivalabs.in/springboot-working-with-jdbctemplate/
drop table if exists users;
CREATE TABLE users
(
    id SERIAL,
    name varchar(100) NOT NULL,
    email varchar(100) DEFAULT NULL,
    PRIMARY KEY (id)
);



insert into users(id, name, email) values(1,'Siva','siva@gmail.com');
insert into users(id, name, email) values(2,'Prasad','prasad@gmail.com');
insert into users(id, name, email) values(3,'Reddy','reddy@gmail.com');