CREATE TABLE GALLERYSCORE (
 IMG_ID             INTEGER UNSIGNED NOT NULL,
 PILOT_ID           INTEGER UNSIGNED NOT NULL,
 PRIMARY KEY (IMG_ID, PILOT_ID),
 FOREIGN KEY (IMG_ID) REFERENCES GALLERY(ID) ON DELETE CASCADE,
 FOREIGN KEY (PILOT_ID) REFERENCES common.USERDATA(ID)
);