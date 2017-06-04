# JadeRelayRace
Experimenting mobility of JADE agents in a relay race scenario

Command line argument to run all the agents

-------Gui Boot--------------------
-gui -host localhost -port 420

-------ExperimentMasterAgent-------
Parameters: maxRetries, initLaps, step
-container -host localhost -port 420 -agents "ExperimentAgent:ExperimentMasterAgent(2,2,1)"

------RunnerAgent------------------
Parameters: isCaptain, Target agent
-container -host localhost -port 420 -agents "RA1:RunnerAgent(false,RA2)"

-----RunnerAgentCaptain------------
Parameters: isCaptain, Target agent
-container -host localhost -port 420 -agents "RAc1:RunnerAgent(true,RA1)"
