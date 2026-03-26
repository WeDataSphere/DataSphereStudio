SET @@autocommit=0;
START TRANSACTION;

ALTER TABLE dss_workflow ADD metrics varchar(1024) NULL;

COMMIT;
SET @@autocommit=1;
