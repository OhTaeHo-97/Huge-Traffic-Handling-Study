package com.example.fastcampusmysql.util;

// 기존에는 페이지 번호, 사이즈를 받았지만
// 커서 기반 페이징에서는 커서 키와 사이즈를 받을 것임

// 클라이언트가 처음 데이터를 요청할 때에는 최초 서버 접근이니 key가 없을 것임
//  -> 그래서 default key를 설정하는 것이 좋다!
//  -> default key는 클라이언트가 서버에 처음 접근할 때에는 key가 없이 들어오는 것이 제일 편할 것임
//  -> 그래서 default key는 null로 하겠다!
//  -> 우리가 key를 읽었을 때, client에서는 마지막 페이지인지는 알 필요는 없지만, 마지막 데이터인지는 알아야 더이상 스크롤 했을 때 서버에 요청하지 않을 것임
//      -> 그래서 더이상 데이터가 없다는 것을 나타내는 것도 있으면 좋을 것임
//          -> 데이터가 더이상 없어라고 하는 키는 우리는 PK를 key로 쓰고 있으므로 -1로 두겠다
//              -> 어차피 -1은 PK로 가질 수 없으니
//  1번 키가 오고 나서 1번 이후의 데이터를 줄 것인데 그 다음에 클라이언트가 사용할 키를 넘겨줘야 함
public record CursorRequest(
        Long key,
        int size
) {
    public static final Long NONE_KEY = -1L;
    // 메서드로 뺀 이유
    //  -> 키만 바뀌고 사이즈는 동일할 것이어서 메서드에서 간단하게 만들면 됨
    public CursorRequest next(Long key) {
        return new CursorRequest(key, size);
    }

    // key를 가지고 있느냐 아니냐의 정책이 바뀔 수 있음
    // 예를 들어 null로 key값을 주는 것이 아니라 client에서 시작값을 주겠다라고 할 수도 있음
    //  - 그래서 계속 바뀔 수 있음
    //  - 그런데 이러한 로직이 여러 Service에 들어가버리면 유지보수 측면에서 좋지 않다
    //  -> 그래서 여기서 제공해주는 것이 좋다!
    public boolean hasKey() {
        return key != null;
    }
}
