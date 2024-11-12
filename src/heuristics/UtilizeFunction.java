

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class UtilizeFunction {
	/**
	 * Returns a psuedo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimim value
	 * @param max Maximim value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	static BufferedReader in;
	static MyGraph g;
	public static boolean isPositive(Vector<Double> k)
	{
		for (int i=0;i<k.size();i++)
			if(k.get(i)<=0)
				return false;
		return true;
	}
	public static Vector<Double> add(Vector<Double> a, Vector<Double> b)
	{
		Vector<Double> temp = new Vector<Double>();
		if(a.size()!=b.size()) return null;
		for (int i=0;i<a.size();i++)
			temp.addElement(a.get(i)+b.get(i));
		return temp;
	}
	public static Vector<Double> minus(Vector<Double> a, Vector<Double> b)
	{
		Vector<Double> temp = new Vector<Double>();
		if(a.size()!=b.size()) return null;
		for (int i=0;i<a.size();i++)
			temp.addElement(a.get(i)-b.get(i));
		return temp;
	}
	public static double multi(Vector<Double> a, Vector<Double> b)
	{
		double temp=0.0;
		if(a.size()!=b.size()) return -1;
		for (int i=0;i<a.size();i++)
			
			temp+=a.get(i)*b.get(i);
		return temp;
	}
	public static Vector<Double> multi(Vector<Double> a, double b)
	{
		Vector<Double> temp = new Vector<Double>();
		for (int i=0;i<a.size();i++)
			
			temp.addElement(b*a.get(i));
		return temp;
	}
	public static boolean isBig(Vector<Double> k1, Vector<Double> k2)
	{
		if(k1.get(0)>=k2.get(0)&& k1.get(1)>=k2.get(1)&& k1.get(2)>=k2.get(2))
			return true;
		else
			return false;
	}
	public static double value(Vector<Double> k)
	{
		double tam=0.0;
		for(int i=0;i<k.size();i++)
			tam+=k.get(i)*k.get(i);
		return Math.sqrt(tam);
	}
	public static int bigger(Vector<Double> k1, Vector<Double> k2)
	{
		// 1: k1 > k2
		// 2: k2 > k1
		// 0: k1 = k2
		//-1: k1 != k2
		if(k1.size() != k2.size()) return -1;
		if(k1.get(0)==k2.get(0))
			if(k1.get(1)==k2.get(1))
				if(k1.get(2)==k2.get(2))
					return 0;
				else
					if(k1.get(2)>k2.get(2))
						return 1;
					else
						return 2;
			else
				if(k1.get(1)>k2.get(1))
					return 1;
				else
					return 2;
		else
			if(k1.get(0)>k2.get(0))
				return 1;
			else
				return 2;
	}
	
	public static int randInt(int min, int max) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}

	public static ArrayList<Integer> randIntArr(ArrayList<Integer> arr, int count) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();
	    ArrayList<Integer> indexes = new ArrayList<Integer>();
	    int c = 0;
	    while (true) {
	    	int randomNum = rand.nextInt((arr.size() - 1) + 1);
	    	int val = arr.get(randomNum);
	    	if (!indexes.contains(val)) {
	    		c++;
	    		indexes.add(val);
	    	}
	    	if (c == count) 
	    		break;
	    }

	    return indexes;
	}

	
	
	public static double randDouble(double min, double max) {
	    double random = new Random().nextDouble();
		double result = min + (random * (max - min));

	    return result;
	}
	public static double randDouble(int hso) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    double randomNum = rand.nextDouble()*hso;

	    return randomNum;
	}
	public static void randomLeafSpineTopo (String filePara)
	{


		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				String fileInput = line[0];
				int _noSpine= Integer.parseInt(line[1]);
				int _noLeaf= Integer.parseInt(line[2]);
				int _noServer= Integer.parseInt(line[3]);
				int _bigC = Integer.parseInt(line[4]);
				int _smallC = Integer.parseInt(line[5]);
				int s = Integer.parseInt(line[6]);
			//	String ext= FilenameUtils.getExtension(fileInput);
				MyGraph g = new MyGraph(_noSpine,_noLeaf, _noServer, _bigC,_smallC);
				
				String newFile = fileInput.substring(fileInput.lastIndexOf("\\")+1);
				int n=g.V();
				int E = g.E();
				Function[] functionArr = new Function[5];
				
			    for (int i=0;i< 5;i++)
			       functionArr[i]= new Function(i+1,1,3);
			    	
			    	out= new BufferedWriter(new FileWriter(newFile));
				    
					Demand[] demandArr= new Demand[s];
					for (int i=0;i<s;i++)
					{
						demandArr[i]= new Demand(i+1,13,functionArr,_noSpine,g.V()); 
					}
				    //ghi ra file
				    out.write("5 "+ s + " "+ n + " "+ E);
				    out.newLine();
				    for (int i=0;i<5;i++)
				    {
				    	out.write(df.format(functionArr[i].getDelay())+" "+ functionArr[i].getReq());
			            out.write(";");
			       	}
				    out.newLine();
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr[i].bwS() );
			       		out.write(demandArr[i].idS() +" " +demandArr[i].sourceS() +" "+ demandArr[i].destinationS()+" "+ demandArr[i].bwS()+" "+demandArr[i].arrivalTimeS()+" ");
			       		out.write(demandArr[i].getRate()+" ");
			       		for (int j=0;j<demandArr[i].noF();j++)
			       			out.write(demandArr[i].getFunctions()[j].id() +" ");
			       		
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{		            
			       		out.write(df.format(g.getCap(i+1)));
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catsch block
			e.printStackTrace();
		}
		
	
	
	}
	
	
	public static void randomFatTopo(String filePara,int k)
	{

		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			strLine = in.readLine();
			int capNo= Integer.parseInt(strLine.split(" ")[0]);
			int bandwidthNo= Integer.parseInt(strLine.split(" ")[1]);
			int bandNoF= Integer.parseInt(strLine.split(" ")[2]);
			Integer[] capFea = new Integer[capNo];
			Integer[] bandFea = new Integer[bandwidthNo];
			Integer[] bandF = new Integer[bandNoF];
			strLine = in.readLine();
			strLine = in.readLine();
			String[] _line = strLine.split(" ");
			for (int j=0;j <capNo;j++)
			{
				capFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandwidthNo;j++)
			{
				bandFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandNoF;j++)
			{
				bandF[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();//min max cua function req
			int minF = Integer.parseInt(strLine.split(" ")[0]);
			int maxF = Integer.parseInt(strLine.split(" ")[1]);
			int temp=1;
			strLine = in.readLine();
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				
				int n = Integer.parseInt(line[0]);
				double p = Double.parseDouble(line[1]);
				int noG= Integer.parseInt(line[2]);
				int m=Integer.parseInt(line[3]);
				MyGraph g = new MyGraph(n,p,capFea,bandFea,k);
				int E = g.E();
				Function[] functionArr = new Function[m];
				
			    for (int i=0;i< m;i++)
			       functionArr[i]= new Function(i+1,minF,maxF);
			    for (int l=0;l<noG;l++)
			    {
			    	String fileName= "data\\in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[4+l]);
				    
					Demand[] demandArr= new Demand[s];
					int NV = (k*k*k+5*k*k)/4;
					for (int i=0;i<s;i++)
					{
						
						int src, dest;
						while (true)
			        	{
			        		src = UtilizeFunction.randInt(5*k+1, NV);
			            	dest = UtilizeFunction.randInt(5*k+1, NV);
			            	if (src != dest)
			            	{
			            		break;
			            	}
			        	}
						
						demandArr[i]= new Demand(i+1,src,dest,13,functionArr,n,g,bandF); 
					}
				    //ghi ra file
				    out.write(m+" "+s + " "+ n + " "+ E);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
				    	out.write(df.format(functionArr[i].getDelay())+" "+ functionArr[i].getReq());
			            out.write(";");
			       	}
				    out.newLine();
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr[i].bwS() );
			       		out.write(demandArr[i].idS() +" " +demandArr[i].sourceS() +" "+ demandArr[i].destinationS()+" "+ demandArr[i].bwS()+" "+demandArr[i].arrivalTimeS()+" ");
			       		out.write(demandArr[i].getRate()+" ");
			       		for (int j=0;j<demandArr[i].noF();j++)
			       			out.write(demandArr[i].getFunctions()[j].id() +" ");
			       		
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{		            
			       		out.write(df.format(g.getCap(i+1)));
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catsch block
			e.printStackTrace();
		}
		
	
	}
    public static void randomNewest(String filePara)//edit model
	{
	/*	DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			strLine = in.readLine();
			int capNo= Integer.parseInt(strLine.split(" ")[0]);
			int bandwidthNo= Integer.parseInt(strLine.split(" ")[1]);
			int bandNoF= Integer.parseInt(strLine.split(" ")[2]);
			Integer[] capFea = new Integer[capNo];
			Integer[] bandFea = new Integer[bandwidthNo];
			Integer[] bandF = new Integer[bandNoF];
			strLine = in.readLine();
			strLine = in.readLine();
			String[] _line = strLine.split(" ");
			for (int j=0;j <capNo;j++)
			{
				capFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandwidthNo;j++)
			{
				bandFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandNoF;j++)
			{
				bandF[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();//min max cua function req
			int minF = Integer.parseInt(strLine.split(" ")[0]);
			int maxF = Integer.parseInt(strLine.split(" ")[1]);
			int temp=1;
			strLine = in.readLine();
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				
				int n = Integer.parseInt(line[0]);
				double p = Double.parseDouble(line[1]);
				int noG= Integer.parseInt(line[2]);
				int m=Integer.parseInt(line[3]);
				MyGraph g = new MyGraph(n,p,capFea,bandFea);
				int E = g.E();
				Function[] functionArr = new Function[m];
				
			    for (int i=0;i< m;i++)
			       functionArr[i]= new Function(i+1,minF,maxF);
			    for (int l=0;l<noG;l++)
			    {
			    	String fileName= "data\\in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[4+l]);
				    
					Demand[] demandArr= new Demand[s];
					for (int i=0;i<s;i++)
					{
						demandArr[i]= new Demand(i+1,13,functionArr,n,g,bandF); 
					}
				    //ghi ra file
				    out.write(m+" "+s + " "+ n + " "+ E);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
				    	out.write(df.format(functionArr[i].getDelay())+" "+ functionArr[i].getReq());
			            out.write(";");
			       	}
				    out.newLine();
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr[i].bwS() );
			       		out.write(demandArr[i].idS() +" " +demandArr[i].sourceS() +" "+ demandArr[i].destinationS()+" "+ demandArr[i].bwS()+" "+demandArr[i].arrivalTimeS()+" ");
			       		out.write(demandArr[i].getRate()+" ");
			       		for (int j=0;j<demandArr[i].noF();j++)
			       			out.write(demandArr[i].getFunctions()[j].id() +" ");
			       		
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{		            
			       		out.write(df.format(g.getCap(i+1)));
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catsch block
			e.printStackTrace();
		}*/
		
	}
    public static MyGraph CreateFromBriteFile(String fileName,int _cap, int _w)
	{
    	ArrayList<Integer> cap = new ArrayList<>();
		ArrayList<ArrayList<Integer>> w= new ArrayList<>();		
		MyGraph g= new MyGraph(cap,w);
    	int numberVertex= 0;
    	int numberEdge=0;
	//	int E =0;
		File file = new File(fileName);
        try {
			in = new BufferedReader(new FileReader(file));
			 DefaultDirectedGraph<String, DefaultWeightedEdge> graph =
		                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
			
			//so node;
		//	boolean _finished= false;
			while(true)
			{
				String ln= in.readLine();
				if(ln!=null)
				{

					String[] line= ln.split(" ");
					if(line[0].equals("Nodes:"))
					{
						numberVertex = Integer.parseInt(line[2]);
						for(int i=0;i<numberVertex;i++)
						{
							String[] node_line = in.readLine().split("\t");
							//String[] nodeStr = node_line.split(" ");
							graph.addVertex(node_line[0]);
						}
						
					}
					else
					{
						if(line[0].equals("Edges:"))
						{
							numberEdge = Integer.parseInt(line[2]);
							for(int i=0;i<numberEdge;i++)
							{
								String[] linkStr = in.readLine().split("\t");
								graph.addEdge(linkStr[1], linkStr[2]);
							}
							break;
						}
						else
							continue;
					}
				}
			}
			int noVertex= numberVertex;
				
			
			for (int i=0;i<noVertex;i++)
			{
				ArrayList<Integer> temp= new ArrayList<>();
				for(int j=0;j<noVertex;j++)
				{
					temp.add(0);
				}
				w.add(temp);
			}
			ArrayList<String> verLst = new ArrayList<>();
			for(String node:graph.vertexSet())
			{
				verLst.add(node);
				int c= _cap;
				cap.add(c);
//				if(node.matches(".*\\d.*"))
//					cap.add(cap_min);
//				else
//					cap.add(cap_max);
			}
			for(DefaultWeightedEdge e:graph.edgeSet())
			{
				//E++;
				String src= graph.getEdgeSource(e);
				String dest = graph.getEdgeTarget(e);
				int idSrc = verLst.indexOf(src);
				int idDest = verLst.indexOf(dest);
				int w_m= _w;
				w.get(idSrc).set(idDest, w_m);
				w.get(idDest).set(idSrc, w_m);
//				if(!src.matches(".*\\d.*") && !dest.matches(".*\\d.*"))
//				{
//					w.get(idSrc).set(idDest, w_max);
//				}
//				else
//					w.get(idSrc).set(idDest, w_min);
				
				
			}
			g= new MyGraph(cap,w);
            // Always close files.
            in.close();  
           
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return g; 
	
    }
    
    public static MyGraph CreateGraphFromFile(String fileName,int _cap, int _w)
    {
    	ArrayList<Integer> cap = new ArrayList<>();
		ArrayList<ArrayList<Integer>> w= new ArrayList<>();		
		MyGraph g= new MyGraph(cap,w);
    	int numberVertex= 0;
		//int E =0;
		File file = new File(fileName);
        try {
			in = new BufferedReader(new FileReader(file));
			 DefaultDirectedGraph<String, DefaultWeightedEdge> graph =
		                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
			
			//so node;
			boolean _finished= false;
			while(true)
			{
				String ln= in.readLine();
				if(ln!=null)
				{

					String[] line= ln.split(" ");
					if(line[0].equals("NODES"))
					{
						while(true)
						{
							String node_line = in.readLine();
							if(node_line.equals(")"))
								break;
							else
							{
								//node_line.replaceAll("^\\s*", "");
								String[] nodeStr = node_line.split(" ");
								graph.addVertex(nodeStr[2]);
								numberVertex++;
								
							}
						}
						
					}
					else
					{
						if(line[0].equals("LINKS"))
						{
							
							while(true)
							{
								String link_line= in.readLine();
								if(link_line.equals(")"))
								{
									_finished=true;
									break;
								}
								else
								{
									link_line=link_line.replaceFirst("^\\s*", "");
									String[] linkStr = link_line.split(" ");
									//String[] edgeStr = linkStr[2].split("_");
									graph.addEdge(linkStr[2], linkStr[3]);
								}
							}
							if(_finished)
								break;
						}
						else
							continue;
					}
				}
			}
			int noVertex= numberVertex;
				
			
			for (int i=0;i<noVertex;i++)
			{
				ArrayList<Integer> temp= new ArrayList<>();
				for(int j=0;j<noVertex;j++)
				{
					temp.add(0);
				}
				w.add(temp);
			}
			ArrayList<String> verLst = new ArrayList<>();
			for(String node:graph.vertexSet())
			{
				verLst.add(node);
				int c= _cap;
				cap.add(c);
//				if(node.matches(".*\\d.*"))
//					cap.add(cap_min);
//				else
//					cap.add(cap_max);
			}
			for(DefaultWeightedEdge e:graph.edgeSet())
			{
				//E++;
				String src= graph.getEdgeSource(e);
				String dest = graph.getEdgeTarget(e);
				int idSrc = verLst.indexOf(src);
				int idDest = verLst.indexOf(dest);
				int w_m= _w;
				w.get(idSrc).set(idDest, w_m);
				w.get(idDest).set(idSrc, w_m);
//				if(!src.matches(".*\\d.*") && !dest.matches(".*\\d.*"))
//				{
//					w.get(idSrc).set(idDest, w_max);
//				}
//				else
//					w.get(idSrc).set(idDest, w_min);
				
				
			}
			g= new MyGraph(cap,w);
            // Always close files.
            in.close();  
           
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return g; 
	
    }
	public static void CreateInput(String filePara,String dateCreate)
	{

		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			strLine = in.readLine();
			int capNo= Integer.parseInt(strLine.split(" ")[0]);
			int bandwidthNo= Integer.parseInt(strLine.split(" ")[1]);
			int bandNoF= Integer.parseInt(strLine.split(" ")[2]);
			Integer[] capFea = new Integer[capNo];
			Integer[] bandFea = new Integer[bandwidthNo];
			Integer[] bandF = new Integer[bandNoF];
			strLine = in.readLine();
			strLine = in.readLine();
			String[] _line = strLine.split(" ");
			for (int j=0;j <capNo;j++)
			{
				capFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandwidthNo;j++)
			{
				bandFea[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();
			_line = strLine.split(" ");
			for (int j=0;j <bandNoF;j++)
			{
				bandF[j] = Integer.parseInt(_line[j]);
			}
			strLine = in.readLine();
			strLine = in.readLine();//min max cua function req
			int minF = Integer.parseInt(strLine.split(" ")[0]);
			int maxF = Integer.parseInt(strLine.split(" ")[1]);
			int temp=1;
			strLine = in.readLine();
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				String fileInput = line[0];
				int _band = Integer.parseInt(line[1]);
				int _cap = Integer.parseInt(line[2]);
				int noG= Integer.parseInt(line[3]);
				int m=Integer.parseInt(line[4]);
				String typeFile = line[5+noG];
				//MyGraph g = CreateGraphFromFile(fileInput, _cap,_w);
				String ext= FilenameUtils.getExtension(fileInput);
				if(ext.equals("txt"))
					g=CreateGraphFromFile(fileInput, _cap, _band);
				else
				{
					g=CreateFromBriteFile(fileInput, _cap, _band);
					fileInput = fileInput.replace("brite","txt" );
				}
				String newFile = fileInput.substring(fileInput.lastIndexOf("\\")+1);
				int n=g.V();
				int E = g.E();
				Function[] functionArr = new Function[m];
				
			    for (int i=0;i< m;i++)
			       functionArr[i]= new Function(i+1,minF,maxF);
			    for (int l=0;l<noG;l++)
			    {
			    	String fileName="";
			    	if (noG==1)
			    	{
			    		fileName= dateCreate+"\\"+typeFile+"_"+newFile;
			    	}
			    	else
			    		fileName= dateCreate+"\\"+typeFile+"_"+(l+temp)+newFile;
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[5+l]);
				    
					Demand[] demandArr= new Demand[s];
					for (int i=0;i<s;i++)
					{
						demandArr[i]= new Demand(i+1,13,functionArr,n,g,bandF); 
					}
				    //ghi ra file
				    out.write(m+" "+s + " "+ n + " "+ E);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
				    	out.write(df.format(functionArr[i].getDelay())+" "+ functionArr[i].getReq());
			            out.write(";");
			       	}
				    out.newLine();
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr[i].bwS() );
			       		out.write(demandArr[i].idS() +" " +demandArr[i].sourceS() +" "+ demandArr[i].destinationS()+" "+ demandArr[i].bwS()+" "+demandArr[i].arrivalTimeS()+" ");
			       		out.write(demandArr[i].getRate()+" ");
			       		for (int j=0;j<demandArr[i].noF();j++)
			       			out.write(demandArr[i].getFunctions()[j].id() +" ");
			       		
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{		            
			       		out.write(df.format(g.getCap(i+1)));
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catsch block
			e.printStackTrace();
		}
		
	
	}
	public static double randomDouble(Integer[] intArray)
	{
		//Integer[] intArray = new Integer[] { 100,150,200,400, 500 };
		
		ArrayList<Integer> asList = new ArrayList<Integer>(Arrays.asList(intArray));
		Collections.shuffle(asList);
		return Double.parseDouble(asList.get(0).toString());
	}
	public static int randomInt(Integer[] intArray)
	{
		//Integer[] intArray = new Integer[] { 100,150,200,400, 500 };
		
		ArrayList<Integer> asList = new ArrayList<Integer>(Arrays.asList(intArray));
		Collections.shuffle(asList);
		
		return Integer.parseInt(asList.get(0).toString());
	}
	public static void main()
	{
		//randomData("inputFile.txt");
		//randomData_lib("out.txt", 3, 5);
	}
}