/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 Ross Richardson
 * Copyright (C) 2015 John Kieran Phillips
 *
 * CRISIS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CRISIS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CRISIS.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.crisis_economics.abm.government;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ai.aitia.meme.paramsweep.platform.mason.recording.annotation.RecorderSource;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.crisis_economics.abm.Agent;
import eu.crisis_economics.abm.agent.AgentOperation;
import eu.crisis_economics.abm.contracts.AllocationException;
import eu.crisis_economics.abm.contracts.Contract;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.FixedValueContract;
import eu.crisis_economics.abm.contracts.InsufficientFundsException;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.contracts.loans.Borrower;
import eu.crisis_economics.abm.contracts.loans.Lender;
import eu.crisis_economics.abm.contracts.settlements.Settlement;
import eu.crisis_economics.abm.contracts.settlements.SettlementFactory;
import eu.crisis_economics.abm.contracts.stocks.StockAccount;
import eu.crisis_economics.abm.contracts.stocks.StockHolder;
import eu.crisis_economics.abm.household.MacroHousehold;
import eu.crisis_economics.abm.inventory.goods.DurableGoodsRepository;
import eu.crisis_economics.abm.markets.Buyer;
import eu.crisis_economics.abm.markets.GoodsBuyer;
import eu.crisis_economics.abm.markets.clearing.ClearingHouse;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsInstrument;
import eu.crisis_economics.abm.markets.clearing.SimpleGoodsMarket;
import eu.crisis_economics.abm.markets.clearing.SimpleLabourMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketInformation;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingMarketParticipant;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.ClearingStockMarket;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.GraduallySellSharesMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.LoanConsumerMarketResponseFunctionFactory;
import eu.crisis_economics.abm.markets.clearing.heterogeneous.MarketResponseFunction;
import eu.crisis_economics.abm.markets.nonclearing.OrderException;
import eu.crisis_economics.abm.markets.nonclearing.StockMarket;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.ScheduleIntervals;
import eu.crisis_economics.abm.simulation.Simulation;
import eu.crisis_economics.abm.simulation.injection.factories.AbstractClearingMarketParticipantFactory;
import eu.crisis_economics.abm.simulation.injection.factories.ClearingMarketParticipantFactory;
import eu.crisis_economics.utilities.Pair;
import eu.crisis_economics.utilities.StateVerifier;

