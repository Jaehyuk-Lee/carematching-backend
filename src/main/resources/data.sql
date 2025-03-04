/* MariaDB init SQL */

/* Initialize ROLE Table */
INSERT IGNORE INTO role (rno, rname) VALUES (1, 'ROLE_ADMIN');
INSERT IGNORE INTO role (rno, rname) VALUES (2, 'ROLE_USER_CAREGIVER');
INSERT IGNORE INTO role (rno, rname) VALUES (3, 'ROLE_USER');

/* Initialize COMMUNITY_CATEGORY Table */
INSERT IGNORE INTO community_category (cpcno, name, access) VALUES (1, 'ALL' , '전체');
INSERT IGNORE INTO community_category (cpcno, name, access) VALUES (2, 'CAREGIVER', '요양사');
