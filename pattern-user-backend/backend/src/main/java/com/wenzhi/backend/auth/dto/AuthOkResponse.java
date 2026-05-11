package com.wenzhi.backend.auth.dto;

public record AuthOkResponse(boolean ok, String username, String token) {}

