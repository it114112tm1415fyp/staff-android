package it114112fyp.util;

public class AddressItem {
	public int addressId;
	public String shortAddress;
	public String longAddress;
	public int regionId;
	public String region;

	public AddressItem(int addressId, String shortAddress, String longAddress, int regionId, String region) {
		this.addressId = addressId;
		this.shortAddress = shortAddress;
		this.longAddress = longAddress;
		this.regionId = regionId;
		this.region = region;
	}

	public int getAddressId() {
		return addressId;
	}

	public String getShortAddress() {
		return shortAddress;
	}

	public String getLongAddress() {
		return longAddress;
	}

	public int getRegionId() {
		return regionId;
	}

	public String getRegion() {
		return region;
	}

}