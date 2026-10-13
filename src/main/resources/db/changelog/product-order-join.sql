CREATE TABLE product (
      id BIGSERIAL PRIMARY KEY,
      description VARCHAR(255) NOT NULL
);

CREATE TABLE order_product  (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT pk_order_product PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES "order" (id)
);