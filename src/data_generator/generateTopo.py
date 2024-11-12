
import random
import sys

import fnss;
import networkx as nx;


def cap_change(topo):

    maxDemand = 100
    functionNo = 14
    funcList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
    # print(funcList)
    nodeCap = 100
    linkCap = 100
    demandBw = random.randint(1, 5)
    # functionReq = random.randint(1, 2)
    functionDelay = 1.0  # not used
    arrivalTime = 1.0  # not used
    # demandRate = random.randint(1, 30) #demand SFC delay
    n = 20

    # ba = nx.barabasi_albert_graph(n, edge_attached, 40) # seed=40
    # Random graph G_{n,p} (Erdos-Renyi graph, binomial graph): n node, chooses each of the possible edges with probability p.

    # BA topology
    if topo == "BA":
        randTopo = fnss.erdos_renyi_topology(n, 0.2)
        G = nx.Graph(randTopo)  # ba topology
        topoName = "BA" + str(n) + "_"

    # The Waxman-2 random topology models place n nodes uniformly at random in a rectangular domain.
    if topo == "WA":
        wmtopo = fnss.waxman_1_topology(n, alpha=0.8, beta=0.2, L=1)
        G = nx.Graph(wmtopo)
        topoName = "WAXMAN" + str(n) + "_"
    if topo == "GEANT":
        G = nx.read_graphml("geant.graphml")  # geant topology
        topoName = "GEANT40_"

    print("print nodes: ----------")
    i = 0
    for node in G.nodes(data=True):
        # print(node)
        i = i + 1

    nodeNo = i
    # print("node Number " + str(nodeNo))
    # print("print edges : --------")


    i = 0
    for edge in G.edges(data=True):
        # print(edge)
        i = i + 1
    #  print(edge[0])
    #  print(edge[1])
    edgeNo = i
    # print("edge number = " + str(edgeNo))
    # print("Longitude-----")
    links = [[0 for _ in range(int(nodeNo))] for _ in range(int(nodeNo))]

    for edge in G.edges(data=True):
        x = int(edge[0]) - 1
        y = int(edge[1]) - 1
        links[x][y] = linkCap
        links[y][x] = linkCap

        # f = open('geant.txt', 'w')
        # f = open('waxman50.txt', 'w')
        # Failures /Cost /Delay on node
        numberList = [0, 1]
        failuredNodes = random.choices(numberList, weights=(80, 20),
                                       k=nodeNo)  # probability of failure equals 40% 60-40

        failure_prob = [0 for _ in range(int(nodeNo))]
        nodeCost = [0 for _ in range(int(nodeNo))]
        nodeDelay = [0 for _ in range(int(nodeNo))]
        nodeType = [0 for _ in range(int(nodeNo))]
        for i in range(nodeNo):
            # if failuredNodes[i]:
            #  failure_prob[i] = random.uniform(0.0, 1.0)
            # else:
            #  failure_prob[i] = 0.0

            nodeCost[i] = random.randint(1, 5)
            nodeDelay[i] = random.uniform(0.01, 0.1)
            nodeType[i] = random.choice([2,3])

        # Datasize on function
        data_size = [0 for _ in range(int(functionNo))]
        for i in range(functionNo):
            data_size[i] = random.randint(1, 3)

        # Function node Delay
        functionNodeDelay = [[0 for _ in range(int(nodeNo))] for _ in range(int(functionNo))]
        for i in range(functionNo):
            for j in range(nodeNo):
                functionNodeDelay[i][j] = random.uniform(0.01, 0.1)

    # m function
    functionReq = []
    functionType = [0 for _ in range(int(functionNo))]
    for i in range(functionNo):
        funReq = random.randint(1, 2)
        functionReq.append(funReq)
        functionType[i] = random.choice([2,3])
        print(functionType[i])

    # d demand
    src = [0 for _ in range(int(maxDemand))]
    dest = [0 for _ in range(int(maxDemand))]
    demandRate = [0 for _ in range(int(maxDemand))]
    demandBw = [0 for _ in range(int(maxDemand))]
    funNo = [0 for _ in range(int(maxDemand))]
    for i in range(maxDemand):
        funNo[i] = random.randint(2, 5)
    fdLst = []


    for i in range(maxDemand):
        src[i] = random.randint(1, int(nodeNo))
        while True:
            temp = random.randint(1, int(nodeNo))
            if (temp != src[i]) :
                source = list(G.nodes)[src[i] - 1]
                # print(source)
                destination = list(G.nodes)[temp - 1]
                #  print(destination)
                if nx.has_path(G, source, destination):
                    dest[i] = temp
                    break
        demandRate[i] = random.randint(1, 30)  # demand SFC delay
        demandBw[i] = random.randint(1, 5)
        fdLst.append(random.sample(funcList, funNo[i]))

    for i in range(maxDemand):
        print(str(src[i]) + ", " + str(dest[i]) + "\n")

    #cap_list = [10, 15, 20, 25, 30, 50, 80, 100, 150, 200]
    cap_list = [5, 8, 10, 12, 15, 18, 20, 25, 30, 50, 80, 100, 150, 200]
    demandNo = maxDemand
    # demandList = [2, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200]
    # demandList = [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000]
    count = 0
    for nodeCap in cap_list:
        # print("demandNo = " + str(demandNo))

        fileName = "./output/" + topoName + str(count) + "_" + str(
            nodeCap) + ".txt";
        count = count + 1;
        f = open(fileName, 'w')
        f.write(str(functionNo) + " " + str(demandNo) + " " + str(nodeNo) + " " + str(edgeNo) + "\n")

        # list of function ----------------
        for i in range(functionNo):
            f.write(str(functionDelay) + " " + str(functionReq[i])+ " " + str(functionType[i]) + ";")
        f.write("\n")

        # list of demand -----------------
        for i in range(demandNo):
            f.write(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + " " + str(demandBw[i]) + " " + str(
                arrivalTime) + " ")
            # print(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + "\n")
            f.write(str(demandRate[i]) + " ")
            for j in fdLst[i]:
                f.write(str(j) + " ")
            f.write("\n")

        # node cap ------------
        for i in range(int(nodeNo)):
            #cu = random.randint(5, 7)
            #f.write(str(cu) + "\n")
            f.write(str(nodeCap) + "\n")
        nodeNo = int(nodeNo)

        # link cap --------------
        for i in range(nodeNo):
            for j in range(nodeNo):
                f.write(str(links[i][j]) + " ")
            f.write("\n")

        # Failures /Cost /Delay on node
        for i in range(nodeNo):
            f.write(str(failure_prob[i]) + " " + str(nodeCost[i]) + " " + str(nodeDelay[i])+ " " + str(nodeType[i]) + "\n")

        # Datasize on function
        for i in range(functionNo):
            f.write(str(data_size[i]) + " ")
        f.write("\n")

        # Function node Delay
        for i in range(functionNo):
            for j in range(nodeNo):
                f.write(str(functionNodeDelay[i][j]) + " ")
            f.write("\n")
        # for id in nx.get_node_attributes(G, "id"):
        #   f.write(id + "\n")
        f.close()

