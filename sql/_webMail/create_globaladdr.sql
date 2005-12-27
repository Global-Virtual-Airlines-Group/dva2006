CREATE TABLE address (
 owner			VARCHAR(128) DEFAULT '' NOT NULL,
 nickname		VARCHAR(16) DEFAULT '' NOT NULL,
 firstname		VARCHAR(48) DEFAULT '' NOT NULL,
 lastname		VARCHAR(96) DEFAULT '' NOT NULL,
 email			VARCHAR(128) DEFAULT '' NOT NULL,
 label			VARCHAR(255),
 PRIMARY KEY (owner, nickname),
 KEY firstname (firstname, lastname)
);
