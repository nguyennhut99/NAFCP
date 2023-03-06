package nafcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NAFCP {

	private int pre;
	private int post;
	private Map<Integer, Integer> hashI1;
	private Map<Integer, String> productMap;
	private List<FCI> fcis_1;
	private int minSupport;
	private Map<Integer, List<Integer>> hashFCIs;
	private List<FCI> fcis;
	private BufferedWriter writer = null;
	private int numOfTrans;
	private List<Result> results = new ArrayList<>();

	public NAFCP() {
		// TODO Auto-generated constructor stub
	}

	ListTransaction readFile(String filename) throws IOException {
		ListTransaction listTransaction = new ListTransaction();

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		int i = 0;
		while (((line = reader.readLine()) != null)) {
			if (line.isEmpty() == true || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
				continue;
			}

			Transaction transaction = new Transaction();
			transaction.transactionId = ++i;

			String[] lineSplited = line.split(" ");

			for (String itemString : lineSplited) {
				Item item = new Item();
				item.name = Integer.parseInt(itemString);
				transaction.items.add(item);
			}
			listTransaction.transactions.add(transaction);
		}

		reader.close();

		return listTransaction;
	}

	ListTransaction readDatabase() throws IOException {
		ListTransaction listTransaction = new ListTransaction();

		Connection con = ConnectSQLServer.getConnection();
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"select SalesOrderID, ProductID from [Sales].[SalesOrderDetail]\r\n" + "order by SalesOrderID");
			// show data
			int preId = -1;
			Transaction transaction = new Transaction();
			while (rs.next()) {
				if (preId != rs.getInt(1)) {
					if (transaction.items.size() > 1) {
						listTransaction.transactions.add(transaction);
					}
					transaction = new Transaction();
					transaction.transactionId = rs.getInt(1);
					preId = rs.getInt(1);
				}
				Item item = new Item();
				item.name = rs.getInt(2);
				transaction.items.add(item);			
			}
			Statement stmt2;
			stmt2 = con.createStatement();
			ResultSet rsProduct = stmt2.executeQuery(
					"select [ProductID], [Name]\r\n"
					+ "from [Production].[Product]");
			// show data
			productMap = new HashMap<Integer, String>();
			while (rsProduct.next()) {
				productMap.put(rsProduct.getInt(1), rsProduct.getString(2));
			}			
			// close connection
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listTransaction;
	}

	void insertTree(Transaction transaction, PPCTree_Node root) {
		while (transaction.items.size() > 0) {
			Item item = transaction.items.get(0);
			transaction.items.remove(0);

			boolean flag = false;

			PPCTree_Node tree_Node = new PPCTree_Node();

			for (int j = 0; j < root.childNodes.size(); j++) {
				// item in tree
				if (root.childNodes.get(j).item.name == item.name) {
					root.childNodes.get(j).item.frequency++;
					tree_Node = root.childNodes.get(j);
					flag = true;
					break;
				}
			}
			// item not in tree
			if (flag == false) {
				tree_Node.item = item;
				tree_Node.item.frequency = 1;
				root.childNodes.add(tree_Node);
			}
			insertTree(transaction, tree_Node);
		}
	}

	void generateOrder(PPCTree_Node root) {
		root.preOrder = pre++;
		for (int i = 0; i < root.childNodes.size(); i++)
			generateOrder(root.childNodes.get(i));
		root.postOrder = post++;
	}

	void generateNodeList(PPCTree_Node root) {
		if (root.item.name != -1) {

			int stt = hashI1.get(root.item.name);
			Node node = new Node();
			node.postOrder = root.postOrder;
			node.preOrder = root.preOrder;
			node.frequency = root.item.frequency;

			fcis_1.get(stt).nodes.add(node);
		}

		for (int i = 0; i < root.childNodes.size(); i++)
			generateNodeList(root.childNodes.get(i));
	}

	boolean N_list_check(List<Node> a, List<Node> b) {
		int i = 0;
		int j = 0;
		while (j < b.size() && i < a.size()) {
			Node aI = a.get(i);
			Node bJ = b.get(j);
			if (aI.preOrder < bJ.preOrder && aI.postOrder > bJ.postOrder)
				j++;
			else
				i++;
		}
		if (j == b.size())
			return true;
		return false;
	}

	List<Integer> itemUnion(List<Integer> a, List<Integer> b) {
		List<Integer> result = new ArrayList<>();

		int i = 0;
		int j = 0;
		while (i < a.size() && j < b.size()) {
			int aI = a.get(i);
			int bJ = b.get(j);
			if (aI > bJ) {
				result.add(aI);
				i++;
			} else if (aI == bJ) {
				result.add(aI);
				i++;
				j++;
			} else {
				result.add(bJ);
				j++;
			}
		}

		while (i < a.size()) {
			result.add(a.get(i));
			i++;
		}
		while (j < b.size()) {
			result.add(b.get(j));
			j++;
		}
		return result;
	}

	List<Node> ncCombination(List<Node> a, List<Node> b, int totalFrequency, IntegerByRef g) {
		List<Node> result = new ArrayList<>();

		int i = 0;
		int j = 0;
		int subFrequency = totalFrequency;

		while (i < a.size() && j < b.size()) {
			Node aI = a.get(i);
			Node bJ = b.get(j);
			if (aI.preOrder < bJ.preOrder) {
				if (aI.postOrder > bJ.postOrder) {
					if (result.size() > 0 && result.get(result.size() - 1).preOrder == aI.preOrder)
						result.get(result.size() - 1).frequency += bJ.frequency;
					else {
						Node temp = new Node();
						temp.postOrder = aI.postOrder;
						temp.preOrder = aI.preOrder;
						temp.frequency = bJ.frequency;
						result.add(temp);
					}
					g.value = g.value + bJ.frequency;
					j++;
				} else {
					subFrequency -= aI.frequency;
					i++;
				}
			} else {
				subFrequency -= bJ.frequency;
				j++;
			}
			if (subFrequency < minSupport)
				return null;
		}

		return result;
	}

	boolean subsetCheck(List<Integer> a, List<Integer> b) {
		if (a.size() > b.size())
			return false;

		int i = 0;
		int j = 0;
		while (i < a.size() && j < b.size()) {
			int aI = a.get(i);
			int bJ = b.get(j);
			if (aI > bJ)
				return false;
			else if (aI == bJ) {
				i++;
				j++;
			} else
				j++;
		}
		if (i < a.size())
			return false;
		else
			return true;
	}

	boolean subsumptionCheck(FCI f) {
		List<Integer> arr = (List<Integer>) hashFCIs.get(f.frequency);
		if (arr != null) {

			for (int i = 0; i < arr.size(); i++) {
				if (subsetCheck(f.items, fcis.get(arr.get(i)).items) == true)
					return true;
			}
		}
		return false;
	}

	void findFCIs(List<FCI> FCIs, int level) throws IOException {
		for (int i = FCIs.size() - 1; i >= 0; i--) {
			FCI FciI = FCIs.get(i);
			List<FCI> FCIs_Next = new ArrayList<>();
			for (int j = i - 1; j >= 0; j--) {

				FCI FciJ = FCIs.get(j);
				if (N_list_check(FciJ.nodes, FciI.nodes) == true) {
					if (FciI.frequency == FciJ.frequency) {
						FciI.items = itemUnion(FciI.items, FciJ.items);
						FCIs.remove(j);
						i--;
					} else {
						FciI.items = itemUnion(FciI.items, FciJ.items);
						for (int k = 0; k < FCIs_Next.size(); k++)
							FCIs_Next.get(k).items = itemUnion(FCIs_Next.get(k).items, FciJ.items);
					}

					continue;
				}

				FCI f = new FCI();
				f.items = itemUnion(FciI.items, FciJ.items);
				IntegerByRef g = new IntegerByRef(0);
				f.nodes = ncCombination(FciJ.nodes, FciI.nodes, FciJ.frequency + FciI.frequency, g);

				if (g.value >= minSupport) {

					f.frequency = g.value;
					FCIs_Next.add(0, f);
				}
			} // j

			if (subsumptionCheck(FciI) == false) {
				fcis.add(FciI);
				writer.write(FciI.toString());
				List<Integer> items = FciI.getItems();
				StringBuilder sb = new StringBuilder();
				for (Integer item : items) {
					sb.append(productMap.get(item));
					sb.append(" / ");
				}
				Result result = new Result(sb.toString(), ""+FciI.getFrequency());
				results.add(result);
				writer.write("\n");

				if (hashFCIs.get(FciI.frequency) == null) {
					List<Integer> ar = new ArrayList<>();
					ar.add(fcis.size() - 1);
					hashFCIs.put(FciI.frequency, ar);
				} else {
					List<Integer> ar = (List<Integer>) hashFCIs.get(FciI.frequency);
					ar.add(fcis.size() - 1);
					hashFCIs.put(FciI.frequency, ar);
				}
			}

			findFCIs(FCIs_Next, level + 1);

		}
	}

	public List<Result> runAlgorithm( double minSupport, String output) throws IOException {

		// create object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));

		System.currentTimeMillis();

		// Initialize some variables
		fcis_1 = new ArrayList<>();
		fcis = new ArrayList<>();
		hashI1 = new HashMap<>();
		hashFCIs = new HashMap<>();
		pre = 0;
		post = 0;

		// ==========================
		//ListTransaction pDB = readFile(filename);
		ListTransaction pDB = readDatabase();
		numOfTrans = pDB.transactions.size();
		System.out.println(numOfTrans);
		// caculate the minSupport
		this.minSupport = (int) Math.ceil(numOfTrans * minSupport);

		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		for (int i = 0; i < pDB.transactions.size(); i++) {
			Transaction pi = pDB.transactions.get(i);
			for (int j = pi.items.size() - 1; j >= 0; j--) {
				Integer item = pi.items.get(j).name;
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
		}

		int i = 0;
		for (Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			if (entry.getValue() >= this.minSupport) {
				FCI f = new FCI();
				f.items.add(entry.getKey());
				f.frequency = entry.getValue();
				fcis_1.add(f);
				i++;
			}
		}

		// Sort 1-fcis list on Frequency
		Collections.sort(fcis_1, FCI.fc);

		// add the order 1-FCIs to Hashtable
		for (i = 0; i < fcis_1.size(); i++)
			hashI1.put(fcis_1.get(i).items.get(0), i);

		PPCTree_Node root = new PPCTree_Node();
		root.item.name = -1;

		// scan database (2): delete infrequent items
		for (i = 0; i < pDB.transactions.size(); i++) {
			Transaction pDBi = pDB.transactions.get(i);
			for (int l = pDBi.items.size() - 1; l >= 0; l--) {
				Item il = pDBi.items.get(l);
				if (hashI1.get(il.name) == null)
					pDBi.items.remove(l);
				else
					il.frequency = fcis_1.get(hashI1.get(il.name)).frequency;
			}
			// sort the items of product i on frequency
			pDBi.Sort();
			insertTree(pDBi, root);
		}
		pDB = null;

		// scan tree (1): generate preOrder and postOrder
		generateOrder(root);

		// scan tree (2) to generate N-list of 1-FCIs
		generateNodeList(root);
		
		findFCIs(fcis_1, 1);		

		fcis.size();
		// ==========================
		writer.close();

		return results;
	}

}

