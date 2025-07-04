CREATE TABLE alias (
 address			VARCHAR(255) NOT NULL,
 goto				VARCHAR(255) NOT NULL,
 domain				VARCHAR(255) NOT NULL DEFAULT '',
 active				BOOL DEFAULT 1,
 PRIMARY KEY (address),
 FOREIGN KEY (goto) REFERENCES mailbox(username) ON DELETE CASCADE ON UPDATE CASCADE
);