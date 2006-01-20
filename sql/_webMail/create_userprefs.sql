CREATE TABLE userprefs (
 user				varchar(128) DEFAULT '' NOT NULL,
 prefkey			varchar(64) DEFAULT '' NOT NULL,
 prefval			BLOB DEFAULT '' NOT NULL,
 PRIMARY KEY (user, prefkey)
);