select date(purchase_date) as date,  sum(p.product_cost) as sales from customer_transaction ct
join product p on ct.product_id = p.product_id group by date order by sales DESC limit 10;