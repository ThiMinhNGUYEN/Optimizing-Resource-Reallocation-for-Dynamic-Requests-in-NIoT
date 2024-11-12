# 2021-04-05: fixed getPath() in floywarshall
# 2020-12-29: nfv_drl2() sacagent ok, retrain ok
# 2020-12-16: branch 2 obs la demand thay doi
# 2020-11-27: state, action, env (input + output)
# Python ≥ 3.5 is required
# Scikit-Learn ≥ 0.20 is required
import time
import fnss
import codecs
import sys
import tempfile

import gym
import sklearn
import tf_agents
import tensorflow as tf
from tensorflow import keras
import numpy as np
import os
from docplex.mp.model import Model
from tensorflow.python.platform import build_info as tf_build_info
import matplotlib as mpl
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from tf_agents.environments import suite_gym
from tf_agents.environments.wrappers import ActionRepeat
import tf_agents.environments.wrappers
from functools import partial
from gym.wrappers import TimeLimit
#from tf_agents.environments.atari_preprocessing import AtariPreprocessing
from tf_agents.environments.atari_wrappers import FrameStack4
from tf_agents.networks.q_network import QNetwork
from tf_agents.environments.tf_py_environment import TFPyEnvironment
from tf_agents.agents.dqn.dqn_agent import DqnAgent
from tf_agents.replay_buffers import tf_uniform_replay_buffer
from tf_agents.metrics import tf_metrics
from tf_agents.eval.metric_utils import log_metrics
import logging
from tf_agents.drivers.dynamic_step_driver import DynamicStepDriver
from tf_agents.policies.random_tf_policy import RandomTFPolicy
from tf_agents.trajectories.trajectory import to_transition
from tf_agents.trajectories import trajectory
from tf_agents.utils.common import function
from tf_agents.utils import common
import PIL
from collections import deque
from tf_agents.metrics import tf_py_metric
from tf_agents.metrics import py_metric
from tf_agents.drivers import py_driver
from tf_agents.drivers import dynamic_episode_driver
# from tf_agents.agents.sac import tanh_normal_projection_network
from tf_agents.agents.ddpg import critic_network
from tf_agents.agents.sac import sac_agent
from tf_agents.drivers import dynamic_step_driver
from tf_agents.environments import suite_mujoco
from tf_agents.environments import tf_py_environment
from tf_agents.eval import metric_utils
from tf_agents.metrics import tf_metrics
from tf_agents.networks import actor_distribution_network
from tf_agents.networks import normal_projection_network
import networkx as nx
import discrete_sac_agent
import discrete_sac_critic_network
from dijkstar import Graph, find_path
from docplex.mp.conflict_refiner import ConflictRefiner
from docplex.mp.relaxer import Relaxer
from tf_agents.policies import policy_saver
import functools

# import pyvirtualdisplay

# to make this notebook's output stable across runs
np.random.seed(42)
tf.random.set_seed(42)
max_episode_steps = 27000  # <=> 108k ALE frames since 1 step = 4 frames

# To plot pretty figures
# %matplotlib inline

mpl.rc('axes', labelsize=14)
mpl.rc('xtick', labelsize=12)
mpl.rc('ytick', labelsize=12)

# Where to save the figures
PROJECT_ROOT_DIR = "."
CHAPTER_ID = "rl"
IMAGES_PATH = os.path.join(PROJECT_ROOT_DIR, "images", CHAPTER_ID)
os.makedirs(IMAGES_PATH, exist_ok=True)


def normal_projection_net(action_spec,
                          init_action_stddev=0.35,
                          init_means_output_factor=0.1):
    del init_action_stddev
    return normal_projection_network.NormalProjectionNetwork(
        action_spec,
        mean_transform=None,
        state_dependent_std=True,
        init_means_output_factor=init_means_output_factor,
        std_transform=sac_agent.std_clip_transform,
        scale_distribution=True)


# display = pyvirtualdisplay.Display(visible=0, size=(1400, 900)).start()


def plot_fig(xdata, ydata, xlabel, ylabel):
    plt.plot(xdata, ydata)
    plt.ylabel(ylabel)
    plt.xlabel(xlabel)
    # plt.ylim(top=250)
    plt.autoscale()
    plt.show()


def save_fig(xdata, ydata, xlabel, ylabel, filename_output):
    plt.plot(xdata, ydata)
    plt.ylabel(ylabel)
    plt.xlabel(xlabel)
    plt.tight_layout()
    filename_output = filename_output.replace("txt", "png")
    plt.savefig(filename_output, dpi=300)
    plt.close()

def save_data(xdata, ydata, filename):
    print(xdata)
    print(ydata)
    fout = codecs.open(filename, "w", encoding='utf-8')
    fout.write("#write to output")

# The most common metric used to evaluate a policy is the average return.
# The return is the sum of rewards obtained while running a policy in an environment for an episode.
# Several episodes are run, creating an average return.
# The following function computes the average return of a policy,
# given the policy, environment, and a number of episodes.
def compute_avg_return(environment, policy, num_episodes=1000):
    total_return = 0.0
    for _ in range(num_episodes):

        time_step = environment.reset()  # state
        episode_return = 0.0

        while not time_step.is_last():
            # using the trained policy to compute an action for the environment state in time_step
            action_step = policy.action(time_step)
            # print(action_step.action)
            time_step = environment.step(action_step.action)
            print("time_step.reward = ", time_step.reward)
            episode_return += time_step.reward
        total_return += episode_return
    avg_return = -total_return / num_episodes
    return avg_return.numpy()[0]

