import java.util.ArrayList;
//import java.util.Iterator;
//import org.jgrapht.Graph;
//import org.jgrapht.generate.*;
//import org.jgrapht.traverse.*;
//import org.omg.DynamicAny._DynEnumStub;

public class MyGraph {
    private int V;
    private int E;
    
    ArrayList<Integer> K;
    ArrayList<ArrayList<Integer>> w;
    
    //khoi tao random graph
    
    //chu y doc du lieu thi pai doc de tao mang cua K, r l� V+1;
    
    
    public MyGraph(int noSpine, int noLeaf, int noServer, int bigC, int smallC)//leaf-spine topology
    {
    	this.V = noSpine+noLeaf;
    	this.E=0;
    	this.K = new ArrayList<>();
        this.w = new ArrayList<>();
        int noE=0;
        for(int i=0;i<this.V;i++)
        {
        	ArrayList<Integer> temp = new ArrayList<>();
        	for(int j=0;j<this.V;j++)
        	{
        		temp.add(0);
        	}
        	w.add(temp);
        }
    	for (int h=0;h<noSpine;h++)//  spine  = 40G
   		{
   			int _c= bigC;
         	K.add(_c);
   		}
   		for (int h=noSpine;h<noSpine+noLeaf;h++)//  leaf = 10G
   		{
   			int _c= smallC;
         	K.add(_c);
   		}
   		
//   		for (int h=noSpine+noLeaf;h<this.V;h++)//  server =0G
//   		{
//   			int _c= 0;
//         	K.add(_c);
//   		}
   		
   		//spine to leaf
   		for (int i =0;i<noSpine;i++)
   		{
   			for (int j=noSpine;j<noLeaf+noSpine;j++)
   			{
   				this.w.get(i).set(j, bigC);
   				this.w.get(j).set(i, bigC);
 				noE++;	
   			}
   		}
   		
   		//server to leaf = neu gan voi server nhat thi gia tri nho.
  /*
   		for (int i =noSpine;i<noSpine+noLeaf;i++)
   		{
   			for (int j=noSpine+noLeaf;j<this.V;j++)
   			{
   				int dist = Math.abs(j-i-noLeaf);
   				if(dist<=10 || dist<=this.V-noLeaf-noSpine)
   				{
   					this.w.get(j).set(i, dist+bigC);
   					this.w.get(i).set(j, dist+bigC);
   					noE++;
   				}
   			}
   		}
   */		
   		this.E=noE;
        
    }
    
    public MyGraph(int NoVertex,double p,Integer[] capFea, Integer[] bandFea, int k)
    {


//      DirectedGraph<Object, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//      ScaleFreeGraphGenerator<Object, DefaultEdge> generator = new ScaleFreeGraphGenerator<>(NoVertex);
//      generator.generateGraph(graph, vertexFactory, null);


       
  	//UndirectedGraph<Object, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
  	//RandomGraphGenerator<Object, DefaultEdge> generator= new RandomGraphGenerator<>(NoVertex, NoEdge);
  	//generator.generateGraph(graph, vertexFactory, null);
   
      //set cap and bandwidth cho do thi g    
          this.V = (k*k*k+5*k*k)/4;
          this.E = 0;
          this.K = new ArrayList<>();
          this.w = new ArrayList<>();
          for(int i=0;i<NoVertex;i++)
          {
          	ArrayList<Integer> temp = new ArrayList<>();
          	for(int j=0;j<NoVertex;j++)
          	{
          		temp.add(0);
          	}
          	w.add(temp);
          }
         int noE = 0;
         
         for (int h=0;h<k/2;h++)// core vs aggregation
   		{
   			for (int i=h*k/2;i<(h+1)*k/2;i++)
       		{
       			for (int j=0;j<k;j++)
       			{
       				int i_temp= k*k/4 +j*k/2+h*(k/2-1);
       				
       				this.w.get(i).set(i_temp, 1000);
       				this.w.get(i_temp).set(i, 1000);
     				noE++;	
       			}
       		}
   		}
   		
   		for (int h=0;h<k;h++)// aggregation vs edge
   		{
   			for (int i=0;i<k/2;i++)
       		{
   				for (int j=0;j<k/2;j++)
   				{
   					int i1= h*k/2+ k*k/4+i*(k/2-1);
       				int i2= h*k/2 + 3*k*k/4 + j*(k/2-1);
       				this.w.get(i1).set(i2, 500);
       				this.w.get(i2).set(i1, 500);
     				noE++;	
   				}
   				
       		}
   		}
   		for (int h=0;h<k;h++)//  edge vs server
   		{
   			for (int j=0;j<k/2;j++)
   			{
   				for (int i=0;i<k/2;i++)
           		{
       				int i1= h*k/2+ 3*k*k/4+j*(k/2-1);
       				int i2= h*k + 5*k*k/4 + i*(k/2-1)+j*k/2;
       				this.w.get(i1).set(i2, 500);
       				this.w.get(i2).set(i1, 500);
     				noE++;	
           		}
   			}
   			
   		}
   		
   		for (int h=0;h<k;h++)//  server (user)
   		{
   			int _c= 300;
         	K.add(_c);
   		}
   		for (int h=k;h<3*k;h++)//  server (user)
   		{
   			int _c= 100;
         	K.add(_c);
   		}
   		
   		for (int h=3*k;h<5*k;h++)//  server (user)
   		{
   			int _c= 50;
         	K.add(_c);
   		}
   		for (int h=5*k;h<NoVertex;h++)//  server (user)
   		{
   			int _c= 1;
         	K.add(_c);
   		}
   		
	   	E = noE;
  
	
    	
    }
    
