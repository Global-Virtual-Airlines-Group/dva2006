CREATE TABLE domain (
 domain				VARCHAR(255) NOT NULL DEFAULT '',
 description		VARCHAR(255) NOT NULL DEFAULT '',
 aliases			INT(10) NOT NULL DEFAULT 0,
 mailboxes			INT(10) NOT NULL DEFAULT 0,
 maxquota			INT(10) NOT NULL DEFAULT 0,
 transport			VARCHAR(255) DEFAULT NULL,
 backupmx			TINYINT NOT NULL DEFAULT 0,
 active				BOOL DEFAULT 1,
 PRIMARY KEY (domain)
);