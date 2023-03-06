package nafcp;

public class Result {
	public String Product;
	public String Sup;
	
	public Result(String product, String sup) {
		super();
		Product = product;
		Sup = sup;
	}

	public String getProduct() {
		return Product;
	}

	public void setProduct(String product) {
		Product = product;
	}

	public String getSup() {
		return Sup;
	}

	public void setSup(String sup) {
		Sup = sup;
	}

	@Override
	public String toString() {
		return "Result [Product=" + Product + ", Sup=" + Sup + "]";
	}
	
	

}
