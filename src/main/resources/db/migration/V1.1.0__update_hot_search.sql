alter table weibo_hot_search_v2
    drop column list_time;

alter table weibo_hot_search_v2
    change create_time record_time datetime not null;

alter table weibo_hot_search_push
    drop column list_time;

alter table weibo_hot_search_push
    add push_time datetime default now() not null;
