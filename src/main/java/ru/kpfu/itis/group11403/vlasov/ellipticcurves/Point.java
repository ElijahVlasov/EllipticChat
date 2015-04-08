package ru.kpfu.itis.group11403.vlasov.ellipticcurves;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Point implements Serializable {
	
	private transient boolean isInf = false;
	
	private BigInteger x, y;
	private transient BigInteger mod;
	
	private transient EllipticCurve ec;

	public Point() {
		isInf = true;
	}
	
	public Point(BigInteger x, BigInteger y, EllipticCurve ec) {
		
		this.x   = x;
		this.y   = y;
		this.mod = ec.getMod();
		this.ec  = ec;
	
	}

	public BigInteger getX() {
		return x;
	}

	public void setX(BigInteger x) {
		this.x = x;
	}

	public BigInteger getY() {
		return y;
	}

	public void setY(BigInteger y) {
		this.y = y;
	}

	public boolean isInf() {
		return isInf;
	}

	public void setInf(boolean isInf) {
		this.isInf = isInf;
	}
	
	public BigInteger getMod() {
		return mod;
	}

	public void setMod(BigInteger mod) {
		this.mod = mod;
	}

	public EllipticCurve getEllipticCurve() {
		return ec;
	}

	public void setEllipticCurve(EllipticCurve ec) {
		this.ec = ec;
	}

	public boolean equalsMod(Point point, BigInteger mod) {
		
		if(point == null) {
			return false;
		}
		
		if(point.isInf) {
			
			if(isInf) {
				return true;
			} else {
				return false;
			}
			
		} else {
			
			if(isInf) {
				return false;
			}
			
		}
		
		BigInteger x1 = x.mod(mod);
		BigInteger y1 = y.mod(mod);
		BigInteger x2 = point.x.mod(mod);
		BigInteger y2 = point.y.mod(mod);
		
		return x1.equals(x2) && y1.equals(y2);
		
	}

	public static Point addPoints(Point p1, Point p2) {
		
		Point result = new Point();
		
		result.ec  = p1.ec;
		result.mod = p1.mod;
		
		BigInteger dy = p2.y.subtract(p1.y);
		BigInteger dx = p2.x.subtract(p1.x);
		
		if(dy.compareTo(BigInteger.ZERO) < 0) {
			dy.add(p1.mod);
		}
		
		if(dx.compareTo(BigInteger.ZERO) < 0) {
			dx.add(p1.mod);
		}
		
		// (y2-y1)/(x2-x1) (mod p)
		
		BigInteger s = dy.multiply(dx.modInverse(p1.mod)).mod(p1.mod);
		
		if(s.compareTo(BigInteger.ZERO) < 0) {
			s.add(p1.mod);
		}
		
		// s^2 - x1 - x2
		result.x = s.pow(2).subtract(p1.x).subtract(p2.x).mod(p1.mod);
		
		// s*(x1-xR)-y1 
		result.y = s.multiply(p1.x.subtract(result.x)).subtract(p1.y).mod(p1.mod);
		
		if(result.x.compareTo(BigInteger.ZERO) < 0) {
			result.x.add(result.mod);
		}
		
		if(result.y.compareTo(BigInteger.ZERO) < 0) {
			result.y.add(result.mod);
		}
		
		return result;
		
	}
	
	public static Point doublePoint(Point point) {
		
		Point result = new Point();
		
		result.ec  = point.ec;
		result.mod = point.mod;
		
		// 3*x^2+a
		
		BigInteger diffNumerator = 
				point.x.pow(2).multiply(BigInteger.valueOf(3)).add(point.ec.getA());
		
		// 2*y
		
		BigInteger diffDenominator = point.y.multiply(BigInteger.valueOf(2));
		
		if(diffNumerator.compareTo(BigInteger.ZERO) < 0) {
			diffNumerator.add(point.mod);
		}
		
		if(diffDenominator.compareTo(BigInteger.ZERO) < 0) {
			diffDenominator.add(point.mod);
		}
		
		// s = (3*x^2+a)/2*y
		
		BigInteger s = 
				diffNumerator.multiply(diffDenominator.modInverse(point.mod))
				.mod(point.mod);
		
		// x = s^2 - 2*x
		result.x = s.pow(2).subtract(point.x.multiply(BigInteger.valueOf(2)))
				.mod(point.mod);
		
		// y = s*(point.x-result.x)-point.y
		result.y = s.multiply(point.x.subtract(result.x)).subtract(point.y)
				.mod(point.mod);
		
		if(result.x.compareTo(BigInteger.ZERO) < 0) {
			result.x.add(result.mod);
		}
		
		if(result.y.compareTo(BigInteger.ZERO) < 0) {
			result.y.add(result.mod);
		}
		
		return result;
	
	}
	
	public static Point multiply(BigInteger k, Point p) {
		
		Point b = p;
		
		k = k.subtract(BigInteger.ONE);
		
		while(!k.equals(BigInteger.ZERO)) {
				
			if(!k.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
				
				if(b.x.equals(p.x) || b.y.equals(p.y)) {
					b = doublePoint(b);
				} else {
					b = addPoints(b, p);
				}
				
				k = k.subtract(BigInteger.ONE);
				
			} else {
				
				b = doublePoint(b);
				k = k.shiftRight(1);
				
			}
			
		}
		
		return b;
		
	}
	
	public String toString() {
		
		return "[" + x + " " + y + "]";
		
	}
		
}
