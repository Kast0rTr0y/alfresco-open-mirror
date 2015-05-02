--
-- Title:      Activity tables
-- Database:   DB2
-- Since:      V3.0 Schema 126
-- Author:     janv
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_activity_feed
(
    id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    post_id BIGINT,
    post_date TIMESTAMP NOT NULL,
    activity_summary VARCHAR(4096),
    feed_user_id VARCHAR(1020),
    activity_type VARCHAR(1020) NOT NULL,
    site_network VARCHAR(1020),
    app_tool VARCHAR(144),
    post_user_id VARCHAR(1020) NOT NULL,
    feed_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX feed_postdate_idx ON alf_activity_feed (post_date);
CREATE INDEX feed_postuserid_idx ON alf_activity_feed (post_user_id);
CREATE INDEX feed_feeduserid_idx ON alf_activity_feed (feed_user_id);
CREATE INDEX feed_sitenetwork_idx ON alf_activity_feed (site_network);

CREATE TABLE alf_activity_feed_control
(
    id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    feed_user_id VARCHAR(1020) NOT NULL,
    site_network VARCHAR(1020),
    app_tool VARCHAR(144),
    last_modified TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX feedctrl_feeduserid_idx ON alf_activity_feed_control (feed_user_id);

CREATE TABLE alf_activity_post
(
    sequence_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY,
    post_date TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL,
    activity_data VARCHAR(4096) NOT NULL,
    post_user_id VARCHAR(1020) NOT NULL,
    job_task_node INTEGER NOT NULL,
    site_network VARCHAR(1020),
    app_tool VARCHAR(144),
    activity_type VARCHAR(1020) NOT NULL,
    last_modified TIMESTAMP NOT NULL,
    PRIMARY KEY (sequence_id)
);
CREATE INDEX post_jobtasknode_idx ON alf_activity_post (job_task_node);
CREATE INDEX post_status_idx ON alf_activity_post (status);