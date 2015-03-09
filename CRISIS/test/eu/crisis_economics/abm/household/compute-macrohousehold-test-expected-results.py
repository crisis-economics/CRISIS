#!/bin/env/python
#
# This file is part of CRISIS, an economics simulator.
#
# Copyright (C) 2015 John Kieran Phillips
#
# CRISIS is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# CRISIS is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
#
# This codefile computes the expected results of the MacroHousehold.test.
# To use: execute 'python ./compute-macrohousehold-test-expected-results.py'
# in the directory of this file. The last five lines printed to the console
# are the expected results of MacroHousehold.test.
#

from household import *
from fund import *
from market import *

fund = Fund(10.)
household = Household(100., 1., fund)
goodsMarket = Market(household)

wagePerUnitLabour = 1.5e7

# Cycle 1
#
# The ordering of household decisions is:
# (1) fund contribution.
# (2) labour offerings, then employment.
# (3) consumption budget calculations.
# (4) goods orders.
#

# first employment session:
print("cycle 1")

fund.debit(10.)

household.employ(0.5, 10.)          # manual contract created

household.interactWithFund()

household.setLabourSupply(1.)

price = 1.
goodsMarket.setSupply(0., 0.)       # no supply
goodsMarket.setAskPrice(0., price)

consumptionBudget = household.computeConsumptionBudget()
demandForGoods = consumptionBudget / price

goodsMarket.setDemand(0., demandForGoods)
goodsMarket.setBidPrice(0., price)

goodsMarket.process(0)

# Cycle 2 #
print("cycle 2")

household.employ(0.5, 10.)          # manual contract created

household.interactWithFund()

household.setLabourSupply(1.)

price = 1.
goodsMarket.setSupply(0., 50.)       # 50.0 units of supply
goodsMarket.setAskPrice(0., price)

consumptionBudget = household.computeConsumptionBudget()
demandForGoods = consumptionBudget / price

goodsMarket.setDemand(0., demandForGoods)
goodsMarket.setBidPrice(0., price)

goodsMarket.process(0)

# Cycle 3 #
print("cycle 3")

household.interactWithFund()

household.setLabourSupply(1.)
household.employ(0.5, 40.)          # manual contract created

price = 1.
goodsMarket.setSupply(0., 50.)       # 50.0 units of supply
goodsMarket.setAskPrice(0., price)

consumptionBudget = household.computeConsumptionBudget()
demandForGoods = consumptionBudget / price

goodsMarket.setDemand(0., demandForGoods)
goodsMarket.setBidPrice(0., price)

goodsMarket.process(0)

# Cycle 4 #
print("cycle 4")

household.interactWithFund()

household.setLabourSupply(1.)
household.employ(0.5, 40.)          # manual contract created

price = 1.
goodsMarket.setSupply(0., 15.)       # 50.0 units of supply
goodsMarket.setAskPrice(0., price)

consumptionBudget = household.computeConsumptionBudget()
demandForGoods = consumptionBudget / price

goodsMarket.setDemand(0., demandForGoods)
goodsMarket.setBidPrice(0., price)

goodsMarket.process(0)

# Results:
print("results:")

print("household equity: {}".format(household.getEquity()))
print("household total assets: {}".format(household.getTotalAssets()))
print("household cash: {}".format(household.getCash()))
print("fund balance: {}".format(fund.getTotalAssets()))
