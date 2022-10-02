package com.poozim.web.example;

import lombok.Data;

@Data
public class StorageResponse<E> {
	private int status;
	private boolean success;
	private E data;
	private String msg;
}
