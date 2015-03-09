/*
 * This file is part of CRISIS, an economics simulator.
 * 
 * Copyright (C) 2015 AITIA International, Inc.
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * Utilities for using the JGoodies Forms package (forms-1.0.7.jar). 
 * The documentation here provides only a few examples and assumes a basic 
 * knowledge of FormLayout's HTML documentation. (I think it's enough to read
 * the "Quick start" and "Reference" parts (including Alignments, Constant Sizes,
 * Component Sizes, Bounded Sizes, Cols & Rows, Cell Constraints, Groups))
 * <br>
 * History:<ul>
 * <li> 2007-03-28 Comments improved
 * <li> 2007-02-28 Initial version
 * </ul>
 */
public class FormsUtils
{
	public static boolean isDebug = false;
	//public static boolean isDebug = true;

	//=========================================================================
	//	buttonStack()

	public static final String BS_UNRELATED	= "";		// unrelated buttons marker for buttonStack() 
	public static final String BS_GLUE		= "glue";	// glue marker for buttonStack()

	//-------------------------------------------------------------------------
	/**
	 * Example:<pre>
	 * 	buttonStack( jMoveUpButton, jMoveDownButton, FormsUtils.BS_UNRELATED, 
	 *	             jRemoveButton, jEditButton ).getPanel()
	 * </pre>
	 */
	public static ButtonStackBuilder buttonStack(Object ... args) {
		ButtonStackBuilder ans = new ButtonStackBuilder();
		boolean addrel = false;
		for (int i = 0; i < args.length; ++i) {
			if (args[i] instanceof javax.swing.JComponent) {
				if (addrel)
					ans.addRelatedGap();
				ans.addGridded((javax.swing.JComponent)args[i]);
				addrel = true;
			}
			else if (BS_UNRELATED.equals(args[i])) {
				ans.addUnrelatedGap();
				addrel = false;
			}
			else if (BS_GLUE.equals(args[i])) {
				ans.addGlue();
				addrel = false;
			}
			else {
				ans.addStrut(Sizes.constant(args[i].toString(), false)); 
				addrel = false;
			}
		}
		return ans;
	}


	//=========================================================================
	//	build()

	/** 
	 * Definition of gap length characters. Must contain constant lengths only (e.g. "4dlu").
	 */
	public static java.util.HashMap<Character, String> gapLength = new java.util.HashMap<Character, String>();
	static { 
		// Factory default gap lengths
		// These characters can be used: ' ` - = ~ / & % $ \\ ^ * # > @
		gapLength.put('`',  "1dlu");
		gapLength.put('\'', "2dlu");
		gapLength.put('-',  "4dlu");
		gapLength.put('~',  "6dlu");
		gapLength.put('=',  "8dlu");
		gapLength.put('%',  "10dlu");
		gapLength.put('$',  "15dlu");
		gapLength.put('\\',  "20dlu");
	}

