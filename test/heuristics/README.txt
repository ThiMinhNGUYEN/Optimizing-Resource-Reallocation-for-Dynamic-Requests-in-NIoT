Please follow these steps to run the Java app:

Step 1: Download and Install Gurobi at https://www.gurobi.com/downloads/gurobi-software/
- Note: We need a version of at least 11.0.3
Step 2: Request and Install the academic license at https://portal.gurobi.com/iam/licenses/list/
Step 3: Download the JAR file for our development from https://drive.google.com/file/d/17Rv5_rUz8BUBS2CI5S7cIEbZhJOzeVnH/view?usp=sharing 
(It contains all required libraries, so it is more than 25MB, which we can not upload directly to Github.)
Step 4: Run the JAR file with the following command line:
  java -jar PTproblem_runfile.jar para1 para2 para3 para4

where: 
- "PTproblem_runfile.jar" is our JAR file
- "para1" is the input folder name containing the input files
- "para2" is the output folder name (if it doesnt exist, a new folder will be created)
- "para3" is the algorithm name, including [Optimize, PTH]
- "para4" is the objective function option: [PTO, PTO_q], [PTH, PTH_q]  

For example: 
1. to run the PTO algorithm:
  java -jar PTproblem_runfile.jar input_files output_files Optimize PTO
  java -jar PTproblem_runfile.jar input_files output_files Optimize PTO_q
2. to run the PTH algorithm:
  java -jar PTproblem_runfile.jar input_files output_files PTH PTH
  java -jar PTproblem_runfile.jar input_files output_files PTH PTH_q
