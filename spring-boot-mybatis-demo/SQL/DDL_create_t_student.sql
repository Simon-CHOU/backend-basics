-- https://programmer.help/blogs/spring-boot-tutorial-mybatis.html
create table t_student
(
   id integer not null,
   student_name varchar(255) not null,
   student_no varchar(255) not null,
   sex varchar(2) not null,
   age integer not null,
   primary key(id)
);

INSERT INTO t_student
(id, student_name, student_no, sex, age)
VALUES(1, 'Cucukan', '88997', '男', 0);

INSERT INTO t_student
(id, student_name, student_no, sex, age)
VALUES(2, 'Quigerk', '80097', '女', 0);