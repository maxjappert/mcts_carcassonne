#! /usr/bin/env python

"""
Example experiment using a simple vertex cover solver.
"""

import glob
import os
import platform

from downward.reports.absolute import AbsoluteReport
from lab.environments import BaselSlurmEnvironment, LocalEnvironment
from lab.experiment import Experiment
from lab.reports import Attribute


# Create custom report class with suitable info and error attributes.
class BaseReport(AbsoluteReport):
    INFO_ATTRIBUTES = ["time_limit", "memory_limit", "seed"]
    ERROR_ATTRIBUTES = [
        "domain",
        "problem",
        "algorithm",
        "unexplained_errors",
        "error",
        "node",
    ]


NODE = platform.node()
REMOTE = NODE.endswith(".scicore.unibas.ch") or NODE.endswith(".cluster.bc2.ch")
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BENCHMARKS_DIR = os.path.join(SCRIPT_DIR, "benchmarks")


TIME_LIMIT = 1800
MEMORY_LIMIT = 8192

if REMOTE:
    ENV = BaselSlurmEnvironment(email="max.jappert@unibas.ch")
    #SUITE = BHOSLIB_GRAPHS + RANDOM_GRAPHS
else:
    ENV = LocalEnvironment(processes=1)
    # Use smaller suite for local tests.
    #SUITE = BHOSLIB_GRAPHS[:1] + RANDOM_GRAPHS[:1]


### muss angepasst werden
ATTRIBUTES = [
    "p1_points",
    "p2_points",
    "p1_contemplation_time",
    "p2_contemplation_time",
    "points_difference"
    #Attribute("solved", absolute=True),
]
####


# Create a new experiment.
exp = Experiment(environment=ENV)
exp.add_resource("solver", os.path.join(SCRIPT_DIR, "carcassonne.jar"))
# Add custom parser.
exp.add_parser("parser.py")

ALGORITHMS = dict()

# What we want to test with this experiment is have the following tree policies play against each other and
# then evaluate their performance:
# -boltzmann
# -uct
# -ucttuned
# -random
# -epsilon-greedy
# -decaying-epsilon-greedy
# -mcts with heuristic tree policy
# -heuristic player (who simply picks the move which maximises the heuristic function


tree_policies = dict()
tree_policies.update({'boltzmann': 7})
tree_policies.update({'uct': 7})
tree_policies.update({'uct-tuned': 13})
tree_policies.update({'epsilon-greedy': 0.3})

meeple_placement_probs = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]


for tree_policy in tree_policies.keys():
    for meeple_placement_prob in meeple_placement_probs:
        key1 = f'{tree_policy}_{meeple_placement_prob}_random_1'
        key2 = f'{tree_policy}_{meeple_placement_prob}_random_2'
        key3 = f'{tree_policy}_{meeple_placement_prob}_heuristic_1'
        key4 = f'{tree_policy}_{meeple_placement_prob}_heuristic_2'

        value1 = ['--p1',  f'{tree_policy}', '--p2',  'random', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', f'{tree_policies[tree_policy]}', '--p2explorationterm', f'{tree_policies[tree_policy]}', '--p1meepleplacementprob', f'{meeple_placement_prob}']
        value2 = ['--p1',  'random', '--p2',  f'{tree_policy}', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', f'{tree_policies[tree_policy]}', '--p2explorationterm', f'{tree_policies[tree_policy]}', '--p2meepleplacementprob', f'{meeple_placement_prob}']
        value3 = ['--p1',  f'{tree_policy}', '--p2',  'heuristic', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', f'{tree_policies[tree_policy]}', '--p2explorationterm', f'{tree_policies[tree_policy]}', '--p1meepleplacementprob', f'{meeple_placement_prob}']
        value4 = ['--p1',  'heuristic', '--p2',  f'{tree_policy}', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', f'{tree_policies[tree_policy]}', '--p2explorationterm', f'{tree_policies[tree_policy]}', '--p2meepleplacementprob', f'{meeple_placement_prob}']

        ALGORITHMS.update({key1: value1})
        ALGORITHMS.update({key2: value2})
        ALGORITHMS.update({key3: value3})
        ALGORITHMS.update({key4: value4})

for algo_name, algo_cmd in ALGORITHMS.items():
    for seed in range(5, 10):
        # loop over both positionings of players?
        run = exp.add_run()

        algo_cmd_string = ''

        for arg in algo_cmd:
            algo_cmd_string = algo_cmd_string + arg + ' '

        run.add_command(
            f"{algo_name}",
            ["java",  "-jar", "{solver}", algo_cmd, "--deckseed", seed, "-v", "false", "--graphviz", "false"],
            time_limit=TIME_LIMIT,
            memory_limit=MEMORY_LIMIT,
        )
        # AbsoluteReport needs the following attributes:
        # 'domain', 'problem' and 'algorithm'.
        domain = "carcassonne"
        task_name = algo_name
        run.set_property("problem", task_name)
        run.set_property("domain", domain)
        run.set_property("algorithm", algo_name)
        # BaseReport needs the following properties:
        # 'time_limit', 'memory_limit', 'seed'.
        run.set_property("time_limit", TIME_LIMIT)
        run.set_property("memory_limit", MEMORY_LIMIT)
        run.set_property("seed", seed)
        # Every run has to have a unique id in the form of a list.
        run.set_property("id", [domain, algo_name, f"{seed}"])

# Add step that writes experiment files to disk.
exp.add_step("build", exp.build)

# Add step that executes all runs.
exp.add_step("start", exp.start_runs)

# Add step that collects properties from run directories and
# writes them to *-eval/properties.
exp.add_fetcher(name="fetch")

# Make a report.
exp.add_report(BaseReport(attributes=ATTRIBUTES), outfile="report.html")

# Parse the commandline and run the given steps.
exp.run_steps()