def collect_reward(environment, policy):
    time_step = environment.reset()  # state
    while not time_step.is_last():
            # using the trained policy to compute an action for the environment state in time_step
        action_step = policy.action(time_step)
            # print(action_step.action)
        time_step = environment.step(action_step.action)

    print("time_step.reward = ", time_step.reward)
    reward_step = time_step.reward
    return reward_step.numpy()[0]

class MyEnvironment2(tf_agents.environments.py_environment.PyEnvironment):
    MAXN = 5  # number of nodes
    MAXE = 20  # number of edges
    MAXD = 50  # number of demands
    MAXMOVE = 100  # number of changes in a weight vector

    #  if the discount factor is close to 1, then
    #  rewards far into the future will count almost as much as immediate rewards.
    def __init__(self, capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, functions, demands,
                 traffic_bandwidth, discount=0.989):

        # ### structure #######
        # nodes : a list of [cap_node, cost_node, type_node, delay_node, [function_delay_node]]
        #                                   with [function_delay_node]= [delay_f1, delay_f2,...]
        # links: a list of [cap_link]
        # functions : a list of [req_f, type_f]
        # demands : a list of [source, dest, bandwidth, sfc delay, [f1, f2, ...]]
        # traffic_bandwidth : a list of 100 traffic bandwidth for each demand ;
        #                         with traffic_bandwidth[d] = [4,4,4,3,3,1,1,1 .....]
        # ######################
        super().__init__()
        # print("MyEnvironment2.__init__()")
        self.V = capnodes  # [c(v1), c(v2), ...]
        self.node_cost = nodeCost
        self.node_type = nodeType
        self.node_delay = nodeDelay
        self.node_function_delay = delayNodeOnFunction

        self.E = links  # e[vi,vj], e.g., e[0,1] = 4

        self.F = functions[:, 0]  # ([f1, f2,...]) resource required for one unit traffic of function fi
        self.function_type = functions[:, 1]

        self.D = demands  # [source, dest, bandwidth, sfc delay, [f1, f2, ...]]

        self.bandwidth = traffic_bandwidth

        self.reward = 0
        self.done = 0
        self.info = []

        self.ne = np.count_nonzero(self.E)
        self.nv = len(self.V)
        self.nd = len(self.D)
        self.nf = len(self.F)
        self.nBand = len(self.bandwidth[0])
        self.nmaxtraffic = 50
        self.W = self.getLinkWeightSystem()
        self.evmap = []
        self.MAXMOVE = 100  # np.int(self.ne/1)
        self.movecount = 0
        self.lastEnergySave = 0

        self.path = self.floydWarshall(self.W, self.nv)

        self.prev_x = np.zeros(shape=(self.nv, self.nd, self.nf), dtype=np.int32)
        #can tinh prev_x
        maxBw = []
        for di in range(self.nd):
            maxBw.append(np.max(self.bandwidth[di]))
        self.x, self.z, self.vnf_cost, self.vnf_cost_demand = self.optimal_solution(maxBw)
        if self.vnf_cost == 0:
            print("Can not find the solution")
            assert()

        self.prev_x = self.x
        self.prev_vnfcost = self.vnf_cost
        self.prev_vnfcost_demand = self.vnf_cost_demand
        self.prev_cost = 0


        # Action [demand d, ith VNF, node] = new node
        # self._action_spec = tf_agents.specs.BoundedArraySpec(
        #   shape=(self.nd*self.nf,), dtype=np.float32, name="action", minimum=0,
        #  maximum=self.nv-1)
        self._action_spec = tf_agents.specs.BoundedArraySpec(
            shape=(), dtype=np.int32, name="action", minimum=0,
            maximum=self.nBand - 1)

        # State [demand d, ith VNF, node, bandwidth] = new bandwidth
        self._state = np.zeros(shape=(self.nBand,), dtype=np.float32)  # current time
        self._observation_spec = tf_agents.specs.BoundedArraySpec(
            shape=(self.nBand,), dtype=np.float32, name="observation", minimum=0)  # total cost at t
        self.discount = discount
        self._finish = False
        print("state = ", self._state[0])
        self._state[0] = self.vnf_cost * self.nBand
        self.reward = 0
        self.prev_action = 0


    def action_spec(self):
        return self._action_spec

    def observation_spec(self):
        return self._observation_spec

    def _reset(self):
        # print("MyEnvironment2._reset(self)")
        self.movecount = 0
        self._state = np.zeros(shape=(self.nBand,), dtype=np.float32)
        self._state[0] = self.vnf_cost * self.nBand
        self.prev_x = self.x
        self.prev_z = self.z
        self.prev_action = 0
        self.prev_vnfcost = self.vnf_cost
        self.prev_vnfcost_demand = self.vnf_cost_demand
        self.prev_cost = 0
        self.reward = 0
        obs = self._state
        self._finish = False
        return tf_agents.trajectories.time_step.restart(obs)

    def _step(self, action):

        if self._finish:
            return self.reset()
        migrationCost = 0
        current_time = self.prev_action
        newbw = []
        pv_bw = []
        if action <= current_time:
            print("finishhh.... action = ", action, " with current time =", current_time, "and state = ", self._state)
            self._finish = True
        else:
            print("continuing.... action = ", action, " with current time =", current_time, "and state = ", self._state)
            for di in range(self.nd):
                newbw.append(self.D[di][2])
            for di in range(self.nd):
                pv_bw.append(self.D[di][2])
            for di in range(self.nd): #calculate max bandwith from pv_action to action : pv_bw
                maxb = 0
                for i in range(self.prev_action, action-1):
                    if self.bandwidth[di][i] > maxb:
                        maxb = self.bandwidth[di][i]
                pv_bw[di] = maxb
            for di in range(self.nd):#calculate max bandwith from action to end : newbw
                # placement:
                newtime = action
                newarr = self.bandwidth[di][newtime:]
                newbw[di] = np.max(newarr)
                #print("bandwidth = ", self.bandwidth, ", action= ", action)
            print ("newbw = ", newbw)

            pv_x,pv_z, pv_vnf_cost, pv_vnf_cost_demand = self.optimal_solution(pv_bw)
            x, z, vnf_cost, vnf_cost_demand = self.optimal_solution(newbw)

            if vnf_cost == 0:
                self._finish = True

            if np.all(self.prev_x):
                migrationCost = 0
            else:
                # calculate migrationCost:
                for d in range(self.nd):
                    for i in range(self.nf):
                        for v in range(self.nv):
                            if self.prev_x[v, d, i] > 0 & self.prev_x[v, d, i] != self.x[v, d, i]:
                                #print("v=", v, ",d = ", d, ", i = ", i)
                                for v_new in range(self.nv):
                                    if self.x[v_new, d, i] > 0:
                                        #print("v_new=", v_new, ",d = ", d, ", i = ", i)
                                        # migration cost from v to v_new
                                        sp = self.getSP(self.path, v, v_new)
                                        migrationCost += (len(sp) - 1) * newbw[d]* 0.02

            self.movecount = self.movecount + 1

        if not self._finish :
            #re calculate vnf_cost from current_time to action:
            old_cost = 0
            print ("current_time ",current_time, " , action = ", action, "migration_cost =", migrationCost)
            total_cost_prev_action = self.prev_cost + pv_vnf_cost * (action - self.prev_action) + migrationCost
            total_cost = total_cost_prev_action + vnf_cost * (self.nBand - action)
            print("migrationCost = ", migrationCost, "vnf_cost = ", vnf_cost, "total_cost = ", total_cost)

            self._state[action] = total_cost  # luu lai cost thoi diem t

            print(self._state)
            self.reward = self.reward + self._state[current_time] - total_cost
            print("update _state[action] =", self._state[action], "===> step reward =  ", self.reward)
            self.prev_x = x
            self.prev_z = z
            self.prev_vnfcost = vnf_cost
            self.prev_vnfcost_demand = vnf_cost_demand
            self.prev_action = action
            self.prev_cost = total_cost_prev_action
            self.lastEnergySave = self._state[current_time] - self._state[action]
        else:
            print("reward = ", self.reward)

        # Compute a reward after applying the action in the enviroment
        if self._finish:
            self.movecount = 0
            return tf_agents.trajectories.time_step.termination(self._state, self.reward)
        else:
            return tf_agents.trajectories.time_step.transition(self._state, self.reward, self.discount)

    def getEnergySave(self):
        return self.lastEnergySave

    def fulfilDemands(self, path, state, linkResource, nodeResource):
        spList = []
        vnfCost = 0
        failedDemand = 0
        for di in range(self.nd):
            v1 = self.D[di][0]
            failed = 0
            for f in range(len(self.D[di][5])):
                (v, bw) = np.unravel_index(np.argmax(self._state[di][f], axis=None), self._state[di][f].shape)
                if bw > 0:
                    vnfCost += bw
                    if v1 == v:
                        nodeResource[v] -= bw  # * costvnf[f]
                    elif path[v1][v] >= 0:  # cannot route sfc
                        sp = self.getSP(path, v1, v)
                        nodeResource[v] -= bw  # * costvnf[f]
                        for i in range(len(sp) - 1):
                            linkResource[sp[i]][sp[i + 1]] -= bw
                        v1 = v
                    else:
                        failed = 1
                        v1 = v
                else:
                    failed = 1
                    break
            failedDemand += failed

        # print(spList)
        return failedDemand, linkResource, nodeResource, vnfCost

    def optimal_solution(self, newbw):
        m = Model(name="MILP-VNF")
        id_x = [(v, d, i) for v in range(self.nv) for d in range(self.nd) for i in range(self.nf)]

        x = m.binary_var_dict(id_x, lb=0, ub=1, name="x")

        id_z = [d for d in range(self.nd)]
        z = m.binary_var_dict(id_z, lb=0, ub=1, name="z")

        #        z = m.binary_var(d, 0, 1, name=f"z_{d}")
        # objective
        print(self.D)

        uc = 0
        ud = self.nd
        for d in range(self.nd):
            funct = self.D[d][4]
            for i in range(len(funct)):
                for v in range(self.nv):
                    uc += x[v, d, i] * self.F[funct[i] - 1] * self.node_cost[v] * newbw[d]
            ud += -z[d]
        alpha = 0.002
        beta = 1
        m.minimize(alpha * uc + beta * ud)

        for v in range(self.nv):
            constraint5 = 0
            for d in range(self.nd):
                funct = self.D[d][4]
                for i in range(len(funct), self.nf):
                    m.add_constraint(x[v, d, i] == 0, ctname="eq0")
        # eq 1: (5)
        for v in range(self.nv):
            constraint5 = 0
            for d in range(self.nd):
                funct = self.D[d][4]
                for i in range(len(funct)):
                    constraint5 += newbw[d] * x[v, d, i] * self.F[funct[i] - 1]
            m.add_constraint(constraint5 <= self.V[v], ctname="eq1")

        # eg 3:(7)
        for d in range(self.nd):
            funct = self.D[d][4]
            for i in range(len(funct)):
                m.add_constraint(m.sum(x[v, d, i] for v in range(self.nv)) <= 1, ctname="eq3")

        # eg 4:(8)
        for d in range(self.nd):
            m.add_constraint(m.sum(x[v, d, i] for v in range(self.nv) for i in range(self.nf))
                             == z[d] * len(self.D[d][4]), ctname="eq4")

        # eg 5:(9)
        for d in range(self.nd):
            funct = self.D[d][4]
            for i in range(len(funct)):
                for v in range(self.nv):
                    if self.function_type[funct[i] - 1] != self.node_type[v]:
                        m.add_constraint(x[v, d, i] == 0, ctname="eq5")

        m.export_as_lp(basename="nfv_opt", path="report/")
        # solve:
        tms = m.solve(TimeLimit=100, log_output=True)
        #assert tms
        #tms.display()
        vnf_cost = 0
        vnf_cost_demand = []
        x_val = np.zeros(shape=(self.nv, self.nd, self.nf), dtype=np.int32)
        z_val = np.zeros(shape=(self.nd,), dtype=np.int32)
        if tms and tms.is_feasible_solution:
            print ("found solution")
            print(tms.objective_value)
            # calculate deployment cost:
            for d in range(self.nd):
                temp_cost = 0
                funct = self.D[d][4]
                if tms.get_value(z[d]) > 0:
                    #print(d)
                    z_val[d] = 1
                    for v in range(self.nv):
                        for i in range(len(funct)):
                            if tms.get_value(x[v, d, i]) > 0:
                                x_val[v, d, i] = 1
                                #print(v)
                                #print(tms.get_value(x[v, d, i]))
                                c = tms.get_value(z[d]) * tms.get_value(x[v, d, i]) * self.F[
                                funct[i] - 1] * self.node_cost[v]
                                temp_cost += c
                                #print(c)
                                vnf_cost += c * newbw[d] * alpha
                vnf_cost_demand.append(temp_cost)
        else:
            print ("infeasible ; need to relax")
            # cr = ConflictRefiner()
            # crr = cr.refine_conflict(m, display=True)
            rx = Relaxer()
            rs = rx.relax(m)
            # rx.print_information()
            rs.display()
            for d in range(self.nd):
                temp_cost = 0
                funct = self.D[d][4]
                if rs.get_value(z[d]) > 0:
                    #print(d)
                    z_val[d] = 1
                    for v in range(self.nv):
                        for i in range(len(funct)):
                            if rs.get_value(x[v, d, i]) > 0:
                                x_val[v, d, i] = 1
                                #print(v)
                                #print(rs.get_value(x[v, d, i]))
                                c = rs.get_value(z[d]) * rs.get_value(x[v, d, i]) * self.F[
                                funct[i] - 1] * self.node_cost[v]
                                temp_cost += c
                                #print(c)
                                vnf_cost += c * newbw[d]* alpha
                vnf_cost_demand.append(temp_cost)

        return x_val, z_val, vnf_cost, vnf_cost_demand


    def getLinkWeightSystem(self):
        obs = np.full((self.nv, self.nv), 0, dtype=np.float32)
        ei = 0
        for i in range(self.nv):
            for j in range(self.nv):
                if self.E[i][j] > 0:  # if (i->j) exists
                    obs[i][j] = 1
                    ei = ei + 1
        return obs

    # Recursive Function to print path of given vertex u from source vertex v
    def getSP(self, path, u, v):
        sp = [v]
        k = path[u][v]
        while k != u:
            sp = [k] + sp
            v = k
            k = path[u][v]
        sp = [u] + sp
        # if path[u][v] == u:
        #    return
        # if path[u][v] >= 0:
        #    sp = self.getSP(path, path[u][v], v)
        #    sp.append(path[u][v])
        return sp

    # sp = [source, ..., dest]
    def vnfAllocation(self, demandTraffic, sfc_delay, sfc, sp, linkResource, nodeResource):
        omega = [None] * (len(sfc) + 1)  # omega[i]=j: j la node dau tien co the cap function sfc[i]
        # print("sfc: " + ' '.join([str(elem) for elem in sfc])  + "\n")
        # print("sp: " + ' '.join([str(elem) for elem in sp])  + "\n")
        # print("self.F: " + ' '.join([str(elem) for elem in self.F]) + "\n")
        sfc_delay = 0

        for i in range(len(sfc)):
            done = False
            for j in range(len(sp)):  # list of nodes on the path from the source to the dest
                # print(str(nodeResource[sp[j]]) + "\n");
                # print(str(sfc[i]) + "\n");
                # print(str(self.F[sfc[i]]) + "\n");
                if (nodeResource[sp[j]] >= self.F[sfc[i]] * demandTraffic) and not done \
                        and (i == 0 or (j >= omega[i - 1])):
                    omega[i] = j  # function sfc[i] duoc cung cap boi node j
                    done = True  # node j phai xet tu node cap function sfc[i-1] (j>=omega[i-1])
                    # hoac i la function 0
            if not done:  # neu duyet qua tat ca node on path ma ko node nao cap duoc sfc[i]
                if i == 0:  # neu la function
                    omega[i] = len(sp) - 1  # neu ko node nao du tai nguyen, thi gan node cuoi cung trong sp
                else:
                    omega[i] = len(sp) - 1  # node cap function sfc[i] = node cap function sfc[i-1]
        omega[len(sfc)] = len(sp) - 1  # them 1 function (vao cuoi sfc) do dest cap

        #  Duyet nguoc danh sach sfc tu cuoi ve
        for i in range(len(sfc) - 1, -1, -1):
            jmin = omega[i]  # chi so node (thu tu node trong sp) co it tai nguyen nhat co the cap cho sfc[i]
            for j in range(omega[i], omega[i + 1] + 1):
                # (1) node sp[j] va node sp[jmin] deu du resource cap cho function sfc[i]
                # va resouce cua node sp[j] nho hon node sp[jmin]
                if (nodeResource[sp[jmin]] >= nodeResource[sp[j]] >= self.F[sfc[i]] * demandTraffic):
                    # or (nodeResource[sp[jmin]] < self.F[sfc[i]] * demandTraffic <= nodeResource[sp[j]]):
                    jmin = j
            omega[i] = jmin
            nodeResource[sp[omega[i]]] = nodeResource[sp[omega[i]]] - self.F[sfc[i]] * demandTraffic
            sfc_delay += self.node_delay_vnf[sp[omega[i]]]  # vnf processing delay

        # for i in range(len(sfc)):
        #    nodeResource[sp[omega[i]]] = nodeResource[sp[omega[i]]] - self.F[i] * demandTraffic

        for i in range(len(sp) - 1):
            linkResource[sp[i]][sp[i + 1]] -= demandTraffic

        # routing delay
        for i in range(len(sp) - 1):
            sfc_delay += self.node_delay_routing[sp[i]]

        return linkResource, nodeResource

    # Recursive Function to print path of given vertex u from source vertex v
    def printPath(self, path, u, v):
        if path[u][v] == u:
            return
        self.printPath(path, u, path[u][v])
        print(path[u][v], end=' ')

    # Function to run Floyd-Warshall algorithm
    def floydWarshall(self, adjMatrix, N):
        # cost and parent matrix stores shortest-path (shortest-cost/shortest route) information

        # initially cost would be same as weight of the edge
        cost = adjMatrix.copy()
        path = [[None for x in range(N)] for y in range(N)]

        # initialize cost and parent
        for u in range(N):
            for v in range(N):
                if u == v:
                    path[u][v] = -2
                elif cost[u][v] > 0:
                    path[u][v] = u
                else:
                    path[u][v] = -1

        # run Floyd-Warshall
        for k in range(N):
            for u in range(N):
                for v in range(N):
                    # If vertex k is on the shortest path from u to v,
                    # then update the value of cost[u][v], path[u][v]
                    if cost[u][k] > 0 and cost[k][v] > 0:
                        if cost[u][v] > 0:
                            if cost[u][k] + cost[k][v] < cost[u][v]:
                                cost[u][v] = cost[u][k] + cost[k][v]
                                path[u][v] = path[k][v]
                        else:
                            cost[u][v] = cost[u][k] + cost[k][v]
                            path[u][v] = path[k][v]

                # if diagonal elements become negative, the
                # graph contains a negative weight cycle
                if cost[u][u] < 0:
                    print("Negative Weight Cycle Found")
                    return
        return path

    def getNumberOfLinks(self):
        return self.ne

    def updateDemand(self, demands):
        self.D = demands

