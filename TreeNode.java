import java.util.ArrayList;


public class TreeNode {
	ArrayList<String[]> trainData;
	String attributeName;
	int value;
	boolean isLChild;
	TreeNode leftChild, rightChild;
	
	public boolean getisLChild() {
		return isLChild;
	}

	public void setLChild(boolean isLChild) {
		this.isLChild = isLChild;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	TreeNode()
	{
		
	}
	
	TreeNode(ArrayList<String[]> Data, String attrName, int val, TreeNode leftCh, TreeNode rightCh)
	{
		this.trainData = Data;
		this.attributeName = attrName;
		this.setLeftChild(leftCh);
		this.setRightChild(rightCh);
	}

	TreeNode(TreeNode node)
	{
		
	}
		
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public ArrayList<String[]> getTrainData() {
		return trainData;
	}
	public void setTrainData(ArrayList<String[]> trainData) {
		this.trainData = trainData;
	}
	public TreeNode getLeftChild() {
		return leftChild;
	}
	public void setLeftChild(TreeNode leftChild) {
		this.leftChild = leftChild;
	}
	public TreeNode getRightChild() {
		return rightChild;
	}
	public void setRightChild(TreeNode rightChild) {
		this.rightChild = rightChild;
	}
}
