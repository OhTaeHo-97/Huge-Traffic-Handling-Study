package com.example.fastcampusmysql.domain.post.repository;

import com.example.fastcampusmysql.util.PageHelper;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class PostRepository {
    static final String TABLE = "Post";

    final private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    final static private RowMapper<DailyPostCount> DAILY_POST_COUNT_MAPPER = (ResultSet resultSet, int rowNum) -> new DailyPostCount(
            resultSet.getLong("memberId"),
            resultSet.getObject("createdDate", LocalDate.class),
            resultSet.getLong("count")
    );

    final static private RowMapper<Post> ROW_MAPPER = (ResultSet resultSet, int rowNum) -> Post.builder()
            .id(resultSet.getLong("id"))
            .memberId(resultSet.getLong("memberId"))
            .contents(resultSet.getString("contents"))
            .createdDate(resultSet.getObject("createdDate", LocalDate.class))
            .likeCount(resultSet.getLong("likeCount"))
            .version(resultSet.getLong("version"))
            .createdAt(resultSet.getObject("createdAt", LocalDateTime.class))
            .build();

    public List<DailyPostCount> groupByCreatedDate(DailyPostCountRequest request) {
        // 이 쿼리가 데이터가 많아진다면 문제가 발생한다
        //  -> 성능이 확 느려짐
        // 데이터를 많이 쌓아두는 코드 -> 100만건 정도 되는 데이터를 삽입하는 작업을 EasyRandom을 통해 해볼 것임
        var sql = String.format("SELECT createdDate, memberId, count(id) as count " +
                "FROM %s " +
                "WHERE memberId = :memberId AND createdDate BETWEEN :firstDate AND :lastDate " +
                "GROUP BY memberId, createdDate", TABLE);

        var params = new BeanPropertySqlParameterSource(request);

        return namedParameterJdbcTemplate.query(sql, params, DAILY_POST_COUNT_MAPPER);
    }

    // PageRequest -> Pageable을 구현한 객체
    public Page<Post> findAllByMemberId(Long memberId, Pageable pageable) {
        var sql = String.format("SELECT * " +
                "FROM %s " +
                "WHERE memberId = :memberId " +
                "ORDER BY %s " +
                "LIMIT :size " +
                "OFFSET :offset", TABLE, PageHelper.orderBy(pageable.getSort()));

        System.out.println(PageHelper.orderBy(pageable.getSort()));

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        // 정렬
        //  - Pageable에 Sort라는 객체가 있음(sort하는 것)
        //  - Sort
        //      - 여러 프로퍼티를 가지지만 direction과 properties를 받는다는 것이 중요!
        //      - properties : 필드(id로 정렬할 것인지, 이름으로 정렬할 것인지 그런 필드)
        //      - direction : 오름차순으로 정렬할지 내림차순으로 정렬할지에 관한 것
        //          - 위 2개가 주요한 지표
        //      - 실제로 사용할 떄에는 Sort에 Sort를 연결해서 사용하기도 함
        //          - Sort에 and() 연산이 있음 -> 이를 통해 Sort 2개 연산을 붙이기도 한다!
        //          - 그래서 쿼리(String)로 사용할 수 있게 변환해주는 코드를 짜야 한다!
        //  -> PageHelper 같은 클래스를 하나 만들어서 구현!
        // Sort 같은 경우 파라미터로 받지는 않음
        //  -> Sort에 어떤 파라미터가 들어가냐에 따라서 index를 타냐 못타냐가 결정됨
        //  -> 그래서 Sort를 동적으로 사용하려면 MySQL보다 다른 DB를 같이 씀
        //  -> 쇼핑몰 같은 경우는 정렬 기준을 몇 개 정해놓고 그것을 받을 수는 있을 것임
        // 실제 서비스에서는 정렬을 파라미터로 주입 받을 것인지에 대해서는 MySQL만 사용한다면 고민이 필요한 부분!

        var posts = namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
        // 만들어야 할 객체 -> Page<Post>
        // Page 객체를 만들기 위해 필요한 것
        //  -> Page : Slice를 상속받고 있음
        //      -> Slice : 많은 필드를 갖고 있음
        //  -> Page, Slice 모두 인터페이스
        //      -> 우리가 담을 구현체가 필요하다!
        //      -> PageImpl을 이용!
        //          -> Spring Data에서 Page 인터페이스를 구현하는 기본 구현체도 제공해주고 있음
        //      -> PageImpl -> content, total, Pageable 이렇게 3개를 인자로 받음
        //          -> content가 Generic 타입이니까 쿼리로 가져온 posts를 content로 넘겨주면 됨
        //          -> Pageable : 컨텐츠를 가지고 올 때 요청받은 PageRequest를 넘겨주면 됨(PageRequest가 Pageable의 구현)
        //          -> total : 전체 개수(전체 개수를 알기 위해 count 쿼리가 필요할 것임)
        return new PageImpl<>(posts, pageable, getCount(memberId));

        // 프론트와의 규약을 어떻게 가져가느냐에 따라 다른데
        // 위에서 받은 pageable을 바로 넘겨주는데 이렇게 할 수도 있고
        // pageable에 대한 다음 페이지 요청을 넘겨줄 수도 있음
    }

    public Optional<Post> findById(Long postId, boolean requiredLock) {
        var sql = String.format("SELECT * FROM %s WHERE id = :postId", TABLE);
        if(requiredLock) {
            sql += " FOR UPDATE";
        }
        System.out.println(sql);
        var params = new MapSqlParameterSource()
                .addValue("postId", postId);

        // queryForObject() -> DataAccessUtils.nullableSingleResult()를 해줌!
        //  - 그래서 직접 사용하지 않아도 queryForObject()를 통해 가능하다!
        var nullablePost = namedParameterJdbcTemplate.queryForObject(sql, params, ROW_MAPPER);
        return Optional.ofNullable(nullablePost);
    }

    private Long getCount(Long memberId) {
        var sql = String.format("SELECT count(id) " +
                "FROM %s " +
                "WHERE memberId = :memberId", TABLE);

        var param = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return namedParameterJdbcTemplate.queryForObject(sql, param, Long.class);
    }

    public List<Post> findAllByMemberIdAndOrderByIdDesc(Long memberId, int size) {
        var sql = String.format("SELECT * " +
                "FROM %s " +
                "WHERE memberId = :memberId " +
                "ORDER BY id DESC " +
                "LIMIT :size", TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInId(List<Long> ids) {
        if(ids.isEmpty()) {
            return List.of();
        }

        var sql = String.format("SELECT * FROM %s WHERE id in (:ids)", TABLE);
        var params = new MapSqlParameterSource()
                .addValue("ids", ids);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByInMemberIdAndOrderByIdDesc(List<Long> memberIds, int size) {
        if(memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("SELECT * " +
                "FROM %s " +
                "WHERE memberId in (:memberIds) " +
                "ORDER BY id DESC " +
                "LIMIT :size", TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    // key를 가지고 key보다 작은 것들을 조회하는 repository 함수
    public List<Post> findAllByLessThanIdAndMemberIdAndOrderByIdDesc(Long id, Long memberId, int size) {
        var sql = String.format("SELECT * " +
                "FROM %s " +
                "WHERE memberId = :memberId AND id < :id " +
                "ORDER BY id DESC " +
                "LIMIT :size", TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("size", size)
                .addValue("id", id);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public List<Post> findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(Long id, List<Long> memberIds, int size) {
        if(memberIds.isEmpty()) {
            return List.of();
        }

        var sql = String.format("SELECT * " +
                "FROM %s " +
                "WHERE memberId in (:memberIds) AND id < :id " +
                "ORDER BY id DESC " +
                "LIMIT :size", TABLE);

        var params = new MapSqlParameterSource()
                .addValue("memberIds", memberIds)
                .addValue("size", size)
                .addValue("id", id);

        return namedParameterJdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public Post save(Post post) {
        if(post.getId() == null)
            return insert(post);
        return update(post);
//        throw new UnsupportedOperationException("Post는 갱신을 지원하지 않습니다.");
    }

    // 쿼리들을 모아서 한 번에 보내는 Bulk Insert 기능 구현
    // insert 쿼리를 날릴 것인데 VALUES()로 보낼 것이고, 그럼 쿼리로 보면 VALUES()가 여러 개가 들어갈 것임
    //  -> VALUES() 쪽에 List를 바인딩해줄 수 있어야 함
    //      -> SqlParameterSource의 List로 파라미터를 넘겨주고 batchUpdate()라는 함수를 부르면 리스트가 바인딩됨
    public void bulkInsert(List<Post> posts) {
        var sql = String.format("INSERT INTO %s (memberId, contents, createdDate, createdAt) " +
                "VALUES (:memberId, :contents, :createdDate, :createdAt)", TABLE);

        SqlParameterSource[] params = posts.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, params);
    }

    private Post insert(Post post) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        System.out.println(post.getMemberId());

        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        var id = jdbcInsert.executeAndReturnKey(params).longValue();

        return Post.builder()
                .id(id)
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .createdDate(post.getCreatedDate())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private Post update(Post post) {
        // JPA를 사용하면 업데이트 쿼리가 아래와 같이 나감(@DynamicUpdate 어노테이션을 사용하지 않는다면)
        var sql = String.format("UPDATE %s SET " +
                    "memberId = :memberId, " +
                    "contents = :contents, " +
                    "createdDate = :createdDate, " +
                    "likeCount = :likeCount, " +
                    "createdAt = :createdAt, " +
                    "version = :version + 1 " +
                "WHERE id = :id AND version = :version", TABLE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(post);
        var updatedCount = namedParameterJdbcTemplate.update(sql, params);

        if(updatedCount == 0) {
            throw new RuntimeException("갱신 실패");
        }
        return post;

    }
}
