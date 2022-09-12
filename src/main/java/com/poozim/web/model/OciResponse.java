package com.poozim.web.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OciResponse <E> {
	private int status;
	private boolean success;
	private E data;
	private String msg;
}
