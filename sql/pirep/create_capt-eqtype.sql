CREATE TABLE IF NOT EXISTS PROMO_EQ (
 ID					INTEGER UNSIGNED NOT NULL,
 EQTYPE				VARCHAR(15) NOT NULL,
 PRIMARY KEY (ID, EQTYPE),
 FOREIGN KEY (ID) REFERENCES PIREPS(ID) ON DELETE CASCADE,
 FOREIGN KEY (EQTYPE) REFERENCES EQTYPES(EQTYPE) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX PR_EQ_IDX ON PROMO_EQ(EQTYPE);
