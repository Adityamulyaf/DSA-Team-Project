#r Using Recursion - O(n!) Time and O(n) Space

# Python program to find the shortest possible route
# that visits every city exactly once and returns to
# the starting point using memoization and bitmasking

#y tsp(curr, mask) = min(cost[curr][i] + tsp(i, mask ∣ (1<<i))), for all cities i that have not been visited yet.
#b curr is the current city in the tour.
#b mask represents the cities that have already been visited.
#b cost[curr][i] is the cost to travel from city curr to city i.
#b tsp(i, mask | (1 << i)) represents the cost of visiting the remaining cities in the new mask (after visiting city i), and continuing the tour from city i.

import sys

def totalCost(mask, pos, n, cost, path, memo):
  
    # Base case: if all cities are visited, return the
    # cost to return to the starting city (0)
    if mask == (1 << n) - 1:
        return cost[pos][0], [0]

    if (mask, pos) in memo:
        return memo[(mask, pos)]

    ans = sys.maxsize
    best_path = []

    # Try visiting every city that has not been visited yet
    for i in range(n):
        if (mask & (1 << i)) == 0: 
  
            # If city i is not visited, visit it and 
             #  update the mask
            sub_cost, sub_path = totalCost(mask | (1 << i), i, n, cost, path, memo)
            temp_cost = cost[pos][i] + sub_cost
            if temp_cost < ans:
                ans = temp_cost
                best_path = [i] + sub_path

    memo[(mask, pos)] = (ans, best_path)
    return memo[(mask, pos)]
 

def tsp(cost):
    n = len(cost)
    
    # Start from city 0, and only city 0 is visited 
    # initially (mask = 1)
    memo = {}
    total_cost, route = totalCost(1, 0, n, cost, [], memo)
    route = [0] + route  # prepend starting city
    return total_cost, route
 
if __name__ == "__main__":
    
    cost = [
        [0, 10, 100, 20, 56],
        [10, 0, 35, 25, 21],
        [100, 35, 0, 30, 12],
        [20, 25, 30, 0, 12],
        [56, 21, 12, 12, 0]
    ]

    city_names = ["Toko A", "Toko B", "Toko C", "Toko D", "Toko E"]

    result_cost, result_route = tsp(cost)
    print("Total cost:", result_cost)
    print("Route taken (index):", result_route)
    print("Route taken (named):", [city_names[i] for i in result_route])

    print("\nDetail perjalanan:")
    for i in range(len(result_route) - 1):
        a = result_route[i]
        b = result_route[i + 1]
        print(f"{city_names[a]} — {cost[a][b]} km → {city_names[b]}")