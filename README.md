# MOMoT for Reactive Use at Runtime

This repository holds the core of the MOMoT framework ([source](https://github.com/martin-fleck/momot), [project page](http://martin-fleck.github.io/momot/)) with extensions to support reinforcement rearning (RL)
methods. It evaluates the tool for (re)planning at runtime.

Executing StackRuntimeSimulator.java in the stack exmaple source results in the following output:

<details>
  <summary>Click to show console output</summary>
  
  ```
##########################################################################################
INITIAL INPUT MODEL
##########################################################################################

Stack_1 Stack_2 Stack_3 Stack_4 Stack_5
1 7 3 9 5

##########################################################################################
NSGAII (1/2)
##########################################################################################

##################################
INITIAL PLAN
##################################

Run 'NSGAII' 1 times...
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by at.ac.tuwien.big.moea.experiment.instrumenter.SearchInstrumenter (file:/C:/Users/sigi/Desktop/uni/CDL_Mint/Projekte/dt_stack/initial/plugins/at.ac.tuwien.big.moea/target/classes/) to field java.util.HashSet.serialVersionUID
WARNING: Please consider reporting this to the maintainers of at.ac.tuwien.big.moea.experiment.instrumenter.SearchInstrumenter
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
[15:41:06.574] Run 1 of 1 started.
[15:41:09.415] Run 1 of 1 terminated after 00:00:02.837 (2837 ms).
[15:41:09.416] Total runtime for 1 seeds: 00:00:02.842 (2842 ms).
Number of variables: 3
Variable[0]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_5'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 4

  Variable[1]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 3

  Variable[2]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_3'
- parameter 'amount' => 2

Number of attributes: 1
AggregatedFitness: 3.632455532033676
Number of objectives: 2
Standard Deviation: 0.6324555320336759
SolutionLength: 3.0
Number of constraints: 0

##################################
🛑 CHANGE EVENT
##################################

-----PLAN EXECUTED SO FAR (1/3 Rules)-----

Number of variables: 1
Variable[0]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_5'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 4

Number of attributes: 1
AggregatedFitness: 3.8284271247461903
Number of objectives: 2
Standard Deviation: 2.8284271247461903
SolutionLength: 1.0
Number of constraints: 0

-----RESULT MODEL (after planned execution so far)-----

Stack_1 Stack_2 Stack_3 Stack_4 Stack_5
5 7 3 9 1

-----MODEL CHANGE (added stack)-----

Stack_1 Stack_2 Stack_3 Stack_4 Stack_5 Stack_6
5 7 3 9 1 0

##################################
REPLANNING (1)
##################################

Run 'NSGAII' 1 times...
[15:41:09.697] Run 1 of 1 started.
[15:41:11.287] Run 1 of 1 terminated after 00:00:01.590 (1590 ms).
[15:41:11.287] Total runtime for 1 seeds: 00:00:01.590 (1590 ms).
Number of variables: 3
Variable[0]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 4

  Variable[1]: Match for rule 'shiftLeft':

- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 3

  Variable[2]: Match for rule 'shiftLeft':

- parameter 'fromId' => 'Stack_1'
- parameter 'toId' => 'Stack_6'
- parameter 'amount' => 4

Number of attributes: 1
AggregatedFitness: 3.6871842709362768
Number of objectives: 2
Standard Deviation: 0.6871842709362768
SolutionLength: 3.0
Number of constraints: 0

##########################################################################################
QLearning (2/2)
##########################################################################################

##################################
INITIAL PLAN
##################################

Run 'QLearning' 1 times...
[15:41:11.335] Run 1 of 1 started.
[15:41:24.142] Run 1 of 1 terminated after 00:00:12.805 (12805 ms).
[15:41:24.142] Total runtime for 1 seeds: 00:00:12.806 (12806 ms).
Number of variables: 5
Variable[0]: Assignment for unit 'shiftLeft':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_3'
- parameter 'amount' => 2
  Match for rule 'shiftLeft':
- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_3'
- parameter 'amount' => 2

  Variable[1]: Assignment for unit 'shiftLeft':

- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 2
  Match for rule 'shiftLeft':
- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 2

  Variable[2]: Assignment for unit 'shiftRight':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 2
  Match for rule 'shiftRight':
- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 2

  Variable[3]: Match for rule 'shiftLeft':

- parameter 'fromId' => 'Stack_1'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 1

  Variable[4]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_5'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 3

Number of attributes: 1
AggregatedFitness: 5.0
Number of objectives: 2
Standard Deviation: 0.0
SolutionLength: 5.0
Number of constraints: 0

##################################
🛑 CHANGE EVENT
##################################

-----PLAN EXECUTED SO FAR (1/5 Rules)-----

Number of variables: 1
Variable[0]: Assignment for unit 'shiftLeft':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_3'
- parameter 'amount' => 2
  Match for rule 'shiftLeft':
- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_3'
- parameter 'amount' => 2

Number of attributes: 1
AggregatedFitness: 3.1908902300206643
Number of objectives: 2
Standard Deviation: 2.1908902300206643
SolutionLength: 1.0
Number of constraints: 0

-----RESULT MODEL (after planned execution so far)-----

Stack_1 Stack_2 Stack_3 Stack_4 Stack_5
1 7 5 7 5

-----MODEL CHANGE (added stack)-----

Stack_1 Stack_2 Stack_3 Stack_4 Stack_5 Stack_6
1 7 5 7 5 0

##################################
REPLANNING (1)
##################################

Run 'QLearning' 1 times...
[15:41:24.207] Run 1 of 1 started.
[15:41:38.108] Run 1 of 1 terminated after 00:00:13.902 (13902 ms).
[15:41:38.109] Total runtime for 1 seeds: 00:00:13.902 (13902 ms).
Number of variables: 5
Variable[0]: Assignment for unit 'shiftLeft':

- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 4
  Match for rule 'shiftLeft':
- parameter 'fromId' => 'Stack_2'
- parameter 'toId' => 'Stack_1'
- parameter 'amount' => 4

  Variable[1]: Assignment for unit 'shiftRight':

- parameter 'fromId' => 'Stack_3'
- parameter 'toId' => 'Stack_4'
- parameter 'amount' => 1
  Match for rule 'shiftRight':
- parameter 'fromId' => 'Stack_3'
- parameter 'toId' => 'Stack_4'
- parameter 'amount' => 1

  Variable[2]: Assignment for unit 'shiftRight':

- parameter 'fromId' => 'Stack_1'
- parameter 'toId' => 'Stack_2'
- parameter 'amount' => 1
  Match for rule 'shiftRight':
- parameter 'fromId' => 'Stack_1'
- parameter 'toId' => 'Stack_2'
- parameter 'amount' => 1

  Variable[3]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_4'
- parameter 'toId' => 'Stack_5'
- parameter 'amount' => 4

  Variable[4]: Match for rule 'shiftRight':

- parameter 'fromId' => 'Stack_5'
- parameter 'toId' => 'Stack_6'
- parameter 'amount' => 5

Number of attributes: 1
AggregatedFitness: 5.372677996249965
Number of objectives: 2
Standard Deviation: 0.37267799624996495
SolutionLength: 5.0
Number of constraints: 0

```
</details>
```

Generated models and plans will be saved as defined with _OUT_BASE_PATH_.
The generated summary.txt has the following format:

<details>
  <summary>Click to show summary.txt output</summary>
  
  ```
##########################################################################################
NSGAII
##########################################################################################

##################################
DIFFERENCE STATS
##################################

Initial Plan: [0.6324555320336759, 3.0]
Without replanning: [1.950783318453271, 2.0]
With replanning: [0.6871842709362768, 3.0]
##########################################################################################
QLearning
##########################################################################################

##################################
DIFFERENCE STATS
##################################

Initial Plan: [0.0, 5.0]
Without replanning: [2.4776781245530843, 3.0]
With replanning: [0.37267799624996495, 5.0]

```
</details>
```
