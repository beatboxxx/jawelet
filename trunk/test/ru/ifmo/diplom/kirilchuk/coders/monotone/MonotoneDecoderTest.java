package ru.ifmo.diplom.kirilchuk.coders.monotone;

import org.junit.Before;
import org.junit.Test;

import ru.ifmo.diplom.kirilchuk.coders.StringBitInput;

import static org.junit.Assert.*;

public class MonotoneDecoderTest {

	private MonotoneDecoder decoder;
	
	@Before
	public void before() {
		decoder = new MonotoneDecoder();
	}
	
	@Test
	public void testDecodingPositive() throws Exception {
		decodeWithAssert("101", 1);
		decodeWithAssert("11111010101", 21);
	}
	
	@Test
	public void testDecodingNegative() throws Exception {
		decodeWithAssert("001", -1);
		decodeWithAssert("01111010101", -21);
	}
	
	private void decodeWithAssert(String value, int expect) throws Exception {
		StringBitInput in = new StringBitInput(value);
		int result = decoder.decodeNext(in);
		assertEquals(expect, result);
	}
}