	//-------------------------------------------------------------------------
	/**
	 * Returns a DefaultFormBuilder containing the specified components and layout.
	 * @param cols This parameter corresponds to the <code>encodedColumnSpecs</code> 
	 *   parameter of FormLayout's ctor. Besides the encoding defined by FormLayout, 
	 *   the following extensions are also available: the characters defined in the 
	 *   global <code>{@link #gapLength}</code> variable (hereafter: gap-characters) 
	 *   can be used to insert gap-columns. Gap columns must not appear in the 
	 *   cell-specification (explained below) and they're automatically included in
	 *   column spans. 
	 *   Consecutive gap-characters are coalesced into 1 gap column by calculating
	 *   their cumulated pixel size.
	 * @param rows A string describing general builder settings + cell-specification 
	 *   + row/colum spans + row heights + row groups. See the examples. 
	 *   The digits and underscores specify which component goes into which cell(s) 
	 *   of the layout grid (cell-specification). There can be at most one character 
	 *   for every (non-gap) column specified by <code>cols</code>. Rows must be 
	 *   separated by the '|' character. Only underscores, digits and letters are 
	 *   allowed in the cell-specification (space isn't). Underscore means that a 
	 *   cell is empty. A digit/letter character refers to a component in the varargs 
	 *   list: '0'..'9', 'A'..'Z', 'a'..'z' (in this order) denote the first 62 
	 *   components of the <code>args</code> list. Repeating the same digit specifies
	 *   the component's column span (and row span, if repeated in consecutive rows
	 *   in the same columns, like '3' in the example).<br> 
	 *   After the cell-specification, but before the newline ('|') character
	 *   the row height and row grouping can also be specified. It must begin 
	 *   with a space to separate it from the cell-specification. The row
	 *   height can be a gap-character (for constant heights only) or a string
	 *   that is interpreted by RowSpec.decodeSpecs(). If omitted, the height 
	 *   spec. of the most recent row is inherited. Content rows inherit the 
	 *   height of the previous content row, gap rows inherit the height of 
	 *   the previous gap row. A row is a gap row if its cell-specification is
	 *   omitted.<br> 
	 *   Row grouping forces equal heights to every member of the group. It  
	 *   can be specified by "grp?" strings using any character in place of
	 *   '?' (except space and '|'. In the example 'grp1' uses '1'). Rows 
	 *   using the same grouping character will be in the same group.
	 *   By default there're no groups.
	 *	 <br>
	 *   General builder-settings can be specified at the beginning of the 
	 *   string, enclosed in square brackets ([..]). (No space is allowed
	 *   after the closing ']'). This is intended for future extensions, too. 
	 *   The list of available settings is described at the {@link Prop} 
	 *   enumeration. Note that setting names are case-sensitive, and should 
	 *   be separated by commas.
	 * @param args List of components. Besides java.awt.Component objects,
	 *   the caller may use plain strings and instances of the {@link Separator}
	 *   class. Plain strings are used to create labels (with mnemonic-expansion). 
	 *   Separator objects will create and add separators to the form. 
	 *   Any of these objects may be followed optionally by a {@link CellConstraints},
	 *   a {@link CellConstraints.Alignment} or a {@link CellInsets} object, 
	 *   which overrides the cell's default alignment, can extend its row/column 
	 *   span and adjust its insets.<br>
	 *   If the first element of <code>args</code> is a java.util.Map object,
	 *   it is treated as an additional mapping for gap-characters. This 
	 *   overrides the default global mapping. Note that gap-characters can 
	 *   help you to set up uniform spacing on your forms. For example, if
	 *   you use "-" as normal column-gap and "~" as normal row-gap, fine-tuning
	 *   the sizes of these gaps later is as easy as changing the mapping for "-"
	 *   and "~" &mdash; there's no need to update all the dlu sizes in all layouts.
	 * @see   
	 *   Example1: <pre>
 	 *       build("6dlu, p, 6dlu, 50dlu, 6dlu", 
 	 *       			"_0_1_ pref| 6dlu|" + 
	 *       			"_2_33 pref:grow(0.5) grp1||" +
	 *       			"_4_33 grp1",
	 *       			component0, component1, component2, component3,
	 *       			component4, cellConstraintsForComponent4).getPanel()
	 * </pre>
	 *   The same exaple with gap-characters: <pre>
 	 *       build("~ p ~ 50dlu, 6dlu", 
 	 *       			"01_ pref|~|" + 
	 *       			"233 pref:grow(0.5) grp1||" +
	 *       			"433 grp1",
	 *       			component0, component1, component2, component3,
	 *       			component4, cellConstraintsForComponent4).getPanel()
	 * </pre>
	 *   Example3 (only the second argument): <pre>
	 *       "[LineGapSize=6dlu, ParagraphGapSize=20dlu]_0_1||_2_3||_4_5"
	 * </pre>
	 *  Note: this method can be used with no components and empty cell-specification,
	 *  too. In this case only a {@link DefaultFormBuilder} is created, configured 
	 *  and returned. Its operations can then be used to append components to the form.
	 */
	@SuppressWarnings("unchecked")
	public static DefaultFormBuilder build(String cols, String rows, Object... args) 
	{
		Context ctx = new Context();

		// Parse column widths
		//
		int firstArg = 0;
		if (args.length > 0 && args[0] instanceof java.util.Map) {
			ctx.localGapSpec = (java.util.Map<Character, String>)args[0];
			firstArg += 1;
		}
		StringBuilder colstmp = new StringBuilder();
		ctx.contentCol = parseColumnWidths(colstmp, cols, ctx.localGapSpec, 0);

		// Parse the list of components (may include individual cell-constraints)
		//
		ctx.components = new ArrayList<Rec>(args.length);
		for (int i = firstArg; i < args.length; ++i) {
			Rec r = new Rec(args[i]);
			if (i+1 < args.length) {
				if (args[i+1] instanceof CellConstraints) {
					r.cc = (CellConstraints)args[++i];
					r.useAlignment = true;
				}
				else if (args[i+1] instanceof CellConstraints.Alignment) {
					CellConstraints.Alignment a = (CellConstraints.Alignment)args[++i];
					if (a == CellConstraints.BOTTOM || a == CellConstraints.TOP)
						r.cc = new CellConstraints(1, 1, CellConstraints.DEFAULT, a);
					else if (a == CellConstraints.LEFT || a == CellConstraints.RIGHT)
						r.cc = new CellConstraints(1, 1, a, CellConstraints.DEFAULT);
					else if (a == CellConstraints.CENTER || a == CellConstraints.FILL)
						r.cc = new CellConstraints(1, 1, a, a);
					r.useAlignment = (r.cc != null);
				}
				else if (args[i+1] instanceof CellInsets) {
					CellInsets ci = ((CellInsets)args[++i]);
					r.cc = ci.cc;
					r.useAlignment = ci.useAlignment;
					r.useInsets = true;
				//}
				//else if (args[i+1] == null) {	// this would allow superfluous 'null' values
				//	i += 1;
				}
			}
			ctx.components.add(r);
		}

		// Parse general settings (but don't apply yet) 
		//
		EnumMap<Prop, Object> props = null;
		int i = rows.indexOf(']');
		if (i >= 0) {
			String defaults = rows.substring(0, i);
			rows = rows.substring(++i);
			i = defaults.indexOf('[');
			ctx.input = defaults.substring(++i);
			props = Prop.parseGeneralSettings(ctx);
		}

		// Parse cell-specification, row heights and row groups
		//
		String cells[] = rows.split("\\|", -1);
		StringBuilder rowstmp = new StringBuilder();
		java.util.HashMap<Character, int[]> rowGroups = new HashMap<Character, int[]>();
		String lastContentRowHeight = "p", lastGapRowHeight = null;
		int rowcnt = 0;
		for (i = 0; i < cells.length; ++i) {
			rowcnt += 1;
			// See if it begins with a gap-character
			String g = (cells[i].length() > 0) ? getGap(cells[i].charAt(0), ctx.localGapSpec) : null;
			if (g != null) cells[i] = ' ' + cells[i];
			int j = cells[i].indexOf(' ');
			boolean gapRow = (j == 0) || (cells[i].length() == 0);
			String rh = null;
			if (j >= 0) {
				String tmp[] = cells[i].substring(j+1).split("\\s");	// expect height and grouping specifications 
				cells[i] = cells[i].substring(0, j);
				ArrayList<String> gaps = new ArrayList<String>();
				for (j = 0; j < tmp.length; ++j) {
					if (tmp[j].length() == 0) continue;
					if (tmp[j].length() == 4 && tmp[j].toLowerCase().startsWith("grp")) {
						Character groupch = tmp[j].charAt(3);
						rowGroups.put(groupch, appendIntArray(rowGroups.get(groupch), rowcnt));
					} else {
						rh = tmp[j];
						for (int k = 0, n = tmp[j].length();
								k < n && addGap(gaps, getGap(tmp[j].charAt(k), ctx.localGapSpec)); ++k)
							;
					}
				}
				if (!gaps.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					flushGaps(gaps, sb, false);
					rh = sb.substring(0, sb.length() - 1);
				}
			}
			if (rh == null) {
				if (gapRow && lastGapRowHeight == null) {
					ctx.b = new DefaultFormBuilder(new FormLayout(colstmp.toString(), ""));
					Prop.setBuilder(props, ctx);
					lastGapRowHeight = parseableRowSpec(ctx.b.getLineGapSpec()); 
				}
				rh = gapRow ? lastGapRowHeight : lastContentRowHeight;
			} else {
				if (gapRow) lastGapRowHeight = rh;
				else lastContentRowHeight = rh;
			}
			if (i > 0) rowstmp.append(',');
			rowstmp.append(rh);
		}

		// Create builder
		//
		FormLayout fml = new FormLayout(colstmp.toString(), rowstmp.toString());
		ctx.b = new DefaultFormBuilder(fml, debuggable());

		// Apply builder settings (e.g. column groups)
		//
		Prop.setBuilder(props, ctx);
		props = null;

		// Set row groups
		//
		if (!rowGroups.isEmpty()) {
			int[][] tmp = new int[rowGroups.size()][];											// ???
			i = 0; for (int[] a : rowGroups.values()) tmp[i++] = a;
			fml.setRowGroups(tmp);
		}
		rowGroups = null;

		JLabel lastLabel = null;
		java.util.HashSet<Character> done = new java.util.HashSet<Character>(ctx.components.size());
		int h = cells.length;
		for (int y = 0; y < cells.length; ++y) {
			int w = cells[y].length();
			int first = -1;
			for (int x = 0; x < w; ++x) {
				char ch = cells[y].charAt(x);
				if (ch == '_' || done.contains(ch)) continue;
				int idx = intValue(ch);

				Rec rec;
				try {
					rec = ctx.components.get(idx);
				} catch (IndexOutOfBoundsException e) {
					throw new IndexOutOfBoundsException(String.format("build() cells=\"%s\" ch=%c rows=\"%s\"", cells[y], ch, rows));
				}
				CellConstraints cc = (rec.cc == null) ? new CellConstraints() : (CellConstraints)rec.cc.clone();

				int sx = cc.gridWidth, sy = cc.gridHeight;	// span x, span y
				while (x+sx < w && cells[y].charAt(x+sx) == ch) sx += 1;
				while (y+sy < h && (	(x < cells[y+sy].length() && cells[y+sy].charAt(x) == ch) ||	
										(cells[y+sy].length()==0 && y+sy+1 < h && 
											x < cells[y+sy+1].length() && cells[y+sy+1].charAt(x) == ch)
									)) { 
					sy += 1;
				}
				int colSpan = ctx.contentCol[x+sx-1] - ctx.contentCol[x] + 1;
				ctx.b.setBounds(ctx.contentCol[x]+1, ctx.b.getRow(), colSpan, sy);
				ctx.b.setLeadingColumnOffset(first & ctx.contentCol[x]);		// 0 vagy x (itt nem kell a +1)
				first = 0;
				x += (sx - 1);

				Object comp = ctx.components.get(idx).component;
				if (comp instanceof Component) {
					ctx.b.append((Component)comp, colSpan);
					if (comp instanceof JLabel)
						lastLabel = (JLabel)comp;
					else {
						if (lastLabel != null) lastLabel.setLabelFor((Component)comp);
						lastLabel = null;
					}
				} else if (comp instanceof Separator) {
					comp = ctx.b.appendSeparator(comp.toString());
					lastLabel = null;
				} else {
					comp = lastLabel = ctx.b.getComponentFactory().createLabel(comp.toString());
					ctx.b.append(lastLabel, colSpan);
				}
				if (rec.useAlignment || rec.useInsets) {
					CellConstraints cc2 = fml.getConstraints((Component)comp);
					cc2.insets = cc.insets;
					cc2.hAlign = cc.hAlign;
					cc2.vAlign = cc.vAlign;
					fml.setConstraints((Component)comp, cc2);
				}

				done.add(ch);
			}
			lastLabel = null;
			ctx.b.nextLine();
		}
		return ctx.b;
	}