def link_change(topo):
    # generate topo in case of cap change in [4 6 8 10 12 15 18 20 25 30]
    # demands = 20
    # topo GEANT n = 40
    # node_failures = 3

    #topo = "BA"
    #topo = "WA"
    #topo = "GEANT"
    maxDemand = 100
    functionNo = 14
    funcList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
    # print(funcList)
    nodeCap = 25
    linkCap = 100
    demandBw = random.randint(1, 5)
    # functionReq = random.randint(1, 2)
    functionDelay = 1.0  # not used
    arrivalTime = 1.0  # not used
    # demandRate = random.randint(1, 30) #demand SFC delay
    n = 50
    # ba = nx.barabasi_albert_graph(n, edge_attached, 40) # seed=40
    # Random graph G_{n,p} (Erdos-Renyi graph, binomial graph): n node, chooses each of the possible edges with probability p.

    # BA topology
    if topo == "BA":
        randTopo = fnss.erdos_renyi_topology(n, 0.2)
        G = nx.Graph(randTopo)  # ba topology
        topoName = "BA" + str(n) + "_"

    # The Waxman-2 random topology models place n nodes uniformly at random in a rectangular domain.
    if topo == "WA":
        wmtopo = fnss.waxman_1_topology(n, alpha=0.8, beta=0.2, L=1)
        G = nx.Graph(wmtopo)
        topoName = "WAXMAN" + str(n) + "_"
    if topo == "GEANT":
        G = nx.read_graphml("geant.graphml")  # geant topology
        topoName = "GEANT40_"

    print("print nodes: ----------")
    i = 0
    for node in G.nodes(data=True):
        # print(node)
        i = i + 1

    nodeNo = i
    # print("node Number " + str(nodeNo))
    # print("print edges : --------")


    i = 0
    for edge in G.edges(data=True):
        # print(edge)
        i = i + 1
    #  print(edge[0])
    #  print(edge[1])
    edgeNo = i
    # print("edge number = " + str(edgeNo))
    # print("Longitude-----")
    links = [[0 for _ in range(int(nodeNo))] for _ in range(int(nodeNo))]

    linkCap = 1
    for edge in G.edges(data=True):
        x = int(edge[0]) - 1
        y = int(edge[1]) - 1
        links[x][y] = linkCap
        links[y][x] = linkCap

        # f = open('geant.txt', 'w')
        # f = open('waxman50.txt', 'w')
        # Failures /Cost /Delay on node
        numberList = [0, 1]
        failuredNodes = random.choices(numberList, weights=(80, 20),
                                       k=nodeNo)  # probability of failure equals 40% 60-40

        failure_prob = [0 for _ in range(int(nodeNo))]
        nodeCost = [0 for _ in range(int(nodeNo))]
        nodeDelay = [0 for _ in range(int(nodeNo))]
        nodeType = [0 for _ in range(int(nodeNo))]
        for i in range(nodeNo):
            # if failuredNodes[i]:
            #  failure_prob[i] = random.uniform(0.0, 1.0)
            # else:
            #  failure_prob[i] = 0.0

            nodeCost[i] = random.randint(1, 5)
            nodeDelay[i] = random.uniform(0.01, 0.1)
            nodeType[i] = random.choice([2,3])

        # Datasize on function
        data_size = [0 for _ in range(int(functionNo))]
        for i in range(functionNo):
            data_size[i] = random.randint(1, 3)

        # Function node Delay
        functionNodeDelay = [[0 for _ in range(int(nodeNo))] for _ in range(int(functionNo))]
        for i in range(functionNo):
            for j in range(nodeNo):
                functionNodeDelay[i][j] = random.uniform(0.01, 0.1)

    # m function
    functionReq = []
    functionType = [0 for _ in range(int(functionNo))]
    for i in range(functionNo):
        funReq = random.randint(1, 2)
        functionReq.append(funReq)
        functionType[i] = random.choice([2,3])


    # d demand
    src = [0 for _ in range(int(maxDemand))]
    dest = [0 for _ in range(int(maxDemand))]
    demandRate = [0 for _ in range(int(maxDemand))]
    demandBw = [0 for _ in range(int(maxDemand))]
    funNo = [0 for _ in range(int(maxDemand))]
    for i in range(maxDemand):
        funNo[i] = random.randint(2, 5)
    fdLst = []


    for i in range(maxDemand):
        src[i] = random.randint(1, int(nodeNo))
        while True:
            temp = random.randint(1, int(nodeNo))
            if (temp != src[i]):
                source = list(G.nodes)[src[i] - 1]
                # print(source)
                destination = list(G.nodes)[temp - 1]
                #  print(destination)
                if nx.has_path(G, source, destination):
                    dest[i] = temp
                    break
        demandRate[i] = random.randint(1, 30)  # demand SFC delay
        demandBw[i] = random.randint(1, 5)
        fdLst.append(random.sample(funcList, funNo[i]))

    for i in range(maxDemand):
        print(str(src[i]) + ", " + str(dest[i]) + "\n")

    linkcap_list = [10, 20, 30, 40, 50, 60, 80, 100, 150, 200]
    nodecap_list = [5, 8, 10, 12, 15, 18, 20, 25, 30, 50, 80, 100, 150, 200]
    demandNo = maxDemand
    # demandList = [2, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200]
    # demandList = [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000]
    count = 0

    for linkCap in linkcap_list:
        for nodeCap in nodecap_list:
            # print("demandNo = " + str(demandNo))

            fileName = "C:/Users/thiminn/Desktop/back-up_Juillet21/research/Optimal_Model-master/paperFev23/LinkChange/" + topoName + str(count) + "_" + str(
                linkCap) + "_" + str(
                nodeCap) + ".txt";
            count = count + 1;
            f = open(fileName, 'w')
            f.write(str(functionNo) + " " + str(demandNo) + " " + str(nodeNo) + " " + str(edgeNo) + "\n")

            # list of function ----------------
            for i in range(functionNo):
                f.write(str(functionDelay) + " " + str(functionReq[i])+ " " + str(functionType[i]) + ";")
            f.write("\n")

            # list of demand -----------------
            for i in range(demandNo):
                f.write(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + " " + str(demandBw[i]) + " " + str(
                    arrivalTime) + " ")
                # print(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + "\n")
                f.write(str(demandRate[i]) + " ")
                for j in fdLst[i]:
                    f.write(str(j) + " ")
                f.write("\n")

            # node cap ------------
            for i in range(int(nodeNo)):
                #cu = random.randint(5, 7)
                #f.write(str(cu) + "\n")
                f.write(str(nodeCap) + "\n")
            nodeNo = int(nodeNo)

            # link cap --------------
            for i in range(nodeNo):
                for j in range(nodeNo):
                    if links[i][j] == 1:
                        f.write(str(linkCap) + " ")
                    else:
                        f.write("0 ")
                f.write("\n")

            # Failures /Cost /Delay on node
            for i in range(nodeNo):
                f.write(str(failure_prob[i]) + " " + str(nodeCost[i]) + " " + str(nodeDelay[i]) + " " + str(nodeType[i])+ "\n")

            # Datasize on function
            for i in range(functionNo):
                f.write(str(data_size[i]) + " ")
            f.write("\n")

            # Function node Delay
            for i in range(functionNo):
                for j in range(nodeNo):
                    f.write(str(functionNodeDelay[i][j]) + " ")
                f.write("\n")
            # for id in nx.get_node_attributes(G, "id"):
            #   f.write(id + "\n")
            f.close()

