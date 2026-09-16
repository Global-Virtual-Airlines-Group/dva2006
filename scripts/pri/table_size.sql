START TRANSACTION;

DELETE FROM common.TABLE_INFO;

INSERT INTO common.TABLE_INFO (SELECT t.TABLE_SCHEMA, t.TABLE_NAME, t.ROW_FORMAT,
    CASE
        WHEN t.CREATE_OPTIONS LIKE '%COMPRESSION=%' THEN
            REPLACE(SUBSTRING_INDEX(SUBSTRING_INDEX(t.CREATE_OPTIONS,'COMPRESSION=',-1),' ',1),'"','')
        WHEN t.ROW_FORMAT = 'Compressed' THEN CONCAT('key_block=', ts.zip_page_size)
        ELSE 'none'
    END AS compression,
    t.TABLE_ROWS, t.DATA_LENGTH / 1024, t.INDEX_LENGTH / 1024, ts.file_size / 1024 AS file_bytes,
    ts.allocated_size / 1024 AS disk_bytes, 
    ROUND(100 * (1 - ts.allocated_size / NULLIF(ts.file_size, 0)), 1) AS page_saved_pct
FROM information_schema.TABLES t
LEFT JOIN (
    SELECT
        SUBSTRING_INDEX(NAME, '#', 1) AS ts_name,
        SUM(FILE_SIZE)                AS file_size,
        SUM(ALLOCATED_SIZE)           AS allocated_size,
        MAX(ZIP_PAGE_SIZE)            AS zip_page_size,
        MAX(FS_BLOCK_SIZE)            AS fs_block_size,
        COUNT(*)                      AS files
    FROM information_schema.INNODB_TABLESPACES
    WHERE SPACE_TYPE = 'Single'
    GROUP BY 1
) ts ON ts.ts_name = CONCAT(t.TABLE_SCHEMA, '/', t.TABLE_NAME)
WHERE t.TABLE_TYPE = 'BASE TABLE' AND t.ENGINE = 'InnoDB' AND t.TABLE_SCHEMA NOT IN ('mysql','information_schema','performance_schema','sys','stats'));

COMMIT;
