CREATE TABLE EXAMQUESTIONSM (
 EXAM_ID            	INTEGER UNSIGNED NOT NULL,
 QUESTION_ID        	INTEGER UNSIGNED NOT NULL,
 SEQ					TINYINT UNSIGNED NOT NULL,
 ANSWER					VARCHAR(255) CHARACTER SET utf8 NOT NULL,
 PRIMARY KEY (EXAM_ID, QUESTION_ID, SEQ),
 FOREIGN KEY (EXAM_ID, QUESTION_ID) REFERENCES EXAMQUESTIONS(EXAM_ID, QUESTION_ID) ON DELETE CASCADE
) ROW_FORMAT=Dynamic;