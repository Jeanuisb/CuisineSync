from yelpapi import YelpAPI
from yaml import load, dump
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper
from processyelp import processyelp

YELP_FILE = 'Keys.yaml'
config = ""

with open(YELP_FILE, 'r') as config_file:
    config = load(config_file, Loader)
apikey = config['yelp'].get('API_KEY')

yelp = YelpAPI(apikey)

def weighted_alg(member_to_preferences, max_dict):
    weights = {
        1: 5, 
        2: 4,
        3: 3,
        4: 2,
        5: 1}
    

def sort_dict_by_value(d, reverse = True):
    return dict(sorted(d.items(), key = lambda x: x[1], reverse = reverse))

def recommendation(member_to_preferences):
    location = "Memphis, TN"
    tally_dict = {}
    max_dict = {}

    for member in member_to_preferences:
        preferencelist = member_to_preferences[member]
        for value in preferencelist:
            if (tally_dict.get(value)):
                tally_dict[value] += 1
            else:
                tally_dict[value] = 1
    tally_dict = sort_dict_by_value(tally_dict)
    max_key = max(tally_dict, key=tally_dict.get)
    max_val = tally_dict[max_key]
    for key,val in tally_dict.items():
        retrieval = tally_dict[key]
        if (max_val == retrieval):
            max_dict[key] = val
    keys = max_dict.keys()
    if (len(keys) == 1):
        search_term = keys[0]
        print("Searching for " + search_term + "restaurants in " + location)
        result = yelp.search_query(term = search_term, location = location, sort_by = 'rating', limit = '5')
    else:
        additionalcheck = int(input("Would you like to search for multiple cuisines and decide from the options, or let algorithm decide for you? (0 for first option, 1 for second) "))
        if additionalcheck == 0:
            for search_term in keys:
                print("Searching for " + search_term + " restaurants in " + location)
                result = yelp.search_query(term = search_term, location = location, sort_by = 'rating', limit = '5')
                processyelp(result)
        else:
            pass




def main():
    #Arnab Das
    #Welela Burayu
    #Nedine Abdulahi
    #Jean Bikorimana
    preferences1 = ("Seafood", "Indian", "Mediterranean", "Bangladeshi", "Japanese")
    preferences2 = ("Thai", "Middle Eastern", "Asian", "Mediterranean", "Japanese")
    preferences3 = ("Cajun", "Middle Eastern", "Ethiopian", "Thai", "Chicken")
    preferences4 = ("Middle Eastern", "Japanese", "Chicken", "Soul Food", "Asian")
    member_to_preferences = {}    
    preferences = [preferences1, preferences2, preferences3, preferences4]
    group = list()
    mainname = input("What's your name? ")
    group.append(mainname)
    check = input("Do you wish to add more to the group? (Y/N) ")
    while (check.upper() == "Y"):
        groupmem = input("Add another member: ")
        group.append(groupmem)
        check = input("Do you wish to add more to the group? (Y/N) ")
    i = 0
    for member in group:
        member_to_preferences[member] = preferences[i]
        i+=1
    recommendation(member_to_preferences)
main()
    

