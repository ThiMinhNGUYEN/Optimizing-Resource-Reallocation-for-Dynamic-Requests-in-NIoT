import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
//import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.uncommons.maths.random.ExponentialGenerator;
import org.uncommons.maths.random.PoissonGenerator;

import com.gurobi.gurobi.*;

public class main {
	static BufferedWriter out;
	static BufferedReader in;
	static int c,n,m,d,z,E;
	static MyGraph g;
	static Function[] functionArr;
//	static Demand[] demandArr;
	static ArrayList<Demand> demandArray;
	
	static long _duration=0;
	static double value_final=0.0;
	static double currentTime=0.0;
	static double finalRunTime = 0.0;
	static int m1;//so function trong 1 demand

	static int noSpine =0;
	static double spineRatio = 0;
	static int leafCapacity = 0;
	
	static double maximumLink = 0;
	static double maximumNode=0;
	
	
	//new model
	static double[] failure;
	static int[] functionDataSize;
	static int[] nodeCost;
	static int[] nodeType;// node can be type layer 2 or layer 3
	static double[] nodeDelay;
	static double[][] functionNodeDelay;
	static double alpha = 1;
	
	static double UD = 0.0;
	static double UC= 0.0;
	static double UQ = 0.0;
	static double Ub = 0.0;
	
	 static double temperature = 500; //1000
	 static double coolingFactor = 0.99; //0.995
	 static int phi_RSS = 10; //100

	
	static GRBVar[][][] x;
	static GRBVar[] Z;
	static GRBVar[][] U;
	static GRBVar we, wc;
	
	static int acceptDemands;
	
	static int failure_no = 3;
	
	public static ArrayList<Integer> Sp_double(int src,int dest, GraphDouble _g,double maxBw)
	{

		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        
		for (int j=0;j<_g.V();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(g.getV()*(g.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<_g.V();j++)
        {	        	
        	for(int k=0;k<_g.V();k++)
        	{
        		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e, _g.getEdgeWeight((j+1), (k+1)));
        		}
        	}
        }       
        GraphPath<String, DefaultWeightedEdge> _p = org.jgrapht.alg.shortestpath.DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
		if (_p != null) {
			for (int i = 0; i < _p.getVertexList().size(); i++) {
				int vertex = Integer.parseInt(_p.getVertexList().get(i).replaceAll("[\\D]", ""));
				_shortestPath.add(vertex);

			}
			for (int _i : _shortestPath) {
				System.out.print(_i + ",");
			}
		} else {
			System.out.println("khong tim duoc duong di giua " + src + " va " + dest);
			return null;

		}  
        
		return _shortestPath;
	
	}
	public static ArrayList<Integer> ShortestPath(int src, int dest, MyGraph _g,double maxBw)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        
		for (int j=0;j<_g.V();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(g.getV()*(g.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<_g.V();j++)
        {	        	
        	for(int k=0;k<_g.V();k++)
        	{
        		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e, _g.getEdgeWeight((j+1), (k+1)));
        		}
        	}
        }       
        GraphPath<String, DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
    	if (_p != null) {
			for (int i = 0; i < _p.getVertexList().size(); i++) {
				int vertex = Integer.parseInt(_p.getVertexList().get(i).replaceAll("[\\D]", ""));
				_shortestPath.add(vertex);

			}
			for (int _i : _shortestPath) {
				System.out.print(_i + ",");
			}
		} else {
			System.out.println("khong tim duoc duong di giua " + src + " va " + dest);
			return null;

		}
        
