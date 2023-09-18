package com.example.fastcampusmysql.domain.member.repository;

import com.example.fastcampusmysql.domain.member.dto.MemberDto;
import com.example.fastcampusmysql.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    // JDBCTemplate을 이용할 것임 -> 외부에서 주입받아야 함
    // NamedParameterJdbcTemplate -> JdbcTemplate을 상속하고 있는 것은 아니고 composite 형태로 JdbcTemplate을 들고 있음
    //  -> 그러므로 JdbcTemplate에서 꺼내와서 값을 직접 넣어줘야 함
    final private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    // 테이블 이름이 MemberRepository에 메서드가 추가될 때마다 사용될 것이므로 이를 상수로 뽑아낼 것임
    static final private String TABLE = "Member";
    private final RowMapper<Member> ROW_MAPPER = (ResultSet resultSet, int rowNum) -> Member.builder()
            .id(resultSet.getLong("id"))
            .email(resultSet.getString("email"))
            .nickname(resultSet.getString("nickname"))
            .birthday(resultSet.getObject("birthday", LocalDate.class))
            .createdAt(resultSet.getObject("createdAt", LocalDateTime.class))
            .build();

    public Optional<Member> findById(Long id) {
        /*
            select *
            from Member
            where id = : id
         */

        // NamedParameterJdbcTemplate을 보면 많은 함수들이 존재
        // 우리는 query()라는 메서드를 활용해볼 것임 -> query()도 많은 메서드 시그니처를 갖고 있음
        // 우리는 qeury(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)을 이용할 것임

        // formatter를 이용하여 SQL 작성 -> %s 자리에 TABLE에 해당하는 값이 들어갈 것임
        var sql = String.format("SELECT * FROM %s WHERE id = :id", TABLE);
        // SqlParameterSource를 insert()에서는 BeanPropertySqlParameterSource를 사용했음
        // SqlParameterSource 내부를 들어가 구현체를 보면 크게 3가지가 있음
        //  - BeanPropertySqlParameterSource, EmptySqlParameterSource, MapSqlParameterSource
        //  - 이번에 사용할 것은 MapSqlParameterSource
        //      -> 객체를 받는 것이 아니고 Long id 하나만 받는 것이기 때문에 BeanPropertySqlParameterSource는 과하고,
        //      -> 빈 것도 아니기 때문에 EmptySqlParameterSource도 아님
        // .addValue()를 통해 key, value를 넣어주면 위에서의 :id에 우리가 파라미터로 받은 id가 바인딩 될 것임
        var param = new MapSqlParameterSource()
                .addValue("id", id);
        // RowMapper -> interface, Generic으로 선언하고 ResultSet과 rowNumber를 받아서 우리가 원하는 generic으로 선언한 객체로 만들어주는 것
        //  -> 하나의 함수만 구현하면 되는 것이므로 FunctionalInterface라고 되어있음 -> 람다식으로 구현해도 무방하다
        // JPA를 사용하면 RowMapper를 작성할 일이 없음 -> JPA가 이런 일들을 해주므로
        //  -> JPA 설정에 보면 naming 전략 룰을 설정할 수 있음
        //  -> entity 이름과 table 명을 특정 룰에 따라 매핑해주는 방법
        // 우리는 entity 이름과 테이블 이름이 같으니 그대로 넣어주면 됨
        // ResultSet.getObject() -> String과 같은 general한 함수들은 있지만 우리가 만든 클래스나 특정 타입에 대해 항상 이러한 get() 함수를 만들어놓을 수는 없음
        //      -> 그래서 Generic 타입을 이용해 컬럼 라벨과 추출하기를 원하는 클래스 레퍼런스를 넣어주면 그것으로 변환해주는 역할을 하는 함수
        // RowMapper를 아래와 같이 직접 구현할 수도 있지만, RowMapper를 따라가다보면 BeanPropertyRowMapper라는 것이 있음
        //  -> 이를 사용하면 mapping 로직을 없앨 수 있음
        //  -> BeanPropertyRowMapper를 사용하려면 setter를 모두 열어줘야 함 -> 모든 필드에 대해서 모두 setter를 열어줘야 하니 사용시에 고민해보고 사용해야 함
//        RowMapper<Member> rowMapper = (ResultSet resultSet, int rowNum) -> Member.builder()
//                .id(resultSet.getLong("id"))
//                .email(resultSet.getString("email"))
//                .nickname(resultSet.getString("nickname"))
//                .birthday(resultSet.getObject("birthday", LocalDate.class))
//                .createdAt(resultSet.getObject("createdAt", LocalDateTime.class))
//                .build();
        // NamedParameterJdbcTemplate에 가보면 단일 T 객체를 반환하는 query 함수가 없음
        //  ->queryForObject()를 사용해볼 수 있을 것임
        //  -> 혹은 List를 반환받는 query()를 사용한 다음에 DataAccessUtils의 singleResult() 함수를 사용
        //      -> 이렇게 사용해도 동일하게 동작할 것임
        var member = namedParameterJdbcTemplate.queryForObject(sql, param, ROW_MAPPER);
        return Optional.ofNullable(member);

//        List<Member> members = namedParameterJdbcTemplate.query(sql, param, rowMapper);
//
//        // jdbcTemplate.query()의 결과 사이즈가 0이면 null, 2 이상이면 예외
//        Member nullableMember = DataAccessUtils.singleResult(members);
//        return Optional.ofNullable(nullableMember);
    }

    public List<Member> findAllByIdIn(List<Long> ids) {
        // ids가 빈 List가 넘어오면 문제가 발생!
        //  -> sql문이 SELECT * FROM Member WHERE id in () 이런 식으로 되면서 SQL 파싱에 실패하게 됨
        //      -> in절에 아무 것도 안 들어가는데 빈 괄호가 들어가서 문제가 생김
        //  -> 그래서 빈 리스트 처리를 꼭 해줘야 한다!
        if(ids.isEmpty())
            return List.of();

        var sql = String.format("SELECT * FROM %s WHERE id in (:ids)", TABLE);
        var params = new MapSqlParameterSource().addValue("ids", ids);
        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public Member save(Member member) {
        /*
            member id를 보고 갱신 또는 삽입을 정함
            반환값은 id를 담아서 반환한다
         */

        if(member.getId() == null) {
            return insert(member);
        }
        return update(member);

//        return Member.builder().build();
//        return member;
    }

    // JPA interface에 맞출 것이므로 save() 역할은 memberId 값을 보고 갱신 또는 삽입을 정함
    // 두 가지의 Private 함수가 필요

    private Member insert(Member member) {
        // 고민해야 될 부분
        // JPA -> 아이디 값이 없는 Member를 받아서 insert를 하면 id값을 담아서 반환해줌
        // 이를 어떻게 처리할까?
        // SimpleJdbcInsert -> 이를 이용하면 insert 쿼리 후에 id를 담아오는 것을 쉽게 구현할 수 있음
        //  JdbcTemplate으로도 구현 가능하다(NamedParameterJdbcTemplate을 들어가보면 KeyHolder라는 객체를 확인할 수 있음, 이를 통해 받아올 수 있다)
        //      -> 코드가 지저분해지고 실수의 여지가 많아짐
        //      -> 그래서 SimpleJdbcInsert를 이용
        // SimpleJdbcInsert 생성자를 보면 DataSource 또는 JdbcTemplate을 받음
        // JdbcTemplate은 DataSource를 들고있기 때문에 테이블을 정해줘야 함
        //  -> withTableName()을 통해 테이블 이름을 지정해줄 수 있음
        //  -> usingGeneratedKeyColumns()를 통해 key를 어떤 column으로부터 가져올 것인지 명시해줄 수 있음
        // 아직 테이블을 안 만들어주었기 때문에 테이블을 만들어줘야 함
        //  -> schema/ddl.sql에 member 테이블에 대한 create문이 만들어져있으니 이를 통해 database에 테이블을 만든다
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName("Member")
                .usingGeneratedKeyColumns("id");

        // SimpleJdbcInsert의 인터페이스를 보면 execute() 함수가 있음
        //  -> Map을 받거나 SqlParameterSource를 받음
        // 이러한 파라미터 형식에 맞춰서 객체를 넘겨주고 실행하면 됨
        // Bean으로 SqlParameterSource를 만들어주는 것이 있음
        //  -> BeanPropertySqlParameterSource() 내부에 POJO 객체를 넣어주면 인터페이스에 맞는 파라미터를 추출해서 만들어줌
        // execute() 함수는 return하는 것인 int(insert 쿼리 나가는 것과 같음)
        // executeAndReturnKey()를 사용하면 반환값으로 key를 받을 수 있음
        SqlParameterSource params = new BeanPropertySqlParameterSource(member);
        var id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        // member에 id를 set해도 되지만, 한 번 만들어진 객체의 id값은 불변이기 때문에
        // 새로 객체를 Immutable하게 만들어서 반환
        return Member.builder().
                id(id)
                .email(member.getEmail())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .createdAt(member.getCreatedAt())
                .build();
    }

    // 이것은 이번 챕터에서 다루지 않을 것임
    private Member update(Member member) {
        // TODO : implemented
        var sql = String.format("UPDATE %s SET email = :email, nickname = :nickname, birthday = :birthday WHERE id = :id", TABLE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(member);
        // NamedParameterJdbcTemplate.update() -> 반환값은 int
        //  -> 조건으로 영향받은 row의 수를 반환할 것임
        //  -> product level이었다면 반환받을 int 값을 확인했곘지만 우선 넘어감
        namedParameterJdbcTemplate.update(sql, params);
        return member;
    }
}
