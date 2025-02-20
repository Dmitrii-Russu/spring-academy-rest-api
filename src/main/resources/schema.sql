create table message(
   id bigserial PRIMARY KEY,
   title text not null,
   owner text not null
);