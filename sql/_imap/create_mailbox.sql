CREATE TABLE mailbox (
 username			VARCHAR(255) NOT NULL DEFAULT '',
 password			VARCHAR(96),
 crypt_pw			VARCHAR(96),
 name				VARCHAR(255) NOT NULL DEFAULT '',
 maildir			VARCHAR(255) NOT NULL DEFAULT '',
 quota				INTEGER UNSIGNED NOT NULL DEFAULT 0,
 domain				VARCHAR(255) NOT NULL DEFAULT '',
 active				BOOL DEFAULT 1,
 ID					INTEGER UNSIGNED,
 PRIMARY KEY (username)
);