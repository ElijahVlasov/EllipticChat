package ru.kpfu.itis.group11403.vlasov.ellipticcurves;

import java.math.BigInteger;

public class EllipticCurve {

	private final BigInteger a;
	private final BigInteger b;
	private final BigInteger mod;
	
	public EllipticCurve(BigInteger a, BigInteger b, BigInteger mod) {
	
		this.a = a;
		this.b = b;
		this.mod = mod;
		
	}

	public BigInteger getA() {
		return a;
	}

	public BigInteger getB() {
		return b;
	}

	public BigInteger getMod() {
		return mod;
	}
	
}
