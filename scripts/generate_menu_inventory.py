import pandas as pd

# Define the menu item to inventory mapping
menu_item_inventory_data = [
    {"product_id": 1, "item_id": 1, "quantity_used": 2},  # Matcha Latte uses Matcha Powder
    {"product_id": 2, "item_id": 2, "quantity_used": 3},  # Classic Milk Tea uses Classic Milk Tea Mix
    {"product_id": 3, "item_id": 3, "quantity_used": 2},  # Almond Milk Tea uses Almond Milk Tea Mix
    {"product_id": 4, "item_id": 4, "quantity_used": 3},  # Strawberry Black Tea uses Strawberry Black Tea Mix
    {"product_id": 5, "item_id": 5, "quantity_used": 2},  # Jasmine Green Tea uses Jasmine Green Tea Mix
    {"product_id": 6, "item_id": 6, "quantity_used": 3},  # Pineapple Black Tea uses Pineapple Black Tea Mix
    {"product_id": 7, "item_id": 7, "quantity_used": 3},  # Passion Fruit Green Tea uses Passion Fruit Green Tea Mix
    {"product_id": 8, "item_id": 8, "quantity_used": 2},  # Lychee Oolong Tea uses Lychee Oolong Tea Mix
    {"product_id": 9, "item_id": 9, "quantity_used": 3},  # Mango Green Tea uses Mango Green Tea Mix
    {"product_id": 10, "item_id": 10, "quantity_used": 2},  # Almond Milk Tea (again)
    {"product_id": 11, "item_id": 11, "quantity_used": 3},  # Chai Milk Tea uses Chai Milk Tea Mix
    {"product_id": 12, "item_id": 12, "quantity_used": 2},  # Rose Oolong Tea uses Rose Oolong Tea Mix
    {"product_id": 13, "item_id": 13, "quantity_used": 2},  # Almond Milk Tea (again)
    {"product_id": 14, "item_id": 14, "quantity_used": 3},  # Brown Sugar Milk Tea uses Brown Sugar Mix
    {"product_id": 15, "item_id": 15, "quantity_used": 3},  # Grapefruit Green Tea uses Grapefruit Green Tea Mix
    {"product_id": 16, "item_id": 16, "quantity_used": 2},  # Jasmine Green Tea (again)
    {"product_id": 17, "item_id": 17, "quantity_used": 3},  # Strawberry Black Tea (again)
    {"product_id": 18, "item_id": 18, "quantity_used": 2},  # Pineapple Black Tea (again)
    {"product_id": 19, "item_id": 19, "quantity_used": 1},  # Pineapple Black Tea (again)
    {"product_id": 20, "item_id": 20, "quantity_used": 1},  # Passion Fruit Green Tea (again)
    {"product_id": 1, "item_id": 21, "quantity_used": 1},  # Cups for all drinks
    {"product_id": 1, "item_id": 22, "quantity_used": 1},  # Straws for all drinks
    {"product_id": 1, "item_id": 23, "quantity_used": 1},  # Ice cubes for all drinks
    {"product_id": 2, "item_id": 24, "quantity_used": 1},  # Milk for Classic Milk Tea
    {"product_id": 3, "item_id": 25, "quantity_used": 1},  # Oat Milk for Almond Milk Tea
    {"product_id": 4, "item_id": 26, "quantity_used": 1},  # Soy Milk for Strawberry Black Tea
    {"product_id": 5, "item_id": 27, "quantity_used": 1},  # Pure Cane Sugar for Jasmine Green Tea
    {"product_id": 6, "item_id": 28, "quantity_used": 1},  # Artificial Sweetener for Pineapple Black Tea
    {"product_id": 7, "item_id": 29, "quantity_used": 1},  # Vanilla Syrup for Passion Fruit Green Tea
    {"product_id": 8, "item_id": 30, "quantity_used": 1}   # Matcha Powder for Lychee Oolong Tea
]

# Create a DataFrame
menu_item_inventory_df = pd.DataFrame(menu_item_inventory_data)

# Save as CSV
csv_filename = "menu_item_inventory.csv"
menu_item_inventory_df.to_csv(csv_filename, index=False)

print(f"CSV file '{csv_filename}' has been generated successfully!")