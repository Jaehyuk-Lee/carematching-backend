/* MariaDB init SQL */

/* Initialize ROLE Table */
INSERT IGNORE INTO role (rno, rname) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO role (rno, rname) VALUES (2, 'ROLE_USER_CAREGIVER');
INSERT IGNORE INTO role (rno, rname) VALUES (3, 'ROLE_USER');

/* Initialize COMMUNITY_CATEGORY Table */
INSERT IGNORE INTO community_category (cpcno, name, access) VALUES (1, '전체', 'ALL');
INSERT IGNORE INTO community_category (cpcno, name, access) VALUES (2, '요양사', 'CAREGIVER');