class IntegerByRef {
	int value;

	IntegerByRef(int _value) {
		value = _value;
	}
}

class Item {
	int name;
	int frequency;

	static Comparator<Item> itemComparator = new Comparator<Item>() {
		public int compare(Item x, Item y) {
			{
				if (x.frequency > y.frequency)
					return -1;
				else if (x.frequency < y.frequency)
					return 1;
				else
					return x.name - y.name;
			}
		}
	};
}

class Transaction {
	int transactionId;
	List<Item> items;

	void Sort() {
		Collections.sort(items, Item.itemComparator);
	}

	Transaction() {
		transactionId = 0;
		items = new ArrayList<>();
	}

}

class ListTransaction {
	List<Transaction> transactions;

	ListTransaction() {
		transactions = new ArrayList<>();
	}
}

class PPCTree_Node {
	Item item;
	List<PPCTree_Node> childNodes;
	int preOrder;
	int postOrder;

	public PPCTree_Node() {
		item = new Item();
		childNodes = new ArrayList<>();
		preOrder = 0;
		postOrder = 0;
	}
}

class Node {
	int postOrder;
	int preOrder;
	int frequency;
}

class FCI {
	List<Integer> items;
	int frequency;
	List<Node> nodes;

	public FCI() {
		items = new ArrayList<>();
		nodes = new ArrayList<>();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Integer item : items) {
			sb.append(item);
			sb.append(' ');
		}
		sb.append("#SUP: ");
		sb.append(this.frequency);
		return sb.toString();
	}
	

	public List<Integer> getItems() {
		return items;
	}

	public void setItems(List<Integer> items) {
		this.items = items;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}


	static Comparator<FCI> fc = new Comparator<FCI>() {
		@Override
		public int compare(FCI x, FCI y) {
			{
				if (x.frequency > y.frequency)
					return -1;
				else if (x.frequency < y.frequency)
					return 1;
				else
					return x.items.get(0).compareTo(y.items.get(0));
			}
		}
	};
}
