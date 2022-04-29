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

for i in range(0, 500, 5):
    for j in range(0, 500, 5):
        if i != j:
            key1 = f'uct{i}-vs-uct{j}-1000its'
            key2 = f'uct{j}-vs-uct{i}-1000its'
            key3 = f'boltzmann{i}-vs-boltzmann{j}-1000its'
            key4 = f'boltzmann{j}-vs-boltzmann{i}-1000its'
            key5 = f'egreedy-1div{i}-vs-egreedy-1div{j}-1000its'
            key6 = f'egreedy-1div{j}-vs-egreedy-1div{i}-1000its'
            key7 = f'ucttuned{i}-vs-ucttuned{j}-1000its'
            key8 = f'ucttuned{j}-vs-ucttuned{i}-1000its'
            value1 = ['--p1',  'uct', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(i), '--p2explorationterm', str(j)]
            value2 = ['--p1',  'uct', '--p2', 'uct', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(j), '--p2explorationterm', str(i)]
            value3 = ['--p1',  'boltzmann', '--p2', 'boltzmann', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(i), '--p2explorationterm', str(j)]
            value4 = ['--p1',  'boltzmann', '--p2', 'boltzmann', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(j), '--p2explorationterm', str(i)]
            value7 = ['--p1',  'uct-tuned', '--p2', 'uct-tuned', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(i), '--p2explorationterm', str(j)]
            value8 = ['--p1',  'uct-tuned', '--p2', 'uct-tuned', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(j), '--p2explorationterm', str(i)]
            ALGORITHMS.update({key1: value1})
            ALGORITHMS.update({key2: value2})
            ALGORITHMS.update({key3: value3})
            ALGORITHMS.update({key4: value4})
            ALGORITHMS.update({key7: value7})
            ALGORITHMS.update({key8: value8})

            if 1 <= i <= 10 and 1 <= j <= 10:
                value5 = ['--p1',  'epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(1.0/i), '--p2explorationterm', str(1.0/j)]
                value6 = ['--p1',  'epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000', '--p1explorationterm', str(1.0/j), '--p2explorationterm', str(1.0/i)]
                ALGORITHMS.update({key5: value5})
                ALGORITHMS.update({key6: value6})



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
