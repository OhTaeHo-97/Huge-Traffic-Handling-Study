package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostReadService {
    private final PostRepository postRepository;

    // 일자별 게시물 개수 반환 메서드
    public List<DailyPostCount> getDailyPostCounts(DailyPostCountRequest request) {
        /*
            반환값 -> 리스트(작성일시, 작성회원, 작성게시물 개수)

            select createdDate, memberId, count(id)
            from posts
            where memberId = :memberId and createdDate between firstDate and lastDate
            group by createdDate memberId
         */
        return postRepository.groupByCreatedDate(request);
    }

    // 페이징에 대한 정보가 필요
    //  - size나 몇 번째 페이지에 대한 요청인지, 정렬 정보 등
    //  -> Spring Data에서 제공해주는 인터페이스인 PageRequest를 사용할 것임
    //      -> Spring Data JPA에서도 많이 사용
    //      -> 생성자를 보면 page, size, sort가 존재
    //          -> sort : 어떤 방식으로 정렬을 해서 페이징을 할 것인지
    //  반환 객체도 Spring Data에서 제공해주는 인터페이스인 Page 인터페이스를 이용
    //      -> totalPages, totalElements 외에 내부에 객체를 매핑할 수 있는 map() 함수와 empty 여부 정도가 있음
    //  offset 관리 : PageRequest 객체를 사용하겍 되면 파라미터로 page, size만 주면 됨
    //      -> 그럼 자체적으로 offset을 관리해줌
    //      -> PageRequest이 구현한 AbstractPageRequest로 들어가보면 getOffset()이라는 함수가 있음
    //          -> offset이 저절로 반환됨
    public Page<Post> getPosts(Long memberId, Pageable pageRequest) {
        // PostRepository의 함수를 호출하는 것
        // 사실상 proxy 역할만 할 것이고 PostRepository로 가서 함수를 구현할 것임
        return postRepository.findAllByMemberId(memberId, pageRequest);
    }

    public CursorResponse<Post> getPosts(Long memberId, CursorRequest cursorRequest) {
        // 응답값
        //  -> Page 객체를 만들기 위해서는 totalElements가 필요하니 count 쿼리를 썼음
        //  -> 그래서 cursor 방식에 맞는 객체를 하나 만들어주는 것이 좋다!

        // CursorReqeust를 받는 레포지토리 함수가 있어야 함
        //  -> CursorRequest의 key는 null이 될 수 있음
        //      -> null일 때와 아닐 때의 분기가 필요함

        var posts = findAllBy(memberId, cursorRequest);

        // PageCursor -> nextKey를 만들어서 줘야 함
        //  -> 내가 가져왔던 데이터 중 가장 마지막 key 값, 우리는 최신순으로 하고 있기 때문에 가장 작은 PK 값을 주면 됨
        //  -> 그럼 다음 요청으로 그것이 들어올 것이고 그것보다 작은 것을 읽어주면 쭉 데이터가 읽어질 것이므로
        // 조회했더니 empty list일 수도 있으니 그럴 경우에는 NONE_KEY값을 준다!
        var nextKey = getNextKey(posts);

        return new CursorResponse<>(cursorRequest.next(nextKey), posts);
    }

    public CursorResponse<Post> getPosts(List<Long> memberIds, CursorRequest cursorRequest) {
        var posts = findAllBy(memberIds,cursorRequest);
        var nextKey = getNextKey(posts);

        return new CursorResponse<>(cursorRequest.next(nextKey), posts);
    }

    public List<Post> getPosts(List<Long> ids) {
        return postRepository.findAllByInId(ids);
    }

    private List<Post> findAllBy(Long memberId, CursorRequest cursorRequest) {
        if(cursorRequest.hasKey()) {
            return postRepository.findAllByLessThanIdAndMemberIdAndOrderByIdDesc(cursorRequest.key(), memberId, cursorRequest.size());
        }
        return postRepository.findAllByMemberIdAndOrderByIdDesc(memberId, cursorRequest.size());
    }

    private List<Post> findAllBy(List<Long> memberId, CursorRequest cursorRequest) {
        if(cursorRequest.hasKey()) {
            return postRepository.findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(cursorRequest.key(), memberId, cursorRequest.size());
        }
        return postRepository.findAllByInMemberIdAndOrderByIdDesc(memberId, cursorRequest.size());
    }

    private long getNextKey(List<Post> posts) {
        return posts.stream()
                .mapToLong(Post::getId)
                .min()
                .orElse(CursorRequest.NONE_KEY);
    }
}