		return _shortestPath;
	}
	
	public static double getDelay(int id)
	{
		if(id==0) return -1;
		for(int i=0;i<m;i++)
			if (functionArr[i].id() ==id)
				return functionArr[i].getDelay();
		return -1;
	}
	public static double getReq(int id)
	{
		if(id==0) return -1;
		for(int i=0;i<m;i++)
			if (functionArr[i].id() ==id)
				return functionArr[i].getReq();
		return -1;
	}
	/**id is from 1 to m*/
	public static Function getFunction(int id)
	{
		if(id==0) return null;
		for(int i=0;i<m;i++)
			if (functionArr[i].id() ==id)
				return functionArr[i];
		return null;
	}
	
	public static double getBwService(int id)
	{
		if(id==0) return 0;
		for(int i=0;i<m;i++)
			if(demandArray.get(i).idS()==id)
				return demandArray.get(i).bwS();
		return -1;
	}
	public static double getRateService(int id)
	{
		if(id==0) return 0;
		for(int i=0;i<m;i++)
			if(demandArray.get(i).idS()==id)
				return demandArray.get(i).getRate();
		return -1;
	}
	/**id is from 1 to d*/
	public static Demand getDemand(int id)
	{
		for (int i=0;i<d;i++)
			if(demandArray.get(i).idS()==id)
				return demandArray.get(i);
		return null;
	}
	
	
	public static void ReadNewInputFile(String fileName) //27Juin2022
	{
		
		m1=4;
		leafCapacity = 40;
		File file = new File(fileName);
		demandArray = new ArrayList<Demand>();
        try {
			in = new BufferedReader(new FileReader(file));
			String[] firstLine=in.readLine().split(" ");
			m= Integer.parseInt(firstLine[0]);
			d= Integer.parseInt(firstLine[1]);
			n= Integer.parseInt(firstLine[2]);
			E = Integer.parseInt(firstLine[3]);
			String[] line= new String[3*n+d+m+2];
			String thisLine=null;
			int k =0;
			
			while((thisLine = in.readLine()) !=null)
			{				
				line[k]=thisLine;
				k++;
			}	
			functionArr= new Function[m];
			
			//m function
			int line_no = 0;
			String[] lineFunc = line[line_no].split(";");
			for(int i = 0;i<m;i++)
			{ 
				functionArr[i]= new Function(i+1,
						Double.parseDouble(lineFunc[i].split(" ")[0]),
						Integer.parseInt(lineFunc[i].split(" ")[1]),
						Integer.parseInt(lineFunc[i].split(" ")[2]));
			}
			String[] tempLine;
			//d demand
			line_no += 1;
			for (int i=0;i<d;i++)
			{
				tempLine = line[line_no].split(" ");
				Function[] f = new Function[tempLine.length-6];
				for (int j=0;j<f.length;j++)
					f[j]= getFunction(Integer.parseInt(tempLine[j+6]));
				Demand d_temp= new Demand(Integer.parseInt(tempLine[0]),Integer.parseInt(tempLine[1]),Integer.parseInt(tempLine[2]),Integer.parseInt(tempLine[3]),Double.parseDouble(tempLine[4]),Double.parseDouble(tempLine[5]),f);
				demandArray.add(d_temp);//
				line_no++;
			}
			//luu vao mang n+1 chieu
			ArrayList<Integer> cap = new ArrayList<>();
			ArrayList<ArrayList<Integer>> ww = new ArrayList<>();  				
			
			// virtual network
			for (int i=0;i <n;i++)
			{				
				
	   	        cap.add(Integer.parseInt(line[line_no]));
	   	        if(Integer.parseInt(line[line_no])>leafCapacity)
	   	        	noSpine++;
	   	        line_no ++;
			}
			
			for (int i=1;i<n+1;i++)
			{
				ArrayList<Integer> temp= new ArrayList<>();
				tempLine = line[line_no].split(" ");
				for(int j=1;j<n+1;j++)
				{
					temp.add(Integer.parseInt(tempLine[j-1]));
				}
				ww.add(temp);
				line_no ++;
			}
			
			failure = new double[n];
			functionDataSize = new int[m];			
			nodeCost = new int[n];		
			nodeDelay = new double[n];
			nodeType = new int[n];
			
			
			functionNodeDelay = new double[m][n];
			for (int i = 0; i <n; i++) {
				tempLine = line[line_no].split(" ");
				failure[i] = Double.parseDouble(tempLine[0]);

				nodeCost[i] = Integer.parseInt(tempLine[1]);
				nodeDelay[i] = Double.parseDouble(tempLine[2]);
				nodeType[i] = Integer.parseInt(tempLine[3]);
				line_no ++;
			}
			for (int i = 0; i <m; i++) {
			
				tempLine = line[line_no].split(" ");
				functionDataSize[i] = Integer.parseInt(tempLine[i]);			
			}			
			line_no++;
			for (int i = 0; i <m; i++) {
				for (int j = 0; j <n; j++) {
					tempLine = line[line_no].split(" ");
					functionNodeDelay[i][j] = Double.parseDouble(tempLine[j]);			
				}
				line_no ++;
			}
			
			g= new MyGraph(cap,ww);
            in.close();  
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//heuristic
	static int[][] Dist;
	public static boolean _Dist()
	{
		SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		Dist = new int[g.V()+1][g.V()+1];
		for(int i=0;i<n+1;i++)
        	for (int j=0;j<n+1;j++)
        		Dist[i][j]=Integer.MAX_VALUE;
		for (int j=0;j<n;j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(n*(n-1))/2];
        int id=0;
        
        for (int j=0;j<n-1;j++)
        {	        	
        	for(int k=j+1;k<n;k++)
        	{
        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
        		g_i.setEdgeWeight(e[id], g.getEdgeWeight((j +1), (k+1)));
        		id++;
        	}
        }
        for(int i=0;i<n-1;i++)
        	for (int j=i+1;j<n;j++)
        	{
        		GraphPath<String, DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+(i+1), "node"+(j+1));
        		if(_p!=null)
        		{
        			Dist[i+1][j+1]=_p.getVertexList().size()+1;
        			Dist[j+1][i+1]=_p.getVertexList().size()+1;
        			
        			//Dist[i+1][j+1]=_p.size()+1;
        			//Dist[j+1][i+1]=_p.size()+1;
        		}
        		else
        		{
        			Dist[i+1][j+1]=Integer.MAX_VALUE;
        			Dist[j+1][i+1]=Integer.MAX_VALUE;
        		}
        	} 
        return true;
        
	}
	
	static double functionCost=0.0;
	static ArrayList<ArrayList<Integer>> srcLst;
	static ArrayList<ArrayList<Integer>> destLst;
	static ArrayList<Integer> exLst;
	public static void clustering(int numCluster)
	{

		for(int i=0;i<numCluster;i++)
		{
			ArrayList<Integer> nodeLst = new ArrayList<>();
		}
	}
	static ArrayList<Integer> setOFEndPoint;
	static ArrayList<Integer> sortingNode (ArrayList<Integer> _nodeLst)
	{
		ArrayList<Integer> id = new ArrayList<>();
		while(true)
		{
			int max=-1;
			int maxID = -1;
			for(int i=0;i<_nodeLst.size();i++)
			{
				if(_nodeLst.get(i)>max)
				{
					max = _nodeLst.get(i);
					maxID = i;
				}
			}
			if(maxID!=-1)
			{
				id.add(maxID+1);
				_nodeLst.set(maxID, -1);
			}
			else
				break;	
			
		}
		return id;
		
	}
	static int distanceCluster(ArrayList<Integer> s1, ArrayList<Integer> s2, MyGraph _g)
	{
		int minVal = Integer.MAX_VALUE;
		
		for(int i=0;i<s1.size();i++)
		{
			for(int j=0;j<s1.size();j++)
			{
				if(_g.getEdgeWeight(s1.get(i), s1.get(j))>0 && _g.getEdgeWeight(s1.get(i), s1.get(j))<minVal)
					minVal = _g.getEdgeWeight(s1.get(i), s1.get(j));
			}
		}
		for(int i=0;i<s2.size();i++)
		{
			for(int j=0;j<s2.size();j++)
			{
				if(_g.getEdgeWeight(s2.get(i), s2.get(j))>0 && _g.getEdgeWeight(s2.get(i), s2.get(j))<minVal)
					minVal = _g.getEdgeWeight(s2.get(i), s2.get(j));
			}
		}
		if(s1.size()==1&&s2.size()==1)
			minVal=0;
		
		for(int i=0;i<s1.size();i++)
		{
			for(int j=0;j<s2.size();j++)
			{
				if(_g.getEdgeWeight(s1.get(i), s2.get(j))>0 && _g.getEdgeWeight(s1.get(i), s2.get(j))>minVal)
					minVal = _g.getEdgeWeight(s1.get(i), s2.get(j));
			}
		}
		if(minVal>0)
			return minVal;
		else
			return -1;
	}
	static ArrayList<ArrayList<Integer>> superUpdateCluster(ArrayList<ArrayList<Integer>> lst, MyGraph _g)
	{
		ArrayList<ArrayList<Integer>> fiCluster = new ArrayList<>();
		ArrayList<ArrayList<Integer>> groups = new ArrayList<>();
		
		for(int i=0;i<lst.size()-1;i++)
		{
			ArrayList<Integer> dist = new ArrayList<>();
			ArrayList<Integer> t = new ArrayList<>();
			for(int j=i+1;j<lst.size();j++)
			{
				dist.add(distanceCluster(lst.get(i), lst.get(j), _g));
			}
			int max =Collections.max(dist);
			for(int j=0;j<dist.size();j++)
			{
				if(dist.get(j)==max)
					t.add(j+i+1);
					
			}
			groups.add(t);
		}

		return fiCluster;
	}
	static ArrayList<ArrayList<Integer>> updateCluster(ArrayList<ArrayList<Integer>> _cluster,ArrayList<Integer> groupCls,int _n)
	{
		ArrayList<ArrayList<Integer>> fiCluster = new ArrayList<>();
		ArrayList<Integer> cls = new ArrayList<>();
		cls.add(_n);
		for(int i=0;i<groupCls.size();i++)
		{
			ArrayList<Integer> t = _cluster.get(groupCls.get(i));
			
			for(int j =0;j<t.size();j++ )
				cls.add(t.get(j));
		}
		fiCluster.add(cls);
		for(int i=0;i<_cluster.size();i++)
		{			
			if(!groupCls.contains(i))
			{
				cls = new ArrayList<>();
				ArrayList<Integer> t = _cluster.get(i);
				for(int j =0;j<t.size();j++ )
					cls.add(t.get(j));
				fiCluster.add(cls);
			}
			
		}
		return fiCluster;
	}
	public static int numberOfTimes(int id, ArrayList<Integer> arr)
	{
		int num = 0;
		for(int i=0;i<arr.size();i++)
			if(arr.get(i)==id)
				num++;
		return num;
	}
	public static int min_hop_back(ArrayList<Integer> _nLst, MyGraph _g,int _n)
	{
		int minVal = -1;
		DefaultDirectedGraph<String, DefaultEdge> g_i = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<String> vertexList = new ArrayList<String>();
		g_i.addVertex("node0");
		vertexList.add("node0");
		for (int i = 0; i < _nLst.size(); i++) {
				int s= i+1;
				vertexList.add("node"+s);
				g_i.addVertex("node"+s);
		}
		for(int i=0;i<_nLst.size();i++)
		{
			for(int j=0;j<_nLst.size();j++)
			{
				if(i!=j && _g.getEdgeWeight(_nLst.get(i), _nLst.get(j))>0)
					g_i.addEdge(vertexList.get(i+1), vertexList.get(j+1));
			}
			if(_g.getEdgeWeight(_nLst.get(i),_n)>0)
				g_i.addEdge(vertexList.get(i+1),vertexList.get(0));
		}
		for(int i=0;i<_nLst.size();i++)
		{
			
			GraphPath<String, DefaultEdge> path = DijkstraShortestPath.findPathBetween(g_i, vertexList.get(i+1), vertexList.get(0));
			if(path!=null)
			{
				int len = path.getVertexList().size();
				if(minVal==-1)
					minVal= len;
				else
					if(len<minVal)
						minVal= len;
			}
			
		}	
		return minVal;
	
	}
	public static ArrayList<Integer> SP(int src, int dest, ArrayList<Integer> lst, MyGraph _g,int bw,int lengF)
	{
		ArrayList<Integer> temp = new ArrayList<>();
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//DirectedGraph<String, DefaultEdge> g_i = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<String> vertexList = new ArrayList<String>();
		g_i.addVertex("node"+src);
		g_i.addVertex("node"+dest);
		vertexList.add("node" + src);
		vertexList.add("node" + dest);
		for (int i = 0; i < lst.size(); i++) {
				int s= lst.get(i);
				vertexList.add("node"+s);
				g_i.addVertex("node"+s);
		}
		for(int i=0;i<lst.size();i++)
		{
			for(int j=0;j<lst.size();j++)
			{
				if(i!=j && _g.getEdgeWeight(lst.get(i), lst.get(j))>=bw)
				{
					
					DefaultWeightedEdge e=g_i.addEdge(vertexList.get(i+2), vertexList.get(j+2));   
					double w = maximumLink*1.0/_g.getEdgeWeight(lst.get(i), lst.get(j))+lengF*maximumNode*1.0/_g.getCap(lst.get(j));
					g_i.setEdgeWeight(e, w);
				}
			}
			if(_g.getEdgeWeight(src, lst.get(i))>=bw)
			{
				DefaultWeightedEdge e=g_i.addEdge(vertexList.get(0), vertexList.get(i+2));   
				double w = maximumLink*1.0/_g.getEdgeWeight(src, lst.get(i))+lengF*maximumNode*1.0/_g.getCap(lst.get(i));
				g_i.setEdgeWeight(e, w);
			}
			if(_g.getEdgeWeight(lst.get(i), dest)>=bw)
			{
				//g_i.addEdge(vertexList.get(i+2), vertexList.get(1));
				DefaultWeightedEdge e=g_i.addEdge(vertexList.get(i+2), vertexList.get(1)); 
				double w = maximumLink*1.0/_g.getEdgeWeight(lst.get(i),dest)+lengF*maximumNode*1.0/_g.getCap(dest);
				g_i.setEdgeWeight(e, w);
			}
		}
		GraphPath<String, DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, vertexList.get(0),vertexList.get(1));
		if(_p!=null )
		{
			for (int i = 0; i < _p.getVertexList().size(); i++) {
				int vertex = Integer.parseInt(_p.getVertexList().get(i).replaceAll("[\\D]", ""));
				temp.add(vertex);

			}
				
		}
		
		return temp;
	}
	public static int min_hop(int _n, ArrayList<Integer> _nLst, MyGraph _g)
	{	

		int minVal = -1;
		DefaultDirectedGraph<String, DefaultEdge> g_i = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<String> vertexList = new ArrayList<String>();
		g_i.addVertex("node0");
		vertexList.add("node0");
		for (int i = 0; i < _nLst.size(); i++) {
				int s= i+1;
				vertexList.add("node"+s);
				g_i.addVertex("node"+s);
		}
		for(int i=0;i<_nLst.size();i++)
		{
			for(int j=0;j<_nLst.size();j++)
			{
				if(i!=j && _g.getEdgeWeight(_nLst.get(i), _nLst.get(j))>0)
					g_i.addEdge(vertexList.get(i+1), vertexList.get(j+1));
			}
			if(_g.getEdgeWeight(_n, _nLst.get(i))>0)
				g_i.addEdge(vertexList.get(0), vertexList.get(i+1));
		}
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(g_i);
		SingleSourcePaths<String, DefaultEdge> allPaths = floyd.getPaths(vertexList.get(0));
		if(allPaths !=null)
		{			
			minVal = allPaths.getPath("node0").getEdgeList().size();
			for (String st : vertexList) {
				GraphPath<String,DefaultEdge> p = allPaths.getPath(st);
		
				if(p.getEdgeList().size()<minVal)
					minVal = p.getEdgeList().size();
			
			}
			return minVal;
		}
		
		return -1;
	}

	static ArrayList<ArrayList<Integer>> partitionNode2(MyGraph _g,int _k)
	{
		//_k la gia tri clusters khoi tao
		ArrayList<ArrayList<Integer>> clusters = new ArrayList<>();
		d= demandArray.size();
		ArrayList<Integer> _cap = new ArrayList<>();
		for(int i=0;i<_g.K.size();i++)
			_cap.add(_g.K.get(i));
		
		ArrayList<Integer> nodeLst = sortingNode(_cap);
		int id = 0;
		for(int i=0;i<_k;i++)
		{
			ArrayList<Integer> temp = new ArrayList<>();
			for(int j = id;j<nodeLst.size();j++)
			{
				if(!setOFEndPoint.contains(nodeLst.get(j)))
				{
					temp.add(nodeLst.get(j));
					id = j+1;
					break;
				}
			}
			clusters.add(temp);
		}
		ArrayList<Integer> nodeRemaining = new ArrayList<>();
		for(int j=id;j<nodeLst.size();j++)
		{
			if(!setOFEndPoint.contains(nodeLst.get(j)))
				nodeRemaining.add(nodeLst.get(j));
		}
		
		for(int i=0;i<nodeRemaining.size();i++)
		{
			int _n = nodeRemaining.get(i);
			if(!setOFEndPoint.contains(_n))
			{
				int max =-1;
				ArrayList<Integer> ClustLst = new ArrayList<>();
				for(int j=0;j<clusters.size();j++)
				{
					ArrayList<Integer> cls = clusters.get(j);
					for (int k=0;k<cls.size();k++)
					{
						if(_g.getEdgeWeight(_n, cls.get(k))>0 || _g.getEdgeWeight(cls.get(k),_n)>0 )
						{
							if(_g.getEdgeWeight(_n, cls.get(k))>max || _g.getEdgeWeight(cls.get(k),_n)>max )
							{
								ClustLst = new ArrayList<>();
								max = _g.getEdgeWeight(_n, cls.get(k));
								ClustLst.add(j);
							}
							else
							{
								if(_g.getEdgeWeight(_n, cls.get(k))==max || _g.getEdgeWeight(cls.get(k),_n)==max )
								{
									if(!ClustLst.contains(j))
										ClustLst.add(j);
								}
							}
						}
						
					}
				}
				clusters = updateCluster(clusters, ClustLst,_n);	
			}
					
		}
		
		
		return clusters;	
	}
	
	//Giai thuat tao graph bang viec chia tung tap dinh dau va dinh cuoi cua demand
	
	public static void partitionNode(MyGraph _g)
	{
		d= demandArray.size();
		srcLst = new ArrayList<>();
		destLst = new ArrayList<>();
		exLst = new ArrayList<>();
		for(int i=0;i<d;i++)
		{
			Demand _d = demandArray.get(i);			
			srcLst.add(new ArrayList<>(Arrays.asList(_d.sourceS())));
			destLst.add(new ArrayList<>(Arrays.asList(_d.destinationS())));			
		}
		for(int i=0;i<_g.V();i++)
		{
			int maxID1 = -1;
			int maxVal= -1;
			int maxIDlst = -1;
			for(int j=0;j<srcLst.size();j++)
			{
				int _s = srcLst.get(j).get(0);
				int _w = _g.getEdgeWeight(_s, i+1);
				if(_w>0 && _w>maxVal)
				{
					maxVal = _w;
					maxID1 = j;
				}
				
			}
			if(maxID1!=-1)
			{
				for(int j=0;j<srcLst.size();j++)
				{
					int _n = srcLst.get(j).get(0);
					if(_n==srcLst.get(maxID1).get(0))
					{
						ArrayList<Integer> _t = srcLst.get(j);
						//neu maxID co o nhieu srclst -> add vao het
						_t.add(i+1);
						srcLst.set(j,_t);
					}					
				}
			}
			int maxID2 =-1;
			maxVal =-1;
			maxIDlst=-1;
			for(int j=0;j<destLst.size();j++)
			{
				int _d = destLst.get(j).get(0);
				int _w = _g.getEdgeWeight(i+1,_d);
				if(_w>0 && _w>maxVal)
				{
					maxVal = _w;
					maxID2 = j;
				}
//				for(int k=0;k<destLst.get(j).size();k++)
//				{
//					int _d = destLst.get(j).get(k);
//					int _w = g.getEdgeWeight(i+1,_d);
//					if(_w>0 &&_w>maxVal)
//					{
//						maxVal = _w;
//						maxID = j;
//						maxIDlst=_d;
//					}
//				}
				
			}
			if(maxID2!=-1)
			{
				for(int j=0;j<destLst.size();j++)
				{
					int _n= destLst.get(j).get(0);
					if(_n==destLst.get(maxID2).get(0))
					{
						ArrayList<Integer> _t = destLst.get(j);
						_t.add(i+1);
						destLst.set(j, _t);
					}
//					for(int k=0;k<destLst.get(j).size();k++)
//					{
//						int _n= destLst.get(j).get(k);
//						if(_n==maxIDlst)
//						{
//							ArrayList<Integer> _t = destLst.get(j);
//							_t.add(i+1);
//							destLst.set(j, _t);
//							break;
//						}
//					}
					
				}
			}
			if(maxID1==-1 && maxID2==-1)
				exLst.add(i+1);
		}
	}
	public static int MinimalConnect(ArrayList<Integer> s1, ArrayList<Integer> s2,MyGraph _g)
	{
		int min = Integer.MAX_VALUE;
		boolean ok = false;
		for(int i=0;i<s1.size();i++)
			for(int j=0;j<s2.size();j++)
			{
				int _w = _g.getEdgeWeight(s1.get(i), s2.get(j));
				if(_w!=0 && _w<min)
				{
					ok=true;
					min = _w;
				}
			}
		if(!ok)
			min=-1;
		return min;
	}
	
	//Tao graph voi dinh dai dien cho cac tap, canh la gia tri nho nhat cua link noi giua 2 tap.
	public static GraphDouble CreateNodeGraph(MyGraph _g)
	{
		ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
		ArrayList<Double> _cap = new ArrayList<>();
		ArrayList<ArrayList<Double>> _w= new ArrayList<>();
	
		for(int i=0;i<srcLst.size();i++)
		{
			temp.add(srcLst.get(i));
			_cap.add(10.0);
		}
		for(int i=0;i<destLst.size();i++)
		{
			temp.add(destLst.get(i));
			_cap.add(10.0);
		}
		temp.add(exLst);
		_cap.add(10.0);
		for(int i=0;i<temp.size();i++)
		{
			ArrayList<Double> _wTemp = new ArrayList<>();
			for(int j=0;j<temp.size();j++)
			{
				_wTemp.add(-1.0);
				
			}
			_w.add(_wTemp);
		}
		for(int i=0;i<srcLst.size();i++)
		{
			ArrayList<Double> _wTemp = _w.get(i);
			for(int j=0;j<temp.size();j++)
			{
				if(i!=j)
				{
					ArrayList<Integer> si=srcLst.get(i);
					ArrayList<Integer> sj = temp.get(j);
					int minimal = MinimalConnect(si, sj,_g);		
					_wTemp.set(j, maximumLink*1.0/minimal);	
				}
							
				
			}
			_w.set(i, _wTemp);
		}
		for(int j=0;j<temp.size();j++)
		{
			ArrayList<Double> _wTemp = _w.get(j);
			for(int i=0;i<destLst.size();i++)
			{
				if(i!=j-srcLst.size())
				{
					ArrayList<Integer> si=destLst.get(i);
					ArrayList<Integer> sj = temp.get(j);
					int minimal = MinimalConnect(sj, si,_g);		
					_wTemp.set(i+srcLst.size(), maximumLink*1.0/minimal);	
				}
			}
			_w.set(j, _wTemp);
		}
		
		GraphDouble gNode = new GraphDouble(_cap, _w);
		return gNode;
		
	}

	public static GraphDouble GraphCombination(ArrayList<ArrayList<Integer>> set,int bw,MyGraph _g)
	{
		ArrayList<Double> _cap = new ArrayList<>();
		ArrayList<ArrayList<Double>> _w= new ArrayList<>();
	
		for(int i=0;i<_g.V();i++)
		{
			_cap.add(_g.getCap(i+1)*1.0);
		}
		for(int i=0;i<_g.V();i++)
		{
			ArrayList<Double> _wTemp = new ArrayList<>();
			for(int j=0;j<_g.V();j++)
			{
				_wTemp.add(-1.0);
			}
			_w.add(_wTemp);
		}
		for(int i=0;i<set.size();i++)
		{
			for(int j1=0;j1<set.get(i).size();j1++)
			{
				for(int j2=0;j2<set.get(i).size();j2++)
				{
					int u = set.get(i).get(j1);
					int v = set.get(i).get(j2);
					if(_g.getEdgeWeight(u,v)>=bw)
					{
						if(_g.getCap(v)>0)
						{
							_w.get(u-1).set(v-1, maximumNode*1.0/_g.getCap(v));
							//_w.get(u-1).set(v-1, maximumLink*1.0/_g.getEdgeWeight(u, v)+maximumNode*1.0/_g.getCap(v));
						}
						
					}
				}
			}
		}
		for(int i1=0;i1<set.size();i1++)
		{
			ArrayList<Integer> s1 = set.get(i1);
			for(int i2=0;i2<set.size();i2++)
			{
				if(i1!=i2)
				{
					ArrayList<Integer> s2 = set.get(i2);
					for(int j1 =0;j1<s1.size();j1++)
						for(int j2=0;j2<s2.size();j2++)
						{
							int u = s1.get(j1);
							int v = s2.get(j2);
							if(_g.getEdgeWeight(u,v)>=bw)
							{
								if(_g.getCap(v)>0)
								{
									//_w.get(u-1).set(v-1, maximumLink*1.0/_g.getEdgeWeight(u, v)+maximumNode*1.0/_g.getCap(v));
									_w.get(u-1).set(v-1, maximumNode*1.0/_g.getCap(v));
								}
							}
								
							if(_g.getEdgeWeight(v,u)>=bw)
								if(_g.getCap(u)>0)
									_w.get(v-1).set(u-1, maximumNode*1.0/_g.getCap(v));
									//_w.get(v-1).set(u-1, maximumLink*1.0/_g.getEdgeWeight(u, v)+maximumNode*1.0/_g.getCap(v));
							
						}
				}
			}
		}
		
		GraphDouble gNode = new GraphDouble(_cap, _w);
		return gNode;
	}
	
	
	public static ArrayList<Integer> SP_unequalCost(int src, int dest, MyGraph _g,int bw,double _al,double _be)
	{
		//maximumLink=1;
		//maximumNode=1;
		ArrayList<Integer> temp = new ArrayList<>();
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//DirectedGraph<String, DefaultEdge> g_i = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<String> vertexList = new ArrayList<String>();
		for (int i = 0; i < _g.V(); i++) {
				int s= i+1;
				vertexList.add("node"+s);
				g_i.addVertex("node"+s);
		}
		for(int i=0;i<_g.V();i++)
		{
			for(int j=0;j<_g.V();j++)
			{
				if(i!=j && _g.getEdgeWeight(i+1, j+1)>=bw)
				{
					if(i!=src-1 || j!=dest-1)
					{				
						DefaultWeightedEdge e=g_i.addEdge(vertexList.get(i), vertexList.get(j));   
						double w = maximumLink*_al/_g.getEdgeWeight(i+1, j+1)+_be*maximumNode/_g.getCap(j+1);
						g_i.setEdgeWeight(e, w);
					}
					else
					{
						DefaultWeightedEdge e=g_i.addEdge(vertexList.get(i), vertexList.get(j));   
						double w = 100;
						g_i.setEdgeWeight(e, w);
					}
				}
			}
		}
		GraphPath<String, DefaultWeightedEdge> _p = org.jgrapht.alg.shortestpath.DijkstraShortestPath.findPathBetween(g_i, vertexList.get(src-1),vertexList.get(dest-1));
		if (_p != null) {
			for (int i = 0; i < _p.getVertexList().size(); i++) {
				int vertex = Integer.parseInt(_p.getVertexList().get(i).replaceAll("[\\D]", ""));
				temp.add(vertex);

			}
			for (int _i : temp) {
				System.out.print(_i + ",");
			}
		} else {
			System.out.println("khong tim duoc duong di giua " + src + " va " + dest);
			return null;

		}
		/*List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, vertexList.get(src-1),vertexList.get(dest-1));
		if(_p!=null && _p.size()>0)
		{
			for(DefaultWeightedEdge e: _p)
			{
				int int_s =Integer.parseInt(g_i.getEdgeSource(e).replaceAll("[\\D]", ""));
				temp.add(int_s);
			}
			temp.add(dest);		
		}*/
		
		return temp;
	}
	
	public static ArrayList<Integer> SP(int src, int dest, MyGraph _g,int bw)
	{
		//maximumLink=1;
		//maximumNode=1;
		ArrayList<Integer> temp = new ArrayList<>();
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//DirectedGraph<String, DefaultEdge> g_i = new DefaultDirectedGraph<>(DefaultEdge.class);
		List<String> vertexList = new ArrayList<String>();
		for (int i = 0; i < _g.V(); i++) {
				int s= i+1;
				vertexList.add("node"+s);
				g_i.addVertex("node"+s);
		}
		for(int i=0;i<_g.V();i++)
		{
			for(int j=0;j<_g.V();j++)
			{
				if(i!=j && _g.getEdgeWeight(i+1, j+1)>=bw)
				{				
						DefaultWeightedEdge e=g_i.addEdge(vertexList.get(i), vertexList.get(j));   
						double w = _g.getEdgeWeight(i+1, j+1);
						g_i.setEdgeWeight(e, w);
				}
			}
		}
		GraphPath<String, DefaultWeightedEdge> _p = org.jgrapht.alg.shortestpath.DijkstraShortestPath.findPathBetween(g_i, vertexList.get(src-1),vertexList.get(dest-1));
		if (_p != null) {
			for (int i = 0; i < _p.getVertexList().size(); i++) {
				int vertex = Integer.parseInt(_p.getVertexList().get(i).replaceAll("[\\D]", ""));
				temp.add(vertex);

			}
			for (int _i : temp) {
				System.out.print(_i + ",");
			}
		} else {
			System.out.println("khong tim duoc duong di giua " + src + " va " + dest);
			return null;

		}
		
		/*List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, vertexList.get(src-1),vertexList.get(dest-1));
		if(_p!=null && _p.size()>0)
		{
			for(DefaultWeightedEdge e: _p)
			{
				int int_s =Integer.parseInt(g_i.getEdgeSource(e).replaceAll("[\\D]", ""));
				temp.add(int_s);
			}
			temp.add(dest);		
		}*/
		
		return temp;
	}
	
	
	public static ArrayList<Demand> sortDemandIncreasingSize(ArrayList<Demand> dLst, ArrayList<Integer> sz, ArrayList<ArrayList<Integer>> _sol)
	{
		ArrayList<Demand> dLstFinal = new ArrayList<>();
		ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
		int l=sz.size();
		while(l>0)
		{
			int max = Integer.MAX_VALUE;
			int max_id = -1;
			for(int i=0;i<sz.size();i++)
			{
				if(sz.get(i)<max)
				{
					max = sz.get(i);
					max_id=i;					
				}
			}
			if(max_id ==-1)
				break;
			dLstFinal.add(dLst.get(max_id));
			temp.add(_sol.get(max_id));
			sz.set(max_id, Integer.MAX_VALUE);
		}
		for(int i=0;i<_sol.size();i++)
			_sol.set(i, temp.get(i));
		return dLstFinal;		
	
	
	}
	
	public static ArrayList<Demand> sortDemandDecreasingSize(ArrayList<Demand> dLst, ArrayList<Integer> sz, ArrayList<ArrayList<Integer>> _sol)
	{

		ArrayList<Demand> dLstFinal = new ArrayList<>();
		ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
		int l=sz.size();
		while(l>0)
		{
			int max = -1;
			int max_id = -1;
			for(int i=0;i<sz.size();i++)
			{
				if(sz.get(i)>max)
				{
					max = sz.get(i);
					max_id=i;					
				}
			}
			if(max_id ==-1)
				break;
			dLstFinal.add(dLst.get(max_id));
			temp.add(_sol.get(max_id));
			sz.set(max_id, -1);
		}
		for(int i=0;i<_sol.size();i++)
			_sol.set(i, temp.get(i));
		return dLstFinal;		
	
	}
	
	public static ArrayList<Demand> sortDemand(ArrayList<Demand> dLst)
	{
		ArrayList<Demand> dLstFinal = new ArrayList<>();
		ArrayList<Demand> temp = new  ArrayList<>();
		for(int i=0;i<dLst.size();i++)
		{
			Demand _d = dLst.get(i);
			temp.add(_d);
		}
		int l=temp.size();
		while(l>0)
		{
			int max_bw = -1;
			int max_id = -1;
			for(int i=0;i<temp.size();i++)
			{
				if(temp.get(i).bwS()>max_bw)
				{
					max_bw = temp.get(i).bwS();
					max_id=i;					
				}
			}
			dLstFinal.add(temp.get(max_id));
			temp.remove(max_id);
			l= temp.size();
		}
		return dLstFinal;
		
	}
	

	static void swap(int x, int y)
	{
	    int temp = x;
	    x = y;
	    y = temp;
	}
	
	public static ArrayList<Demand> sortDemandRequiredCores(ArrayList<Demand> dLst)
	{
		ArrayList<Demand> dLstFinal = new ArrayList<>();
		ArrayList<Integer> requiredCores = new  ArrayList<>();
		ArrayList<Integer> ids = new  ArrayList<>();
		int demandSize = dLst.size();
		for(int i=0;i<dLst.size();i++)
		{
			Demand _d = dLst.get(i);
			ids.add(_d.idS());
			int cores = 0;
			for (Function _f : _d.getFunctions()) {
				cores += _f.getReq();				
			}
			requiredCores.add(cores);
		}
		
		
		//bubbleSort
		for (int i = 0; i< demandSize - 1; i++) {
			for (int j = i+1; j< demandSize - i -1; j++) {
				if (requiredCores.get(j)> requiredCores.get(i)) {
					//swap
					swap(requiredCores.get(i), requiredCores.get(j));
					swap(ids.get(i), ids.get(j));				
				}				
			}			
		}
		
		for(int i = 0; i<ids.size(); i++) {
			for (Demand _d:dLst) {
				if (_d.idS() == ids.get(i)) {
					dLstFinal.add(_d);
					break;
				}			
			}
		}
		System.out.println("size : " + dLstFinal.size());
		
		return dLstFinal;
		
	}
	public static ArrayList<ArrayList<Integer>> getMultiPaths(int source, int destination, MyGraph _g,double bw)
	{
		ArrayList<ArrayList<Integer>> _shortestPathLst = new ArrayList<>();
		int _v = _g.V();
		VertexM source_node = new VertexM(0);
		VertexM dest_node = new VertexM(0);
		DefaultDirectedWeightedGraph<VertexM, DefaultWeightedEdge> g_i = new DefaultDirectedWeightedGraph<>(
				DefaultWeightedEdge.class);
		List<VertexM> vertexList = new ArrayList<VertexM>();
		for (int i = 0; i < _v; i++) {
			int s = i + 1;
			VertexM v = new VertexM(s);
			if (s == source) source_node = v;
			if (s == destination) dest_node = v;
			vertexList.add(v);
			g_i.addVertex(v);
		}
		for (int j1 = 0; j1 < _v; j1++) {
			for (int j2 = 0; j2 < _v; j2++) {
				if ((j1 != j2) && _g.K.get(j1) > 0 && _g.K.get(j2) > 0 && (_g.getEdgeWeight(j1 + 1, j2 + 1) >= bw)) {
					VertexM v1 = vertexList.get(j1);
					VertexM v2 = vertexList.get(j2);
					DefaultWeightedEdge e = g_i.addEdge(v1, v2);
					g_i.setEdgeWeight(e, _g.getEdgeWeight(j1 + 1, j2 + 1));
				}
			}
		}
		AllDirectedPaths<VertexM, DefaultWeightedEdge> allPathFinding = new AllDirectedPaths<>(g_i);
		List<GraphPath<VertexM, DefaultWeightedEdge>> allPaths = allPathFinding.getAllPaths(source_node, dest_node, true, 5);
		
		for (GraphPath<VertexM, DefaultWeightedEdge> _p: allPaths) {			
			if (_p != null) {
				ArrayList<Integer> _shortestPath = new ArrayList<>();
				for (int i = 0; i < _p.getVertexList().size(); i++) {
					int vertex = _p.getVertexList().get(i).getId();
					_shortestPath.add(vertex);

				}
				/*for (int _i : _shortestPath) {
					System.out.print(_i + ",");
				}*/
				_shortestPathLst.add(_shortestPath);				
			}
		}
		
		if (_shortestPathLst.isEmpty()) {
			System.out.println(" no path between " + source + " and " + destination);
		}
		
		return _shortestPathLst;
	}
	
	public static ArrayList<Integer> maxCapPath(int source, int destination, MyGraph _g,double bw)
	{
		//ArrayList<Integer> _shortestPath = new ArrayList<>();
		//return _shortestPath;
		
		int _v = _g.V();
		ArrayList<ArrayList<Integer>> _shortestPathLst = new ArrayList<>();
		ArrayList<Integer> _shortestPath = new ArrayList<>();
		DefaultDirectedWeightedGraph<VertexM, DefaultWeightedEdge> g_i = new DefaultDirectedWeightedGraph<>(
				DefaultWeightedEdge.class);
		List<VertexM> vertexList = new ArrayList<VertexM>();
		for (int i = 0; i < _v; i++) {
			int s = i + 1;
			VertexM v = new VertexM(s);
			vertexList.add(v);
			g_i.addVertex(v);
		}
		for (int j1 = 0; j1 < _v; j1++)
			for (int j2 = 0; j2 < _v; j2++) {
				if ((j1 != j2) && _g.K.get(j1) > 0 && _g.K.get(j2) > 0 && (_g.getEdgeWeight(j1 + 1, j2 + 1) >= bw)) {
					VertexM v1 = vertexList.get(j1);
					VertexM v2 = vertexList.get(j2);
					DefaultWeightedEdge e = g_i.addEdge(v1, v2);
					g_i.setEdgeWeight(e, _g.getEdgeWeight(j1 + 1, j2 + 1));
					// g_i.setEdgeWeight(e,_g.getEdgeWeight(j1+1, j2+1)/(_g.getCap(j2+1)+1));
					// g_i.setEdgeWeight(e,_g.getEdgeWeight(j1+1,
					// j2+1)*(_g.getCap(j2+1)+1)/maximumNode);
				}
			}
		//System.out.println("src: "+ src);
		modifiedDijkstra d = new modifiedDijkstra(g_i);
		
		d.computeAllShortestPaths(source);
		//Collection<VertexM> vertices = g_i.getVertices();
		VertexM v= vertexList.get(0);
		for (VertexM ve:vertexList)
		{
			if (ve.getId()==destination)
			{
				v=ve;
				break;
			}
		}
		int i = 1;
			List<VertexM> _sp = d.getShortestPathTo(v);	
			_shortestPath = new ArrayList<>();
			if(_sp!=null && _sp.get(0).getId()==source)
			{
				
				for (VertexM v1:_sp)
				{
					_shortestPath.add(v1.getId());
				}
				//System.out.println("Path " + i + ": " + p);
			}
//		Set<List<VertexM>> allShortestPaths = d.getAllShortestPathsTo(v);
// 
//		for (Iterator<List<VertexM>> iter = allShortestPaths.iterator(); iter.hasNext(); i++)
//		{
//			check=false;
//			_shortestPath = new ArrayList<>();
//			List<VertexM> p = (List<VertexM>) iter.next();
//			if(!check && p.get(0).getId()==source)
//			{
//				
//				for (VertexM v1:p)
//				{
//					_shortestPath.add(v1.getId());
//				}
//				_shortestPathLst.add(_shortestPath);
//				//System.out.println("Path " + i + ": " + p);
//			}
//		}
		
		if(_shortestPath.size()==0)
			return null;

		return _shortestPath;
	
	}
	public static ArrayList<Integer> NodePlacement(Demand _d, ArrayList<Integer> p, MyGraph _g)
	{
		ArrayList<Integer> N_q= new ArrayList<>();
		Function[] fArr = _d.getFunctions();
		ArrayList<Integer> cap_p = new ArrayList<>();
		for(int i=0;i<p.size();i++)
		{
			cap_p.add(_g.getCap(p.get(i)));
		}
		int dem=0;
		for(int i=0;i<fArr.length;i++)
		{
			Function _f = fArr[i];
			for(int j=dem;j<p.size();j++)
			{
				//tim node dau tien thoa man de dat function thu i
				if(cap_p.get(j)>_f.getReq())
				{
					N_q.add(p.get(j));
					cap_p.set(j, cap_p.get(j)-_f.getReq());
					dem=j;
					break;
				}
			}
		}
		if(N_q.size()!= fArr.length)// solution not found
			return null;
		int ed = p.size();
		for(int i=fArr.length-1;i>0;i--)
		{
			Function _f = fArr[i];
			int st = p.indexOf( N_q.get(i));	
			int min=-1;
			int id_min =-1;
			for(int j=st;j<ed;j++)
			{
				if(min==-1||min<cap_p.get(j))
				{
					min = cap_p.get(j);
					id_min= j;
				}
			}
			cap_p.set(st, cap_p.get(st)+_f.getReq());
			
			N_q.set(i, p.get(id_min));
			cap_p.set(id_min, cap_p.get(id_min)-_f.getReq());
			ed=id_min+1;
		}
		return N_q;
	}
	
	 public static int[] sortDecreasingFractional(double[] srcLst)
	  {

		  int[] temp= new int[srcLst.length];
			int dem=0;
			double[] savelst = new double[srcLst.length];
			for(int i=0;i<srcLst.length;i++)
				savelst[i]=srcLst[i];
			System.out.println("length "+ srcLst.length);
			while (dem<srcLst.length)
			{
				double max=-1.0;
				int id=-1;
				for (int i=0;i< srcLst.length;i++)
				{
					double dtemp= srcLst[i];
					if(dtemp>max && dtemp!=-1)
					{
						max = dtemp;
						id=i;
					}
				
				}			
				if(id==-1)
				{
					System.out.println("Het chua 1 "+ dem);
					return null;
				}
				srcLst[id] = -1.0;
				temp[dem]=id;
				dem++;
				System.out.println("chua xong: "+ dem);
			}
			return temp;
		
		  
	  
	  }
	 
	 public static class ArrayIndexComparator implements Comparator<Integer>
	 {
	     private final double[] array;

	     public ArrayIndexComparator(double[] array)
	     {
	         this.array = array;
	     }

	     public Integer[] createIndexArray()
	     {
	         Integer[] indexes = new Integer[array.length];
	         for (int i = 0; i < array.length; i++)
	         {
	             indexes[i] = i; // Autoboxing
	         }
	         return indexes;
	     }

	     @Override
	     public int compare(Integer index1, Integer index2)
	     {
	          // Autounbox from Integer to int to use as array indexes
	    	 if(array[index1]>array[index2])
	    		 return -1;
	    	 else
	    	 {
	    		 if(array[index1]==array[index2])
	    			 return 0;
	    		 else
	    			 return 1;
	    	 }
	     }
	 }
	 
	
	static public void combinations(int noComb, ArrayList<Integer> arr, ArrayList<Integer> list,int startPoisition,ArrayList<ArrayList<Integer>> result) {
		
		
		if(list.size()<=0)
		{
			for (int i=0;i<noComb;i++)
				list.add(-1);
		}		
		if (noComb == 0){
			ArrayList<Integer> t= new ArrayList<>();
			for(int i=0;i<list.size();i++)
				t.add(list.get(i));
	          result.add(t);
	          list = new ArrayList<>();
	          return;
	      } 
		
	      for (int i = startPoisition; i <= arr.size()-noComb; i++){
	          list.set(list.size() - noComb,arr.get(i));
	          combinations(noComb-1,arr, list, i+1,result);
	      }
	    }
	static public ArrayList<ArrayList<Integer>> FindCover(ArrayList<Integer> idSet, int[] b_d,int _w, int noCover)
	  {
		  ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
		  ArrayList<ArrayList<Integer>> comb = new ArrayList<>();
		  ArrayList<Integer> combLst = new ArrayList<>();
		  combinations(noCover, idSet, combLst,0,comb);
		  for(int i=0;i<comb.size();i++)
		  {
			  ArrayList<Integer> cover1= comb.get(i);
			  double sum=0;
			  for(int j=0;j<cover1.size();j++)
				  sum+= b_d[cover1.get(j)];
			  if(sum>_w)
				  temp.add(cover1);
		  }
		  return temp;
	  }
	static protected int[] sortVal(ArrayList<Double> srcLst1)
	  {
		  double[] srcLst = new double[srcLst1.size()];
		  for(int i=0;i<srcLst1.size();i++)
			  srcLst[i]= srcLst1.get(i);
		  int[] temp= new int[srcLst.length];
			int dem=0;
			
			while (dem<srcLst.length)
			{
				double min=10000.0;
				int id=-1;
				for (int i=0;i< srcLst.length;i++)
				{
					double dtemp= srcLst[i];
					if(dtemp<min)
					{
						min = dtemp;
						id=i;
					}
				
				}			
				if(id==-1)
				{
					System.out.println("Het chua "+ dem);
					continue;
				}
				srcLst[id] =100000.0;
				temp[dem]=id;
				dem++;
			}
			return temp;
		
		  
	  }
	
	public static int numberofHops(ArrayList<Integer> path) {
		return path.size() - 1;
		
	}
	
	public static boolean check_feasible() {
		return true;
	}
	
	public static int getIndexDemand(int demandId, ArrayList<Demand> demands) {
		for (int i = 0; i < demands.size(); i++) {
			if (demands.get(i).idS() == demandId)
				return i;
		}
		return -1;
	}
	
	public static int getIndexNode(int node, ArrayList<Integer> nodeLst) {
		for (int i = 0; i < nodeLst.size(); i++) {
			if (nodeLst.get(i) == node)
				return i;
		}
		return -1;
	}
	
	public static double calculateUd(List<Triplet<Integer, Integer, Integer>> y1, List<Triplet<Integer, Integer, Integer>> y2, ArrayList<Demand> dLst) {
		double val = 0.0;
		//System.out.println("calculateUd -------------------");
	/*	System.out.print("y1(d,i,v) = ");
		for (Triplet<Integer, Integer, Integer> item : y1) {
			System.out.print("("+ item.getValue0()+", " + item.getValue1() + ", " + item.getValue2() + ") ");
		}
		System.out.println();
		
		System.out.print("y2(d,i,v) = ");
		for (Triplet<Integer, Integer, Integer> item : y2) {
			System.out.print("("+ item.getValue0()+", " + item.getValue1() + ", " + item.getValue2() + ") ");
		}
		System.out.println();*/
		for (int demandIndex = 0; demandIndex < y1.size(); demandIndex ++) {
			//for each demand;
			Triplet<Integer, Integer, Integer> y1Placement = y1.get(demandIndex);
			Triplet<Integer, Integer, Integer> y2Placement = y2.get(demandIndex);
			/*System.out.println("y1Placement("+ y1Placement.getValue0()+", " + y1Placement.getValue1() + ", " + y1Placement.getValue2() + ") ");
			System.out.println("y2Placement("+ y2Placement.getValue0()+", " + y2Placement.getValue1() + ", " + y2Placement.getValue2() + ") ");*/
			int u = y1Placement.getValue2();
			int v = y2Placement.getValue2();
			if (u != v) {
				//System.out.println("Go here ...");
				int dId = y1Placement.getValue0();
				int dIndex = getIndexDemand(dId, dLst);
				int fIndex = y1Placement.getValue1();
				
				//System.out.println("dId = " + dId + ", fIndex = " + fIndex);
				//System.out.println("u = " + u + ", v= " +v);
				
				if(y2Placement.getValue0()!= dId) {
					System.out.println("Error 1 here!!!!");
					return -1.0;
				}
				if(y2Placement.getValue1()!= fIndex) {
					System.out.println("Error 2 here!!!!");
					return -1.0;
				}
				int minHops = 0;							
				ArrayList<Integer> shp= maxCapPath(u, v, g, dLst.get(dIndex).bwS()*1.02);
				if(shp==null || shp.size()==0)
				{
					minHops = 0;
					System.out.println("Error 3 here!!!!");
					return -1.0;

				} else {					
					minHops = shp.size();
					
				}
			//	System.out.println("minHops = " + minHops);
				if (minHops!=0) {
					val += minHops * piFunction(fIndex, dLst.get(dIndex)) * functionDataSize[dLst.get(dIndex).getFunctions()[fIndex].id()-1];									
				}				
			}			
		}
	//	System.out.println("val = "+ val);
		
		return val;
	}
	
	public static int piFunction(int i, Demand d) {
		//return outcoming traffic for function ith of demand d
		int r = d.bwS();
		if (i< d.getFunctions().length -1) return r;
		/*for (int k = i-1; k < i; k++) {
			//Function f = d.getFunctions()[k];
			r *= 1.09;
			//r *= f.getDelay();		
		}*/
		return (int)(r*1.02);		
	}
	
	public static boolean placementPossible(Demand _d, ArrayList<Integer> cap, ArrayList<Integer> p) {
		int pIndex = 0;
		int funcIndex = 0;
		for (Function _f : _d.getFunctions()) {
			while (true) {
				//System.out.println("node : " + p.get(pIndex) + " has cap: " + cap.get(p.get(pIndex) - 1));
				if (cap.get(p.get(pIndex) - 1) >= _f.getReq()) {
					
					cap.set(p.get(pIndex) - 1, cap.get(p.get(pIndex)-1) - _f.getReq());

					funcIndex++;
					//System.out.println("function : " + _f.id() + " of demand " + _d.idS() + " put on "
						//	+ p.get(pIndex));
					pIndex++;
					if (funcIndex != _d.getFunctions().length && pIndex == p.size()) {
						
						//System.out.println("Not enough capacity");
						return false;
					}
					break;
				} else {
					pIndex++;
				}
				if (pIndex == p.size()) {
					
					//System.out.println("Not enough capacity");
					return false;

				}
			}
		}
		return true;
	}
	
	public static void optimize_resilient(String outFile, String objStr) {

		try {

			//File file = new File(outFile);
			//out = new BufferedWriter(new FileWriter(file));
			out = new BufferedWriter(new FileWriter(outFile));
			out.write("number of function:" + m);
			out.newLine();
			for (int i=0;i<m;i++)
	       	{	 
	               out.write(functionArr[i].toString());
	               out.newLine();
	       	}
	   		out.write("number of Demand:" + d);
	   		
	   		out.newLine();
	       	for (int i=0;i<d;i++)
	       	{    		
	       		out.write(demandArray.get(i).toString());
	       		out.newLine();
	       	}
	       	out.write("virtual node:"+ n);
	       	out.newLine();
	       	for (int i=0;i<n;i++)
	       	{
	       		for (int j=0;j<n;j++)
	       			out.write(g.getEdgeWeight(i+1, j+1) + " ");
	       		out.newLine();
	       	}
	       	
	       	for (int i= 0; i<n; i ++) {
	       		out.write(failure[i] + " ");
	       	}
	       	out.newLine();
	       	
	       	//preprocess -> tim duong di ngan nhat cho tat ca cac demand
	       	
	      
	       	System.out.println();
	       	
	       		
			x= new GRBVar[n][d][m];//y2[v][d][i]: node v provide function ith of demand d
			Z = new GRBVar[d]; // Z[d]: demand d is served
			U = new GRBVar[d][m];
	        //ArrayList<Demand> dLst = sortDemand(demandArray);
	        
	        ArrayList<Demand> dLst = demandArray;
	       	
	       	MyGraph g1= new MyGraph(g.K, g.w);
	        int[][] h1 = new int[n][d]; //h2[v][d]
	        int[][][] l1 = new int[n][n][d];//l2[u][v][d]
	        for(int v = 0; v<n; v++) {
				for (int dId = 0;dId<demandArray.size(); dId++) {
					h1[v][demandArray.get(dId).idS()-1] = 0;
					
				}
	        }
	        
	        for(int v = 0; v<n; v++) {
	        	 for(int u = 0; u<n; u++) {
				for (int dId = 0;dId<demandArray.size(); dId++) {
					l1[v][u][demandArray.get(dId).idS()-1] = 0;
				}
				}
	        }
	        
			for (int i = 0; i < dLst.size(); i++) {

				Demand _d = dLst.get(i);
				int src = _d.sourceS();
				int dest = _d.destinationS();
				ArrayList<ArrayList<Integer>> allPaths = getMultiPaths(src, dest, g1, _d.bwS()*1.02);
				if (allPaths.isEmpty()) {
					System.out.println("Path no failure routing violation: " + src + "," + dest);
					out.write("Path no failure routing violation: " + src + "," + dest + "\n");

					//no solution -> input is imposible to solve
					//acceptDemands --;
					//continue;
					UD = -1;
					UC = -1;
					UQ = -1;
					Ub=-1;
					return;
				}
				for (ArrayList<Integer> p : allPaths) {	
					ArrayList<Integer> _cap = new ArrayList<Integer>(g1.K);
				
					
					System.out.println(" \nPath for demand "+ _d.idS());		
					out.write("Path for demand "+ _d.idS() + " in case no failure\n");	
					for(int j=0;j<p.size();j++) {
						System.out.print(p.get(j) + " ");
						out.write(p.get(j) + " ");
					}
					System.out.println();
					out.write("\n");
					
					
					for (int j = 0; j < p.size() - 1; j++) {
						int w = g1.getEdgeWeight(p.get(j), p.get(j + 1)) - (int)(_d.bwS()*1.02);
						g1.setEdgeWeight(p.get(j), p.get(j + 1), w);

					}
					//h1[v][d]
					for(int j=0;j<p.size();j++) {
						h1[p.get(j)-1][_d.idS()-1]  = 1;
						System.out.println ("H1: "+ h1[p.get(j)-1][_d.idS()-1]);
					}
					// l1[u][v][d]
					for (int j = 0; j < p.size() - 1; j++) {
						l1[p.get(j) - 1][p.get(j+1) - 1][_d.idS() - 1] = 1;
						System.out.println("l1: " + l1[p.get(j) - 1][p.get(j+1) - 1][_d.idS() - 1]);

					}	
					break;
					
				}
			}
	        
	       
				
	       	
			try{
				GRBEnv env = new GRBEnv("qp.log");
				
				env.set(GRB.DoubleParam.MIPGap, 0.0000005);
				env.set(GRB.DoubleParam.FeasibilityTol, 0.00000001);
				env.set(GRB.IntParam.Threads,8);
				env.set(GRB.DoubleParam.TimeLimit,4000);
				GRBModel model = new GRBModel(env);
				model.getEnv().set(GRB.IntParam.PreCrush,1);//add cut
				model.getEnv().set(GRB.IntParam.FlowCoverCuts, 0);
				model.getEnv().set(GRB.IntParam.Cuts, 0);
				model.getEnv().set(GRB.DoubleParam.Heuristics,0);
				model.getEnv().set(GRB.IntParam.Presolve,0);
				

				
	//variable declaire
				
				System.out.println(demandArray.size());
				System.out.println(demandArray.get(0).getFunctions().length);
				System.out.println(n);
				
				for(int v = 0; v<n; v++) {
					for (int dId = 0;dId<demandArray.size(); dId++) {
						for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
							//Function _f = demandArray.get(dId).getFunctions()[i];
							String st = "x["+(v+1)+ "]["+(demandArray.get(dId).idS())+ "]["+i+ "]";
			    			x[v][demandArray.get(dId).idS()-1][i] = model.addVar(0, 1, 0, GRB.BINARY, st);
							
						}
					}
				}
				
				
				// Z[d]
				for (int dId = 0; dId < demandArray.size(); dId++) {// Function _f =
																	// demandArray.get(dId).getFunctions()[i];
					String st = "Z[" + (demandArray.get(dId).idS()) + "]";
					Z[demandArray.get(dId).idS() - 1] = model.addVar(0, 1, 0, GRB.BINARY, st);

				}
		model.update();
				

				//add variable to process max Uc (13)
				double maxUc = 0;
				for(int v = 0; v<n; v++) {
					for (int dId = 0;dId<demandArray.size(); dId++) {
						double bwD = demandArray.get(dId).bwS();
						for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {							
							Function _f = demandArray.get(dId).getFunctions()[i];
							maxUc +=bwD*_f.getReq()*nodeCost[v];

						}
					}
				}
				
				out.write(maxUc + "\n");
				
			
				//(13) constant
				GRBLinExpr obj = new GRBLinExpr();
				obj.addConstant(d);
				for (int dId = 0;dId<demandArray.size(); dId++) {
					obj.addTerm(-1, Z[demandArray.get(dId).idS() - 1]);
				}
				out.write(objStr + " preee\n");
				if (objStr.equals("PTO")) {
					out.write(objStr + " ddjdjjd\n");
					for (int v = 0; v < n; v++) {
						for (int dId = 0; dId < demandArray.size(); dId++) {
							double bwD = demandArray.get(dId).bwS();
							for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								Function _f = demandArray.get(dId).getFunctions()[i];
								obj.addTerm(bwD * _f.getReq() * nodeCost[v] / maxUc,
										x[v][demandArray.get(dId).idS() - 1][i]);

							}
						}
					}
				}
				
				if (objStr.equals("PTO_q")) {
					double maxUq = 0;					
					for (int dId = 0; dId < demandArray.size(); dId++) {
						for (int v = 0; v < n; v++) {
							/*
							 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
							 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
							 * demandArray.get(dId)); }
							 */
							maxUq += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
									* 0.1;
						}
						for (int v = 0; v < n; v++) {
							for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								Function _f = demandArray.get(dId).getFunctions()[i];
								maxUq +=functionNodeDelay[_f.id() - 1][v];
							}
						}
					}
					
					for (int dId = 0; dId < demandArray.size(); dId++) {
						double param = 0.0;
						for (int v = 0; v < n; v++) {
							/*
							 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
							 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
							 * demandArray.get(dId)); }
							 */
							param += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
									* 0.1;

						}
						obj.addConstant(param/maxUq);
						for (int v = 0; v < n; v++) {

							for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								Function _f = demandArray.get(dId).getFunctions()[i];
								obj.addTerm(functionNodeDelay[_f.id() - 1][v]/maxUq, x[v][demandArray.get(dId).idS() - 1][i]);

							}
						}
					}
				}

				if (objStr.equals("PTO_b")) {
					double maxW = 0;
					for (int v = 0; v < n; v++) {
						if (nodeType[v] == 2) {
							for (int dId = 0;dId<demandArray.size(); dId++) {
								for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									Function _f = demandArray.get(dId).getFunctions()[i];
									maxW +=_f.getReq()/g.K.get(v);
							
							
								}
							}						
							
						
						} else if (nodeType[v] == 3) {
							for (int dId = 0;dId<demandArray.size(); dId++) {
								for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									Function _f = demandArray.get(dId).getFunctions()[i];
									maxW += _f.getReq()/g.K.get(v);
							
								}
							}							
							
						}
					}
					
					GRBVar[] temp_we = new GRBVar[n];					
					
					GRBVar[] temp_wc = new GRBVar[n];
					for (int v = 0; v < n; v++) {
						if (nodeType[v] == 2) {
							 String st = "temp_we[" + (v) + "]";
							 temp_we[v] = model.addVar(0, maxW, 0, GRB.CONTINUOUS, st);
						} else if (nodeType[v] == 3) {
							 String st = "temp_wc[" + (v) + "]";
							 temp_wc[v] = model.addVar(0, maxW, 0, GRB.CONTINUOUS, st);
							
						}
					}
					for (int v = 0; v < n; v++) {
						if (nodeType[v] == 2) {
							GRBLinExpr expr = new GRBLinExpr();
							for (int dId = 0;dId<demandArray.size(); dId++) {
								for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									Function _f = demandArray.get(dId).getFunctions()[i];
									expr.addTerm(_f.getReq()/g.K.get(v), x[v][demandArray.get(dId).idS()-1][i]);
							
							
								}
							}
							expr.addTerm(-1, temp_we[v]);
							String st = "expr_we["+(v) +"]";
							model.addConstr(expr, GRB.EQUAL, 0, st);
							expr = null;
							
						
						} else if (nodeType[v] == 3) {
							GRBLinExpr expr = new GRBLinExpr();
							for (int dId = 0;dId<demandArray.size(); dId++) {
								for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									Function _f = demandArray.get(dId).getFunctions()[i];
									expr.addTerm(_f.getReq()/g.K.get(v), x[v][demandArray.get(dId).idS()-1][i]);
							
							
								}
							}
							expr.addTerm(-1, temp_wc[v]);
							String st = "expr_wc["+(v) +"]";
							model.addConstr(expr, GRB.EQUAL, 0, st);
							expr = null;
							
						}
					}
				 we = model.addVar(0, maxW, 0, GRB.CONTINUOUS, "we");

					model.addGenConstrMax(we, temp_we, maxW, "we_exp");
					wc = model.addVar(0, maxW, 0, GRB.CONTINUOUS, "wc");

					model.addGenConstrMax(wc, temp_wc, maxW, "wc_exp");
					
					
					obj.addTerm(1/ 2*maxW, we);
					obj.addTerm(1/ 2*maxW, wc);

				}
				
		
					model.setObjective(obj,GRB.MINIMIZE);	
				//add constraints

				/*	//(4)
					
					for (int dId = 0; dId < demandArray.size(); dId++) {
						for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
							GRBLinExpr expr1 = new GRBLinExpr();
							expr1.addTerm(-1, U[demandArray.get(dId).idS()-1][i]);
							for (int v = 0; v < n; v++) {
								expr1.addTerm(v+1, x[v][demandArray.get(dId).idS()-1][i]);
							}
							String st = "expr1["+(demandArray.get(dId).idS()-1)+ "]["+(i)+ "]";
							model.addConstr(expr1, GRB.EQUAL, 0, st);
							expr1 = null;
						}
					}*/
					
					//(5)
					for(int v = 0; v<n; v++) {
						GRBLinExpr expr2 = new GRBLinExpr();
						for (int dId = 0;dId<demandArray.size(); dId++) {
							for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								Function _f = demandArray.get(dId).getFunctions()[i];
								expr2.addTerm(piFunction(i, demandArray.get(dId))*_f.getReq(), x[v][demandArray.get(dId).idS()-1][i]);
								//expr5.addTerm(_f.getReq(), x[v][demandArray.get(dId).idS()-1][i]);
						
							}
						}
						String st = "expr2["+(v) +"]";
						model.addConstr(expr2, GRB.LESS_EQUAL, g.K.get(v), st);
						expr2 = null;
					}
					System.gc();
					// (7)
					for (int dId = 0; dId < demandArray.size(); dId++) {
						for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
							GRBLinExpr expr3 = new GRBLinExpr();
							for(int v = 0; v<n; v++) {
								expr3.addTerm(1, x[v][demandArray.get(dId).idS()-1][i]);
							
							}
							String st = "expr3["+(demandArray.get(dId).idS()-1)+ "]["+(i)+ "]";
							model.addConstr(expr3, GRB.LESS_EQUAL, 1, st);
							expr3 = null;
						}
					}
					System.gc();
					
					//(8)
					for (int dId = 0; dId < demandArray.size(); dId++) {
						GRBLinExpr expr4 = new GRBLinExpr();
						int jd =  demandArray.get(dId).getFunctions().length;
						for (int i = 0; i <jd; i++) {
							
							for(int v = 0; v<n; v++) {
								expr4.addTerm(1, x[v][demandArray.get(dId).idS()-1][i]);
							
							}
							
						}
						expr4.addTerm(-jd, Z[demandArray.get(dId).idS()-1]);
						String st = "expr4["+(demandArray.get(dId).idS()-1)+ "]";
						model.addConstr(expr4, GRB.EQUAL, 0, st);
						expr4 = null;
					}
					System.gc();
					
					//(9)
					
					for (int dId = 0; dId < demandArray.size(); dId++) {
						for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
							for (int v = 0; v < n; v++) {
								if ((nodeType[v] == 2 && demandArray.get(dId).getFunctions()[i].getType() == 3)
										|| (nodeType[v] == 3
												&& demandArray.get(dId).getFunctions()[i].getType() == 2)) {
									GRBLinExpr expr5 = new GRBLinExpr();
									expr5.addTerm(1, x[v][demandArray.get(dId).idS() - 1][i]);
									String st = "expr4[" + (v) + "][" + (demandArray.get(dId).idS() - 1) + "][" + (i)
											+ "]";
									model.addConstr(expr5, GRB.EQUAL, 0, st);
									expr5 = null;
								}
							}
						}
					}
					System.gc();
					
					
					//(10)(11)
					
					//use rate_demand as delay of demand
					System.out.println("constraint 7");
					for (int dId = 0;dId<demandArray.size(); dId++) {
						GRBLinExpr expr7 = new GRBLinExpr();
						double param = 0.0;
						for(int v = 0; v<n; v++) {
							/*for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								param += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i, demandArray.get(dId));						
							}*/
							param += nodeDelay[v] * h1[v][demandArray.get(dId).idS()-1] * demandArray.get(dId).bwS()*0.1;		
							
						}
						param -= demandArray.get(dId).getRate();
						for(int v = 0; v<n; v++) {
							
							for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								Function _f = demandArray.get(dId).getFunctions()[i];
								expr7.addTerm(functionNodeDelay[_f.id()-1][v],x[v][demandArray.get(dId).idS()-1][i]);
								
							}
						}
						String st = "expr7["+(demandArray.get(dId).idS()-1) +"]";
						model.addConstr(expr7, GRB.LESS_EQUAL, -param, st);
						
						expr7 = null;
						
					}
					System.gc();
					
					
				
				
			

				// Optimize model
				try {
					
					model.optimize();
					
					
				UD = 0;
				UC=0;
				model.write("model1.lp");
					out.write("Solution for the problem:");
					out.newLine();
				
					int optimstatus = model.get(GRB.IntAttr.Status); 
					if (optimstatus == GRB.Status.OPTIMAL) 
					{ 
						out.write("UC1111 =" +UC +"\n");
						//r_min= r.get(GRB.DoubleAttr.X);
						value_final = obj.getValue();
						out.write("Objective optimal Value: "+obj.getValue());
						out.newLine();
						
						//_acceptNo = (value_final + maxLinkLoad + maxNodeLoad)*demandArray.size()/2;
						//out.write("Particularly,"+r_l+":"+r_n);
						out.newLine();
						
						out.write(objStr + "\n");
						
						UD = d;
						for (int dId = 0; dId < demandArray.size(); dId++) {
							if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
								UD = UD - 1;
							}
						}

						// UC
						if (objStr.equals("PTO")) {
							out.write(objStr + "\n");
							UC = 0;
							for (int v = 0; v < n; v++) {
								for (int dId = 0; dId < demandArray.size(); dId++) {

									for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
										if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
											double bwD = demandArray.get(dId).bwS();
											Function _f = demandArray.get(dId).getFunctions()[i];
											UC += bwD * _f.getReq() * nodeCost[v];											

										}
									}
								}
							}
							out.write("UC222 =" +UC +"\n");
						} else if (objStr.equals("PTO_q")) {
							UQ = 0;
							for (int dId = 0; dId < demandArray.size(); dId++) {
								double param = 0.0;
								for (int v = 0; v < n; v++) {
									/*
									 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
									 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
									 * demandArray.get(dId)); }
									 */
									param += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
											* 0.1;

								}
								UQ+= param;
								for (int v = 0; v < n; v++) {
									for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
										if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {

											Function _f = demandArray.get(dId).getFunctions()[i];
											UQ += functionNodeDelay[_f.id() - 1][v];
										}
									}
								}
							}
							
						} else if (objStr.equals( "PTO_b") ) {
							Ub = 0;
							if (we.get(GRB.DoubleAttr.X) > 0) {
								Ub+=we.get(GRB.DoubleAttr.X);
							}
							if (wc.get(GRB.DoubleAttr.X) > 0) {
								Ub+=wc.get(GRB.DoubleAttr.X);
							}

							
							
						}
						
						
						for (int v = 0; v < n; v++) {
							for (int dId = 0; dId < demandArray.size(); dId++) {
								for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
										out.write(x[v][demandArray.get(dId).idS() - 1][i].get(GRB.StringAttr.VarName)
												+ " : "
												+ x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X));
										out.newLine();
									}

								}
							}
						}
						for (int dId = 0; dId < demandArray.size(); dId++) {

							if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
								out.write(Z[demandArray.get(dId).idS() - 1].get(GRB.StringAttr.VarName) + " : "
										+ Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X));
								out.newLine();
							}

						}
					
		    			out.newLine();
				
					 } else if (optimstatus == GRB.Status.INF_OR_UNBD) 
					 	{ 
					        System.out.println("Model is infeasible or unbounded"); 
					        return;
					 	} else if (optimstatus == GRB.Status.INFEASIBLE) 
					        	{ 
							        System.out.println("Model is infeasible AAAAAAAAAAAAAA"); 
							        return; 
					        	} else if (optimstatus == GRB.Status.INTERRUPTED)
					        	{
					        		out.write("UC333 =" +UC +"\n");
					        		//r_min= r.get(GRB.DoubleAttr.X);
									value_final = obj.getValue();
									out.write("Objective optimal Value: "+obj.getValue());
									out.newLine();
					        	
									//_acceptNo = (value_final + maxLinkLoad + maxNodeLoad)*demandArray.size()/2;
									//out.write("Particularly,"+r_l+":"+r_n);
									out.newLine();
									

									UD = d;
									for (int dId = 0; dId < demandArray.size(); dId++) {
										if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
											UD = UD - 1;
										}
									}

									// UC
									if (objStr.equals( "PTO")) {
										UC = 0;
										for (int v = 0; v < n; v++) {
											for (int dId = 0; dId < demandArray.size(); dId++) {

												for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
													if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
														double bwD = demandArray.get(dId).bwS();
														Function _f = demandArray.get(dId).getFunctions()[i];
														UC += bwD * _f.getReq() * nodeCost[v];

													}
												}
											}
										}
										out.write("UC444 =" +UC +"\n");
									} else if (objStr.equals("PTO_q")) {
										UQ = 0;
										for (int dId = 0; dId < demandArray.size(); dId++) {
											double param = 0.0;
											for (int v = 0; v < n; v++) {
												/*
												 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
												 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
												 * demandArray.get(dId)); }
												 */
												param += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
														* 0.1;

											}
											UQ+= param;
											for (int v = 0; v < n; v++) {
												for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
													if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {

														Function _f = demandArray.get(dId).getFunctions()[i];
														UQ += functionNodeDelay[_f.id() - 1][v];
													}
												}
											}
										}
										
									} else if (objStr.equals("PTO_b"))  {
										Ub = 0;
										if (we.get(GRB.DoubleAttr.X) > 0) {
											Ub+=we.get(GRB.DoubleAttr.X);
										}
										if (wc.get(GRB.DoubleAttr.X) > 0) {
											Ub+=wc.get(GRB.DoubleAttr.X);
										}

										
										
									}
									
									for (int v = 0; v < n; v++) {
										for (int dId = 0; dId < demandArray.size(); dId++) {
											for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
												if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
													out.write(x[v][demandArray.get(dId).idS() - 1][i].get(GRB.StringAttr.VarName)
															+ " : "
															+ x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X));
													out.newLine();
												}

											}
										}
									}
									for (int dId = 0; dId < demandArray.size(); dId++) {

										if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
											out.write(Z[demandArray.get(dId).idS() - 1].get(GRB.StringAttr.VarName) + " : "
													+ Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X));
											out.newLine();
										}

									}
								
					    			out.newLine();
					        		
					        	}
					
					 else
					 {
						//r_min= r.get(GRB.DoubleAttr.X);
							value_final = obj.getValue();
							out.write("Objective optimal Value: "+obj.getValue());
							out.newLine();
		
							out.write("UC555 =" +UC +"\n");

							UD = d;
							for (int dId = 0; dId < demandArray.size(); dId++) {
								if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
									UD = UD - 1;
								}
							}

							// UC
							if (objStr.equals("PTO")) {
								UC = 0;
								for (int v = 0; v < n; v++) {
									for (int dId = 0; dId < demandArray.size(); dId++) {

										for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
											if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
												double bwD = demandArray.get(dId).bwS();
												Function _f = demandArray.get(dId).getFunctions()[i];
												UC += bwD * _f.getReq() * nodeCost[v];

											}
										}
									}
								}
								out.write("UC666 =" +UC +"\n");
							} else if (objStr.equals("PTO_q")) {
								UQ = 0;
								for (int dId = 0; dId < demandArray.size(); dId++) {
									double param = 0.0;
									for (int v = 0; v < n; v++) {
										/*
										 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
										 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
										 * demandArray.get(dId)); }
										 */
										param += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
												* 0.1;

									}
									UQ+= param;
									for (int v = 0; v < n; v++) {
										for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
											if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {

												Function _f = demandArray.get(dId).getFunctions()[i];
												UQ += functionNodeDelay[_f.id() - 1][v];
											}
										}
									}
								}
								
							} else if (objStr.equals("PTO_b"))  {
								Ub = 0;
								if (we.get(GRB.DoubleAttr.X) > 0) {
									Ub+=we.get(GRB.DoubleAttr.X);
								}
								if (wc.get(GRB.DoubleAttr.X) > 0) {
									Ub+=wc.get(GRB.DoubleAttr.X);
								}

								
								
							}
							
							for (int v = 0; v < n; v++) {
								for (int dId = 0; dId < demandArray.size(); dId++) {
									for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
										if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
											out.write(x[v][demandArray.get(dId).idS() - 1][i].get(GRB.StringAttr.VarName)
													+ " : "
													+ x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X));
											out.newLine();
										}

									}
								}
							}
							for (int dId = 0; dId < demandArray.size(); dId++) {

								if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
									out.write(Z[demandArray.get(dId).idS() - 1].get(GRB.StringAttr.VarName) + " : "
											+ Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X));
									out.newLine();
								}

							}
						
			    			out.newLine();
			    			out.newLine();
							
					  }
				
					
				} catch (Exception e) {
					//r_min= r.get(GRB.DoubleAttr.X);
					value_final = obj.getValue();
					out.write("Objective optimal Value: "+obj.getValue());
					out.newLine();
	        		
					//Ud ->case1
					out.write("UC777 =" +UC +"\n");
					UD = d;
					for (int dId = 0; dId < demandArray.size(); dId++) {
						if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
							UD = UD - 1;
						}
					}
					// UC
					if (objStr.equals( "PTO")) {
						UC = 0;
						for (int v = 0; v < n; v++) {
							for (int dId = 0; dId < demandArray.size(); dId++) {

								for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
										double bwD = demandArray.get(dId).bwS();
										Function _f = demandArray.get(dId).getFunctions()[i];
										UC += bwD * _f.getReq() * nodeCost[v];

									}
								}
							}
						}
						out.write("UC999 =" +UC +"\n");
					} else if (objStr.equals( "PTO_q")) {
						UQ = 0;
						for (int dId = 0; dId < demandArray.size(); dId++) {
							double param = 0.0;
							for (int v = 0; v < n; v++) {
								/*
								 * for(int i = 0; i < demandArray.get(dId).getFunctions().length; i++) { param
								 * += nodeDelay[v] * h2[v][demandArray.get(dId).idS()-1] * piFunction(i,
								 * demandArray.get(dId)); }
								 */
								param += nodeDelay[v] * h1[v][demandArray.get(dId).idS() - 1] * demandArray.get(dId).bwS()
										* 0.1;

							}
							UQ+= param;
							for (int v = 0; v < n; v++) {
								for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
									if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {

										Function _f = demandArray.get(dId).getFunctions()[i];
										UQ += functionNodeDelay[_f.id() - 1][v];
									}
								}
							}
						}
						
					} else if (objStr.equals("PTO_b"))  {
						Ub = 0;
						if (we.get(GRB.DoubleAttr.X) > 0) {
							Ub+=we.get(GRB.DoubleAttr.X);
						}
						if (wc.get(GRB.DoubleAttr.X) > 0) {
							Ub+=wc.get(GRB.DoubleAttr.X);
						}

						
						
					}
					for (int v = 0; v < n; v++) {
						for (int dId = 0; dId < demandArray.size(); dId++) {
							for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
								if (x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X) > 0) {
									out.write(x[v][demandArray.get(dId).idS() - 1][i].get(GRB.StringAttr.VarName)
											+ " : "
											+ x[v][demandArray.get(dId).idS() - 1][i].get(GRB.DoubleAttr.X));
									out.newLine();
								}

							}
						}
					}
					for (int dId = 0; dId < demandArray.size(); dId++) {

						if (Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X) > 0) {
							out.write(Z[demandArray.get(dId).idS() - 1].get(GRB.StringAttr.VarName) + " : "
									+ Z[demandArray.get(dId).idS() - 1].get(GRB.DoubleAttr.X));
							out.newLine();
						}

					}
				
	    			out.newLine();
					
	
				}
					model.dispose();
				env.dispose();
				System.gc();
			
				} catch(GRBException e3){			
					System.out.println("Error code1: " + e3.getErrorCode() + ". " +
							e3.getMessage());
					System.out.print("This problem can't be solved");
					
					
					}
			} catch ( IOException e1 ) {
					e1.printStackTrace();
					} finally {
						if ( out != null )
							try {
								out.close();
								} catch (IOException e) {
									e.printStackTrace();}
						}    
			try {
		  		out.close();
		  		} catch (IOException e2) {
		  			e2.printStackTrace();
		  			}
	
		
	}
	
	public static myTuple initFunction_q(int v, int u, Demand _d, int cap_v, double theta) {
		int index = 0;
		double cost_v = nodeDelay[v] * _d.bwS() *0.1;
		int req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;

		while ((index < _d.getFunctions().length) &&
				( req <= cap_v))  {
			if ((_d.getFunctions()[index].getType() == 2 && nodeType[v] == 2)
				||(_d.getFunctions()[index].getType() == 3 && nodeType[v] == 3)) {
				cost_v += functionNodeDelay[_d.getFunctions()[index].id() - 1][v];
				cap_v = cap_v - req;					
			} else {
				if (index > 0) {
					index--;
				}
				break;
			}
			if (index == _d.getFunctions().length -1) {
				break;
			}
			index++;
			req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;
		
		}
			if (cost_v == 0) {
				return new myTuple(v, -1.0, -1, cap_v, u);
			}
		cost_v += (_d.getFunctions().length - index)*theta;
		
		return new myTuple(v, cost_v, index, cap_v, u);
	}
	
	public static void UpdateFunction_q(myTuple vstar, int v, int cap_v, ArrayList<myTuple> labels, Demand _d, double theta  ) {
		if (vstar.getIndex() == _d.getFunctions().length -1) return;
		int index = vstar.getIndex() + 1;
		double cost_temp = nodeDelay[v] * _d.bwS()* 0.1;
		int req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;

		boolean finished = false;
		while ((index < _d.getFunctions().length) &&
				( req <= cap_v))  {
			if ((_d.getFunctions()[index].getType() == 2 && nodeType[v] == 2)
				||(_d.getFunctions()[index].getType() == 3 && nodeType[v] == 3)) {
				cost_temp += functionNodeDelay[_d.getFunctions()[index].id() - 1][v];
				cap_v = cap_v - req;
				
			} else {
				if (index > 0) {
					index--;
				}
				break;
			}
			if (index == _d.getFunctions().length -1) {
				finished = true;
				break;
			}
			index++;
			req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;		
		}
		if (index == vstar.getIndex()) return; //can not put function in this node
		double cost_v1;
		if (!finished) {
			cost_v1 =  vstar.getCost() + cost_temp - (index - vstar.getIndex())*theta;
		} else {
			cost_v1 = vstar.getCost() + cost_temp;
			
		}

		int index_v = -1;
		for (int i= 0; i< labels.size();i++) {
			if (labels.get(i).getNodeId() == v) {
				index_v = i;
				break;
			}
		}
		if (index_v== -1) {
			//add v to labels
			labels.add(new myTuple(v,cost_v1,index, cap_v, vstar.getNodeId()));
			return;
		}
		if (labels.get(index_v).getCost()== -1 || labels.get(index_v).getCost() > cost_v1)  {
			labels.get(index_v).setCost(cost_v1);
			labels.get(index_v).setCapacity(cap_v);
			labels.get(index_v).setIndex(index);
			labels.get(index_v).setPreNode(vstar.getNodeId());
		}
	}
	
	public static myTuple initFunction(int v, int u, Demand _d, int cap_v, double theta) {
		int index = 0;
		double cost_v = 0.0;
		int req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;

		while ((index < _d.getFunctions().length) &&
				( req <= cap_v))  {
			if ((_d.getFunctions()[index].getType() == 2 && nodeType[v] == 2)
				||(_d.getFunctions()[index].getType() == 3 && nodeType[v] == 3)) {
				cost_v += req * nodeCost[v];
				cap_v = cap_v - req;					
			} else {
				if (index > 0) {
					index--;
				}
				break;
			}
			if (index == _d.getFunctions().length -1) {
				break;
			}
			index++;
			req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;
		
		}
			if (cost_v == 0) {
				return new myTuple(v, -1.0, -1, cap_v, u);
			}
		cost_v += (_d.getFunctions().length - index)*theta;
		
		return new myTuple(v, cost_v, index, cap_v, u);
	}
	
	public static void UpdateFunction(myTuple vstar, int v, int cap_v, ArrayList<myTuple> labels, Demand _d, double theta  ) {
		if (vstar.getIndex() == _d.getFunctions().length -1) return;
		int index = vstar.getIndex() + 1;
		double cost_temp = 0.0;
		int req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;

		boolean finished = false;
		while ((index < _d.getFunctions().length) &&
				( req <= cap_v))  {
			if ((_d.getFunctions()[index].getType() == 2 && nodeType[v] == 2)
				||(_d.getFunctions()[index].getType() == 3 && nodeType[v] == 3)) {
				cost_temp += req * nodeCost[v];
				cap_v = cap_v - req;
				
			} else {
				if (index > 0) {
					index--;
				}
				break;
			}
			if (index == _d.getFunctions().length -1) {
				finished = true;
				break;
			}
			index++;
			req = piFunction(index, _d)* _d.getFunctions()[index].getReq() ;		
		}
		if (index == vstar.getIndex()) return; //can not put function in this node
		double cost_v1;
		if (!finished) {
			cost_v1 =  vstar.getCost() + cost_temp - (index - vstar.getIndex())*theta;
		} else {
			cost_v1 = vstar.getCost() + cost_temp;
			
		}

		int index_v = -1;
		for (int i= 0; i< labels.size();i++) {
			if (labels.get(i).getNodeId() == v) {
				index_v = i;
				break;
			}
		}
		if (index_v== -1) {
			//add v to labels
			labels.add(new myTuple(v,cost_v1,index, cap_v, vstar.getNodeId()));
			return;
		}
		if (labels.get(index_v).getCost()== -1 || labels.get(index_v).getCost() > cost_v1)  {
			labels.get(index_v).setCost(cost_v1);
			labels.get(index_v).setCapacity(cap_v);
			labels.get(index_v).setIndex(index);
			labels.get(index_v).setPreNode(vstar.getNodeId());
		}
	}
	public static myTuple getMinTuple(ArrayList<myTuple> labels, ArrayList<Integer> delta) {
		myTuple minTuple = new myTuple();
		double minval = -1;
		for (int i = 0; i < labels.size(); i++) {
			if (!delta.contains(labels.get(i).getNodeId())) {
				if (labels.get(i).getCost()>0 && (minval == -1 || minval > labels.get(i).getCost())) {
					minTuple = labels.get(i);
					minval = labels.get(i).getCost();
				}
			}
		}
		return minTuple;		
	}
	
	public static myTuple getTuple(int u, ArrayList<myTuple> labels) {
		for (int i = 0; i<labels.size();i++) {
			if (labels.get(i).getNodeId() == u) 
				return labels.get(i);
		}
		return new myTuple();
	}
	
	public static void PTH_q(String outFile) { //HEURISTIC
		try {

			// File file = new File(outFile);
			// out = new BufferedWriter(new FileWriter(file));
			out = new BufferedWriter(new FileWriter(outFile));
			out.write("number of function:" + m);
			out.newLine();
			for (int i = 0; i < m; i++) {
				out.write(functionArr[i].toString());
				out.newLine();
			}
			out.write("number of Demand:" + d);
			out.newLine();
			for (int i = 0; i < d; i++) {
				out.write(demandArray.get(i).toString());
				out.newLine();
			}
			out.write("virtual node:" + n);
			out.newLine();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++)
					out.write(g.getEdgeWeight(i + 1, j + 1) + " ");
				out.newLine();
			}

			for (int i = 0; i < n; i++) {
				out.write(failure[i] + " ");
			}
			out.newLine();

			UD = 0.0;
			UC = 0.0;
			UQ = 0.0;
			Ub = 0.0;
			//calculate theta_q: The maximum value of the total delay for routing and processing when deploy any
			//function of any demand on any node
			double theta = 0.0;
			
			for (int v = 0; v < n; v++) {
				for (int dId = 0; dId < demandArray.size(); dId++) {
					double bwD = demandArray.get(dId).bwS();
					for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
						Function _f = demandArray.get(dId).getFunctions()[i];
						if (theta < functionNodeDelay[_f.id() - 1][v] + nodeDelay[v] * demandArray.get(dId).bwS()* 0.1) {
							theta = functionNodeDelay[_f.id() - 1][v] + nodeDelay[v] * demandArray.get(dId).bwS()* 0.1;
						}

					}
				}
			}

			MyGraph g1 = new MyGraph(g.K, g.w);
			acceptDemands = demandArray.size();
			ArrayList<Demand> dLst = sortDemandRequiredCores(demandArray);
	
			
			for (int i = 0; i <dLst.size();i++) {

				ArrayList<myTuple> labels = new ArrayList<>();
				
				ArrayList<Integer> delta = new ArrayList<>();
				
				Demand _d = dLst.get(i);
				int _src= _d.sourceS();
				int _dest = _d.destinationS();
				double bwD = _d.bwS();
			
				delta.add(_src - 1);
				for (int u =0; u<n; u++) {
					double cap_e = g1.w.get(_src -1).get(u);
					if ( cap_e > _d.bwS()) { 
						//(s,u) is an edge 
						myTuple tuple_u = initFunction_q(u,_src-1, _d, g1.K.get(u), theta);
						labels.add(tuple_u);						
					} 
				}
				boolean failed = false;
				do {					
					myTuple vstar = getMinTuple(labels, delta);
					if (vstar.getCost() == -1) {
						acceptDemands--;
						//System.out.println ("request refused");
						failed = true;
						break;
					}
					if (vstar.getIndex() == _d.getFunctions().length-1) {
						//System.out.println ("request accepted");
						failed = false;
						break;
					}
					delta.add(vstar.getNodeId());
					for (int u = 0; u < n; u++) {
						double cap_e = g1.w.get(vstar.getNodeId()).get(u);
						if (!delta.contains(u) && cap_e > _d.bwS()) {
							UpdateFunction_q(vstar,u,g1.K.get(u), labels, _d, theta);							
						}
					}
				} while (true);	
				if (failed) continue;
				ArrayList<myTuple> candidates = new ArrayList<>();
				int demandNumber = _d.getFunctions().length;
				for (int j= 0; j< labels.size();j++) {
					if (labels.get(j).getIndex() == demandNumber-1) {
						candidates.add(labels.get(j));
					}
				}
				//select min
				failed = true;
				while (!candidates.isEmpty()) {
					myTuple result = getMinTuple(candidates, new ArrayList<Integer>());
					int node_result = result.getNodeId();
					
					//check if it exist path from node_result to _dest 
					ArrayList<Integer> path = maxCapPath(node_result + 1, _dest, g1, _d.bwS());
					
					if (path !=null && !path.isEmpty()) {
						failed = false;						
						int currIndex= result.getIndex();
						//update capacity
						g1.setCap(node_result + 1, (int) result.getCapacity());
						int prevNode = result.getPreNode();
						
						//update capacity
						int startIndex = -1;
						while (node_result!= _src -1) {
							startIndex = 0; //start from prevIndex + 1
							int prevNode_backup = -1;
							if (prevNode != _src - 1) {
								myTuple nn = getTuple(prevNode, labels);
								startIndex= nn.getIndex() + 1;	
								prevNode_backup = nn.getPreNode();
							}
							
							for (int idx = startIndex; idx <= currIndex; idx ++) {
								UC+= bwD * _d.getFunctions()[idx].getReq() * nodeCost[node_result];
								out.write("function " + idx + " of demand " + _d.idS() + " is placed to" + (node_result +1) );
								out.newLine();								
							}
							//update capacity
							g1.setCap(node_result + 1, (int) result.getCapacity());						
							
							node_result = prevNode;
							currIndex = startIndex -1;							
							prevNode = prevNode_backup;
						}
						if (startIndex > 0) {
							//startIndex = 0 to current Index is placed to src node
							
							for (int idx = 0; idx < startIndex; idx ++) {
								UC+= bwD * _d.getFunctions()[idx].getReq() * nodeCost[_src-1];
								out.write("function " + idx + " of demand " + _d.idS() + " is placed to" + (_src) );
								out.newLine();	
								g1.setCap(_src, g1.getCap(_src) - _d.getFunctions()[idx].getReq());
							}							
						
						}						
						break;
					}
					candidates.remove(result);
				}
				if (failed) {
					acceptDemands--;	
					//System.out.println ("request refused");
				} 
							
			}
			out.write("UC = " + UC);
			System.out.println("UC = " + UC);
			System.out.println();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}
	
	public static void PTH(String outFile) { //HEURISTIC
		try {

			// File file = new File(outFile);
			// out = new BufferedWriter(new FileWriter(file));
			out = new BufferedWriter(new FileWriter(outFile));
			out.write("number of function:" + m);
			out.newLine();
			for (int i = 0; i < m; i++) {
				out.write(functionArr[i].toString());
				out.newLine();
			}
			out.write("number of Demand:" + d);
			out.newLine();
			for (int i = 0; i < d; i++) {
				out.write(demandArray.get(i).toString());
				out.newLine();
			}
			out.write("virtual node:" + n);
			out.newLine();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++)
					out.write(g.getEdgeWeight(i + 1, j + 1) + " ");
				out.newLine();
			}

			for (int i = 0; i < n; i++) {
				out.write(failure[i] + " ");
			}
			out.newLine();

			UD = 0.0;
			UC = 0.0;
			UQ = 0.0;
			Ub = 0.0;
			//calculate theta: The maximum cost required to deploy any
			//function of any demand on any node
			double theta = 0.0;
			
			for (int v = 0; v < n; v++) {
				for (int dId = 0; dId < demandArray.size(); dId++) {
					double bwD = demandArray.get(dId).bwS();
					for (int i = 0; i < demandArray.get(dId).getFunctions().length; i++) {
						Function _f = demandArray.get(dId).getFunctions()[i];
						if (theta < bwD * _f.getReq() * nodeCost[v]) {
							theta = bwD * _f.getReq() * nodeCost[v];
						}

					}
				}
			}

			MyGraph g1 = new MyGraph(g.K, g.w);
			acceptDemands = demandArray.size();
			ArrayList<Demand> dLst = sortDemandRequiredCores(demandArray);
	
			
			for (int i = 0; i <dLst.size();i++) {

				ArrayList<myTuple> labels = new ArrayList<>();
				
				ArrayList<Integer> delta = new ArrayList<>();
				
				Demand _d = dLst.get(i);
				int _src= _d.sourceS();
				int _dest = _d.destinationS();
				double bwD = _d.bwS();
			
				delta.add(_src - 1);
				for (int u =0; u<n; u++) {
					double cap_e = g1.w.get(_src -1).get(u);
					if ( cap_e > _d.bwS()) { 
						//(s,u) is an edge 
						myTuple tuple_u = initFunction(u,_src-1, _d, g1.K.get(u), theta);
						labels.add(tuple_u);						
					} 
				}
				boolean failed = false;
				do {					
					myTuple vstar = getMinTuple(labels, delta);
					if (vstar.getCost() == -1) {
						acceptDemands--;
						//System.out.println ("request refused");
						failed = true;
						break;
					}
					if (vstar.getIndex() == _d.getFunctions().length-1) {
						//System.out.println ("request accepted");
						failed = false;
						break;
					}
					delta.add(vstar.getNodeId());
					for (int u = 0; u < n; u++) {
						double cap_e = g1.w.get(vstar.getNodeId()).get(u);
						if (!delta.contains(u) && cap_e > _d.bwS()) {
							UpdateFunction(vstar,u,g1.K.get(u), labels, _d, theta);							
						}
					}
				} while (true);	
				if (failed) continue;
				ArrayList<myTuple> candidates = new ArrayList<>();
				int demandNumber = _d.getFunctions().length;
				for (int j= 0; j< labels.size();j++) {
					if (labels.get(j).getIndex() == demandNumber-1) {
						candidates.add(labels.get(j));
					}
				}
				//select min
				failed = true;
				while (!candidates.isEmpty()) {
					myTuple result = getMinTuple(candidates, new ArrayList<Integer>());
					int node_result = result.getNodeId();
					
					//check if it exist path from node_result to _dest 
					ArrayList<Integer> path = maxCapPath(node_result + 1, _dest, g1, _d.bwS());
					
					if (path !=null && !path.isEmpty()) {
						failed = false;						
						int currIndex= result.getIndex();
						//update capacity
						g1.setCap(node_result + 1, (int) result.getCapacity());
						int prevNode = result.getPreNode();
						
						//update capacity
						int startIndex = -1;
						while (node_result!= _src -1) {
							startIndex = 0; //start from prevIndex + 1
							int prevNode_backup = -1;
							if (prevNode != _src - 1) {
								myTuple nn = getTuple(prevNode, labels);
								startIndex= nn.getIndex() + 1;	
								prevNode_backup = nn.getPreNode();
							}
							
							for (int idx = startIndex; idx <= currIndex; idx ++) {
								UC+= bwD * _d.getFunctions()[idx].getReq() * nodeCost[node_result];
								out.write("function " + idx + " of demand " + _d.idS() + " is placed to" + (node_result +1) );
								out.newLine();								
							}
							//update capacity
							g1.setCap(node_result + 1, (int) result.getCapacity());						
							
							node_result = prevNode;
							currIndex = startIndex -1;							
							prevNode = prevNode_backup;
						}
						if (startIndex > 0) {
							//startIndex = 0 to current Index is placed to src node
							
							for (int idx = 0; idx < startIndex; idx ++) {
								UC+= bwD * _d.getFunctions()[idx].getReq() * nodeCost[_src-1];
								out.write("function " + idx + " of demand " + _d.idS() + " is placed to" + (_src) );
								out.newLine();	
								g1.setCap(_src, g1.getCap(_src) - _d.getFunctions()[idx].getReq());
							}							
						
						}						
						break;
					}
					candidates.remove(result);
				}
				if (failed) {
					acceptDemands--;	
					//System.out.println ("request refused");
				} 
							
			}
			out.write("UC = " + UC);
			System.out.println("UC = " + UC);
			System.out.println();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}
	
	
	public static void mainPTH(String inputFolder, String outputFolder, String option)
	{
		//Cover
				BufferedWriter out1 = null;
				//currentTime=Double.parseDouble(args[0]);
				//maxNode = Double.parseDouble(args[0]);
				//String folderName = args[0];
				File dir = new File(inputFolder);
				String[] extensions = new String[] { "txt" };
				try {
					if (!dir.exists()) {
						System.out.println("Folder: " + dir.getCanonicalPath() + "doest not exist");
					}
					System.out.println("Getting all .txt in " + dir.getCanonicalPath()
							+ " including those in subdirectories");
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
				File out_dir = new File(outputFolder + "/details");
				if (!out_dir.exists()) {
					System.out.println ("create output folder ...");
					out_dir.mkdirs();
					//create new directory if does not exist					
				}
				System.out.println(option);
				String chuoi1= outputFolder;
				switch (option) {
					case "PTH":
						chuoi1 += "/output_pth_all.txt";
						break;
					case "PTH_q":
						chuoi1 += "/output_pth_q_all.txt";
						break;
					default:
						System.out.println("4th parameter: " + option + " is incorrect!");
						return;						
				}
				//File _f = new File(chuoi1 );
				String str="";
				try {
					//out1 = new BufferedWriter(new FileWriter(_f,true));
					out1 = new BufferedWriter(new FileWriter(chuoi1, true));
					//double[] alpha_val = {1.246, 1.264, 1.163, 1.636, 0.979, 1.931, 1.828};

					for (File file : files) {
						try {
							System.out.println("file: " + file.getCanonicalPath());
							ReadNewInputFile(file.getPath());
							str=file.getName(); 
							String chuoi2=  outputFolder;
							if (option.equals("PTH")) {
								chuoi2 += "/details/pth_resultDetail_";
							} else {
								chuoi2 += "/details/pth_q_resultDetail_";
							}
							str = chuoi2+str;
							out1.write(str);
							_duration=0;
						
							value_final =0;
							finalRunTime =0;
							UD = 0.0;
							UC = 0.0;
							UQ = 0.0;
							Ub = 0.0;
							//alpha = alpha_val[index];
							alpha = 0.000001;
							final long startTime = System.currentTimeMillis();
							if (option.equals("PTH")) {
								PTH(str);
							} else {
								PTH_q(str);
							}
							
							_duration = System.currentTimeMillis() - startTime;
							out1.write(" "+m + " " +d +" "+n+ " "+value_final+" "+ acceptDemands+ " "+ UD+" "+UC +" "+ UQ +" "+Ub +" "+_duration);
								out1.newLine();
							
							
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				} catch ( IOException e1 ) {
					e1.printStackTrace();
					} 
				try {
					out1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				
		    
	}
	
	public static void mainOptimizeResilient(String inputFolder, String outputFolder, String objStr)
	{
		//Cover
				BufferedWriter out1 = null;
				File dir = new File(inputFolder);
				
				String[] extensions = new String[] { "txt" };
				try {
					if (!dir.exists()) {
						System.out.println("Folder: " + dir.getCanonicalPath() + "doest not exist");
					}
					System.out.println("Getting all .txt in " + dir.getCanonicalPath()
							+ " including those in subdirectories");
				} catch (IOException e) {
					e.printStackTrace();
				}
				List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
				File out_dir = new File(outputFolder + "/details");
				if (!out_dir.exists()) {
					System.out.println ("create output folder ...");
					out_dir.mkdirs();
					//create new directory if does not exist					
				}
				
				String chuoi1= outputFolder + "/output-Optimize-"+objStr+".txt";
				//File _f = new File(chuoi1 );
				String str="";
				try {
					//out1 = new BufferedWriter(new FileWriter(_f,true));
					out1 = new BufferedWriter(new FileWriter(chuoi1, true));
					//double[] alpha_val = {1.246, 1.264, 1.163, 1.636, 0.979, 1.931, 1.828};
					for (File file : files) {
						try {
							System.out.println("file: " + file.getCanonicalPath());
							ReadNewInputFile(file.getPath());
							//ReadInput(file.getPath());
							System.out.println("Gurobi OCtober: ");
							str=file.getName(); 
							String chuoi2= outputFolder +"/details/resultDetail_Optimize_"+objStr+"_";
							str = chuoi2+str;
							//str = str.replace("in",chuoi2 );
							out1.write(str);
							_duration=0;

							value_final =0;
							
							finalRunTime =0;
							UD = 0.0;
							UC = 0.0;
							UQ = 0.0;
							Ub = 0.0;
							alpha = 0.000001;
							final long startTime = System.currentTimeMillis();
							optimize_resilient(str, objStr);
							_duration = System.currentTimeMillis() - startTime;
							//System.out.println(" "+m + " " +d +" "+n+" "+ tau+ " "+value_final+" "+ maxLinkLoad+" "+maxNodeLoad + " "+ s0Size + " "+noCoverFlow+" "+ _gap +" "+_nodeBB+" "+ _acceptNo.intValue()+" "+ _duration);
								out1.write(" "+m + " " +d +" "+n+ " "+value_final+ " " + acceptDemands+" "+ UD+" "+UC +" "+ UQ +" "+Ub +" "+_duration);
								out1.newLine();
							
							
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				} catch ( IOException e1 ) {
					e1.printStackTrace();
					} 
				try {
					out1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				
		    
	}
	
public static void mainInput() {
	//UtilizeFunction.CreateInput("./lib/createTOPO.txt");
	//UtilizeFunction.CreateInput("./lib/realworld.txt","data_130917");
	UtilizeFunction.randomLeafSpineTopo("./lib/LeafSpineTopo.txt");
}


public static double exponentialRandom(double mu)
{
	Random r = new Random();
	ExponentialGenerator ex = new ExponentialGenerator(mu, r);
	System.out.println("Random value: "+ ex.nextValue());
	return ex.nextValue();
	
}
public static double poissonRandom(double lamda)
{
	Random r = new Random();
	PoissonGenerator pois = new PoissonGenerator(lamda, r);
	System.out.println("Random poisson value: "+ pois.nextValue());
	return pois.nextValue();
}

public static double nextTime(double rateParameter)
{
	Random r = new Random();
	return -Math.log(1.0 - r.nextDouble()) / rateParameter;
}


public static void main(String[] args)//opt
{

	
	String inputFolder = args[0];
	String outputFolder = args[1];
	
	String fileMain =  args[2];
	String objStr = args[3]; //PTO; PTO_q; PTO_b; PTH; PTH_q
	
	switch (fileMain) {
	case "Input":
		mainInput();
		break;
	
	case "Optimize":
		mainOptimizeResilient(inputFolder, outputFolder, objStr);
		break;
	case "PTH":
		mainPTH(inputFolder, outputFolder, objStr);
		break;
	default:		
		break;
	}

	
}

}
