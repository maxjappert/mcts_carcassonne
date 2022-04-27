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
BHOSLIB_GRAPHS = sorted(glob.glob(os.path.join(BENCHMARKS_DIR, "bhoslib", "*.mis")))
RANDOM_GRAPHS = sorted(glob.glob(os.path.join(BENCHMARKS_DIR, "random", "*.txt")))
ALGORITHMS = ["2approx", "greedy"]
SEEDS = 2018
####


TIME_LIMIT = 1800
MEMORY_LIMIT = 2048

if REMOTE:
    ENV = BaselSlurmEnvironment(email="my.name@unibas.ch")
    SUITE = BHOSLIB_GRAPHS + RANDOM_GRAPHS
else:
    ENV = LocalEnvironment(processes=8)
    # Use smaller suite for local tests.
    SUITE = BHOSLIB_GRAPHS[:1] + RANDOM_GRAPHS[:1]


### muss angepasst werden
ATTRIBUTES = [
    "cover",
    "cover_size",
    "error",
    "solve_time",
    "solver_exit_code",
    Attribute("solved", absolute=True),
]
####


# Create a new experiment.
exp = Experiment(environment=ENV)
# TODO
exp.add_resource("solver", os.path.join(SCRIPT_DIR, "solver.py"))
# Add custom parser.
exp.add_parser("parser.py")

ALGORITHMS = {
    'uct-vs-heuristic': ['--player1', 'uct', ...],
    'heuristic-vs-uct': ['--player1', 'heuristic', ...],
    'random-vs-heuristic': ['--player1', 'random', ...],
    'heuristic-vs-random': ['--player1', 'random', ...],
}
for algo_name, algo_cmd in ALGORITHMS.items():
    for seed in range(5):
        # loop over both positionings of players?
        run = exp.add_run()
        run.add_command(
            f"{algo_name}",
            ["{solver}", algo_cmd, "--seed", 2018],
            time_limit=TIME_LIMIT,
            memory_limit=MEMORY_LIMIT,
        )
        # AbsoluteReport needs the following attributes:
        # 'domain', 'problem' and 'algorithm'.
        domain = "carcassone"
        task_name = "carcassone"
        run.set_property("domain", domain)
        run.set_property("problem", task_name)
        run.set_property("algorithm", algo_name)
        # BaseReport needs the following properties:
        # 'time_limit', 'memory_limit', 'seed'.
        run.set_property("time_limit", TIME_LIMIT)
        run.set_property("memory_limit", MEMORY_LIMIT)
        run.set_property("seed", SEED)
        # Every run has to have a unique id in the form of a list.
        run.set_property("id", [algo, domain, task_name, seed])

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
