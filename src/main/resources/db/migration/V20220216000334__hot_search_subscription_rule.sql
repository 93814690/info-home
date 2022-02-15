alter table subscription_weibo_hot_search
    add min_num bigint default 0 null comment '最小搜索量';