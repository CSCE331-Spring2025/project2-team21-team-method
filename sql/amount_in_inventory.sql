\prompt 'Enter item name: ' prodname
SELECT amount FROM inventory WHERE item_name = :'prodname';