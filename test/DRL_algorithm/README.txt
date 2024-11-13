Please follow these steps to run python app:

### If Gurobi is already install in your machine, please ignore Step 1 and Step 2 ####

Step 1: Download and Install Gurobi at https://www.gurobi.com/downloads/gurobi-software/
- NOTE: We need a version from 11.0.3

Step 2: Request and Install the academic license at https://portal.gurobi.com/iam/licenses/list/

Step 3: Install python and required package with version as following:
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

Step 4: Run jar file with command line
  java -jar PTproblem_runfile.jar para1 para2 para3 para4

where: 
- PTproblem_runfile.jar : our jar file
- para1 : input folder name containing input files
- para2 : output folder name (if it doesnt exist, they will create new folder)
- para3: algorithm name including Optimize, PTH
- para4: objective function option: PTO, PTO_b, PTO_q  

For example: we put the test "FW_40_1_10.txt" inside a folder "input_files" then run
  java -jar PTproblem_runfile.jar input_files output_files PTO PTO
  java -jar PTproblem_runfile.jar input_files output_files PTH PTO
