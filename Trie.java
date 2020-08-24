package trie;

import java.util.ArrayList;

/**
 * This class implements a Trie. 
 * 
 * @author Sesh Venugopal
 *
 */
public class Trie {
	
	// prevent instantiation
	private Trie() { }
	
	/**
	 * Builds a trie by inserting all words in the input array, one at a time,
	 * in sequence FROM FIRST TO LAST. (The sequence is IMPORTANT!)
	 * The words in the input array are all lower case.
	 * 
	 * @param allWords Input array of words (lowercase) to be inserted.
	 * @return Root of trie with all words inserted from the input array
	 */
	public static TrieNode buildTrie(String[] allWords) {
		if (allWords.length == 0)
			return null;
		
		TrieNode root = new TrieNode(null, null, null);
		
		for (int i = 0; i < allWords.length; i++) {
			TrieNode prev = root, temp = root.firstChild;
			String word = allWords[i], subs = "", task = "";
			int j = 0;
			while (j < word.length()) {
				if (temp == null && prev == root) {
					Indexes n = new Indexes(i, (short) 0, (short) (word.length() - 1));
					temp = new TrieNode(n, null, null);
					prev.firstChild = temp;			
					break;
				}
				boolean sib = false;
				while (j < word.length()) {
					subs += word.charAt(j++);
					while (!getStr(temp, allWords).contains(subs)) {
						if (temp == null && !sib) {
							task = "sibling";
							break;
						}
						else if (temp == null && sib) {
							task = "split";
							break;
						}
						if (!sib) prev = temp;
						temp = temp.sibling;
						if (getStr(temp, allWords).contains(subs)) {
							prev = temp;
							task = "split";
							break;
						}
					}
					while (temp != null && getStr(temp, allWords).contains(subs)) {
						subs += word.charAt(j++);
						if (!getStr(temp, allWords).contains(subs)) {
							sib = true;
							prev = temp;
							temp = temp.firstChild;
							break;
						}
						if (j < word.length() - 1) {
							prev = temp;
							task = "split";
							break;
						}
					}
					if (task != "") break;
				}

				if (task == "sibling") {
					Indexes n = new Indexes(i, (short) 0, (short) (word.length() - 1));
					temp = new TrieNode(n, null, null);
					prev.sibling = temp;			
					break;
				}
				
				if (task == "split") {
					String common = "";
					for (int k = 0; k < word.length() && k < getStr(prev, allWords).length(); k++) {
						if (word.charAt(k) == getStr(prev, allWords).charAt(k)) {
							common += word.charAt(k);
						}
					}
					int match = common.length();
					String commonhighest = common, comtemp = "";
					TrieNode safe = prev;
					while (prev.firstChild != null) {
						temp = prev.firstChild;
						while (temp != null) {
							comtemp = "";
							for (int k = 0; k < word.length() && k < allWords[temp.substr.wordIndex].substring(0, temp.substr.endIndex + 1).length(); k++) {
								if (word.charAt(k) == allWords[temp.substr.wordIndex].substring(0, temp.substr.endIndex + 1).charAt(k)) {
									comtemp += word.charAt(k);
								}
							}
							if (comtemp.length() > commonhighest.length()) {
								commonhighest = comtemp;
								prev = temp;
								break;
							}
							prev = temp;
							temp = temp.sibling;
						}
					}
					if (commonhighest.length() <= match) {
						prev = safe;
						comtemp = "";
					}
					else {
						common = commonhighest;
					}
					
					if (commonhighest == common && word.contains(getStr(prev, allWords))) {
						prev = prev.firstChild;
						temp = prev.sibling;
						while (temp != null) {
							prev = temp;
							temp = temp.sibling;
						}
						Indexes newSib = new Indexes(i, (short) common.length(), (short) (word.length() - 1));
						temp = new TrieNode(newSib, null, null);
						prev.sibling = temp;
						break;
					}
					
					Indexes prevNew = new Indexes(prev.substr.wordIndex, (short) prev.substr.startIndex, (short) (common.length() - 1));
					
					Indexes tempChi = new Indexes(prev.substr.wordIndex ,(short) common.length(), (short) prev.substr.endIndex);
					TrieNode tempChild = new TrieNode(tempChi, null, null);
					TrieNode children = prev.firstChild;
					prev.firstChild = tempChild;
					prev.substr = prevNew;
					tempChild.firstChild = children;
					
					while (children != null) {
						children.substr.startIndex = (short) (common.length());
						children = children.sibling;
					}
					
					Indexes newN = new Indexes(i, (short) (prev.substr.endIndex + 1), (short) (word.length() - 1));
					TrieNode newNode = new TrieNode(newN, null, null);
					tempChild.sibling = newNode;					
					break;
				}
				j++;
			}
		}
		
		return root;
	}
	
