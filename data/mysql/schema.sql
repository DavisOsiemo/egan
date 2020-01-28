DROP DATABASE IF EXISTS egan_web;
CREATE DATABASE egan_web DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_unicode_ci;
USE egan_web;

DROP TABLE IF EXISTS user;
CREATE TABLE user(
  id int(11) NOT NULL AUTO_INCREMENT,
  username varchar(32) NOT NULL,
  api_key varchar(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY id (id),
  UNIQUE KEY username (username));
  ENGINE=MyISAM DEFAULT CHARSET=latin1;