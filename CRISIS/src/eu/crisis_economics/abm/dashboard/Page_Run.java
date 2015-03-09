/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
 * Copyright (C) 2015 John Kieran Phillips
 * Copyright (C) 2015 Ariel Y. Hoffman
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
package eu.crisis_economics.abm.dashboard;

import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ai.aitia.meme.gui.Wizard;
import ai.aitia.meme.gui.Wizard.Button;
import ai.aitia.meme.gui.Wizard.IWizardPage;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * @author Tamás Máhr
 *
 */
public class Page_Run implements IWizardPage, IIconsInHeader {

	private static final String PAUSE_BUTTON_TEXT_PAUSE = "Pause";

	private static final String PAUSE_BUTTON_TEXT_CONTINUE = "Continue";

	private static final int DECORATION_IMAGE_WIDTH = 300;
	
	private static final String DECORATION_IMAGE = "blue-butterfly-485509-m.png";

	private static final String TITLE = "Run";

	private static final String INFO_TEXT = "Please wait for the simulation to end!";

	private Dashboard dashboard;
	
	private Wizard wizard;
	
	private Container container;

	private JProgressBar progressBar;

	private JButton pauseButton;
	
	private JLabel iterationLabel;
	
	/**
	 * 
	 */
	public Page_Run(final Wizard wizard, final Dashboard dashboard) {
		this.dashboard = dashboard;
		this.wizard = wizard;
		
		container = initContainer();
	}

	/** {@inheritDoc} 
	 */
	@Override
	public Container getPanel() {
		return container;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public String getInfoText(Wizard w) {
		return w.getArrowsHeader(INFO_TEXT);
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("icons/run.png"));
	}
	
	/** {@inheritDoc} 
	 */
	@Override
	public boolean isEnabled(Button b) {
		switch (b) {
		case BACK:
			return true;

		case NEXT:
			return false;
			
		case CANCEL: // this is the charts page
			return true;
			
		case FINISH:
			return false;
		
		default:
			break;
		}
		return false;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public boolean onButtonPress(Button b) {
		if (b == Button.BACK){
			if (dashboard.isRunning()){
				dashboard.interruptRun();
			}
		}
		return true;
	}

	/** {@inheritDoc} 
	 */
	@Override
	public void onPageChange(boolean show) {
		if (show){
			dashboard.getSaveConfigMenuItem().setEnabled(true);

			if (!dashboard.isRunning()) {
				iterationLabel.setText(dashboard.isGARun() ? "<html><b>Iteration 1</b></html>" : "");
				dashboard.runModel();
				if (dashboard.isOnLineCharts()){
					wizard.gotoPageRel(1);
				}
			}
		} else {
			setProgress(0);
		}
	}

	private Container initContainer() {
		JLabel label = new JLabel(new ImageIcon(new ImageIcon(getClass().getResource(DECORATION_IMAGE)).getImage().getScaledInstance(DECORATION_IMAGE_WIDTH, -1, Image.SCALE_SMOOTH)));
		
		iterationLabel = new JLabel();
		final Font font = iterationLabel.getFont();
		iterationLabel.setFont(new Font(font.getName(),Font.PLAIN,font.getSize() * 2));
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		pauseButton = new JButton(PAUSE_BUTTON_TEXT_PAUSE);
		pauseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (PAUSE_BUTTON_TEXT_PAUSE.equals(pauseButton.getText())){
					pauseButton.setText(PAUSE_BUTTON_TEXT_CONTINUE);
				} else {
					pauseButton.setText(PAUSE_BUTTON_TEXT_PAUSE);
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(pauseButton);
		
		return FormsUtils.build("d:g",
				"0 p:g |" +
				"1 p:g |" +
				"2 p:g |" +
				"3", 
				iterationLabel, new CellConstraints(1,1,CellConstraints.CENTER,CellConstraints.BOTTOM), 
				progressBar, CellConstraints.CENTER,
				label, CellConstraints.CENTER, buttonPanel).getPanel();

	}
	
	public void setProgress(final int progress) {
		progressBar.setValue(progress);
	}
	
	public void setIteration(final long iteration) {
		if (iteration > 0)
			iterationLabel.setText("<html><b>Iteration " + iteration + "</b></html>");
		else
			iterationLabel.setText("");
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
}