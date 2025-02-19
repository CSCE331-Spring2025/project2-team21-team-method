\prompt 'Enter Customer ID: ' custid
select points from customer_reward where customer_id = :custid;