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

for i in [i*0.5 for i in range(0, 30)]:
    key1 = f'uct{i}_1'
    key2 = f'uct{i}_2'
    key3 = f'ucttuned{i}_1'
    key4 = f'ucttuned{i}_2'
    key5 = f'boltzmann{i}_1'
    key6 = f'boltzmann{i}_2'
    value1 = ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', str(i), '--p2explorationterm', '1']
    value2 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', '1', '--p2explorationterm', str(i)]
    value3 = ['--p1',  'ucttuned', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', str(i), '--p2explorationterm', '1']
    value4 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'ucttuned', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', '1', '--p2explorationterm', str(i)]
    value5 = ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', str(i), '--p2explorationterm', '1']
    value6 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', '1', '--p2explorationterm', str(i)]

    ALGORITHMS.update({key1: value1})
    ALGORITHMS.update({key2: value2})
    ALGORITHMS.update({key3: value3})
    ALGORITHMS.update({key4: value4})
    ALGORITHMS.update({key5: value5})
    ALGORITHMS.update({key6: value6})

    epsilon = 1

    if i == 0:
        epsilon = 0
    elif i == 0.5:
        continue
    else:
        epsilon = 1.0 / i

    key7 = f'epsilongreedy{epsilon}_1'
    key8 = f'epsilongreedy{epsilon}_2'

    value7 = ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', str(epsilon), '--p2explorationterm', '1']
    value8 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '300', '--p2trainingiterations', '300', '--p1explorationterm', '1', '--p2explorationterm', str(epsilon)]

    ALGORITHMS.update({key7: value7})
    ALGORITHMS.update({key8: value8})



for algo_name, algo_cmd in ALGORITHMS.items():
    for seed in range(10):
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
