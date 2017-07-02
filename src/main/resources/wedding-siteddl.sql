
    create sequence hibernate_sequence start with 1 increment by 1;

    create table Roles (
        id integer not null,
        role varchar(255),
        weddingUser_id integer,
        primary key (id)
    );

    create table WeddingUser (
        id integer not null,
        enabled boolean not null,
        password varchar(255),
        userName varchar(255),
        primary key (id)
    );

    create table WeddingUser_Roles (
        WeddingUser_id integer not null,
        roles_id integer not null,
        primary key (WeddingUser_id, roles_id)
    );

    alter table WeddingUser_Roles 
        add constraint UK_hn8oh8bw6gokae4c7ik0fvvp2 unique (roles_id);

    alter table Roles 
        add constraint FK1kxqn3uj3is8gig13vbiuhgwk 
        foreign key (weddingUser_id) 
        references WeddingUser;

    alter table WeddingUser_Roles 
        add constraint FKqnvlobu78x77923tpvi9o6glr 
        foreign key (roles_id) 
        references Roles;

    alter table WeddingUser_Roles 
        add constraint FKimu6xsw1fjuon7q4iila613vf 
        foreign key (WeddingUser_id) 
        references WeddingUser;
