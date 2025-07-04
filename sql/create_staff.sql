CREATE TABLE STAFF (
 ID				INTEGER UNSIGNED NOT NULL,
 TITLE			VARCHAR(94) CHARACTER SET utf8mb4 NOT NULL,
 SORT_ORDER		SMALLINT UNSIGNED NOT NULL,
 BIO			TEXT CHARACTER SET utf8mb4 NOT NULL,
 AREA			VARCHAR(64) NOT NULL,
 PRIMARY KEY (ID),
 FOREIGN KEY (ID) REFERENCES PILOTS(ID) ON DELETE CASCADE
);
