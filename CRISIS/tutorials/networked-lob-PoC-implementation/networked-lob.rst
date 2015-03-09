Copyright (C) 2015 AITIA International, Inc.
    Permission is granted to copy, distribute and/or modify this document
    under the terms of the GNU Free Documentation License, Version 1.3
    or any later version published by the Free Software Foundation;
    with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts.
    A copy of the license is included in /tutorials/FDL.txt, or can 
    be downloaded from http://www.gnu.org/copyleft/fdl.html.

==================================================================
Tutorial on Using Order Filtering in the Crisis Simulation Library
==================================================================

:Authors:
   Richard O. Legendi
:Version:
   Pre-release
   
This tutorial covers how the *filtering facility* works in the Crisis Simulation
Library that can be used to implement Networked Limit Order Book behaviour.  

Submitting Orders
=================

To add an order to any of the markets, only an instance of the ``eu.crisis_economics.abm.markets.nonclearing.Market``
class is required.

As an example, a ``firm`` and a ``bank`` may use a ``CommercialLoanMarket`` to
submit orders that are matched automatically::

   ...
   market.addBuyOrder( firm, maturity, size, price );
   market.addOrder( bank, maturity, size, price );
   
Assig Filters to Orders
=======================

Filters are implemented in a way that when the market allows the matching of a
buy and a sell order, it also executes the filter conditions assigned to **both**
orders. If both of them is satisfied, the matching is executed; otherwise the
matching process continues on with the next order in the limited order book,
even if its price is higher. 

The Available Default Filters
-----------------------------

In order to use filters, first add the following ``import`` statement to the
Java file::

   import static eu.crisis_economics.abm.markets.nonclearing.DefaultFilters.*;
   
From now on, a set of basic filters are available to use, please refer to the
documentation of the ``DefaultFilters`` class for the details.

There is two small examples that may be used to demonstrate the features, ``any()``
and ``only()``:

- ``any()`` is an optional filter that allows the order to be matched with any
  order
- ``only(party1, party2, ...)`` is a filter that allows the order to be matched
  only by the orders of the specified parties (e.g., a bank can submit specific
  orders to specific firms)

Specifying the Filters
----------------------

All of the ``addOrder()`` methods should have a version that accept an optional
filter argument as well.

For example, a bank may add an order that is acceptable by any other orders as::

   market.addOrder( bank2, 1, 10, 1, any() );
   
Similarly, a firm may request a loan from a specific bank as::

   market.addOrder( bank, 1, 10, 1, only( firm ) );

Defining a New Filter Type
==========================

It is also possible to implement custom filter expressions by subclassing the
``Filter`` class, which is a class defined within the ``DefaultFilters`` class.
It defines a single function that gets the current order which the market would
like to match the current order with, and must return a boolean value that
specifies if the matching is whether allowed or not::

   public abstract boolean matches(Order order);
   
As an example, a new filter that does not allow any matching at all can be
defined as follows::

   private static Filter none = new Filter() {
      // This method is called by the Limited Order Book implementation on matching
      @Override
      public boolean matches(final Order order) {
         // Always returning false prohibits the matching of the assigned order
         return false;
      }
   };

Using the filter is similar to the others, just pass an instance as the optional
filter argument for the ``addOrder()`` function::

   market.addOrder( firm, 1, - 10, 1, none );

More Examples
=============

For elementary examples, please take a look on the ``MarketTest`` class. For a
filter that allows banks to give loans to firms based on their risk level, take
a look on the ``palermo`` branch of the library.

Notes
-----
Please note that this facility has only a proof-of-concept implementation at the
moment: its usage might have unexpected influence on the statistics of the LOB.
