INSERT INTO user_entity (username, password) VALUES
('jack', '{bcrypt}$2a$12$ifylJ0yP6.whKFuuM0sZ1.reBl9ZwKWtojh3gfgy63hdc/bTQ0xN2'),
('ann', '{bcrypt}$2a$12$4dWGmp/BpUhVTZR2w3Gv6ObATDF1XO.hvvzBkV9lhN0NvwGjxvpQW'),
('hank', '{bcrypt}$2a$12$7kyUBmifFaGvWd0/gUxA8ueA2lAFNTxY5juz7IZ.BzX86jgas6frS');

INSERT INTO role (name) VALUES
('USER'),
('NON-USER');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),  -- jack -> ROLE_USER
(2, 1),  -- ann -> ROLE_USER
(3, 2);  -- hank -> ROLE_NON-USER

insert into message(title, owner) VALUES ('testData1', 'jack');
insert into message(title, owner) VALUES ('testData2', 'jack');
insert into message(title, owner) VALUES ('testData3', 'jack');
insert into message(title, owner) VALUES ('testData4', 'jack');
insert into message(title, owner) VALUES ('testData5', 'jack');
insert into message(title, owner) VALUES ('testData6', 'jack');
insert into message(title, owner) VALUES ('testData7', 'ann');
insert into message(title, owner) VALUES ('testData8', 'ann');
insert into message(title, owner) VALUES ('testData9', 'ann');
insert into message(title, owner) VALUES ('testData10', 'ann');
insert into message(title, owner) VALUES ('testData11', 'ann');
insert into message(title, owner) VALUES ('testData12', 'ann');
insert into message(title, owner) VALUES ('testData13', 'hank');