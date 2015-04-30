--
-- Title:      Property Value tables
-- Database:   DB2
-- Since:      V3.2 Schema 3001
-- Author:     Pavel Yurkevich
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_prop_class
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   java_class_name VARCHAR(1020) NOT NULL,
   java_class_name_short VARCHAR(128) NOT NULL,
   java_class_name_crc BIGINT NOT NULL,   
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_propc_crc ON alf_prop_class (java_class_name_crc, java_class_name_short);
CREATE INDEX idx_alf_propc_clas ON alf_prop_class (java_class_name);

CREATE TABLE alf_prop_date_value
(
   date_value BIGINT NOT NULL,
   full_year INTEGER NOT NULL,
   half_of_year SMALLINT NOT NULL,
   quarter_of_year SMALLINT NOT NULL,
   month_of_year SMALLINT NOT NULL,
   week_of_year SMALLINT NOT NULL,
   week_of_month SMALLINT NOT NULL,
   day_of_year INTEGER NOT NULL,
   day_of_month SMALLINT NOT NULL,
   day_of_week SMALLINT NOT NULL,   
   PRIMARY KEY (date_value)
);
CREATE INDEX idx_alf_propdt_dt ON alf_prop_date_value (full_year, month_of_year, day_of_month);

CREATE TABLE alf_prop_double_value
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   double_value DOUBLE NOT NULL, 
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_propd_val ON alf_prop_double_value (double_value);

-- Stores unique, case-sensitive string values --
CREATE TABLE alf_prop_string_value
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   string_value VARCHAR(4096) NOT NULL,
   string_end_lower VARCHAR(128) NOT NULL,
   string_crc BIGINT NOT NULL,   
   PRIMARY KEY (id)
);
CREATE INDEX idx_alf_props_str ON alf_prop_string_value (string_value);
CREATE UNIQUE INDEX idx_alf_props_crc ON alf_prop_string_value (string_end_lower, string_crc);

CREATE TABLE alf_prop_serializable_value
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   serializable_value BLOB,
   PRIMARY KEY (id)
);

CREATE TABLE alf_prop_value
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   actual_type_id BIGINT NOT NULL,
   persisted_type SMALLINT NOT NULL,
   long_value BIGINT NOT NULL,   
   PRIMARY KEY (id)
);
CREATE INDEX idx_alf_propv_per ON alf_prop_value (persisted_type, long_value);
CREATE UNIQUE INDEX idx_alf_propv_act ON alf_prop_value (actual_type_id, long_value);

CREATE TABLE alf_prop_root
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   version INTEGER NOT NULL,
   PRIMARY KEY (id)
);

CREATE TABLE alf_prop_link
(
   root_prop_id BIGINT NOT NULL,
   prop_index BIGINT NOT NULL,
   contained_in BIGINT NOT NULL,
   key_prop_id BIGINT NOT NULL,
   value_prop_id BIGINT NOT NULL,
   CONSTRAINT fk_alf_propln_root FOREIGN KEY (root_prop_id) REFERENCES alf_prop_root (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_propln_key FOREIGN KEY (key_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_propln_val FOREIGN KEY (value_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,   
   PRIMARY KEY (root_prop_id, contained_in, prop_index)
);
CREATE INDEX idx_alf_propln_for ON alf_prop_link (root_prop_id, key_prop_id, value_prop_id);
CREATE INDEX fk_alf_propln_key ON alf_prop_link(key_prop_id);
CREATE INDEX fk_alf_propln_val ON alf_prop_link(value_prop_id);

CREATE TABLE alf_prop_unique_ctx
(
   id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
   version INTEGER NOT NULL,
   value1_prop_id BIGINT NOT NULL,
   value2_prop_id BIGINT NOT NULL,
   value3_prop_id BIGINT NOT NULL,   
   prop1_id BIGINT,
   CONSTRAINT fk_alf_propuctx_v1 FOREIGN KEY (value1_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_propuctx_v2 FOREIGN KEY (value2_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_propuctx_v3 FOREIGN KEY (value3_prop_id) REFERENCES alf_prop_value (id) ON DELETE CASCADE,
   CONSTRAINT fk_alf_propuctx_p1 FOREIGN KEY (prop1_id) REFERENCES alf_prop_root (id),
   PRIMARY KEY (id)
);
CREATE UNIQUE INDEX idx_alf_propuctx ON alf_prop_unique_ctx (value1_prop_id, value2_prop_id, value3_prop_id);
CREATE INDEX fk_alf_propuctx_v2 ON alf_prop_unique_ctx(value2_prop_id);
CREATE INDEX fk_alf_propuctx_v3 ON alf_prop_unique_ctx(value3_prop_id);
CREATE INDEX fk_alf_propuctx_p1 ON alf_prop_unique_ctx(prop1_id);
