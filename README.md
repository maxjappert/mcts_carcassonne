# Monte-Carlo Tree Search for Carcassonne

This repository contains the code for my Bachelor's thesis, which involves implementing variations of the Monte Carlo 
tree search framework for the board game Carcassonne. The program additionally allows for playing the game via console
inputs. Please use the `-h` or `--help` flag for details on the possible arguments.

All random actions can be reproduced for each player separately by passing a random seed as an argument. Additionally,
the deck can be reproduced by the same means.

The following types of players can be chosen and compared:

## MCTS

There are different types of MCTS players to choose from. For MCTS, a number of training iterations needs to be specified
as an argument. The default number of iterations is 150. Additionally, MCTS can either use a random default policy, 
where the playout consists of choosing random moves, or a heuristic default policy, which utilises domain-specific
knowledge during playout, always picking the next move which maximises the heuristic function.

For the MCTS player, the playout-payoff to be propagated back up the tree can be weighted according to the
point in the game by adding a backpropagation-weight-delta. This comes from the intuitive notion that later playouts are
less random and more representative of the actual value of a node depending on the size of the tree, i.e., the depth 
of the node (Xie, Liu 2009).

The following tree policies for MCTS have been implemented:

### UCT

An obvious candidate, UCT uses the UCT function during the tree policy, thereby balancing exploration and exploitation
according to the exploration term, which can be passed as an argument. Additionally, an exploration term delta can be
passed, which is a term added to the exploration term after every decision the player has made. It makes sense to pass
a negative value, since it makes sense to explore more at the beginning and become increasingly more greedy.

### UCT-Tuned

Similar to UCT, yet uses an alternative formula (UCB1-Tuned) for the upper confidence bounds. As proposed by Auer et al.
(2002).

### Epsilon-Greedy MCTS

Epsilon-greedy uses a tree policy, whereby with a probability of epsilon (here epsilon is passed as the exploration term)
a random move is chosen, otherwise the move with the highest approximated game theoretical value is chosen (this value
corresponds to the UCT function with an exploration term of 0).

### Decaying Epsilon-Greedy

With decaying epsilon-greedy the value of epsilon is always 1/k for iteration k of the training process. Thereby the
algorithm gets increasingly greedy during the training process, which corresponds to intuition.

### Heuristic Epsilon-Greedy MCTS

Same as the normal epsilon-greedy the node is chosen which maximises the heuristic function with a probability of
(epsilon-1). A random node is chosen with a probability of epsilon. For epsilon = 0 we have a heuristic
tree policy which traverses the tree only according to the heuristic function.

### MCTS with Boltzmann Exploration

With this option the tree policy chooses the next nodes according to the Boltzmann distribution. The exploration term
corresponds to tau in the formula.

## Random

As a benchmark, players can also choose completely random moves. This allows for testing if an implementation even
improves on choosing moves randomly.

## Human

A human player takes inputs from the console in order to play the game. Thereby a user can play the game against any
type of player which is implemented.

## Greedy

The greedy (or heuristic) player always picks the next move which maximises the heuristic function. This implementation
is useful for testing the effectiveness of a heuristic function.

## Minimax with alpha-beta Pruning

The search depth, before the value of a state is determined with a playout, can be specified as an argument via the 
console. Since this has an infeasible runtime (probably due to the insane branching factor), this is more of a symbolic

implementation.

### Backpropagation Weight

Additionally, for the MCTS player, the playout-payoff to be propagated back up the tree can be weighted according to the
point in the game by adding a backpropagation-weight-delta. This comes from the intuitive notion that the playouts are
more representative of the actual value of a node depending on the size of the tree, i.e., the depth of the node.