	//-------------------------------------------------------------------------
	public static class Separator {
		String title;
		public 						Separator(String title)	{ this.title = title; }
		@Override public String		toString()				{ return title; }
	}
	
	//-------------------------------------------------------------------------
	public static class CellInsets {
		CellConstraints cc;
		boolean			useAlignment = false;
		public CellInsets(int px_top, int px_left, int px_bottom, int px_right) {
			this(px_top, px_left, px_bottom, px_right, (String)null);
		}
		/** @param c used for dlu comptation */ 
		public CellInsets(int dlu_top, int dlu_left, int dlu_bottom, int dlu_right, Component c) {
			this(dlu_top, dlu_left, dlu_bottom, dlu_right, (String)null, c);
		}
		public CellInsets(int px_top, int px_left, int px_bottom, int px_right, String alignments) {
			useAlignment = (alignments != null);
			cc = (useAlignment) ? new CellConstraints("1,1," + alignments) : new CellConstraints();
			cc.insets = new java.awt.Insets(px_top, px_left, px_bottom, px_right);
		}
		/** @param c used for dlu comptation */ 
		public CellInsets(int dlu_top, int dlu_left, int dlu_bottom, int dlu_right, String alignments, Component c) {
			this(Sizes.dialogUnitYAsPixel(dlu_top, c),
				Sizes.dialogUnitXAsPixel(dlu_left, c),
				Sizes.dialogUnitYAsPixel(dlu_bottom, c), 
				Sizes.dialogUnitXAsPixel(dlu_right, c), alignments);
		}
	}

