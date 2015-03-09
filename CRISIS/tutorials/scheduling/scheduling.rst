Copyright (C) 2015 AITIA International, Inc.
    Permission is granted to copy, distribute and/or modify this document
    under the terms of the GNU Free Documentation License, Version 1.3
    or any later version published by the Free Software Foundation;
    with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts.
    A copy of the license is included in /tutorials/FDL.txt, or can 
    be downloaded from http://www.gnu.org/copyleft/fdl.html.

==============================================================
Tutorial on Scheduling Events in the Crisis Simulation Library
==============================================================

:Authors:
   Richard O. Legendi
:Version:
   Pre-release

The integrated simulation that is under development in the CRISIS project has a
strict ordering for the events scheduled for firing at the same time. However, when creating prototypes or performing
experiments with different parts of the economy, one might need to take
control over the ordering of these events. This tutorial demonstrates how to work
with and manipulate the scheduling order.

Scheduling in General
=====================

The most important class is ``eu.crisis_economics.abm.ScheduleOrder`` when it
comes to defining events. It is a simple (but slightly improved) enumeration
that defines a few constants for the ordering of different events used by Mason.

In most of the cases a new Mason ``Steppable`` is scheduled like this in the
API::
   
   import static eu.crisis_economics.abm.ScheduleOrder.*;
   
   schedule.scheduleOnce( Simulation.getSimState().schedule.getTime() + Simulation.getStepInterval(),
         ScheduleOrder.orderOf( POST_BANKRUPTCY_STATS),
         new Steppable() {
            @Override
            public void step(final SimState state) {
               // ... Required events take place here...
               
               schedule.scheduleOnce( Simulation.getSimState().schedule.getTime() + Simulation.getStepInterval(), orderOf( POST_BANKRUPTCY_STATS ), this );
            }
         } );

Here we are using the ``scheduleOnce()`` method for two reasons:

1. It is possible to control the simulation in a centralized way. For instance,
   changing the default *step interval* constant will change all the events in
   the simulation
2. The order of events which take place in the same time step can be enforced.
3. It is possible this way to remove an event from the schedule by introducing a
   new boolean flag. If the agent go bankrupt for instance, simply skip the next
   ``scheduleOnce()`` call and it is removed from the schedule. Otherwise,
   simply use the ``scheduleRepeating()`` function.

Manipulating the Events
=======================

The ordering of events is completely customizable. Any new events can be
inserted into the API and even the whole scheduling can be altered.

These operations can also be done through the ``ScheduleOrder`` class where the
ordering is automatically managed and can be changed dynamically even during
runtime.

Inserting a New Event
---------------------

It is common that a new event should be inserted between two already defined
event.

There is basically two ways to do that, by inserting a new event *before* or
*after* a referenced event.

The functions to do that are the ``registerEventAfter()`` and
``registerEventBefore()``. Both of them have two parameters: the previously
defined event (as a reference where to insert the new event) and a new
identifier of the new event, which can be a unique String.

As an example, to define a new event after a previously defined event like
``COMMERCIAL_MARKET_BIDDING``, create a new constant identifier and register it.
From there on, the ``orderOf()`` function recognises the new event::

   import static eu.crisis_economics.abm.ScheduleOrder.*;
   
   public class ModelWithNewCustomEvent {
   
       public static String CUSTOM_EVENT = "...";
       ...
   
       public ModelWithNewCustomEvent(long seed) {
           ...
           registerEventAfter( COMMERCIAL_MARKET_BIDDING, CUSTOM_EVENT) ;
       }
   
       public void start() {
           ...
           scheduleOnce( ..., orderOf(CUSTOM_EVENT) );
       }
   }

Redefining the Order of Events
------------------------------
 
It is also possible to remove all defined orders, and create a custom ordering
by using the ``setOrderTo()`` method. This drops any previously defined ordering
and replaces it with the ordering defined by its arguments::

    setOrderTo( COMMERCIAL_LOAN_MARKET, EVENT, ... )

*Note* If an ordering is requested for an event that was removed previously from
the list of events, the API throws a runtime exception. This is important to
hunt down bugs when there is an event scheduled but its ordering is accidentally
removed. This situation indicates a possible bug.
