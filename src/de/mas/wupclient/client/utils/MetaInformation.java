package de.mas.wupclient.client.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MetaInformation implements Comparable<MetaInformation>, Serializable{
	private static final long serialVersionUID = 1L;
	
	private long titleID;
	private String longnameEN;
	private String ID6;
	private String product_code;
	private String content_platform;
	private String company_code;
	private int region;
	
	public enum Region{
		EUR,
		USA,
		JAP,
		UKWN
	}

	public MetaInformation(long titleID, String longnameEN, String ID6, String product_code,String content_platform,String company_code,int region) {
		setTitleID(titleID);
		setLongnameEN(longnameEN);
		setID6(ID6);	
		setProduct_code(product_code);
		setCompany_code(company_code);
		setContent_platform(content_platform);
		setRegion(region);
	}

	public MetaInformation() {
	    
	}	

	public Region getRegionAsRegion() {		
		switch (region) {
        	case 1:  return Region.JAP;                 
        	case 2:  return  Region.USA;
        	case 4:  return  Region.EUR;
        	default: return  Region.UKWN;
		}
	}

	public String getContent_platform() {
		return content_platform;
	}

	public void setContent_platform(String content_platform) {
		this.content_platform = content_platform;
	}

	public String getCompany_code() {
		return company_code;
	}

	public void setCompany_code(String company_code) {
		this.company_code = company_code;
	}

	public String getProduct_code() {
		return product_code;
	}

	public void setProduct_code(String product_code) {
		this.product_code = product_code;
	}

	public long getTitleID() {
		return titleID;
	}

	public void setTitleID(long titleID) {
		this.titleID = titleID;
	}

	public String getLongnameEN() {
		return longnameEN;
	}

	public void setLongnameEN(String longnameEN) {
		this.longnameEN = longnameEN;
	}

	public String getID6() {
		return ID6;
	}

	public void setID6(String iD6) {
		ID6 = iD6;
	}	
	
	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public String getTitleIDAsString() {
		return String.format("%08X-%08X", titleID>>32,titleID<<32>>32);
		
	}
	
	@Override
	public String toString(){
		String result =  getTitleIDAsString() + ";" + region +";" + getContent_platform() + ";" + getCompany_code() + ";"+ getProduct_code()+ ";" + getID6() + ";" + getLongnameEN();
		for(Integer i :versions){
			result += ";" + i;
		}
		return result;
	}

	@Override
	public int compareTo(MetaInformation o) {
		return getLongnameEN().compareTo(o.getLongnameEN());
	}

	public void init(MetaInformation n) {
		setTitleID(n.getTitleID());
		setRegion(n.region);
		setCompany_code(n.company_code);
		setContent_platform(n.content_platform);
		setID6(n.ID6);
		setLongnameEN(n.longnameEN);
		setProduct_code(n.product_code);
	}

	@Override
	public boolean equals(Object o){		
		return titleID == ((MetaInformation)o).titleID;
	}
}
