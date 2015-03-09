/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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
package eu.crisis_economics.abm.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ai.aitia.meme.gui.Wizard;
import aurelienribon.ui.css.Style;

/**
 * @author Tamás Máhr
 *
 */
public class DWizard extends Wizard {

	/**
	 * This defines which page transitions are allowed. This is used to display visual clue in the info header.
	 */
	private boolean allowedPageTransition[][] = {
			{true, false, false, true},
			{true, true, true, true},
			{true, true, true, true},
			{true, true, true, true}
	};
	
	private static final int INFO_HEADER_STRUT = 15;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected JButton jChartsButton;
	
	protected IWizardPage chartsPage;

	protected JScrollPane scrollPane;

	protected JPanel infoPane;

	protected JLabel messageLabel;
	
	protected Style style;
	
	public DWizard(final Style style){
		super();
		
		this.style = style;
		
		initialize();
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				getRootPane().setDefaultButton(getJNextButton());
			}
		});
	}
	
	private void initialize(){
		// super constructor has added a button panel to the south of this container; we should replace it by a message panel
		
		messageLabel = new JLabel();
		messageLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		Style.registerCssClasses(messageLabel, "#statusMessage");
		
		Box tmp = new Box(BoxLayout.Y_AXIS);
		tmp.add(new javax.swing.JSeparator());
		tmp.add(messageLabel);
		
		
		add(tmp, BorderLayout.SOUTH);
	}
	
	public <T extends IWizardPage> T addChartsPage(T page) {
		chartsPage = page;
		return super.addPage(page);
	}
	
	@Override
   public void actionPerformed(ActionEvent event){
		if (event.getSource() == getJChartsButton()){
			gotoPage(chartsPage);
		} else {
			super.actionPerformed(event);
		}
	}
	
	@Override
	public String getArrowsHeader(String secondLine) {
		return secondLine;
	}
	
	@Override
	protected JButton getJCancelButton() {
		if (jChartsButton == null){
			jChartsButton = super.getJCancelButton();
			jChartsButton.setText("Charts");
		}
		
		return jChartsButton;
	}

	protected JButton getJChartsButton(){
		return getJCancelButton();
	}
	
	public void clickDefaultButton(){
		getRootPane().getDefaultButton().doClick();
	}
	
	@Override
	public JScrollPane getJInfoScrollPane() {
		if (scrollPane == null ){

			infoPane = new JPanel();
			FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, INFO_HEADER_STRUT, INFO_HEADER_STRUT);
			infoPane.setLayout(flowLayout);
			
			scrollPane = new JScrollPane(infoPane);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());

			ImageIcon imageIcon = new ImageIcon(Dashboard.class.getResource("201310_crisis_logo.png"));
			JLabel logo = new JLabel(imageIcon);
			Style.registerCssClasses(logo, "#logo");

			infoPane.add(logo);
			
			scrollPane.getViewport().setView(infoPane);

			Style.registerCssClasses(infoPane, "#infoTextPane");
		}
		
		return scrollPane;
	}
	
	@Override
	public void updateInfo() {
		for (int i = 0 ; i < infoPane.getComponentCount() ; i++){
			Component component = infoPane.getComponent(i);
			if (component instanceof InfoHeaderLabel) {
				InfoHeaderLabel label = (InfoHeaderLabel) component;
				
				Style.unregister(label);
				if (i == 2 * current + 1){
					Style.registerCssClasses(label, ".pageTitle", ".selectedTitle");
				} else {
					Style.registerCssClasses(label, ".pageTitle");
				}

				int target = (i - 1) / 2;
				String title = ((IIconsInHeader)pages.get(target)).getTitle().toLowerCase();
				
				if (allowedPageTransition[current][target]){
					title = underLine(title);
				}
				label.setText(title);
			}
		}
		
		Style.apply(infoPane, style);
		
		messageLabel.setText(pages.get(current).getInfoText(this));
	}

	/**
	 * This method populates the info panel with the titles of the pages added to this wizard. This can be called multiple times, if new pages added in
	 * the meanwhile only those will be added to the info panel.
	 */
	public void populateInfoPane(){
		if (pages.size() * 2 != infoPane.getComponentCount()){
			
			for (int i = infoPane.getComponentCount() - 1 ; i < pages.size() ; i++){
				IWizardPage page = pages.get(i);
//			for (IWizardPage page : pages) {
				if (page instanceof IIconsInHeader) {
					IIconsInHeader pageWithIcons = (IIconsInHeader) page;
					String title = pageWithIcons.getTitle().toLowerCase();
					
					if (i != 0){
						JLabel arrowLabel = new JLabel(">");
						Style.registerCssClasses(arrowLabel, ".pageTitleArrow");
						infoPane.add(arrowLabel);
					}
					
					if (allowedPageTransition[current][i]){
						title = underLine(title);
					}
					
					final InfoHeaderLabel label = new InfoHeaderLabel(title, pageWithIcons.getIcon());
					Style.registerCssClasses(label, ".pageTitle");
					if (i == current){
						Style.registerCssClasses(label, ".selectedTitle");
					}

					label.setFocusable(true);
					label.setCursor(new Cursor(Cursor.HAND_CURSOR));
					final int pageIndex = i;
					label.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							
							if (pageIndex == current){
								return;
							}
							
							IWizardPage page = pages.get(current);
							Button button = null;
							
							if (pageIndex < current){
								button = Button.BACK;
							}
							
							if (pageIndex > current){
								button = Button.NEXT;
							}
							
							if (pageIndex == pages.size() - 1){
								button = Button.CANCEL; // this is the charts page
							}
							
							if (page.isEnabled(button) && page.onButtonPress(button)){
								gotoPage(pageIndex);
							}

							if (pageIndex == 0){
								gotoPage(pageIndex);
							}
						}
					});
					
					infoPane.add(label);
				}
			}
		}
	}

	private String underLine(String title) {
		return "<html><u>" + title + "</u></html>";
	}
	
//	protected JComponent getJInfoPane(){
//		JPanel panel = new JPanel();
//		
//		JLabel arrowLabel = null;
//		
//		ImageIcon imageIcon = new ImageIcon(Dashboard.class.getResource("201310_crisis_logo.png"));
//		JLabel logo = new JLabel(imageIcon);
//		Style.registerCssClasses(logo, "#logo");
//		panel.add(logo);
//		
//		for (IWizardPage page : pages) {
//			if (page instanceof IArrowsInHeader) {
//				IArrowsInHeader pageWithArrows = (IArrowsInHeader) page;
//				String title = pageWithArrows.getTitle();
//				
//				JLabel label = new JLabel(title);
//				Style.registerCssClasses(label, ".pageTitle");
//				
//				panel.add(label);
//				
//				arrowLabel = new JLabel(">");
//				Style.registerCssClasses(arrowLabel, ".pageTitleArrow");
//				
//				panel.add(arrowLabel);
//			}
//		}
//		
//		if (arrowLabel != null){
//			panel.remove(arrowLabel);
//		}
//		
//		return panel;
//	}
	
//	@Override
//	public void paint(Graphics g) {
//		super.paint(g);
//		
//		Style.apply(this, style);
//	}
	
	@Override
	public void gotoPage(int page) {
		super.gotoPage(page);

		Style.apply(this, style);
	}
}
