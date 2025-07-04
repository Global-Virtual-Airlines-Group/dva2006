CREATE TABLE MSG_TEMPLATE_ACTIONS (
 NAME                VARCHAR(48) NOT NULL,
 ACTION              SMALLINT UNSIGNED NOT NULL,
 PRIMARY KEY (NAME, ACTION),
 FOREIGN KEY (NAME) REFERENCES MSG_TEMPLATES(NAME) ON UPDATE CASCADE ON DELETE CASCADE
) CHARACTER SET latin1;