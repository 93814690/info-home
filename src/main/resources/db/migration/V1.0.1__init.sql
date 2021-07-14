DROP TABLE IF EXISTS `subscription_weibo_hot_search`;
create table subscription_weibo_hot_search
(
    id           bigint auto_increment
        primary key,
    user_id      varchar(255)         not null,
    ranking_list int        default 0 not null,
    state        int        default 0 not null,
    sound        tinyint(1) default 0 not null
);
insert into subscription_weibo_hot_search (id, user_id, ranking_list, state, sound)
values (1, 'fei', 1, 0, 1);
insert into subscription_weibo_hot_search (id, user_id, ranking_list, state, sound)
values (2, 'fei', 0, 3, 1);

DROP TABLE IF EXISTS `user_chanify`;
create table user_chanify
(
    id            bigint auto_increment
        primary key,
    user_id       varchar(255) not null,
    chanify_token varchar(255) not null,
    constraint user_chanify_user_id_uindex
        unique (user_id)
);
insert into user_chanify (id, user_id, chanify_token)
values (1, 'fei',
        'CICy4YgGEiJBREpGVTM3RFpNNEZMRlZaN1FCWDVGSE5BTlY0TVM0RFpNGhRmU0vjxji92dxl8bfsQfWCC4Km-SIECAEQASoiQUhSN1pLV1czUkNRQVFJUlpCNUVDVElFS09WWFBSU05TTQ..sbiZSJu63KdZK1dm2l0Rtljnz-btD3V3tdLX3SeRimA');

DROP TABLE IF EXISTS `weibo_configuration`;
create table weibo_configuration
(
    id         int auto_increment
        primary key,
    cookie     varchar(1000) null,
    xsrf_token varchar(255)  null
);

DROP TABLE IF EXISTS `weibo_hot_search_push`;
create table weibo_hot_search_push
(
    id        bigint auto_increment
        primary key,
    user_id   varchar(255) not null,
    word      varchar(255) null,
    list_time bigint       null
);

DROP TABLE IF EXISTS `weibo_hot_search`;
DROP TABLE IF EXISTS `weibo_hot_search_v2`;
create table weibo_hot_search_v2
(
    id          bigint auto_increment
        primary key,
    word        varchar(255)  not null,
    emoticon    varchar(255)  null,
    `rank`      int           not null comment '排名',
    num         bigint        not null,
    state       int default 0 null comment '0:无
1:新
2:热
3:沸',
    list_time   bigint        not null comment '上榜时间',
    create_time datetime      not null
);

create table if not exists zsxq_configuration
(
    group_id   bigint        not null
        primary key,
    request_id varchar(255)  null,
    signature  varchar(255)  null,
    timestamp  varchar(255)  null,
    version    varchar(255)  null,
    cookie     varchar(1000) null,
    user_agent varchar(255)  null
);

create table if not exists zsxq_group
(
    group_id bigint       not null
        primary key,
    name     varchar(255) null,
    type     varchar(255) null
);

create table if not exists zsxq_user
(
    user_id     bigint       not null
        primary key,
    avatar_url  varchar(255) null,
    description varchar(255) null,
    name        varchar(255) null
);

create table if not exists zsxq_answer
(
    id            bigint auto_increment
        primary key,
    text          longtext null,
    owner_user_id bigint   null,
    constraint FK9f50tps3yf95ll5b0k540ayd2
        foreign key (owner_user_id) references zsxq_user (user_id)
);

create table if not exists zsxq_comment
(
    comment_id    bigint         not null
        primary key,
    create_time   varchar(255)   null,
    text          varchar(10000) null,
    owner_user_id bigint         null,
    constraint FKrun9nkr0010eyagh47dsq5v90
        foreign key (owner_user_id) references zsxq_user (user_id)
);

create table if not exists zsxq_question
(
    id            bigint auto_increment
        primary key,
    text          longtext null,
    owner_user_id bigint   null,
    constraint FKgpmhk8pcdmel4j47wys6sd0h0
        foreign key (owner_user_id) references zsxq_user (user_id)
);

create table if not exists zsxq_talk
(
    id            bigint auto_increment
        primary key,
    text          longtext null,
    owner_user_id bigint   null,
    constraint FKjfdff0owgmjc5wsqtayt5ku8v
        foreign key (owner_user_id) references zsxq_user (user_id)
);

create table if not exists zsxq_topic
(
    topic_id       bigint       not null
        primary key,
    create_time    varchar(255) null,
    modify_time    varchar(255) null,
    type           varchar(255) null,
    answer_id      bigint       null,
    group_group_id bigint       null,
    question_id    bigint       null,
    talk_id        bigint       null,
    constraint FK2pab575har3ij76yjwq6frb4x
        foreign key (question_id) references zsxq_question (id),
    constraint FKgspb7kf3c6ir4k54kjam1l5s6
        foreign key (group_group_id) references zsxq_group (group_id),
    constraint FKjgvaq81dnbrh5vahrxxyoh479
        foreign key (answer_id) references zsxq_answer (id),
    constraint FKl98ssw45g1byiortxlswe6629
        foreign key (talk_id) references zsxq_talk (id)
);

create table if not exists zsxq_topic_show_comments
(
    zsxq_topic_topic_id      bigint not null,
    show_comments_comment_id bigint not null,
    constraint UK_eegccuc292r6cvacwxrttcox2
        unique (show_comments_comment_id),
    constraint FKacmqq0h3g4c7r2jnr0tij8431
        foreign key (zsxq_topic_topic_id) references zsxq_topic (topic_id),
    constraint FKjm7y3dla9wke2qnhfcrq3t0no
        foreign key (show_comments_comment_id) references zsxq_comment (comment_id)
);