public final class Government extends Agent implements
   StockHolder, Lender, Borrower, Employer, Buyer, GoodsBuyer, ClearingMarketParticipant {
   
   private double
      labourTaxLevel,
      goodsTaxLevel,
      capitalGainsTaxLevel,
      cashReserveTarget,
      totalCashEverSpendOnBailoutProcedures,
      taxedCollectedLastCycle,
      taxesCollectedAtThisTime,
      totalExpensesBudget,
      wagePerUnitLabourToBid,
      welfareCosts = 0,
      stockAccountNumberOfSharesAtWhichToDisown = 1.e-14;
   
   private final FixedValueContract cashReserve;
   
   private final Map<String, StockAccount> stockAccounts;
   
   private final Map<String, Employee> welfaceRecipientsInAuthority;
   private final Set<Labour> currentEmployment;
   private SimpleLabourMarket labourMarket;
   
   private SimpleGoodsMarket
      goodsMarket;
   private Map<String, Double>
      consumptionWeights;
   private DurableGoodsRepository
      goodsRepository;
   
   private double
      giltDebtTarget;
   
   private static class TimestampedTransaction extends Pair<Double, Double> {
      public TimestampedTransaction(Double first, Double second) {
         super(first, second);
      }
   }
   private Deque<TimestampedTransaction> bailoutExpenseHistory;
   
   private static final double
      INITIAL_TAX_LEVEL = .10,              // Initial taxation level.
      DEFAULT_INITIAL_EMPLOYMENT_PERCENT_OF_WORKFORCE = .2,
                                            // Default employent, as percent of available labour.
      BAILOUT_MEMORY_TIME_HORIZON = 50. * ScheduleIntervals.ONE_YEAR.getValue(); // 50 Year Memory.
   
   private ClearingMarketParticipant
      clearingMarketParticipation;
   
   /**
     * Create a government object with:
     *   (a) fixed labour taxation (% of all labour wage),
     *   (b) fixed goods taxation (% of all cash-for-goods settlements),
     *   (c) an initial cash endowment (starting liquidity).
     */
   @Inject
   public Government(
   @Named("GOVERNMENT_INITIAL_STARTING_CASH")
      final double startingLiquidity,
   @Named("GOVERNMENT_CLEARING_MARKET_PARTICIPATION")
      final ClearingMarketParticipantFactory clearingMarketParticipation
      ) {
      super();
      Preconditions.checkNotNull(clearingMarketParticipation);
      Preconditions.checkArgument(startingLiquidity > 0.);
      this.totalCashEverSpendOnBailoutProcedures = 0.;
      this.labourTaxLevel = INITIAL_TAX_LEVEL;
      this.goodsTaxLevel = INITIAL_TAX_LEVEL;
      this.capitalGainsTaxLevel = INITIAL_TAX_LEVEL;
      this.cashReserve = new FixedValueContract(Double.MAX_VALUE);
      cashReserve.setValue(startingLiquidity);
      this.assets.add(cashReserve);
      this.stockAccounts = new HashMap<String, StockAccount>();
      this.welfaceRecipientsInAuthority = new HashMap<String, Employee>();
      this.cashReserveTarget = startingLiquidity;
      this.currentEmployment = new HashSet<Labour>();
      this.bailoutExpenseHistory = new ArrayDeque<Government.TimestampedTransaction>();
      this.giltDebtTarget = 0.;
      this.clearingMarketParticipation = clearingMarketParticipation.create(this);
      
      scheduleSelf();
   }
   
   /**
     * Create a {@link Government} {@link Agent} with custom parameters.<br><br>
     * 
     * This constructor simplifies the creation of a {@link Government} {@link Agent} by
     * assuming that the {@link Government} responds to all stock markets by gradually 
     * selling all types of shares. This {@link Government} will never bid to buy shares
     * of any kind.
     * 
     * @param initialStartingCash
     *        The initial {@link Government} cash endowment.
     * @param clearingHouse
     *        The {@link ClearingHouse} through which this {@link Agent} will participate
     *        in the clearing markets.
     */
   public Government(
      final double initialStartingCash,
      final ClearingHouse clearingHouse
      ) {
      this(
         initialStartingCash,
         new AbstractClearingMarketParticipantFactory() {
            @Override
            protected void setupStockMarketResponses() {
               for(final ClearingStockMarket market :
                  clearingHouse.getMarketsOfType(ClearingStockMarket.class))
                  responses().put(
                     market.getMarketName(),
                     (new GraduallySellSharesMarketResponseFunctionFactory(market))
                     );
               }
            @Override
            protected void setupLoanMarketResponses() {
               for(final ClearingStockMarket market :
                  clearingHouse.getMarketsOfType(ClearingStockMarket.class))
                  responses().put(
                     market.getMarketName(),
                     (new LoanConsumerMarketResponseFunctionFactory(market))
                     );
               }
         }
      );
   }

   
   private void scheduleSelf() {
      Simulation.repeat(
         this, "resetMemories", NamedEventOrderings.BEFORE_ALL);
      Simulation.repeat(
         this, "postLabourMarketOrders", NamedEventOrderings.HIRE_WORKERS);
      Simulation.repeat(
         this, "orderGoodsFromMarket", NamedEventOrderings.FIRM_BUY_INPUTS);
      Simulation.repeat(
         this, "distributeExcessCash", NamedEventOrderings.GOVERNMENT_PAY_WELFARE_AND_BENEFITS);
      Simulation.repeat(
         this, "reviewStockAssets", NamedEventOrderings.POST_CLEARING_MARKET_MATCHING);
   }
   
   @SuppressWarnings("unused")   // Scheduled
   private void reviewStockAssets() {
      final List<StockAccount>
         accounts = new ArrayList<StockAccount>(getStockAccounts().values());
      for(StockAccount account : accounts)
         if(account.getQuantity() <= stockAccountNumberOfSharesAtWhichToDisown)
            account.disownShares();
   }
   
   /**
     * Enable labour market participation with a custom 
     * government employment target (percent of workforce).
     */
   public void enableLabourMarketParticipation(
      final SimpleLabourMarket labourMarket,
      final double wagePerUnitLabourToBid
      ) {
      Preconditions.checkNotNull(labourMarket);
      this.labourMarket = labourMarket;
      this.wagePerUnitLabourToBid = wagePerUnitLabourToBid;
   }
   
   /**
     * Enable Gilt market participation with a custom 
     * government debt target. The argument to this method should
     * be non-negative. Otherwise, the argument is silently trimmed
     * to zero.
     */
   public void enableGiltMarketParticipation(
      final double giltDebtTarget
      ) {
      this.giltDebtTarget = Math.max(giltDebtTarget, 0.);
   }
   
   /**
     * Enable goods market participation for this {@link Government} {@link Agent}
     * 
     * @param goodsMarket
     *        The {@link SimpleGoodsMarket} through which to submit orders
     * @param consumptionWeights
     *        Consumption weights for each goods type. This map contains 
     *        non-negative consumption weights for each type of good, keyed by 
     *        the name of the good. The sum of all such weights should be equal
     *        to {@code 1.0}.
     */
   public void enableGoodsMarketParticipation(
      final SimpleGoodsMarket goodsMarket,
      final Map<String, Double> consumptionWeights
      ) {
      StateVerifier.checkNotNull(goodsMarket, consumptionWeights);
      this.goodsMarket = goodsMarket;
      this.goodsRepository = new DurableGoodsRepository(goodsMarket.getGoodsClassifier());
      { // Assert the sum of consumption weights is not greater than 1.0:
         double
            weightSum = 0.;
         for(final Entry<String, Double> record : consumptionWeights.entrySet())
            weightSum += record.getValue();
         if(Math.abs(weightSum - 1.) > 1.e-10)
            throw new IllegalArgumentException(
               "Government.enableGoodsMarketParticipation: illegal argument: the sum of "
             + "goods consumption weights is not 1.0.");
      } 
      this.consumptionWeights = Collections.unmodifiableMap(consumptionWeights);
   }
   
   @SuppressWarnings("unused") // Scheduled
   private void resetMemories() {
      taxedCollectedLastCycle = taxesCollectedAtThisTime;
      taxesCollectedAtThisTime = 0.;
      
      if(cashReserve.getValue() > cashReserveTarget)
         totalExpensesBudget = cashReserve.getValue() - cashReserveTarget;
      else
         totalExpensesBudget =
            Math.max(.9 * taxedCollectedLastCycle - getMeanBailoutCostPerCycle(), 0.);
   }
   
   @SuppressWarnings("unused") // Scheduled
   /**
     * Add market orders for labour contracts.
     */
   private void postLabourMarketOrders() {
      if(labourMarket == null) return;
      final double
         wagePerUnitLabour = wagePerUnitLabourToBid,
         desiredWorkforce = getMaximumLabourExpenseBudget() / wagePerUnitLabour,
         existingWorkforce = getLabourForce(),
         targetRecruitment = Math.max(desiredWorkforce - existingWorkforce, 0.),
         recruitmentCost = targetRecruitment * wagePerUnitLabour;
      if(targetRecruitment == 0.) return;
      try {
         labourMarket.addOrder(this, 1, -targetRecruitment, wagePerUnitLabour);
      } catch (final AllocationException neverThrows) { }
      catch (final OrderException e) {
         System.err.println(
            "Government.postLabourMarketOrders: labour market rejected a goods market order of"
          + " for " + targetRecruitment + " units of labour at price " + wagePerUnitLabour
          + " per unit. No action was taken.");
      } 
   }
   
   @SuppressWarnings("unused") // Scheduled
   /**
     * Add market orders for goods.
     */
   private void orderGoodsFromMarket() {
      if(goodsMarket == null)
         return;                                                           // No Goods Market
      final double
         totalConsumptionBudget = Math.max(0., getMaximumGoodsExpenseBudget());
      final HashMap<String, Double>
         worstAskPrices = getGoodsMarketPrices(consumptionWeights);
      for(final Entry<String, Double> record : worstAskPrices.entrySet()) {
         final String
            name = record.getKey();
         final double
            price = record.getValue(),
            budgetForThisGood = totalConsumptionBudget * consumptionWeights.get(name),
            orderSize = budgetForThisGood / price;
         try {
            goodsMarket.addOrder(this, record.getKey(), -orderSize, price);
         }
         catch (final AllocationException neverThrows) { }
         catch (final OrderException e) {
            System.err.println(
               "Government.orderGoodsFromMarket: goods market rejected a goods market order of"
             + " for " + orderSize + " units of labour at price " + price + " per unit. No action"
             + " was taken.");
         } 
      }
   }
   
   /**
     * Get a {@link LinkedHashMap} of goods market ask prices. The prices in 
     * the returned list are the worst (most expensive) ask prices among all
     * registered market sellers.<br><br>
     * 
     * This method returns a {@link Map} populated with the same keys as are
     * specified by the argument. If the argument contains a key (goods type)
     * which is unknown to the {@link SimpleGoodsMarket}, then the returned
     * value omits this key.
     * 
     * @param consumptionWeights
     *        A {@link LinkedHashMap} of consumption weights for each type
     *        of goods. The keys of this map specify which goods should be 
     *        bought by the {@link Government} {@link Agent}. The values of
     *        this map must be non-negative {@link Double}{@code s} summing
     *        to {@code 1}.
     */
   private HashMap<String, Double> getGoodsMarketPrices(
      final Map<String, Double> consumptionWeights
      ) {
      final HashMap<String, Double>
         result = new LinkedHashMap<String, Double>();
      for(final Entry<String, Double> record : consumptionWeights.entrySet()) {
         final String
            goodsType = record.getKey();
         final SimpleGoodsInstrument
            instrument = goodsMarket.getInstrument(goodsType);
         if(instrument == null) {
            System.err.println(
               "Government: market does not contain an instrument for goods of type " 
              + goodsType + ". The government is unable to order goods of this type.");
            continue;
         }
         else
            result.put(record.getKey(), instrument.getWorstAskPriceAmongSellers());
      }
      return result;
   }
   
   /**
     * Distribute welfare and benefit cash to beneficiaries in the authority
     * of this {@link Government} {@link Agent}. The procedure used to distribute
     * welfare cash to beneficiaries is as follows:<br><br>
     * 
     *   (a) The {@link Government} computes the maximum amount of cash, B, that 
     *       it is willing to pay in welfare and benefits;<br>
     *   (b) The {@link Government} determines the aggregate unemployment, U,
     *       among all welfare recipients in its authority;<br>
     *   (c) The {@link Government} pays approximately B * U(beneficiary) / U to each
     *       beneficiary, where U(beneficiary) is the current unemployment of
     *       the beneficiary. In the interest of numerical stability, in the
     *       event that U(beneficiary) is less than 10**-6 *
     *       L(beneficiary), where L(beneficiary) is the maximum amount of 
     *       labour that the beneficiary could provide in this cycle, the 
     *       beneficiary receives no welfare payments from the {@link Government}.
     * 
     * If the total unemployment observed in the authority of this {@link Government}
     * is zero, then the {@link Government} pays no cash in welfare.
     */
   @SuppressWarnings("unused") // Scheduled
   private void distributeExcessCash() {
      welfareCosts = 0;
      double
         excessCashToDistribute = getMaximumWelfareAndBenefitsBudget(),
         maximumCashPerBeneficiary = excessCashToDistribute / welfaceRecipientsInAuthority.size();
//      if(cashReserve.getValue() - excessCashToDistribute 
//       - taxesCollectedAtThisTime - getMaximumGoodsExpenseBudget() >
//         cashReserveTarget)
//         excessCashToDistribute +=
//            cashReserve.getValue() - excessCashToDistribute 
//          - cashReserveTarget - taxesCollectedAtThisTime
//          - getMaximumGoodsExpenseBudget();
      if(excessCashToDistribute < 0. || welfaceRecipientsInAuthority.size() == 0)
         return;
      double
         totalUnemployment = 0.;
      for(final Employee welfareRecipient : welfaceRecipientsInAuthority.values())
         totalUnemployment += welfareRecipient.getLabourAmountUnemployed();
      if(totalUnemployment <= 0.)                          // Zero unemployment.
         return;
      for(final Employee welfareRecipient : welfaceRecipientsInAuthority.values()) {
         final Settlement settlment =
            SettlementFactory.createDirectSettlement(this, welfareRecipient);
         final double
            labourOffered = welfareRecipient.getMaximumLabourSupply(),
            labourUnsold = welfareRecipient.getLabourAmountUnemployed();
         if(labourOffered <= 0.)
            continue;
         if(labourUnsold / labourOffered < 1.e-6)
            continue;
         final double
            welfareAllowance = maximumCashPerBeneficiary * (labourUnsold / labourOffered);
         
         // 
         // TODO: remove this
         // 
         if(welfareRecipient instanceof MacroHousehold)
            ((MacroHousehold)welfareRecipient).setWelfareAllowance(welfareAllowance);
         
         welfareCosts += welfareAllowance;
         if(welfareAllowance <= 0.) continue;
         try {
            settlment.transfer(welfareAllowance);
         } catch (final InsufficientFundsException neverThrows) {
            settlementFailure(welfareAllowance, welfareRecipient);
         }
      }
   }
   
   @RecorderSource("WelfareCosts")
   public double getGovernmentWelfareCosts() {
      return welfareCosts;
   }
   
   private void settlementFailure(
      final double cashValue, final Object recipient) {
      System.err.println(
         "Government: failed to process a settlment of value " + cashValue + ". Recipient: " + 
         recipient + ", government liquidity: " + cashReserve.getValue() + ".");
   }
   
   /**
     * Instruct the government to remove cash from its reserve.
     * 
     * @returns The amount withdrawn from the government cash reserve.
     */
   public double credit(final double cashAmount) throws InsufficientFundsException {
      if(cashAmount < 0.) {
         System.err.println(
            "Government.credit: argument (value" + cashAmount + ") is negative. No action was "
          + "taken.");
         return 0.;
      }
      if(cashAmount > cashReserve.getValue())
         throw new InsufficientFundsException(
            "Government.credit: cannot remove " + cashAmount + " units of cash. Current cash " +
            "reserve value: " + cashReserve.getValue() + ".");
      cashReserve.setValue(Math.max(cashReserve.getValue() - cashAmount, 0.));
      return cashAmount;
   }
   
   /**
     * Instruct the government to add cash to its reserve.
     */
   public void debit(double cashAmount) {
      if(cashAmount < 0.) {
         System.err.println(
            "Government.debit: argument (value" + cashAmount + ") is negative. No action was "
          + "taken.");
         return;
      }
      cashReserve.setValue(cashReserve.getValue() + cashAmount);
      taxesCollectedAtThisTime += cashAmount;
   }
   
   /**
    * Get the cash reserve value of the government. The cash reserve is kept on the asset
    * side of the government balance sheet.
    * 
    * @return The value of the current government cash reserve.
    */
   public double getCashReserveValue() {
      return cashReserve.getValue();
   }
   
   /**
     * Perform a bailout bankruptcy resolution procedure to the amount specified.
     */
   public double bailout(final double bailoutCashAmount) throws InsufficientFundsException {
      if(bailoutCashAmount < 0.) {
         System.err.println(
            "Government.bailout: argument (value" + bailoutCashAmount + ") is negative. No "
          + "action was taken.");
         return 0.;
      }
      final double amountWithdrawn = credit(bailoutCashAmount);
      totalCashEverSpendOnBailoutProcedures += amountWithdrawn;
      recordBailoutExpense(bailoutCashAmount);
      return amountWithdrawn;
   }
   
   /**
     * Record a bailout intervention (time and total expense incurred).
     */
   private void recordBailoutExpense(final double cashExpense) {
      final double
         timeNow = Simulation.getTime(),
         timeHorizon = timeNow - BAILOUT_MEMORY_TIME_HORIZON;
      bailoutExpenseHistory.add(
         new TimestampedTransaction(Simulation.getTime(), cashExpense));
      while(bailoutExpenseHistory.peekFirst().getFirst() < timeHorizon)
         bailoutExpenseHistory.pollFirst();
   }
   
   /**
     * Get the mean bailout expense per cycle. The time window over which to
     * compute this number is specified by BAILOUT_MEMORY_TIME_HORIZON.
     */
   private double getMeanBailoutCostPerCycle() {
      if(bailoutExpenseHistory.isEmpty()) return 0.;
      double result = 0.;
      for(TimestampedTransaction record : bailoutExpenseHistory)
         result += record.getSecond();
      return result / BAILOUT_MEMORY_TIME_HORIZON;
   }
   
   /**
     * Get the total amount of cash ever spent on bailout procedures.
     */
   public double getTotalLiquiditySpentOnBailouts() {
      return totalCashEverSpendOnBailoutProcedures;
   }
   
   /**
     * Get the total sum the government is prepared to pay in
     * cash for goods aquisition.
     */
   private double getMaximumGoodsExpenseBudget() {
      return totalExpensesBudget * 2./5.;
   }
   
   /**
     * Get the total sum the government is prepared to pay in
     * cash for labour wages.
     */
   private double getMaximumLabourExpenseBudget() {
      return totalExpensesBudget * 2./5.;
   }
   
   /** 
     * Get the total sum the government is prepared to pay in
     * cash for welfare and benefits.
     */
   private double getMaximumWelfareAndBenefitsBudget() {
      return totalExpensesBudget * 1./5.;
   }
   
   @Override
   public void cashFlowInjection(double cashAmount) {
      // No action
   }
   
   @Override
   public MarketResponseFunction getMarketResponseFunction(
      final ClearingMarketInformation marketInformation
      ) {
      return clearingMarketParticipation.getMarketResponseFunction(marketInformation);
   }
   
   /**
     * Get the maximum government gilt cash demand.
     */
   double getMaximumGiltCashDemand() {
      return Math.max(giltDebtTarget - getTotalLiabilities(), 0.);        // TODO: single out bonds
   }
   
   /**
     * Get the number of shares owned in a stock of the given type.
     */
   @Override
   public double getNumberOfSharesOwnedIn(String stockName) {
      StockAccount account = getStockAccount(stockName);
      if(account == null)
         return 0.;
      else return account.getQuantity();
   }
   
   /**
     * @return Does the government own shares in stocks of the given type?
     */
   @Override
   public boolean hasShareIn(String stockName) {
      return (getStockAccount(stockName) != null);
   }
   
   /**
     * @return Get the government stock account, if it exists, in shares of the given type.
     */
   @Override
   public StockAccount getStockAccount(String stockName) {
      return stockAccounts.get(stockName);
   }
   
   /**
     * @return Get an unmodifable view of all existing government stock accounts.
     */
   @Override
   public Map<String, StockAccount> getStockAccounts() {
      return Collections.unmodifiableMap(stockAccounts);
   }
   
   @Override
   public void addAsset(final Contract asset) {
      if(asset instanceof StockAccount)
         stockAccounts.put(((StockAccount)asset).getInstrumentName(), (StockAccount) asset);
   }

   @Override
   public boolean removeAsset(Contract asset) {
       boolean found = false;
       if (asset instanceof StockAccount) {
           found = stockAccounts.containsKey(((StockAccount) asset).getInstrumentName());
           stockAccounts.remove(((StockAccount) asset).getInstrumentName());
       }
       return found;
   }
   
   @Override
   public Set<StockMarket> getStockMarkets() {
      // No non-clearing markets.
      return null;
   }
   
   @Override
   public void registerIncomingDividendPayment(
      final double dividendPayment,
      final double pricePerShare,
      final double numberOfSharesHeld
      ) {
      // No action.
   }
   
   /**
     * Get the tax per unit labour. This method returns a value in the range [0, 1].
     */
   public double getLabourTaxLevel() {
      return labourTaxLevel;
   }
   
   /**
     * Get the tax per unit cash spent on goods. This method returns a value in 
     * the range [0, 1].
     */
   public double getGoodsTaxLevel() {
      return goodsTaxLevel;
   }
   
   /**
     * Get the tax per unit cash for capital gains. This method returns a value
     * in the range [0, 1].
     */
   public double getCapitalGainsTaxLevel() {
      return capitalGainsTaxLevel;
   }
   
   /**
    * Get the tax per unit cash for dividend payments. This method returns a value
    * in the range [0, 1].
    */
   public double getDividendTaxLevel() {
      return getCapitalGainsTaxLevel() * .75; // Dividend payments are also taxed as profits.
   }
      
   /**
     * Attach a household to this government.
     */
   public void addBenefitsRecipient(final Employee employee) {
      Preconditions.checkNotNull(employee);
      welfaceRecipientsInAuthority.put(employee.getUniqueName(), employee);
   }
   
   /**
     * Remove a household from this government.
     */
   public void removeBenefitsRecipient(final Employee employee) {
      Preconditions.checkNotNull(employee);
      welfaceRecipientsInAuthority.remove(employee.getUniqueName());
   }
   
   @Override
   public <T> T accept(final AgentOperation<T> operation) {
      return operation.operateOn(this);
   }
   
   // Employer
   
   @Override
   public void addLabour(Labour labour) {
      currentEmployment.add(labour);
   }

   @Override
   public boolean removeLabour(Labour labour) {
      return currentEmployment.remove(labour);
   }

   @Override
   public double getLabourForce() {
      double totalEmployment = 0.;
      for(final Labour labour : currentEmployment)
         totalEmployment += labour.getQuantity();
      return totalEmployment;
   }

   @Override
   public void disallocateLabour(double size) {
      // No labour allocation.
   }
   
   @Override
   public double getAllocatedCash() {
      return 0;                                                         // No Cash Allocation
   }
   
   @Override
   public double getUnallocatedCash() {
      return cashReserve.getValue();
   }
   
   @Override
   public double allocateCash(double positiveAmount) {
      return positiveAmount;
   }
   
   @Override
   public double disallocateCash(double positiveAmount) {
      return positiveAmount;
   }

   @Override
   public void registerIncomingEmployment(
      final double labourEmployed,
      final double unitLabourWage) {
      // No Action.
   }
   
   // Borrower
   
   @Override
   public void registerNewLoanTransfer(
      final double principalCashValue,
      final Lender lender,
      final double interestRate) {
      // No Action.
   }

   @Override
   public void registerNewLoanRepaymentInstallment(
      final double amountToRepayNow,
      final Lender lender,
      final double interestRate) {
      // No Action.
   }
   
   // Buyer
   
   @Override
   public double calculateAllocatedCashFromActiveOrders() {
      return 0.;
   }
   
   @Override
   public DurableGoodsRepository getGoodsRepository() {
      return goodsRepository;
   }

   @Override
   public void registerIncomingTradeVolume(
      final String goodsType,
      final double goodsAmount,
      final double unitPricePaid
      ) {
      // No Action.
   }
   
   static {
      if(Double.valueOf(INITIAL_TAX_LEVEL) < 0. || Double.valueOf(INITIAL_TAX_LEVEL) > 1.)
         throw new IllegalStateException(
            "Government: initial tax level (value " + INITIAL_TAX_LEVEL + ") must be in " +
            "the range [0, 1].");
      if(Double.valueOf(DEFAULT_INITIAL_EMPLOYMENT_PERCENT_OF_WORKFORCE) < 0. ||
         Double.valueOf(DEFAULT_INITIAL_EMPLOYMENT_PERCENT_OF_WORKFORCE) > 1.)
         throw new IllegalStateException(
            "Government: initial employment target (value " + 
            100.*DEFAULT_INITIAL_EMPLOYMENT_PERCENT_OF_WORKFORCE + "%) must be in " +
            "the range [0, 1].");
      if(Double.valueOf(BAILOUT_MEMORY_TIME_HORIZON) <= 0.)
         throw new IllegalStateException(
            "Government: bailout memory horizon (value " + BAILOUT_MEMORY_TIME_HORIZON
          + " is non-positive.");
   }
   
   /**
     * Returns a brief description of this object. The exact details of the
     * string are subject to change, and should not be regarded as fixed.
     */
   @Override
   public String toString() {
      return "Government, Tax level:" + labourTaxLevel + ", Cash reserve:"
            + cashReserve + ", Total cash spent on bailouts:"
            + totalCashEverSpendOnBailoutProcedures + ", households in "
            + "authority: " + welfaceRecipientsInAuthority.size() + ".";
   }
   
   public int getNumberOfWelfareRecipients(){
	   return welfaceRecipientsInAuthority.size();
   }
   
   public double getTaxRevenue(){
	   return taxesCollectedAtThisTime;
   }
   
   public double getTotalExpensesBudget(){
      return totalExpensesBudget;
   }
   
   /**
     * Set the threshold {@link StockAccount} value at which this {@link Government}
     * agent will disown shares.
     * 
     * @param stockAccountNumberOfSharesAtWhichToDisown
     *        The quantity threshold at which this {@link Government} will disown
     *        shares in any {@link StockAccount} asset. This argument should be
     *        non-negative; otherwise, it silently set to zero.
     */
   @Inject
   public void setStockAccountNumberOfSharesAtWhichToDisown(
   @Named("GOVERNMENT_THRESHOLD_AT_WHICH_TO_DISOWN_SHARES")
      final double stockAccountNumberOfSharesAtWhichToDisown) {
      this.stockAccountNumberOfSharesAtWhichToDisown =
         Math.max(stockAccountNumberOfSharesAtWhichToDisown, 0.);
   }

}
