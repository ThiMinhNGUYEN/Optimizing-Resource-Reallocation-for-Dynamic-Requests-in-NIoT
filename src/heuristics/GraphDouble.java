import java.util.ArrayList;

public class GraphDouble {
    private int V;
    private int E;
    
    ArrayList<Double> K;
    ArrayList<ArrayList<Double>> w;
    
    //khoi tao random graph
    
    //chu y doc du lieu thi pai doc de tao mang cua K, r l� V+1;
    
   
    
  public GraphDouble(ArrayList<Double> _K, ArrayList<ArrayList<Double>> _w) {//edit model su dung doc du lieu vao
    	
    	//r , K, w phai co kich thuoc (n+1) -> tinh ca id 0
   	
        this.V = _K.size();
        this.K = new ArrayList<>();
        this.w = new ArrayList<>();
        for(int i = 0;i<_K.size();i++)
        	this.K.add(_K.get(i));
        for(int i=0;i<_K.size();i++)
        {
        	ArrayList<Double> temp = new ArrayList<>();
        	for(int j=0;j<_K.size();j++)
        		temp.add(_w.get(i).get(j));
        	this.w.add(temp);
        }
       
   }

    // number of vertices and edges
    public int V() { return V; }
    public int E() { return E; }
    public double getCap(int v)//new model
    {
    	return K.get(v-1);
    }
    public boolean setCap(int v, double c)//new model
    {
    	K.set(v-1, c);
    	return true;
    }
   
    public boolean setEdgeWeight(int v, int u,double c)
    {
    	this.w.get(v-1).set(u-1, c);
    	return true;
    }
    public double getEdgeWeight(int u, int v)
    {
    		return w.get(u-1).get(v-1);
    }
    // test client
    public static void main(String[] args) {
       // int V = Integer.parseInt(args[0]);
        //int E = Integer.parseInt(args[1]);
        //Graph G = new Graph(V, E);
        //StdOut.println(G);
    }

}