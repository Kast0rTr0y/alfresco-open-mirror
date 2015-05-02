--
-- Title:      DROP Indexes
-- Database:   Oracle
-- Since:      V4.1 Schema 6030
-- Author:     Alex Mukha
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--
-- MNT-9275: When upgrading on Oracle RAC from version 3.2.2 to version 3.3 or higher, values returned by sequences are not ordered.

ALTER SEQUENCE ALF_ACCESS_CONTROL_ENTRY_SEQ ORDER;
ALTER SEQUENCE ALF_ACCESS_CONTROL_LIST_SEQ ORDER;
ALTER SEQUENCE ALF_ACE_CONTEXT_SEQ ORDER;
ALTER SEQUENCE ALF_ACL_CHANGE_SET_SEQ ORDER;
ALTER SEQUENCE ALF_ACL_MEMBER_SEQ ORDER;
ALTER SEQUENCE ALF_ACTIVITY_FEED_CONTROL_SEQ ORDER;
ALTER SEQUENCE ALF_ACTIVITY_FEED_SEQ ORDER;
ALTER SEQUENCE ALF_ACTIVITY_POST_SEQ ORDER;
ALTER SEQUENCE ALF_AUDIT_APP_SEQ ORDER;
ALTER SEQUENCE ALF_AUDIT_ENTRY_SEQ ORDER;
ALTER SEQUENCE ALF_AUDIT_MODEL_SEQ ORDER;
ALTER SEQUENCE ALF_AUTHORITY_ALIAS_SEQ ORDER;
ALTER SEQUENCE ALF_AUTHORITY_SEQ ORDER;
ALTER SEQUENCE ALF_CHILD_ASSOC_SEQ ORDER;
ALTER SEQUENCE ALF_CONTENT_DATA_SEQ ORDER;
ALTER SEQUENCE ALF_CONTENT_URL_SEQ ORDER;
ALTER SEQUENCE ALF_ENCODING_SEQ ORDER;
ALTER SEQUENCE ALF_LOCALE_SEQ ORDER;
ALTER SEQUENCE ALF_LOCK_RESOURCE_SEQ ORDER;
ALTER SEQUENCE ALF_LOCK_SEQ ORDER;
ALTER SEQUENCE ALF_MIMETYPE_SEQ ORDER;
ALTER SEQUENCE ALF_NAMESPACE_SEQ ORDER;
ALTER SEQUENCE ALF_NODE_ASSOC_SEQ ORDER;
ALTER SEQUENCE ALF_NODE_SEQ ORDER;
ALTER SEQUENCE ALF_PERMISSION_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_CLASS_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_DATE_VALUE_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_DOUBLE_VALUE_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_LINK_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_ROOT_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_SERIAL_VALUE_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_STRING_VALUE_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_UNIQUE_CTX_SEQ ORDER;
ALTER SEQUENCE ALF_PROP_VALUE_SEQ ORDER;
ALTER SEQUENCE ALF_QNAME_SEQ ORDER;
ALTER SEQUENCE ALF_SERVER_SEQ ORDER;
ALTER SEQUENCE ALF_STORE_SEQ ORDER;
ALTER SEQUENCE ALF_TRANSACTION_SEQ ORDER;
ALTER SEQUENCE ALF_USAGE_DELTA_SEQ ORDER;
ALTER SEQUENCE HIBERNATE_SEQUENCE ORDER;

--
-- Record script finish
--

DELETE FROM alf_applied_patch WHERE id = 'patch.db-V4.1-fix-Repo-seqs-order';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V4.1-fix-Repo-seqs-order', 'Manually executed script to set ORDER bit for sequences',
    0, 6030, -1, 6031, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
  );