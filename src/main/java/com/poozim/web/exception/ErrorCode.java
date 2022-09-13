package com.poozim.web.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
	INVALID_INPUT_VALUE(400, "ER001", "입력값이 올바르지 않습니다."),
	METHOD_NOT_ALLOWED(405, "ER002", "지원하지 않는 요청 메서드입니다."),
	HANDLE_ACCESS_DENIED(403, "ER003", "접근 권한이 없습니다."),
	INTERNAL_SERVER_ERROR(500, "ER004", "INTERNAL SERVER ERROR"),
	INIT_SERVER_ERROR(500, "ER005", "서버 초기 세팅 에러"),
	
	HEADER_NOT_FOUND(400, "ER006", "필수 헤더값이 존재하지 않습니다."),
	PREAUTH_NOT_FOUND(400, "ER007", "사전 인증값이 존재하지 않습니다."),
	BUCKET_NOT_FOUND(400, "ER008", "버킷명이 존재하지 않습니다."),
	
	OBJECT_UPLOAD_ERROR(500, "ER009", "오브젝트 업로드에 실패했습니다."),
	
	PROPERTIES_READ_ERROR(500, "ER010", "프로퍼티 파일 읽기를 실패했습니다.");
	;
	
	private final String code;
    private final String message;
    private int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
