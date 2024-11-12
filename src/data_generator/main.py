import generateTopo as gen

if __name__ == '__main__':
    #### READ_ME ######
    # create new folder "output"  #
    ####################

    #gen.demand_change("BB")
    #gen.demand_change("FW")

    n = 20
    gen.demand_change("EB", n)
    gen.demand_change("EW", n)
    gen.demand_change("WB", n)
    gen.demand_change("BF", n)