	//-------------------------------------------------------------------------
	public static <B extends PanelBuilder> B titledPanel(String title, B pb) {
		titledBorder(title, pb.getPanel());
		return pb;
	}

	//-------------------------------------------------------------------------
	public static <B extends javax.swing.JComponent> B titledBorder(String title, B comp) {
		comp.setBorder(BorderFactory.createTitledBorder(title));
		return comp;
	}

	//-------------------------------------------------------------------------
	public static <B extends javax.swing.JComponent> B emptyBorder(String encodedSizes, B comp) {
		comp.setBorder(Borders.createEmptyBorder(decodeGapChars(encodedSizes, true, null)));
		return comp;
	}

	//-------------------------------------------------------------------------
	public static JPanel debuggable() {
		return (isDebug) ? new FormDebugPanel() : new JPanel();
	}

	//-------------------------------------------------------------------------
	public static String decodeGapChars(String s, boolean horizontal, java.util.Map<Character, String> localGapSpec) {
		StringBuilder sb = new StringBuilder();
		parseColumnWidths(sb, s, localGapSpec, (horizontal ? 0 : 1) | 2 | 4);
		return sb.toString();
	}

	//-------------------------------------------------------------------------
	static class Rec {
		Object			component;
		CellConstraints	cc = null;
		boolean			useAlignment = false;
		boolean			useInsets = false;
		Rec(Object comp) { component = comp; }
	}

