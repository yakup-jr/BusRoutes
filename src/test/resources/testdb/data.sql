insert into transport.t_geography_coordinates (id, latitude, longitude)
values (gen_random_uuid(), 55.7558, 37.6173),
       (gen_random_uuid(), 55.7517, 37.6178),
       (gen_random_uuid(), 55.7415, 37.6156),
       (gen_random_uuid(), 55.7312, 37.6012),
       (gen_random_uuid(), 55.7250, 37.5900),
       (gen_random_uuid(), 55.7100, 37.5800),
       (gen_random_uuid(), 55.7000, 37.5700),
       (gen_random_uuid(), 55.6900, 37.5600),
       (gen_random_uuid(), 55.6800, 37.5500),
       (gen_random_uuid(), 55.6700, 37.5400);

insert into transport.t_stop (id, name, geographic_coordinates_id)
values (gen_random_uuid(), 'Stop1',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7558)),
       (gen_random_uuid(), 'Stop2',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7517)),
       (gen_random_uuid(), 'Stop3',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7415)),
       (gen_random_uuid(), 'Stop4',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7312)),
       (gen_random_uuid(), 'Stop5',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7250)),
       (gen_random_uuid(), 'Stop6',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7100)),
       (gen_random_uuid(), 'Stop7',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.7000)),
       (gen_random_uuid(), 'Stop8',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.6900)),
       (gen_random_uuid(), 'Stop9',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.6800)),
       (gen_random_uuid(), 'Stop10',
        (select id from transport.t_geography_coordinates where t_geography_coordinates.latitude = 55.6700));

insert into transport.t_business_hours (id, start_at, end_at)
values (gen_random_uuid(), '06:00:00', '23:00:00'),
       (gen_random_uuid(), '00:00:00', '23:59:59'),
       (gen_random_uuid(), '08:00:00', '20:00:00');

insert into transport.t_route (id, name, type, interval, business_hours_id)
values (gen_random_uuid(), 'Route1', 'Bus', 600000000000,
        (select id from transport.t_business_hours where start_at = '06:00:00'));

insert into transport.t_route (id, name, type, interval, business_hours_id)
values (gen_random_uuid(), 'Route2', 'Bus', 900000000000,
        (select id from transport.t_business_hours where start_at = '00:00:00'));

insert into transport.t_route (id, name, type, interval, business_hours_id)
values (gen_random_uuid(), 'Route3', 'Bus', 600000000000,
        (select id from transport.t_business_hours where start_at = '08:00:00'));

insert into transport.t_route_stop (id, route_id, stop_id, stop_order, arrive_at_from_start)
values (gen_random_uuid(), (select id from transport.t_route where name = 'Route1'),
        (select id from transport.t_stop where name = 'Stop1'), 1, 0),
       (gen_random_uuid(), (select id from transport.t_route where name = 'Route1'),
        (select id from transport.t_stop where name = 'Stop2'), 2, 600);

insert into transport.t_route_stop (id, route_id, stop_id, stop_order, arrive_at_from_start)
values (gen_random_uuid(), (select id from transport.t_route where name = 'Route2'),
        (select id from transport.t_stop where name = 'Stop3'), 1, 0),
       (gen_random_uuid(), (select id from transport.t_route where name = 'Route2'),
        (select id from transport.t_stop where name = 'Stop4'), 2, 600),
       (gen_random_uuid(), (select id from transport.t_route where name = 'Route2'),
        (select id from transport.t_stop where name = 'Stop5'), 3, 900);

insert into transport.t_route_stop (id, route_id, stop_id, stop_order, arrive_at_from_start)
values (gen_random_uuid(), (select id from transport.t_route where name = 'Route3'),
        (select id from transport.t_stop where name = 'Stop10'), 1, 0),
       (gen_random_uuid(), (select id from transport.t_route where name = 'Route3'),
        (select id from transport.t_stop where name = 'Stop1'), 2, 300);