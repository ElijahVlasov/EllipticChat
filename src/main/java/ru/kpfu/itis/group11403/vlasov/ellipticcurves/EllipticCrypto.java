package ru.kpfu.itis.group11403.vlasov.ellipticcurves;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

import org.apache.commons.lang3.SerializationUtils;

public abstract class EllipticCrypto {

	public final static CryptoInfo P192;	

	/**
	 * Сгенерировать закрытый ключ.
	 * @param info Информация шифрования
	 * @return Закрытый ключ или null, если входные данные некоректны
	 */
	
	public static BigInteger generatePrivateKey(CryptoInfo info) {
		
		if(info == null) {
			return null;
		}
		
		BigInteger d;
		
		do { // Генерируем рандомные числа пока не попадут в интервал (0;info.order)
			d = new BigInteger(info.order.bitCount(), new Random());
		} while((d.compareTo(BigInteger.ZERO) < 0) || (d.compareTo(info.order) > 0));
		
		return d;
		
	}
	
	/**
	 * Сгенерировать открытый ключ по закрытому.
	 * @param privateKey Закрытый ключ
	 * @param info Информация шифрования
	 * @return Открытый ключ или null, если аругменты некоректные
	 */
	
	public static Point generateOpenKey(BigInteger privateKey, CryptoInfo info) {
		
		if(privateKey == null || info == null) {
			return null;
		}
		
		return Point.multiply(privateKey, info.generator);
		
	}
	
	
	/**
	 * Получить общий закрытый ключ
	 * @param privateKey
	 * @param publicKey
	 * @return
	 */
	
	public static Point generateCommonPrivateKey(BigInteger privateKey, Point publicKey) {
		
		if(privateKey == null || publicKey == null) {
			return null;
		}
		
		return Point.multiply(privateKey, publicKey);
		
	}

	/**
	 * Зашифровать сообщение 
	 * @param message Сообщение
	 * @param recipientPublicKey Открытый ключ получателя
	 * @param info Данные шифрования
	 * @return Зашифрованное сообщение или null в случае некоретных входных данных
	 */
	
	public static byte[] encrypt(byte[] message, Point recipientPublicKey, CryptoInfo info) {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		if(message == null || recipientPublicKey == null || info == null) {
			return null;
		}
		
		CryptedByte[] cryptedBytes = new CryptedByte[message.length];
		
		// Шифруем каждый байт
		
		for (int i = 0; i < cryptedBytes.length; i++) {

			CryptedByte cb = encryptByte(message[i], recipientPublicKey, info);

			cryptedBytes[i] = cb;
			
		}
		
		SerializationUtils.serialize(cryptedBytes, os);
		
		return os.toByteArray();

	}
	
	public static byte[] decrypt(byte[] message, BigInteger privateKey, CryptoInfo info) {
		
		if(message == null || privateKey == null || info == null) {
			return null;
		}
		
		CryptedByte[] cryptedBytes = SerializationUtils.deserialize(message);
		
		return decrypt(cryptedBytes, privateKey, info);
		
	}
	
	public static byte[] decrypt(CryptedByte[] cryptedBytes, BigInteger privateKey, CryptoInfo info) {
		
		if(cryptedBytes == null || privateKey == null || info == null) {
			return null;
		}
		
		ByteArrayOutputStream decodedBytesStream = new ByteArrayOutputStream();
		
		// Дешифруем каждый байт
		
		for (CryptedByte cryptedByte : cryptedBytes) {
			
			// После десериализации эти поля null,
			// поэтому устанавливаем значения
			
			cryptedByte.hint.setEllipticCurve(info.ec);
			cryptedByte.hint.setMod(info.ec.getMod());
			
			decodedBytesStream.write(decryptByte(cryptedByte, privateKey, info));
			
		}
		
		return decodedBytesStream.toByteArray();
		
		
	}

	private static CryptedByte encryptByte(byte b, Point recipientPublicKey, CryptoInfo info) {
		
		Random rand = new Random();
		
		CryptedByte cb = new CryptedByte();
		
		BigInteger k;
		
		do { // Получаем рандомное число попадающее в интервал (0;order-1)
			
			k = new BigInteger(info.order.bitCount(), rand);
			
		} while((k.compareTo(BigInteger.ZERO) < 0) || (k.compareTo(info.order) > 0));
		
		Point kG = Point.multiply(k, info.generator);
		Point kP = Point.multiply(k, recipientPublicKey);
		
		// При отрицательных b могут быть проблемы при обычном кастовании
		// Поэтому раcширяем до long таким образом
		long longValue = b & 0xff;
		
		cb.hint    = kG;
		cb.crypted = kP.getX().multiply(BigInteger.valueOf(longValue)).mod(info.ec.getMod());
		
		return cb;
		
	}
	
	private static byte decryptByte(CryptedByte cb, BigInteger privateKey, CryptoInfo info) {
		
		// D = privateKey * cb.crypted
		Point d = Point.multiply(privateKey, cb.hint);
				
		// c * D_x^(-1)
		BigInteger decodedByte = cb.crypted.multiply(d.getX().modInverse(info.ec.getMod()));
		
		byte[] ar = decodedByte.mod(info.ec.getMod()).toByteArray(); 
	
		return ar[ar.length - 1];
		
	}
	
	public static class CryptedByte implements Serializable {
		
		private Point hint;
		private BigInteger crypted;
		
		public Point getHint() {
			return hint;
		}
		
		public void setHint(Point crypted) {
			this.hint = crypted;
		}
		
		public BigInteger getCrypted() {
			return crypted;
		}
		
		public void setCrypted(BigInteger hint) {
			this.crypted = crypted;
		}
		
	}
	
	public static class CryptoInfo {
		
		/**
		 * Эллиптическая кривая
		 */
		
		private EllipticCurve ec;
		
		/**
		 * Генератор группы точек эллиптической кривой
		 */
		
		private Point generator;
		
		/**
		 * Порядок эллиптической кривой
		 */
		private BigInteger order;
		
		public CryptoInfo(EllipticCurve ec, Point generator, BigInteger order) {
			this.ec = ec;
			this.generator = generator;
			this.order = order;
		}

		public EllipticCurve getEc() {
			return ec;
		}

		public void setEc(EllipticCurve ec) {
			this.ec = ec;
		}

		public Point getGenerator() {
			return generator;
		}

		public void setGenerator(Point generator) {
			this.generator = generator;
		}

		public BigInteger getOrder() {
			return order;
		}

		public void setOrder(BigInteger order) {
			this.order = order;
		}
		
	}
	
	static {
		
		EllipticCurve ecP192 = 
			new EllipticCurve(
				BigInteger.valueOf(-3), 
				new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16),	
				new BigInteger("6277101735386680763835789423207666416083908700390324961279")
			);
		
		P192 = new CryptoInfo(
				ecP192, 
				
				new Point(
					new BigInteger("188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012", 16),
					new BigInteger("07192b95ffc8da78631011ed6b24cdd573f977a11e794811",16),
					ecP192
				),
				
				new BigInteger("6277101735386680763835789423176059013767194773182842284081")
			  );
		
	}
	
}
