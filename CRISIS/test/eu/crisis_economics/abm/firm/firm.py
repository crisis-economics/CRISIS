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

import math

class Firm:
   def __init__(self, cash):
      self.cash = cash
      self.labourInputDemand = 0.0  # labour desired
      self.labour = 0.0             # labour employed
      self.goodsOwned = 0.0         # goods owned and available for production or sale
      self.goodsPrice = 0.0         # selling price of goods (per unit)
      self.goodsProduced = 0.0      # goods produced but not yet in inventory
      self.loansToPay = 0.0         # outstanding loans to pay
      self.loanInterestRate = 0.0   # loan interest rate
      self.targetProduction = 0.0   # target goods yield
      self.dividendToPay = 0.0      # dividend to pay
   
   def debit(self, value):
      self.cash += value
   
   def credit(self, value):
      self.cash -= value
   
   # Loans
   
   def giveLoan(self, principal, interestRate):
      self.loansToPay = principal
      self.loanInterestRate = interestRate
      self.debit(principal)
   
   def payLoan(self):
      totalRepayment = self.loansToPay * (1.0 + self.loanInterestRate)
      self.credit(totalRepayment)
      self.loansToPay = 0.0
      self.loanInterestRate = 0.0
      print(\
         "repaying loan:\n total payment: {}\n cash remaining: {}".format( \
         totalRepayment, self.getCash()
         ))
   
   # Labour and employment:
   
   def employ(self, labour, wage):
      self.credit(labour * wage)
      self.labour += labour
   
   def fire(self, labour, wage):
      self.labour -= labour
      self.labour = math.max(self.labour, 0.0)
   
   def getLabourDemand(self):
      return self.labourInputDemand
   
   # Production, yields and selling prices:
   
   def setGoodsOwned(self, goodsOwned):
      self.goodsOwned = goodsOwned
   
   def getGoodsOwned(self):
      return self.goodsOwned
   
   def sellGoods(self, quantity, price):
      income = quantity * price
      self.debit(income)
      self.goodsOwned -= quantity
      self.goodsOwned = max(self.goodsOwned, 0.0)
      print( \
         "selling goods:\n income: {}\n goods remaining: {}\n cash now: {}".format( \
         income, self.goodsOwned, self.getCash()))
   
   def setSellingPrice(self, sellingPrice):
      self.goodsPrice = sellingPrice
   
   def getSellingPrice(self):
      return self.goodsPrice
   
   def produceGoods(self):
      goodsYield = self.labour * self.TFP
      self.goodsProduced = goodsYield
   
   def setProduction(self, production):
      self.goodsProduced = production
   
   def setTargetProduction(self, target):
      self.targetProduction = target
   
   def considerProduction(self):
      self.labourInputDemand = self.targetProduction / self.TFP
      print("considering production:\n target yield: {}\n TFP: {}\n " \
         "labour required: {}".format( \
         self.targetProduction, self.TFP, self.labourInputDemand))
   
   def getCommercialLoanDemand(self, wage):
      cost = self.labourInputDemand * wage
      print("computing loan demand: \n wage: {}\n labour expense: {}\n cash: {}".format( \
         wage, cost, self.getCash()))
      return max(0.0, self.labourInputDemand * wage - self.getCash())
   
   def setTFP(self, TFP):
      self.TFP = TFP
   
   # Equity, cash, total assets and liabilities:
   
   def getCash(self):
      return self.cash
   
   def getEquity(self):
      return self.getTotalAssets()
   
   def getTotalAssets(self):
      # not including pending production goods here:
      inventoryValue = (self.goodsOwned) * self.goodsPrice
      print( \
         "total assets:\n cash: {}\n goods owned: {}\n produced: {}" \
         "\n price per unit: {}\n inventory total: {}\n total assets: {}".format( \
         self.cash, self.goodsOwned, self.goodsProduced, self.goodsPrice, \
         inventoryValue, self.cash + inventoryValue))
      # consumption (saleable) goods will actually not appear in
      # firm equity at the time of measurement. This is because the
      # unsold consumption goods decay at midnight on the simulation
      # clock, and the firm dividend payment it scheduled early in 
      # the next soonest simulation cycle.
      return self.cash + inventoryValue
   
   def getTotalLiabilities(self):
      return self.loansToPay * (1.0 + self.loanInterestRate)
   
   # Dividends
   
   def setLiquidityTarget(self, target):
      self.liquidityTarget = target
   
   def computeIntendedDividend(self):
      self.dividendToPay = max(0.0, \
         self.getCash() - self.liquidityTarget \
       - self.loansToPay * (1.0 + self.loanInterestRate) \
         )
      print( \
         "computing dividend to pay: \n cash: {}\n liquidity target: {}\n loan debt: {}" \
         "\n dividend to pay: {}".format(self.getCash(), self.liquidityTarget, \
         self.loansToPay * (1.0 + self.loanInterestRate), self.dividendToPay) \
         )
   
   def payDividends(self):
      print("paying dividends:\n dividend to pay: {}\n cash before: {}"\
         .format(self.dividendToPay, self.getCash()))
      self.credit(self.dividendToPay)
      self.dividendToPay = 0.0
      print(" cash after: {}".format(self.getCash()))
   
   # reset state
   
   def flush(self):
      self.labour = 0.0
      self.goodsOwned = self.goodsProduced
      self.goodsProduced = 0.0