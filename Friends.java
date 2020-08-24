package friends;

import java.util.ArrayList;
import java.util.HashMap;

import structures.Queue;
import structures.Stack;

public class Friends {

	/**
	 * Finds the shortest chain of people from p1 to p2.
	 * Chain is returned as a sequence of names starting with p1,
	 * and ending with p2. Each pair (n1,n2) of consecutive names in
	 * the returned chain is an edge in the graph.
	 * 
	 * @param g Graph for which shortest chain is to be found.
	 * @param p1 Person with whom the chain originates
	 * @param p2 Person at whom the chain terminates
	 * @return The shortest chain from p1 to p2. Null if there is no
	 *         path from p1 to p2
	 */
	public static ArrayList<String> shortestChain(Graph g, String p1, String p2) {
		
		Person origin = new Person();
		for (Person p : g.members) {
			if (p.name.equals(p1))
				origin = p;
		}
		if (origin.equals(null))
			return null;
		
		Queue<Person> q = new Queue<Person>();
		q.enqueue(origin);
		
		int shortestLength = bfs(g, q, new Queue<Person>(), 1, p2, new ArrayList<Integer>());
		if (shortestLength == -1)
			return null;
		
		ArrayList<String> shortestPath = new ArrayList<String>();
		shortestPath.add(p1);
		shortestLength--;
		while (shortestLength > 0) {
			Friend f = origin.first;
			q = new Queue<Person>();
			q.enqueue(g.members[f.fnum]);
			while (f != null && bfs(g, q, new Queue<Person>(), 1, p2, new ArrayList<Integer>()) > shortestLength) {
				f = f.next;
				q = new Queue<Person>();
				if (f != null)
					q.enqueue(g.members[f.fnum]);
			}
			if (f == null)
				break;
			shortestPath.add(g.members[f.fnum].name);
			shortestLength--;
			origin = g.members[f.fnum];
		}
		shortestPath.add(p2);
		return shortestPath;
		
	}
	
	private static int bfs(Graph g, Queue<Person> upper, Queue<Person> lower, int level, String dest, ArrayList<Integer> visited) {

		if (upper.size() == 0) {
			if (lower.size() == 0)
				return -1;
			upper = lower;
			lower = new Queue<Person>();
			level++;
		}

		Person p = upper.dequeue();
		Friend f = p.first;
		
		while (f != null) {
			if (g.members[f.fnum].name.equals(dest))
				return ++level;
			if (!visited.contains(f.fnum)) {
				lower.enqueue(g.members[f.fnum]);
				visited.add(f.fnum);
			}
			f = f.next;
		}
		
		return bfs(g, upper, lower, level, dest, visited);
	}
	
	/**
	 * Finds all cliques of students in a given school.
	 * 
	 * Returns an array list of array lists - each constituent array list contains
	 * the names of all students in a clique.
	 * 
	 * @param g Graph for which cliques are to be found.
	 * @param school Name of school
	 * @return Array list of clique array lists. Null if there is no student in the
	 *         given school
	 */
	public static ArrayList<ArrayList<String>> cliques(Graph g, String school) {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<Integer> visited = new ArrayList<Integer>();
		for (int i = 0; i < g.members.length; i++) {
			Person p = g.members[i];
			if (!visited.contains(i) && p.student && p.school.equals(school)) {
				Queue<Person> upper = new Queue<Person>();
				upper.enqueue(p);
				visited.add(i);
				result.add(cliqueBfs(g, upper, new Queue<Person>(), new ArrayList<String>(), school, visited));
			}
		}
		
		return result;
		
	}
	
	private static ArrayList<String> cliqueBfs(Graph g, Queue<Person> upper, Queue<Person> lower, ArrayList<String> clique, String school, ArrayList<Integer> visited) {
		
		if (upper.size() == 0) {
			if (lower.size() == 0)
				return clique;
			upper = lower;
			lower = new Queue<Person>();
		}
		
		Person p = upper.dequeue();
		if (p.student && p.school.equals(school) && !clique.contains(p.name)) {
			clique.add(p.name);
		}
		Friend f = p.first;
		
		while (f != null) {
			if (!visited.contains(f.fnum) && g.members[f.fnum].student && g.members[f.fnum].school.equals(school)) {
				clique.add(g.members[f.fnum].name);
				lower.enqueue(g.members[f.fnum]);
				visited.add(f.fnum);
			}
			f = f.next;
		}
		
		return cliqueBfs(g, upper, lower, clique, school, visited);
	}
	
	/**
	 * Finds and returns all connectors in the graph.
	 * 
	 * @param g Graph for which connectors needs to be found.
	 * @return Names of all connectors. Null if there are no connectors.
	 */
	public static ArrayList<String> connectors(Graph g) {
		
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Integer> visited = new ArrayList<Integer>();
		int[] dfsNum = new int[g.members.length];
		int[] back = new int[g.members.length];
		
		for (int i = 0; i < g.members.length; i++) {
			if (!visited.contains(i)) {
				
				ArrayList<String> temp = dfs(g, 1, i, dfsNum, back, new HashMap<Integer, Integer>(), visited, new ArrayList<String>(), g.members[i]);
				for (String each : temp)
					result.add(each);
			}
		}
		
		return result;
		
	}
	
	private static ArrayList<String> dfs(Graph g, int level, int index, int[] dfsNum, int[] backNum, HashMap<Integer, Integer> back, ArrayList<Integer> visited, ArrayList<String> connectors, Person origin) {	

		dfsNum[index] = level;
		backNum[index] = level;
		level++;
		visited.add(index);
		
		Friend f = g.members[index].first;
		while (f != null) {
			if (!visited.contains(f.fnum)) {
				back.put(f.fnum, index);
				dfs(g, level, f.fnum, dfsNum, backNum, back, visited, connectors, origin);
				backNum[index] = Math.min(backNum[index], backNum[f.fnum]);
				if (backNum[f.fnum] >= dfsNum[index]) {
					int friends = 0;
					if (g.members[index].equals(origin)) {
						Friend temp = origin.first;
						while (temp != null) {
							friends++;
							temp = temp.next;
						}
					}
					if (!connectors.contains(g.members[index].name) && ((g.members[index].equals(origin) && friends > 1) || !g.members[index].equals(origin))) {
						connectors.add(g.members[index].name);
					}
				}
			}
			else if (f.fnum != back.get(index)) {
				backNum[index] = Math.min(backNum[index], dfsNum[f.fnum]);
			}
			f = f.next;
		}
		
		return connectors;
	}
	
}

