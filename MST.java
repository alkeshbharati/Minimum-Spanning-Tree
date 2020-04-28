package ASB180015;

import ASB180015.Graph;
import ASB180015.Graph.Vertex;
import ASB180015.Graph.Edge;
import ASB180015.Graph.GraphAlgorithm;
import ASB180015.Graph.Factory;
import ASB180015.Graph.Timer;

import ASB180015.BinaryHeap.Index;
import ASB180015.BinaryHeap.IndexedHeap;

import java.util.*;
import java.io.FileNotFoundException;
import java.io.File;

public class MST extends GraphAlgorithm<MST.MSTVertex> {
	String algorithm;
	public long wmst;
	List<Edge> mst;

	MST(Graph g) {
		super(g, new MSTVertex((Vertex) null));
	}

	public static class MSTVertex implements Index, Comparable<MSTVertex>, Factory {

		private boolean seen;
		private Vertex parent;
		private int d;
		private Vertex vertex;
		private int name;
		private int index;
		private int comp;

		MSTVertex(Vertex u) {
			this.seen = false;
			this.parent = null;
			this.d = Integer.MAX_VALUE;
			this.vertex = u;
			this.name = 0;
			this.comp =0;
		}

		MSTVertex(MSTVertex u) {  // for prim2
			this(u.vertex);
		}

		public MSTVertex make(Vertex u) {
			return new MSTVertex(u);
		}

		/**
		 * set the index of the MSTVertex
		 * @param index
		 */
		public void putIndex(int index) {
			this.index = index;
		}

		/**
		 * @return index of MSTVertex
		 */
		public int getIndex() {
			return this.index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * Compare the edge
		 * @param  other
		 */
		public int compareTo(MSTVertex other) {
			if(this.d > other.d || other == null )
				return 1;
			else if(this.d < other.d)
				return -1;
			return 0;
		}
	}

	public long boruvka() {
		algorithm = "Boruvka";
		Graph f = new Graph(g.size());
		int count = countandLabel(f);
		while( count >1 ){
			AddSafeEdges(count,f);
			count = countandLabel(f);
		}
		wmst = 0;
		for(Edge e: f.getEdgeArray()) {
			wmst = wmst + e.getWeight();
		}
		return wmst;
	}

	/**
	 * To add edge in the forest to compute the minimum weight of graph
	 * @param count no of component
	 * @param f forest
	 * @return total weight of minimum spanning tree
	 */
	private void AddSafeEdges(int count, Graph f) {
		Edge[] safe = new Edge[count];
		for(int i = 0; i < count-1; i++) {
			safe[i] = null;
		}
		for(Edge edge: g.getEdgeArray()) {
			MSTVertex from = this.get(edge.fromVertex());
			MSTVertex to = this.get(edge.toVertex());
			if(from.comp != to.comp) {
				if(safe[from.comp - 1] == null || edge.compareTo(safe[from.comp-1]) < 0)
					safe[from.comp - 1 ]  = edge;
				if(safe[to.comp - 1] == null || edge.compareTo(safe[to.comp-1]) < 0)
					safe[to.comp - 1 ] = edge;
			}
		}

		Set<Edge> uniqueEdge = new HashSet<>();
		for (Edge e : safe) {
			if (e != null) {
				uniqueEdge.add(e);
			}
		}
		for (Edge e : uniqueEdge) {
			f.addEdge(e.fromVertex().getIndex(), e.toVertex().getIndex(), e.getWeight());
		}

	}

	/**
	 * To compute the no of component
	 * @param f forest
	 * @return total weight of minimum spanning tree
	 */
	public int countandLabel(Graph f)
	{
		int count = 0;
		for (Vertex u :f)
		{
			get(u).seen = false;
		}
		for (Vertex u :f)
		{
			if(!get(u).seen)
			{
				count++;
				label(u,count,f);
			}
		}
		return count;
	}

	/**
	 * To assign component/label to vertex
	 * @param u vertex
	 * @param c count
	 * @param f forest
	 * @return total weight of minimum spanning tree
	 */
	public void label(Vertex u, int c,Graph f)
	{
		Queue<MSTVertex> bag = new LinkedList();
		bag.add(get(u));
		while(!bag.isEmpty())
		{
			MSTVertex v= bag.remove();
			if(!get(v.vertex).seen)
			{
				get(v.vertex).seen = true;
				v.comp= c;
				for(Edge e: f.incident(v.vertex)){
					Vertex w = e.otherEnd(v.vertex);
					bag.add(get(w));
				}
			}
		}
	}


	/**
	 * To find minimum spanning tree using Second algorithm of prim from source vertex
	 * @param s source vertex
	 * @return total weight of minimum spanning tree
	 */
	public long prim2(Vertex s) {
		algorithm = "indexed heaps";
		mst = new LinkedList<>();

		for(Vertex u: g){
			get(u).seen = false;
			get(u).parent =null;
			get(u).d = Integer.MAX_VALUE;
		}

		get(s).d = 0;
		wmst = 0;
		IndexedHeap<MSTVertex> q = new IndexedHeap<>(g.size());

		for(Vertex u: g)
		{
			q.add(get(u));
		}

		HashMap<Integer, Edge> edgeMap = new HashMap<>();

		while(!q.isEmpty()){
			MSTVertex u = q.remove();
			Vertex u1 = u.vertex;
			u.seen = true;
			wmst = wmst + u.d;

			if( u.parent != null ){
				mst.add(edgeMap.get(u.name));
			}

			for( Edge e: g.incident(u1) ){
				Vertex v = e.otherEnd(u1);
				if( !get(v).seen && e.getWeight() < get(v).d ){
					get(v).d =e.getWeight();
					get(v).parent =u1;
					edgeMap.put(e.name, e);
					get(v).name =e.name;
					q.decreaseKey(get(v));
				}
			}
		}
		return wmst;
	}



	public static MST mst(Graph g, Vertex s, int choice) {
		MST m = new MST(g);
		switch(choice) {
			case 0:
				m.boruvka();
				break;
			case 1:
				m.prim2(s);
				break;
			default:
				break;
		}
		return m;
	}

	public static void main(String[] args) throws FileNotFoundException {
		Scanner in;
		int choice = 1;
		if (args.length == 0 || args[0].equals("-")) {
		in = new Scanner(System.in);
		} else {
			File inputFile = new File(args[0]);
		in = new Scanner(inputFile);
		}

		if (args.length > 1) { choice = Integer.parseInt(args[1]); }

		Graph g = Graph.readGraph(in);
		Vertex s = g.getVertex(1);

		Timer timer = new Timer();
		MST m = mst(g, s, choice);
		System.out.println(m.algorithm + "\n" + m.wmst);
		System.out.println(timer.end());
	}
}