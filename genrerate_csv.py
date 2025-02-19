import pandas as pd
import random
from datetime import datetime, timedelta

# Load the products table
products_csv_path = "boba_tea_products_updated.csv"  # Make sure this file is in the same directory
products_df = pd.read_csv(products_csv_path)

# Extract product IDs
product_ids = products_df["product_id"].tolist()

# Define date range (52 weeks of sales data)
start_date = datetime.now() - timedelta(weeks=52)
end_date = datetime.now()

# Define customer range (1000 customers)
customer_ids = list(range(1001, 2001))

# Define peak days (e.g., semester start, special holidays)
peak_days = [
    start_date + timedelta(weeks=2),
    start_date + timedelta(weeks=20),
    start_date + timedelta(weeks=40),
]

# Function to generate realistic emails
first_names = ["john", "sarah", "michael", "emily", "david", "jessica", "chris", "amanda", "brian", "laura"]
last_names = ["smith", "johnson", "williams", "brown", "jones", "miller", "davis", "garcia", "martinez", "rodriguez"]
domains = ["gmail.com", "yahoo.com", "outlook.com", "hotmail.com"]

def generate_email():
    first = random.choice(first_names)
    last = random.choice(last_names)
    num = random.randint(1, 999)
    domain = random.choice(domains)
    return f"{first}.{last}{num}@{domain}"

# Reset product sales tracking
product_sales_daily = {product_id: 0 for product_id in product_ids}
product_sales_weekly = {product_id: 0 for product_id in product_ids}

# Generate transactions ensuring $1 million in revenue with random times
transactions = []
current_date = start_date
transaction_id = 1
total_revenue = 0

while current_date <= end_date:
    daily_product_sales = {product_id: 0 for product_id in product_ids}
    daily_transactions = random.randint(150, 300)

    if any(abs((current_date - peak).days) <= 1 for peak in peak_days):
        daily_transactions *= 4  # Increase transactions on peak days

    for _ in range(daily_transactions):
        product_id = random.choice(product_ids)
        customer_id = random.choice(customer_ids)
        ice_amount = random.choice([0, 0.25, 0.5, 0.75])
        topping_type = random.choice(["Boba", "Aloe Vera", "Popping Boba", "Red Bean", "None"])
        product_cost = products_df.loc[products_df["product_id"] == product_id, "product_cost"].values[0]

        # Generate random time for transaction (between 8 AM and 10 PM)
        random_hour = random.randint(8, 22)
        random_minute = random.randint(0, 59)
        random_second = random.randint(0, 59)
        transaction_datetime = current_date.replace(hour=random_hour, minute=random_minute, second=random_second)

        transactions.append([
            transaction_id,
            random.randint(10000, 99999),
            product_id,
            customer_id,
            transaction_datetime.strftime("%Y-%m-%d %H:%M:%S"),  # Include date and time
            ice_amount,
            topping_type
        ])

        transaction_id += 1
        daily_product_sales[product_id] += 1
        product_sales_weekly[product_id] += 1
        total_revenue += product_cost

    if (current_date - start_date).days % 7 == 0:
        for product_id in product_sales_weekly:
            product_sales_weekly[product_id] = daily_product_sales[product_id]

    current_date += timedelta(days=1)

# Scale revenue to $1 million while maintaining product prices
scaling_factor = 1_000_000 / total_revenue

# Create transactions DataFrame and save CSV (without product cost)
transactions_df = pd.DataFrame(transactions, columns=[
    "customer_transaction_num", "order_id", "product_id", "customer_id", 
    "purchase_date", "ice_amount", "topping_type"
])
transactions_df.to_csv("customer_transactions.csv", index=False)

# Generate customer rewards data
customer_rewards = [
    [customer_id, random.randint(0, 5000), generate_email(), random.randint(1000000000, 9999999999)]
    for customer_id in customer_ids
]
customer_rewards_df = pd.DataFrame(customer_rewards, columns=["customer_id", "points", "email", "phone_number"])
customer_rewards_df.to_csv("customer_rewards.csv", index=False)

# Generate trends data based on transactions
daily_purchases = transactions_df.groupby(["purchase_date", "product_id"]).size().reset_index(name="daily_purchased")
daily_purchases = daily_purchases.sort_values(["product_id", "purchase_date"])
daily_purchases["weekly_purchased"] = (
    daily_purchases.groupby("product_id")["daily_purchased"]
    .rolling(window=7, min_periods=1).sum().reset_index(level=0, drop=True).astype(int)
)

# Summarize trends data
final_trends = daily_purchases.groupby("product_id").agg(
    daily_purchased=("daily_purchased", "sum"),
    weekly_purchased=("weekly_purchased", "sum")
).reset_index()

# Save updated trends CSV
final_trends.to_csv("trends.csv", index=False)

print("CSV files generated successfully: customer_transactions.csv, customer_rewards.csv, trends.csv")