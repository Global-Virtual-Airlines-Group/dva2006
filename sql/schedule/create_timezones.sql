CREATE TABLE TZ (
 CODE				VARCHAR(32) NOT NULL,
 NAME				VARCHAR(48) NOT NULL,
 ABBR				VARCHAR(9) NOT NULL,
 GMT_OFFSET			SMALLINT,
 DST				BOOLEAN DEFAULT 0,
 PRIMARY KEY (CODE)
);