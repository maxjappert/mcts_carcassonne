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

TIME_LIMIT = 14400
MEMORY_LIMIT = 16384

if REMOTE:
    ENV = BaselSlurmEnvironment(email="max.jappert@unibas.ch")
else:
    ENV = LocalEnvironment(processes=8)

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

# Tests the performance against a random opponent using different exploration terms

for iterations in [2**i for i in range(0, 9)] + list(range(300, 3500, 100)):

    key1 = f'uct-{iterations}its1'
    key2 = f'ucttuned-{iterations}its1'
    key4 = f'decayingepsilongreedy-{iterations}its1'
    key5 = f'uct-{iterations}its2'
    key6 = f'ucttuned-{iterations}its2'
    key8 = f'decayingepsilongreedy-{iterations}its2'
    key9 = f'epsilongreedy-{iterations}its1'
    key10 = f'epsilongreedy-{iterations}its2'

    value1 = ['--p1',  'uct', '--p2', 'random', '--p1trainingiterations', f'{iterations}', '--p1explorationterm', '4']
    value2 = ['--p1',  'uct-tuned', '--p2', 'random', '--p1trainingiterations', f'{iterations}', '--p1explorationterm', '2']
    value4 = ['--p1',  'decaying-epsilon-greedy', '--p2', 'random', '--p1trainingiterations', f'{iterations}', '--p1explorationterm', '1']
    value5 = ['--p2',  'uct', '--p1', 'random', '--p2trainingiterations', f'{iterations}', '--p2explorationterm', '4']
    value6 = ['--p2',  'uct-tuned', '--p1', 'random', '--p2trainingiterations', f'{iterations}', '--p2explorationterm', '2']
    value8 = ['--p2',  'decaying-epsilon-greedy', '--p1', 'random', '--p2trainingiterations', f'{iterations}', '--p2explorationterm', '1']
    value9 = ['--p1',  'epsilon-greedy', '--p2', 'random', '--p1trainingiterations', f'{iterations}', '--p1explorationterm', '0.3']
    value10 = ['--p2',  'epsilon-greedy', '--p1', 'random', '--p2trainingiterations', f'{iterations}', '--p2explorationterm', '0.3']

    ALGORITHMS.update({key1: value1})
    ALGORITHMS.update({key2: value2})
    ALGORITHMS.update({key4: value4})
    ALGORITHMS.update({key5: value5})
    ALGORITHMS.update({key6: value6})
    ALGORITHMS.update({key8: value8})
    ALGORITHMS.update({key9: value9})
    ALGORITHMS.update({key10: value10})

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