	//-------------------------------------------------------------------------
	public static enum Prop { 
		/** Example: "[LineGapSize=3dlu]" <br>It must be a constant value */
		LineGapSize {
			@Override Object parseGeneralSetting(Context c)	{ return Sizes.constant(c.input, false); }
			@Override void setBuilder(Object value, Context c) { c.b.setLineGapSize((ConstantSize)value); }
		},
		/** Example: "[ParagraphGapSize=6dlu]" <br>It must be a constant value */
		ParagraphGapSize {
			@Override Object parseGeneralSetting(Context c)	{ return Sizes.constant(c.input, false); }
			@Override void setBuilder(Object value, Context c)	{ c.b.setParagraphGapSize((ConstantSize)value); }
		},
		/** Example: "[DialogBorder]" */
		DialogBorder {
			@Override void setBuilder(Object value, Context c)	{ c.b.setDefaultDialogBorder(); }
		},
		/** Example: "[EmptyBorder=0~0~]". Top left bottom right sizes spearated by spaces. */
		EmptyBorder {
			@Override Object parseGeneralSetting(Context c)	{ return decodeGapChars(c.input, true, c.localGapSpec); }
			@Override void setBuilder(Object value, Context c)	{ emptyBorder(value.toString(), c.b.getPanel()); }
		},
		/** Example: "[ColGroups=01-23]". Gap-columns must be counted. */
		ColGroups { 
			@Override
			Object parseGeneralSetting(Context c) {
				StringTokenizer t = new StringTokenizer(c.input, "-");
				int n = t.countTokens(), colGroups[][] = new int[n][];
				for (int i = 0; i < n; ++i) {
					String s = t.nextToken();
					int sl = s.length();
					colGroups[i] = new int[sl];
					while (--sl >= 0) colGroups[i][sl] = intValue(s.charAt(sl));
				}
				return colGroups;
			}
			
			@Override
			void setBuilder(Object value, Context c)	{
				c.b.getLayout().setColumnGroups((int[][])value);
			}
		};

		// Default implementation
		Object		parseGeneralSetting(Context c)		{ return null; }
		void		setBuilder(Object value, Context c)	{}

		// The string to be parsed is expected in 'c.input'. This string may be replaced. 
		static EnumMap<Prop, Object> parseGeneralSettings(Context c) {
			EnumMap<Prop, Object> ans = new EnumMap<Prop, Object>(Prop.class);
			String str = c.input;
			W: while (str != null && str.length() > 0) {
				for (Prop p : values()) {
					if (str.startsWith(p.name())) {
						int i = str.indexOf(','), j = p.name().length();
						if (i < 0) i = str.length();
						if (j < str.length() && str.charAt(j) == '=') j += 1;
						c.input = str.substring(j, i);
						ans.put(p, p.parseGeneralSetting(c));
						str = str.substring(i).replaceFirst("[,\\s]*", "");
						continue W;
					}
				}
				break;
			}
			return ans;
		}

		static void setBuilder(EnumMap<Prop, Object> settings, Context c) {
			if (settings != null) 
				for (Entry<Prop, Object> e : settings.entrySet())
					e.getKey().setBuilder(e.getValue(), c);
		}
	};

