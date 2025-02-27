SELECT cr.customer_id, cr.points, cr.email
FROM customer_reward cr
ORDER BY cr.points DESC
LIMIT 10;