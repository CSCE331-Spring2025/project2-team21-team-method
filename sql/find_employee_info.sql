\prompt 'Enter employee ID: ' empid
SELECT emp_phone, emp_email FROM employee WHERE employee_id = :empid;