# Create a simple custom observer that counts and
# displays the number of times it is called
# (except when it is passed a trajectory that represents the boundary between two episodes,
# as this does not count as a step):
class ShowProgress:
    def __init__(self, total):
        self.counter = 0
        self.total = total

    def __call__(self, trajectory):
        if not trajectory.is_boundary():
            self.counter += 1
        # if self.counter % 100 == 0:
        # print("\r{}/{}".format(self.counter, self.total), end="")
        print("\r{}/{}".format(self.counter, self.total), end="")


def collect_step(environment, policy, buffer):
    time_step = environment.current_time_step()

    ########
    if time_step.is_last():
        time_step = environment.reset()

    action_step = policy.action(time_step)
    next_time_step = environment.step(action_step.action)
    traj = trajectory.from_transition(time_step, action_step, next_time_step)
    # Add trajectory to the replay buffer
    buffer.add_batch(traj)


def collect_data(env, policy, buffer, steps):
    for _ in range(steps):
        collect_step(env, policy, buffer)


def nfv_drl2(capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, functions, demands, traffic_bandwidth):
    #### structure #######
    # nodes : a list of [cap_node, cost_node, type_node, delay_node, [function_delay_node] = [delay_f1, delay_f2,...]
    # links: a list of [cap_link]
    # functions : a list of [req_f, type_f]
    # demands : a list of [source, dest, bandwidth, sfc delay, [f1, f2, ...]]
    # traffic_bandwidth : a list of 100 traffic bandwidth for each demand ; traffic_bandwidth[d] = [4,4,4,3,3,1,1,1 .....]
    #######################

    assert sys.version_info >= (3, 5)
    assert sklearn.__version__ >= "0.20"
    assert tf.__version__ >= "2.1.0"
    print(tf.__version__)


    print("nfv_drl2: start")
    time1 = time.time()

    # We want to parallelize computation,
    # so we should convert the PyEnvironment into a TensorflowEnvironment
    # which uses tensors instead of arrays.
    train_py_env = MyEnvironment2(capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, functions, demands, traffic_bandwidth)
    train_env = TFPyEnvironment(train_py_env)
    print('Observation Spec:')
    print(train_env.time_step_spec().observation)
    print('Reward Spec:')
    print(train_env.time_step_spec().reward)
    print('Action Spec:')
    print(train_env.action_spec())
    eval_py_env = MyEnvironment2(capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, functions, demands, traffic_bandwidth)
    eval_env = TFPyEnvironment(eval_py_env)

    # Hyper parameters
    num_iterations =5000  # @param {type:"integer"}

    initial_collect_steps = 1000  # @param {type:"integer"}
    collect_steps_per_iteration = 8  # @param {type:"integer"}
    replay_buffer_max_length = 128  # 100000@param {type:"integer"}

    batch_size = 16  # @param {type:"integer"}
    learning_rate = 0.005#1e-3  # @param {type:"number"}
    log_interval = 1  # @param {type:"integer"}

    num_eval_episodes = 1  # @param {type:"integer"}
    eval_interval = 1  # @param {type:"integer"}

    # Creating the DQN
    # Create the Q-Network:
    # preprocessing_layer = keras.layers.Lambda(lambda obs: tf.cast(obs, np.float32))
    # the network applies three convolutional layers:
    # the first has 32 8 × 8 filters and uses a stride of 4,
    # the second has 64 4 × 4 filters and a stride of 2,
    # and the third has 64 3 × 3 filters and a strid                                                       e of 1.
    # conv_layer_params = [(32, (8, 8), 4), (64, (4, 4), 2), (64, (3, 3), 1)]
    # Lastly, it applies a dense layer with 512 units,
    # followed by a dense output layer with 4 units,
    # one per Q-Value to output (i.e., one per action)
    fc_layer_params = (32, 32)  # (32,32) the number and size of the model's hidden layers

    q_net = QNetwork(
        train_env.observation_spec(),
        train_env.action_spec(),
        # preprocessing_layers=preprocessing_layer,
        # conv_layer_params=conv_layer_params,
        fc_layer_params=fc_layer_params
    )

    # Create the DQN Agent:
    # see TF-agents issue #113
    optimizer = tf.compat.v1.train.AdamOptimizer(learning_rate=learning_rate)
    loss_fn = keras.losses.Huber(reduction="none")
    train_step_counter = tf.Variable(0)

    # CHECK: run a training step every 2 collect steps
    update_period = 4

    starter_learning_rate = 0.5 #0.75
    end_learning_rate = 0.005#0.001
    decay_steps = 5000#1000
    epsilon_fn = keras.optimizers.schedules.PolynomialDecay(
        starter_learning_rate,
        decay_steps,
        end_learning_rate,
        power=0.5)

    agent = DqnAgent(train_env.time_step_spec(),
                     train_env.action_spec(),
                     q_network=q_net,
                     optimizer=optimizer,
                     target_update_period=2000,  # 2000 <=> 32,000 ALE frames
                     # td_errors_loss_fn=loss_fn,
                     td_errors_loss_fn=common.element_wise_squared_loss,
                     # n_step_update=collect_steps_per_iteration,
                     gamma=0.989,  # discount factor
                     train_step_counter=train_step_counter,
                     # _counter=train_step,
                     # emit_log_probability=True,
                     #epsilon_greedy=epsilon_fn)
                     epsilon_greedy=lambda: epsilon_fn(train_step_counter))

    agent.initialize()
    eval_policy = agent.policy
    collect_policy = agent.collect_policy
    random_policy = RandomTFPolicy(train_env.time_step_spec(), train_env.action_spec())

    # Create the replay buffer
    # The replay buffer keeps track of data collected from the environment.
    # The most common is tf_agents.replay_buffers.tf_uniform_replay_buffer.TFUniformReplayBuffer.
    # The constructor requires the specs for the data it will be collecting.
    # This is available from the agent using the collect_data_spec method.
    # The batch size and maximum buffer length are also required.
    # For most agents, collect_data_spec is a named tuple called Trajectory,
    # containing the specs for observations, actions, rewards, and other items.
    replay_buffer = tf_uniform_replay_buffer.TFUniformReplayBuffer(
        data_spec=agent.collect_data_spec,
        batch_size=train_env.batch_size,
        max_length=replay_buffer_max_length)  # max_length=1000000)

    # print("tf_env.batch_size=", tf_env.batch_size)
    # replay_buffer_observer = replay_buffer.add_batch

    # Collect the initial experiences, before training:
    # Execute the random policy in the environment for a few steps,
    # recording the data in the replay buffer.
    # The replay buffer is a collection of Trajectories.
    collect_data(train_env, random_policy, replay_buffer, initial_collect_steps)

    # Now let's create the dataset.
    # The agent needs access to the replay buffer.
    # This is provided by creating an iterable tf.data.Dataset pipeline which will feed data to the agent.
    # Each row of the replay buffer only stores a single observation step.
    # But since the DQN Agent needs both the current and next observation to compute the loss,
    # the dataset pipeline will sample two adjacent rows for each item in the batch (num_steps=2).
    # This dataset is also optimized by running parallel calls and prefetching data.
    dataset = replay_buffer.as_dataset(
        sample_batch_size=batch_size,
        num_steps=2,
        num_parallel_calls=3).prefetch(3)

    # Convert the main functions to TF Functions for better performance:
    agent.train = function(agent.train)
    iterator = iter(dataset)

    # Reset the train step
    agent.train_step_counter.assign(0)

    # Dataset 1: Evaluate the agent's policy once before training.
    avg_return = compute_avg_return(eval_env, agent.policy, num_eval_episodes)
    returns = [avg_return]
    reward_step = collect_reward(eval_env, agent.policy)
    rewards = [reward_step]

    for _ in range(num_iterations):
        # Collect a few steps using collect_policy and save to the replay buffer.
        collect_data(train_env, agent.collect_policy, replay_buffer, collect_steps_per_iteration)
        # Sample a batch of data from the buffer and update the agent's network.
        experience, unused_info = next(iterator)
        train_loss = agent.train(experience).loss
        step = agent.train_step_counter.numpy()
        reward_step = collect_reward(eval_env, agent.policy)
        rewards.append(reward_step)
        if step % log_interval == 0:
            print('step = {0}: loss = {1}'.format(step, train_loss))
        if step % eval_interval == 0:
            avg_return = compute_avg_return(eval_env, agent.policy, num_eval_episodes)
            print('step = {0}: Average Return = {1}'.format(step, avg_return))
            print('Energy Save = {0}'.format(eval_py_env.getEnergySave()))
            returns.append(avg_return)


    print('Energy Save = {0}'.format(eval_py_env.getEnergySave()))

    iterations = range(0, num_iterations + 1, eval_interval)
    iterations_full = range(0, num_iterations + 1, 1)
    # plot_fig(iterations, returns, 'Iterations', 'Average Return')

    # Save to checkpoint
    # train_checkpointer.save(global_step)

    # train_checkpointer.initialize_or_restore()
    # global_step = tf.compat.v1.train.get_global_step()

    # saved_policy = agent.policy
    # saved_collect_policy = agent.collect_policy
    model_version = "0001"
    model_name = "RTL_model"
    model_path = os.path.join(model_name, model_version)
    tf.saved_model.save(agent, model_path)

    time_seconds = time.time() - time1

    return iterations, returns, time_seconds, rewards, iterations_full


