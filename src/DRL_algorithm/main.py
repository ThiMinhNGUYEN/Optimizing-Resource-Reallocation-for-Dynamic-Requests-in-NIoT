# This is a sample Python script.
import nfv_drl_traffic_model_change as drl_alg
import os
import numpy as np
import time

#### READ_ME ######

# create new folder "simple_test_input" and move the input file to it ###
# create new folder "simple_test_output" to save the output  ####
####################

if __name__ == '__main__':
   # assign directory
    algo = sys.argv[1] # algo can be "drl" or "opt"
    directory = sys.argv[2] # directory = 'simple_test_input'

    # iterate over files in the directory
    for f in os.listdir(directory):
        filename = os.path.join(directory, f)
        # checking if it is a file
        if os.path.isfile(filename):

            # filename = "output/EW20_1_10.txt"
            input_file = filename
            output_file = filename.replace("input", "output")
            print(input_file, " ::: ", output_file)
            capNodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, demands, functions = \
                drl_alg.readInputFile(input_file)

            # generate traffic from poisson process
            traffic_bandwidth = []
            for demand in range(len(demands)):
                s = np.random.poisson(45, 2000)
                # print(s)
                bwd = np.full(2000, 3)  # [size ,bandwidth]
                i_temp = 0
                finish = False
                for s_idx in s:
                    rand = np.random.randint(1, 11)
                    for idx in range(s_idx):
                        if (i_temp + idx) >= 2000:
                            finish = True
                            break
                        bwd[i_temp + idx] = rand
                    i_temp += s_idx
                    if finish or i_temp >= 2000:
                        break
                # print("bwddd = ", bwd)
                traffic_bandwidth.append(bwd)

            if algo == "drl":
                iterations, returns, time_seconds, rewards, iterations_full = \
                    drl_alg.nfv_drl2(capNodes, nodeCost, nodeType,
                                     nodeDelay, delayNodeOnFunction, links, functions, demands, traffic_bandwidth)
                drl_alg.save_fig(iterations, returns, 'Iterations', 'Average Deployment Cost', output_file)
                drl_alg.save_fig(iterations_full, rewards, 'Iterations', 'Rewards', "rewards_converge.png")
                drl_alg.save_to_output_file(iterations, returns, time_seconds, output_file)
                drl_alg.save_to_output_file(iterations_full, rewards, time_seconds, "rewards_converge.txt")
                output_file = "test_output/output_drl.txt"
                drl_alg.save_file(input_file, len(demands), returns[49], time_seconds, output_file)
            if algo == "opt":
                print("opt: start")
                time1 = time.time()
                output_file = "test_output/output.txt"
                bw = set()
                for traffic_d in traffic_bandwidth:
                    bw.add(0)
                    for idx in range(1, len(traffic_d)):
                        if traffic_d[idx - 1] != traffic_d[idx]:
                            bw.add(idx)
                old_idx = 0
                total_cost = 0
                vnf_cost = 0
                print("bw ::: ", bw)
                for b in bw:
                    newbw = []
                    total_cost += vnf_cost * (b - old_idx)
                    for di in range(len(demands)):
                        newbw.append(traffic_bandwidth[di][b])
                    vnf_cost = drl_alg.find_optimal(newbw, capNodes, nodeCost, nodeType, nodeDelay,
                                                    delayNodeOnFunction, links, functions, demands)
                    old_idx = b
                total_cost += vnf_cost * (100 - old_idx)
                print(total_cost)
                time_total = time.time() - time1
                drl_alg.save_file(input_file, len(demands), total_cost, time_total, output_file)
