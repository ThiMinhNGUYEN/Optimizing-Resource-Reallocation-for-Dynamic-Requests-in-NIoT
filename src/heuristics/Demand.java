

import java.util.Properties;
import java.io.BufferedWriter;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;


public class Demand {
	//source, destination,bandwidth,arrival time,set of function (number of function)
    private int idS;//id of demand
    private int noF;//number of Functions
    private int bwS;//bandwidth
    private int minBw;
    private int source;
    private int destination;
    private double arrivalTime;
    private double processTime;
    private double rate;//rate requirement for each demand
    private Function[] arrF;//set of functions
    static BufferedWriter out;
    public static Properties props;
    
    // empty graph with V vertices
    public Demand(int idS) {
        if (idS <= 0) throw new RuntimeException("Number of vertices must be nonnegative");
        this.idS = idS;
    }
    public Demand(int idS,int bwS) {
        if (idS <= 0) throw new RuntimeException("Number of vertices must be nonnegative");
        this.idS = idS;
        this.bwS = bwS;
    }
  

public Demand(int idS,int Source, int Destination,int bwS, double ArrivalTime,double _rate, Function[] f) {
    //edit	
	// random bandwidth for service
        this(idS);
        
        	//truong hop khong random
        	this.noF = f.length;
        	this.source=Source;
        	this.rate = _rate;
        	this.destination = Destination;
        	this.arrivalTime = ArrivalTime;
        	this.bwS= bwS;
        	this.arrF = new Function[this.noF];
        	for (int i=0;i<this.noF;i++)
        	{
        		this.arrF[i]= f[i];// tạo một mảng các function cho từng service        	
        	}
        	
        
    }

 
    
    
public static boolean checkConnect (int src,int dest, MyGraph _g,double maxBw)
{
	SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
    
	for (int j=0;j<_g.V();j++)
    {
    	g_i.addVertex("node"+(j+1));
    }      
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
		return true;
	} else {
		System.out.println("khong tim duoc duong di giua " + src + " va " + dest);
		return false;

	}
    
   /* List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
	if(_p!=null && _p.size()>0)
	{
		return true;
	}
	else
	{
		return false;
	}*/
	}
//demand cho input
public Demand(int idS, int src, int dest,double ArrivalTime, Function[] f,int noVirtualNode, MyGraph _g,Integer[] intArray)
{
	//truong hop cho topology Fat da biet src and destination;
	

    //edit	
	// random bandwidth for service
        this(idS);
        	this.noF = UtilizeFunction.randInt(2, f.length);
        	this.source = src;
        	this.destination = dest;
        	
            this.rate = UtilizeFunction.randDouble(5);
            this.arrivalTime = ArrivalTime;//gan thoi gian den cua demand
        	//Integer[] intArray = new Integer[] { 50,80,100,150,200 };
        	this.bwS = UtilizeFunction.randomInt(intArray);
        	
        	this.arrF = new Function[this.noF];
        	int[] temp = new int[noF];
        	for (int i=0;i<this.noF;i++)
        	{
        		temp[i]=-1;
        	}
        	boolean flag=false;
        	for (int i=0;i<this.noF;i++)
        	{
        		flag=false;
        		while (!flag)
        		{
        			int idF= UtilizeFunction.randInt(0, f.length-1);
        			if(i>0)
        			{
        				for (int k=0;k<i;k++)
        					if(idF == temp[k])
        					{
        						flag =true;
        						break;
        					}
        			}
        			if(!flag)
        			temp[i]=idF;
        			flag = !flag;        		
        		}
        	
        	}
        	for (int i=0;i<this.noF;i++)
        	{
        		this.arrF[i]= f[temp[i]];// tạo một mảng các function cho từng service
        	
        	}
        
        
    
	}