def demand_change(topo, n):
    # generate topo in case of cap change in [4 6 8 10 12 15 18 20 25 30]
    # demands = 20
    # topo GEANT n = 40
    # node_failures = 3

    #topo = "BA"
    #topo = "WA"
    #topo = "GEANT"
    maxDemand = 200
    functionNo = 14
    funcList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
    # print(funcList)
    nodeCap = 20
    linkCap = 50
    sameLevelLink = 70
    connectedLink = 100 #between edge and cloud
    demandBw = random.randint(1, 5)
    # functionReq = random.randint(1, 2)
    functionDelay = 1.0  # not used
    arrivalTime = 1.0  # not used
    # demandRate = random.randint(1, 30) #demand SFC delay
    # ba = nx.barabasi_albert_graph(n, edge_attached, 40) # seed=40
    # Random graph G_{n,p} (Erdos-Renyi graph, binomial graph): n node, chooses each of the possible edges with probability p.

    # BA topology
    if topo == "BB":
        barabasi = fnss.barabasi_albert_topology(n, 3, 4)
        G_edge = nx.Graph(barabasi)  # ba topology
        bcube = fnss.bcube_topology(2,4)
        G_cloud =nx.Graph(bcube)
        topoName = "BB" + str(n) + "_"

    # The Waxman-2 random topology models place n nodes uniformly at random in a rectangular domain.
    if topo == "FW":
        waxman = fnss.waxman_1_topology(n, alpha=0.9, beta=0.1)
        G_edge = nx.Graph(waxman)
        fattree = fnss.fat_tree_topology(6)
        G_cloud= nx.Graph(fattree)
        topoName = "FW" + str(n) + "_"
    if topo == "EB":
        endo= fnss.erdos_renyi_topology(n, p=0.2)
        G_edge = nx.Graph(endo)  # ba topology
        bcube = fnss.bcube_topology(2, 4)
        G_cloud = nx.Graph(bcube)
        topoName = "EB" + str(n) + "_"
    if topo == "EW":
        endo = fnss.erdos_renyi_topology(n, p=0.2)
        G_edge = nx.Graph(endo)
        fattree = fnss.fat_tree_topology(6)
        G_cloud = nx.Graph(fattree)
        topoName = "EW" + str(n) + "_"
    if topo == "BF":
        barabasi = fnss.barabasi_albert_topology(n, 3, 4)
        G_edge = nx.Graph(barabasi)  # ba topology
        fattree = fnss.fat_tree_topology(6)
        G_cloud = nx.Graph(fattree)
        topoName = "BF" + str(n) + "_"
    if topo == "WB":
        waxman = fnss.waxman_1_topology(n, alpha=0.9, beta=0.1)
        G_edge = nx.Graph(waxman)
        bcube = fnss.bcube_topology(2, 4)
        G_cloud = nx.Graph(bcube)
        topoName = "WB" + str(n) + "_"



    print("print nodes: ----------")
    i = 0
    for node in G_edge.nodes(data=True):
        i = i + 1

    edgeNodeNo = i
    for node in G_cloud.nodes(data=True):
        i = i + 1
    nodeNo = i
    cloudNodeNo = nodeNo - edgeNodeNo


    i = 0
    for edge in G_edge.edges(data=True):
        i = i + 1
    for edge in G_cloud.edges(data=True):
        # print(edge)
        i = i + 1
    edgeNo = i

    links = [[0 for _ in range(int(nodeNo))] for _ in range(int(nodeNo))]


    for edge in G_edge.edges(data=True):
        x = int(edge[0]) - 1
        y = int(edge[1]) - 1
        links[x][y] = sameLevelLink
        links[y][x] = sameLevelLink

    for edge in G_cloud.edges(data=True):
        x = int(edge[0]) - 1 + edgeNodeNo
        y = int(edge[1]) - 1 + edgeNodeNo
        links[x][y] = sameLevelLink
        links[y][x] = sameLevelLink
    for i in range(edgeNodeNo):
        for j in range(edgeNodeNo, nodeNo):
            rand_bool = random.choices([0, 1], weights=(20, 80))
            print ("random : ", rand_bool)
            if rand_bool[0] == 1:
                print ("[", i, ", ", j, "]")
                links[i][j] = connectedLink
                links[i][j] = connectedLink

    numberList = [0, 1]
    failuredNodes = random.choices(numberList, weights=(80, 20),
                                   k=nodeNo)  # probability of failure equals 40% 60-40

    failure_prob = [0 for _ in range(int(nodeNo))]
    nodeCost = [0 for _ in range(int(nodeNo))]
    nodeDelay = [0 for _ in range(int(nodeNo))]
    nodeType = [0 for _ in range(int(nodeNo))]
    for i in range(nodeNo):
        if failuredNodes[i]:
            failure_prob[i] = random.uniform(0.0, 1.0)
        else:
            failure_prob[i] = 0.0
        nodeCost[i] = random.randint(1, 5)
        nodeDelay[i] = random.uniform(0.01, 0.1)
    for i in range(edgeNodeNo):
        nodeType[i] = 2
    for i in range(edgeNodeNo, nodeNo):
        nodeType[i] = 3

    # Datasize on function
    data_size = [0 for _ in range(int(functionNo))]
    for i in range(functionNo):
        data_size[i] = random.randint(1, 3)

    # Function node Delay
    functionNodeDelay = [[0 for _ in range(int(nodeNo))] for _ in range(int(functionNo))]
    for i in range(functionNo):
        for j in range(nodeNo):
            functionNodeDelay[i][j] = random.uniform(0.05, 0.5)

    # m function
    functionReq = []
    functionType = [0 for _ in range(int(functionNo))]

    edgeFunc = int(functionNo/2)
    for i in range(edgeFunc):
        functionType[i] = 2
    for i in range(edgeFunc, functionNo):
        functionType[i] = 3

    for i in range(functionNo):
        funReq = random.randint(1, 2)
        functionReq.append(funReq)


    # d demand
    src = [0 for _ in range(int(maxDemand))]
    dest = [0 for _ in range(int(maxDemand))]
    demandRate = [0 for _ in range(int(maxDemand))]
    demandBw = [0 for _ in range(int(maxDemand))]
    funNo = [0 for _ in range(int(maxDemand))]
    for i in range(maxDemand):
        funNo[i] = random.randint(3, 6)
    fdLst = []


    for i in range(maxDemand):
        src[i] = random.randint(1, int(edgeNodeNo))
        dest[i] = random.randint(int(edgeNodeNo) + 1, int(nodeNo))
        # while True:
        #     temp = random.randint(1, int(edgeNodeNo))
        #     if temp != src[i]:
        #         # source = list(G.nodes)[src[i] - 1]
        #         # # print(source)
        #         # destination = list(G.nodes)[temp - 1]
        #         # #  print(destination)
        #         # if nx.has_path(G, source, destination):
        #         #     dest[i] = temp
        #         dest[i] = temp
        #         break
        demandRate[i] = random.randint(5, 30)  # demand SFC delay
        demandBw[i] = random.randint(1, 5)
        edgeFuncNo = int(funNo[i]/2)
        list = []
        count = 0
        while count < edgeFuncNo:
            randNumber = random.randint(1, edgeFunc)
            if randNumber in list:
                continue
            else:
                list.append(randNumber)
                count += 1
        count = 0
        while count < (funNo[i] - edgeFuncNo):
            randNumber = random.randint(edgeFunc, functionNo)
            if randNumber in list:
                continue
            else:
                list.append(randNumber)
                count += 1
        fdLst.append(list)

    # for i in range(maxDemand):
    #     print(str(src[i]) + ", " + str(dest[i]) + "\n")


    demandList = [2, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200]
    # demandList = [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000]
    count = 0

    for demandNo in demandList:
        # print("demandNo = " + str(demandNo))

        fileName = "./output/" + topoName + str(
            count) + "_" + str(demandNo) + ".txt";
        count = count + 1;
        f = open(fileName, 'w')
        f.write(str(functionNo) + " " + str(demandNo) + " " + str(nodeNo) + " " + str(edgeNo) + "\n")

        # list of function ----------------
        for i in range(functionNo):
            f.write(str(functionDelay) + " " + str(functionReq[i]) + " " + str(functionType[i]) + ";")
        f.write("\n")

        # list of demand -----------------
        for i in range(demandNo):
            f.write(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + " " + str(demandBw[i]) + " " + str(arrivalTime) + " ")
            # print(str(i + 1) + " " + str(src[i]) + " " + str(dest[i]) + "\n")
            f.write(str(demandRate[i]) + " ")
            for j in fdLst[i]:
                f.write(str(j) + " ")
            f.write("\n")

        # node cap ------------
        for i in range(int(nodeNo)):
            # cu = random.randint(5, 7)
            # f.write(str(cu) + "\n")
            f.write(str(nodeCap) + "\n")
        nodeNo = int(nodeNo)

        # link cap --------------
        for i in range(nodeNo):
            for j in range(nodeNo):
                f.write(str(links[i][j]) + " ")
                # if links[i][j] == 1:
                #     f.write(str(linkCap) + " ")
                # else:
                #     f.write("0 ")
            f.write("\n")

        # Failures /Cost /Delay on node
        for i in range(nodeNo):
            f.write(
                str(failure_prob[i]) + " " + str(nodeCost[i]) + " " + str(nodeDelay[i]) + " " + str(nodeType[i]) + "\n")

        # Datasize on function
        for i in range(functionNo):
            f.write(str(data_size[i]) + " ")
        f.write("\n")

        # Function node Delay
        for i in range(functionNo):
            for j in range(nodeNo):
                f.write(str(functionNodeDelay[i][j]) + " ")
            f.write("\n")
        # for id in nx.get_node_attributes(G, "id"):
        #   f.write(id + "\n")
        f.close()
