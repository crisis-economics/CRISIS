Copyright (C) 2015 AITIA International, Inc.
    Permission is granted to copy, distribute and/or modify this document
    under the terms of the GNU Free Documentation License, Version 1.3
    or any later version published by the Free Software Foundation;
    with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts.
    A copy of the license is included in /tutorials/FDL.txt, or can 
    be downloaded from http://www.gnu.org/copyleft/fdl.html.

===========================================
Tutorial on Creating Custom Bank Strategies
===========================================

:Authors:
   Richard O. Legendi
:Version:
   Pre-release

Bank strategies can be implemented and used in a pluggable way: they can be
defined in a generic way, once they are defined, it is easy to change them
dynamically or create banks with different strategies.

Defining a New Strategy
=======================

A new strategy can be created by implementing the ``eu.crisis_economics.abm.bank.strategies.IBankStrategy``
interface.

Each of its methods get the current bank which would like to perform some
actions. It is important to note however that each of the banks may have a
separate strategy, or a group of them might work along the same strategy, it is
generally useful to specify a new strategy for each bank.

Required Methods
----------------

The most important definitions are the following ones for a new strategy::

   public abstract void considerDepositMarkets(StrategyBank bank);
   public abstract void considerCommercialLoanMarkets(StrategyBank bank);
   public abstract void considerStockMarkets(StrategyBank bank);
   public abstract void considerInterbankMarkets(StrategyBank bank);

These methods are automatically called by the Crisis Simulation Library and the
strategies can specify the action of the bank (e.g., to post orders on the
deposit market).

Plugging in Strategies
======================

To set up a bank that uses a given strategy, like the ``RandomStrategy``, does
not require more than to set it in its constructor::

   double cash = 100.0;
   IBankStrategy strategy = new RandomStrategy(...)
   StrategyBank bank = new StrategyBank(cash, strategy);

It is also possible to change the bank's strategy dynamically during runtime
with the ``setStrategy()`` method::

   if (bank.getCashReserves() < 100) {
      bank.setStrategy( new PriceStrategy(...) );
   } else {
      bank.setStrategy( new RandomStrategy(...) );
   }

Note on Information Availability
================================

Basically a strategy can access all the information encapsulated in the bank,
including the markets it is acting on (thus containing its currernt orders).

However, by creating a ``Bank`` implementation that has a reference to the
required information, it can be delegated as well to the strategies. This can
even include the model, so the whole universe of information can be accessed
too.
