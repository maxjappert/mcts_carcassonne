# Monte-Carlo Tree Search on Carcassonne

This repository contains the code for my Bachelor's thesis, which involves implementing variations of the Monte Carlo 
tree search framework for the board game Carcassonne. The program additionally allows for playing the game via console
inputs. With the argument `-h` the syntax for passing the introduced variables is printed to console.

All random actions can be reproduced for each player separately by passing a random seed as an argument. Additionally,
the deck can be reproduced by the same means.

The following types of players can be chosen and compared:

## MCTS

There are different types of MCTS players to choose from. For MCTS, a number of training iterations needs to be specified
as an argument. The default number of iterations is 150. Additionally, MCTS can either use a random default policy, 
where the playout consists of choosing random moves, or a heuristic default policy, which utilises domain-specific
knowledge during playout, always picking the next move which maximises the heuristic function.

The following implementations of MCTS can be chosen:

### UCT

An obvious candidate, UCT uses the UCT function during the tree policy, thereby balancing exploration and exploitation
according to the exploration term, which can be passed as an argument. Additionally, an exploration term delta can be
passed, which is a term added to the exploration term after every decision the player has made. It makes sense to pass
a negative value, since it makes sense to explore more at the beginning and become increasingly more greedy.

### Epsilon-Greedy MCTS

Epsilon-greedy uses a tree policy, whereby with a probability of epsilon (here epsilon is passed as the exploration term)
a random move is chosen, otherwise the move with the highest approximated game theoretical value is chosen (this value
corresponds to the UCT function with an exploration term of 0).

### Heuristic Epsilon-Greedy MCTS

Same as the normal epsilon-greedy, with the exception that with a probability of (1-epsilon) during the tree policy
the next node is chosen as the node representing the state which maximises the heuristic function.

### Heuristic MCTS

Hereby the tree policy picks the move which maximises the heuristic function. Corresponds to heuristic-epsilon-greedy
with an epsilon = 0.

## Random

As a benchmark, players can also choose completely random moves. This allows for testing if an implementation even
improves on choosing moves randomly.

## Human

A human player takes inputs from the console in order to play the game. Thereby a user can play the game against any
type of player which is implemented.

## Greedy

The greedy (or heuristic) player always picks the next move which maximises the heuristic function. This implementation
is useful for testing the effectiveness of a heuristic function.