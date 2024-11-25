Please follow these steps to run the Python app:

Step 1: Download and Install Gurobi (Reference: https://www.gurobi.com/downloads/gurobi-software/):
      python -m pip install gurobipy==11.0.3

- Note: Our app requires at least version 11.0.3 of Gurobi. 
With Gurobi 11, 'gurobipy' can be used with Python versions 3.8, 3.9, 3.10, 3.11, and 3.12
  
Step 2: Request and Install the academic license at https://portal.gurobi.com/iam/licenses/list/

Step 3: Install required packages with version as following:
- pip 21.3.1
- tensorflow 2.15.0
- tensorboard 2.15.2
- keras 2.15.0
- agents 1.4.0
- tf_agents 0.19.0
- gym 0.23.0
- pandas 2.2.1
- sympy 1.12
- sac 0.7.1
- cplex 22.1.1.0
- matplotlib 3.8.3
- dijkstar 2.6.0
- dijkstra 0.2.1
- numpy 1.26.4
- scipy 1.12.0
- fnss 0.9.1
- docplex 2.25.236

Step 4: Run the Python file using the command line:
  python main.py para1 para2
Where: 
- "main.py" : Our Python file
- "para1"   : The algorithm name, such as 'drl', 'opt'
- "para2"   : The input folder name containing input files

For example: 
 1. To run the RTL algorithm
  python main.py drl input_files 
 2. To run the RTA algorithm
  python main.py opt input_files
