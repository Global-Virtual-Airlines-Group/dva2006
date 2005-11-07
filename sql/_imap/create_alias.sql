CREATE TABLE alias (
 address			VARCHAR(255) NOT NULL,
 goto				VARCHAR(255) NOT NULL,
 domain				varchar(255) NOT NULL DEFAULT '',
 created			datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
 modified			datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
 active				BOOL DEFAULT 1,
 PRIMARY KEY (address),
 FOREIGN KEY (goto) REFERENCES mailbox(username)
);