	static String getStr(TrieNode node, String[] allWords) {
		if (node == null) return "";
		if (node.substr.startIndex == node.substr.endIndex && node.substr.endIndex < allWords[node.substr.wordIndex].length())
			return allWords[node.substr.wordIndex].substring(node.substr.startIndex, node.substr.startIndex + 1);
		else
			return allWords[node.substr.wordIndex].substring(node.substr.startIndex, node.substr.endIndex + 1);
	}
	
	/**
	 * Given a trie, returns the "completion list" for a prefix, i.e. all the leaf nodes in the 
	 * trie whose words start with this prefix. 
	 * For instance, if the trie had the words "bear", "bull", "stock", and "bell",
	 * the completion list for prefix "b" would be the leaf nodes that hold "bear", "bull", and "bell"; 
	 * for prefix "be", the completion would be the leaf nodes that hold "bear" and "bell", 
	 * and for prefix "bell", completion would be the leaf node that holds "bell". 
	 * (The last example shows that an input prefix can be an entire word.) 
	 * The order of returned leaf nodes DOES NOT MATTER. So, for prefix "be",
	 * the returned list of leaf nodes can be either hold [bear,bell] or [bell,bear].
	 *
	 * @param root Root of Trie that stores all words to search on for completion lists
	 * @param allWords Array of words that have been inserted into the trie
	 * @param prefix Prefix to be completed with words in trie
	 * @return List of all leaf nodes in trie that hold words that start with the prefix, 
	 * 			order of leaf nodes does not matter.
	 *         If there is no word in the tree that has this prefix, null is returned.
	 */
	public static ArrayList<TrieNode> completionList(TrieNode root,
										String[] allWords, String prefix) {
		if (root == null || prefix == null || prefix.length() < 1)
			return null;
		
		ArrayList<TrieNode> nodes = new ArrayList<TrieNode>();
		String comtemp = "", commonhighest = "";
		TrieNode prev = root, temp = root.firstChild, above = prev;
		while (prev.firstChild != null) {
			temp = prev.firstChild;
			while (prev == root && (!prefix.contains(getStr(temp, allWords)) && !getStr(temp, allWords).contains(prefix))) {
				temp = temp.sibling;
				if (temp == null) {
					System.out.println("HI");
					return null;
				}
			}
			while (temp != null) {
				comtemp = "";
				for (int k = 0; k < prefix.length() && k < allWords[temp.substr.wordIndex].substring(0, temp.substr.endIndex + 1).length(); k++) {
					if (prefix.charAt(k) == allWords[temp.substr.wordIndex].substring(0, temp.substr.endIndex + 1).charAt(k)) {
						comtemp += prefix.charAt(k);
					}
				}
				if (comtemp.length() > commonhighest.length()) {
					commonhighest = comtemp;
					above = temp;
					prev = temp;
					break;
				}
				prev = temp;
				temp = temp.sibling;
			}
		}
		
		if (above.firstChild == null) {
			if (!allWords[above.substr.wordIndex].contains(prefix)) return null;
			nodes.add(above);
			return nodes;
		}
		nodes = leafs(above.firstChild, nodes);
		if (!allWords[nodes.get(0).substr.wordIndex].contains(prefix)) return null;
		return nodes;
	}
	
	static ArrayList<TrieNode> leafs(TrieNode node, ArrayList<TrieNode> nodes) {
		if (node == null) return null;
		if (node.firstChild != null) leafs(node.firstChild, nodes);
		if (node.sibling != null) leafs(node.sibling, nodes);
		if (node.firstChild == null) nodes.add(node);
		return nodes;
	}
	
	public static void print(TrieNode root, String[] allWords) {
		System.out.println("\nTRIE\n");
		print(root, 1, allWords);
	}
	
	private static void print(TrieNode root, int indent, String[] words) {
		if (root == null) {
			return;
		}
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		
		if (root.substr != null) {
			String pre = words[root.substr.wordIndex]
							.substring(0, root.substr.endIndex+1);
			System.out.println("      " + pre);
		}
		
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		System.out.print(" ---");
		if (root.substr == null) {
			System.out.println("root");
		} else {
			System.out.println(root.substr);
		}
		
		for (TrieNode ptr=root.firstChild; ptr != null; ptr=ptr.sibling) {
			for (int i=0; i < indent-1; i++) {
				System.out.print("    ");
			}
			System.out.println("     |");
			print(ptr, indent+1, words);
		}
	}
 }
