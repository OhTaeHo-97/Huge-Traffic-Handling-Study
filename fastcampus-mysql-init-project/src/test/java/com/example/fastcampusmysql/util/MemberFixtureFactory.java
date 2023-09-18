package com.example.fastcampusmysql.util;

import com.example.fastcampusmysql.domain.member.entity.Member;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

public class MemberFixtureFactory {
    // default seed를 사용하는 메서드
    static public Member create() {
        var param = new EasyRandomParameters();
        return new EasyRandom(param).nextObject(Member.class);
    }

    static public Member create(Long seed) {
        // fixture library의 EasyRandom
        //  -> 오픈소스
        //  -> Java bean을 만들어주는 라이브러리
        //  -> 테스트 코드를 짤 때 굉장히 편함
        //  -> https://github.com/j-easy/easy-random
        //  -> seed(), objectPoolSize(), randomizationDepth(), timeRange() 등을 지정해주면 nextObject() 호출을 통해 값을 랜덤하게 채워서 객체가 만들어짐
        // EasyRandom -> 일정 상태 정도의 star를 받음
        //  -> 넷플리스, JetBrains, Mulesoft 등이 사용하고 있음
        //  -> Netflix 등을 누르면 해당 코드를 사용하고 있는 부분으로 연결됨 -> 이를 통해 큰 기업에서는 테스트 코드를 어떻게 짜는지 알 수 있음
        // EasyRandom 사용 시에 여러 어려움을 겪을 수 있음
        //  -> https://github.com/j-easy/easy-random/wiki
        //  -> 위 사이트를 통해 대략적인 EasyRandom에 대한 설명을 볼 수 있고, Configuration Parameters 부분을 읽어보면 유연하게 사용할 수 있음
        // EasyRandom를 보면
        //  -> 기본 생성자가 있고, 파라미터를 주입받지 않고 만들어줌
        //  -> EasyRandomParameters를 주입받는 생성자가 존재
        //      -> EasyRandomParameters를 통해서 타입별로 어떤 랜덤한 값을 넣어줄 것인지, 범위는 어떻게 정할 것인지 등을 정할 수 있음
        // EasyRandom -> 시드 기반으로 동작
        //  -> 랜덤한 수를 만들 때 시드를 주는 것처럼 시드가 동일한 상태에서 nextObject()를 계속 동일한 시드의 EasyRandom을 만들어서 nextObject()를 호출하면 계속 똑같은 값을 가진 객체가 반환됨
        //  -> 그래서, 이를 공유할 수 있어야 함
        // 시드를 다르게 줘야 함
        //  -> EasyRandomParameters 내부를 들어가보면 seed라는 값이 있음
        //  -> seed는 setSeed()를 통해 설정 가능
        //  -> setSeed()는 void 타입인데, seed() 메서드는 EasyRandomParameters를 반환하므로 이를 사용(setSeed()를 진행하고 해당 객체를 그대로 반환)
        // EasyRandom의 좋은 점 -> org.jeasy.random 안에 들어가보면 randomizers라는 패키지가 보임
        //  -> 여기서 여러가지 객체를 Randomizer로 쓸 수 있음!
        var param = new EasyRandomParameters().seed(seed);
        return new EasyRandom(param).nextObject(Member.class);
    }
}
