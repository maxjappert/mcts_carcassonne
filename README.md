# Monte-Carlo Tree Search for Carcassonne
This repository contains the code for my Bachelor's thesis, which involves implementing variations of the Monte Carlo
tree search framework for the board game Carcassonne. The program additionally allows for playing the game via console
inputs.

All random actions can be reproduced for each player separately by passing a random seed as an argument. Additionally,
the deck can be reproduced by the same means.

The following arguments can be stated:

                  --p[1/2] [decaying-]<tree policy type> Specify the type of tree policy the MCTS player should
                                                         use. The possible types are:
                                                         ['uct'/'human'/'random'/'epsilon-greedy'/
                                                         'heuristic'/'heuristic-mcts'/'boltzmann'/'minimax'/
                                                         'uct-tuned']
                  --p[1/2]seed <Integer>                 Make the random actions for a given player reproducible
                                                         by specifying a seed.
                  --deckseed <Integer>                   Make the shuffling of the deck reproducible.
                  --p[1/2]explorationconstant <Float>    The exploration term for the UCT player. Acts as the
                                                         ε for ε-greedy players.
                  --p[1/2]meepleplacementprob <Float>    Probability of considering a meeple placement for random
                                                         playouts.
                  --p[1/2]trainingiterations <Integer>   The number of training iterations for a given MCTS player.
                  --p[1/2]explorationtermdelta <Float>   This term is added to the exploration term after every
                                                         move a MCTS  player plays.
                  --p[1/2]playout <Type>                 Decides the type of playout the MCTS player will perform.
                                                         Possible types are: ['random'/'heuristic']
                  --p[1/2]backpropdelta <Float>          This term is added to the backpropagation-weight
                                                         after every move an MCTS-player makes.
                  --p[1/2]minimaxdepth <Integer>         How deep does the Minimax-Player actually perform a
                                                         Minimax-Search before switching to the default policy.
                  -g or --graphwiz <Boolean>             Specify if a .dot file should be created which can be
                                                         used to visualise the MCTS-tree.
                  -v or --verbose <Boolean>              Controls if detailed information on the game progress
                                                         should be printed to console. True by default. If set
                                                         to false, then the only things printed to console are
                                                         a dump of the configurations, the final board and the
                                                         final score.

