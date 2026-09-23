INSERT INTO products (id, sku, name, description, price, active, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'SKU-COFFEE', 'Whole Bean Coffee', 'Medium roast coffee beans', 14.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 'SKU-MUG', 'Ceramic Mug', 'InventoryHub ceramic mug', 9.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 'SKU-OLD', 'Inactive Product', 'Not available for ordering', 1.99, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO inventory_items (id, product_id, available_quantity, reserved_quantity, version, updated_at)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 25, 0, 0, CURRENT_TIMESTAMP),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 10, 0, 0, CURRENT_TIMESTAMP),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '33333333-3333-3333-3333-333333333333', 5, 0, 0, CURRENT_TIMESTAMP);
