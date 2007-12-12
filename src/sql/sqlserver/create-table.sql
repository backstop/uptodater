create table uptodater (
    sqltext_hash varchar(100) primary key not null,
    insert_date datetime not null default getdate(),
    description varchar(250) not null,
    sqltext text,
    applied_date datetime
)

-- drop table ddlchanges;