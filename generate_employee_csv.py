import csv
import random
import faker

# Initialize Faker to generate fake data
fake = faker.Faker()

# Function to generate a single employee entry
def generate_employee(employee_id):
    return {
        "employee_id": employee_id,
        "emp_email": fake.email(),
        "emp_phone": random.randint(1000000000, 9999999999),  # Simulating a 10-digit phone number
        "is_manager": random.choice([True, False]),  # Randomly assigns if the employee is a manager
        "social_security": random.randint(100000000, 999999999),  # Simulating a 9-digit SSN
        "emp_pay": round(random.uniform(40000, 150000), 2),  # Generates salary between 40k - 150k
        "emp_bank_account": random.randint(100000, 999999)  # Simulating a 6-digit bank account number
    }

# Function to generate multiple employees and save to CSV
def generate_employees_csv(filename, n):
    employees = [generate_employee(i+1) for i in range(n)]  # Generate n employees

    # Define CSV file and field names
    fieldnames = ["employee_id", "emp_email", "emp_phone", "is_manager", "social_security", "emp_pay", "emp_bank_account"]
    
    # Write to CSV file
    with open(filename, mode='w', newline='') as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)

        # Write header
        writer.writeheader()

        # Write employee data
        writer.writerows(employees)

    print(f"CSV file '{filename}' with {n} employees has been created successfully!")

# Example usage
if __name__ == "__main__":
    num_employees = int(input("Enter the number of employees to generate: "))  # Get user input
    generate_employees_csv("employees_data.csv", num_employees)
