package ca.utoronto.utm.floatingpoint;

public class q2 {
	public static void main(String[] args) {
		q2 p = new q2();
		System.out.println(p.solve711());
	}
	public String solve711() {
		for (double a = 1; a <= 711/4 ; a++){
			for (double b = a ; b <= 711/4 ; b++){
				for (double c = b; c <= 711/4; c++){
					double sum = a+b+c;
					double d = 711 - sum;
					if (d<c){
						break;
					}else if (a * b * c *d  == 711000000){
						a = a/100;
						b = b/100;
						c = c/100;
						d = d/100;
						return "a" + a + "b" + b + "c" +c + "d" +d ;
					}
				}
			}
		}
		return null;

	}
}
