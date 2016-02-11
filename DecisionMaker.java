import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class DecisionMaker {
	
	public static int val = 1;

	public static void main(String[] args) throws IOException 
	{
		int L = Integer.parseInt(args[0]);
		int K = Integer.parseInt(args[1]);
		String trainingSetPath = args[2];
		String validationSetPath = args[3];
		String testingSetPath = args[4];
		String toPrint = args[5];
			
		String line = "";
		
		FileInputStream fin = new FileInputStream(trainingSetPath);
		BufferedReader input = new BufferedReader(new InputStreamReader(fin));
		
		ArrayList<String[]> trainData = new ArrayList<String[]>();
		ArrayList<String> usedAttributes = new ArrayList<String>();
	
		String[] attributes = input.readLine().split(",");
		
		while((line = input.readLine()) != null)
		{
			trainData.add(line.split(","));
		}

		double posEg = 0, negEg = 0;
		
		for(String[] str : trainData)
		{
			if(Integer.parseInt(str[str.length - 1]) == 1)
				posEg++;
			else
				negEg++;
		}
		
		double total = posEg + negEg;
		
		double entropy = -((posEg/total) * findLog(posEg/total)) - ((negEg/total) * findLog(negEg/total));

		TreeNode builtRoot = new TreeNode();
		builtRoot = GrowTrees(trainData, usedAttributes, entropy, attributes, 0, 0);
		
		if(toPrint.equalsIgnoreCase("yes"))
			printTree(builtRoot, 0);
			
		System.out.println("Accuracy of information gain heuristics: " + findAccuracy(builtRoot, testingSetPath) * 100);

		TreeNode postPrunedRoot = PerformPostPruning(trainData, builtRoot, attributes, K, L, validationSetPath);
				
		double k = total;
		double k0 = negEg;
		double k1 = posEg;
		
		double variance = (k0 * k1)/(k * k);
	
		TreeNode varianceRoot = new TreeNode();	
		varianceRoot = VarianceImpurityTree(trainData, usedAttributes, variance, attributes, 0, 0);
		
		System.out.println("Accuracy of variance heuristic is : " + findAccuracy(varianceRoot, testingSetPath) * 100);
		
		if(toPrint.equalsIgnoreCase("yes"))
			printTree(varianceRoot, 0);
		
		TreeNode postPrunedVariance = PerformPostPruning(trainData, varianceRoot, attributes, K, L, validationSetPath);
	}
	
	public static TreeNode VarianceImpurityTree(ArrayList<String[]> trainData, ArrayList<String> usedAttributes, double variance, String[] attributes, int maxAttr, int val) 
	{
		double posEg = 0, negEg = 0;
		
		ArrayList<String> newattributes = new ArrayList<String>(usedAttributes);
		
		if(newattributes.size() == attributes.length - 1 || trainData.isEmpty())
		{
			TreeNode leaf = new TreeNode();
			
			leaf.setValue(val);
			return leaf;
		}

		if(variance == 0)
		{
			TreeNode leaf = new TreeNode();
			leaf.setValue(val);
			leaf.setValue(Integer.parseInt((trainData.get(0)[trainData.get(0).length - 1])));
		}

		
		for(String[] str : trainData)
		{
			int end = str.length - 1;
			
			if(Integer.parseInt(str[end]) == 1)
				posEg++;
			
			if(Integer.parseInt(str[end]) == 0)
				negEg++;
		}
		if(posEg == (posEg + negEg))
		{
			TreeNode leaf = new TreeNode();
			leaf.setAttributeName(attributes[maxAttr]);
			leaf.setTrainData(trainData);
			leaf.setValue(1);
			leaf.setLChild(false);
			return leaf;
		}
		if(negEg == (negEg + posEg))
		{
			TreeNode leaf = new TreeNode();
			leaf.setAttributeName(attributes[maxAttr]);
			leaf.setTrainData(trainData);
			leaf.setValue(0);
			leaf.setLChild(true);
			return leaf;				
		}
		
		else
		{
			Double max = Double.valueOf(Double.NEGATIVE_INFINITY);
			int i = 0, maxIndex = 0;
			double maxgain1 = 0, maxgain0 = 0;
			
			while(i != attributes.length - 1)
			{		
				double class0Neg = 0, class0Pos = 0, class1Neg = 0, class1Pos = 0, posEgs = 0, negEgs = 0, variance0, variance1, Gain = 0;

				if(!newattributes.contains(attributes[i]))
				{
					for(String[] rec : trainData)
					{
						int end = rec.length - 1;
						
						if(Integer.parseInt(rec[i]) == 1)
							posEgs++;
						
						else
							negEgs++;
						
						if(Integer.parseInt(rec[end]) == 1 && Integer.parseInt(rec[i]) == 1)
							class1Pos++;
						
						else if(Integer.parseInt(rec[end]) == 1 && Integer.parseInt(rec[i]) == 0)
							class1Neg++;
						
						else if(Integer.parseInt(rec[end]) == 0 && Integer.parseInt(rec[i]) == 0)
							class0Neg++;
						
						else if(Integer.parseInt(rec[end]) == 0 && Integer.parseInt(rec[i]) == 1)							
							class0Pos++;
					}
					double sum = class0Neg + class0Pos + class1Neg + class1Pos;

					variance0 = -(((class1Neg + class0Neg)/sum) * ((class1Neg * class0Neg)/((class0Neg + class1Neg) * (class0Neg + class1Neg))));
											
					variance1 = -(((class1Pos + class0Pos)/sum) * ((class1Pos * class0Pos)/((class1Pos + class0Pos) * (class1Pos + class0Pos))));
						
					Gain = variance + variance1 + variance0;
					
					if(max < Gain)
					{
						max = Gain;
						maxIndex = i;
						
						maxgain1 = variance1;
						maxgain0 = variance0;
					}
				}
				i++;			
			}
			
			ArrayList<String[]> leftChild = new ArrayList<String[]>();
			ArrayList<String[]> rightChild = new ArrayList<String[]>();
			
			newattributes.add(attributes[maxIndex]);
			
			for(String[] addRecs : trainData)
			{
				if(Integer.parseInt(addRecs[maxIndex]) == 0)
					leftChild.add(addRecs);

				else
					rightChild.add(addRecs);
			}
			
			TreeNode node = new TreeNode();
			node.setAttributeName(attributes[maxIndex]);
			node.setTrainData(trainData);

			node.setLeftChild(VarianceImpurityTree(leftChild, newattributes, maxgain0, attributes, maxIndex, 0));
			node.setRightChild(VarianceImpurityTree(rightChild, newattributes, maxgain1, attributes, maxIndex, 1));

			return node;
		}
	}
	
	public static TreeNode copytree(TreeNode finalroot, TreeNode Dtemp) 
	{
		if (finalroot.getLeftChild() == null && finalroot.getRightChild() == null) 
		{
			Dtemp = new TreeNode(finalroot.getTrainData(), finalroot.getAttributeName(), finalroot.getValue(), null, null);

		} 
		else 
		{
			Dtemp = new TreeNode(finalroot.getTrainData(), finalroot.getAttributeName(), finalroot.getValue(), finalroot.getLeftChild(), finalroot.getRightChild());
			
			Dtemp.setLeftChild(copytree(finalroot.getLeftChild(), Dtemp.getLeftChild()));
			Dtemp.setRightChild(copytree(finalroot.getRightChild(), Dtemp.getRightChild()));
		}

		return Dtemp;
	}
	
	public static ArrayList<TreeNode> findNodeToreplacedNode(ArrayList<TreeNode> nodeList, TreeNode finalroot) {

		if (finalroot != null && finalroot.getLeftChild() == null && finalroot.getRightChild() == null) {
			return nodeList;

		} else if (finalroot != null) {
			nodeList.add(finalroot);
			findNodeToreplacedNode(nodeList, finalroot.getLeftChild());
			findNodeToreplacedNode(nodeList, finalroot.getRightChild());
		}
		return nodeList;
	}



	public static TreeNode PerformPostPruning(ArrayList<String[]> trainData, TreeNode D, String[] attributes, int K, int L, String path) throws IOException
	{
		TreeNode Dbest = D;
		int countnodes = 0;
		double posEg = 0, negEg = 0;

		double accuracy = findAccuracy(D, path);
		double accuracyBest = 0;
		
		for (int i = 1; i < L; i++) 
		{
			TreeNode Dtemp = null;
			Dtemp = copytree(D, Dtemp);
			
			int M = (1 + (int) (Math.random() * K));

			for (int j = 1; j < M; j++) {
				int P = 0;
				
				ArrayList<TreeNode> nodeList = new ArrayList<>();
				nodeList = findNodeToreplacedNode(nodeList, Dtemp);
				countnodes = nodeList.size() - 2;

				P = (1 + (int) (Math.random() * countnodes));
			
				if (P != 0 && nodeList.size() >= 2) 
				{
					TreeNode replacedNode = nodeList.get(P);
					
					replacedNode.setLeftChild(null);
					replacedNode.setRightChild(null);

					for(String[] s : replacedNode.getTrainData())
					{
						if(Integer.parseInt(s[s.length -1]) == 0)
							negEg++;
						
						else
							posEg++;
					}
					
					if(posEg > negEg)
						replacedNode.setValue(1);
					else
						replacedNode.setValue(0);			
				}

			}

			accuracyBest = findAccuracy(Dtemp, path);
			if (accuracyBest > accuracy) {
				accuracy = accuracyBest;
				copytree(Dbest, Dtemp);
				// Dtemp = null;
			}
		}

		System.out.println("Accuracy after pruning is : " + accuracy * 100);
		return Dbest;
		
		
	}
			
	public static int findNonLeafNodes(TreeNode node)
	{
		if(node == null)
			return 0;
		
		if(node.getLeftChild() == null && node.getRightChild() == null)
			return 1;
		
		else
			return findNonLeafNodes(node.getLeftChild()) + findNonLeafNodes(node.getRightChild());
	}
	
	public static TreeNode findNodeToreplacedNode(int p, TreeNode node)
	{
		if(node != null)
		{
			if(node.getValue() == p)
				return node;
			
			else 
			{
				TreeNode foundNode = findNodeToreplacedNode(p, node.getLeftChild());
				if(foundNode == null)
					foundNode = findNodeToreplacedNode(p, node.getRightChild());
			
				return foundNode;
			}			
		}
		else
			return null;
		
	}
	
	public static int traverseToGetValue(String[] str, TreeNode node, String[] attributesList)
	{
		int val = 0;
		
		if(node == null)
			return 0;
		 
		if(node.getLeftChild() == null && node.getRightChild() == null)
			return node.getValue();
		
		else
		{
			String attr = node.getAttributeName();
		
			for(int i = 0; i< attributesList.length; i++)
			{
				if(attributesList[i].equalsIgnoreCase(attr))
				{
					val = Integer.parseInt(str[i]);
					break;
				}
			}
			
			if(val == 0)
				return traverseToGetValue(str, node.getLeftChild(), attributesList);
			
			else
				return traverseToGetValue(str, node.getRightChild(), attributesList);
			
		}
	}
	
	public static double findAccuracy(TreeNode root, String path) throws IOException
	{
		String line = "";
		ArrayList<String[]> validationData = new ArrayList<String[]>();
		double posClass = 0, negClass = 0;
	
		FileInputStream fin = new FileInputStream(path);
		BufferedReader input = new BufferedReader(new InputStreamReader(fin));	
		
		String[] attributesList = input.readLine().split(",");
		
		while((line = input.readLine()) != null)
		{
			validationData.add(line.split(","));
		}
		
		for(int i = 0; i< validationData.size(); i++)
		{
			String[] str = validationData.get(i);
			
			int classifiedValue = traverseToGetValue(str, root, attributesList);
			
			if(classifiedValue == Integer.parseInt(str[str.length - 1]))
				posClass++;
			else
				negClass++;
		}

		double accuracy = posClass/(posClass + negClass);

		return accuracy;
	}
	
	public static TreeNode GrowTrees(ArrayList<String[]> trainData, ArrayList<String> usedAttributes, double entropy, String[] attributes, int maxAttr, int val) 
	{
		double posEg = 0, negEg = 0;
		ArrayList<String> newattributes = new ArrayList<String>(usedAttributes);
		
		if(newattributes.size() == attributes.length - 1 || trainData.isEmpty())
		{
			TreeNode leaf = new TreeNode();
			
			leaf.setValue(val);
			return leaf;
		}

		if(entropy == 0)
		{
			TreeNode leaf = new TreeNode();
			leaf.setValue(val);
			leaf.setValue(Integer.parseInt((trainData.get(0)[trainData.get(0).length - 1])));
		}
		
		for(String[] str : trainData)
		{
			int end = str.length - 1;
			
			if(Integer.parseInt(str[end]) == 1)
				posEg++;
			
			if(Integer.parseInt(str[end]) == 0)
				negEg++;
		}
		if(posEg == (posEg + negEg))
		{
			TreeNode leaf = new TreeNode();
			leaf.setAttributeName(attributes[maxAttr]);
			leaf.setTrainData(trainData);
			leaf.setValue(1);
			leaf.setLChild(false);
			return leaf;
		}
		if(negEg == (negEg + posEg))
		{
			TreeNode leaf = new TreeNode();
			leaf.setAttributeName(attributes[maxAttr]);
			leaf.setTrainData(trainData);
			leaf.setValue(0);
			leaf.setLChild(true);
			return leaf;				
		}
		
		if(posEg == 0 && negEg == 0)
		{
			return null;
		}
		
		else
		{
			double entropy1 = 0, entropy0 = 0, Gain = 0, maxentropy1 = 0, maxentropy0 = 0;
			Double max = Double.valueOf(Double.NEGATIVE_INFINITY);
			String maxGain = "";
			int i = 0, maxIndex = 0;

			while(i != attributes.length - 1)
			{		
				double class0Neg = 0, class0Pos = 0, class1Neg = 0, class1Pos = 0, posEgs = 0, negEgs = 0;

				if(!newattributes.contains(attributes[i]))
				{
					for(String[] rec : trainData)
					{
						int end = rec.length - 1;
						
						if(Integer.parseInt(rec[i]) == 1)
							posEgs++;
						
						else
							negEgs++;
						
						if(Integer.parseInt(rec[end]) == 1 && Integer.parseInt(rec[i]) == 1)
							class1Pos++;
						
						else if(Integer.parseInt(rec[end]) == 1 && Integer.parseInt(rec[i]) == 0)
							class0Pos++;
						
						else if(Integer.parseInt(rec[end]) == 0 && Integer.parseInt(rec[i]) == 0)
							class0Neg++;
						
						else if(Integer.parseInt(rec[end]) == 0 && Integer.parseInt(rec[i]) == 1)							
							class1Neg++;
					}
					
					if(class1Pos == 0 || class1Neg == 0)
						entropy1 = 0;				
					else
						entropy1 = - ((class1Pos/(class1Pos + class1Neg)) * findLog((class1Pos/(class1Pos + class1Neg)))) - ((class1Neg/(class1Pos + class1Neg)) * findLog((class1Neg/(class1Neg + class1Pos))));                

					
					if(class0Pos == 0 || class0Neg == 0)
						entropy0 = 0;
					else
						entropy0 = -((class0Neg/(class0Neg + class0Pos)) * findLog((class0Neg/(class0Neg + class0Pos)))) - ((class0Pos/(class0Pos + class0Neg)) * findLog((class0Pos/(class0Pos + class0Neg))));

					Gain = entropy - (((posEgs/(posEgs + negEgs)) * entropy1)) - (((negEgs/(posEgs + negEgs)) * entropy0));

					if(max < Gain)
					{
						max = Gain;
						maxGain = attributes[i];
						maxIndex = i;
						
						maxentropy1 = entropy1;
						maxentropy0 = entropy0;
					}

				}
				i++;
			}
			
			ArrayList<String[]> leftChild = new ArrayList<String[]>();
			ArrayList<String[]> rightChild = new ArrayList<String[]>();
			
			newattributes.add(attributes[maxIndex]);
			
			for(String[] addRecs : trainData)
			{
				if(Integer.parseInt(addRecs[maxIndex]) == 0)
					leftChild.add(addRecs);

				else
					rightChild.add(addRecs);
			}
			
			TreeNode node = new TreeNode();
			node.setAttributeName(attributes[maxIndex]);
			node.setTrainData(trainData);
			
			
			node.setLeftChild(GrowTrees(leftChild, newattributes, maxentropy0, attributes, maxIndex, 0));
			node.setRightChild(GrowTrees(rightChild, newattributes, maxentropy1, attributes, maxIndex, 1));
			
			return node;
		}
	}


	public static void printTree(TreeNode root, int ht)
	{
		if(root == null)
			return;

		if(root.getLeftChild()!=null)
		{
			for(int i = 0; i< ht; i++)
				System.out.print("  |  ");
			
			String temp = "";
			
			if(root.getLeftChild().getLeftChild() == null)
				temp = " : " + root.getLeftChild().getValue();
			
			System.out.println(root.getAttributeName() + " = 0" + temp);
		}
			
		printTree(root.getLeftChild(), ht + 1);

		if(root.getRightChild()!=null)
		{
			for(int i = 0; i< ht; i++)
				System.out.print("  |  ");
				
			String temp = "";
				
			if(root.getRightChild().getRightChild() == null)
				temp = " : " + root.getRightChild().getValue();
				
			System.out.println(root.getAttributeName() + " = 1" + temp);

			}
		printTree(root.getRightChild(), ht + 1);
	}
	
	public static double findLog(double arg)
	{
		double temp = (Math.log(arg) / Math.log(2));
		
		return temp;
	}
}
