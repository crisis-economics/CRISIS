/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Daniel Tang
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
package eu.crisis_economics.abm.markets.nonclearing;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import eu.crisis_economics.abm.contracts.DoubleEmploymentException;
import eu.crisis_economics.abm.contracts.Employee;
import eu.crisis_economics.abm.contracts.Employer;
import eu.crisis_economics.abm.contracts.Labour;
import eu.crisis_economics.abm.simulation.NamedEventOrderings;
import eu.crisis_economics.abm.simulation.Simulation;

public class LabourInstrument extends Instrument {
   private static final long serialVersionUID = -5790824107217341471L;
   private int maturity;

    public LabourInstrument(final int maturity, final BlockingQueue<Order> updatedOrders) {
        super(generateTicker(maturity), updatedOrders);
        this.maturity = maturity;
        scheduleSelf();
    }
    
    public LabourInstrument(final int maturity, final BlockingQueue<Order> updatedOrders, final MatchingMode matchingMode,
            final Set<InstrumentListener> listener) {
        super(generateTicker(maturity), updatedOrders, matchingMode, listener);
        this.maturity = maturity;
        scheduleSelf();
        
    }
    
    private void scheduleSelf() {
        Simulation.repeat(this, "cancelAllOrders", NamedEventOrderings.BEFORE_ALL);
    }
    
    public static String generateTicker(final int maturity) {
        return "LABOUR"+new Integer(maturity).toString();
    }

    @Override
    protected void setupContract(Order askOrder, Order bidOrder, double quantity, double wage) {
        
        try {
        	Labour newLabour = Labour.create(
               (Employer)askOrder.getParty(), 
               (Employee)bidOrder.getParty(), 
               quantity, 
               this.getMaturity(), 
               wage
               );
            newLabour.getEmployee().disallocateLabour(quantity);
        } catch (DoubleEmploymentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getMaturity() {
        return maturity;
    }

}