public Demand(int idS, double ArrivalTime, Function[] f,int noVirtualNode, MyGraph _g,Integer[] intArray) {
    //edit	
	// random bandwidth for service
        this(idS);
        //this.noF = UtilizeFunction.randInt(2, f.length);
        //this.noF = f.length;
        	this.noF =4;
        	while (true)
        	{
        		this.source = UtilizeFunction.randInt(1, noVirtualNode);
            	this.destination = UtilizeFunction.randInt(1, noVirtualNode);
            	if ((this.source != this.destination) && checkConnect(this.source, this.destination, _g,0.000001))
            	{
            		break;
            	}
        	}
        	
        	
            this.rate = UtilizeFunction.randDouble(5);
            this.arrivalTime = ArrivalTime;//gan thoi gian den cua demand
        	//Integer[] intArray = new Integer[] { 50,80,100,150,200 };
        	//this.bwS = UtilizeFunction.randomInt(intArray);
            this.bwS = UtilizeFunction.randInt(intArray[0],intArray[1]);
        	
        	this.arrF = new Function[this.noF];
        	int[] temp = new int[noF];
        	for (int i=0;i<this.noF;i++)
        	{
        		temp[i]=-1;
        	}
        	boolean flag=false;
        	for (int i=0;i<this.noF;i++)
        	{
        		flag=false;
        		while (!flag)
        		{
        			int idF= UtilizeFunction.randInt(0, f.length-1);
        			if(i>0)
        			{
        				for (int k=0;k<i;k++)
        					if(idF == temp[k])
        					{
        						flag =true;
        						break;
        					}
        			}
        			if(!flag)
        			temp[i]=idF;
        			flag = !flag;        		
        		}
        	
        	}
        	for (int i=0;i<this.noF;i++)
        	{
        		this.arrF[i]= f[temp[i]];// tạo một mảng các function cho từng service
        	
        	}
        
        
    }


public Demand(int idS, double ArrivalTime, Function[] f,int _startNode, int _endNode) {
    //edit	
	// random bandwidth for service
        this(idS);
        //this.noF = UtilizeFunction.randInt(3, f.length);
        this.noF = 4;
        	//this.noF =5;
        	while (true)
        	{
        		this.source = UtilizeFunction.randInt(_startNode+1, _endNode);
            	this.destination = UtilizeFunction.randInt(_startNode+1, _endNode);
            	if ((this.source != this.destination) )
            	{
            		break;
            	}
        	}
        	
        	
            this.rate = UtilizeFunction.randDouble(2);
            this.arrivalTime = ArrivalTime;//gan thoi gian den cua demand
        	//Integer[] intArray = new Integer[] { 50,80,100,150,200 };
        	//this.bwS = UtilizeFunction.randomInt(intArray);
            this.bwS = 1;
        	
        	this.arrF = new Function[this.noF];
        	int[] temp = new int[noF];
        	for (int i=0;i<this.noF;i++)
        	{
        		temp[i]=-1;
        	}
        	boolean flag=false;
        	for (int i=0;i<this.noF;i++)
        	{
        		flag=false;
        		while (!flag)
        		{
        			int idF= UtilizeFunction.randInt(0, f.length-1);
        			if(i>0)
        			{
        				for (int k=0;k<i;k++)
        					if(idF == temp[k])
        					{
        						flag =true;
        						break;
        					}
        			}
        			if(!flag)
        			temp[i]=idF;
        			flag = !flag;        		
        		}
        	
        	}
        	for (int i=0;i<this.noF;i++)
        	{
        		this.arrF[i]= f[temp[i]];// tạo một mảng các function cho từng service
        	
        	}
        
        
    }

	public void set_bwS(int _bwS)
	{
		this.bwS = _bwS;
	}
    // id of Service
	
    public int idS() { return idS; }
    // return Source
    public int sourceS() { return source; }
 // return Destination
    public int destinationS() { return destination; }
 // return arrival Time
    public double arrivalTimeS() { return arrivalTime; }
    public double processingTimeS() { return processTime; }
    public void setArrivalTime(double arrivaltime)
    {
    	arrivalTime = arrivaltime;
    }
    public void setProcessingTime(double processingtime)
    {
    	processTime = processingtime;
    }
    // number of functions in service
    public int noF() { return noF; }
    // return bandwidth of service;
    public int getMinbw() { return this.minBw; }
    public void setMinbw(int minBw) {this.minBw=minBw; }
    public int bwS() { return this.bwS; }
    //return array of Function in service;
    public Function[] getFunctions() {return this.arrF;}
    public double getRate(){return rate;}
    /**If id cua function tu 1->m*/
    public int getOrderFunction(int id)    
    {
    	int temp =0;
    	if (id ==0)
    		return 0;
    	for (int x= 0; x<this.arrF.length; x++)
    	{
    		if (arrF[x].id()==id)
    		{
    			temp=x+1;
    			break;
    			
    		}
    	}
    	return temp;
    }

    // string representation of Graph - takes quadratic time
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(idS + ": "+source+": "+destination+": " + bwS +": "+noF+ " : ");
        for (int v = 0; v < noF; v++) {
            s.append(arrF[v].id() +"    ");
        }
        return s.toString();
    }
    // test client
    public static void main(String[] args) {}

}