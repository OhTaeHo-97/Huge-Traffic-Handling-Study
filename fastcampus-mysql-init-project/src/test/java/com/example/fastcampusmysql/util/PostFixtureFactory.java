package com.example.fastcampusmysql.util;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.time.LocalDate;

import static org.jeasy.random.FieldPredicates.*;

public class PostFixtureFactory {
    // MemberFixtureFactory에서는 EasyRandom 파라미터를 계속해서 new로 만들고 있음
    // 혹은 seed를 받아서 계속 만들어주고 있음

    static public EasyRandom get(Long memberId, LocalDate firstDate, LocalDate lastDate) {
        // 원하는 것 -> PostFixtureFactory get()으로 받은 EasyRandom에서는 memberId가 고정이고, firstDate와 lastDate 사이의 데이터를 반환하도록 만들 것임
        // https://github.com/j-easy/easy-random : document
        //  - EasyRandom wiki의 사용 예제를 보면 dateRange()를 지정해줄 수 있음
        //  - 파라미터(EasyRandomParameters)로 주입받은 EasyRandom을 사용하게 되면 dateRange()에 해당되는 값들만 나오게 된다는 것!
        //      -> Date는 이를 통해 범위를 제한해줄 수 있을 것임
        //  - memberId를 고정시킬 수 있어야 함 -> randomize()를 호출해서 해당 타입과 이 타입이 반환할 값을 넣는 람다함수를 받을 수 있음
        //      -> randomize() 함수를 사용해서 memberId 필드는 주입받은 memberId를 반환하도록 하면 될 것임
        //  - 필드명도 정의할 수 있음
        //      -> excludeField() 함수를 살펴보자
        //          - age라는 이름을 가진 Integer 필드를 랜덤값에서 제외한다는 뜻
        //              - inClass()를 통해 어떤 클래스의 age인지 나타냄

        /*
        public static Predicate<Field> named(final String name) {
            final Pattern pattern = Pattern.compile(name);
            return field -> pattern.matcher(field.getName()).matches();
        }
        -> specification을 그대로 구현한 것
        -> 비동기 JPA specification도 거의 비슷한 형태(QueryDSL도 마찬가지)
        -> specification pattern을 한 번 찾아보자
         */
        var idPredicate = named("id")
                .and(ofType(Long.class))
                .and(inClass(Post.class));

        var memberIdPredicate = named("memberId")
                .and(ofType(Long.class))
                .and(inClass(Post.class));

        // id는 null이 나올 것이고 나머지는 다 랜덤하게 나올 것임
        var param = new EasyRandomParameters()
                .excludeField(idPredicate)
                .dateRange(firstDate, lastDate)
                .randomize(memberIdPredicate, () -> memberId);

        return new EasyRandom(param);
    }
}
