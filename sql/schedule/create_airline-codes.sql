CREATE TABLE AIRLINE_CODES (
 CODE					CHAR(3) NOT NULL,
 ALTCODE				CHAR(3) NOT NULL,
 PRIMARY KEY (CODE, ALTCODE),
 FOREIGN KEY (CODE) REFERENCES AIRLINES(CODE) ON DELETE CASCADE ON UPDATE CASCADE
);