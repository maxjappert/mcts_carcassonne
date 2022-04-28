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
    #Attribute("solved", absolute=True),
]
####


# Create a new experiment.
exp = Experiment(environment=ENV)
exp.add_resource("solver", os.path.join(SCRIPT_DIR, "carcassonne.jar"))
# Add custom parser.
exp.add_parser("parser.py")

ALGORITHMS = {
    'uct-vs-decaying-epsilon-greedy-expterm-0-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '0'],
    'decaying-epsilon-greedy-vs-uct-expterm-0-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '0'],
    'uct-vs-decaying-epsilon-greedy-expterm-0-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '0'],
    'decaying-epsilon-greedy-vs-uct-expterm-0-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '0'],
    'uct-vs-decaying-epsilon-greedy-expterm-0dot5-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '0.5'],
    'decaying-epsilon-greedy-vs-uct-expterm-0dot5-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '0.5'],
    'uct-vs-decaying-epsilon-greedy-expterm-0dot5-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '0.5'],
    'decaying-epsilon-greedy-vs-uct-expterm-0dot5-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '0.5'],
    'uct-vs-decaying-epsilon-greedy-expterm-sqrt2-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '1.4142'],
    'decaying-epsilon-greedy-vs-uct-expterm-sqrt2-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '1.4142'],
    'uct-vs-decaying-epsilon-greedy-expterm-sqrt2-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '1.4142'],
    'decaying-epsilon-greedy-vs-uct-expterm-sqrt2-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '1.4142'],
    'uct-vs-decaying-epsilon-greedy-expterm-3-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '3'],
    'decaying-epsilon-greedy-vs-uct-expterm-3-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '3'],
    'uct-vs-decaying-epsilon-greedy-expterm-3-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '3'],
    'decaying-epsilon-greedy-vs-uct-expterm-3-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '3'],
    'uct-vs-decaying-epsilon-greedy-expterm-5-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '5'],
    'decaying-epsilon-greedy-vs-uct-expterm-5-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '5'],
    'uct-vs-decaying-epsilon-greedy-expterm-5-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '5'],
    'decaying-epsilon-greedy-vs-uct-expterm-5-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '5'],
    'uct-vs-decaying-epsilon-greedy-expterm-10-50its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p1explorationterm', '10'],
    'decaying-epsilon-greedy-vs-uct-expterm-10-50its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '50', '--p2trainingiterations', '50', '--p2explorationterm', '10'],
    'uct-vs-decaying-epsilon-greedy-expterm-10-500its': ['--p1',  'uct', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p1explorationterm', '10'],
    'decaying-epsilon-greedy-vs-uct-expterm-10-500its': ['--p1',  'decaying-epsilon-greedy', '--p2', 'uct', '--p1trainingiterations', '500', '--p2trainingiterations', '500', '--p2explorationterm', '10'],

    'boltzmann-vs-decaying-epsilon-greedy-tau-1': ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '1'],
    'decaying-epsilon-greedy-vs-boltzmann-tau-1': ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '1'],
    'boltzmann-vs-decaying-epsilon-greedy-tau-2': ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '2'],
    'decaying-epsilon-greedy-vs-boltzmann-tau-2': ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '2'],
    'boltzmann-vs-decaying-epsilon-greedy-tau-5': ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '5'],
    'decaying-epsilon-greedy-vs-boltzmann-tau-5': ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '5'],
    'boltzmann-vs-decaying-epsilon-greedy-tau-10': ['--p1',  'boltzmann', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '10'],
    'decaying-epsilon-greedy-vs-boltzmann-tau-10': ['--p1',  'decaying-epsilon-greedy', '--p2', 'boltzmann', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '10'],

    'epsilon-greedy-vs-decaying-epsilon-greedy-0':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0'],
    'epsilon-greedy-vs-decaying-epsilon-greedy-0dot1':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0.1'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0dot1':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0.1'],
    'epsilon-greedy-vs-decaying-epsilon-greedy-0dot2':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0.2'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0dot2':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0.2'],
    'epsilon-greedy-vs-decaying-epsilon-greedy-0dot3':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0.3'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0dot3':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0.3'],
    'epsilon-greedy-vs-decaying-epsilon-greedy-0dot4':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0.4'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0dot4':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0.4'],
    'epsilon-greedy-vs-decaying-epsilon-greedy-0dot5':  ['--p1',  'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p1explorationterm', '0.5'],
    'decaying-epsilon-greedy-vs-epsilon-greedy-0dot5':  ['--p1',  'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1trainingiterations', '200', '--p2trainingiterations', '200', '--p2explorationterm', '0.5'],

    # 'random-vs-uct-50its':          ['--p1',  'random', '--p2', 'uct', '--p2trainingiterations', '50', '--p2playout', 'random'],
    # 'random-vs-uct-200its':         ['--p1',  'random', '--p2', 'uct', '--p2trainingiterations', '200', '--p2playout', 'random'],
    # 'random-vs-uct-1000its':        ['--p1',  'random', '--p2', 'uct', '--p2trainingiterations', '1000', '--p2playout', 'random'],
    # 'uct-vs-random-50its':          ['--p1', 'uct', '--p2', 'random', '--p1trainingiterations', '50', '--p1playout', 'random'],
    # 'uct-vs-random-200its':         ['--p1', 'uct', '--p2', 'random', '--p1trainingiterations', '200', '--p1playout', 'random'],
    # 'uct-vs-random-1000its':        ['--p1', 'uct', '--p2', 'random', '--p1trainingiterations', '1000', '--p1playout', 'random'],
    # 'uct-vs-heuristic-uct-50its':   ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'heuristic', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'uct-vs-heuristic-uct-200its':  ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'heuristic', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'uct-vs-heuristic-uct-1000its': ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'heuristic', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'heuristic-uct-vs-uct-50its':   ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'heuristic', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'heuristic-uct-vs-uct-200its':   ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'heuristic', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'heuristic-uct-vs-uct-1000its':   ['--p1', 'uct', '--p2', 'uct', '--p1playout', 'heuristic', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'uct-vs-boltzmann-50its':       ['--p1', 'uct', '--p2', 'boltzmann', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'uct-vs-boltzmann-200its':       ['--p1', 'uct', '--p2', 'boltzmann', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'uct-vs-boltzmann-1000its':       ['--p1', 'uct', '--p2', 'boltzmann', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'boltzmann-vs-uct-50its':       ['--p1', 'boltzmann', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'boltzmann-vs-uct-200its':       ['--p1', 'boltzmann', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'boltzmann-vs-uct-1000its':       ['--p1', 'boltzmann', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'minimax-depth2-vs-uct-50its':        ['--p1', 'minimax', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p2trainingiterations', '50', '--p1minimaxdepth', '2'],
    # 'minimax-depth2-vs-uct-200its':        ['--p1', 'minimax', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p2trainingiterations', '200', '--p1minimaxdepth', '2'],
    # 'minimax-depth2-vs-uct-1000its':        ['--p1', 'minimax', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p2trainingiterations', '1000', '--p1minimaxdepth', '2'],
    # 'uct-minimax-depth2-50its':        ['--p1', 'uct', '--p2', 'minimax', '--p1playout', 'random', '--p1playout', 'random', '--p1trainingiterations', '50', 'p2minimaxdepth', '2'],
    # 'uct-minimax-depth2-200its':        ['--p1', 'uct', '--p2', 'minimax', '--p1playout', 'random', '--p1playout', 'random', '--p1trainingiterations', '200', 'p2minimaxdepth', '2'],
    # 'uct-minimax-depth2-1000its':        ['--p1', 'uct', '--p2', 'minimax', '--p1playout', 'random', '--p1playout', 'random', '--p1trainingiterations', '1000', 'p2minimaxdepth', '2'],
    # 'uct-vs-uct-tuned-50its':   ['--p1', 'uct', '--p2', 'uct-tuned', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'uct-vs-uct-tuned-200its':   ['--p1', 'uct', '--p2', 'uct-tuned', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'uct-vs-uct-tuned-1000its':   ['--p1', 'uct', '--p2', 'uct-tuned', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'uct-tuned-vs-uct-50its':       ['--p1', 'uct-tuned', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'uct-tuned-vs-uct-200its':       ['--p1', 'uct-tuned', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'uct-tuned-vs-uct-1000its':       ['--p1', 'uct-tuned', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'uct-vs-decaying-epsilon-greedy-50its':   ['--p1', 'uct', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'uct-vs-decaying-epsilon-greedy-200its':   ['--p1', 'uct', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'uct-vs-decaying-epsilon-greedy-1000its':   ['--p1', 'uct', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'decaying-epsilon-greedy-vs-uct-50its':  ['--p1', 'decaying-epsilon-greedy', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'decaying-epsilon-greedy-vs-uct-200its':  ['--p1', 'decaying-epsilon-greedy', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'decaying-epsilon-greedy-vs-uct-1000its':  ['--p1', 'decaying-epsilon-greedy', '--p2', 'uct', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'epsilon-greedy-vs-decaying-epsilon-greedy-50its':   ['--p1', 'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'epsilon-greedy-vs-decaying-epsilon-greedy-200its':   ['--p1', 'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'epsilon-greedy-vs-decaying-epsilon-greedy-1000its':   ['--p1', 'epsilon-greedy', '--p2', 'decaying-epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
    # 'decaying-epsilon-greedy-vs-epsilon-greedy-50its': ['--p1', 'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '50', '--p2trainingiterations', '50'],
    # 'decaying-epsilon-greedy-vs-epsilon-greedy-200its': ['--p1', 'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '200', '--p2trainingiterations', '200'],
    # 'decaying-epsilon-greedy-vs-epsilon-greedy-1000its': ['--p1', 'decaying-epsilon-greedy', '--p2', 'epsilon-greedy', '--p1playout', 'random', '--p2playout', 'random', '--p1trainingiterations', '1000', '--p2trainingiterations', '1000'],
}

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
