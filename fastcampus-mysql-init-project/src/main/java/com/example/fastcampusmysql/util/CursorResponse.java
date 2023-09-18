package com.example.fastcampusmysql.util;

import java.util.List;

public record CursorResponse<T>(
        CursorRequest nextCursorRequest, // 클라이언트가 다음에 요청할 키
        List<T> contents
) {

}
