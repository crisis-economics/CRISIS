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

class Market:
   def __init__(self, buyer):
      self.buyer = buyer
      self.askPrices = {}
      self.bidPrices = {}
      self.supply = {}
      self.demand = {}
   
   def setAskPrice(self, type, price):
      self.askPrices[type] = price
   
   def setBidPrice(self, type, price):
      self.bidPrices[type] = price
   
   def setSupply(self, type, supply):
      self.supply[type] = supply
   
   def setDemand(self, type, demand):
      self.demand[type] = demand
   
   def process(self, type):
      demandFor = (self.demand[type] if self.demand.has_key(type) else 0)
      supplyOf = self.supply[type] if self.supply.has_key(type) else 0
      askPrice = self.askPrices[type] if self.askPrices.has_key(type) else 0
      bidPrice = self.bidPrices[type] if self.bidPrices.has_key(type) else 0
      price = (bidPrice + askPrice) / 2.
      trade = min(demandFor, supplyOf)
      self.buyer.credit(price * trade)
      self.buyer.addGoods(type, trade)
      print("bought {} units of goods, total cost {}".format(price * trade, trade))
   
   def clear():
      self.askPrices.clear()
      self.bidPrices.clear()
      self.supply.clear()
      self.demand.clear()