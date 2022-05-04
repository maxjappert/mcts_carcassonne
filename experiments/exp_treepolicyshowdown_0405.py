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

### kann gelöscht werden
# BHOSLIB_GRAPHS = sorted(glob.glob(os.path.join(BENCHMARKS_DIR, "bhoslib", "*.mis")))
# RANDOM_GRAPHS = sorted(glob.glob(os.path.join(BENCHMARKS_DIR, "random", "*.txt")))
# ALGORITHMS = ["2approx", "greedy"]
# SEEDS = 2018
####


TIME_LIMIT = 1800
MEMORY_LIMIT = 8192

if REMOTE:
    ENV = BaselSlurmEnvironment(email="max.jappert@unibas.ch")
    #SUITE = BHOSLIB_GRAPHS + RANDOM_GRAPHS
else:
    ENV = LocalEnvironment(processes=8)
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

# -boltzmann
# -uct
# -ucttuned
# -espilon-greedy

key1 = f'uct3-vs-boltzmann10-1000its'
key2 = f'boltzmann10-vs-uct3-1000its'
key3 = f'ucttuned10-vs-boltzmann10-1000its'
key4 = f'boltzmann10-vs-ucttuned10-1000its'
key5 = f'epsilongreedy-vs-boltzmann10-1000its'
key6 = f'boltzmann10-vs-epsilongreedy-1000its'

key7 = f'uct3-vs-ucttuned10-1000its'
key8 = f'ucttuned10-vs-uct3-1000its'
key9 = f'uct3-vs-epsilongreedy-1000its'
key10 = f'epsilongreedy-vs-uct3-1000its'

key11 = f'ucttuned10-vs-epsilongreedy-1000its'
key12 = f'epsilongreedy-vs-ucttuned10-1000its'

value1 = ['--p1',  'uct', '--p2', 'boltzmann', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 3, '--p2explorationterm', 10]
value2 = ['--p1',  'boltzmann', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 10, '--p2explorationterm', 3]
value3 = ['--p1',  'ucttuned', '--p2', 'boltzmann', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 10, '--p2explorationterm', 10]
value4 = ['--p1',  'boltzmann', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 10, '--p2explorationterm', 10]
value5 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 1, '--p2explorationterm', 10]
value6 = ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 10, '--p2explorationterm', 1]
value7 = ['--p1',  'uct', '--p2', 'ucttuned', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 3, '--p2explorationterm', 10]
value8 = ['--p1',  'ucttuned', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 10, '--p2explorationterm', 3]
value9 = ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 3, '--p2explorationterm', 1]
value10 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 1, '--p2explorationterm', 3]
value11 = ['--p1',  'ucttuned', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 1, '--p2explorationterm', 3]
value12 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'ucttuned', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', 3, '--p2explorationterm', 1]

ALGORITHMS.update({key1: value1})
ALGORITHMS.update({key2: value1})
ALGORITHMS.update({key3: value1})
ALGORITHMS.update({key4: value1})
ALGORITHMS.update({key5: value1})
ALGORITHMS.update({key6: value1})
ALGORITHMS.update({key7: value1})
ALGORITHMS.update({key8: value1})
ALGORITHMS.update({key9: value1})
ALGORITHMS.update({key10: value1})
ALGORITHMS.update({key11: value1})
ALGORITHMS.update({key12: value1})

for algo_name, algo_cmd in ALGORITHMS.items():
    for seed in range(5):
        # loop over both positionings of players?
        run = exp.add_run()

        algo_cmd_string = ''

        for arg in algo_cmd:
            algo_cmd_string = algo_cmd_string + arg + ' '

        run.add_command(
            f"{algo_name}",
            ["java",  "-jar", "{solver}", algo_cmd, "--deckseed", seed, "-v", "false"],
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
