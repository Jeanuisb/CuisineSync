# Note, to run script, run python3 -m scripts.test_script in terminal

# Import the search_restaurants function from api.py

from modules.api import search_restaurants

# Define search parameters
search_term = "Italian"
location = "New York, NY"

# Call the search_restaurants function
result = search_restaurants(search_term, location)

# Print the result
print(result)