#### structure #######
# nodes : a list of [cap_node, cost_node, type_node, delay_node, [function_delay_node] = [delay_f1, delay_f2,...]
# links: a list of [cap_link]
# functions : a list of [req_f, type_f]
# demands : a list of [source, dest, bandwidth, sfc delay, [f1, f2, ...]]
# traffic_bandwidth : a list of 100 traffic bandwidth for each demand ; traffic_bandwidth[d] = [4,4,4,3,3,1,1,1 .....]
#######################
def readInputFile(filename):
    fout = codecs.open(filename, "r", encoding='utf-8')
    line0 = fout.readline()
    numbers = [int(n) for n in line0.split()]
    nFunction = numbers[0]
    nDemand = numbers[1]
    nNode = numbers[2]
    nEdge = numbers[3]

    line1 = fout.readline()  # a list of function
    lines = line1.split(";")
    functions = []
    for i in range(nFunction):
        func = lines[i].split()
        functions.append([int(func[1]), int(func[2])]) #[req_f, type_f]
    functions = np.array(functions)
    demands = []
    for i in range(nDemand):
        line = fout.readline()  # demand
        lineList = line.split()
        demand1 = []
        demand1.append(int(lineList[1]))  # source
        demand1.append(int(lineList[2]))  # dest
        demand1.append(int(lineList[3]))  # bw
        demand1.append(int(lineList[5]))  # sfc delay
        fd = []  # sfc
        #for f in range(int(lineList[6])):  # line1List[5] = number of VNFs
         #   fd.append(int(lineList[7 + f]))  # line1List[6] = vnf[0], line1Lsist[7] = vnf[1] ...
        for f in range(6, len(lineList)):  # line1List[5] = number of VNFs
            fd.append(int(lineList[f]))  # line1List[6] = vnf[0], line1Lsist[7] = vnf[1] ...
        demand1.append(fd)
        demands.append(demand1)

    capnodes = []
    for i in range(nNode):
        capnodes.append(int(fout.readline()))
    links = []
    for i in range(nNode):
        line = fout.readline()
        lineList = [int(n) for n in line.split()]
        links.append(lineList)
    nodeCost = []
    nodeDelay = []
    nodeType = []
    for i in range(nNode):
        line = fout.readline()
        lineList = line.split()
        nodeCost.append(int(lineList[1]))
        nodeDelay.append(float(lineList[2]))
        nodeType.append(int(lineList[3]))
    fout.readline()  # datasize on function
    functionDelayNode = []
    for i in range(nFunction):
        line = fout.readline()
        delay = [float(n) for n in line.split()]
        functionDelayNode.append(delay)
    functionDelayNode = np.array(functionDelayNode)
    print(functionDelayNode)
    nodes = []

    delayNodeOnFunction = []
    for i in range(nNode):
        print(functionDelayNode[:, i])
        delayNodeOnFunction.append(functionDelayNode[:, i])

    return capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, demands, functions

