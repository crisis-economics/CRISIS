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

class Household:
   def __init__(self, cash, labour, mutualFund):
      self.cash = cash
      self.labour = labour
      self.consumptionDemand = {}
      self.mutualFund = mutualFund
      self.goodsOwned = {}
   
   def debit(self, value):
      self.cash += value
   
   def credit(self, value):
      self.cash -= value
   
   def employ(self, labour, wage):
      self.debit(labour * wage)
      self.labour -= labour
   
   def setConsumptionDemand(self, goodsType, quantity):
      self.consumptionDemand[goodsType] = quantity
   
   def setLabourSupply(self, labour):
      self.labour = labour
   
   def getEquity(self):
      return self.getTotalAssets()
   
   def getCash(self):
      return self.cash
   
   def getTotalAssets(self):
      return self.cash + self.mutualFund.getTotalAssets()
   
   def addGoods(self, type, amount):
      if(self.goodsOwned.has_key(type)):
         self.goodsOwned[type] += amount
      else:
         self.goodsOwned[type] = amount
   
   def computeConsumptionBudget(self):
      value = .9 * self.cash
      print("consumption budget: {0}".format(value))
      return value
   
   def interactWithFund(self):
      fundValue = self.mutualFund.getTotalAssets()
      if(fundValue > 150.):
         value = self.mutualFund.getTotalAssets() - 150.
         self.mutualFund.credit(value)
         self.debit(value)
         print("withdraw {} from fund".format(value))
      else:
         value = .1 * self.cash
         self.credit(value)
         self.mutualFund.debit(value)
         print("invest {} in fund".format(value))