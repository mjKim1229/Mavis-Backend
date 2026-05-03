create table if not exists admin
(
    id         bigint auto_increment
        primary key,
    is_deleted bit          not null,
    password   varchar(255) null,
    username   varchar(255) null
);

create table if not exists color
(
    created_at datetime(6)  null,
    id         bigint auto_increment
        primary key,
    updated_at datetime(6)  null,
    name       varchar(255) null
);

create table if not exists notice
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    content    varchar(255) null,
    is_deleted bit          not null,
    title      varchar(255) null
);

create table if not exists password_reset_token
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    email      varchar(255) null,
    expired_at datetime(6)  null,
    token      varchar(255) null,
    username   varchar(255) null
);

create table if not exists payment_idempotency
(
    id              bigint auto_increment
        primary key,
    created_at      datetime(6)                               null,
    updated_at      datetime(6)                               null,
    api_type        enum ('CANCEL', 'CONFIRM')                not null,
    expired_at      datetime(6)                               not null,
    failure_reason  text                                      null,
    idempotency_key varchar(300)                              not null,
    status          enum ('FAILURE', 'PROCESSING', 'SUCCESS') not null,
    constraint UKiwa7m0kr4c2mhbt01pcxc59ni
        unique (idempotency_key, api_type)
);

create table if not exists pending_order
(
    id           bigint auto_increment
        primary key,
    amount       int          not null,
    is_confirmed bit          not null,
    order_id     varchar(255) null
);

create table if not exists product
(
    is_deleted   bit          not null,
    price        int          null,
    created_at   datetime(6)  null,
    id           bigint auto_increment
        primary key,
    updated_at   datetime(6)  null,
    name         varchar(255) null,
    sub_category varchar(255) null,
    is_clearance bit          not null
);

create table if not exists product_color
(
    created_at datetime(6)  null,
    id         bigint auto_increment
        primary key,
    product_id bigint       null,
    updated_at datetime(6)  null,
    color      varchar(255) null,
    is_deleted bit          not null,
    constraint FKqb6lncpndi0w5po3rr5r9up5e
        foreign key (product_id) references product (id)
);

create table if not exists product_image
(
    id         bigint auto_increment
        primary key,
    image_url  varchar(255) null,
    product_id bigint       null,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    image_type varchar(255) null,
    order_num  int          not null,
    is_deleted bit          not null,
    constraint FK6oo0cvcdtb6qmwsga468uuukk
        foreign key (product_id) references product (id)
);

create table if not exists product_notice
(
    id             bigint auto_increment
        primary key,
    product_id     bigint        null,
    precaution     varchar(1000) null,
    return_process varchar(1000) null,
    return_request varchar(1000) null,
    shipping_info  varchar(1000) null,
    created_at     datetime(6)   null,
    updated_at     datetime(6)   null,
    constraint UKjmn5uxydjga37o6gyhfyk3r8i
        unique (product_id),
    constraint FKgtgh1sdrdnb5go3mq6hvvuq44
        foreign key (product_id) references product (id)
);

create table if not exists product_total_view
(
    id          bigint auto_increment
        primary key,
    total_views bigint      not null,
    week_end    date        null,
    week_start  date        null,
    product_id  bigint      null,
    is_deleted  bit         not null,
    created_at  datetime(6) null,
    updated_at  datetime(6) null,
    constraint FKmsvsavw0316p8uqaqsp2vmael
        foreign key (product_id) references product (id)
);

create table if not exists users
(
    id                  bigint auto_increment
        primary key,
    birth_day           date         null,
    email               varchar(255) null,
    gender              varchar(255) null,
    name                varchar(255) null,
    nickname            varchar(255) null,
    sns_id              varchar(255) null,
    sns_type            varchar(255) null,
    is_deleted          bit          not null,
    password            varchar(255) null,
    username            varchar(255) null,
    phone_number        varchar(255) null,
    naver_refresh_token varchar(255) null,
    created_at          datetime(6)  null,
    updated_at          datetime(6)  null,
    address             varchar(255) null,
    address_detail      varchar(255) null,
    receiver_name       varchar(255) null,
    receiver_phone      varchar(255) null,
    zip_code            varchar(255) null,
    age                 varchar(255) null,
    profile_image       varchar(255) null,
    email_agreed_at     datetime(6)  null,
    is_email_agreed     bit          null,
    is_sms_agreed       bit          null,
    sms_agreed_at       datetime(6)  null
);

create table if not exists cart_item
(
    id          bigint auto_increment
        primary key,
    color       varchar(255) null,
    is_deleted  bit          not null,
    quantity    int          not null,
    total_price int          not null,
    product_id  bigint       null,
    user_id     bigint       null,
    created_at  datetime(6)  null,
    updated_at  datetime(6)  null,
    constraint FKjcyd5wv4igqnw413rgxbfu4nv
        foreign key (product_id) references product (id),
    constraint FKka3t831w0aw2vrwgsbhcn5y4m
        foreign key (user_id) references users (id)
);

create table if not exists favorite
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) null,
    updated_at datetime(6) null,
    is_deleted bit         not null,
    product_id bigint      null,
    user_id    bigint      null,
    constraint FKa2lwa7bjrnbti5v12mga2et1y
        foreign key (user_id) references users (id),
    constraint FKbg4txsew6x3gl6r9swcq190hg
        foreign key (product_id) references product (id)
);

