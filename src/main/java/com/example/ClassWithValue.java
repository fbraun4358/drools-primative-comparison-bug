package com.example;

public class ClassWithValue {

	private int valueInteger;
	private double valueDouble;
	private float valueFloat;
	private short valueShort;
	private byte valueByte;
	
	public int getValueInteger() {
		return valueInteger;
	}
	public void setValueInteger(int valueInt) {
		this.valueInteger = valueInt;
	}
	public double getValueDouble() {
		return valueDouble;
	}
	public void setValueDouble(double valueDouble) {
		this.valueDouble = valueDouble;
	}
	public float getValueFloat() {
		return valueFloat;
	}
	public void setValueFloat(float valueFloat) {
		this.valueFloat = valueFloat;
	}
	public short getValueShort() {
		return valueShort;
	}
	public void setValueShort(short valueShort) {
		this.valueShort = valueShort;
	}
	public byte getValueByte() {
		return valueByte;
	}
	public void setValueByte(byte valueByte) {
		this.valueByte = valueByte;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + valueByte;
		long temp;
		temp = Double.doubleToLongBits(valueDouble);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(valueFloat);
		result = prime * result + valueInteger;
		result = prime * result + valueShort;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassWithValue other = (ClassWithValue) obj;
		if (valueByte != other.valueByte)
			return false;
		if (Double.doubleToLongBits(valueDouble) != Double.doubleToLongBits(other.valueDouble))
			return false;
		if (Float.floatToIntBits(valueFloat) != Float.floatToIntBits(other.valueFloat))
			return false;
		if (valueInteger != other.valueInteger)
			return false;
		if (valueShort != other.valueShort)
			return false;
		return true;
	}
}
