import csv
import sys
from collections import OrderedDict

from matplotlib import pyplot as plt

file = open(sys.argv[1])
reader = csv.reader(file)
algo = sys.argv[2]

achieved_points = dict()

for row in reader:
    for i in [1, 2]:
        try:
            index = row[0].index(algo) + len(algo)
        except ValueError:
            continue
        term = row[0][index]

        if term == 't':
            continue

        if row[0][index + 1] != '-':
            if row[0][index + 1:index + 4] == 'div':
                if float(row[0][index + 4:index+6] == '10'):
                    continue
                else:
                    term = float(term) / float(row[0][index + 4])
            else:
                term = term + row[0][index + 1]

        term = float(term)

        if term in achieved_points.keys():
            achieved_points[term] += int(row[i])
        else:
            achieved_points.update({term: 0})

        try:
            row[0] = row[0][:row[0].index("vs")]
        except ValueError:
            continue

achieved_points = dict(sorted(achieved_points.items()))
fig = plt.figure()
terms = achieved_points.keys()
points = achieved_points.values()

print(terms)

print(points)

plt.bar(terms, points)
plt.show()
