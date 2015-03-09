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
# This codefile computes the expected results of MacroFirm.test.
#
# To use:
#   execute 'python ./compute-macrofirm-test-expected-results.py'
#   in the directory of this file. The last lines printed to the
#   console are the expected results of MacroFirm.test.
#

class GoodsMarket:
   def __init__(self, seller):
      self.seller = seller          # the goods seller
      self.demand = 0.0             # the level of market demand
   
   def setDemand(self, demand):
      self.demand = demand
   
   def processTrade(self):
      supply = self.seller.getGoodsOwned()
      sellingPrice = self.seller.getSellingPrice()
      trade = min(self.demand, supply)
      self.seller.sellGoods(trade, sellingPrice)
      print( \
         "processing goods market:\n supply: {}\n demand: {}\n trade: {}\n" \
         "cost: {}".format(supply, self.demand, trade, trade * sellingPrice))

class LabourMarket:
   def __init__(self, employer, wage):
      self.employer = employer      # the employer
      self.supply = 0.0             # the supply of labour (number of units)
      self.wage = wage              # wage per unit labour
   
   def setSupply(self, supply):
      self.supply = supply
   
   def setWage(self, wage):
      self.wage = wage
   
   def processTrade(self):
      demand = self.employer.getLabourDemand()
      trade = min(demand, self.supply)
      self.employer.employ(trade, self.wage)
      print( \
         "processing labour market:\n supply: {}\n demand: {}\n trade: {}\n" \
         " cost: {}\n firm cash: {}".format( \
         self.supply, demand, trade, self.wage, self.employer.getCash()))

   
from firm import *

wage = 1.0
firm = Firm(0.5)
firm.setTFP(1.0)
firm.setGoodsOwned(1.0)
goodsMarket = GoodsMarket(firm)
labourMarket = LabourMarket(firm, wage)

#
# The ordering of firm decisions is:
#   (1) sell goods produced in the last business cycle
#   (2) consider production targets for the current business cycle
#   (3) pay outstanding dividends (as decided in the last business cycle)
#   (4) bid for and receive commercial loans
#   (5) buy input goods (production goods) and labour
#   (6) produce new goods
#   (7) household consumption
#   (8) firm accounting, including the computation of future dividends to pay
#

# stimuli

consumptionDemand     = [ 0.5, 0.55, 0.6, 0.55, 0.5 ];
firmtargetProductions = [ 1.0, 2.0,  3.0, 2.0,  1.0 ];
goodsPrices           = [ 5.0, 6.0,  7.0, 6.0,  4.0 ];
wageBidPrices         = [ 1.0, 0.9,  0.2, 0.8,  1.1 ];
liquidityTargets      = [ 1.0, 0.8,  0.6, 0.7,  0.8 ];

for i in range(0, 4):
   print("\n\n * cycle " + str(i))
   
   wage = wageBidPrices[i]
   firm.setSellingPrice(goodsPrices[i])
   firm.setTargetProduction(firmtargetProductions[i])
   firm.setProduction(firmtargetProductions[i])
   firm.setLiquidityTarget(liquidityTargets[i])
   labourMarket.setWage(wage)
   
   # commercial loans
   firm.considerProduction()
   loanRequired = firm.getCommercialLoanDemand(wage)
   firm.giveLoan(loanRequired, 0.1)
   print("loan demand: {}".format(loanRequired))
   
   goodsMarket.setDemand(consumptionDemand[i]) 
   labourMarket.setSupply(20.0)
   labourMarket.processTrade()
   
   firm.produceGoods()
   
   goodsMarket.processTrade()
   
   firm.computeIntendedDividend()
   firm.payLoan()
   firm.payDividends()
   
   print("* firm cash: {}".format(firm.getCash()))
   
   firm.flush()
   continue

print("\n The expected firm deposit account value is: {}\n".format(firm.getCash()))