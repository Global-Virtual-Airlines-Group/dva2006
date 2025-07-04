CREATE TABLE CERTEXAMS (
 CERTNAME			VARCHAR(32) NOT NULL,
 EXAMNAME			VARCHAR(32) NOT NULL,
 PRIMARY KEY (CERTNAME, EXAMNAME),
 FOREIGN KEY (CERTNAME) REFERENCES CERTS(NAME) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (EXAMNAME) REFERENCES EXAMINFO(NAME) ON UPDATE CASCADE ON DELETE CASCADE
);