create table if not exists inquiry
(
    is_deleted bit          not null,
    is_private bit          not null,
    created_at datetime(6)  null,
    id         bigint auto_increment
        primary key,
    product_id bigint       null,
    updated_at datetime(6)  null,
    answer     varchar(255) null,
    question   varchar(255) null,
    user_id    bigint       null,
    constraint FK94fr9h3pwvsh2nos32o7jr0op
        foreign key (product_id) references product (id),
    constraint FKray80kmwpjpjb91ime7ogijjr
        foreign key (user_id) references users (id)
);

create table if not exists inquiry_answer
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    answer     varchar(255) null,
    is_deleted bit          not null,
    admin_id   bigint       null,
    inquiry_id bigint       null,
    constraint FK3irhermk3cm96u0y7oiv3ya4p
        foreign key (inquiry_id) references inquiry (id),
    constraint FKsil39co7y836xgaq04xh033lc
        foreign key (admin_id) references admin (id)
);

create table if not exists inquiry_image
(
    id         bigint auto_increment
        primary key,
    image_url  varchar(255) null,
    inquiry_id bigint       null,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    constraint FK23ss3xptk07hdng8df3xtnrv
        foreign key (inquiry_id) references inquiry (id)
);

create table if not exists orders
(
    created_at     datetime(6)  null,
    id             bigint auto_increment
        primary key,
    updated_at     datetime(6)  null,
    user_id        bigint       null,
    is_deleted     bit          not null,
    address        varchar(255) null,
    address_memo   varchar(255) null,
    order_status   varchar(255) null,
    total_price    int          not null,
    order_id       varchar(255) null,
    address_detail varchar(255) null,
    receiver_name  varchar(255) null,
    receiver_phone varchar(255) null,
    zip_code       varchar(255) null,
    constraint UK_ORDER_ID
        unique (order_id),
    constraint FK32ql8ubntj5uh44ph9659tiih
        foreign key (user_id) references users (id)
);

create table if not exists delivery
(
    id              bigint auto_increment
        primary key,
    created_at      datetime(6)  null,
    updated_at      datetime(6)  null,
    delivery_status varchar(255) null,
    order_id        bigint       null,
    carrier         varchar(255) null,
    tracking_number varchar(255) null,
    constraint UK3bdrbd2jcybaaa5rxkj4s7vlk
        unique (order_id),
    constraint FKu4e8rjwmg09vmas3ccjwglso
        foreign key (order_id) references orders (id)
);

create index idx_delivery_status_order
    on delivery (delivery_status, order_id);

create table if not exists order_item
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    color      varchar(255) null,
    is_deleted bit          not null,
    price      int          not null,
    quantity   int          not null,
    order_id   bigint       null,
    product_id bigint       null,
    constraint FK551losx9j75ss5d6bfsqvijna
        foreign key (product_id) references product (id),
    constraint FKt4dc2r9nbvbujrljv3e23iibt
        foreign key (order_id) references orders (id)
);

create index idx_order_item_deleted_order
    on order_item (is_deleted, order_id);

create table if not exists payment
(
    id                     bigint auto_increment
        primary key,
    created_at             datetime(6)  null,
    updated_at             datetime(6)  null,
    approved_at            datetime(6)  null,
    balance_amount         bigint       null,
    card_number            varchar(255) null,
    last_transaction_key   varchar(200) null,
    method                 varchar(255) null,
    partial_cancelable     bit          null,
    payment_key            varchar(255) null,
    provider               varchar(255) null,
    receipt_url            varchar(255) null,
    requested_at           datetime(6)  null,
    total_amount           bigint       null,
    order_id               bigint       null,
    virtual_account_secret varchar(255) null,
    constraint UKmf7n8wo2rwrxsd6f3t9ub2mep
        unique (order_id),
    constraint FKlouu98csyullos9k25tbpk4va
        foreign key (order_id) references orders (id)
);

create table if not exists refund
(
    id                     bigint auto_increment
        primary key,
    created_at             datetime(6)  null,
    updated_at             datetime(6)  null,
    cancel_transaction_key varchar(255) null,
    refund_amount          int          not null,
    refund_reason          varchar(255) null,
    refund_status          varchar(255) null,
    order_item_id          bigint       null,
    tracking_number        varchar(255) null,
    refund_type            varchar(255) null,
    carrier                varchar(255) null,
    constraint FKhi94nmb6h56j863wjvd82b1dk
        foreign key (order_item_id) references order_item (id)
);

create table if not exists refund_image
(
    id         bigint auto_increment
        primary key,
    image_url  varchar(255) null,
    refund_id  bigint       null,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    constraint FKevx39mc5f60u0b4awcne0ysbr
        foreign key (refund_id) references refund (id)
);

create table if not exists review
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6)  null,
    updated_at    datetime(6)  null,
    content       varchar(255) null,
    is_deleted    bit          not null,
    is_private    bit          not null,
    score         int          not null,
    order_item_id bigint       null,
    user_id       bigint       null,
    constraint UKfi19ihqypou1atahgbq3a0508
        unique (order_item_id),
    constraint FK6cpw2nlklblpvc7hyt7ko6v3e
        foreign key (user_id) references users (id),
    constraint FKcpceqmajrln2x7iqc4jua0hu1
        foreign key (order_item_id) references order_item (id)
);

create table if not exists review_image
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    updated_at datetime(6)  null,
    image_url  varchar(255) null,
    review_id  bigint       null,
    constraint FK16wp089tx9nm0obc217gvdd6l
        foreign key (review_id) references review (id)
);

create table if not exists verification_code
(
    id                bigint auto_increment
        primary key,
    created_at        datetime(6)  null,
    updated_at        datetime(6)  null,
    code              int          null,
    email             varchar(255) null,
    is_deleted        bit          not null,
    verification_type varchar(255) null,
    expired_at        datetime(6)  null
);

