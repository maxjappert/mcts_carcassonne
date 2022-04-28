import csv
import json
import os
import sys

print("Parsing .json file " + sys.argv[1] + "...")
dict_input = json.load(open(sys.argv[1]))
# results = json.loads(file)
output = dict()

# The list has the format [p1_points, p2_points, p1_contemplation_time, p2_contemplation_time]
for entry in dict_input.keys():
    name = entry[:-2]
    if name in output.keys():
        output[name][0] += int(dict_input[entry]["p1_points"])
        output[name][1] += int(dict_input[entry]["p2_points"])
        output[name][2] += float(dict_input[entry]["p1_contemplation_time"])
        output[name][3] += float(dict_input[entry]["p2_contemplation_time"])
    else:
        try:
            output.update({name: [int(dict_input[entry]["p1_points"]), int(dict_input[entry]["p2_points"]),
                                  int(dict_input[entry]["p1_contemplation_time"]),
                                  int(dict_input[entry]["p2_contemplation_time"])]})
        except KeyError:
            print("Missing fields for " + entry)

print("Finished parsing .json file.")
print("Creating .csv file...")

os.remove(sys.argv[1] + "_table.csv")
with open(sys.argv[1] + "_table.csv", 'w') as csvfile:

    csvfile.write("name, p1_points, p2_points, p1_contemplation_time, p2_contemplation_time\n")
    csvfile.flush()

    for key in output.keys():
        csvfile.write(key + "," + str(output[key])[1:-1] + "\n")
        csvfile.flush()

print("Finished creating .csv file.")