	//-------------------------------------------------------------------------
	static class Context {
		String								input			= "";
		java.util.Map<Character, String>	localGapSpec	= null;
		ArrayList<Rec>						components		= null;
		int[]								contentCol		= null;
		DefaultFormBuilder					b				= null;
	};

	//-------------------------------------------------------------------------
	/** Converts a RowSpec object to parseable string format */
	public static String parseableRowSpec(RowSpec rowspec) {
		return parseableSize(rowspec.toString()).replace(":noGrow", "");
	}

	//-------------------------------------------------------------------------
	/** Converts a Size object (e.g. ConstantSize) to parseable string format */
	public static String parseableSize(String sizeStr) {
		return sizeStr.replaceAll("dlu[XY]", "dlu");
	}

	//-------------------------------------------------------------------------
	public static int[] appendIntArray(int[] array, int value) {
		if (array == null || array.length == 0)
			return new int[] { value };
		int[] ans = new int[array.length + 1];
		System.arraycopy(array, 0, ans, 0, array.length);
		ans[array.length] = value;
		return ans;
	}

	//-------------------------------------------------------------------------
	public static int intValue(char ch) {
		int ans = Integer.parseInt(String.valueOf(ch), 36);
		if (ch > 'Z') ans += 26;	// lowercase letters mean higher values
		return ans;
	}

	//-------------------------------------------------------------------------
	public static String getGap(Character ch, java.util.Map<Character, String> local) {
		String ans = (local != null) ? local.get(ch) : null;
		if (ans == null)
			ans = gapLength.get(ch);
		return ans;
	}
	
	//-------------------------------------------------------------------------
	private static int[] parseColumnWidths(StringBuilder						colstmp,
											 String								cols,
											 java.util.Map<Character, String>	localGapSpec,
											 int								flags)
	{
		boolean horizontal				= (flags & 1) == 0;
		boolean	 allowAdjacentGapCols	= (flags & 2) != 0;
		boolean	 returnContentCols		= (flags & 4) == 0;

		java.util.Set<Character> gapChars = gapLength.keySet();
		if (localGapSpec != null) {
			gapChars = new java.util.HashSet<Character>(gapChars);
			gapChars.addAll(localGapSpec.keySet());
		}
		StringBuilder gapcharstr = new StringBuilder(gapChars.size());
		for (Character ch : gapChars) gapcharstr.append(ch);
		gapcharstr.append(", ");

		colstmp.setLength(0);
		int contentCol[] = null;		// contentCol[content_column] == FormLayout_column 
		int nCols = 0;					// number of FormLayout-columns (includes gap-columns)
		StringTokenizer t = new StringTokenizer(cols, gapcharstr.toString(), true);
		ArrayList<String> gaps = new ArrayList<String>();
		while (t.hasMoreTokens()) {
			String s = t.nextToken();
			if (s.length() == 1) {
				char ch = s.charAt(0);
				if (ch == ' ' || ch == ',') {
					if (!allowAdjacentGapCols) continue;
				}
				else if (addGap(gaps, getGap(ch, localGapSpec)))
					continue;
			}
			nCols += flushGaps(gaps, colstmp, true);
			if (s != null) {
				if (returnContentCols)
					contentCol = appendIntArray(contentCol, nCols++); 
				colstmp.append(s);
				colstmp.append(',');
			}
		}
		nCols += flushGaps(gaps, colstmp, horizontal);
		int l = colstmp.length();
		if (l > 0 && colstmp.charAt(l-1) == ',') colstmp.setLength(l-1);
		return contentCol;
	}
	
	//-------------------------------------------------------------------------
	private static boolean addGap(ArrayList<String> gaps, String gap) {
		if (gap == null)
			return false;
		gaps.add(gap);
		return true;
	}

	//-------------------------------------------------------------------------
	private static int flushGaps(ArrayList<String> gaps, StringBuilder colstmp, boolean horizontal) {
		if (gaps.isEmpty())
			return 0;
		if (gaps.size() == 1) {
			colstmp.append(gaps.get(0));
			colstmp.append(',');
		}
		else {
			int pixelSize = 0;
			for (String size : gaps) {
				// Ide a 'null' helyere erdemes lenne bejuttatni egy GUI komponenst, ha lehet
				pixelSize += Sizes.constant(size, horizontal).getPixelSize(null);
			}
			colstmp.append(pixelSize);
			colstmp.append("px,");
		}
		gaps.clear();
		return 1;
	}
}