   /* public MyGraph(int NoVertex,double p,Integer[] capFea, Integer[] bandFea)
    {
//        DirectedGraph<Object, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//        ScaleFreeGraphGenerator<Object, DefaultEdge> generator = new ScaleFreeGraphGenerator<>(NoVertex);
//        generator.generateGraph(graph, vertexFactory, null);
 

         
    	//UndirectedGraph<Object, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    	//RandomGraphGenerator<Object, DefaultEdge> generator= new RandomGraphGenerator<>(NoVertex, NoEdge);
    	//generator.generateGraph(graph, vertexFactory, null);
       
    	final int seed = 33;
        final double edgeProbability = p;
        final int numberVertices = NoVertex;

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gg =
                new GnpRandomGraphGenerator<Integer, DefaultWeightedEdge>(
                   numberVertices, edgeProbability, seed, false);
//        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gg =
//            new GnpRandomGraphGenerator<Integer, DefaultWeightedEdge>(
//                numberVertices, edgeProbability, seed, false);
//        
//        WeightedPseudograph<Integer, DefaultWeightedEdge> graph =
//                new WeightedPseudograph<>(DefaultWeightedEdge.class);
        
        DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph =
                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
        VertexFactory<Integer> vertexFactoryInteger= new VertexFactory<Integer>() {
        	 private int i=0;

 	        @Override
 	        public Integer createVertex()
 	        {
 	            return ++i;
 	        }
		};
		
		gg.generateGraph(graph);
            //gg.generateGraph(graph, vertexFactoryInteger, null);
    	
        //set cap and bandwidth cho do thi g
            
            this.V = NoVertex;
            this.E = 0;
            this.K = new ArrayList<>();
            this.w = new ArrayList<>();
            for(int i=0;i<NoVertex;i++)
            {
            	ArrayList<Integer> temp = new ArrayList<>();
            	for(int j=0;j<NoVertex;j++)
            	{
            		temp.add(0);
            	}
            	w.add(temp);
            }
           int noE = 0;
           for (DefaultWeightedEdge edges : graph.edgeSet()) {
           	
           	int s = Integer.parseInt(graph.getEdgeSource(edges).toString());
           	int t = Integer.parseInt(graph.getEdgeTarget(edges).toString());
   			//System.out.println("Dinh: "+ s+ "..." + t+ "..."+w);
   			//double w= UtilizeFunction.randomDouble(new Integer[] {5000,6000,7000,8000,9000,10000});
   			if(s!=t)
   			{
   				int b= UtilizeFunction.randomInt(bandFea);
   				this.w.get(s-1).set(t-1, b);
   				noE++;
   			}
   			else
   				System.out.println("Loop");
   		}
           for (int i=0;i<NoVertex+1;i++)
           {
        	   int _c= UtilizeFunction.randomInt(capFea);
         	  K.add(_c);
           }    
	   	E = noE;
    }*/
    
  public MyGraph(ArrayList<Integer> _K, ArrayList<ArrayList<Integer>> _w) {//edit model su dung doc du lieu vao
    	
    	//r , K, w phai co kich thuoc (n+1) -> tinh ca id 0
   	
        this.V = _K.size();
        this.K = new ArrayList<>();
        this.w = new ArrayList<>();
        
        for(int i=0;i<_K.size();i++)
        {
        	ArrayList<Integer> temp = new ArrayList<>();
        	for(int j=0;j<_K.size();j++)
        		temp.add(_w.get(i).get(j));
        	this.w.add(temp);
        }
        for(int i = 0;i<_K.size();i++)
        	this.K.add(_K.get(i));
   }

    // number of vertices and edges
    public int V() { return V; }
    public int E() { return E; }
    public int getCap(int v)//new model
    {
    	return K.get(v-1);
    }
    public boolean setCap(int v, int c)//new model
    {
    	K.set(v-1, c);
    	return true;
    }
   
    public boolean setEdgeWeight(int v, int u,int c)
    {
    	this.w.get(v-1).set(u-1, c);
    	return true;
    }
    public int getEdgeWeight(int u, int v)
    {
    		return w.get(u-1).get(v-1);
    }
    // test client
    public static void main(String[] args) {
        //int V = Integer.parseInt(args[0]);
        //int E = Integer.parseInt(args[1]);
        //Graph G = new Graph(V, E);
        //StdOut.println(G);
    }

}