package com.bit.kodari.repository.user;

<<<<<<< HEAD
=======
import com.bit.kodari.dto.UserDto;
import org.apache.catalina.User;
>>>>>>> 5a93f64ea64c75bfd230c134d1cd73db1ed8c6c3
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class UserRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    UserSql userSql;
    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
<<<<<<< HEAD
//    // User 레코드 추가
//    public User.RegisterRes insert(User.RegisterReq user) {
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        SqlParameterSource parameterSource = new MapSqlParameterSource("name", user.getName())
//                .addValue("nickName", user.getNickName())
//                .addValue("profileImgUrl", user.getProfileImgUrl())
//                .addValue("email", user.getEmail())
//                .addValue("password", user.getPassword())
//                .addValue("phoneNumber", user.getPhoneNumber())
//                .addValue("deleteYN", "N");
//        int affectedRows = namedParameterJdbcTemplate.update(userSql.INSERT, parameterSource, keyHolder);
//        return User.RegisterRes.builder().name(user.getName()).build();
//    }
=======
    // User 레코드 추가
    public User.RegisterRes insert(User.RegisterReq user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource("name", user.getName())
                .addValue("nickName", user.getNickName())
                .addValue("profileImgUrl", user.getProfileImgUrl())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("phoneNumber", user.getPhoneNumber())
                .addValue("deleteYN", "N");
        int affectedRows = namedParameterJdbcTemplate.update(userSql.INSERT, parameterSource, keyHolder);
        return User.RegisterRes.builder().name(user.getName()).build();
    }
>>>>>>> 5a93f64ea64c75bfd230c134d1cd73db1ed8c6c3
}
