DROP DATABASE IF EXISTS recall_data;
CREATE DATABASE recall_data;

USE recall_data;

CREATE TABLE recall(
   recall_id int not null auto_increment primary key,
   recall_serial varchar(200) not null,
   issue_date timestamp not null,
   description text not null,
   danger text not null,
   instructions text not null,
   version int not null,
   UNIQUE KEY uq_recall (recall_serial)
) ENGINE=InnoDB;

CREATE TABLE urn(
   urn_id int not null auto_increment primary key,
   recall_id int not null,
   urn varchar(52) not null,
   FOREIGN KEY fk_recall(recall_id) REFERENCES recall(recall_id) ON DELETE CASCADE
) ENGINE=InnoDB;