/*
 * This file is part of CRISIS, an economics simulator.
 * 
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
package eu.crisis_economics.abm.contracts.settlements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class SettlementAdjacencyMatrixXGMML  implements SettlementListener {

	private OutputStreamWriter out;
	private SettlementAdjacencyMatrix a;
	private int count;

	public SettlementAdjacencyMatrixXGMML(String filename) {
		this.a = new SettlementAdjacencyMatrix();;
		this.count = 0;
		try {
			File file = File.createTempFile(filename, ".xgmml");
			System.out.print("created file in "+file.getAbsolutePath());
			OutputStream fstream = new FileOutputStream(file,false); 
			out = new OutputStreamWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
			out.write(String.format("<!-- Created by Olaf Bochmann, %1$tm/%1$te, %1$tY at %1$tl:%1$tM%1$Tp -->\n", new Date()));
			out.write(String.format("<graph label=\"%1s\"\n",filename));
			out.write("    directed=\"1\">\n");
		}
		catch (IOException e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	@Override
   public void finalize () throws Throwable {
		if (out != null) {
			out.write("</graph>\n");
			out.close();
			out = null;
			super.finalize();
		}
	}

	private void writeNodes() {
		for(int i=0; i<a.size(); i++){ 
			if (a.totDegree(i)>0) {
				try {
					out.write(String.format("  <node label=\"%d\" id=\"%d\" start=\"%d\" end=\"%d\">\n",i,i,count,count+1));
					out.write(String.format("    <att name=\"in-degree\" type=\"real\" value=\"%f\" start=\"%d\" end=\"%d\"/>\n",a.inDegree(i),count,count+1));
					out.write(String.format("    <att name=\"out-degree\" type=\"real\" value=\"%f\" start=\"%d\" end=\"%d\"/>\n",a.outDegree(i),count,count+1));
					out.write(String.format("    <att name=\"tot-degree\" type=\"real\" value=\"%f\" start=\"%d\" end=\"%d\"/>\n",a.totDegree(i),count,count+1));
					//						out.write(String.format("    <graphics type=\"%s\" fill=\"#%s\" size=\"%f\" start=\"%d\" end=\"%d\"/>\n",getShape(a.getNode(i)),getColor(a.inDegree(i)/a.maxInDegree(),0,a.outDegree(i)/a.maxOutDegree()),40*(a.inDegree(i)+a.outDegree(i))/(a.maxInDegree()+a.maxOutDegree()),count,count+1));
					out.write("  </node>\n");
				}
				catch (IOException e){
					System.err.println("Error: " + e.getMessage());
				}	
			}
		}
	}

	private void writeEdges() {
		for(int i=0; i<a.size(); i++){ 
			for(int j=0; j<a.size(); j++){ 
				if (a.getValue(i, j)>0.0) {
					try {
						out.write(String.format("  <edge label=\"edge_%d_%d\" source=\"%d\" target=\"%d\" start=\"%d\" end=\"%d\">\n",i,j,i,j,count,count+1));
						out.write(String.format("    <att name=\"weight\" type=\"real\" value=\"%f\" start=\"%d\" end=\"%d\"/>\n",a.getValue(i, j),count,count+1));
						out.write(String.format("    <att name=\"distance\" type=\"real\" value=\"%f\" start=\"%d\" end=\"%d\"/>\n",1/a.getValue(i, j),count,count+1));
						//							out.write(String.format("    <att name=\"color\" type=\"string\" value=\"%s\" start=\"%d\" end=\"%d\"/>\n",getColor(a.inDegree(i)/a.maxInDegree(),0,a.outDegree(i)/a.maxOutDegree()),count,count+1));
						//							out.write(String.format("    <graphics width=\"%f\" start=\"%d\" end=\"%d\"/>\n",3*a.getValue(i, j)/a.maxDegree(),count,count+1));
						//							out.write(String.format("    <graphics fill=\"#%s\" width=\"%f\" start=\"%d\" end=\"%d\"/>\n",getColor(a.getValue(i, j)/a.maxInDegree(),a.getValue(i, j)/a.maxOutDegree(),a.getValue(i, j)/a.maxDegree()),6*a.getValue(i, j),count,count+1));
						//							out.write(String.format("    <graphics transparency=\"%d\" width=\"%f\" start=\"%d\" end=\"%d\"/>\n",(int) Math.round(255*a.getValue(i, j)/a.maxDegree()),6*a.getValue(i, j),count,count+1));
						out.write("  </edge>\n");
					}
					catch (IOException e){
						System.err.println("Error: " + e.getMessage());
					}						
				}
			}
		}			
	}

	@Override
	public void transferDone(SettlementEvent settlementEvent) {
		a.transferDone(settlementEvent);
		writeNodes();
		writeEdges();
		count ++;
	}

	@Override
	public void inverseTransferDone(SettlementEvent settlementEvent) {
		a.inverseTransferDone(settlementEvent);
		writeNodes();
		writeEdges();
		count ++;
	}
}
