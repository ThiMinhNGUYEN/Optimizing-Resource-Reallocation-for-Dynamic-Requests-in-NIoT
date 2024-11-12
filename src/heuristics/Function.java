public class Function {
	private int id;
    private double delay;
    private int req;
    private int type;
    //private myVector lamda= new myVector(3);
    
    // empty graph with V vertices
    public Function(int id,int min,int max) {//khoi tao random function
        if ( id <=0 ) throw new RuntimeException("Number of vertices must be nonnegative");
        this.id = id; 
        this.delay =  24 * Math.random()+1;//random requirement for function
        this.req = UtilizeFunction.randInt(min,max);

        
    }
    public Function(int _id, double _delay,int _req) {//gan id va bw cho 1 function
        id=_id;
        delay = _delay;
        req = _req;
    }
    public Function(int _id, double _delay,int _req, int _type) {//gan id va bw cho 1 function
        id=_id;
        delay = _delay;
        req = _req;
        type = _type;
    }

    // number of vertices and edges
    public int id() { return id; }
    public double getDelay() {
    	return delay;}
    public int getReq()
    {
    	return req;
    }
    public int getType() {
    	return type;
    }



    // string representation of Graph - takes quadratic time
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(id + ": " + req);
        return s.toString();
    }


    // test client
    public static void main(String[] args) {
        //int id = Integer.parseInt(args[0]);
    	//int id = UtilizeFunction.randInt(1, 9);
        //Function f = new Function(id);
        //Graph G = new Graph(V, E);
        //System.out.print(f.toString());
        //StdOut.println(G);
    }

}