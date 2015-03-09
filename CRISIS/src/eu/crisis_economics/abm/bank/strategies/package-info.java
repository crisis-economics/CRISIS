/**
  * Bank Strategies.
  * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
  * + Notes about hidden files.
  * The following files exist on the code repository but are not
  * included in the package bank.strategies by default. To examine
  * or restore any of the following units, see revision 1932 of
  * the Default branch.
  * ClearingFundamentalistStrategy.java
  *  This file was removed on 28 May 2014. ClearingFundamentalistStrategy.java was 
  *  considered to be redundant following the addition of ClearingBankStrategy.java.
  * IncrementalWeightAdjustmentStrategy.java
  *  This file was removed on 28 May 2014. bankStrategies.IncrementalWeightAdjustmentStrategy 
  *  (IWAS) is from the LoB (Limit-Order-Book) era. In this strategy, regularBanks choose between 
  *  trend-follower and fundamentalist bankStrategies based on their profitability. Currently 
  *  deprecated, but perhaps this codefile will be useful in future if we return to LoB.
  */
/**
  * @author      JKP
  * @category    Bank Strategies
  * @since       1.0
  * @version     1.0
  */
package eu.crisis_economics.abm.bank.strategies;