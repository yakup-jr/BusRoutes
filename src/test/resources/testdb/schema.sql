create schema if not exists transport;

create table if not exists transport.t_geography_coordinates
(
    id        uuid primary key,
    latitude  double precision not null,
    longitude double precision not null
);

create table if not exists transport.t_stop
(
    id                        uuid primary key,
    name                      varchar(255) not null unique,
    geographic_coordinates_id uuid unique,
    constraint fk_stop_coords foreign key (geographic_coordinates_id)
        references transport.t_geography_coordinates (id) on delete cascade
);

create table if not exists transport.t_business_hours
(
    id       uuid primary key,
    start_at TIME not null,
    end_at   TIME not null
);

create table if not exists transport.t_route
(
    id                uuid primary key,
    name              varchar(255) not null unique,
    type              varchar(50)  not null,
    interval          bigint       not null,
    business_hours_id uuid unique,
    constraint fk_route_hours foreign key (business_hours_id)
        references transport.t_business_hours (id) on delete set null
);

create table if not exists transport.t_route_stop
(
    id                   uuid primary key,
    route_id             uuid    not null,
    stop_id              uuid    not null,
    stop_order           integer not null,
    arrive_at_from_start integer not null,

    constraint fk_rs_route foreign key (route_id)
        references transport.t_route (id) on delete cascade,

    constraint fk_rs_stop foreign key (stop_id)
        references transport.t_stop (id) on delete cascade
);

create index if not exists idx_route_stop_stop_id on transport.t_route_stop (stop_id);
create index if not exists idx_route_stop_route_id on transport.t_route_stop (route_id);