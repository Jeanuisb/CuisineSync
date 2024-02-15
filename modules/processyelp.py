
def processyelp(result):
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