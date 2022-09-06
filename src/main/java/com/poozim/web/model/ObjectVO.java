package com.poozim.web.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.oracle.bmc.objectstorage.model.ObjectSummary;

import lombok.Data;

@Data
public class ObjectVO {
	private String name;
	private long size;
	private String regdate;
	private String moddate;
	
	public ObjectVO convertFromObjectSummary(ObjectSummary objectSummary) {
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		
		this.name = objectSummary.getName();
		this.size = objectSummary.getSize();
		this.regdate = dtFormat.format(objectSummary.getTimeCreated());
		this.regdate = dtFormat.format(objectSummary.getTimeModified());
		return this;
	}
}
