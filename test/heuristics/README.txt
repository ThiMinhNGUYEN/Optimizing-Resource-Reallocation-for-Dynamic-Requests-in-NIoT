Please follow these steps to run java app:

Step 1: Download and Install Gurobi at https://www.gurobi.com/downloads/gurobi-software/
- NOTE: We need a version from 11.0.3
Step 2: Request and Install the academic license at https://portal.gurobi.com/iam/licenses/list/
Step 3: Download jar file for our development at https://drive.google.com/file/d/17Rv5_rUz8BUBS2CI5S7cIEbZhJOzeVnH/view?usp=sharing 
(It contains all required libraries so it is more than 25 MB that we can not upload directly to Github)
Step 4: Run jar file with command line
  java -jar PTproblem_runfile.jar para1 para2 para3 para4

where: 
- PTproblem_runfile.jar : our jar file
- para1 : input folder name containing input files
- para2 : output folder name (if it doesnt exist, they will create new folder)
- para3: algorithm name including Optimize, PTH
- para4: objective function option: PTO, PTO_b, PTO_q  

For example: we put the test "FW_40_1_10.txt" inside a folder "input_files" then run
1. run PTO algorithm:
  java -jar PTproblem_runfile.jar input_files output_files PTO PTO 
2. run PTH algorithm:
  java -jar PTproblem_runfile.jar input_files output_files PTH PTH
