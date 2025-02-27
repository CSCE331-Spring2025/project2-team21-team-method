\prompt 'Enter product ID: ' prodid
select count(*) from menu_item_inventory where product_id = :prodid;