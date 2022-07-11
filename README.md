# MOMoT for Reactive Use at Runtime

This project demonstrates a reactive planning approach that integrates Digital Twins (DTs) with planning approaches to deal with uncertainty in the exposed environment. The MOMoT Framework for model-driven optimization hereby acts as planning unit for system operations. It represents problem domains by means of Ecore meta-models and problem instances as models to be optimized through executing graph transformation rules. The DT carries is capable to automatically detect and treat unforeseen changes that affect operation. The project considers a case study to demonstrate the reactive planning frameworks feasibility and potential to optimize system performance.

The repository holds the core of the MOMoT framework ([source](https://github.com/martin-fleck/momot), [project page](http://martin-fleck.github.io/momot/)) with extensions to simulate a running system and invoke replanning as instructed by the DT. The following sections briefly introduces the background, the case study, the configurable components, and the output.

## About

In this prototype we test our reactive planning approach as we employ a model-driven optimization framework, MOMoT, as the planning unit for a given running system.
The reactive planning architecture involves a DT that keeps track of the systems **current runtime model** and the **expected runtime model** based on planned operations. When
a descrepancy is detected by the **Conformance Checker**, e.g., caused by perturbation, the **Decision Maker** may trigger a replanning process. Depending on the configuration the **Plan Preparer** invokes
the planner to find a new plan compliant with user and system specification. A new plan will then be deployed later by the DT to continue or maintain system operation in the best found
way. The goal we pursue with such integrated DT systems is to improve plans at runtime, adapt to unforeseen disturbances quickly, and consider replanning parallel to execution to mitigate idle states.

## Case study

A _StackModel_ consists of up to multiple *Stack*s with a load each. The goal in this case study is to apply shift-operations in form of two rules, _shiftLeft_ and _shiftRight_, where a certain amount is
transfered from the source stack to its left or right neighbour, i.e., the target stack. A plan for execution is determined as an ordered sequences of rule applications. Hereby, the standard deviations should be minimized, which poses the primary objective for operation planning.

<figure>

<img src="./examples/at.ac.tuwien.big.momot.examples.stack/model/stack.svg" alt="Stack Meta-Model">
<figcaption><b>Meta-Model: Stack Load Balancing Case Study</b></figcaption>
</figure>

More information on the case study and a model-driven solution using MOMoT can be found [here](http://martin-fleck.github.io/momot/casestudy/stack/).

## Configuration

The utilities for reactive planning are implemented in the MOMoT core plugins as provided in this project. They are utilized to configure the planning setup for the case study in an own project (_./examples/at.ac.tuwien.big.momot.examples.stack_), which involves a planning strategy and the disturber component. Regarding the former, the planner may search for a plan that suffices certain quality requirements or runs for fixed number of algorithm evaluations.

Defining quality requirements and planning strategies:

```java
   /* PLANNING CONFIGURATION */
   final static double OBJECTIVE_THRESHOLD = 1.5905; // Search plan with standard deviation of 1.5905 at maximum
   final static int OBJ_INDEX = 0; // 0 = standard deviation for stack allocation case study
   final static Map<Integer, Double> objectiveThresholds = Map.of(OBJ_INDEX, OBJ_THRESHOLD);

   final static List<Planning> PLANNING_STRATEGIES = Arrays.asList(
        // Strategy 1: Initinal plan to reach objective, replan for 20,000 evaluations
         Planning.create(PlanningStrategy.create("NSGAII", 0).withObjectiveThresholds(objectiveThresholds),
               ReplanningStrategy.create("NSGAII", 0).withMaxEvaluations(20000).asReplanningStrategy()),
        // Strategy 2: Initial plan to reach objective, replan using "Naive repair"
         Planning.create(PlanningStrategy.create("NSGAII", 0).withObjectiveThresholds(objectiveThresholds),
               ReplanningStrategy.naive())
    );
```

Instead of a simple replanning approach, "replanning-on-the-fly" allows for replanning while system operation continues with feasible operations of the remaining plan. Therefore, the system state is forecasted
for a certain number of steps depening on the operations executed next to planning, and a new plan determined to proceed from that expected state.

```java
   final static List<Planning> PLANNING_STRATEGIES = Arrays.asList(
         // Initial plan to reach objective, replan from forecasted state (x+1, x+2, ..., x+10)
         // for available time while system keeps exeuting and until threshold is reached
          Planning.create(PlanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds),
          ReplanningStrategy.create("NSGAII", 1).withObjectiveThresholds(objectiveThresholds)
          .castAsReplanningStrategy()
          .withPredictivePlanning(PredictiveReplanningStrategy
          .create("NSGAII", 1, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
          PredictiveReplanningType.TERMINATE_AFTER_TIME_IF_OBJECTIVE_SATISFIED, 10)
          .withObjectiveThresholds(objectiveThresholds).castAsPredictiveReplanningStrategy()))
    );
```

The disturber simulates the effects of the system operating in an uncertain environment. It causes unplannend changes amid execution of the current plan. Therefore, such perturbances can
be set to occur as the current plan is executed, either in the first/middle/last 10% of planned operations. The type of change to the model is defined by a givven error type. The _ERRORS_PER_DISTURBANCE_ denotes the number of random operations to be executed when the disturbance arrises, and indicates its severity.

```java
   private static final List<ErrorType> ERROR_TYPE_LIST = ImmutableList.of(ErrorType.REMOVE_STACKS); // Perturbation involves removing stacks from model
   private static final List<ErrorOccurence> ERROR_OCCURENCE_LIST = ImmutableList.of(ErrorOccurence.FIRST_10_PERCENT,
         ErrorOccurence.MIDDLE_10_PERCENT, ErrorOccurence.LAST_10_PERCENT);
   final static int ERRORS_PER_DISTURBANCE = 5;
```

In a PlanningSuite setup, multiple planning strategies and/or disturbance configurations may be tested and recorded in a single run as is shown in the snippets above.

## Output

Runtime information and relevant results such as planning times, execution times, and obtained objective
qualities for all runs in the executed suite are saved in the case studies project subfolder _./output/simulation_.
