SELECT setval(
    pg_get_serial_sequence('customer', 'id'),
    (SELECT MAX(id) FROM customer));

SELECT setval(
    pg_get_serial_sequence('"order"', 'id'),
    (SELECT MAX(id) FROM "order")
);

