package com.financeai.dto.response;

public record AuthResponse(String token, Long userId, String name, String email) {}
