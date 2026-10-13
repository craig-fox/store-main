INSERT INTO product (id, description) VALUES (1, 'Handcrafted Soft Chair');
INSERT INTO product (id, description) VALUES (2, 'Awesome Metal Fish');
INSERT INTO product (id, description) VALUES (3, 'Handmade Frozen Salad');
INSERT INTO product (id, description) VALUES (4, 'Sleek Soft Chair');
INSERT INTO product (id, description) VALUES (5, 'Recycled Wooden Shoes');

SELECT setval(
    pg_get_serial_sequence('product', 'id'),
    COALESCE((SELECT MAX(id) FROM product), 1),
    EXISTS (SELECT 1 FROM product)
);
