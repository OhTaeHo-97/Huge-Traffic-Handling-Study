package com.example.fastcampusmysql.domain.post;

import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.util.PostFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.stream.IntStream;

@SpringBootTest
public class PostBulkInsertTest {
    @Autowired
    private PostRepository postRepository;

    // SpringBootTest로 돌기 때문에 Spring Boot Server(ApplicationContext)가 뜰 것이고
    // 거기서 PostRepository를 주입받아서 직접 save까지 진행해볼 것임
    @Test
    public void bulkInsert() {
        var easyRandom = PostFixtureFactory.get(
                2L,
                LocalDate.of(1970, 1, 1),
                LocalDate.of(2023, 2, 1)
        );

        // 100만건을 넣어볼 것임 -> 그렇다보니 100만건만큼(루프 도는 횟수만큼) save()가 호출됨
        // -> 그래서 MySQL의 Bulk Insert 기능을 이용해서 건건이 나가는 쿼리를 하나로 모아서 나갈 수 있도록 구현해볼 것임
//        IntStream.range(0, 5)
//                .mapToObj(i -> easyRandom.nextObject(Post.class))
//                .forEach(x ->
//                        postRepository.save(x)
//                );

        // 실제로 batch 같은 것을 만들 때, JPA를 쓰고 있어도 JdbcTemplate을 많이 사용함
        //  -> bulk insert 때문에
        //  JPA -> saveAll()이 save() 루프를 돌면서 나감
        // Spring Data JPA를 사용하면 리스트를 받는 insert가 saveAll()인데, 그것은 save()를 루프를 돌면서 호출
        //  -> 그래서 PK가 auto increment인 경우에는 단건으로 하나씩 나감
        //  -> 그래서 Bulk Insert 기능 때문에 JdbcTemplate을 사용

        var stopWatch = new StopWatch();
        stopWatch.start();

        // 우리는 100만건을 넣어야 하는데 단건으로 돌리면 객체 만드는 데에만 해도 시간이 오래 걸림
        // 그래서 parallel()로 돌릴 것임, 시간을 재볼 것임
        var posts = IntStream.range(0, 1_000_000)
                .parallel()
                .mapToObj(i -> easyRandom.nextObject(Post.class))
                .toList();

        stopWatch.stop();
        System.out.println("객체 생성 시간 = " + stopWatch.getTotalTimeSeconds());

        var queryStopWatch = new StopWatch();
        queryStopWatch.start();

        // 100만건 row를 insert하는 것은 DB 입장에서 엄청난 부하
        //  -> bulk insert할 때 실제로 DB CPU가 100만건이 들어왔을 때 얼마나 튀게 되는지 간단히 커맨드나 작업관리자 같은 것으로 볼 것임
        postRepository.bulkInsert(posts);
        queryStopWatch.stop();

        // Edit Custom VM Option을 바꿈 -> IntelliJ 설정
        //  -> Gradle로 돌리면 해당 메모리 설정을 못 먹고 default 설정인 500MB로 잡힘
        //  -> Preferences에서 gradle 검색하고 Build, Execution, Deployment -> BuildTools -> Gradle에서 Run Tests using을 IntelliJ로 설정해야 설정한 메모리 설정을 사용
        System.out.println("DB insert 시간 : " + queryStopWatch.getTotalTimeSeconds());
    }

    // 100만건이어서 메모리 설정이 필요
    //  -> cmd + shift + a 눌러서 Edit Custom VM Option 누르기
    //  -> -Xmx2048m 이라는 글씨가 보일텐데, 2048m이 메모리 크기를 의미
}
// 100만건 넣는 테스트는 다양한 방법으로 해볼 수 있음
//  -> 지금 우리가 하고 있는 것은 굉장히 원초적인 방법
//  -> 쿼리로 짤 수도 있고, Ngrinder 같은 것을 이용해서 API단부터 부하를 줄 수도 있음
//  -> 지금 우리는 DB 강의여서 원초적으로 JdbcTemplate을 이용해서 넣고 있음
//      -> 이것도 최적화하는 방법들이 여러가지가 있음
//      -> 100만건 dummy data 쌓는 것도 최적화해보고, API 부하 테스트도 해보는 것이 좋다!