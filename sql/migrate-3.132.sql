\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

UPDATE iris.system_attribute SET value = '3.132.0'
	WHERE name = 'database_version';

UPDATE iris.system_attribute SET name = 'dmsxml_modem_op_timeout_secs'
	WHERE name = 'dmslite_modem_op_timeout_secs';
UPDATE iris.system_attribute SET name = 'dmsxml_op_timeout_secs'
	WHERE name = 'dmslite_op_timeout_secs';

UPDATE iris.comm_protocol SET description = 'DMS XML' WHERE id = 9;
UPDATE iris.comm_protocol SET description = 'MSG_FEED' WHERE id = 10;

ALTER TABLE iris.controller ADD COLUMN fail_time timestamp WITH time zone;
