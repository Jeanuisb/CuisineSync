from yelpapi import YelpAPI
from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper

YELP_FILE = 'Keys.yaml'
config = ""

with open(YELP_FILE, 'r') as config_file:
    config = load(config_file, Loader)
apikey = config['yelp'].get('API_KEY')

yelp = YelpAPI(apikey)

# Define search parameters
search_term = "Italian"
location = "New York, NY"

# Call the search_query function
result = yelp.search_query(term = "Italian", location = location, sort_by = 'rating', limit = '5')

# Print the result
count = 0
for restaurants in result['businesses']:
    print()
    count+=1
    print (restaurants['name'])
    if (restaurants.get('price')):
        print(restaurants['price'])
    print (restaurants['rating'])
    print (restaurants['review_count'])
    address = restaurants['location'].get('display_address')
    print(address[0] + " " + address[1])
    print()
print(count)