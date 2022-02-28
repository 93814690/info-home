create table movie
(
    id                   bigint auto_increment
        primary key,
    imdb_code            varchar(10)  null,
    db_code              int          not null,
    chinese_title        varchar(255) null,
    original_title       varchar(255) null,
    other_title          varchar(255) null,
    year                 int          null,
    genre                varchar(255) null comment '电影的类型',
    country              varchar(255) null comment '制片国家/地区',
    initial_release_date varchar(255) null comment '上映日期',
    runtime              int          null comment '电影长度',
    mpaa_rating          varchar(50)  null comment '美国的电影分级',
    update_time          datetime     not null,
    constraint movie_db_code_uindex
        unique (db_code),
    constraint movie_imdb_code_uindex
        unique (imdb_code)
);

create table movie_celebrity
(
    id             bigint auto_increment
        primary key,
    movie_id       bigint       not null,
    celebrity_id   bigint       null,
    celebrity_name varchar(255) not null,
    type           int          not null comment '0:导演
1:编剧
2:主演'
);

create table movie_rating
(
    movie_id           bigint   not null
        primary key,
    db_rating          double   null,
    db_rating_people   int      null,
    imdb_rating        double   null,
    imdb_rating_people int      null,
    update_time        datetime not null
);