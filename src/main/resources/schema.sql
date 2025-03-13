CREATE TABLE user_entity (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES user_entity(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

create table message(
   id bigserial PRIMARY KEY,
   title text not null,
   owner text not null
);