def find_optimal(newbw, capnodes, nodeCost, nodeType, nodeDelay, delayNodeOnFunction, links, functions, demands):
    V = capnodes  # [c(v1), c(v2), ...]
    node_cost = nodeCost
    node_type = nodeType
    node_delay = nodeDelay
    node_function_delay = delayNodeOnFunction

    E = links  # e[vi,vj], e.g., e[0,1] = 4

    F = functions[:, 0]  # ([f1, f2,...]) resource required for one unit traffic of function fi
    function_type = functions[:, 1]

    D = demands  # [source, dest, bandwidth, sfc delay, [f1, f2, ...]]

    ne = np.count_nonzero(E)
    nv = len(V)
    nd = len(D)
    nf = len(F)
    m = Model(name="MILP-VNF")
    id_x = [(v, d, i) for v in range(nv) for d in range(nd) for i in range(nf)]

    x = m.binary_var_dict(id_x, lb=0, ub=1, name="x")

    id_z = [d for d in range(nd)]
    z = m.binary_var_dict(id_z, lb=0, ub=1, name="z")
    # objective
    print(D)

    uc = 0
    ud = nd
    for d in range(nd):
        funct = D[d][4]
        print(newbw[d])
        for i in range(len(funct)):
            for v in range(nv):
                uc += x[v, d, i] * D[d][2] * F[funct[i] - 1] * node_cost[v] * newbw[d]
        ud += -z[d]
    alpha = 0.25
    beta = 0.75
    m.minimize(alpha * uc + beta * ud)

    # constrains
    # eq 0:(4) dont need
    # id_u = [(d, i) for d in range(nd) for i in range(nf)]
    # u = m.integer_var_dict(id_u, lb=0, ub=nv-1, name="u")
    # print(u)
    # for d in range(nd):
    #    for i in range(nf):
    #        m.add_constraint(m.sum(x[v, d, i] * v for v in range(nv)) - u[d, i] == 0)

    for v in range(nv):
        constraint5 = 0
        for d in range(nd):
            funct = D[d][4]
            for i in range(len(funct), nf):
                m.add_constraint(x[v, d, i] == 0, ctname="eq0")
    # eq 1: (5)
    for v in range(nv):
        constraint5 = 0
        for d in range(nd):
            funct = D[d][4]
            for i in range(len(funct)):
                constraint5 += newbw[d] * x[v, d, i] * F[funct[i] - 1]
        m.add_constraint(constraint5 <= V[v], ctname="eq1")


    # eg 3:(7)
    for d in range(nd):
        funct = D[d][4]
        for i in range(len(funct)):
            m.add_constraint(m.sum(x[v, d, i] for v in range(nv)) <= 1, ctname="eq3")

    # eg 4:(8)
    for d in range(nd):
        m.add_constraint(m.sum(x[v, d, i] for v in range(nv) for i in range(nf))
                         == z[d] * len(D[d][4]), ctname="eq4")

    # eg 5:(9)
    for d in range(nd):
        funct = D[d][4]
        for i in range(len(funct)):
            for v in range(nv):
                if function_type[funct[i] - 1] != node_type[v]:
                    m.add_constraint(x[v, d, i] == 0, ctname="eq5")



    m.export_as_lp(basename="nfv_opt", path="report/")
    m.set_time_limit(600)
    # solve:
    tms = m.solve(log_output=True)
    # assert tms
    # tms.display()
    vnf_cost = 0
    vnf_cost_demand = []
    x_val = np.zeros(shape=(nv, nd, nf), dtype=np.int32)
    z_val = np.zeros(shape=(nd,), dtype=np.int32)
    if tms and tms.is_feasible_solution:
        print("found solution")
        print(tms.objective_value)
        # calculate deployment cost:
        for d in range(nd):
            temp_cost = 0
            funct = D[d][4]
            if tms.get_value(z[d]) > 0:
                print(d)
                z_val[d] = 1
                for v in range(nv):
                    for i in range(len(funct)):
                        if tms.get_value(x[v, d, i]) > 0:
                            x_val[v, d, i] = 1
                            print(v)
                            print(tms.get_value(x[v, d, i]))
                            c = tms.get_value(z[d]) * tms.get_value(x[v, d, i]) * D[d][2] * F[
                                funct[i] - 1] * node_cost[v]
                            temp_cost += c
                            print(c)
                            vnf_cost += c * newbw[d]
            vnf_cost_demand.append(temp_cost)
    else:
        print("infeasible ; need to relax")
        # cr = ConflictRefiner()
        # crr = cr.refine_conflict(m, display=True)
        rx = Relaxer()
        rs = rx.relax(m)
        # rx.print_information()
        rs.display()
        for d in range(nd):
            temp_cost = 0
            funct = D[d][4]
            if rs.get_value(z[d]) > 0:
                print(d)
                z_val[d] = 1
                for v in range(nv):
                    for i in range(len(funct)):
                        if rs.get_value(x[v, d, i]) > 0:
                            x_val[v, d, i] = 1
                            print(v)
                            print(rs.get_value(x[v, d, i]))
                            c = rs.get_value(z[d]) * rs.get_value(x[v, d, i]) * D[d][2] * F[
                                funct[i] - 1] * node_cost[v]
                            temp_cost += c
                            print(c)
                            vnf_cost += c * newbw[d]
            vnf_cost_demand.append(temp_cost)
    #return x_val, z_val, vnf_cost, vnf_cost_demand
    return vnf_cost

def save_to_output_file(iterations, returns, time_seconds, filename_output):
    with open(filename_output, mode='w') as f:
        for i1 in range(len(iterations)):
            f.write(str(iterations[i1]) + ' ' + str(returns[i1]) + '\n')
        f.write("time in seconds = " + str(time_seconds))
def save_file(filename_input, no_demand, cost, time_total, filename_output):
    with open(filename_output, mode='a') as f:
        f.write(filename_input + ' '+ str(no_demand) + ' ' + str(cost) + ' ' + str(time_total) + '\n')
